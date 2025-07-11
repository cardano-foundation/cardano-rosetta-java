Data is taken from the test with spent UTXOs are retained for 30 days by setting `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=129600`

| ID | Endpoint                | Max Concurrency  | p95 (ms)  | p99 (ms)  | Non-2xx  | Error Rate (%)   | Reqs/sec  |
|----|-------------------------|------------------|-----------|-----------|----------|------------------|-----------|
| 1  | /network/status         | 1000             | 300ms     | 430ms     | 0        | 0.00%            | 8673.16   |
| 2  | /account/balance        | 675              | 718ms     | 961ms     | 0        | 0.00%            | 2038.31   |
| 3  | /account/coins          | 700              | 673ms     | 853ms     | 0        | 0.00%            | 2039.88   |
| 4  | /block                  | 625              | 707ms     | 924ms     | 0        | 0.00%            | 1764.33   |
| 5  | /block/transaction      | 700              | 549ms     | 727ms     | 0        | 0.00%            | 2439.62   |
| 6  | /search/transactions    | 700              | 448ms     | 590ms     | 0        | 0.00%            | 3009.91   |
| 7  | /construction/metadata  | 1000             | 473ms     | 713ms     | 0        | 0.00%            | 9783.88   |

