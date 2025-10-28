"""
Tests for Rosetta /block endpoints.

Tests both /block and /block/transaction endpoints with behavioral assertions.
"""

import pytest
import allure
from conftest import get_error_message

pytestmark = pytest.mark.pr


@allure.feature("Block")
@allure.story("Block Lookup")
class TestBlockLookup:
    """Test /block endpoint with different lookup methods."""

    @pytest.mark.pr
    def test_lookup_by_index(self, client, network, blockchain_height):
        """Lookup block by index."""
        # Test at multiple points to ensure it works across blockchain
        test_index = blockchain_height // 2

        response = client.block(network=network, block_identifier={"index": test_index})
        assert response.status_code == 200

        data = response.json()
        block = data["block"]

        # Verify returned block matches requested index
        assert block["block_identifier"]["index"] == test_index

        # Cardano invariant: block hash is 64 hex chars
        block_hash = block["block_identifier"]["hash"]
        assert len(block_hash) == 64, "Block hash must be 64 hex chars"
        assert all(c in "0123456789abcdef" for c in block_hash.lower()), (
            "Block hash must be hexadecimal"
        )

    def test_lookup_by_hash(self, client, network):
        """Lookup block by hash."""
        # Dynamically fetch a block to get a valid hash
        sample_response = client.block(network=network, block_identifier={"index": 100})
        assert sample_response.status_code == 200
        block_hash = sample_response.json()["block"]["block_identifier"]["hash"]

        # Lookup by that hash
        response = client.block(network=network, block_identifier={"hash": block_hash})
        assert response.status_code == 200

        # Verify same block returned
        assert response.json()["block"]["block_identifier"]["hash"] == block_hash

    def test_lookup_by_index_and_hash_matching(self, client, network):
        """Lookup with both index and hash (matching)."""
        # Get a block
        sample_response = client.block(network=network, block_identifier={"index": 100})
        assert sample_response.status_code == 200
        sample_block = sample_response.json()["block"]["block_identifier"]

        # Query with both
        response = client.block(
            network=network,
            block_identifier={
                "index": sample_block["index"],
                "hash": sample_block["hash"],
            },
        )
        assert response.status_code == 200

        block = response.json()["block"]["block_identifier"]
        assert block["index"] == sample_block["index"]
        assert block["hash"] == sample_block["hash"]

    def test_lookup_with_mismatched_index_and_hash_returns_error(self, client, network):
        """Providing both index and hash that don't match should error."""
        # Get a real hash but use wrong index
        sample_response = client.block(network=network, block_identifier={"index": 100})
        real_hash = sample_response.json()["block"]["block_identifier"]["hash"]

        response = client.block(
            network=network,
            block_identifier={"index": 999, "hash": real_hash},  # Mismatched!
        )
        # Should return error
        assert response.status_code == 500


@allure.feature("Block")
@allure.story("Block Structure")
class TestBlockStructure:
    """Test block structure invariants."""

    def test_genesis_block_properties(self, client, network):
        """Genesis block should be at index 0 with parent index -1."""
        response = client.block(network=network, block_identifier={"index": 0})
        assert response.status_code == 200

        block = response.json()["block"]["block_identifier"]
        assert block["index"] == 0, "Genesis must be at index 0"

        # Genesis parent is index -1 (no real parent)
        parent = response.json()["block"]["parent_block_identifier"]
        assert parent["index"] == -1, "Genesis parent index must be -1"

    def test_parent_block_linkage(self, client, network):
        """Block N's parent should be block N-1."""
        # Test at a point well past genesis
        test_index = 1000

        response = client.block(network=network, block_identifier={"index": test_index})
        assert response.status_code == 200

        block = response.json()["block"]
        parent = block["parent_block_identifier"]

        assert parent["index"] == test_index - 1, (
            f"Block {test_index} parent should be {test_index - 1}"
        )

        # Verify parent hash is valid format
        assert len(parent["hash"]) == 64

    def test_empty_blocks_allowed(self, client, network):
        """Empty blocks (0 transactions) are valid in early preprod."""
        # Early blocks likely empty
        response = client.block(network=network, block_identifier={"index": 100})
        assert response.status_code == 200

        block = response.json()["block"]
        # Don't assert transaction count - empty blocks are valid
        assert isinstance(block.get("transactions", []), list)

    def test_block_timestamp_present(self, client, network):
        """Block must have timestamp."""
        response = client.block(network=network, block_identifier={"index": 100})
        assert response.status_code == 200

        timestamp = response.json()["block"]["timestamp"]
        assert isinstance(timestamp, int) and timestamp > 0, (
            "Block timestamp must be positive integer (milliseconds since epoch)"
        )

    def test_block_metadata_structure(self, client, network):
        """Block metadata should contain Cardano-specific fields."""
        response = client.block(network=network, block_identifier={"index": 100})
        assert response.status_code == 200

        metadata = response.json()["block"]["metadata"]

        # Required Cardano metadata fields
        assert "transactionsCount" in metadata
        assert "createdBy" in metadata, "Block must identify creator (pool ID)"
        assert "size" in metadata, "Block must have size"
        assert "epochNo" in metadata, "Block must have epoch number"
        assert "slotNo" in metadata, "Block must have slot number"

        # Validate types and ranges
        assert isinstance(metadata["transactionsCount"], int)
        assert metadata["transactionsCount"] >= 0

        # createdBy is pool ID (56 hex chars = 28 bytes)
        pool_id = metadata["createdBy"]
        assert len(pool_id) == 56, "Pool ID must be 56 hex chars"
        assert all(c in "0123456789abcdef" for c in pool_id.lower())

        assert isinstance(metadata["size"], int) and metadata["size"] > 0
        assert isinstance(metadata["epochNo"], int) and metadata["epochNo"] >= 0
        assert isinstance(metadata["slotNo"], int) and metadata["slotNo"] >= 0


