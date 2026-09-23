#!/usr/bin/env python3
# /// script
# dependencies = [
#     "pycardano>=0.12.0",
#     "blockfrost-python>=0.6.0",
#     "python-dotenv>=0.19.0",
#     "mnemonic>=0.20",
#     "rich>=13.0",
#     "requests>=2.28.0",
# ]
# requires-python = ">=3.11"
# ///
"""
Prepare and validate the e2e test wallet on a Cardano testnet.

Usage:
    uv run prepare_wallet.py check       # Full pre-flight check (UTXOs + on-chain state + .env)
    uv run prepare_wallet.py split       # Split UTXOs into 12+ ADA-only outputs
    uv run prepare_wallet.py mint        # Mint a native token (token bundle UTXO)
    uv run prepare_wallet.py lookup      # Look up governance values for .env
"""
import os
import re
import sys
from functools import lru_cache

import requests as http_requests
from dotenv import load_dotenv
from blockfrost import ApiUrls, ApiError, BlockFrostApi
from pycardano.crypto.bech32 import encode as bech32_encode
from pycardano import (
    Address,
    Asset,
    AssetName,
    BlockFrostChainContext,
    HDWallet,
    MultiAsset,
    Network,
    PaymentExtendedSigningKey,
    ScriptPubkey,
    StakeExtendedSigningKey,
    TransactionBuilder,
    TransactionOutput,
    Value,
    min_lovelace,
)
from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich import box

console = Console()

load_dotenv()

# --- Environment ---
NETWORK = os.getenv("CARDANO_NETWORK", "preview")
BLOCKFROST_KEY = os.getenv("BLOCKFROST_PROJECT_ID") or os.getenv("BLOCKFROST_API_KEY")
MNEMONIC = os.getenv("TEST_WALLET_MNEMONIC")

# --- Constants ---
MIN_ADA_ONLY_UTXOS = 11
MIN_ADA_FOR_FEES = 5_000_000  # 5 ADA in lovelace
# 500 ADA pool deposit plus headroom for the rest of the suite.
MIN_E2E_ADA_ONLY_LOVELACE = 525_000_000
SPLIT_OUTPUT_LOVELACE = 25_000_000  # 25 ADA per split output
SPLIT_NUM_OUTPUTS = 12
SPLIT_FEE_HEADROOM = 2_000_000  # ~2 ADA headroom for fees
MAX_INPUTS = 20  # cap inputs to avoid exceeding max tx size
MINT_AMOUNT = 1_000_000
MINT_TOKEN_NAME = b"E2ETestToken"


# ── Shared helpers ──────────────────────────────────────────────────────────


def get_blockfrost_url(network: str) -> str:
    n = network.lower()
    if n in ("preprod", "testnet"):
        return ApiUrls.preprod.value
    if n == "preview":
        return ApiUrls.preview.value
    if n == "mainnet":
        return ApiUrls.mainnet.value
    console.print(f"[bold red]ERROR:[/] Unsupported network: {network}")
    sys.exit(1)


def resolve_network(network: str) -> Network:
    if network.lower() in ("mainnet",):
        return Network.MAINNET
    return Network.TESTNET


def is_hex(value: str) -> bool:
    return bool(re.fullmatch(r"[0-9a-fA-F]+", value))


def load_wallet():
    """Load wallet from mnemonic. Returns (payment_skey, payment_vkey, stake_vkey, address)."""
    try:
        hd = HDWallet.from_mnemonic(MNEMONIC)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Invalid mnemonic: {e}")
        sys.exit(1)

    pay_sk = PaymentExtendedSigningKey.from_hdwallet(
        hd.derive_from_path("m/1852'/1815'/0'/0/0")
    )
    pay_vk = pay_sk.to_verification_key()

    stake_sk = StakeExtendedSigningKey.from_hdwallet(
        hd.derive_from_path("m/1852'/1815'/0'/2/0")
    )
    stake_vk = stake_sk.to_verification_key()

    addr = Address(
        payment_part=pay_vk.hash(),
        staking_part=stake_vk.hash(),
        network=resolve_network(NETWORK),
    )
    return pay_sk, pay_vk, stake_vk, addr


def get_context():
    return BlockFrostChainContext(
        project_id=BLOCKFROST_KEY,
        base_url=get_blockfrost_url(NETWORK),
        network=resolve_network(NETWORK),
    )


