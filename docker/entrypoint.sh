#!/bin/bash
echo "entrypoint - run node"
cp -r /config/${NETWORK}/* /config/
cardano-node run --database-path /data/db --host-addr 0.0.0.0 --port ${CARDANO_NODE_PORT} --socket-path /ipc/node.socket --topology /config/topology.json --config /config/config.json > /logs/node.log &

echo "entrypoint - run postgres"
/etc/init.d/postgresql start

if [[ -z $(sudo -u postgres psql -U postgres -Atc "SELECT 1 FROM pg_catalog.pg_user WHERE usename = '${DB_USER}'";) ]]; then
  echo "entrypoint - create db"
  sudo -u postgres psql -U postgres -c "CREATE ROLE \"${DB_USER}\" with LOGIN CREATEDB PASSWORD '${DB_SECRET}';" >/dev/null
fi

if [[ -z $(sudo -u postgres psql -U postgres -Atc "SELECT 1 FROM pg_catalog.pg_database WHERE datname = '${DB_NAME}'";) ]]; then
  echo "entrypoint - create user"
  sudo -u postgres psql -U postgres -c "CREATE DATABASE \"${DB_NAME}\";" >/dev/null
  sudo -u postgres psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"${DB_NAME}\" to \"${DB_USER}\";" >/dev/null
fi

echo "entrypoint - run indexer"
exec java -jar /yaci-indexer/app.jar > /logs/indexer.log &

echo "entrypoint - run api"
exec java -jar /api/app.jar > /logs/api.log &

$@
