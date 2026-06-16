FROM ubuntu:24.04

# Harden apt against transient Ubuntu-mirror hiccups (mirror sync in progress,
# flaky DNS, stale CDN edges). Retry a few times, bump the read timeout, and
# disable HTTP caching so a stale index doesn't get re-served.
RUN printf 'Acquire::Retries "5";\nAcquire::http::Timeout "30";\nAcquire::https::Timeout "30";\nAcquire::http::No-Cache "true";\n' \
        > /etc/apt/apt.conf.d/99-retries

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
  libicu-dev \
  pkg-config \
  curl \
  && sed -i '/en_US.UTF-8/s/^# //g' /etc/locale.gen \
  && locale-gen \
  && update-locale LANG=en_US.UTF-8 \
  && apt clean

ENV PG_VERSION_TAG=REL_18_0
ENV PG_DATA="/var/lib/postgresql/data"
ENV PG_BIN="/usr/local/pgsql/bin"
ENV PATH="$PG_BIN:$PATH"

WORKDIR /usr/src

RUN git clone --branch $PG_VERSION_TAG https://github.com/postgres/postgres.git \
  && cd postgres \
  && ./configure --prefix=/usr/local/pgsql \
  && make -j$(nproc) \
  && make install

RUN useradd -m -U -s /bin/bash postgres

RUN mkdir -p $PG_DATA 

VOLUME ["$PG_DATA"]

COPY ./docker/dockerfiles/postgres/entrypoint.sh /sbin/entrypoint.sh
RUN chmod +x /sbin/entrypoint.sh

WORKDIR /

ENTRYPOINT ["/sbin/entrypoint.sh"]

