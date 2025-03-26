FROM openjdk:24-rc-jdk-bookworm AS build-common
WORKDIR /build

RUN /usr/bin/apt update --fix-missing \
    && /usr/bin/apt install -y --no-install-recommends maven curl \
    && /usr/bin/apt clean

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

WORKDIR /app
RUN cp /build/yaci-indexer/target/*.jar /app/yaci-indexer.jar
RUN rm -rf /build

ENTRYPOINT ["java", "-jar",  "/app/yaci-indexer.jar"]

CMD ["/bin/sh", "-c", "bash"]
