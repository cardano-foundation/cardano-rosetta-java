#!/bin/bash

if [[ -z "${LIQUIBASE_ENABLE}" ]]; then
  echo "Bootstrapping the database ..."
  cd scripts
  ./bootstrap_database.sh

  echo "Applying schema to database ..."
  ./migrate_database.sh
fi