def get_api():
    return BlockFrostApi(
        project_id=BLOCKFROST_KEY,
        base_url=get_blockfrost_url(NETWORK),
    )


def blockfrost_get(path: str, **params):
    """Direct REST call to Blockfrost (for endpoints missing from the SDK)."""
    base = get_blockfrost_url(NETWORK)
    url = f"{base}/v0/{path.lstrip('/')}"
    r = http_requests.get(url, headers={"project_id": BLOCKFROST_KEY}, params=params, timeout=30)
    r.raise_for_status()
    return r.json()


def drep_bech32(hex_hash: str, is_script: bool = False) -> str:
    """Convert a raw 28-byte DRep hex hash to CIP-129 bech32 drep ID."""
    prefix = "23" if is_script else "22"
    return bech32_encode("drep", bytes.fromhex(prefix + hex_hash))


def is_ada_only(utxo) -> bool:
    amt = utxo.output.amount
    if isinstance(amt, int):
        return True
    if isinstance(amt, Value):
        return amt.multi_asset is None or len(amt.multi_asset) == 0
    return False


def utxo_lovelace(utxo) -> int:
    amt = utxo.output.amount
    if isinstance(amt, int):
        return amt
    if isinstance(amt, Value):
        return amt.coin
    return 0


def select_ada_utxos(all_utxos, required_lovelace: int, max_count: int = MAX_INPUTS):
    """Select ADA-only UTXOs up to required amount, capped at max_count. Largest first."""
    ada_only = [u for u in all_utxos if is_ada_only(u)]
    ada_only.sort(key=utxo_lovelace, reverse=True)

    selected = []
    total = 0
    for u in ada_only[:max_count]:
        selected.append(u)
        total += utxo_lovelace(u)
        if total >= required_lovelace:
            break
    return selected, total


def analyze_utxos(utxos):
    """Analyze Blockfrost UTXOs."""
    ada_only_count = 0
    ada_only_lovelace = 0
    with_tokens_count = 0
    has_fee_utxo = False
    total_lovelace = 0

    for u in utxos:
        units = [a.unit for a in u.amount]
        lovelace = sum(int(a.quantity) for a in u.amount if a.unit == "lovelace")
        total_lovelace += lovelace
        if len(units) == 1 and units[0] == "lovelace":
            ada_only_count += 1
            ada_only_lovelace += lovelace
            if lovelace >= MIN_ADA_FOR_FEES:
                has_fee_utxo = True
        else:
            with_tokens_count += 1

    return (
        ada_only_count,
        ada_only_lovelace,
        with_tokens_count,
        has_fee_utxo,
        total_lovelace,
    )


# ── Commands ────────────────────────────────────────────────────────────────


# Governance action types that stake pool operators can vote on (CIP-1694).
SPO_VOTABLE_ACTIONS = {
    "hard_fork_initiation",
    "new_committee",
    "no_confidence",
    "info_action",
    "parameter_change",
}

# Pool votes only count for parameter changes touching the security-relevant
# group, so a proposal changing anything else is unusable for the pool vote test.
SECURITY_RELEVANT_PARAMS = {
    "maxBlockBodySize",
    "maxTxSize",
    "maxBlockHeaderSize",
    "maxValueSize",
    "maxBlockExecutionUnits",
    "maxTxExecutionUnits",
    "txFeePerByte",
    "txFeeFixed",
    "utxoCostPerByte",
    "govActionDeposit",
    "minFeeRefScriptCostPerByte",
    "minPoolCost",
}


def spo_can_vote(detail: dict) -> bool:
    """Whether a stake pool can cast a meaningful vote on this proposal."""
    action = detail.get("governance_type")
    if action not in SPO_VOTABLE_ACTIONS:
        return False
    if action != "parameter_change":
        return True
    changed = set()
    for entry in (detail.get("governance_description") or {}).get("contents") or []:
        if isinstance(entry, dict):
            changed |= set(entry)
    return bool(changed & SECURITY_RELEVANT_PARAMS)


