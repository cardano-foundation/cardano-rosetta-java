FROM maven:3.8.5-openjdk-18 AS build-common
WORKDIR /app
COPY ./pom.xml /app/pom.xml

COPY ./common/pom.xml /app/common/pom.xml
COPY ./common /app/common

COPY ./api/pom.xml /app/api/pom.xml
COPY ./api /app/api

COPY ./consumer/pom.xml /app/consumer/pom.xml
COPY ./consumer /app/consumer

RUN mvn clean install -DskipTests
