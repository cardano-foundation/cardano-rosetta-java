This scenario evaluates the performance of querying an address with approximately 373k transactions.

:::note SLA Adjustment for Extreme Load
For this specific test with pruning disabled on an address with ~373k transactions, the standard 1-second Service Level Agreement (SLA) is not achievable. The SLA was adjusted to 10 seconds to evaluate system stability under extreme load.
:::

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint         | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /account/balance | 4               | 7689ms   | 8633ms   | 0       | 0.00%          | 0.90     |
| 2   | /account/coins   | 4               | 7322ms   | 8006ms   | 0       | 0.00%          | 0.90     |
