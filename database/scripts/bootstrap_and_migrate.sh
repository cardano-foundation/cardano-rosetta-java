#!/bin/bash

if [[ "${LIQUIBASE_ENABLE}" == true ]]; then
  echo "Bootstrapping the database ..."
  cd scripts
  ./bootstrap_database.sh

  echo "Applying schema to database ..."
  ./migrate_database.sh
fi
