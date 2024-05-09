FROM ubuntu:22.04 AS cardano-builder

WORKDIR /root/src

# Install dependencies
RUN apt-get update --fix-missing

RUN apt -y --no-install-recommends install \
     automake build-essential pkg-config libffi-dev libgmp-dev libssl-dev libncurses-dev libsystemd-dev zlib1g-dev make \
     g++ tmux git jq wget libncursesw5-dev libtool autoconf liblmdb-dev curl ca-certificates

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

# Cardano node version
ARG CARDANO_NODE_VERSION=8.9.2

# Install sodium
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
    && make install

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

RUN apt -y --no-install-recommends install libsecp256k1-dev

# Install node
RUN git clone https://github.com/intersectmbo/cardano-node.git \
    && cd cardano-node \
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
RUN bash -c "cabal build cardano-submit-api"

RUN mkdir -p /root/.local/bin \
    && cp -p "$(./scripts/bin-path.sh cardano-node)" /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-cli)" /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-submit-api)" /root/.local/bin/

# Compile java applications
FROM ubuntu:22.04 AS java-builder

WORKDIR /root/app

RUN apt-get update --fix-missing \
    && apt install -y --no-install-recommends openjdk-21-jdk maven

COPY ./pom.xml /root/app/pom.xml

COPY ./api /root/app/api
COPY ./yaci-indexer /root/app/yaci-indexer
COPY ./test-data-generator /root/app/test-data-generator

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# Main
FROM ubuntu:22.04

WORKDIR /

# Install postgres
ARG PG_VERSION=14

RUN apt-get update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y wget sudo gnupg \
    && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
    && echo 'deb http://apt.postgresql.org/pub/repos/apt/ jammy-pgdg main' >> /etc/apt/sources.list

RUN apt-get update --fix-missing \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    postgresql-${PG_VERSION}

RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/$PG_VERSION/main/pg_hba.conf
RUN echo "listen_addresses='*'" >> /etc/postgresql/$PG_VERSION/main/postgresql.conf

# Install jdk 21
RUN apt -y --no-install-recommends install openjdk-21-jdk

# Copy cardano node
COPY --from=cardano-builder /usr/local/lib /usr/local/lib
COPY --from=cardano-builder /root/.local/bin/cardano-* /usr/local/bin/
COPY --from=cardano-builder /root/src/cardano-node/cardano-submit-api/config/tx-submit-mainnet-config.yaml /root/cardano-submit-api/

ENV LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
ENV PATH=/usr/local/lib/:$PATH

COPY ./config /config
RUN mkdir /current

RUN mkdir -p /data/db

# Copy jars
COPY --from=java-builder /root/app/api/target/*.jar /api/app.jar
COPY --from=java-builder /root/app/yaci-indexer/target/*.jar /yaci-indexer/app.jar

# Run
RUN mkdir /logs

EXPOSE 8082

COPY ./docker/entrypoint.sh /sbin/entrypoint.sh
ENTRYPOINT ["/sbin/entrypoint.sh"]

CMD ["/bin/sh", "-c", "bash"]
