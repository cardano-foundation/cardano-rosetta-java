- **Hardware Profile:** mid-level
- **Machine Specs:** 16 cores, 16 threads, 94GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

**Maximum concurrency achieved per endpoint**

| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) | Non-2xx | Error Rate (%) | Reqs/sec |
| --- | ---------------------- | --------------- | -------- | -------- | ------- | -------------- | -------- |
| 1   | /network/status        | 500             | 185ms    | 274ms    | 0       | 0.00%          | 5842.36  |
| 2   | /account/balance       | 150             | 554ms    | 957ms    | 0       | 0.00%          | 301.71   |
| 3   | /account/coins         | 150             | 511ms    | 890ms    | 0       | 0.00%          | 323.91   |
| 4   | /block                 | 8               | 45ms     | 50ms     | 0       | 0.00%          | 212.68   |
| 5   | /block/transaction     | 8               | 36ms     | 39ms     | 0       | 0.00%          | 255.65   |
| 6   | /search/transactions   | 8               | 18ms     | 22ms     | 0       | 0.00%          | 743.97   |
| 7   | /construction/metadata | 300             | 152ms    | 201ms    | 0       | 0.00%          | 6305.56  |
