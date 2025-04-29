FROM ubuntu:24.04 AS build-common
WORKDIR /build

RUN apt update --fix-missing \
    && apt install -y --no-install-recommends openjdk-21-jdk maven curl \
    && apt clean

COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

WORKDIR /app
RUN cp /build/api/target/*.jar /app/api.jar
RUN rm -rf /build

ENTRYPOINT ["java", "--enable-preview", "-jar",  "/app/api.jar"]

CMD ["/bin/sh", "-c", "bash"]
