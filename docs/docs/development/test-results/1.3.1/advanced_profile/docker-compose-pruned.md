Data is taken from the test with spent UTXOs are retained for 30 days by setting `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=129600`

| ID | Endpoint                | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec   |
|----|-------------------------|------------------|----------|----------|----------|------------------|-----------|
| 1  | /network/status         | 1000             | 293ms    | 368ms    | 0        | 0.00%            | 7965.05   |
| 2  | /account/balance        | 675              | 503ms    | 841ms    | 0        | 0.00%            | 2520.33   |
| 3  | /account/coins          | 575              | 410ms    | 678ms    | 0        | 0.00%            | 2761.44   |
| 4  | /block                  | 425              | 765ms    | 929ms    | 0        | 0.00%            | 861.76    |
| 5  | /block/transaction      | 375              | 758ms    | 935ms    | 0        | 0.00%            | 772.02    |
| 6  | /search/transactions    | 550              | 384ms    | 468ms    | 0        | 0.00%            | 2748.95   |
| 7  | /search/transactions    | 20               | 899ms    | 953ms    | 0        | 0.00%            | 27.41     |
| 8  | /construction/metadata  | 1000             | 501ms    | 705ms    | 0        | 0.00%            | 10317.23  |

> The first `/search/transactions` result was executed using `tx_hash`, while the second one was executed using `address`.
