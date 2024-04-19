ARG NETWORK="preprod"

FROM ubuntu:24.04 AS cardano-builder

ARG CABAL_VERSION=3.8.1.0
ARG GHC_VERSION=8.10.7
ARG CARDANO_NODE_VERSION=8.9.2
ARG NETWORK

WORKDIR /root

# Install dependencies
RUN apt-get update --fix-missing

RUN apt -y --no-install-recommends install \
     automake build-essential pkg-config libffi-dev libgmp-dev libssl-dev libncurses-dev libsystemd-dev zlib1g-dev make \
     g++ tmux git jq wget libncursesw5-dev libtool autoconf liblmdb-dev curl ca-certificates

# Install ghcup
ENV BOOTSTRAP_HASKELL_NONINTERACTIVE=1
RUN bash -c "curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org | sh"

# Add ghcup to PATH
ENV PATH=${PATH}:/root/.local/bin
ENV PATH=${PATH}:/root/.ghcup/bin

#RUN bash -c "ghcup upgrade"

# Install cabal
RUN bash -c "ghcup install cabal ${CABAL_VERSION}"
RUN bash -c "ghcup set cabal ${CABAL_VERSION}"

# Install GHC
RUN bash -c "ghcup install ghc ${GHC_VERSION}"
RUN bash -c "ghcup set ghc ${GHC_VERSION}"

# Cabal
#ENV CABAL_VERSION=${CABAL_VERSION}
#ENV PATH="/root/.cabal/bin:/root/.ghcup/bin:/root/.local/bin:$PATH"
#RUN wget https://downloads.haskell.org/~cabal/cabal-install-${CABAL_VERSION}/cabal-install-${CABAL_VERSION}-$(uname -m)-linux-ubuntu20_04.tar.xz \
#    && tar -xf cabal-install-${CABAL_VERSION}-$(uname -m)-linux-ubuntu20_04.tar.xz \
#    && rm cabal-install-${CABAL_VERSION}-$(uname -m)-linux-ubuntu20_04.tar.xz \
#    && mkdir -p ~/.local/bin \
#    && mv cabal ~/.local/bin/ \
#    && cabal update && cabal --version
#
## GHC
#ENV GHC_VERSION=${GHC_VERSION}
#RUN wget https://downloads.haskell.org/~ghc/${GHC_VERSION}/ghc-${GHC_VERSION}-$(uname -m)-deb10-linux.tar.xz \
#    && tar -xf ghc-${GHC_VERSION}-$(uname -m)-deb10-linux.tar.xz \
#    && rm ghc-${GHC_VERSION}-$(uname -m)-deb10-linux.tar.xz \
#    && cd ghc-${GHC_VERSION} \
#    && ./configure \
#    && make install


WORKDIR /root/src