@lru_cache(maxsize=1)
def find_active_dreps() -> dict:
    """Discover an active, unexpired key DRep and script DRep.

    The list endpoint already carries has_script, retired and expired, so the
    scan costs one request per hundred DReps and runs to the end of the list.
    With no candidate cap, "not found" cannot mean "stopped looking".

    Returns {"key": hex|None, "script": hex|None}.
    """
    key_hex = script_hex = None
    page = 1
    while not (key_hex and script_hex):
        dreps = blockfrost_get("governance/dreps", count=100, page=page, order="desc")
        if not dreps:
            break
        for entry in dreps:
            if entry.get("retired", False) or entry.get("expired", False):
                continue
            has_script = entry.get("has_script", False)
            if has_script and script_hex:
                continue
            if not has_script and key_hex:
                continue
            raw_hex = entry.get("hex", "")
            prefix = "23" if has_script else "22"
            clean = raw_hex[2:] if raw_hex.startswith(prefix) else raw_hex
            if has_script:
                script_hex = clean
            else:
                key_hex = clean
        page += 1
    return {"key": key_hex, "script": script_hex}


@lru_cache(maxsize=1)
def find_spo_votable_proposal() -> str | None:
    """Discover an open governance proposal a stake pool can vote on.

    The list carries governance_type, so actions no pool can vote on are dropped
    before spending a request on their detail. The remaining candidates are few,
    so the scan runs to the end of the list rather than to a page cap.
    """
    page = 1
    while True:
        proposals = blockfrost_get("governance/proposals", count=100, page=page, order="desc")
        if not proposals:
            return None
        for entry in proposals:
            if entry.get("governance_type") not in SPO_VOTABLE_ACTIONS:
                continue
            tx_hash = entry.get("tx_hash", "")
            cert_index = entry.get("cert_index", 0)
            detail = blockfrost_get(f"governance/proposals/{tx_hash}/{cert_index}")
            is_open = not any([
                detail.get("enacted_epoch"),
                detail.get("dropped_epoch"),
                detail.get("expired_epoch"),
            ])
            if is_open and spo_can_vote(detail):
                return f"{tx_hash}{int(cert_index):02x}"
        page += 1


def find_stake_pool() -> str | None:
    """Discover a stake pool hash usable for the delegation tests."""
    pools = blockfrost_get("pools", count=1, page=1, order="asc")
    if not pools:
        return None
    return blockfrost_get(f"pools/{pools[0]}").get("hex", pools[0])


def drep_unusable_reason(hex_hash: str, is_script: bool) -> str | None:
    """Why this DRep cannot back a vote delegation test, or None when it can.

    Shared with the pytest suite, so the pre-flight table and the test skips can
    never disagree about the same value.
    """
    label = "script" if is_script else "key"
    if not hex_hash:
        return f"DRep {label} hash is not set; run prepare_wallet.py lookup"
    if len(hex_hash) != 56:
        return f"expected a 28-byte hex hash, got {len(hex_hash)} chars"
    try:
        info = blockfrost_get(f"governance/dreps/{drep_bech32(hex_hash, is_script=is_script)}")
    except Exception as exc:
        return f"on-chain lookup failed: {exc}"
    if not info.get("active"):
        return "not active on-chain"
    if info.get("expired"):
        return "expired on-chain; run prepare_wallet.py lookup for a current one"
    if is_script and not info.get("has_script"):
        return "not a script-based DRep"
    return None


def proposal_unusable_reason(proposal_id: str) -> str | None:
    """Why this governance proposal cannot back the pool vote test, or None."""
    if not proposal_id:
        return "POOL_GOVERNANCE_PROPOSAL_ID is not set; run prepare_wallet.py lookup"
    if len(proposal_id) < 66:
        return f"expected tx hash plus cert index, got {len(proposal_id)} chars"
    try:
        info = blockfrost_get(
            f"governance/proposals/{proposal_id[:64]}/{int(proposal_id[64:], 16)}"
        )
    except Exception as exc:
        return f"on-chain lookup failed: {exc}"
    enacted = info.get("enacted_epoch")
    dropped = info.get("dropped_epoch")
    expired = info.get("expired_epoch")
    if any([enacted, dropped, expired]):
        return f"closed (enacted={enacted} dropped={dropped} expired={expired})"
    if not spo_can_vote(info):
        return f"open but not SPO-votable ({info.get('governance_type')})"
    return None


