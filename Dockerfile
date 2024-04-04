#ARG UBUNTU_VERSION=22.04
#FROM ghcr.io/blinklabs-io/haskell:9.6.3-3.10.2.0-1 AS cardano-node-build
## Install cardano-node
#ARG NODE_VERSION=8.9.0
#ENV NODE_VERSION=${NODE_VERSION}
#RUN echo "Building tags/${NODE_VERSION}..." \
#    && echo tags/${NODE_VERSION} > /CARDANO_BRANCH \
#    && git clone https://github.com/intersectmbo/cardano-node.git \
#    && cd cardano-node \
#    && git fetch --all --recurse-submodules --tags \
#    && git tag \
#    && git checkout tags/${NODE_VERSION} \
#    && echo "with-compiler: ghc-${GHC_VERSION}" >> cabal.project.local \
#    && echo "tests: False" >> cabal.project.local \
#    && cabal update \
#    && cabal build all \
#    && mkdir -p /root/.local/bin/ \
#    && cp -p "$(./scripts/bin-path.sh cardano-node)" /root/.local/bin/


FROM maven:3.9.6-sapmachine-21 AS java-build
WORKDIR /app

COPY ./pom.xml /app/pom.xml

COPY ./api/pom.xml /app/api/pom.xml
COPY ./api /app/api

COPY ./yaci-indexer/pom.xml /app/yaci-indexer/pom.xml
COPY ./yaci-indexer /app/yaci-indexer

COPY ./test-data-generator/pom.xml /app/test-data-generator/pom.xml
COPY ./test-data-generator /app/test-data-generator

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM ubuntu:22.04
ARG NETWORK=mainnet

RUN apt-get update -y && apt-get install -y wget gnupg2 ca-certificates

RUN apt-get update \
 && apt-get install -y wget gnupg \
 && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
 && echo 'deb http://apt.postgresql.org/pub/repos/apt/ jammy-pgdg main' >> /etc/apt/sources.list

ENV PG_VERSION=14 \
    PG_DATADIR=/var/lib/postgresql/${PG_VERSION}/main
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y acl sudo locales \
      postgresql-${PG_VERSION} postgresql-client-${PG_VERSION} postgresql-contrib-${PG_VERSION} postgresql


COPY --from=java-build /app/api/target/*.jar /app/api.jar
COPY --from=java-build /app/yaci-indexer/target/*.jar /app/yaci-store.jar
COPY ./config/${NETWORK} /config
COPY .env.docker-compose /app/.env

WORKDIR /app
COPY ./scripts/run_rosetta.sh /app/run_rosetta.sh

EXPOSE 8080
ENTRYPOINT ["/bin/sh", "/app/run_rosetta.sh"]