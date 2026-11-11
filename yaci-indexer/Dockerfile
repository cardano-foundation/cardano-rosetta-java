FROM ubuntu:26.04 AS build-common
WORKDIR /build

# Harden apt against transient Ubuntu-mirror hiccups (mirror sync in progress,
# flaky DNS, stale CDN edges). Retry a few times, bump the read timeout, and
# disable HTTP caching so a stale index doesn't get re-served.
RUN printf 'Acquire::Retries "5";\nAcquire::http::Timeout "30";\nAcquire::https::Timeout "30";\nAcquire::http::No-Cache "true";\n' \
        > /etc/apt/apt.conf.d/99-retries

# Ubuntu 26.04's distro maven package is 3.9.12.
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends maven curl \
    && apt clean

# Download and setup JDK 25 LTS (Eclipse Temurin, GPLv2 + Classpath Exception).
# 25.0.5+8 ships the Mozilla NSS 3.131.1 trust store, which removes the roots that signed
# the compromised eMudhra intermediates: https://adoptium.net/news/2026/10/temurin-october-2026-emudhra/
# Checksum from https://api.adoptium.net/v3/assets/release_name/eclipse/jdk-25.0.5%2B8
ARG TEMURIN_VERSION=25.0.5
ARG TEMURIN_BUILD=8
ARG TEMURIN_SHA256=68982529869257d0ee6b824ec5aa270ff8b118bb438670da0b97ebdab178d7fb
RUN mkdir -p /opt/java \
    && curl -fsSL https://github.com/adoptium/temurin25-binaries/releases/download/jdk-${TEMURIN_VERSION}%2B${TEMURIN_BUILD}/OpenJDK25U-jdk_x64_linux_hotspot_${TEMURIN_VERSION}_${TEMURIN_BUILD}.tar.gz -o /opt/jdk.tar.gz \
    && echo "${TEMURIN_SHA256}  /opt/jdk.tar.gz" | sha256sum -c - \
    && tar -xzf /opt/jdk.tar.gz -C /opt/java \
    && rm /opt/jdk.tar.gz

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/jdk-${TEMURIN_VERSION}+${TEMURIN_BUILD}
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

RUN --mount=type=cache,target=/root/.m2 mvn -U clean package -DskipTests
RUN java --version

WORKDIR /app
RUN cp /build/yaci-indexer/target/*.jar /app/yaci-indexer.jar
RUN rm -rf /build

ENTRYPOINT ["java", "-jar",  "/app/yaci-indexer.jar"]

CMD ["/bin/sh", "-c", "bash"]
