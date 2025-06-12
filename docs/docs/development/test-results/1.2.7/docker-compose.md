- **Hardware Profile:** mid-level
- **Machine Specs:** 8 cores, 8 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) |
| --- | ---------------------- | --------------- | -------- | -------- |
| 1   | /network/status        | 200             | 61ms     | 73ms     |
| 2   | /account/balance       | 200             | 708ms    | 833ms    |
| 3   | /account/coins         | 200             | 737ms    | 875ms    |
| 4   | /block                 | 200             | 618ms    | 722ms    |
| 5   | /block/transaction     | 175             | 552ms    | 691ms    |
| 6   | /search/transactions   | 175             | 119ms    | 143ms    |
| 7   | /construction/metadata | 56              | 11ms     | 15ms     |
