FROM ubuntu:22.04 AS build-common
WORKDIR /app

RUN apt-get update --fix-missing \
    && apt install -y --no-install-recommends openjdk-21-jdk maven curl

COPY ./pom.xml /app/pom.xml
COPY ./api /app/api
COPY ./yaci-indexer /app/yaci-indexer
COPY ./test-data-generator /app/test-data-generator

RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

RUN cp /app/api/target/*.jar /app/api.jar

ENTRYPOINT ["java", "-jar",  "/app/api.jar"]

CMD ["/bin/sh", "-c", "bash"]
