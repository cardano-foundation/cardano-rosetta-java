FROM ubuntu:24.04

WORKDIR /root

# Install dependencies
RUN apt-get update -qq --fix-missing

RUN apt -y --no-install-recommends install \
     automake build-essential pkg-config libffi-dev libgmp-dev libssl-dev libncurses-dev libsystemd-dev zlib1g-dev make \
     g++ tmux git jq wget libncursesw5-dev libtool autoconf liblmdb-dev curl ca-certificates

# Install ghcup
ENV BOOTSTRAP_HASKELL_NONINTERACTIVE=1
RUN bash -c "curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org | sh "
#curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org > ghcup_install.sh

# Add ghcup to PATH
ENV PATH=${PATH}:/root/.local/bin
ENV PATH=${PATH}:/root/.ghcup/bin

RUN bash -c "ghcup upgrade"

# Install cabal
ENV CABAL_VERSION=3.8.1.0
RUN bash -c "ghcup install cabal ${CABAL_VERSION}"
RUN bash -c "ghcup set cabal ${CABAL_VERSION}"

# Install GHC
ENV GHC_VERSION=8.10.7
RUN bash -c "ghcup install ghc ${GHC_VERSION}"
RUN bash -c "ghcup set ghc ${GHC_VERSION}"

WORKDIR /root/src

ENV CARDANO_NODE_VERSION=8.7.3

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
    &&  bash -c "chmod u=rw,go=r /usr/local/{lib/{libblst.a,pkgconfig/libblst.pc},include/{blst.{h,hpp},blst_aux.h}}" \
    && cd ..

RUN apt -y --no-install-recommends install libsecp256k1-dev

#Install node
ENV NODE_VERSION=8.9.2

RUN git clone https://github.com/intersectmbo/cardano-node.git \
    && cd cardano-node \
#    && git fetch --all --recurse-submodules --tags \
#    && git tag | sort -V \
    && git checkout tags/$NODE_VERSION \
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

RUN cardano-cli --version

WORKDIR /root

#ARG CARDANO_NODE_SOCKET="./node-ipc"
#ARG CARDANO_NODE_DB="./node/db"
##ARG CARDANO_CONFIG="./docker/${NETWORK}"
#ARG CARDANO_CONFIG="./docker/config/${NETWORK}"
#
#COPY ${CARDANO_NODE_SOCKET} /node/ipc
#COPY ${CARDANO_NODE_DB} /node/data/db
#COPY ${CARDANO_CONFIG} /node/config
#
#EXPOSE 3001 12788 12798

#ENTRYPOINT cardano-node run --database-path /data/db --host-addr 0.0.0.0 --port 3001 --socket-path /node/ipc/node.socket --topology /node/config/topology.json --config /node/config/config.json
CMD ["/bin/sh", "-c", "bash"]



