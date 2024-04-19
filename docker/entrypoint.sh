#!/bin/bash
echo "entrypoint - run node"
cardano-node run --database-path /data/db --host-addr 0.0.0.0 --port ${CARDANO_NODE_PORT} --socket-path /ipc/node.socket --topology /config/topology.json --config /config/config.json &

echo "entrypoint - run postgres"
/etc/init.d/postgresql start

if [[ -z $(sudo -u postgres psql -U ${PG_USER} -Atc "SELECT 1 FROM pg_catalog.pg_user WHERE usename = '${DB_ADMIN_USER_NAME}'";) ]]; then
  sudo -u postgres psql -U ${PG_USER} -c "CREATE ROLE \"${DB_ADMIN_USER_NAME}\" with LOGIN CREATEDB PASSWORD '${DB_ADMIN_USER_SECRET}';" >/dev/null
fi

if [[ -z $(sudo -u postgres psql -U ${PG_USER} -Atc "SELECT 1 FROM pg_catalog.pg_database WHERE datname = '${DB_NAME}'";) ]]; then
  sudo -u postgres psql -U ${PG_USER} -c "CREATE DATABASE \"${DB_NAME}\";" >/dev/null
  sudo -u postgres psql -U ${PG_USER} -c "GRANT ALL PRIVILEGES ON DATABASE \"${DB_NAME}\" to \"${DB_ADMIN_USER_NAME}\";" >/dev/null
  sudo -u postgres psql -U ${PG_USER} -d ${DB_NAME} -c "CREATE SCHEMA IF NOT EXISTS \"${DB_SCHEMA}\";" >/dev/null
fi

echo "entrypoint - run indexer"
exec java -jar -DdbUrl="${DB_URL}" -DdbUser="${DB_USER}" -DdbSecret="${DB_SECRET}" -DdbDriverName="${DB_DRIVER_CLASS_NAME}" /root/yaci-indexer/app.jar &

echo "entrypoint - run api"
exec java -jar -DdbUrl="${DB_URL}" -DdbUser="${DB_USER}" -DdbSecret="${DB_SECRET}" -DdbDriverName="${DB_DRIVER_CLASS_NAME}" /root/api/app.jar &

echo "entrypoint - run bash"
/bin/sh -c bash