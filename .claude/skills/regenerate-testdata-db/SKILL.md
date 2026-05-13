---
name: regenerate-testdata-db
description: "Regenerate the H2 test fixture testData/devkit.db.mv.db and testData/testdata.json end-to-end, then fix any test assertions that drift because of the new fixture. Use when the user asks to 'regenerate testdata', 'regenerate testdata db', 'refresh test data', 'rebuild devkit.db', or when CI fails with errors like 'Table CIP26_METADATA not found' / 'Table FT_OFFCHAIN_METADATA not found' that indicate the committed fixture is stale relative to the current yaci-store schema."
---

# Regenerate testData DB

The committed fixture `testData/devkit.db.mv.db` is produced by running rosetta's own `yaci-indexer` against a freshly-booted yaci-cli devnet. Whenever the yaci-store dependency version (in `yaci-indexer/pom.xml`) is bumped and the schema changes, that fixture becomes stale and integration tests fail with table-not-found / 500 errors.

This skill regenerates the fixture, then fixes the small handful of test assertions that depend on absolute counts in the fixture.

## Prerequisites the user must have

- `yaci-cli` on `PATH` (typically at `~/.local/bin/yaci-cli`)
- `java` (matching the `<java.version>` in the parent pom), `mvn`
- yaci-cli has previously been run at least once so `~/.yaci-cli/components/store/yaci-store.jar` exists

If any are missing, stop and tell the user — do not try to install them.

## What to do

### 1. Run the orchestration script

```bash
.claude/skills/regenerate-testdata-db/regen-devkit-db.sh
```

The script handles everything: pre-cleans `~/.yaci-cli/local-clusters/`, starts yaci-cli with `topup_addresses` + `yaci_store_enabled=true`, waits for the BlockFrost API on `:8080` to index past the topup, builds rosetta with `mvn -pl test-data-generator,yaci-indexer -am install -DskipTests`, runs the indexer against the devnet (writes to `testData/devkit.db`), runs `TestDataGeneratorApp`, cleanly shuts the indexer, applies the genesis-block SQL workaround, and verifies `cip26_metadata` + `cip68_metadata` are present.

Wall-clock time: ~3 minutes. Run it in the background and monitor `build/regen-logs/*.log` for progress.

### 2. Diagnose if the script fails

The script emits coloured `[regen HH:MM:SS]` log lines and exits non-zero on the first failed step. Look at:

- `build/regen-logs/yaci-cli.log` — node + yaci-store boot, topup transactions
- `build/regen-logs/yaci-indexer.log` — rosetta yaci-indexer (most likely culprit)
- `build/regen-logs/test-data-generator.log` — transaction-submission errors
- `build/regen-logs/h2-patch.log` — H2 shell output for the genesis SQL workaround

Common failure modes:

- **Port conflict on 3001 / 8080 / 8090 / 10000**: the script's preflight `pkill`s `cardano-submit`, `cardano-node`, `yaci-store`, `yaci-indexer`, but if a non-standard process is bound there it won't be cleared.
- **"Yaci Store is only supported for 'default' cluster"**: the script hard-codes `CLUSTER_NAME=default` for this reason. Don't change it.
- **Stale `~/.yaci-cli/local-clusters/default`**: the script's preflight removes this. If you see "already exists" warnings, the user has a non-default `clusters` location and the cleanup is wrong.
- **`Shelley genesis file not found at path : /config/byron-genesis.json`**: the script passes `--store.cardano.{byron,shelley,alonzo,conway}-genesis-file` paths into the indexer. If they fail, the genesis dir layout has changed in yaci-cli — check `~/.yaci-cli/local-clusters/default/node/genesis/`.
- **`No qualifying bean of type 'AccountService'`**: the indexer's `AccountServiceImpl` is `@ConditionalOnExpression("'${store.cardano.n2c-node-socket-path:}' != '' || '${store.cardano.n2c-host:}' != ''")`. The script wires `--store.cardano.n2c-node-socket-path` to yaci-cli's socket; if the path moved, fix it in the script.

### 3. Run the test suite

