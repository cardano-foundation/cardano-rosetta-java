This scenario evaluates the performance of querying an address with approximately 1.6 million transactions under non pruning configurations. For details on pruning, see [Spent UTXO Pruning](../../../advanced-configuration/pruning.md).


#### Pruning Disabled (`REMOVE_SPENT_UTXOS=false`)

:::note SLA Adjustment for Extreme Load
For this specific test with pruning disabled on an address with ~1.6M transactions, the standard 1-second Service Level Agreement (SLA) is not achievable. The SLA was adjusted to 10 seconds to evaluate system stability under extreme load.
:::

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 2               | 9209ms   | 9209ms   | 0       | 0.00%          | 0.29     |
| 2   | /account/coins   | 2               | 7419ms   | 7437ms   | 0       | 0.00%          | 0.32     |
