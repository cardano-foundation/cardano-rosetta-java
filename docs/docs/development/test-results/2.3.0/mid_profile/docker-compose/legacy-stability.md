The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|-----------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 100             | 60       | 75       | 0       | 0.00%          | 2644.85    |
| 2  | /account/balance                      | 200             | 138      | 198      | 0       | 0.00%          | 2718.13    |
| 3  | /account/coins                        | 200             | 162      | 215      | 0       | 0.00%          | 2183.32    |
| 4  | /block                                | 150             | 501      | 634      | 0       | 0.00%          | 526.67     |
| 5  | /block/transaction                    | 150             | 401      | 506      | 0       | 0.00%          | 650.03     |
| 6  | /search/transactions (by hash)        | 150             | 122      | 153      | 0       | 0.00%          | 2133.67    |
| 7  | /search/transactions (by address)     | 100             | 521      | 618      | 0       | 0.00%          | 304.46     |
| 8  | /construction/metadata                | 200             | 44       | 62       | 0       | 0.00%          | 8040.04    |
