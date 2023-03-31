#!/bin/bash

if [[ -z "${DB_NAME}" ]]; then
  DB_NAME="rosetta"
fi
if [[ -z "${DEFAULT_DB_NAME}" ]]; then
  DEFAULT_DB_NAME="postgres"
fi
#if [[ -z "${DB_MIGRATION_SCRIPTS_FOLDER}" ]]; then
DB_MIGRATION_SCRIPTS_FOLDER="./"
#fi
if [[ -z "${DB_PORT}" ]]; then
  DB_PORT="5432"
fi
if [[ -z "${DB_HOST}" ]]; then
  DB_HOST="localhost"
fi
if [[ -z "${ENVIRONMENT}" ]]; then
  ENVIRONMENT="dev"
fi
if [[ -z "${REGION}" ]]; then
  REGION="eu-west-1"
fi
if [[ -z "${DB_MIGRATION_LIQUIBASE_ROOT_CHANGELOG}" ]]; then
  DB_MIGRATION_LIQUIBASE_ROOT_CHANGELOG="../liquibase/rosetta.root-changelog.yaml"
fi

# fetch secrets from secrets manager
if [[ -z "${DB_ADMIN_USER_SECRET}" ]]; then
  DB_MASTER_USER_SECRET_ARN="$(AWS_REGION=${REGION} aws secretsmanager list-secrets | jq .SecretList | jq -c 'map(select(.Name | contains("cf-rosetta-api-'${ENVIRONMENT}'-db-master-user")))' | jq -r .[0].ARN)"
  DB_ADMIN_USER_SECRET="$(AWS_REGION=${REGION} aws secretsmanager get-secret-value --secret-id "${DB_MASTER_USER_SECRET_ARN}" | jq -r .SecretString | jq -r .password)"
fi
if [[ -z "${DB_ADMIN_USER_NAME}" ]]; then
  DB_MASTER_USER_SECRET_ARN="$(AWS_REGION=${REGION} aws secretsmanager list-secrets | jq .SecretList | jq -c 'map(select(.Name | contains("cf-rosetta-api-'${ENVIRONMENT}'-db-master-user")))' | jq -r .[0].ARN)"
  DB_ADMIN_USER_NAME="$(AWS_REGION=${REGION} aws secretsmanager get-secret-value --secret-id "${DB_MASTER_USER_SECRET_ARN}" | jq -r .SecretString | jq -r .username)"
fi

# fetch secrets from secrets manager
if [[ -z "${SERVICE_USER_SECRET}" ]]; then
  DB_SERVICE_USER_SECRET_ARN="$(AWS_REGION=${REGION} aws secretsmanager list-secrets | jq .SecretList | jq -c 'map(select(.Name | contains("cf-rosetta-api-'${ENVIRONMENT}'-db-service-user")))' | jq -r .[0].ARN)"
  SERVICE_USER_SECRET="$(AWS_REGION=${REGION} aws secretsmanager get-secret-value --secret-id "${DB_SERVICE_USER_SECRET_ARN}" | jq -r .SecretString | jq -r .password)"
fi
if [[ -z "${SERVICE_USER_NAME}" ]]; then
  DB_SERVICE_USER_SECRET_ARN="$(AWS_REGION=${REGION} aws secretsmanager list-secrets | jq .SecretList | jq -c 'map(select(.Name | contains("cf-rosetta-api-'${ENVIRONMENT}'-db-service-user")))' | jq -r .[0].ARN)"
  SERVICE_USER_NAME="$(AWS_REGION=${REGION} aws secretsmanager get-secret-value --secret-id "${DB_SERVICE_USER_SECRET_ARN}" | jq -r .SecretString | jq -r .username)"
fi

#echo "Setting up database ..."
PGPASSWORD="${DB_ADMIN_USER_SECRET}" psql --set=cf_dbname="${DB_NAME}" --set=cf_serviceuser_name="${SERVICE_USER_NAME}" --set=cf_serviceuser_secret="${SERVICE_USER_SECRET}" -U "${DB_ADMIN_USER_NAME}" -h "${DB_HOST}" -p ${DB_PORT} -d ${DEFAULT_DB_NAME} -f "${DB_MIGRATION_SCRIPTS_FOLDER}teardown.sql"
PGPASSWORD="${DB_ADMIN_USER_SECRET}" psql --set=cf_dbname="${DB_NAME}" --set=cf_serviceuser_name="${SERVICE_USER_NAME}" --set=cf_serviceuser_secret="${SERVICE_USER_SECRET}" -U "${DB_ADMIN_USER_NAME}" -h "${DB_HOST}" -p ${DB_PORT} -d ${DEFAULT_DB_NAME} -f "${DB_MIGRATION_SCRIPTS_FOLDER}bootstrap.sql"
