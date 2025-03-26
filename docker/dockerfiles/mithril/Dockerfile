FROM openjdk:24-rc-jdk-bookworm AS cardano-builder

WORKDIR /root/src

RUN /usr/bin/apt update --fix-missing \
    && /usr/bin/apt install -y --no-install-recommends curl ca-certificates \
    && /usr/bin/apt clean

# Mithril setup
ARG MITHRIL_VERSION=2450.0
# Install dependencies
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | bash -s -- -y \
    && export PATH="$HOME/.cargo/bin:$PATH" \
    && /usr/bin/apt update --fix-missing \
    && /usr/bin/apt install -y --no-install-recommends \
       build-essential m4 libssl-dev docker jq git \
    && /usr/bin/apt clean
RUN git clone https://github.com/input-output-hk/mithril.git \
    && export PATH="$HOME/.cargo/bin:$PATH" \
    && cd mithril \
    && git checkout $MITHRIL_VERSION \
    && cd mithril-client-cli \
    && make build \
    && mkdir -p /root/.local/bin \
    && cp mithril-client /root/.local/bin/


FROM openjdk:24-rc-jdk-bookworm AS mithril-runner

COPY --from=cardano-builder /root/.local/bin/mithril-client /usr/local/bin
RUN /usr/bin/apt update --fix-missing \
    && /usr/bin/apt install -y wget
COPY entrypoint.sh /sbin/entrypoint.sh
RUN chmod +x /sbin/entrypoint.sh

CMD ["/sbin/entrypoint.sh"]