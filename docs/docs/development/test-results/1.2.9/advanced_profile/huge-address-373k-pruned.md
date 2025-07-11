This scenario evaluates the performance of querying an address with approximately 373,000 transactions under pruning configurations. For details on pruning, see [Spent UTXO Pruning](../../../../advanced-configuration/pruning.md).

:::note SLA Adjustment for Extreme Load
The SLA was adjusted to 10 seconds to evaluate system stability under extreme load, even with degraded performance.
:::
Data is taken from the test with spent UTXOs are retained for 30 days by setting `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=129600`

| ID | Endpoint         | Max Concurrency  | p95 (ms)  | p99 (ms)  | Non-2xx  | Error Rate (%)   | Reqs/sec  |
|----|------------------|------------------|-----------|-----------|----------|------------------|-----------|
| 1  | /account/balance | 500              | 146ms     | 252ms     | 0        | 0.00%            | 7651.95   |
| 2  | /account/coins   | 500              | 169ms     | 330ms     | 0        | 0.00%            | 6335.54   |

