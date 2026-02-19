The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|------------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 125              | 30       | 37       | 0       | 0.00%          | 6797.41    |
| 2  | /account/balance                      | 225              | 791      | 996      | 0       | 0.00%          | 448.01     |
| 3  | /account/coins                        | 225              | 734      | 961      | 0       | 0.00%          | 493.19     |
| 4  | /block                                | 150              | 695      | 848      | 0       | 0.00%          | 345.76     |
| 5  | /block/transaction                    | 175              | 799      | 985      | 0       | 0.00%          | 348.11     |
| 6  | /search/transactions (by hash)        | 175              | 82       | 104      | 0       | 0.00%          | 3785.62    |
| 7  | /search/transactions (by address)     | 20               | 725      | 781      | 0       | 0.00%          | 33.56      |
| 8  | /construction/metadata                | 500              | 150      | 237      | 0       | 0.00%          | 13635.74   |
