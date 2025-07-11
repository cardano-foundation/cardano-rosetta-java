
| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec  |
| --- | ---------------------- | --------------- | -------- | -------- | ------- | -------------- | --------- |
| 1   | /network/status        | 800             | 113ms    | 142ms    | 0       | 0.00%          | 14370.82  |
| 2   | /account/balance       | 325             | 793ms    | 951ms    | 0       | 0.00%          | 671.91    |
| 3   | /account/coins         | 275             | 812ms    | 993ms    | 0       | 0.00%          | 569.68    |
| 4   | /block                 | 200             | 762ms    | 916ms    | 0       | 0.00%          | 409.54    |
| 5   | /block/transaction     | 200             | 787ms    | 974ms    | 0       | 0.00%          | 417.31    |
| 6   | /search/transactions   | 625             | 417ms    | 555ms    | 0       | 0.00%          | 3069.85   |
| 7   | /construction/metadata | 800             | 143ms    | 273ms    | 0       | 0.00%          | 13296.60  |
