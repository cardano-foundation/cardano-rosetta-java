
Data is taken from the test with spent UTXOs are retained for 7 days by setting `REMOVE_SPENT_UTXOS_LAST_BLOCKS_GRACE_COUNT=30240`

| ID | Endpoint                | Max Concurrency  | p95 (ms)  | p99 (ms)  | Non-2xx  | Error Rate (%)   | Reqs/sec   |
|----|-------------------------|------------------|-----------|-----------|----------|------------------|------------|
| 1  | /network/status         | 1000             | 169ms     | 218ms     | 0        | 0.00%            | 11206.81   |
| 2  | /account/balance        | 825              | 693ms     | 954ms     | 0        | 0.00%            | 2295.26    |
| 3  | /account/coins          | 725              | 654ms     | 907ms     | 0        | 0.00%            | 2194.91    |
| 4  | /block                  | 575              | 811ms     | 995ms     | 0        | 0.00%            | 1129.28    |
| 5  | /block/transaction      | 550              | 737ms     | 913ms     | 0        | 0.00%            | 1187.63    |
| 6  | /search/transactions    | 675              | 314ms     | 417ms     | 0        | 0.00%            | 4269.48    |
| 7  | /construction/metadata  | 1000             | 197ms     | 343ms     | 0        | 0.00%            | 14321.98   |
