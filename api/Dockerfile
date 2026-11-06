FROM ubuntu:24.04 AS build-common
WORKDIR /build

# Harden apt against transient Ubuntu-mirror hiccups (mirror sync in progress,
# flaky DNS, stale CDN edges). Retry a few times, bump the read timeout, and
# disable HTTP caching so a stale index doesn't get re-served.
RUN printf 'Acquire::Retries "5";\nAcquire::http::Timeout "30";\nAcquire::https::Timeout "30";\nAcquire::http::No-Cache "true";\n' \
        > /etc/apt/apt.conf.d/99-retries

# Install necessary tools but not OpenJDK from apt
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends maven curl ca-certificates \
    && apt clean

# Download and setup JDK 25 LTS (Amazon Corretto, GPLv2 + Classpath Exception).
# 25.0.5.0.10 includes the eMudhra CA fixes (CVE-2026-91403, CVE-2026-114820).
# Checksum from https://github.com/corretto/corretto-25/releases
ARG CORRETTO_VERSION=25.0.5.0.10
ARG CORRETTO_SHA256=1d89b4194f20ee513f4dd9fc2aadd038de3c713c37fd444d77566b3da7b0ee65
RUN mkdir -p /opt/java \
    && curl -fsSL https://corretto.aws/downloads/resources/${CORRETTO_VERSION}/amazon-corretto-${CORRETTO_VERSION}-linux-x64.tar.gz -o /opt/jdk.tar.gz \
    && echo "${CORRETTO_SHA256}  /opt/jdk.tar.gz" | sha256sum -c - \
    && tar -xzf /opt/jdk.tar.gz -C /opt/java \
    && rm /opt/jdk.tar.gz

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/amazon-corretto-${CORRETTO_VERSION}-linux-x64
ENV PATH="${JAVA_HOME}/bin:${PATH}"

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

RUN --mount=type=cache,target=/root/.m2 mvn -U clean package -DskipTests
RUN java --version

WORKDIR /app
RUN cp /build/api/target/*.jar /app/api.jar
RUN rm -rf /build

ENTRYPOINT ["java", "--enable-preview", "-jar",  "/app/api.jar"]

CMD ["/bin/sh", "-c", "bash"]