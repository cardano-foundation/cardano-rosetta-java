---
name: cardano-rosetta-java-regenerate-testdata-db
description: "Regenerate cardano-rosetta-java's H2 test fixture testData/devkit.db.mv.db and testData/testdata.json end-to-end, then fix any test assertions that drift because of the new fixture. Use when the user asks to 'regenerate testdata', 'regenerate testdata db', 'refresh test data', 'rebuild devkit.db', or when CI fails with errors like 'Table CIP26_METADATA not found' / 'Table FT_OFFCHAIN_METADATA not found' that indicate the committed fixture is stale relative to the current yaci-store schema."
---

# Regenerate testData DB

The committed fixture `testData/devkit.db.mv.db` is produced by running rosetta's own `yaci-indexer` against a freshly-booted yaci-cli devnet. Whenever the yaci-store dependency version (in `yaci-indexer/pom.xml`) is bumped and the schema changes, that fixture becomes stale and integration tests fail with table-not-found / 500 errors.

This skill regenerates the fixture, then fixes the small handful of test assertions that depend on absolute counts in the fixture.

## Prerequisites the user must have

- `yaci-cli` on `PATH` (tested with 0.12.0), with its components downloaded once (`download` inside yaci-cli; it reads `config/download.properties` from the directory it runs in, so run it — and this script — from the yaci-cli install directory if that is where its `config/` folder lives)
- yaci-cli's `cardano-node` must run on the machine: on Apple Silicon it downloads an x86_64 build, which needs Rosetta 2 (`softwareupdate --install-rosetta`); on Linux x86_64 it runs natively
- `java` (matching the `<java.version>` in the parent pom), `mvn`
- Nothing else listening on 3001 / 8080 / 8090 / 10000 (yaci-cli's node, yaci-store, submit API, admin). Stop a local rosetta docker stack first (`docker compose ... stop`, start it again afterwards)

If any are missing, stop and tell the user — do not try to install them without asking.

## What the script touches on the machine

- Kills only the devnet's own processes: `~/.yaci-cli/cardano-node/bin/cardano-node`, `.../cardano-submit-api`, `~/.yaci-cli/components/store/...` and `yaci-indexer/target/yaci-indexer-*`
- Moves `~/.yaci-cli/local-clusters` and `~/.yaci-cli/pids` aside to `*.bak-<timestamp>` (yaci-cli's `create-node` fails if the cluster exists); delete or restore them afterwards
- Deletes and rewrites `testData/devkit.db.mv.db` (restore with `git checkout -- testData` if the run fails)

## Optional environment

| Variable | Use |
|---|---|
| `REPO_ROOT` | cardano-rosetta-java checkout (default: derived from the script path) |
| `H2_JAR` | H2 jar for the SQL patch / verify steps (default: the project's own `h2` dependency, resolved with `mvn dependency:build-classpath`, so the file format matches) |
| `INDEXER_EXTRA_ARGS` | extra rosetta yaci-indexer arguments, e.g. `--server.port=19096` when 9095 is taken. Do not export `SERVER_PORT` instead: yaci-cli and yaci-store are Spring Boot apps too and would pick it up |
| `MAVEN_OPTS` | e.g. `-Dmaven.repo.local=...` to use a separate local Maven repository |

## What to do

### 1. Run the orchestration script

```bash
.claude/skills/cardano-rosetta-java-regenerate-testdata-db/regen-devkit-db.sh
```

The script handles everything: moves `~/.yaci-cli/local-clusters/` aside, starts yaci-cli with `topup_addresses` + `yaci_store_enabled=true`, waits for the BlockFrost API on `:8080` to index past the topup, builds rosetta with `mvn -pl test-data-generator,yaci-indexer -am install -DskipTests`, runs the indexer against the devnet (writes to `testData/devkit.db`), runs `TestDataGeneratorApp`, cleanly shuts the indexer, applies the genesis-block SQL workaround, and verifies `cip26_metadata` + `cip68_metadata` are present.

Wall-clock time: ~3 minutes. Run it in the background and monitor `build/regen-logs/*.log` for progress.

### 2. Diagnose if the script fails

The script emits coloured `[regen HH:MM:SS]` log lines and exits non-zero on the first failed step. Look at:

- `build/regen-logs/yaci-cli.log` — node + yaci-store boot, topup transactions
- `build/regen-logs/yaci-indexer.log` — rosetta yaci-indexer (most likely culprit)
- `build/regen-logs/test-data-generator.log` — transaction-submission errors
- `build/regen-logs/h2-patch.log` — H2 shell output for the genesis SQL workaround

Common failure modes:

- **Port conflict on 3001 / 8080 / 8090 / 10000**: the script only stops the devnet's own processes (and frees the ports with `lsof` where available); anything else bound there, e.g. a rosetta docker stack, must be stopped first.
- **`Port 9095 was already in use`** (rosetta yaci-indexer): pass `INDEXER_EXTRA_ARGS="--server.port=<free port>"`.
- **`bad CPU type in executable`** (macOS): yaci-cli's `cardano-node` is x86_64; install Rosetta 2 or run on Linux x86_64.
- **`Yaci Store process started: timed out`**: check `yaci-cli.log` for `Starting Yaci Store: started`. yaci-cli changed this message between versions; the script accepts the known variants.
- **`Table "PG_INDEX" not found`**: the indexer picked `PgIndexService` on H2; `h2-testdata` must be in `NoOpIndexService`'s `@Profile` (fixed in 3.0.0).
- **`StakePoolRetirementWrongEpochPOOL`** (test-data-generator): the retirement epoch must be after the current epoch; `Step2_PoolTransactions` uses the latest block's epoch + 1.
- **"Yaci Store is only supported for 'default' cluster"**: the script hard-codes `CLUSTER_NAME=default` for this reason. Don't change it.
- **Stale `~/.yaci-cli/local-clusters/default`**: the script's preflight removes this. If you see "already exists" warnings, the user has a non-default `clusters` location and the cleanup is wrong.
- **`Shelley genesis file not found at path : /config/byron-genesis.json`**: the script passes `--store.cardano.{byron,shelley,alonzo,conway}-genesis-file` paths into the indexer. If they fail, the genesis dir layout has changed in yaci-cli — check `~/.yaci-cli/local-clusters/default/node/genesis/`.
- **`No qualifying bean of type 'AccountService'`**: the indexer's `AccountServiceImpl` is `@ConditionalOnExpression("'${store.cardano.n2c-node-socket-path:}' != '' || '${store.cardano.n2c-host:}' != ''")`. The script wires `--store.cardano.n2c-node-socket-path` to yaci-cli's socket; if the path moved, fix it in the script.

### 3. Run the test suite

Once the script reports `Regen complete`:

```bash
mvn -pl api -am test
```

Expect ~1099 tests. Some will fail because the new fixture has different absolute counts / chain-tip slot. These are *fixture-drift* failures, not regressions — work through them one by one with the categories below.

### 4. Categorise and fix the fixture-drift failures

The queries below use the project's H2 jar:
```bash
H2_JAR="$(mvn -q -pl yaci-indexer dependency:build-classpath -Dmdep.outputFile=/dev/stdout | tr ':' '\n' | grep -E '/h2-[0-9.]+\.jar$' | head -1)"
```

These four categories cover everything we've seen so far. For each, the fix is a one-line edit, not a refactor.

**A. Hardcoded transaction counts** — assertions like `total_count == 27`.
The new fixture has a different transaction count (we last regenerated to 51). Query the new fixture to get the truth:
```bash
java -cp "$H2_JAR" org.h2.tools.Shell \
  -url "jdbc:h2:file:$(pwd)/testData/devkit.db" \
  -user rosetta_db_admin -password 'weakpwd#123_d' \
  -sql "SELECT COUNT(*) FROM TRANSACTION"
```
Use `Edit` with `replace_all=true` on the literal count if it appears multiple times in one test class.

**B. Positional balance / coin assertions** — e.g. `getBalances().get(2).getCurrency().getSymbol()` expecting a specific hex symbol.
The minting order on a freshly-regenerated devkit is *not deterministic*. Replace positional access with a `.filter(b -> "<hex>".equals(b.getCurrency().getSymbol())).findFirst()` lookup. Example: `AccountBalanceApiTest.accountBalanceMintedTokenAndEmptyName_TestORG`.

**C. Slot-dependent values** — `TTL`, `current_slot`, future-slot calculations.
The devkit chain tip slot at the moment `TestDataGeneratorApp` finishes is fixture-dependent (currently 1950; yaci-cli 0.12 shifts genesis back a few epochs, so slots are higher than with older yaci-cli). Tests that assert an absolute TTL value need updating to the new chain-tip. Get the current tip with:
```bash
java -cp "$H2_JAR" org.h2.tools.Shell \
  -url "jdbc:h2:file:$(pwd)/testData/devkit.db" \
  -user rosetta_db_admin -password 'weakpwd#123_d' \
  -sql "SELECT MAX(SLOT), MAX(NUMBER) FROM BLOCK"
```
Then update the absolute expectation (`MAX(SLOT)` + 10). Example: `MetadataApiTest.combineWithMetadataTest`.

**D. Token-registry decimals** — tests expecting `decimals == 6` for `MyAsset` or similar minted-on-devkit tokens.
The devkit fixture does **not** include CIP-26 entries for its minted tokens, so `TokenRegistryService` returns the default `decimals=0`. The intent of the test is fine but the assertion must match reality — change to `0` and add a comment that decimals resolution is covered separately by `TokenQueryServiceTest`. Example: `AccountCoinsApiTest.accountCoinsMultipleSpecifiedCurrencies_Test`.

### CIP-68 test tokens

`Step4_Cip68Transactions` mints a CIP-68 333 token (`RosettaFT`) and a 444 token (`RosettaRFT`) on a deterministic policy (SENDER_1's payment key), paid by the CIP-68 holder (`TestConstants.CIP68_HOLDER_ADDRESS`, account 1 of SENDER_1's mnemonic, topped up by the script as a third address). `TokenQueryServiceImplIntTest` reads them from the fixture. If it fails after a regeneration, check that `cip68_metadata` has two rows (labels 333 and 444).

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
