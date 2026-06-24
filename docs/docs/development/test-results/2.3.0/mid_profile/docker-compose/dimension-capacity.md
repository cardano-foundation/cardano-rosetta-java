Dimension-isolated capacity test for /account/balance and /account/coins by UTXO count.
SLA: 1000 ms. Search strategy: exponential (max 2048).

| Level | Range           | Endpoint         | Max Conc | p95 (ms) | p99 (ms) | Reqs/sec |
|-------|-----------------|------------------|----------|----------|----------|----------|
| 1     | 1-9 UTXOs       | /account/balance | 784      | 508      | 747      | 3757.00  |
| 1     | 1-9 UTXOs       | /account/coins   | 877      | 648      | 996      | 3461.96  |
| 10    | 10-99 UTXOs     | /account/balance | 57       | 726      | 970      | 145.76   |
| 10    | 10-99 UTXOs     | /account/coins   | 49       | 667      | 849      | 125.30   |
| 100   | 100-999 UTXOs   | /account/balance | 12       | 835      | 898      | 19.44    |
| 100   | 100-999 UTXOs   | /account/coins   | 13       | 751      | 819      | 21.12    |
| 1000  | 1000-9999 UTXOs | /account/balance | 1        | 8224     | 8224     | 0.50     |
| 1000  | 1000-9999 UTXOs | /account/coins   | 1        | 1934     | 2023     | 0.68     |
| 10000 | ≥10000 UTXOs    | /account/balance | 1        | 13991    | 13991    | 0.10     |
| 10000 | ≥10000 UTXOs    | /account/coins   | 1        | 9574     | 9574     | 0.12     |
