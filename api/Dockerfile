FROM openjdk:18-jdk-slim-bullseye
RUN apt-get update
RUN apt-get install -y curl

#download cardano-node binary
ARG CARDANO_NODE_VERSION=8.0.0
WORKDIR /dl-bin
RUN curl https://update-cardano-mainnet.iohk.io/cardano-node-releases/cardano-node-${CARDANO_NODE_VERSION}-linux.tar.gz -o cardano-node.tar.gz
RUN tar -xzf cardano-node.tar.gz && rm cardano-node.tar.gz
RUN cp -r /dl-bin/. /usr/local/bin
WORKDIR /
RUN rm -rf /dl-bin

ARG NETWORK
WORKDIR /cf-rosetta-api/

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring /cf-rosetta-api
USER spring:spring

ENV NETWORK ${NETWORK}

COPY --from=cardano-rosetta-java-base /app/api/target/*.jar /cf-rosetta-api/app.jar
COPY api/src/main/resources/network-config/network/${NETWORK} /cf-rosetta-api/config/
COPY api/src/main/resources/network-config/network/exemptions.json /cf-rosetta-api/config/exemptions.json

COPY ./api/run_backend.sh /cf-rosetta-api/run_backend.sh

ENTRYPOINT ["/bin/sh", "/cf-rosetta-api/run_backend.sh"]
