[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=cardano-foundation_cardano-rosetta-java)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cardano-foundation_cardano-rosetta-java&metric=coverage)](https://sonarcloud.io/summary/new_code?id=cardano-foundation_cardano-rosetta-java)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B45571%2Fgithub.com%2Fcardano-foundation%2Fcardano-rosetta-java.svg?type=shield&issueType=license)](https://app.fossa.com/projects/custom%2B45571%2Fgithub.com%2Fcardano-foundation%2Fcardano-rosetta-java?ref=badge_shield&issueType=license)

## What the project is about?

This repository provides a lightweight java implementation of the [Rosetta API](https://github.com/coinbase/mesh-specifications). It uses [Yaci-Store](https://github.com/bloxbean/yaci-store) as an indexer
to fetch the data from a Cardano node. 

## System requirements
Thanks to design differences in Yaci-Store the system requirements for this Cardano Rosetta implemenations are drastically lower, while it also increases performance.

- 32GB RAM
To be completed!

## Installation

### Docker (build from source)
If your user is not in the `docker` group you might have to execute these commands with `sudo`.
The default config is focused on mainnet. If you want to test this on other Cardano netwoks (like `preview` or `preprod`) please adjust the `docker/.env.dockerfile` or read the Wiki page on [Environment variables](https://github.com/cardano-foundation/cardano-rosetta-java/wiki/5.-Environment-Variables) on other options and their default values.
######
    git clone https://github.com/cardano-foundation/cardano-rosetta-java
######
    cd cardano-rosetta-java
######
    docker build -t rosetta-java -f ./docker/Dockerfile .
#####
    docker run --name rosetta --env-file ./docker/.env.dockerfile -p 8082:8082 -d rosetta-java:latest

**Useful commands:**
- Following Docker container logs:
#####
    docker logs rosetta -f

- Access node logs:
#####
    docker exec rosetta tail -f /logs/node.log

- Interactive access to container:
#####
    docker exec -it rosetta bash

- Verify node sync:
#####
    cardano-cli query tip --mainnet


### Docker (image)

TO BE COMPLETED!

## Documentation

Please refer to our [wiki pages](https://github.com/cardano-foundation/cardano-rosetta-java/wiki) for more information on the project.

---
Thanks for visiting us and enjoy :heart:!
