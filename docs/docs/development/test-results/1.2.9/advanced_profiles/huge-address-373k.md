This scenario evaluates the performance of querying an address with approximately 373,000 transactions under non pruning configurations. For details on pruning, see [Spent UTXO Pruning](../../../advanced-configuration/pruning.md).

#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)

:::note SLA Adjustment for Extreme Load
For this specific test with pruning disabled, the standard 1-second Service Level Agreement (SLA) is not achievable. Therefore, the SLA was adjusted to 10 seconds to evaluate system stability under extreme load, even with degraded performance.
:::

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 0               | 0ms      | 0ms      | 0       | 0.00%          | 0.00     |
| 2   | /account/coins   | 20              | 9492ms   | 9892ms   | 0       | 0.00%          | 2.50     |

