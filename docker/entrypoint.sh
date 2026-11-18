#!/bin/bash

function clean_up() {
  # Killing all processes before exiting
  kill -2 "$CARDANO_NODE_PID" "$API_PID" "$MITHRIL_PID" "$CARDANO_SUBMIT_PID" "$YACI_STORE_PID"
  wait $CARDANO_NODE_PID
  exit
}

trap clean_up SIGHUP SIGINT SIGTERM

# NODE FUNCTIONS
show_progress() {
    message="$1"; percent="$2"
    done=$(bc <<< "scale=0; 40 * ${percent%.*} / 100" )
    todo=$(bc <<< "scale=0; 40 - $done" )
    done_sub_bar=$(printf "%${done}s" | tr " " "#")
    todo_sub_bar=$(printf "%${todo}s" | tr " " "-")
    echo -ne "$message [${done_sub_bar}${todo_sub_bar}] ${percent}%"\\r
}

node_verification() {
    echo "Stating Cardano node verification..."

    REGEX_VALIDATED="^.?\[.*\].?\[.*\].?(Validating|Validated) chunk no\. ([0-9]+) out of ([0-9]+)\. Progress: ([0-9]+\.[0-9][0-9])\%.*$"
    REGEX_REPLAYED="^.?\[.*\].?\[.*\].?Replayed block: slot ([0-9]+) out of ([0-9]+)\. Progress: ([0-9]+\.[0-9][0-9])\%.*$"
    REGEX_PUSHING="^.?\[.*\].?\[.*\].?Pushing ledger state for block ([a-f0-9]+) at slot ([0-9]+)\. Progress: ([0-9]+\.[0-9][0-9])\%.*$"
    REGEX_STARTED="^.?\[.*\].?\[.*\].?(Started .*)$"

    while [ ! -S "$CARDANO_NODE_SOCKET_PATH" ]; do
        new_line=$(tail -n 1 /logs/node.log)
        if [ "${new_line}" == "${line}" ]; then continue; fi
        line=$new_line
        if [[ "$line" =~ $REGEX_VALIDATED ]]; then
            show_progress "Node verification: Chunk ${BASH_REMATCH[2]}/${BASH_REMATCH[3]}" ${BASH_REMATCH[4]}
        elif [[ "$line" =~ $REGEX_REPLAYED ]]; then
            show_progress "Replayed block: Block ${BASH_REMATCH[1]}/${BASH_REMATCH[2]}"  ${BASH_REMATCH[3]}
        elif [[ "$line" =~ $REGEX_PUSHING ]]; then
            show_progress "Pushing ledger state: Slot ${BASH_REMATCH[2]}" ${BASH_REMATCH[3]}
        elif [[ "$line" =~ $REGEX_STARTED ]]; then
            echo -e "${BASH_REMATCH[1]}..."
        fi
        sleep 1
    done
    echo "Node verification: DONE"
}

node_synchronization() {
    echo -e "Starting Cardano node synchronization..."
    epoch_length=$(jq -r .epochLength $GENESIS_SHELLEY_PATH)
    slot_length=$(jq -r .slotLength $GENESIS_SHELLEY_PATH)
    byron_slot_length=$(( $(jq -r .blockVersionData.slotDuration $GENESIS_BYRON_PATH) / 1000 ))
    byron_epoch_length=$(( $(jq -r .protocolConsts.k $GENESIS_BYRON_PATH) * 10 ))
    byron_start=$(jq -r .startTime $GENESIS_BYRON_PATH)
    byron_end=$((byron_start + $HARDFORK_EPOCH * byron_epoch_length * byron_slot_length))
    byron_slots=$(($HARDFORK_EPOCH * byron_epoch_length))
    now=$(date +'%s')
    expected_slot=$((byron_slots + (now - byron_end) / slot_length))

    sync_progress=0
    while (( ${sync_progress%.*} < 100 )); do
        current_status=$(cardano-cli query tip $NETWORK_STR)
        current_slot=$(echo $current_status | jq -r '.slot')
        sync_progress=$(echo $current_status | jq -r '.syncProgress')

        show_progress "Node synchronization: Slot $current_slot/$expected_slot" $sync_progress
        sleep 1
    done
    echo "Node synchronization: DONE"
}

