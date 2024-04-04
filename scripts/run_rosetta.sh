#!/bin/bash

source /app/.env

# setup DB
postgres createdb ${DB_NAME} --owner=${DB_ADMIN_USER_NAME}