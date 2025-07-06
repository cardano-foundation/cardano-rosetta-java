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

ARG BLST_VERSION="0.3.11"
ARG LIBSODIUM_VERSION="dbb48cc"
ARG SECP256K1_VERSION="ac83be33"
ARG LMDB_VERSION="ce20108"

ARG CABAL_VERSION=${CABAL_VERSION:-3.12.1.0}
ARG GHC_VERSION=${GHC_VERSION:-9.6.7}

# Cardano node version
ARG CARDANO_NODE_VERSION=${CARDANO_NODE_VERSION:-10.4.1}

# Clone config fork reposotory
ARG CONFIG_FORK_REPO="VladislavKudrin/gnu-config-fork"
ARG CONFIG_FORK_BRANCH="master"

# Mithril version
ARG MITHRIL_VERSION=${MITHRIL_VERSION:-2524.0}

RUN bash -c "ghcup install cabal ${CABAL_VERSION}"
RUN bash -c "ghcup set cabal ${CABAL_VERSION}"

RUN bash -c "ghcup install ghc ${GHC_VERSION}"
RUN bash -c "ghcup set ghc ${GHC_VERSION}"

# Install mithril
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

WORKDIR /usr/local/src

# Install sodium
RUN git clone --branch master https://github.com/IntersectMBO/libsodium.git \
 && cd libsodium \
 && git checkout ${LIBSODIUM_VERSION} \
 && ./autogen.sh -b \
 && curl -sSL --fail -o build-aux/config.guess \
    "https://raw.githubusercontent.com/${CONFIG_FORK_REPO}/${CONFIG_FORK_BRANCH}/config.guess" \
 && chmod +x build-aux/config.guess \
 && curl -sSL --fail -o build-aux/config.sub \
    "https://raw.githubusercontent.com/${CONFIG_FORK_REPO}/${CONFIG_FORK_BRANCH}/config.sub" \
 && chmod +x build-aux/config.sub \
 && ./configure \
 && make \
 && make check \
 && make install

# Install secp256k1
RUN git clone --branch master https://github.com/bitcoin-core/secp256k1.git \
 && cd secp256k1 \
 && git checkout ${SECP256K1_VERSION} \
 && ./autogen.sh \
 && ./configure --prefix=/usr --enable-module-schnorrsig --enable-experimental \
 && make \
 && make check \
 && make install

# Install blst
RUN git clone --branch master https://github.com/supranational/blst.git \
    && cd blst \
    && git checkout v${BLST_VERSION} \
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

# Install lmdb
RUN git clone --branch mdb.master https://github.com/LMDB/lmdb.git \
 && cd lmdb \
 && git checkout ${LMDB_VERSION} \
 && cd libraries/liblmdb \
 && make \
 && make install

# Install cardano-node
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

WORKDIR /usr/local/src/cardano-node

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

# Install dependencies
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends \
    maven curl \
    && apt-get clean

# Download and setup JDK 24.0.1
RUN mkdir -p /opt/java \
    && curl -L https://download.java.net/java/GA/jdk24.0.1/24a58e0e276943138bf3e963e6291ac2/9/GPL/openjdk-24.0.1_linux-x64_bin.tar.gz -o /opt/jdk.tar.gz \
    && tar -xzf /opt/jdk.tar.gz -C /opt/java \
    && rm /opt/jdk.tar.gz

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/jdk-24.0.1
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /root/app

COPY ./pom.xml /root/app/pom.xml

COPY ./api /root/app/api
COPY ./yaci-indexer /root/app/yaci-indexer
COPY ./test-data-generator /root/app/test-data-generator
COPY ./.git /root/app/.git

RUN --mount=type=cache,target=/root/.m2 mvn -U clean package -DskipTests

# Build postgres
FROM ubuntu:24.04 AS postgres-builder

RUN apt update && apt install -y \
  build-essential \
  libreadline-dev \
  zlib1g-dev \
  flex \
  bison \
  wget \
  git \
  ca-certificates \
  sudo \
  locales \
  && sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen \
  && locale-gen \
  && update-locale LANG=en_US.UTF-8 \
  && apt clean

ENV PG_VERSION_TAG=REL_14_11

WORKDIR /usr/src

RUN git clone --branch $PG_VERSION_TAG https://github.com/postgres/postgres.git \
  && cd postgres \
  && ./configure --prefix=/usr/local/pgsql \
  && make -j$(nproc) \
  && make install


# Main
FROM ubuntu:24.04

RUN apt update && apt install -y --no-install-recommends jq bc sudo curl \
  libreadline-dev \
  sudo \
  locales \
  && sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen \
  && locale-gen \
  && update-locale LANG=en_US.UTF-8 \
  && apt clean

WORKDIR /

# Copy cardano node
COPY --from=cardano-builder /usr/local/lib /usr/local/lib
COPY --from=cardano-builder /root/.local/bin/cardano-* /usr/local/bin/
COPY --from=cardano-builder /root/.local/bin/mithril-client /usr/local/bin/
COPY --from=cardano-builder /usr/local/src/cardano-node/cardano-submit-api/config/tx-submit-mainnet-config.yaml /cardano-submit-api-config/cardano-submit-api.yaml
ENV NODE_DATA="/node/db"
RUN mkdir -p $NODE_DATA

# Link libsodium library
COPY --from=cardano-builder --chown=root:root /usr/local/lib/libsodium.so /usr/local/lib
RUN ln -snf /usr/local/lib/libsodium.so /usr/local/lib/libsodium.so.23 \
    && ln -snf /usr/local/lib/libsodium.so /usr/local/lib/libsodium.so.23.3.0
RUN ldconfig

# Link secp256k1 library
COPY --from=cardano-builder --chown=root:root /usr/local/src/secp256k1/.libs/libsecp256k1.so /usr/local/lib
RUN ln -snf /usr/local/lib/libsecp256k1.so /usr/local/lib/libsecp256k1.so.0 \
    && ln -snf /usr/local/lib/libsecp256k1.so /usr/local/lib/libsecp256k1.so.1 \
    && ln -snf /usr/local/lib/libsecp256k1.so /usr/local/lib/libsecp256k1.so.1.0.1
RUN ldconfig

# Link lmdb library
COPY --from=cardano-builder /usr/local/lib/liblmdb.so /usr/local/lib/
RUN ln -snf /usr/local/lib/liblmdb.so /usr/local/lib/liblmdb.so.0
RUN ldconfig

ENV LD_LIBRARY_PATH=/usr/local/lib
ENV PATH=/usr/local/lib/:$PATH

COPY ./config /networks
RUN mkdir /config

# Copy jars
COPY --from=java-builder /root/app/api/target/*.jar /api/app.jar
COPY --from=java-builder /root/app/yaci-indexer/target/*.jar /yaci-indexer/app.jar
# Copy JDK
COPY --from=java-builder /opt/java/jdk-24.0.1 /opt/java/jdk-24.0.1
ENV JAVA_HOME=/opt/java/jdk-24.0.1
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy and configure postgres
COPY --from=postgres-builder /usr/local/pgsql /usr/local/pgsql
ENV PG_BIN="/usr/local/pgsql/bin"
ENV PG_DATA="/var/lib/postgresql/data"
ENV PATH="$PG_BIN:$PATH"
RUN mkdir -p $PG_DATA 
RUN useradd -m -U -s /bin/bash postgres

# Run
RUN mkdir /logs

COPY ./docker/entrypoint.sh /sbin/entrypoint.sh

RUN chmod +x /sbin/entrypoint.sh

EXPOSE 8082

ENTRYPOINT ["/sbin/entrypoint.sh"]
