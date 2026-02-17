"""
Behavioral tests for governance operation metadata in data endpoints.

Validates that dRepVoteDelegation and poolGovernanceVote operations
returned by /block, /block/transaction, and /search/transactions
have the correct metadata structure.

"""

import re

import allure
import pytest


def is_hex(s, length=None):
    """Check if string is valid hex, optionally of exact length."""
    if length and len(s) != length:
        return False
    return bool(re.fullmatch(r"[0-9a-f]+", s))


def find_ops_by_type(transaction, op_type):
    """Find all operations of a given type in a transaction."""
    return [op for op in transaction["operations"] if op["type"] == op_type]


# ---------------------------------------------------------------------------
# dRepVoteDelegation
# ---------------------------------------------------------------------------


@allure.feature("Block")
@allure.story("Governance Metadata")
class TestDRepVoteDelegationMetadata:
    """dRepVoteDelegation operations must have correct metadata.drep structure."""

    @pytest.fixture()
    def drep_test_data(self, network_data):
        gov = network_data.get("governance", {})
        return gov.get("drep_vote_delegation", {})

    @pytest.mark.parametrize(
        "variant",
        ["key_hash", "script_hash", "abstain", "no_confidence"],
    )
    def test_drep_delegation_metadata_via_block(
        self, client, drep_test_data, variant
    ):
        """Fetch block containing dRepVoteDelegation and validate metadata.drep shape."""
        data = drep_test_data.get(variant)
        if not data:
            pytest.skip(f"No test data for drep variant '{variant}'")

        response = client.block(block_identifier={"index": data["block"]})
        assert response.status_code == 200

        block = response.json()["block"]
        tx = next(
            (
                t
                for t in block["transactions"]
                if t["transaction_identifier"]["hash"] == data["tx_hash"]
            ),
            None,
        )
        assert tx is not None, f"Transaction {data['tx_hash']} not found in block {data['block']}"

        ops = find_ops_by_type(tx, "dRepVoteDelegation")
        assert len(ops) >= 1, "No dRepVoteDelegation operations found"

        op = ops[0]
        assert op["status"] == "success"
        assert op["account"]["address"].startswith("stake_")

        drep = op["metadata"]["drep"]
        assert drep["type"] == variant

        if variant in ("key_hash", "script_hash"):
            assert "id" in drep, f"drep.id missing for type {variant}"
            assert is_hex(drep["id"], 56), f"drep.id not valid 56-char hex: {drep['id']}"
        else:
            assert "id" not in drep, f"drep.id should not be present for type {variant}"

    @pytest.mark.parametrize(
        "variant",
        ["key_hash", "script_hash", "abstain", "no_confidence"],
    )
    def test_drep_delegation_metadata_via_search(
        self, client, drep_test_data, variant
    ):
        """Same assertions via /search/transactions by tx hash."""
        data = drep_test_data.get(variant)
        if not data:
            pytest.skip(f"No test data for drep variant '{variant}'")

        response = client.search_transactions(
            transaction_identifier={"hash": data["tx_hash"]}
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) >= 1, f"Transaction {data['tx_hash']} not found via /search/transactions"
        tx = txs[0]["transaction"]

        ops = find_ops_by_type(tx, "dRepVoteDelegation")
        assert len(ops) >= 1, "No dRepVoteDelegation operations found in search result"

        op = ops[0]
        assert op["status"] == "success"

        drep = op["metadata"]["drep"]
        assert drep["type"] == variant

        if variant in ("key_hash", "script_hash"):
            assert is_hex(drep["id"], 56), f"drep.id not valid 56-char hex: {drep['id']}"
        else:
            assert "id" not in drep, f"drep.id should not be present for type {variant}"


# ---------------------------------------------------------------------------
# poolGovernanceVote
# ---------------------------------------------------------------------------