Once the script reports `Regen complete`:

```bash
mvn -pl api -am test
```

Expect ~995 tests. Some will fail because the new fixture has different absolute counts / chain-tip slot. These are *fixture-drift* failures, not regressions — work through them one by one with the categories below.

### 4. Categorise and fix the fixture-drift failures

These four categories cover everything we've seen so far. For each, the fix is a one-line edit, not a refactor.

**A. Hardcoded transaction counts** — assertions like `total_count == 27`.
The new fixture has a different transaction count (we last regenerated to 45). Query the new fixture to get the truth:
```bash
java -cp testData/h2-2.2.224.jar org.h2.tools.Shell \
  -url "jdbc:h2:file:$(pwd)/testData/devkit.db" \
  -user rosetta_db_admin -password 'weakpwd#123_d' \
  -sql "SELECT COUNT(*) FROM TRANSACTION"
```
Use `Edit` with `replace_all=true` on the literal count if it appears multiple times in one test class.

**B. Positional balance / coin assertions** — e.g. `getBalances().get(2).getCurrency().getSymbol()` expecting a specific hex symbol.
The minting order on a freshly-regenerated devkit is *not deterministic*. Replace positional access with a `.filter(b -> "<hex>".equals(b.getCurrency().getSymbol())).findFirst()` lookup. Example: `AccountBalanceApiTest.accountBalanceMintedTokenAndEmptyName_TestORG`.

**C. Slot-dependent values** — `TTL`, `current_slot`, future-slot calculations.
The devkit chain tip slot at the moment `TestDataGeneratorApp` finishes is fixture-dependent (currently ~145). Tests that assert an absolute TTL value need updating to the new chain-tip. Get the current tip with:
```bash
java -cp testData/h2-2.2.224.jar org.h2.tools.Shell \
  -url "jdbc:h2:file:$(pwd)/testData/devkit.db" \
  -user rosetta_db_admin -password 'weakpwd#123_d' \
  -sql "SELECT MAX(SLOT), MAX(NUMBER) FROM BLOCK"
```
Then update the absolute expectation. Example: `MetadataApiTest.combineWithMetadataTest`.

**D. Token-registry decimals** — tests expecting `decimals == 6` for `MyAsset` or similar minted-on-devkit tokens.
The devkit fixture does **not** include CIP-26 entries for its minted tokens, so `TokenRegistryService` returns the default `decimals=0`. The intent of the test is fine but the assertion must match reality — change to `0` and add a comment that decimals resolution is covered separately by `TokenQueryServiceTest`. Example: `AccountCoinsApiTest.accountCoinsMultipleSpecifiedCurrencies_Test`.

### 5. Verify and commit

After the test suite is fully green:

```bash
mvn -pl api -am test          # confirm 0 failures, 0 errors
git status                    # devkit.db.mv.db, testdata.json, *Test.java files
git add testData/devkit.db.mv.db testData/testdata.json <changed test files>
```

Commit message convention to mirror the existing one (commit `84dbb9978`):

```
chore: regenerate devkit fixture against assets-ext V<N> schema

<one-paragraph why: which yaci-store bump triggered the regen>

<bullets: which test classes were adjusted and which category (A/B/C/D)
each adjustment belongs to>

Co-Authored-By: <whatever the user normally uses>
```

## Things NOT to do

- **Do not edit the H2 fixture by hand.** If you ever feel like opening `testData/devkit.db.mv.db` in a tool and tweaking a row, stop — regenerate end-to-end instead. Hand-edits break the determinism that `testdata.json` documents.
- **Do not commit `build/regen-logs/`.** It's in `.gitignore`.
- **Do not bump the yaci-store version *in this skill*.** That's a separate concern — version bumps live in `yaci-indexer/pom.xml`. This skill assumes the version is correct and the fixture needs to catch up to it.
- **Do not try to install yaci-cli or yaci-devkit.** If the user doesn't have it, tell them — installation instructions live in [yaci-cli](https://yaci-cli.bloxbean.com/) / [yaci-devkit](https://github.com/bloxbean/yaci-devkit).
