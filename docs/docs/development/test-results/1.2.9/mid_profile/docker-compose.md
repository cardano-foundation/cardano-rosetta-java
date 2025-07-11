
| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /network/status        | 500             | 169ms    | 198ms    | 0       | 0.00%          | 6120.35  |
| 2   | /account/balance       | 225             | 824ms    | 958ms    | 0       | 0.00%          | 391.41   |
| 3   | /account/coins         | 225             | 783ms    | 917ms    | 0       | 0.00%          | 411.56   |
| 4   | /block                 | 175             | 684ms    | 837ms    | 0       | 0.00%          | 407.10   |
| 5   | /block/transaction     | 200             | 691ms    | 967ms    | 10      | 0.04%          | 435.23   |
| 6   | /search/transactions   | 175             | 154ms    | 204ms    | 150     | 0.30%          | 1727.60  |
| 7   | /construction/metadata | 500             | 210ms    | 379ms    | 0       | 0.00%          | 8331.37  |
