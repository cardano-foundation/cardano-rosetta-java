ARG CARDANO_BRANCH=8.0.0

FROM debian:stable-slim as cardano-builder
# Install build dependencies
RUN apt-get update -y && apt-get install -y autoconf \
    automake \
    build-essential \
    pkg-config  \
    libffi-dev \
    libgmp-dev \
    libssl-dev \
    libtinfo-dev \
    libsystemd-dev \
    libncursesw5 \
    zlib1g-dev\
    libtool \
    make \
    pkg-config \
    g++ \
    tmux \
    git \
    jq \
    wget \
    liblmdb-dev
ENV PATH="/root/.cabal/bin:/root/.ghcup/bin:/root/.local/bin:$PATH"

ARG TARGETARCH
#RUN \
#  if [ "$TARGETARCH" = "arm64" ]; then \
#    apt-get install -y libnuma-dev=2.0.* llvm-14; \
#  fi \
ARG CABAL_VERSION=3.6.2.0
RUN \
  if [ "$TARGETARCH" = "arm64" ]; then \
    TARGETARCH1=aarch64; \
  else \
    TARGETARCH1=x86_64; \
  fi; \
  wget --secure-protocol=TLSv1_2 \
    https://downloads.haskell.org/~cabal/cabal-install-${CABAL_VERSION}/cabal-install-${CABAL_VERSION}-${TARGETARCH1}-linux-deb10.tar.xz &&\
  tar -xf cabal-install-${CABAL_VERSION}-${TARGETARCH1}-linux-deb10.tar.xz &&\
  rm cabal-install-${CABAL_VERSION}-${TARGETARCH1}-linux-deb10.tar.xz &&\
  mv cabal /usr/local/bin/
RUN cabal update

WORKDIR /app/ghc
ARG GHC_VERSION=8.10.7
RUN \
  if [ "$TARGETARCH" = "arm64" ]; then \
    TARGETARCH1=aarch64; \
  else \
    TARGETARCH1=x86_64; \
  fi; \
  wget --secure-protocol=TLSv1_2 \
    https://downloads.haskell.org/~ghc/${GHC_VERSION}/ghc-${GHC_VERSION}-${TARGETARCH1}-deb10-linux.tar.xz &&\
  tar -xf ghc-${GHC_VERSION}-${TARGETARCH1}-deb10-linux.tar.xz && \
  rm ghc-${GHC_VERSION}-${TARGETARCH1}-deb10-linux.tar.xz

WORKDIR /app/ghc/ghc-${GHC_VERSION}
RUN ./configure && make install

WORKDIR /app/src
ARG IOHK_LIBSODIUM_GIT_REV=11bb20dba02b013bf1d83e3c16c51eab2ff07efc
RUN git clone https://github.com/input-output-hk/libsodium.git &&\
  cd libsodium &&\
  git fetch --all --tags &&\
  git checkout ${IOHK_LIBSODIUM_GIT_REV}
WORKDIR /app/src/libsodium
RUN ./autogen.sh && ./configure && make && make check && make install
ENV LD_LIBRARY_PATH="/usr/local/lib:$LD_LIBRARY_PATH"
ENV PKG_CONFIG_PATH="/usr/local/lib/pkgconfig:$PKG_CONFIG_PATH"

WORKDIR /app/src
RUN git clone https://github.com/bitcoin-core/secp256k1 &&\
  cd secp256k1 &&\
  git checkout ac83be33
WORKDIR /app/src/secp256k1
RUN ./autogen.sh && ./configure --enable-module-schnorrsig --enable-experimental &&\
    make && make check &&  make install

WORKDIR /app/src
ARG CARDANO_NODE_VERSION=8.0.0
RUN git clone https://github.com/input-output-hk/cardano-node.git &&\
  cd cardano-node &&\
  git fetch --all --recurse-submodules --tags &&\
  git checkout ${CARDANO_NODE_VERSION}

WORKDIR /app/src/cardano-node
RUN echo "with-compiler: ghc-8.10.7" >> cabal.project.local  \
    && echo "package cardano-crypto-praos" >>  cabal.project.local \
    && echo "  flags: -external-libsodium-vrf" >>  cabal.project.local
RUN cabal update
RUN cabal build all && mkdir -p /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-node)" /root/.local/bin/ \
    && cp -p "$(./scripts/bin-path.sh cardano-cli)" /root/.local/bin/

FROM eclipse-temurin:latest
RUN apt-get update
# Copy the binaries/libraries we've just built
COPY --from=cardano-builder /root/.local/bin/cardano-cli /root/.local/bin/cardano-node /usr/local/bin/
COPY --from=cardano-builder /usr/local/lib/ /usr/local/lib/
ENV LD_LIBRARY_PATH="/usr/local/lib:$LD_LIBRARY_PATH"


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
