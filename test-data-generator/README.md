# Test Data Generator

This submodule offer a reproducable way to populate the database with test data. It is not intended to be used in production.
We are using yaci-devkit and yaci-store to populate a m2 database. The default path will be ``./testData/devkit.db``.

The results and corresponding hashes will be saved in JSON file. This can be consumed by the integration tests to get the right hashes.

## How to generate data
- Start [yaci-devkit](https://github.com/bloxbean/yaci-devkit)
- Topup test addresses with some ADA
  - ```topup addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah 2000```
- Start Module yaci-indexer with the h2 profile. It is already predefined in the yaci-indexer/src/main/resources/application-h2-testdata.properties file.
- Run the ``TestDataGenerator`` class in this module. It will populate the database with test data.
- Run the following sql commands on generated H2 file based database: (workaround for yaci-store, we need to experiment with how to have transaction in block 0)
```sql
UPDATE block SET number = 0 WHERE number = -1;
UPDATE transaction SET block = 0 WHERE block_hash = 'Genesis';
```