# Cardano Rosetta E2E Tests

End-to-end testing framework for Cardano's Rosetta API implementation.

## Features

- Basic ADA transfers
- Native token transfers (tokenBundle)
- UTXO management
- Transaction construction and signing
- Multi-input/output transactions
- Stake key registration and delegation
- Governance operations (DRep voting)
- Testnet/mainnet support

## Directory Structure

```
e2e_tests/
├── rosetta_client/          # Rosetta API client
│   ├── client.py            # API endpoint mapping
│   ├── exceptions.py        # Custom exceptions
│   └── request_debugger.py  # HTTP request logging
├── test_utils/              # Test utilities
│   ├── transaction_orchestrator.py  # Transaction construction workflow
│   ├── signing_handler.py           # Transaction signing
│   ├── operation_builders.py        # Rosetta operation construction
│   └── utxo_selector.py             # UTXO selection strategies
├── wallet_utils/            # Wallet management
│   └── pycardano_wallet.py  # PyCardano wallet wrapper
└── tests/                   # Test cases
    ├── test_multi_io_transactions.py  # Multi-input/output tests
    ├── test_native_asset_transfer.py  # Native asset transfer test
    └── test_stake_operations.py       # Stake operations tests
```

## Setup

### Prerequisites

- Python 3.11+
- pip
- Cardano Rosetta API endpoint
- Test wallet with funds and at least 11 ada-only UTXOs
- For native asset tests: at least 1 UTXO containing a token bundle and at least 1 ADA-only UTXO (≥ 5 ADA recommended to cover fee)

### Quick Start

```bash
cd e2e_tests
# First create a virtual environment
python3 -m venv .venv
# Activate the virtual environment
source .venv/bin/activate  # On Linux/macOS
# OR
.\.venv\Scripts\activate   # On Windows
# Then install dependencies
pip install -r requirements.txt
```

### Configuration

Create `.env` file:

```env
ROSETTA_ENDPOINT=http://localhost:8082
CARDANO_NETWORK=preview
TEST_WALLET_MNEMONIC="your mnemonic here"
STAKE_POOL_HASH=d9812f8d30b5db4b03e5b76cfd242db9cd2763da4671ed062be808a0 # Required for stake delegation tests
DREP_KEY_HASH_ID=03ccae794affbe27a5f5f74da6266002db11daa6ae446aea783b972d # Required for DRep vote delegation tests with key_hash type
DREP_SCRIPT_HASH_ID=2d4cb680b5f400d3521d272b4295d61150e0eff3950ef4285406a953 # Required for DRep vote delegation tests with script_hash type
POOL_REGISTRATION_CERT=<hex-encoded-cert> # Required for poolRegistrationWithCert test
POOL_GOVERNANCE_PROPOSAL_ID=df58f714c0765f3489afb6909384a16c31d600695be7e86ff9c59cf2e8a48c7900 # Required for pool governance vote tests
POOL_VOTE_CHOICE=yes # Optional: Vote choice for pool governance vote (yes/no/abstain, default: yes)

## Usage

- Create a new wallet (e.g. Eternl) and fund it with faucet
- Add the mnemonic to the `.env` file (`TEST_WALLET_MNEMONIC`)
- Create at least 11 new utxos with at least 1500000 lovelaces each (you can use the fan-out test 2x for this)
- Run the tests

### Running All Tests

```bash
# Run with detailed logging (includes request/response data)
pytest --log-cli-level=DEBUG

# Run with less verbose logging
pytest --log-cli-level=INFO
```

### Running Specific Tests

```bash
# Multi-input/output transfer tests

# Run all multi-input/output tests
pytest --log-cli-level=INFO tests/test_multi_io_transactions.py

# Run basic test (1 input, 2 outputs)
pytest --log-cli-level=INFO "tests/test_multi_io_transactions.py::test_multi_io_transaction[1-2-basic]"

# Run fan-out test (1 input, 10 outputs)
pytest --log-cli-level=INFO "tests/test_multi_io_transactions.py::test_multi_io_transaction[1-10-fan-out]"

# Run consolidation test (10 inputs, 1 output)
pytest --log-cli-level=INFO "tests/test_multi_io_transactions.py::test_multi_io_transaction[10-1-consolidation]"

# Run complex test (10 inputs, 10 outputs)
pytest --log-cli-level=INFO "tests/test_multi_io_transactions.py::test_multi_io_transaction[10-10-complex]"

# Run fixed fee test
pytest --log-cli-level=INFO tests/test_multi_io_transactions.py::test_fixed_fee_transaction

# Stake operation tests

