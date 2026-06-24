The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|-----------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 100             | 62       | 79       | 0       | 0.00%          | 2610.15    |
| 2  | /account/balance                      | 200             | 136      | 186      | 0       | 0.00%          | 2817.42    |
| 3  | /account/coins                        | 200             | 171      | 229      | 0       | 0.00%          | 2081.40    |
| 4  | /block                                | 150             | 571      | 713      | 0       | 0.00%          | 465.20     |
| 5  | /block/transaction                    | 150             | 488      | 610      | 0       | 0.00%          | 541.25     |
| 6  | /search/transactions (by hash)        | 150             | 177      | 228      | 0       | 0.00%          | 1587.20    |
| 7  | /search/transactions (by address)     | 150             | 696      | 841      | 0       | 0.00%          | 354.36     |
| 8  | /construction/metadata                | 200             | 57       | 94       | 0       | 0.00%          | 6840.46    |