def resolve_drep(is_script: bool) -> tuple[str | None, str | None]:
    """The DRep hash to test with, or the reason there is none.

    Discovery is the default because a DRep's registration expires: pinning it
    in configuration guarantees it goes stale. Setting the environment variable
    overrides discovery for reproducibility, and a pinned value that no longer
    works is reported rather than silently replaced.
    """
    name = "DREP_SCRIPT_HASH_ID" if is_script else "DREP_KEY_HASH_ID"
    kind = "script-based " if is_script else ""
    pinned = (os.getenv(name) or "").strip()
    if pinned:
        reason = drep_unusable_reason(pinned, is_script=is_script)
        return (None, f"{name} is pinned but {reason}") if reason else (pinned, None)

    value = find_active_dreps()["script" if is_script else "key"]
    if value:
        return value, None
    return None, f"no active {kind}DRep exists on {NETWORK}"


def resolve_proposal() -> tuple[str | None, str | None]:
    """The governance proposal to vote on, or the reason there is none."""
    pinned = (os.getenv("POOL_GOVERNANCE_PROPOSAL_ID") or "").strip()
    if pinned:
        reason = proposal_unusable_reason(pinned)
        return (None, f"POOL_GOVERNANCE_PROPOSAL_ID is pinned but {reason}") if reason else (pinned, None)

    found = find_spo_votable_proposal()
    if found:
        return found, None
    return None, f"no open SPO-votable proposal exists on {NETWORK}"


def resolve_stake_pool() -> tuple[str | None, str | None]:
    """The stake pool hash to delegate to, or the reason there is none."""
    pinned = (os.getenv("STAKE_POOL_HASH") or "").strip()
    if pinned:
        return pinned, None
    try:
        found = find_stake_pool()
    except Exception as exc:
        return None, f"stake pool discovery failed: {exc}"
    return (found, None) if found else (None, f"no stake pool found on {NETWORK}")