@allure.feature("Block")
@allure.story("Block Errors")
class TestBlockErrors:
    """Test error handling for /block endpoint."""

    def test_invalid_block_index_returns_error(self, client, network):
        """Negative block index should return error."""
        response = client.block(network=network, block_identifier={"index": -1})
        assert response.status_code in [400, 500]

    def test_non_existent_block_index_returns_error(
        self, client, network, blockchain_height
    ):
        """Block index beyond tip should return error."""
        future_block = blockchain_height + 1000000

        response = client.block(
            network=network, block_identifier={"index": future_block}
        )
        assert response.status_code == 500

    def test_invalid_block_hash_returns_error(self, client, network):
        """Invalid hash format should return error."""
        response = client.block(
            network=network, block_identifier={"hash": "invalid_hash"}
        )
        assert response.status_code == 500

    def test_missing_block_identifier_returns_error(self, client, network):
        """Missing block_identifier should return error."""
        response = client.block(network=network)
        assert response.status_code == 400, "Missing required parameter should return 400"

        error = response.json()
        error_message = get_error_message(error).lower()
        assert "block" in error_message or "identifier" in error_message


@allure.feature("Block Transaction")
@allure.story("Block Transaction Lookup")
class TestBlockTransactionLookup:
    """Test /block/transaction endpoint."""

    def test_get_transaction_from_block(self, client, network):
        """Get specific transaction from block."""
        # Find a block with transactions
        search_response = client.search_transactions(network=network)
        tx_data = search_response.json()["transactions"][0]

        block_id = tx_data["block_identifier"]
        tx_id = tx_data["transaction"]["transaction_identifier"]

        # Get transaction from block
        response = client.block_transaction(
            network=network,
            block_identifier={"index": block_id["index"], "hash": block_id["hash"]},
            transaction_identifier={"hash": tx_id["hash"]},
        )
        assert response.status_code == 200

        returned_tx = response.json()["transaction"]["transaction_identifier"]["hash"]
        assert returned_tx == tx_id["hash"]

    def test_requires_block_hash_not_just_index(self, client, network):
        """block_identifier requires both index AND hash."""
        # Get a transaction
        search_response = client.search_transactions(network=network)
        tx_data = search_response.json()["transactions"][0]

        # Try with only index (should fail)
        response = client.block_transaction(
            network=network,
            block_identifier={"index": tx_data["block_identifier"]["index"]},
            transaction_identifier={
                "hash": tx_data["transaction"]["transaction_identifier"]["hash"]
            },
        )
        assert response.status_code == 400, "Missing required parameter should return 400"

        error = response.json()
        error_message = get_error_message(error)
        assert "hash" in error_message.lower(), (
            "Error should indicate missing block hash"
        )

    def test_transaction_not_in_block_returns_error(self, client, network):
        """Requesting transaction not in specified block should error."""
        # Get transactions and ensure they're from different blocks
        search_response = client.search_transactions(network=network)
        txs = search_response.json()["transactions"]

        # Find two transactions from different blocks
        block1 = txs[0]["block_identifier"]
        tx2_hash = None

        for tx in txs[1:]:
            if tx["block_identifier"]["index"] != block1["index"]:
                tx2_hash = tx["transaction"]["transaction_identifier"]["hash"]
                break

        if not tx2_hash:
            raise AssertionError(
                "All recent transactions are in the same block. "
                "Blockchain state unsuitable for block/transaction mismatch test."
            )

        # Try to get tx2 from block1 (wrong block)
        response = client.block_transaction(
            network=network,
            block_identifier={"index": block1["index"], "hash": block1["hash"]},
            transaction_identifier={"hash": tx2_hash},
        )
        assert response.status_code == 500

        error = response.json()
        assert error.get("code") == 4006
        assert error.get("message") == "Transaction not found"


@allure.feature("Block Transaction")
@allure.story("Block Transaction Errors")
class TestBlockTransactionErrors:
    """Test error handling for /block/transaction endpoint."""

    def test_missing_transaction_identifier_returns_error(self, client, network):
        """Missing transaction_identifier should return error."""
        response = client.block_transaction(
            network=network,
            block_identifier={"index": 100, "hash": "a" * 64},
        )
        assert response.status_code == 400, "Missing required parameter should return 400"

    def test_invalid_transaction_hash_returns_error(self, client, network):
        """Invalid transaction hash should return error."""
        # Get valid block
        block_response = client.block(network=network, block_identifier={"index": 100})
        block_id = block_response.json()["block"]["block_identifier"]

        response = client.block_transaction(
            network=network,
            block_identifier=block_id,
            transaction_identifier={"hash": "invalid_hash"},
        )
        assert response.status_code == 500
