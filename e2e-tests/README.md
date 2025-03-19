# Cardano Rosetta E2E Tests

Simple end-to-end testing framework for Cardano's Rosetta API implementation.

## Features

- Basic ADA transfers
- UTXO management
- Transaction construction and signing
- Multi-input/output transactions
- Stake key registration and delegation
- Testnet/mainnet support

## Setup

### Prerequisites

- Python 3.11+
- Poetry (recommended) or pip
- Cardano Rosetta API endpoint
- Test wallet with funds

### Quick Start

```bash
# Using Poetry (recommended)
poetry install

# Using pip
pip install -r requirements.txt
```

### Configuration

Create `.env` file:

```env
ROSETTA_ENDPOINT=http://localhost:8082
CARDANO_NETWORK=preview
TEST_WALLET_MNEMONIC="your mnemonic here"
STAKE_POOL_ID=d9812f8d30b5db4b03e5b76cfd242db9cd2763da4671ed062be808a0 # Required for stake delegation tests
DREP_KEY_HASH_ID=03ccae794affbe27a5f5f74da6266002db11daa6ae446aea783b972d # Required for DRep vote delegation tests with key_hash type
DREP_SCRIPT_HASH_ID=2d4cb680b5f400d3521d272b4295d61150e0eff3950ef4285406a953 # Required for DRep vote delegation tests with script_hash type
```

## Usage

### Running Tests

```bash
# Run all tests
pytest # minimal verbose
pytest --log-cli-level=DEBUG # verbose, include request/response data

# Run specific test scenarios
pytest tests/test_multi_io_transactions.py -k basic
pytest tests/test_multi_io_transactions.py -k fan-out
pytest tests/test_multi_io_transactions.py -k consolidation
pytest tests/test_multi_io_transactions.py -k complex

# Run stake tests
pytest tests/test_stake_scenarios.py

# Run only specific DRep vote delegation tests
pytest tests/test_stake_scenarios.py -k test_scenario2_combined_registration_delegation
pytest tests/test_stake_scenarios.py -k test_scenario2_drep_vote_delegation_abstain
pytest tests/test_stake_scenarios.py -k test_scenario2_drep_vote_delegation_no_confidence
pytest tests/test_stake_scenarios.py -k test_scenario2_drep_vote_delegation_key_hash
pytest tests/test_stake_scenarios.py -k test_scenario2_drep_vote_delegation_script_hash
```

## Project Structure

```
├── rosetta_client/      # Rosetta API client
├── wallet_utils/        # PyCardano wallet wrapper
├── tests/               # Test suites
└── pyproject.toml       # Project configuration
```

## Test Scenarios

### Transaction Tests

1. **Basic**: Single input → 2 outputs (transfer + change)
2. **Consolidation**: Multiple inputs → Single output
3. **Fan-out**: Single input → Multiple outputs
4. **Complex**: Multiple inputs → Multiple outputs

### Stake Operations

1. **Registration**: Register stake key
2. **Delegation**: Delegate to stake pool
3. **Deregistration**: Deregister stake key

### Governance Operations

1. **DRep Vote Delegation - Abstain**: Delegate voting power to abstain
2. **DRep Vote Delegation - No Confidence**: Delegate voting power to no confidence
3. **DRep Vote Delegation - Key Hash**: Delegate voting power to a DRep with key hash
4. **DRep Vote Delegation - Script Hash**: Delegate voting power to a DRep with script hash