def cmd_check():
    """Full pre-flight check: UTXOs + on-chain state + .env governance values."""
    _, _, stake_vk, addr = load_wallet()
    address = str(addr)
    stake_addr = str(Address(staking_part=stake_vk.hash(), network=resolve_network(NETWORK)))
    api = get_api()
    errors = []

    console.print(f"\n[bold]Pre-flight check[/] — [bold]{NETWORK}[/]\n")

    # ── Helper ──
    def row(name: str, ok: bool, detail: str):
        status = "[green]PASS[/]" if ok else "[red]FAIL[/]"
        check_table.add_row(name, status, detail)
        if not ok:
            errors.append(name)

    def warn(name: str, detail: str):
        check_table.add_row(name, "[yellow]WARN[/]", detail)

    def require_env(name: str) -> str:
        value = os.getenv(name)
        if not value:
            errors.append(name)
            check_table.add_row(name, "[red]FAIL[/]", "missing from .env")
            return ""
        return value

    # ── 1. Wallet UTXOs ──
    try:
        utxos = api.address_utxos(address, gather_pages=True)
    except ApiError as e:
        console.print(f"[bold red]FAIL:[/] Cannot fetch UTXOs: {e}")
        sys.exit(1)

    if not utxos:
        console.print(f"[bold red]FAIL:[/] No UTXOs found for {address}")
        sys.exit(1)

    (
        ada_only_count,
        ada_only_lovelace,
        with_tokens_count,
        has_fee_utxo,
        total_lovelace,
    ) = analyze_utxos(utxos)

    utxo_table = Table(box=box.SIMPLE_HEAVY, title="Wallet UTXOs")
    utxo_table.add_column("UTXO", style="dim")
    utxo_table.add_column("ADA", justify="right", style="cyan")
    utxo_table.add_column("Tokens", justify="right")

    for u in utxos:
        units = [a.unit for a in u.amount]
        lovelace = sum(int(a.quantity) for a in u.amount if a.unit == "lovelace")
        token_count = len(units) - 1
        if token_count == 0:
            utxo_table.add_row(f"{u.tx_hash[:12]}...#{u.tx_index}", f"{lovelace/1e6:.2f}", "")
        else:
            utxo_table.add_row(f"{u.tx_hash[:12]}...#{u.tx_index}", f"{lovelace/1e6:.2f}", f"[yellow]+{token_count}[/]")

    console.print(utxo_table)
    console.print(f"[bold]Total:[/] {total_lovelace/1e6:.2f} ADA across {len(utxos)} UTXOs\n")

    # ── 2. Checks table ──
    check_table = Table(title="Pre-flight Checks", box=box.ROUNDED, show_lines=False)
    check_table.add_column("Check", style="bold")
    check_table.add_column("Status")
    check_table.add_column("Detail", style="dim")

    row("ADA-only UTXOs", ada_only_count >= MIN_ADA_ONLY_UTXOS, f"{ada_only_count}/{MIN_ADA_ONLY_UTXOS}")
    row(
        "E2E suite funds",
        ada_only_lovelace >= MIN_E2E_ADA_ONLY_LOVELACE,
        f"{ada_only_lovelace / 1e6:.2f}/{MIN_E2E_ADA_ONLY_LOVELACE / 1e6:.2f} ADA-only",
    )
    row("Token bundle UTXO", with_tokens_count >= 1, "found" if with_tokens_count >= 1 else "missing")
    row("Fee UTXO (>= 5 ADA)", has_fee_utxo, "found" if has_fee_utxo else "missing")

    # ── 3. On-chain state: stake key ──
    try:
        acct = api.accounts(stake_addr)
        stake_registered = acct.active
        stake_pool = acct.pool_id
        if stake_registered:
            detail = f"registered, delegated to {stake_pool}" if stake_pool else "registered, not delegated"
            warn("Stake key", detail + " (tests expect clean slate)")
        else:
            row("Stake key", True, "not registered (clean)")
    except ApiError as exc:
        if getattr(exc, "status_code", None) == 404:
            row("Stake key", True, "not registered (clean)")
        else:
            warn("Stake key", f"lookup error: {exc}")

    # ── 4. On-chain state: pool from cert ──
    pool_cert = (os.getenv("POOL_REGISTRATION_CERT") or "").strip()
    if not pool_cert:
        warn("POOL_REGISTRATION_CERT", "not set; certificate test is statically skipped")
    elif is_hex(pool_cert) and len(pool_cert) >= 60:
        # Extract pool key hash from cert CBOR (first 581c = 28-byte hash)
        idx = pool_cert.find("581c")
        if idx < 0:
            row("POOL_REGISTRATION_CERT", True, f"{len(pool_cert)} hex chars")
        else:
            cert_pool_hash = pool_cert[idx + 4 : idx + 4 + 56]
            # The pool endpoint answers 200 for a retired pool too, since it
            # serves the last known record, so registration state is read from
            # the ordered update history instead.
            try:
                updates = blockfrost_get(f"pools/{cert_pool_hash}/updates", count=100)
            except Exception as exc:
                status = getattr(getattr(exc, "response", None), "status_code", None)
                if status == 404:
                    row("Pool from cert", True, f"{cert_pool_hash[:16]}... never registered (fresh)")
                else:
                    warn("Pool from cert", f"{cert_pool_hash[:16]}... lookup failed: {exc}")
                updates = None

            if updates is not None:
                last_action = updates[-1].get("action") if updates else None
                if last_action == "registered":
                    warn("Pool from cert", f"{cert_pool_hash[:16]}... currently registered; "
                                           "the retirement test cleans it up")
                else:
                    row("Pool from cert", True, f"{cert_pool_hash[:16]}... not registered "
                                                f"(last action: {last_action or 'none'})")
    else:
        row("POOL_REGISTRATION_CERT", False, "not valid even-length hex")

    # ── 5. Governance values, resolved the way the suite resolves them ──
    for name, (value, reason) in (
        ("STAKE_POOL_HASH", resolve_stake_pool()),
        ("DREP_KEY_HASH_ID", resolve_drep(is_script=False)),
        ("DREP_SCRIPT_HASH_ID", resolve_drep(is_script=True)),
        ("POOL_GOVERNANCE_PROPOSAL_ID", resolve_proposal()),
    ):
        if value:
            source = "pinned" if os.getenv(name) else "discovered"
            row(name, True, f"{value[:16]}... ({source})")
        else:
            warn(name, f"{reason}; the dependent test will be skipped")

    vote = os.getenv("POOL_VOTE_CHOICE", "yes")
    row("POOL_VOTE_CHOICE", vote in ("yes", "no", "abstain"), vote)

    console.print(check_table)
    console.print()

    if errors:
        hints = []
        if ada_only_count < MIN_ADA_ONLY_UTXOS:
            hints.append(f"Need {MIN_ADA_ONLY_UTXOS - ada_only_count} more ADA-only UTXOs → [bold]uv run prepare_wallet.py split[/]")
        if ada_only_lovelace < MIN_E2E_ADA_ONLY_LOVELACE:
            missing_ada = (MIN_E2E_ADA_ONLY_LOVELACE - ada_only_lovelace) / 1e6
            hints.append(f"Need {missing_ada:.2f} more ADA in ADA-only UTXOs")
        if with_tokens_count < 1:
            hints.append("Need 1 token UTXO → [bold]uv run prepare_wallet.py mint[/]")
        gov_fails = [e for e in errors if e.startswith("DREP_") or e.startswith("POOL_GOVERNANCE")]
        if gov_fails:
            hints.append("Stale governance values → [bold]uv run prepare_wallet.py lookup[/]")
        console.print(Panel(f"[bold red]{len(errors)} check(s) failed[/]" + ("\n" + "\n".join(hints) if hints else ""), border_style="red"))
        sys.exit(1)
    else:
        console.print(Panel("[bold green]All checks passed — ready for e2e tests[/]", border_style="green"))


