FROM ubuntu:24.04 AS build-common
WORKDIR /build

# Harden apt against transient Ubuntu-mirror hiccups (mirror sync in progress,
# flaky DNS, stale CDN edges). Retry a few times, bump the read timeout, and
# disable HTTP caching so a stale index doesn't get re-served.
RUN printf 'Acquire::Retries "5";\nAcquire::http::Timeout "30";\nAcquire::https::Timeout "30";\nAcquire::http::No-Cache "true";\n' \
        > /etc/apt/apt.conf.d/99-retries

# Ubuntu 24.04 only packages Maven 3.8.7, and flatten-maven-plugin 1.9.0 requires 3.9+.
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends curl ca-certificates \
    && apt clean

# Maven 3.10.0. Checksum is the sha256 of apache-maven-3.10.0-bin.tar.gz from Maven Central
# (the published .sha512 matches the same archive).
ARG MAVEN_VERSION=3.10.0
ARG MAVEN_SHA256=a46cc51bc74fa23fd267c7a0b9132b146dcf526da60d31aa5174e565632e9e0e
RUN curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tar.gz \
    && echo "${MAVEN_SHA256}  /tmp/maven.tar.gz" | sha256sum -c - \
    && tar -xzf /tmp/maven.tar.gz -C /opt \
    && ln -sfn "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn \
    && rm /tmp/maven.tar.gz

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