@allure.feature("Block")
@allure.story("Governance Metadata")
class TestPoolGovernanceVoteMetadata:
    """poolGovernanceVote operations must have correct metadata.poolGovernanceVoteParams structure."""

    @pytest.fixture()
    def vote_test_data(self, network_data):
        gov = network_data.get("governance", {})
        return gov.get("pool_governance_vote", {})

    @pytest.mark.parametrize("variant", ["yes", "no"])
    def test_pool_vote_metadata_via_block(self, client, vote_test_data, variant):
        """Fetch block containing poolGovernanceVote and validate metadata shape."""
        data = vote_test_data.get(variant)
        if not data:
            pytest.skip(f"No test data for vote variant '{variant}'")

        response = client.block(block_identifier={"index": data["block"]})
        assert response.status_code == 200

        block = response.json()["block"]
        tx = next(
            (
                t
                for t in block["transactions"]
                if t["transaction_identifier"]["hash"] == data["tx_hash"]
            ),
            None,
        )
        assert tx is not None, f"Transaction {data['tx_hash']} not found in block {data['block']}"

        ops = find_ops_by_type(tx, "poolGovernanceVote")
        assert len(ops) >= 1, "No poolGovernanceVote operations found"

        for op in ops:
            assert op["status"] == "success"

            params = op["metadata"]["poolGovernanceVoteParams"]
            assert is_hex(params["governance_action_hash"]), "governance_action_hash not hex"
            assert params["pool_credential"]["curve_type"] == "edwards25519"
            assert is_hex(params["pool_credential"]["hex_bytes"], 56)
            assert params["vote"] in ("yes", "no", "abstain")
            assert params["vote"] == variant

            rationale = params.get("vote_rationale")
            if rationale is not None:
                assert "url" in rationale
                assert "data_hash" in rationale
                assert is_hex(rationale["data_hash"], 64)

    @pytest.mark.parametrize("variant", ["yes", "no"])
    def test_pool_vote_metadata_via_search(self, client, vote_test_data, variant):
        """Same assertions via /search/transactions by tx hash."""
        data = vote_test_data.get(variant)
        if not data:
            pytest.skip(f"No test data for vote variant '{variant}'")

        response = client.search_transactions(
            transaction_identifier={"hash": data["tx_hash"]}
        )
        assert response.status_code == 200

        txs = response.json()["transactions"]
        assert len(txs) >= 1, f"Transaction {data['tx_hash']} not found via /search/transactions"
        tx = txs[0]["transaction"]

        ops = find_ops_by_type(tx, "poolGovernanceVote")
        assert len(ops) >= 1, "No poolGovernanceVote operations found in search result"

        for op in ops:
            params = op["metadata"]["poolGovernanceVoteParams"]
            assert is_hex(params["governance_action_hash"])
            assert params["pool_credential"]["curve_type"] == "edwards25519"
            assert is_hex(params["pool_credential"]["hex_bytes"], 56)
            assert params["vote"] == variant

    def test_multiple_votes_in_single_tx(self, client, vote_test_data):
        """A single transaction can contain multiple poolGovernanceVote operations."""
        data = vote_test_data.get("multiple_votes")
        if not data:
            pytest.skip("No test data for multiple_votes")

        response = client.block(block_identifier={"index": data["block"]})
        assert response.status_code == 200

        block = response.json()["block"]
        tx = next(
            (
                t
                for t in block["transactions"]
                if t["transaction_identifier"]["hash"] == data["tx_hash"]
            ),
            None,
        )
        assert tx is not None

        ops = find_ops_by_type(tx, "poolGovernanceVote")
        assert len(ops) >= 2, f"Expected multiple votes, got {len(ops)}"

        # Each vote must have distinct pool_credential
        credentials = {op["metadata"]["poolGovernanceVoteParams"]["pool_credential"]["hex_bytes"] for op in ops}
        assert len(credentials) == len(ops), "Expected distinct pool credentials per vote"

        # All must reference the same governance action
        actions = {op["metadata"]["poolGovernanceVoteParams"]["governance_action_hash"] for op in ops}
        assert len(actions) == 1, f"Expected same governance action, got {len(actions)}"
