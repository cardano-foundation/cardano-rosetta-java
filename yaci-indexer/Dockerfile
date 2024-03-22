FROM maven:3.9.6-sapmachine-21 AS build-common
WORKDIR /app
COPY ./pom.xml /app/pom.xml

COPY ./api/pom.xml /app/api/pom.xml
COPY ./api /app/api

COPY ./yaci-indexer/pom.xml /app/yaci-indexer/pom.xml
COPY ./yaci-indexer /app/yaci-indexer

COPY ./test-data-generator/pom.xml /app/test-data-generator/pom.xml
COPY ./test-data-generator /app/test-data-generator

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:latest
RUN apt-get update
WORKDIR /cf-rosetta-yaci-indexer
COPY --from=build-common /app/yaci-indexer/target/*.jar /cf-rosetta-yaci-indexer/app.jar

COPY ./api/run_backend.sh /cf-rosetta-yaci-indexer/run_backend.sh

ENTRYPOINT ["/bin/sh", "/cf-rosetta-yaci-indexer/run_backend.sh"]