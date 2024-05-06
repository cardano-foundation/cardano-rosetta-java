## Getting Started with Docker

### Prerequisites

- Docker
- Docker Compose
- Java 21
- For integration tests: Node 14+

### How to build

- Clone the repository
- For local environment:
    - Copy `.env.docker-compose`  to `.env`
    - Fill the `.env` file with your values (explain below) or use the provided for docker-compose setup
    - Start SpringBoot application with `mvn spring-boot:run` within submodule `api` or `yaci-indexer`
    - Run `docker compose -f docker-compose.yaml up --build` to start rosetta api service including yaci-store and a cardano node
    - Using the provided env file `docker-compose --env-file .env.docker-compose -f docker-compose.yaml up --build`
* Note: the first time you run the command, it will take a little bit of your time to build the cardano-node, and next time it will be cached when run. So please be patient.

### How to run integration tests

- Run `docker compose --env-file .env.IntegrationTest -f docker-integration-test-environment.yaml up --build -d --wait`
- Using CLI
    - Install newman `npm install -g newman` (Node version 14+ needed)
    - Run `newman run ./postmanTests/rosetta-java.postman_collection.json -e ./postmanTests/Rosetta-java-env.postman_environment.json -r cli`
- Using Postman
    - Install [Postman](https://www.postman.com)
    - Import the collection `./postmanTests/rosetta-java.postman_collection.json`
    - Import the environment `./postmanTests/Rosetta-java-env.postman_environment.json`
    - Run the collection