#Install sodium
RUN export IOHKNIX_VERSION=$(curl https://raw.githubusercontent.com/IntersectMBO/cardano-node/$CARDANO_NODE_VERSION/flake.lock | jq -r '.nodes.iohkNix.locked.rev') \
    && echo "iohk-nix version: $IOHKNIX_VERSION" \
    && export SODIUM_VERSION=$(curl https://raw.githubusercontent.com/input-output-hk/iohk-nix/$IOHKNIX_VERSION/flake.lock | jq -r '.nodes.sodium.original.rev') \
    && echo "Using sodium version: $SODIUM_VERSION" \
    && git clone https://github.com/intersectmbo/libsodium \
    && cd libsodium \
    && git checkout $SODIUM_VERSION \
    && ./autogen.sh \
    && ./configure \
    && make \
    && make check \
    && make install \
    && cd ..

ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV PKG_CONFIG_PATH=/usr/local/lib/pkgconfig:$PKG_CONFIG_PATH

# Install libsodium
RUN apt -y --no-install-recommends install libsodium-dev

# Install secp256k1
RUN export IOHKNIX_VERSION=$(curl https://raw.githubusercontent.com/IntersectMBO/cardano-node/$CARDANO_NODE_VERSION/flake.lock | jq -r '.nodes.iohkNix.locked.rev') \
    && echo "iohk-nix version: $IOHKNIX_VERSION" \
    && export SECP256K1_VERSION=$(curl https://raw.githubusercontent.com/input-output-hk/iohk-nix/${IOHKNIX_VERSION}/flake.lock | jq -r '.nodes.secp256k1.original.ref') \
    && echo "Using secp256k1 version:$SECP256K1_VERSION" \
    && git clone --depth 1 --branch $SECP256K1_VERSION https://github.com/bitcoin-core/secp256k1 \
    && cd secp256k1 \
    && ./autogen.sh \
    && ./configure --enable-module-schnorrsig --enable-experimental \
    && make \
    && make check \
    && make install \
    && cd ..

# Install blst
RUN export BLST_VERSION=$(curl https://raw.githubusercontent.com/input-output-hk/iohk-nix/master/flake.lock | jq -r '.nodes.blst.original.ref') \
    && git clone --depth 1 --branch ${BLST_VERSION} https://github.com/supranational/blst \
    && cd blst \
    && ./build.sh \
    && echo "prefix=/usr/local" >> libblst.pc \
    && echo "exec_prefix=\${prefix}" >> libblst.pc \
    && echo "libdir=\${exec_prefix}/lib" >> libblst.pc \
    && echo "includedir=\${prefix}/include" >> libblst.pc \
    && echo "" >> libblst.pc \
    && echo "Name: libblst" >> libblst.pc \
    && echo "Description: Multilingual BLS12-381 signature library" >> libblst.pc \
    && echo "URL: https://github.com/supranational/blst" >> libblst.pc \
    && echo "Version: ${BLST_VERSION#v}" >> libblst.pc \
    && echo "Cflags: -I\${includedir}" >> libblst.pc \
    && echo "Libs: -L\${libdir} -lblst" >> libblst.pc \
    && cp libblst.pc /usr/local/lib/pkgconfig/ \
    && cp bindings/blst_aux.h bindings/blst.h bindings/blst.hpp  /usr/local/include/ \
    && cp libblst.a /usr/local/lib \
    && bash -c "chmod u=rw,go=r /usr/local/{lib/{libblst.a,pkgconfig/libblst.pc},include/{blst.{h,hpp},blst_aux.h}}" \
    && cd ..

RUN apt -y --no-install-recommends install libsecp256k1-dev

#Install node
RUN git clone https://github.com/intersectmbo/cardano-node.git \
    && cd cardano-node \
#    && git fetch --all --recurse-submodules --tags \
#    && git tag | sort -V \
    && git checkout tags/$CARDANO_NODE_VERSION \
    && echo "with-compiler: ghc-$GHC_VERSION" >> cabal.project.local \
    && echo "" >> cabal.project.local \
    && echo "package cardano-crypto-praos" >> cabal.project.local \
    && echo "  flags: -external-libsodium-vrf" >> cabal.project.local \
    && echo "" >> cabal.project.local \
    && echo "package trace-dispatcher" >> cabal.project.local \
    && echo "  ghc-options: -Wwarn" >> cabal.project.local \
    && echo "" >> cabal.project.local \
    && echo "package HsOpenSSL" >> cabal.project.local \
    && echo "  flags: -homebrew-openssl" >> cabal.project.local \
    && echo "" >> cabal.project.local \
    && mkdir -p /usr/local/opt/openssl \
    && ln -s /opt/homebrew/opt/openssl@3/lib /usr/local/opt/openssl/lib \
    && ln -s /opt/homebrew/opt/openssl@3/include /usr/local/opt/openssl/include

WORKDIR /root/src/cardano-node

RUN bash -c "cabal update"
RUN bash -c "cabal build all"
RUN bash -c "cabal build cardano-cli"

RUN mkdir -p ~/.local/bin \
    && cp -p "$(./scripts/bin-path.sh cardano-node)" ~/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-cli)" ~/.local/bin/ \
    && cd ..

FROM maven:3.9.6-sapmachine-21 AS build-common

ARG NETWORK
ARG DB_HOST="localhjost"
ARG DB_PORT="5432"
ARG DB_NAME="rosetta-java-preprod"
ARG DB_ADMIN_USER_NAME="rosetta_db_admin"
ARG DB_ADMIN_USER_SECRET="weakpwd#123_d"
ARG DB_DRIVER_CLASS_NAME="org.postgresql.Driver"
ARG API_PORT=8081
ARG CARDANO_NODE_VERSION="8.9.0"
ARG API_SPRING_PROFILES_ACTIVE_API="dev"
ARG DB_CONNECTION_PARAMS_PROVIDER_TYPE="ENVIRONMENT"
ARG PROTOCOL_MAGIC=1
ARG TRANSACTION_TTL=3000
ARG TOPOLOGY_FILEPATH=/config/${NETWORK}/topology.json
ARG GENESIS_SHELLEY_PATH=/config/${NETWORK}/shelley-genesis.json
ARG NODE_SUBMIT_API_PORT=8090
ARG CARDANO_NODE_SUBMIT_HOST="cardano-submit-api"
ARG DEVKIT_ENABLED=false
ARG DEVKIT_URL=yaci-cli
ARG HOST_N2C_SOCAT_PORT=3333
ARG CARDANO_NODE_HOST="cardano-node"
ARG CARDANO_NODE_PORT="3001"
ARG GENESIS_BYRON_PATH=/config/${NETWORK}/byron-genesis.json
ARG GENESIS_ALONZO_PATH=/config/${NETWORK}/alonzo-genesis.json
ARG GENESIS_CONWAY_PATH=/config/${NETWORK}/conway-genesis.json

ENV NETWORK="${NETWORK}"
ENV DB_HOST="${DB_HOST}"
ENV DB_PORT="${DB_PORT}"
ENV DB_NAME="${DB_NAME}"
ENV DB_USER="${DB_ADMIN_USER_NAME}"
ENV DB_SECRET="${DB_ADMIN_USER_SECRET}"
ENV DB_DRIVER_CLASS_NAME="${DB_DRIVER_CLASS_NAME}"
ENV API_PORT=${API_PORT}
ENV CARDANO_NODE_VERSION=${CARDANO_NODE_VERSION}
ENV API_SPRING_PROFILES_ACTIVE=${API_SPRING_PROFILES_ACTIVE_API}
ENV DB_CONNECTION_PARAMS_PROVIDER_TYPE=${DB_CONNECTION_PARAMS_PROVIDER_TYPE}
ENV PROTOCOL_MAGIC=${PROTOCOL_MAGIC}
ENV NETWORK_MAGIC=${PROTOCOL_MAGIC}
ENV TRANSACTION_TTL=${TRANSACTION_TTL}
ENV TOPOLOGY_FILEPATH=${TOPOLOGY_FILEPATH}
ENV GENESIS_SHELLEY_PATH=${GENESIS_SHELLEY_PATH}
ENV NODE_SUBMIT_API_PORT=${NODE_SUBMIT_API_PORT}
ENV CARDANO_NODE_SUBMIT_HOST=${CARDANO_NODE_SUBMIT_HOST}
ENV CARDANO_NODE_SOCKET=/ipc/node.socket
ENV DEVKIT_ENABLED=${DEVKIT_ENABLED}
ENV DEVKIT_URL=${DEVKIT_URL}
ENV DEVKIT_PORT=${HOST_N2C_SOCAT_PORT}
ENV CARDANO_NODE_HOST: "${CARDANO_NODE_HOST}"
ENV CARDANO_NODE_PORT: "${INDEXER_NODE_PORT}"
ENV GENESIS_BYRON_PATH: "${GENESIS_BYRON_PATH}"
ENV GENESIS_ALONZO_PATH: "${GENESIS_ALONZO_PATH}"
ENV GENESIS_CONWAY_PATH: "${GENESIS_CONWAY_PATH}"

WORKDIR /root/app

COPY ./pom.xml /root/app/pom.xml

COPY ./api/pom.xml /root/app/api/pom.xml
COPY ./api /root/app/api

COPY ./yaci-indexer/pom.xml /root/app/yaci-indexer/pom.xml
COPY ./yaci-indexer /root/app/yaci-indexer

COPY ./test-data-generator/pom.xml /root/app/test-data-generator/pom.xml
COPY ./test-data-generator /root/app/test-data-generator

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

ARG NETWORK

ARG PG_VERSION=11

ARG DB_ADMIN_USER_NAME="rosetta_db_admin"
ARG DB_ADMIN_USER_SECRET="weakpwd#123_d"

ARG DB_NAME="rosetta-java-preprod"
ARG DB_PORT="5432"
ARG DB_SCHEMA=${NETWORK}

ARG CARDANO_CONFIG="./config/${NETWORK}"

COPY --from=cardano-builder /usr/local/lib /usr/local/lib
COPY --from=cardano-builder /root/.local/bin/cardano-* /usr/local/bin/

COPY ${CARDANO_CONFIG} /config

RUN mkdir -p /data/db
RUN mkdir /ipc

COPY --from=build-common /root/app/api/target/*.jar /root/api/app.jar
COPY --from=build-common /root/app/yaci-indexer/target/*.jar /root/yaci-indexer/app.jar

WORKDIR /root

RUN apt-get update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y wget gnupg \
    && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
    && echo 'deb http://apt.postgresql.org/pub/repos/apt/ jammy-pgdg main' >> /etc/apt/sources.list

RUN apt-get update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends sudo \
    postgresql-${PG_VERSION}

ENV DB_ADMIN_USER_NAME=$DB_ADMIN_USER_NAME
ENV DB_ADMIN_USER_SECRET=$DB_ADMIN_USER_SECRET

ENV DB_NAME=$DB_NAME
ENV DB_PORT=$DB_PORT
ENV DB_SCHEMA=$NETWORK

ENV PG_USER="postgres"

EXPOSE 5432 3001 12788 12798

COPY docker/entrypoint.sh /sbin/entrypoint.sh
ENTRYPOINT ["/sbin/entrypoint.sh"]

CMD ["/bin/sh", "-c", "bash"]