# Run all stake tests
pytest --log-cli-level=INFO tests/test_stake_operations.py

# Run specific stake tests (Scenario A: Separate Operations)
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_stake_key_registration
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_stake_delegation
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_reward_withdrawal_zero
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_scenario_A_deregistration

# Run specific stake tests (Scenario B: Combined Operations + Votes)
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_combined_registration_delegation
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_drep_vote_delegation_abstain
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_drep_vote_delegation_no_confidence
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_drep_vote_delegation_key_hash
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_drep_vote_delegation_script_hash
pytest --log-cli-level=INFO tests/test_stake_operations.py::test_scenario_B_final_deregistration

# Pool operation tests

# Run all pool operation tests
pytest --log-cli-level=INFO tests/test_pool_operations.py

# Run specific pool operation tests
pytest --log-cli-level=INFO tests/test_pool_operations.py::test_pool_registration
pytest --log-cli-level=INFO tests/test_pool_operations.py::test_pool_registration_with_cert
pytest --log-cli-level=INFO tests/test_pool_operations.py::test_pool_governance_vote
pytest --log-cli-level=INFO tests/test_pool_operations.py::test_pool_retirement

# Native asset tests

# Run the native asset self-transfer test (spends a token UTXO and recreates the token bundle to self)
pytest --log-cli-level=INFO tests/test_native_asset_transfer.py::test_native_asset_self_transfer
```

## Test Scenarios

### Transaction Tests

1. **Basic**: Single input → 2 outputs (transfer + change)
2. **Consolidation**: Multiple inputs → Single output
3. **Fan-out**: Single input → Multiple outputs
4. **Complex**: Multiple inputs → Multiple outputs
5. **Fixed Fee**: Transaction with a fixed 4 ADA fee (4,000,000 lovelaces) that bypasses suggested fee
6. **Native Asset Self-Transfer**: Spend a UTXO carrying native tokens (tokenBundle) and recreate the same bundle to self; add an ADA-only UTXO to comfortably cover fees.

### Notes for Native Asset Tests

- The Rosetta response for `/account/coins` is expected to include token bundles on UTXOs in `coin.metadata`.
- The test attaches a `tokenBundle` to the input (with negative values) and to the token output (with positive values), matching the documented format in `docs/user-guides/multi-assets.md`.
- Ensure your wallet has at least one UTXO that carries a token bundle and a separate ADA-only UTXO with enough ADA to pay fees.

### Stake Operations

1. **Registration**: Register stake key
2. **Delegation**: Delegate to stake pool
3. **Deregistration**: Deregister stake key

### Governance Operations

1. **DRep Vote Delegation - Abstain**: Delegate voting power to abstain
2. **DRep Vote Delegation - No Confidence**: Delegate voting power to no confidence
3. **DRep Vote Delegation - Key Hash**: Delegate voting power to a DRep with key hash
4. **DRep Vote Delegation - Script Hash**: Delegate voting power to a DRep with script hash

### Pool Operations

1. **Pool Registration**: Register a new stake pool with generated parameters
2. **Pool Registration With Certificate**: Register a pool using a pre-created certificate
3. **Pool Governance Vote**: Submit governance votes as a Stake Pool Operator (SPO)
4. **Pool Retirement**: Retire the registered stake pool and reclaim deposit

## API Coverage

The following Rosetta API endpoints are covered in our e2e tests:

- `/network/list`: Verified at test startup to ensure the desired network is supported (in `conftest.py`).
- `/network/options`: Verified at test startup to ensure required operations are supported (in `conftest.py`).
- `/network/status`: Verified at test startup to check current block information (in `conftest.py`).
- `/account/balance`: Used in all test cases to verify initial and final balances.
- `/account/coins`: Used in all test cases for UTXO selection and management.
- `/construction/derive`: Verified during transaction tests to ensure the derived address matches the wallet's public key.
- `/construction/preprocess`: Used in transaction construction workflow.
- `/construction/metadata`: Used in transaction construction workflow.
- `/construction/payloads`: Used in transaction construction workflow.
- `/construction/parse`: Verified during transaction construction to ensure all operations are included correctly in both unsigned and signed transactions.
- `/construction/combine`: Used in transaction signing workflow.
- `/construction/hash`: Used to get the transaction hash for submission.
- `/construction/submit`: Used to submit signed transactions to the blockchain.
- `/search/transactions`: Used to search for submitted transactions and verify they are on-chain.
- `/block`: Used to fetch block details for on-chain verification.
- `/block/transaction`: Used to fetch full transaction details for on-chain verification.
