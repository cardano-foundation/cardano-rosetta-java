**Hardware:** mid-level  
**Machine Specs:** 16 cores, 16 threads, 125GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

Maximum concurrency achieved per endpoint

| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) |
| --- | ---------------------- | --------------- | -------- | -------- |
| 1   | /network/status        | 500             | 82ms     | 103ms    |
| 2   | /account/balance       | 500             | 750ms    | 902ms    |
| 3   | /account/coins         | 500             | 720ms    | 883ms    |
| 4   | /block                 | 200             | 726ms    | 980ms    |
| 5   | /block/transaction     | 175             | 596ms    | 733ms    |
| 6   | /search/transactions   | 175             | 74ms     | 94ms     |
| 7   | /construction/metadata | 500             | 72ms     | 97ms     |
