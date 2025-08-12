FROM postgres:14-alpine

# Remove PostgreSQL server components we don't need for client-only operations
# Keep only client tools: pg_dump, pg_isready, psql, and compression tools

RUN apk add --no-cache \
    pigz \
    bash \
    findutils \
    coreutils \
    curl \
    jq

# Copy snapshot scripts
COPY ./docker/dockerfiles/postgres-client/create-snapshot.sh /sbin/create-snapshot.sh
COPY ./docker/dockerfiles/postgres-client/restore-entrypoint.sh /sbin/restore-entrypoint.sh
RUN chmod +x /sbin/create-snapshot.sh /sbin/restore-entrypoint.sh

# Set PostgreSQL client environment
ENV PGCLIENTENCODING=UTF8
ENV LC_ALL=C.UTF-8

WORKDIR /

ENTRYPOINT ["/sbin/create-snapshot.sh"]