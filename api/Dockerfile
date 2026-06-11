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

RUN --mount=type=cache,target=/root/.m2 mvn -U clean package -DskipTests
RUN java --version

WORKDIR /app
RUN cp /build/api/target/*.jar /app/api.jar
RUN rm -rf /build

ENTRYPOINT ["java", "--enable-preview", "-jar",  "/app/api.jar"]

CMD ["/bin/sh", "-c", "bash"]