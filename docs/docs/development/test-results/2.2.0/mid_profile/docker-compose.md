The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|------------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 125              | 55       | 67       | 0       | 0.00%          | 3907.71    |
| 2  | /account/balance                      | 100              | 611      | 756      | 0       | 0.00%          | 268.82     |
| 3  | /account/coins                        | 100              | 592      | 808      | 0       | 0.00%          | 287.80     |
| 4  | /block                                | 150              | 603      | 742      | 0       | 0.00%          | 420.77     |
| 5  | /block/transaction                    | 175              | 622      | 771      | 0       | 0.00%          | 476.30     |
| 6  | /search/transactions (by hash)        | 175              | 166      | 237      | 201     | 0.40%          | 1395.38    |
| 7  | /search/transactions (by address)     | 20               | 869      | 999      | 0       | 0.00%          | 29.96      |
| 8  | /construction/metadata                | 500              | 138      | 341      | 0       | 0.00%          | 8418.45    |
