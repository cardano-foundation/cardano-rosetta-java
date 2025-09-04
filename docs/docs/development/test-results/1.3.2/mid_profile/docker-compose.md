The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|-------------------------|------------------|----------|----------|----------|------------------|-----------|
| 1  | /network/status         | 500              | 187      | 223      | 0        | 0.00%            | 4763.58   |
| 2  | /account/balance        | 200              | 850      | 990      | 0        | 0.00%            | 340.34    |
| 3  | /account/coins          | 200              | 837      | 967      | 0        | 0.00%            | 357.62    |
| 4  | /block                  | 150              | 579      | 703      | 0        | 0.00%            | 434.22    |
| 5  | /block/transaction      | 200              | 648      | 905      | 151      | 0.57%            | 433.11    |
| 6  | /search/transactions    | 175              | 127      | 169      | 4        | 0.01%            | 2041.35   |
| 7  | /construction/metadata  | 500              | 125      | 247      | 0        | 0.00%            | 9043.61   |