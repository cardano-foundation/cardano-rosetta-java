#!/bin/bash
## to add a cronjob use 'crontab -e' add '0 23 * * * sudo ./[PATH_TO_ROSETTA_JAVA]/cardano-rosetta-java/postmanTests/updateEnvironment.sh'
cd $(dirname $0)
git pull
sudo docker compose -f ../docker-compose.yaml --env-file ../.envPreprod up --build -d
