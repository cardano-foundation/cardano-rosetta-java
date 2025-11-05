#!/bin/bash

database_initialization() {
    echo "$PGPASSWORD" > /tmp/password
    echo "*:*:*:postgres:$PGPASSWORD" > /home/postgres/.pgpass

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
    sudo -u postgres "$PG_BIN/postgres" -p "$DB_PORT" -D "$PG_DATA" -c config_file="$PG_DATA/postgresql.conf" &
    PG_PID=$!

    until "$PG_BIN/pg_isready" -U postgres -p "$DB_PORT" > /dev/null; do sleep 1; done
}

create_database_and_user() {
    export DB_SCHEMA="public"

    flag=true
    while [ $(sudo -u postgres "$PG_BIN/psql" -p "$DB_PORT" -U postgres -Atc "SELECT pg_is_in_recovery()";) == "t" ]; do
        if $flag ; then
            echo "Waiting for database recovery..."
            flag=false
        fi
        sleep 1
    done

    if [[ -z $(sudo -u postgres "$PG_BIN/psql" -p "$DB_PORT" -U postgres -Atc "SELECT 1 FROM pg_catalog.pg_user WHERE usename = '$DB_USER'";) ]]; then
        echo "Creating database..."
        sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -c "CREATE ROLE \"$DB_USER\" with LOGIN CREATEDB PASSWORD '$DB_SECRET';" > /dev/null
    fi

    if [[ -z $(sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -Atc "SELECT 1 FROM pg_catalog.pg_database WHERE datname = '$DB_NAME'";) ]]; then
        echo "Creating user..."
        sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -c "CREATE DATABASE \"$DB_NAME\";" >/dev/null
        sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -c "GRANT ALL PRIVILEGES ON DATABASE \"$DB_NAME\" to \"$DB_USER\";" > /dev/null
    fi

    # Grant schema privileges (required for PostgreSQL 15+)
    echo "Granting schema privileges..."
    sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -d "$DB_NAME" -c "GRANT ALL ON SCHEMA public TO \"$DB_USER\";" > /dev/null
    sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -d "$DB_NAME" -c "GRANT ALL ON ALL TABLES IN SCHEMA public TO \"$DB_USER\";" > /dev/null
    sudo -u postgres "$PG_BIN/psql" -U postgres -p "$DB_PORT" -d "$DB_NAME" -c "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO \"$DB_USER\";" > /dev/null

    echo "User configured"
}

chown -R postgres:postgres "$PG_DATA"
chmod -R 0700 "$PG_DATA"

echo "Initializing Database..."
database_initialization

echo "Configuring the Database..."
configure_postgres

echo "Starting Postgres..."
start_postgres

echo "Creating database and user..."
create_database_and_user

wait "$PG_PID"

