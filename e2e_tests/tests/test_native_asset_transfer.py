import logging
import pytest

from e2e_tests.test_utils.utxo_selector import UtxoSelector

logger = logging.getLogger(__name__)


def _extract_token_bundle_from_utxo(utxo: dict) -> list:
    """
    Extract the tokenBundle list from an account/coins UTXO entry.

    Supports the Rosetta representation where coin.metadata is a map keyed by
    the UTXO identifier. Falls back to legacy shapes if necessary.
    """
    md = utxo.get("metadata") or {}
    utxo_id = utxo.get("coin_identifier", {}).get("identifier")
    if utxo_id and isinstance(md.get(utxo_id), list):
        return md[utxo_id]
    # Fallback (legacy): a flat assets list
    if isinstance(md.get("assets"), list):
        # Convert a legacy flat list into a tokenBundle-like list if possible
        # Expect entries like { policyId, tokens: [...] }
        return md["assets"]
    return []


def _negate_token_bundle_values(bundle: list) -> list:
    """Return a deep-copied tokenBundle list with token values negated (for inputs)."""
    out = []
    for policy in bundle:
        if not isinstance(policy, dict):
            continue
        tokens = []
        for t in policy.get("tokens", []) or []:
            try:
                v = int(str(t.get("value", "0")))
            except Exception:
                v = 0
            tokens.append({
                "value": str(-v),
                "currency": t.get("currency")
            })
        out.append({
            "policyId": policy.get("policyId"),
            "tokens": tokens
        })
    return out


@pytest.mark.e2e
def test_native_asset_self_transfer(
    rosetta_client,
    test_wallet,
    transaction_orchestrator,
    signing_handler,
    utxo_selector,
):
    """
    E2E: Spend a UTXO carrying native tokens and recreate the token bundle in an output to self.

    - Select a UTXO with a tokenBundle (native asset)
    - Add one ADA-only UTXO to comfortably pay the fee
    - Create input ops for both UTXOs; attach tokenBundle metadata only to the asset input
    - Create two outputs to self: one reproducing the token bundle with its ADA; one change output for remaining ADA
    - Build, sign, submit, and wait for confirmation
    - Verify the confirmed transaction includes the tokenBundle on an output
    """
    sender_addr = test_wallet.get_address()

    # 1) Select asset UTXO
    asset_utxo = UtxoSelector.find_first_asset_utxo(rosetta_client, sender_addr)
    asset_utxo_id = asset_utxo["coin_identifier"]["identifier"]
    asset_utxo_ada = int(asset_utxo["amount"]["value"])  # ADA contained in the asset UTXO
    token_bundle = _extract_token_bundle_from_utxo(asset_utxo)
    assert token_bundle, "Selected UTXO must contain token bundle"

    logger.info(f"Using asset UTXO {asset_utxo_id} with {asset_utxo_ada} lovelace and token bundle")

    # 2) Add an ADA-only UTXO to cover fee and keep change positive
    ada_only_inputs = utxo_selector.select_utxos(
        rosetta_client,
        sender_addr,
        required_amount=5_000_000,
        exclude_utxos=[asset_utxo_id],
        strategy="single",
        utxo_count=1,
    )
    ada_utxo = ada_only_inputs[0]
    ada_utxo_id = ada_utxo["coin_identifier"]["identifier"]
    ada_utxo_ada = int(ada_utxo["amount"]["value"])  # ADA-only amount

    total_input_ada = asset_utxo_ada + ada_utxo_ada

    # 3) Build operations
    ops = []

    # Input: asset UTXO (ADA negative; tokenBundle values negative)
    ops.append({
        "operation_identifier": {"index": len(ops)},
        "type": "input",
        "account": {"address": sender_addr},
        "amount": {"value": f"-{asset_utxo_ada}", "currency": {"symbol": "ADA", "decimals": 6}},
        "coin_change": {
            "coin_identifier": {"identifier": asset_utxo_id},
            "coin_action": "coin_spent",
        },
        "metadata": {
            "tokenBundle": _negate_token_bundle_values(token_bundle)
        },
    })

    # Input: ADA-only UTXO
    ops.append({
        "operation_identifier": {"index": len(ops)},
        "type": "input",
        "account": {"address": sender_addr},
        "amount": {"value": f"-{ada_utxo_ada}", "currency": {"symbol": "ADA", "decimals": 6}},
        "coin_change": {
            "coin_identifier": {"identifier": ada_utxo_id},
            "coin_action": "coin_spent",
        },
    })

    # Output: token bundle to self, ADA equals the ADA in the asset UTXO
    ops.append({
        "operation_identifier": {"index": len(ops)},
        "type": "output",
        "account": {"address": sender_addr},
        "amount": {"value": str(asset_utxo_ada), "currency": {"symbol": "ADA", "decimals": 6}},
        "metadata": {
            "tokenBundle": token_bundle
        },
    })

    # Output: change to self, ADA is remaining; fee will be deducted by orchestrator
    change_ada = total_input_ada - asset_utxo_ada
    assert change_ada > 0, "Change must be positive before fee deduction"
    ops.append({
        "operation_identifier": {"index": len(ops)},
        "type": "output",
        "account": {"address": sender_addr},
        "amount": {"value": str(change_ada), "currency": {"symbol": "ADA", "decimals": 6}},
    })

    # 4) Build unsigned tx (let orchestrator fetch suggested fee and adjust change)
    unsigned_tx, payloads, metadata, fee = transaction_orchestrator.build_transaction(ops)
    logger.info(f"Unsigned tx built. Suggested fee: {fee} lovelace")

    # 5) Sign and submit; wait for confirmation
    sign_fn = signing_handler.get_signing_function()  # payment key
    tx_hash, tx_details = transaction_orchestrator.sign_and_submit(
        unsigned_transaction=unsigned_tx,
        payloads=payloads,
        signing_function=sign_fn,
        wait_for_confirmation=True,
        confirmation_timeout=180,
    )

    logger.info(f"Native asset tx confirmed · {tx_hash}")

    # 6) Verify the confirmed tx includes an output with the tokenBundle
    assert tx_details and "transaction" in tx_details, "Missing transaction details after confirmation"
    parsed_ops = tx_details["transaction"].get("operations", [])
    assert parsed_ops, "Confirmed transaction should include operations"

    has_token_output = False
    for op in parsed_ops:
        if op.get("type") == "output":
            md = op.get("metadata") or {}
            tb = md.get("tokenBundle")
            if isinstance(tb, list) and len(tb) > 0:
                has_token_output = True
                break

    assert has_token_output, "Expected an output operation with tokenBundle metadata"

