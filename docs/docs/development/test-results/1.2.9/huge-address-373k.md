This scenario evaluates the performance of querying an address with approximately 373,000 transactions under different pruning configurations. For details on pruning, see [Spent UTXO Pruning](../../../advanced-configuration/pruning.md).

#### Pruning Enabled (`REMOVE_SPENT_UTXOS=true`)

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 500             | 153ms    | 414ms    | 0       | 0.00%          | 7012.95  |
| 2   | /account/coins   | 500             | 184ms    | 402ms    | 0       | 0.00%          | 5964.08  |

#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)

:::note SLA Adjustment for Extreme Load
For this specific test with pruning disabled, the standard 1-second Service Level Agreement (SLA) is not achievable. Therefore, the SLA was adjusted to 10 seconds to evaluate system stability under extreme load, even with degraded performance.
:::

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 4               | 6486ms   | 7263ms   | 0       | 0.00%          | 1.02     |
| 2   | /account/coins   | 8               | 9011ms   | 9384ms   | 0       | 0.00%          | 1.20     |
