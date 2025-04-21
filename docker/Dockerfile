FROM ubuntu:24.04 AS cardano-builder

SHELL ["/bin/bash", "-c"]

WORKDIR /root/src

# Install dependencies
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends \
    automake build-essential pkg-config libffi-dev libgmp-dev libssl-dev libncurses-dev libsystemd-dev zlib1g-dev make \
    g++ tmux git jq wget libncursesw5-dev libtool autoconf liblmdb-dev curl ca-certificates pkg-config \
    && apt-get clean

WORKDIR /root/src

# Install ghcup
ENV BOOTSTRAP_HASKELL_NONINTERACTIVE=1
RUN bash -c "curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org | sh"

ENV PATH=/root/.local/bin:/root/.ghcup/bin:/root/.cabal/bin:${PATH}

# Install cabal
ARG CABAL_VERSION=3.8.1.0

RUN bash -c "ghcup install cabal ${CABAL_VERSION}"
RUN bash -c "ghcup set cabal ${CABAL_VERSION}"

# Install GHC
ARG GHC_VERSION=8.10.7

RUN bash -c "ghcup install ghc ${GHC_VERSION}"
RUN bash -c "ghcup set ghc ${GHC_VERSION}"


# Mithril setup
ARG MITHRIL_VERSION=2513.0
# Install dependencies

RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | bash -s -- -y \
    && export PATH="$HOME/.cargo/bin:$PATH" \
    && apt update --fix-missing \
    && apt install -y --no-install-recommends \
    build-essential m4 libssl-dev docker.io jq git \
    && rustup update stable \
    && apt-get clean

RUN git clone https://github.com/input-output-hk/mithril.git \
    && export PATH="$HOME/.cargo/bin:$PATH" \
    && cd mithril \
    && git checkout $MITHRIL_VERSION \
    && cd mithril-client-cli \
    && make build \
    && mkdir -p /root/.local/bin \
    && cp mithril-client /root/.local/bin/

# Cardano node version
ARG CARDANO_NODE_VERSION=10.2.1

# Install sodium
RUN export IOHKNIX_VERSION=$(curl https://raw.githubusercontent.com/IntersectMBO/cardano-node/${CARDANO_NODE_VERSION}/flake.lock | jq -r '.nodes.iohkNix.locked.rev') \
    && echo "iohk-nix version: $IOHKNIX_VERSION" \
    && export SODIUM_VERSION=$(curl https://raw.githubusercontent.com/input-output-hk/iohk-nix/${IOHKNIX_VERSION}/flake.lock | jq -r '.nodes.sodium.original.rev') \
    && echo "Using sodium version: $SODIUM_VERSION" \
    && git clone https://github.com/intersectmbo/libsodium \
    && cd libsodium \
    && git checkout ${SODIUM_VERSION} \
    && ./autogen.sh \
    && ./configure \
    && make \
    && make check \
    && make install

# Install libsodium
RUN apt install -y --no-install-recommends libsodium-dev \
    && apt-get clean

# Install secp256k1
RUN export IOHKNIX_VERSION=$(curl https://raw.githubusercontent.com/IntersectMBO/cardano-node/${CARDANO_NODE_VERSION}/flake.lock | jq -r '.nodes.iohkNix.locked.rev') \
    && echo "iohk-nix version: ${IOHKNIX_VERSION}" \
    && export SECP256K1_VERSION=$(curl https://raw.githubusercontent.com/input-output-hk/iohk-nix/${IOHKNIX_VERSION}/flake.lock | jq -r '.nodes.secp256k1.original.ref') \
    && echo "Using secp256k1 version:${SECP256K1_VERSION}" \
    && git clone --depth 1 --branch ${SECP256K1_VERSION} https://github.com/bitcoin-core/secp256k1 \
    && cd secp256k1 \
    && ./autogen.sh \
    && ./configure --enable-module-schnorrsig --enable-experimental \
    && make \
    && make check \
    && make install

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
    && bash -c "chmod u=rw,go=r /usr/local/{lib/{libblst.a,pkgconfig/libblst.pc},include/{blst.{h,hpp},blst_aux.h}}"

RUN apt install -y --no-install-recommends libsecp256k1-dev \
    && apt-get clean

# Install node
RUN git clone https://github.com/intersectmbo/cardano-node.git \
    && cd cardano-node \
    && git checkout tags/${CARDANO_NODE_VERSION} \
    && echo "with-compiler: ghc-${GHC_VERSION}" >> cabal.project.local \
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
RUN bash -c "cabal build cardano-submit-api"

RUN mkdir -p /root/.local/bin \
    && cp -p "$(./scripts/bin-path.sh cardano-node)" /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-cli)" /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-submit-api)" /root/.local/bin/


# Compile java applications
FROM ubuntu:24.04 AS java-builder

WORKDIR /root/app

RUN apt update --fix-missing \
    && apt install -y --no-install-recommends openjdk-21-jdk maven \
    && apt-get clean

COPY ./pom.xml /root/app/pom.xml

COPY ./api /root/app/api
COPY ./yaci-indexer /root/app/yaci-indexer
COPY ./test-data-generator /root/app/test-data-generator
COPY ./.git /root/app/.git

RUN --mount=type=cache,target=/root/.m2 mvn -U clean package -DskipTests

# Main
FROM ubuntu:24.04

WORKDIR /

# Install postgres
ARG PG_VERSION=14
ARG PG_VERSION_TEMP=$PG_VERSION
ENV PG_VERSION=$PG_VERSION_TEMP

RUN apt update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt install -y wget sudo gnupg \
    && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
    && echo 'deb http://apt.postgresql.org/pub/repos/apt/ jammy-pgdg main' >> /etc/apt/sources.list \
    && apt-get clean

RUN apt update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt install -y --no-install-recommends  \
    postgresql-${PG_VERSION} \
    && apt-get clean

# Allow remote connections
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/${PG_VERSION}/main/pg_hba.conf
# Listen on all interfaces
RUN echo "listen_addresses='*'" >> /etc/postgresql/${PG_VERSION}/main/postgresql.conf
# Change data directory path
RUN sed -i "s|var/lib/postgresql/${PG_VERSION}/main|node/postgres|" /etc/postgresql/${PG_VERSION}/main/postgresql.conf
RUN mkdir -p /node/postgres
RUN rm -rf /var/lib/postgresql/${PG_VERSION}/main

# Install jdk 21
RUN apt install -y --no-install-recommends jq bc sudo curl \
    && apt clean

# Copy cardano node
COPY --from=cardano-builder /usr/local/lib /usr/local/lib
COPY --from=cardano-builder /root/.local/bin/cardano-* /usr/local/bin/
COPY --from=cardano-builder /root/.local/bin/mithril-client /usr/local/bin/
COPY --from=cardano-builder /root/src/cardano-node/cardano-submit-api/config/tx-submit-mainnet-config.yaml /cardano-submit-api-config/cardano-submit-api.yaml

ENV LD_LIBRARY_PATH=/usr/local/lib
ENV PATH=/usr/local/lib/:$PATH

COPY ./config /networks
RUN mkdir /config

RUN mkdir -p /node/db
RUN mkdir -p /node/postgres

# Copy jars
COPY --from=java-builder /root/app/api/target/*.jar /api/app.jar
COPY --from=java-builder /root/app/yaci-indexer/target/*.jar /yaci-indexer/app.jar

# Run
RUN mkdir /logs

COPY ./docker/entrypoint.sh /sbin/entrypoint.sh

RUN chmod +x /sbin/entrypoint.sh

EXPOSE 8082

ENTRYPOINT ["/sbin/entrypoint.sh"]
