#!/usr/bin/env bash
#
# Regenerate testData/devkit.db.mv.db and testData/testdata.json end-to-end.
#
# What this does:
#   1. Pre-cleans yaci-cli cluster state, kills stale processes, removes old H2 file.
#   2. Boots yaci-cli local devnet (cardano node + yaci-store BlockFrost API).
#      Topups the two test addresses via the topup_addresses env var.
#   3. Boots rosetta-java's own yaci-indexer with the h2-testdata profile so it
#      writes to ./testData/devkit.db (assets-ext V2 schema).
#   4. Runs the test-data-generator main, which submits transactions through the
#      BlockFrost-compatible API on :8080 and walks the indexed data.
#   5. Cleanly stops the indexer (so H2 flushes) and applies the genesis-block
#      SQL workaround.
#   6. Verifies the resulting devkit.db.mv.db has the new cip26_metadata and
#      cip68_metadata tables.
#
# Pre-requisites:
#   - yaci-cli on PATH (https://yaci-cli.bloxbean.com/)
#   - Java 25 (matches the rosetta build)
#   - Maven on PATH
#   - This script run from the repo root (cardano-rosetta-java/)

set -euo pipefail

# ---- knobs ------------------------------------------------------------------
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="${REPO_ROOT}/build/regen-logs"
H2_FILE="${REPO_ROOT}/testData/devkit.db.mv.db"
H2_TRACE="${REPO_ROOT}/testData/devkit.db.trace.db"
H2_JAR="${REPO_ROOT}/testData/h2-2.2.224.jar"
CLUSTERS_DIR="${HOME}/.yaci-cli/local-clusters"
PIDS_DIR="${HOME}/.yaci-cli/pids"
# yaci-cli only enables its bundled yaci-store (BlockFrost API on :8080) when the
# cluster is named "default". TestDataGenerator depends on that API, so don't
# rename this.
CLUSTER_NAME="default"

# Pulled from test-data-generator TestConstants:
ADDR1="addr_test1qz5t8wq55e09usmh07ymxry8atzwxwt2nwwzfngg6esffxvw2pfap6uqmkj3n6zmlrsgz397md2gt7yqs5p255uygaesx608y5"
ADDR2="addr_test1qp73ljurtknpm5fgey5r2y9aympd33ksgw0f8rc5khheg83y35rncur9mjvs665cg4052985ry9rzzmqend9sqw0cdksxvefah"

# Ports yaci-cli local cluster exposes:
BF_URL="http://localhost:8080/api/v1/blocks/latest"
ADMIN_URL="http://localhost:10000/local-cluster/api"
NODE_PORT=3001

YACI_PID=""
INDEXER_PID=""

# ---- helpers ----------------------------------------------------------------
log()  { printf '\033[1;36m[regen %s]\033[0m %s\n' "$(date +%H:%M:%S)" "$*"; }
warn() { printf '\033[1;33m[regen %s WARN]\033[0m %s\n' "$(date +%H:%M:%S)" "$*"; }
err()  { printf '\033[1;31m[regen %s ERROR]\033[0m %s\n' "$(date +%H:%M:%S)" "$*" >&2; }

cleanup() {
  local code=$?
  log "Cleanup (exit=$code)"
  [[ -n "$INDEXER_PID" ]] && kill "$INDEXER_PID" 2>/dev/null || true
  [[ -n "$YACI_PID"    ]] && kill "$YACI_PID"    2>/dev/null || true
  # yaci-cli forks cardano-node, cardano-submit-api, and yaci-store; killing the
  # parent leaves those orphans alive and they hold our ports.
  pkill -TERM -f "cardano-submit" 2>/dev/null || true
  pkill -TERM -f "cardano-node"   2>/dev/null || true
  pkill -TERM -f "yaci-store"     2>/dev/null || true
  pkill -TERM -f "yaci-indexer"   2>/dev/null || true
  sleep 2
  [[ -n "$INDEXER_PID" ]] && kill -9 "$INDEXER_PID" 2>/dev/null || true
  [[ -n "$YACI_PID"    ]] && kill -9 "$YACI_PID"    2>/dev/null || true
  pkill -9 -f "cardano-submit" 2>/dev/null || true
  pkill -9 -f "cardano-node"   2>/dev/null || true
  pkill -9 -f "yaci-store"     2>/dev/null || true
  pkill -9 -f "yaci-indexer"   2>/dev/null || true
  exit $code
}
trap cleanup EXIT INT TERM

