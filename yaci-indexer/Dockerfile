# Optional host-provided Maven repo for seeding local snapshots
# (e.g. yaci-store built locally). Defaults to empty so regular builds
# are unaffected. Override at build time with:
#   docker buildx build --build-context host-m2=$HOME/.m2/repository ...
FROM scratch AS host-m2

FROM ubuntu:24.04 AS build-common
WORKDIR /build

# Harden apt against transient Ubuntu-mirror hiccups (mirror sync in progress,
# flaky DNS, stale CDN edges). Retry a few times, bump the read timeout, and
# disable HTTP caching so a stale index doesn't get re-served.
RUN printf 'Acquire::Retries "5";\nAcquire::http::Timeout "30";\nAcquire::https::Timeout "30";\nAcquire::http::No-Cache "true";\n' \
        > /etc/apt/apt.conf.d/99-retries

RUN apt update --fix-missing \
    && apt install -y --no-install-recommends maven curl \
    && apt clean

# Download and setup JDK 24.0.1
RUN mkdir -p /opt/java \
    && curl -L https://download.java.net/java/GA/jdk24.0.1/24a58e0e276943138bf3e963e6291ac2/9/GPL/openjdk-24.0.1_linux-x64_bin.tar.gz -o /opt/jdk.tar.gz \
    && tar -xzf /opt/jdk.tar.gz -C /opt/java \
    && rm /opt/jdk.tar.gz

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/jdk-24.0.1
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

# Seed the Maven cache with artifacts from the optional host-m2 context
# (e.g. locally-built yaci-store snapshots), then build with -nsu so Maven
# does not try to re-resolve those snapshots from remote repos.
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=bind,from=host-m2,target=/host-m2 \
    if [ -d /host-m2/com/bloxbean/cardano ]; then \
        mkdir -p /root/.m2/repository/com/bloxbean/cardano && \
        cp -r /host-m2/com/bloxbean/cardano/. /root/.m2/repository/com/bloxbean/cardano/; \
    fi && \
    mvn -nsu clean package -DskipTests
RUN java --version

WORKDIR /app
RUN cp /build/yaci-indexer/target/*.jar /app/yaci-indexer.jar
RUN rm -rf /build

ENTRYPOINT ["java", "-jar",  "/app/yaci-indexer.jar"]

CMD ["/bin/sh", "-c", "bash"]
