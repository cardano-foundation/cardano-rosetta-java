# Upgrade to 2.3.3: mainnet repair

Version 2.3.3 upgrades Yaci to **0.4.7** (Yaci Store stays at 2.0.2.1).

> **Mainnet only: one-time repair required.**
> Yaci 0.4.6 and earlier read mainnet block **14,053,493** as having **0 transactions**. The block has **137**.
> Rosetta does not stop syncing, so a mainnet database that synced past this block (slot 200136161,
> 2026-10-11 07:07 UTC) is **missing those 137 transactions**. Upgrading alone does not fix data that is already stored.
>
> Preprod and preview are not affected. Databases that have not yet reached this block only need the upgrade.

## 1. Check whether you are affected

```bash
docker compose exec -e PGPASSWORD=<DB_SECRET> db psql -h localhost -U rosetta_db_admin -d rosetta-java -c \
  "SELECT count(*) FROM transaction WHERE block = 14053493;"
```

| Result | Action |
|--------|--------|
| `137` | Already correct. Just upgrade. |
| `0` to `136` | Run the repair below. |
| `0` and the node has not synced past block 14,053,493 yet | Just upgrade. |

## 2. Repair

The repair script moves the sync cursor back to block 14,053,492. On the next start the indexer rolls back to that
slot and fetches block 14,053,493 again with the fixed parser. The script is
[`repair-mainnet-block-14053493.sql`](repair-mainnet-block-14053493.sql) in this folder (taken unchanged from
the [Yaci Store v2.0.7 release](https://github.com/bloxbean/yaci-store/blob/v2.0.7/scripts/repair/repair-mainnet-block-14053493.sql)).

1. **Stop the indexer and API. Keep the database running.**

   ```bash
   docker compose stop yaci-indexer api
   ```

2. **Upgrade to 2.3.3** by building the images from this version or pulling them, and set `RELEASE_VERSION=2.3.3`.
   Do not start the indexer yet.

3. **Run the repair script against the Rosetta database** (from the repository root):

   ```bash
   docker compose exec -T -e PGPASSWORD=<DB_SECRET> db psql -h localhost -U rosetta_db_admin -d rosetta-java -v ON_ERROR_STOP=1 \
     -f - < scripts/repair/repair-mainnet-block-14053493.sql
   ```

   Replace `<DB_SECRET>` with the `DB_SECRET` from your env file (the same applies to the check queries), and use your
   own `DB_USER` / `DB_NAME` if you changed them. If your tables are not in the `public` schema, add
   `-c 'SET search_path TO <your DB_SCHEMA>'` before `-f -`. Expected output:

   ```
   NOTICE:  Block 14053493 has 0 of 137 transactions. Repairing.
   NOTICE:  Cursor moved from slot ... back to 200136161 (block 14053492). Start yaci-store to re-sync.
   ```

   If it fails, nothing is changed. The script is safe to run more than once, and it does nothing if the cursor has
   not passed the block or the block is already complete.

4. **Start the stack:**

   ```bash
   docker compose up -d
   ```

   The indexer log shows a rollback to `slot=200136161`, then syncing continues.

5. **Verify:** the query from step 1 now returns `137`.

### Notes

- **Mainnet detection.** The script aborts with `Could not confirm this is a mainnet database` on non-mainnet or empty
  databases. This is expected. Only if your mainnet database was synced from a custom start slot, set
  `v_force_mainnet := true` at the top of the script.
- **Event publisher id.** The script assumes `store.event-publisher-id` is the default `1`.
- **Restored snapshots.** A database restored from a snapshot taken after 2026-10-11 07:07 UTC needs the same repair.

## Kubernetes / Helm

The Helm chart does not stop the old indexer pod before starting the new one. Scale it down first:

```bash
kubectl scale deployment <release>-yaci-indexer --replicas=0
```

Then run the script inside the PostgreSQL pod, for example:

```bash
kubectl exec -i <postgres-pod> -- env PGPASSWORD=<DB_SECRET> psql -h localhost -U rosetta_db_admin -d rosetta-java -v ON_ERROR_STOP=1 -f - \
  < scripts/repair/repair-mainnet-block-14053493.sql
```

Finally run `helm upgrade` with `global.releaseVersion="2.3.3"`, which starts the indexer again, and verify with the
query from step 1.
