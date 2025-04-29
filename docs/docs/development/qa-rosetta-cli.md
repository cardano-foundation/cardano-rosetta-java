# `rosetta-cli` Verification Checks

The `rosetta-cli` is a command-line tool for testing Rosetta API implementations against the official Rosetta API specifications.

## Available Checks

### `check:data`

The `check:data` command validates that the Data API implementation is both complete and correct. It verifies:

- All endpoints are implemented correctly
- All returned data is properly formatted and valid
- Blocks can be processed in sequence without issues

Example usage:

```console
./rosetta-cli check:data --configuration-file ./configuration/data/byron_sample.json
```

This verification runs during [integration tests](https://github.com/cardano-foundation/cardano-rosetta-java/blob/main/.github/workflows/integration-test.yaml) against the first 200 blocks of the blockchain within the devkit environment.

## Configuration Files

Configuration files specify the parameters for verification:

- Network information
- Block range to test
- Data pruning settings
- Other test-specific parameters

See the `./configuration/data/` directory for example configurations.