wait_until() {
  local label="$1"; shift
  local deadline_secs="$1"; shift
  local interval="$1"; shift
  # remaining args = command to check
  local start=$SECONDS
  while (( SECONDS - start < deadline_secs )); do
    if "$@" >/dev/null 2>&1; then
      log "  ${label}: ready after $((SECONDS - start))s"
      return 0
    fi
    sleep "$interval"
  done
  err "  ${label}: timed out after ${deadline_secs}s"
  return 1
}

# ---- step 0: preflight ------------------------------------------------------
log "Preflight"
command -v yaci-cli >/dev/null || { err "yaci-cli not on PATH"; exit 2; }
command -v mvn      >/dev/null || { err "mvn not on PATH"; exit 2; }
command -v java     >/dev/null || { err "java not on PATH"; exit 2; }
[[ -f "$H2_JAR" ]] || { err "Missing $H2_JAR"; exit 2; }

mkdir -p "$LOG_DIR"

# Kill any stale processes that would clash with our ports. yaci-cli spawns
# cardano-node, cardano-submit-api, yaci-store, and the indexer as long-lived
# subprocesses — pkill them by name first since they may not show up in lsof's
# port view if they're transitioning.
pkill -f "cardano-submit"  2>/dev/null || true
pkill -f "cardano-node"    2>/dev/null || true
pkill -f "yaci-store"      2>/dev/null || true
pkill -f "yaci-indexer"    2>/dev/null || true
sleep 1

for port in 3001 8080 8090 10000; do
  if lsof -ti tcp:$port >/dev/null 2>&1; then
    warn "  killing stale process on :$port"
    lsof -ti tcp:$port | xargs kill -9 2>/dev/null || true
  fi
done

# Remove every existing cluster + leftover PID files. yaci-cli's create-node
# bails with "already exists" if any directory remains, and stale PID files cause
# the node to think a previous instance is still alive.
rm -rf "${CLUSTERS_DIR}"
rm -rf "${PIDS_DIR}"
rm -f  "$H2_FILE" "$H2_TRACE"
log "  pre-cleaned cluster + H2 + PIDs"

# ---- step 1: start yaci-cli devnet ------------------------------------------
log "Starting yaci-cli devnet (cluster: $CLUSTER_NAME)"

# These two env vars drive yaci-cli's bundled FirstRunTopupAccounts listener
# and enable yaci-store (port 8080 BlockFrost API needed by TestDataGenerator).
export yaci_store_enabled=true
export topup_addresses="${ADDR1}:110000,${ADDR2}:110000"

# Drive yaci-cli non-interactively by piping commands. Final 'exit' is omitted —
# we hold the process alive so the node stays up while we run the indexer.
{
  echo "create-node ${CLUSTER_NAME}"
  echo "node ${CLUSTER_NAME}"
  echo "start"
  # block forever so the shell doesn't exit; we'll SIGTERM it on cleanup
  while true; do sleep 60; echo info; done
} | yaci-cli >"${LOG_DIR}/yaci-cli.log" 2>&1 &
YACI_PID=$!
log "  yaci-cli PID: $YACI_PID"

# yaci-cli starts the cardano node, runs the topup transactions, then starts
# yaci-store. We need yaci-store fully up + caught up to current tip before
# TestDataGenerator submits transactions through it.
wait_until "Yaci Store process started" 180 2 grep -q "Yaci Store Started" "${LOG_DIR}/yaci-cli.log"

# Even after yaci-store starts, it takes a moment to index the topup blocks.
# Poll BlockFrost /blocks/latest until block number is > 5 (the two topup
# transactions land within the first few blocks).
wait_until "BlockFrost indexed past topup" 180 2 bash -c "
  curl -sf '$BF_URL' | grep -qE '\"height\"\s*:\s*([5-9]|[1-9][0-9]+)'
"

# ---- step 2: build rosetta jars (cached after first run) --------------------
log "Building rosetta-java (test-data-generator + yaci-indexer)"
( cd "$REPO_ROOT" && mvn -pl test-data-generator,yaci-indexer -am install -DskipTests -q ) \
  >"${LOG_DIR}/mvn-install.log" 2>&1

