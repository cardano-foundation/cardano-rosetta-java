The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|------------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 125              | 35       | 46       | 0       | 0.00%          | 6152.33    |
| 2  | /account/balance                      | 200              | 761      | 964      | 0       | 0.00%          | 439.08     |
| 3  | /account/coins                        | 175              | 683      | 880      | 0       | 0.00%          | 459.27     |
| 4  | /block                                | 175              | 737      | 895      | 0       | 0.00%          | 385.93     |
| 5  | /block/transaction                    | 200              | 659      | 897      | 150     | 0.56%          | 443.63     |
| 6  | /search/transactions (by hash)        | 175              | 79       | 101      | 0       | 0.00%          | 3915.87    |
| 7  | /search/transactions (by address)     | 28               | 833      | 923      | 0       | 0.00%          | 43.62      |
| 8  | /construction/metadata                | 500              | 91       | 186      | 0       | 0.00%          | 16493.65   |
