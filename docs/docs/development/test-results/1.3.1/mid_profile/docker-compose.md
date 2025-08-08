
| ID | Endpoint                | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|-------------------------|------------------|----------|----------|----------|------------------|-----------|
| 1  | /network/status         | 700              | 227ms    | 308ms    | 0        | 0.00%            | 6699.38   |
| 2  | /account/balance        | 200              | 817ms    | 947ms    | 0        | 0.00%            | 342.02    |
| 3  | /account/coins          | 225              | 864ms    | 982ms    | 0        | 0.00%            | 360.73    |
| 4  | /block                  | 175              | 693ms    | 966ms    | 150      | 0.67%            | 375.43    |
| 5  | /block/transaction      | 175              | 566ms    | 716ms    | 149      | 0.55%            | 452.90    |
| 6  | /search/transactions    | 175              | 162ms    | 211ms    | 150      | 0.30%            | 1621.06   |
| 7  | /search/transactions    | 0                | 0ms      | 0ms      | 0        | 0.00%            | 0.00      |
| 8  | /construction/metadata  | 700              | 369ms    | 703ms    | 0        | 0.00%            | 8418.52   |

> The first `/search/transactions` result was executed using `tx_hash`, while the second one was executed using `address`.