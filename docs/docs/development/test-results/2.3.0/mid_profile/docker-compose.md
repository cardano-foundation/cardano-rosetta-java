The performance metrics in this table were measured against an SLA of 1000 ms.

| ID | Endpoint                              | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|---------------------------------------|------------------|----------|----------|---------|----------------|------------|
| 1  | /network/status                       | 125              | 62       | 77       | 0       | 0.00%          | 3340.80    |
| 2  | /account/balance                      | 100              | 612      | 737      | 0       | 0.00%          | 266.31     |
| 3  | /account/coins                        | 100              | 605      | 865      | 0       | 0.00%          | 285.75     |
| 4  | /block                                | 175              | 744      | 914      | 0       | 0.00%          | 405.32     |
| 5  | /block/transaction                    | 175              | 652      | 807      | 0       | 0.00%          | 455.24     |
| 6  | /search/transactions (by hash)        | 150              | 135      | 175      | 0       | 0.00%          | 1987.31    |
| 7  | /search/transactions (by address)     | 20               | 844      | 937      | 0       | 0.00%          | 30.65      |
| 8  | /construction/metadata                | 500              | 281      | 444      | 0       | 0.00%          | 7235.16    |