def cmd_split():
    """Split a large UTXO into 12+ smaller ADA-only ones."""
    pay_sk, _, _, addr = load_wallet()
    context = get_context()

    try:
        all_utxos = context.utxos(addr)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Fetching UTXOs: {e}")
        sys.exit(1)

    required = SPLIT_NUM_OUTPUTS * SPLIT_OUTPUT_LOVELACE + SPLIT_FEE_HEADROOM
    selected, total_ada = select_ada_utxos(all_utxos, required)

    if not selected:
        console.print("[bold red]ERROR:[/] No ADA-only UTXOs found to split.")
        sys.exit(1)
    if total_ada < required:
        console.print(f"[bold red]ERROR:[/] Need at least {required/1e6:.0f} ADA, have {total_ada/1e6:.2f} ADA")
        sys.exit(1)

    console.print(f"Selected {len(selected)} UTXOs with [cyan]{total_ada/1e6:.2f} ADA[/] total")
    console.print(f"Creating {SPLIT_NUM_OUTPUTS} outputs of [cyan]{SPLIT_OUTPUT_LOVELACE/1e6:.0f} ADA[/] each...")

    builder = TransactionBuilder(context)
    for utxo in selected:
        builder.add_input(utxo)
    for _ in range(SPLIT_NUM_OUTPUTS):
        builder.add_output(TransactionOutput(addr, SPLIT_OUTPUT_LOVELACE))

    try:
        tx = builder.build_and_sign(signing_keys=[pay_sk], change_address=addr)
    except Exception as e:
        console.print(f"[bold red]ERROR building transaction:[/] {e}")
        sys.exit(1)

    console.print(f"Transaction built · [dim]{len(tx.to_cbor())} bytes[/]")

    try:
        tx_hash = context.submit_tx(tx)
    except Exception as e:
        console.print(f"[bold red]ERROR submitting transaction:[/] {e}")
        sys.exit(1)

    console.print(Panel(f"TX hash: [bold]{tx_hash}[/]\n\nWait ~20s, then run: [bold]uv run prepare_wallet.py status[/]", title="Submitted", border_style="green"))


