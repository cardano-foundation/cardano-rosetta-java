FROM ubuntu:22.04 AS cardano-builder

WORKDIR /root/src

# Mithril setup
ARG MITHRIL_VERSION=2445.0
# Install dependencies
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | bash -s -- -y \
    && apt update --fix-missing \
    && apt install -y --no-install-recommends \
       build-essential m4 libssl-dev docker jq git cargo ca-certificates wget \
    && apt-get clean
RUN git clone https://github.com/input-output-hk/mithril.git \
    && cd mithril \
    && git checkout $MITHRIL_VERSION \
    && cd mithril-client-cli \
    && make build \
    && mkdir -p /root/.local/bin \
    && cp mithril-client /root/.local/bin/


FROM ubuntu:22.04 AS mithril-runner

COPY --from=cardano-builder /root/.local/bin/mithril-client /usr/local/bin
RUN apt update --fix-missing \
    && apt install -y wget
COPY entrypoint.sh /sbin/entrypoint.sh
RUN chmod +x /sbin/entrypoint.sh

CMD ["/sbin/entrypoint.sh"]