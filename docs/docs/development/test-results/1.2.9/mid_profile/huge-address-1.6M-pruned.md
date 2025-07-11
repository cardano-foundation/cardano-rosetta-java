This scenario evaluates the performance of querying an address with approximately 1.6 million transactions under pruning configurations. For details on pruning, see [Spent UTXO Pruning](../../../../advanced-configuration/pruning.md).
:::note SLA Adjustment for Extreme Load
The SLA was adjusted to 10 seconds to evaluate system stability under extreme load, even with degraded performance.
:::
Data is taken from the test with spent UTXOs are retained for 7 days by setting `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=30240`

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 500             | 1304ms   | 1710ms   | 0       | 0.00%          | 694.10   |
| 2   | /account/coins   | 500             | 1184ms   | 1551ms   | 0       | 0.00%          | 779.39   |