# ---- step 3: start rosetta yaci-indexer with h2-testdata profile -----------
log "Starting rosetta yaci-indexer (h2-testdata profile)"
INDEXER_JAR="$(find "${REPO_ROOT}/yaci-indexer/target" -maxdepth 1 -name 'yaci-indexer-*.jar' -not -name '*sources*' -not -name '*plain*' | head -1)"
[[ -f "$INDEXER_JAR" ]] || { err "yaci-indexer jar not found"; exit 3; }

# Indexer connects to yaci-cli's local node via n2n on :3001. The AccountService
# bean is also gated on n2c socket settings being non-empty (without it the
# entire ApplicationContext refuses to start because AccountResource requires
# the service), so we wire the n2c socket too.
NODE_DIR="${HOME}/.yaci-cli/local-clusters/${CLUSTER_NAME}/node"
NODE_SOCKET="${NODE_DIR}/node.sock"
GENESIS_DIR="${NODE_DIR}/genesis"
(
  cd "$REPO_ROOT"
  java -jar "$INDEXER_JAR" \
    --spring.profiles.active=h2-testdata \
    --store.cardano.host=localhost \
    --store.cardano.port=${NODE_PORT} \
    --store.cardano.protocol-magic=42 \
    --store.cardano.n2c-node-socket-path="${NODE_SOCKET}" \
    --store.cardano.byron-genesis-file="${GENESIS_DIR}/byron-genesis.json" \
    --store.cardano.shelley-genesis-file="${GENESIS_DIR}/shelley-genesis.json" \
    --store.cardano.alonzo-genesis-file="${GENESIS_DIR}/alonzo-genesis.json" \
    --store.cardano.conway-genesis-file="${GENESIS_DIR}/conway-genesis.json" \
    >"${LOG_DIR}/yaci-indexer.log" 2>&1 &
  echo $! > "${LOG_DIR}/indexer.pid"
) &
sleep 3
INDEXER_PID="$(cat "${LOG_DIR}/indexer.pid")"
log "  yaci-indexer PID: $INDEXER_PID"

wait_until "indexer started" 60 2 grep -q "Started YaciIndexerApplication" "${LOG_DIR}/yaci-indexer.log"

# Give indexer time to catch up to the topup blocks before TestDataGenerator runs.
sleep 8

# ---- step 4: run TestDataGenerator ------------------------------------------
log "Running TestDataGenerator"
( cd "$REPO_ROOT" && mvn -pl test-data-generator exec:java \
    -Dexec.mainClass=org.cardanofoundation.rosetta.testgenerator.TestDataGeneratorApp \
    -q ) >"${LOG_DIR}/test-data-generator.log" 2>&1

# Let the indexer catch up to the transactions just submitted.
log "Waiting for indexer to catch up to latest tx blocks..."
sleep 15

# ---- step 5: clean shutdown indexer (so H2 flushes) -------------------------
log "Stopping yaci-indexer cleanly"
kill -TERM "$INDEXER_PID" || true
# Wait until the process is gone OR up to 30s.
for _ in $(seq 1 30); do
  kill -0 "$INDEXER_PID" 2>/dev/null || break
  sleep 1
done
INDEXER_PID=""

# ---- step 6: apply genesis-block SQL workaround -----------------------------
log "Applying genesis-block SQL workaround"
java -cp "$H2_JAR" org.h2.tools.Shell \
  -url "jdbc:h2:file:${REPO_ROOT}/testData/devkit.db" \
  -user rosetta_db_admin -password "weakpwd#123_d" \
  -sql "UPDATE block SET number = 0 WHERE number = -1; UPDATE transaction SET block = 0 WHERE block_hash = 'Genesis';" \
  >"${LOG_DIR}/h2-patch.log" 2>&1

# ---- step 7: verify ---------------------------------------------------------
log "Verifying schema"
TABLES="$(java -cp "$H2_JAR" org.h2.tools.Shell \
  -url "jdbc:h2:file:${REPO_ROOT}/testData/devkit.db" \
  -user rosetta_db_admin -password "weakpwd#123_d" \
  -sql "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' ORDER BY TABLE_NAME" 2>/dev/null \
  | tr '[:upper:]' '[:lower:]')"

for required in cip26_metadata cip68_metadata; do
  if ! grep -qw "$required" <<<"$TABLES"; then
    err "  schema verify FAILED: table '$required' not found"
    err "  tables present: $(echo "$TABLES" | tr '\n' ' ')"
    exit 4
  fi
  log "  ✓ $required present"
done

log "Regen complete. Files:"
log "  $H2_FILE"
log "  ${REPO_ROOT}/testData/testdata.json"