# POSTGRES FUNCTIONS
database_initialization() {
    echo "postgres" > /tmp/password
    echo "*:*:*:postgres:postgres" > /home/postgres/.pgpass

    chmod 600 /home/postgres/.pgpass
    chown postgres:postgres /home/postgres/.pgpass

    if [ -z "$(ls -A "$PG_DATA")" ]; then
        sudo -u postgres "$PG_BIN/initdb" --pgdata="$PG_DATA" --auth=md5 --auth-local=md5 --auth-host=md5 --username=postgres --pwfile=/tmp/password
    else
        echo "Database already initialized, skipping initdb."
    fi

    rm -f /tmp/password
}

configure_postgres() {
    local config_file="$PG_DATA/postgresql.conf"

    # List of parameter names and their corresponding environment variables
    declare -A param_vars=(
        ["max_connections"]="DB_POSTGRES_MAX_CONNECTIONS"
        ["shared_buffers"]="DB_POSTGRES_SHARED_BUFFERS"
        ["effective_cache_size"]="DB_POSTGRES_EFFECTIVE_CACHE_SIZE"
        ["work_mem"]="DB_POSTGRES_WORK_MEM"
        ["maintenance_work_mem"]="DB_POSTGRES_MAINTENANCE_WORK_MEM"
        ["wal_buffers"]="DB_POSTGRES_WAL_BUFFERS"
        ["checkpoint_completion_target"]="DB_POSTGRES_CHECKPOINT_COMPLETION_TARGET"
        ["random_page_cost"]="DB_POSTGRES_RANDOM_PAGE_COST"
        ["effective_io_concurrency"]="DB_POSTGRES_EFFECTIVE_IO_CONCURRENCY"
        ["parallel_tuple_cost"]="DB_POSTGRES_PARALLEL_TUPLE_COST"
        ["parallel_setup_cost"]="DB_POSTGRES_PARALLEL_SETUP_COST"
        ["max_parallel_workers_per_gather"]="DB_POSTGRES_MAX_PARALLEL_WORKERS_PER_GATHER"
        ["max_parallel_workers"]="DB_POSTGRES_MAX_PARALLEL_WORKERS"
        ["seq_page_cost"]="DB_POSTGRES_SEQ_PAGE_COST"
        ["jit"]="DB_POSTGRES_JIT"
        ["bgwriter_lru_maxpages"]="DB_POSTGRES_BGWRITER_LRU_MAXPAGES"
        ["bgwriter_delay"]="DB_POSTGRES_BGWRITER_DELAY"
    )

    # Check for missing required environment variables
    local missing_vars=()
    for param in "${!param_vars[@]}"; do
        local env_var="${param_vars[$param]}"
        if [ -z "${!env_var}" ]; then
            missing_vars+=("$env_var")
        fi
    done

    # If there are missing variables, print an error and exit
    if [ ${#missing_vars[@]} -gt 0 ]; then
        echo "Error: The following required environment variables are missing or empty:"
        for var in "${missing_vars[@]}"; do
            echo "   - $var"
        done
        echo ""
        echo "Most likely, you are missing a hardware profile in your environment configuration."
        echo "Make sure to pass an additional --env-file parameter when running the container."
        echo ""
        echo "Example Docker run command for an 'entry_level' hardware profile:"
        echo "docker run --env-file ./docker/.env.dockerfile --env-file ./docker/.env.docker-profile-mid-level -p 8082:8082 --shm-size=4g -d cardanofoundation/cardano-rosetta-java:latest"
        echo ""
        exit 1
    fi

    # Remove all commented-out lines for the parameters we are about to update
    for param in "${!param_vars[@]}"; do
        sed -i "/^#$param /d" "$config_file"
    done

    # Update or add parameters
    for param in "${!param_vars[@]}"; do
        local env_var="${param_vars[$param]}"
        local value="${!env_var}"

        # Try to replace existing parameter
        sed -i "s/^$param = .*/$param = $value/" "$config_file"

        # Parameter not found, append it
        if ! grep -q "^$param =" "$config_file"; then
            echo "$param = $value" >> "$config_file"
        fi
    done

    if ! grep -q "^host all all 0.0.0.0/0 md5\$" "$PG_DATA/pg_hba.conf"; then
        echo "host all all 0.0.0.0/0 md5" >> "$PG_DATA/pg_hba.conf"
    fi

    if ! grep -q "^listen_addresses *= *'\*'\$" "$PG_DATA/postgresql.conf"; then
        echo "listen_addresses='*'" >> "$PG_DATA/postgresql.conf"
    fi

    echo "PostgreSQL configuration updated successfully!"
}

start_postgres() {
    sudo -u postgres "$PG_BIN/postgres" -D "$PG_DATA" -p "$DB_PORT" -c config_file="$PG_DATA/postgresql.conf" > /logs/postgres.log 2>&1 &
    POSTGRES_PID=$!

    until "$PG_BIN/pg_isready" -U postgres > /dev/null; do sleep 1; done
}

create_database_and_user() {
    export DB_SCHEMA="$NETWORK"

    flag=true
    while [ $(sudo -u postgres "$PG_BIN/psql" -U postgres -Atc "SELECT pg_is_in_recovery()";) == "t" ]; do
        if $flag ; then
            echo "Waiting for database recovery..."
            flag=false
        fi
        sleep 1
    done

    if [[ -z $(sudo -u postgres "$PG_BIN/psql" -U postgres -Atc "SELECT 1 FROM pg_catalog.pg_user WHERE usename = '$DB_USER'";) ]]; then
        echo "Creating database..."
        sudo -u postgres "$PG_BIN/psql" -U postgres -c "CREATE ROLE \"$DB_USER\" with LOGIN CREATEDB PASSWORD '$DB_SECRET';" > /dev/null
    fi

    if [[ -z $(sudo -u postgres "$PG_BIN/psql" -U postgres -Atc "SELECT 1 FROM pg_catalog.pg_database WHERE datname = '$DB_NAME'";) ]]; then
        echo "Creating user..."
        sudo -u postgres "$PG_BIN/psql" -U postgres -c "CREATE DATABASE \"$DB_NAME\";" >/dev/null
        sudo -u postgres "$PG_BIN/psql" -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"$DB_NAME\" to \"$DB_USER\";" > /dev/null
    fi

    echo "User configured"
}

# API FUNCTIONS
get_current_index() {
    json="{\"network_identifier\":{\"blockchain\":\"cardano\",\"network\":\"${NETWORK}\"},\"metadata\":{}}"
    response=$(curl -s -X POST -H "Content-Type: application/json" -H "Content-length: 1000" -H "Host: localhost.com" --data "$json" "localhost:{$API_PORT}/network/status")
    current_index=$(echo $response | jq -r '.current_block_identifier.index')
    if [[ -z "$current_index" || "$current_index" == "null" ]]; then current_index=0; fi
}

# MITHRIL FUNCTIONS
download_mithril_snapshot() {
    echo "Downloading Mithril Snapshot..."
    export CARDANO_NETWORK=$NETWORK
    case $NETWORK in
    mainnet)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.release-mainnet.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-mainnet/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-mainnet/ancillary.vkey)}
      ;;
    preprod)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.release-preprod.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-preprod/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/release-preprod/ancillary.vkey)}
      ;;
    preview)
      AGGREGATOR_ENDPOINT=${AGGREGATOR_ENDPOINT:-https://aggregator.pre-release-preview.api.mithril.network/aggregator}
      GENESIS_VERIFICATION_KEY=${GENESIS_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/pre-release-preview/genesis.vkey)}
      ANCILLARY_VERIFICATION_KEY=${ANCILLARY_VERIFICATION_KEY:-$(wget -q -O - https://raw.githubusercontent.com/input-output-hk/mithril/main/mithril-infra/configuration/pre-release-testing-preview/ancillary.vkey)}
      ;;
    esac
    echo "Listing content of /node dir:"
    ls -la /node
    mithril-client cardano-db download latest --include-ancillary --ancillary-verification-key $ANCILLARY_VERIFICATION_KEY --download-dir /node > >(tee $logf) &
    MITHRIL_PID=$!
    wait $MITHRIL_PID
    echo "Done downloading Mithril Snapshot"
}

cp -r /networks/$NETWORK/* /config/
rm -f $CARDANO_NODE_SOCKET_PATH
if [ "$API_SPRING_PROFILES_ACTIVE" == "offline" ]; then
  echo "Starting in offline mode - No Database, Node, submit api or indexer will be started!"
else
  echo "Network: $NETWORK"
  if [ "$NETWORK" == "mainnet" ]; then
      NETWORK_STR="--mainnet"
      HARDFORK_EPOCH=208
  else
      NETWORK_STR="--testnet-magic $PROTOCOL_MAGIC"
      HARDFORK_EPOCH=1
  fi

  echo "Starting Cardano node..."
  mkdir -p /node/db

  if [ "${MITHRIL_SYNC}" == "true" ]; then
      if [ "$(ls -A /node/db)" ]; then
          echo "Mithril Snapshot already exists - Skipping Download..."
      else
        download_mithril_snapshot
      fi
  fi

  mkdir -p "$(dirname "$CARDANO_NODE_SOCKET_PATH")"
  sleep 1
  cardano-node run --socket-path "$CARDANO_NODE_SOCKET_PATH" --port $CARDANO_NODE_PORT --database-path /node/db --config /config/config.json --topology /config/topology.json > /logs/node.log &
  CARDANO_NODE_PID=$!
  sleep 2

  if [ "${VERIFICATION}" == "true" ] || [ "${SYNC}" == "true" ] ; then
      node_verification
  fi

  if [ "${SYNC}" == "true" ] ; then
      node_synchronization
  fi

  echo "Starting Cardano submit api..."
  cardano-submit-api --listen-address 0.0.0.0 --socket-path "$CARDANO_NODE_SOCKET_PATH" --port $NODE_SUBMIT_API_PORT $NETWORK_STR  --config /cardano-submit-api-config/cardano-submit-api.yaml > /logs/submit-api.log &
  CARDANO_SUBMIT_PID=$!

  echo "Initializing Database..."
  chown -R postgres:postgres "$PG_DATA"
  chmod -R 0700 "$PG_DATA"
  database_initialization

  echo "Configuring the Database..."
  configure_postgres

  echo "Starting Postgres..."
  start_postgres

  echo "Creating database and user..."
  create_database_and_user

  echo "Starting Yaci indexer..."
  exec java --enable-preview -jar /yaci-indexer/app.jar > /logs/indexer.log &
  YACI_STORE_PID=$!

fi
echo "Starting Rosetta API..."
exec java --enable-preview -jar /api/app.jar > /logs/api.log &
API_PID=$!

if [ "$API_SPRING_PROFILES_ACTIVE" == "online" ]; then
  echo "Waiting Rosetta API initialization..."
  sleep 5
  get_current_index
  while (( ! $current_index > 0 )); do
      get_current_index
      sleep 2
  done
fi

echo "DONE"

if [ "$API_SPRING_PROFILES_ACTIVE" == "offline" ]; then
  tail -f -n +1 /logs/*.log > >(tee $logf) &
  tail_pid=$!
  wait $API_PID
else
  tail -f -n +1 /logs/*.log > >(tee $logf) &
  tail_pid=$!
  wait $CARDANO_NODE_PID
fi