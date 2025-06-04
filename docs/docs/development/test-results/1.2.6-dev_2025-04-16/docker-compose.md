**Hardware Profile:** mid_level 

**Machine Specs:** 8 cores, 8 threads, 47GB RAM, 3.9TB NVMe, QEMU Virtual CPU v2.5+

<Link
className="button button--secondary button--block"
to="./install-and-deploy/hardware-profiles">
Hardware Profiles
</Link>

Maximum concurrency achieved per endpoint

| ID  | Endpoint               | Max Concurrency | p95 (ms) | p99 (ms) |
| --- | ---------------------- | --------------- | -------- | -------- |
| 1   | /network/status        | 500             | 150ms    | 171ms    |
| 2   | /account/balance       | 150             | 640ms    | 777ms    |
| 3   | /account/coins         | 225             | 812ms    | 949ms    |
| 4   | /block                 | 200             | 706ms    | 960ms    |
| 5   | /block/transaction     | 125             | 397ms    | 469ms    |
| 6   | /search/transactions   | 175             | 130ms    | 161ms    |
| 7   | /construction/metadata | 500             | 127ms    | 178ms    |
