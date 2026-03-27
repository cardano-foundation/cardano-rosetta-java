FROM ubuntu:24.04 AS build-common
WORKDIR /build

# Install necessary tools but not OpenJDK from apt
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends maven curl ca-certificates \
    && apt clean

# Download and setup JDK 25.0.2
RUN mkdir -p /opt/java \
    && curl -L https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.2%2B10/OpenJDK25U-jdk_x64_linux_hotspot_25.0.2_10.tar.gz -o /opt/jdk.tar.gz \
    && tar -xzf /opt/jdk.tar.gz -C /opt/java \
    && rm /opt/jdk.tar.gz

# Set JAVA_HOME and update PATH
ENV JAVA_HOME=/opt/java/jdk-25.0.2+10
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