def cmd_mint():
    """Mint a native token to create a UTXO with a token bundle."""
    pay_sk, pay_vk, _, addr = load_wallet()
    context = get_context()

    try:
        all_utxos = context.utxos(addr)
    except Exception as e:
        console.print(f"[bold red]ERROR:[/] Fetching UTXOs: {e}")
        sys.exit(1)

    selected, total_ada = select_ada_utxos(all_utxos, 5_000_000, max_count=3)
    if not selected:
        console.print("[bold red]ERROR:[/] No ADA-only UTXOs found for minting.")
        sys.exit(1)

    policy_script = ScriptPubkey(pay_vk.hash())
    policy_id = policy_script.hash()
    token_name = AssetName(MINT_TOKEN_NAME)

    console.print(f"Minting [bold cyan]{MINT_AMOUNT}[/] {token_name.payload.decode()} under policy [dim]{policy_id.payload.hex()}[/]")

    builder = TransactionBuilder(context)
    for utxo in selected:
        builder.add_input(utxo)

    builder.mint = MultiAsset({policy_id: Asset({token_name: MINT_AMOUNT})})
    builder.native_scripts = [policy_script]

    multi_asset = MultiAsset({policy_id: Asset({token_name: MINT_AMOUNT})})
    provisional_output = TransactionOutput(addr, Value(0, multi_asset))
    required_lovelace = min_lovelace(context, output=provisional_output)
    builder.add_output(TransactionOutput(addr, Value(required_lovelace, multi_asset)))

    console.print(f"Token output min lovelace: [cyan]{required_lovelace/1e6:.2f} ADA[/]")

    try:
        tx = builder.build_and_sign(signing_keys=[pay_sk], change_address=addr)
    except Exception as e:
        console.print(f"[bold red]ERROR building transaction:[/] {e}")
        sys.exit(1)

    console.print(f"Transaction built · [dim]{len(tx.to_cbor())} bytes[/]")

    try:
        tx_hash = context.submit_tx(tx)
    except Exception as e:
        console.print(f"[bold red]ERROR submitting transaction:[/] {e}")
        sys.exit(1)

    console.print(Panel(f"TX hash: [bold]{tx_hash}[/]\n\nWait ~20s, then run: [bold]uv run prepare_wallet.py status[/]", title="Submitted", border_style="green"))


def cmd_lookup():
    """Show the governance values the suite would discover on this network."""
    console.print(f"\nLooking up [bold]{NETWORK}[/] network governance data...\n")

    table = Table(title="Governance Values", box=box.ROUNDED, show_lines=True)
    table.add_column("Variable", style="bold", no_wrap=True)
    table.add_column("Value", style="cyan", overflow="fold")

    with console.status("Resolving on-chain values..."):
        rows = [
            ("STAKE_POOL_HASH", resolve_stake_pool()),
            ("DREP_KEY_HASH_ID", resolve_drep(is_script=False)),
            ("DREP_SCRIPT_HASH_ID", resolve_drep(is_script=True)),
            ("POOL_GOVERNANCE_PROPOSAL_ID", resolve_proposal()),
        ]

    for name, (value, reason) in rows:
        table.add_row(name, value or f"[red]{reason}[/]")
    table.add_row("POOL_VOTE_CHOICE", os.getenv("POOL_VOTE_CHOICE", "yes"))

    console.print(table)
    console.print(
        "\n[dim]The suite resolves these the same way at run time; set one only to "
        "pin it.[/]"
    )
    console.print(
        "[dim]POOL_REGISTRATION_CERT needs to be generated separately (pool-specific).[/]"
    )


# ── Main ────────────────────────────────────────────────────────────────────


def cmd_help():
    """Show available commands."""
    console.print("\n[bold]prepare_wallet.py[/] — e2e test wallet manager\n")
    commands = [
        ("check", "Full pre-flight check: UTXOs + on-chain state + .env governance"),
        ("split", "Split a large UTXO into 12+ smaller ADA-only outputs"),
        ("mint", "Mint a native token to create a UTXO with a token bundle"),
        ("lookup", "Look up fresh governance values (DReps, proposals) for .env"),
        ("help", "Show this help"),
    ]
    table = Table(box=box.SIMPLE, show_header=False, padding=(0, 2))
    table.add_column(style="bold cyan")
    table.add_column(style="dim")
    for name, desc in commands:
        table.add_row(name, desc)
    console.print(table)
    console.print("\n[dim]Usage: uv run prepare_wallet.py <command>[/]")
    console.print("[dim]Aliases: status, validate → check[/]\n")


COMMANDS = {
    "check": cmd_check,
    "status": cmd_check,       # alias
    "validate": cmd_check,     # alias
    "split": cmd_split,
    "mint": cmd_mint,
    "lookup": cmd_lookup,
    "help": cmd_help,
}

if __name__ == "__main__":
    if not BLOCKFROST_KEY:
        console.print("[bold red]ERROR:[/] Set BLOCKFROST_PROJECT_ID in .env")
        sys.exit(1)
    if not MNEMONIC:
        console.print("[bold red]ERROR:[/] Set TEST_WALLET_MNEMONIC in .env")
        sys.exit(1)

    cmd = sys.argv[1] if len(sys.argv) > 1 else "check"

    if cmd not in COMMANDS:
        console.print(f"[bold red]Unknown command:[/] {cmd}")
        cmd_help()
        sys.exit(1)

    COMMANDS[cmd]()
