#FROM eclipse-temurin:18-jdk as build
#WORKDIR /workspace/app
#COPY . .
#WORKDIR /workspace/app/common
#RUN ./mvnw clean package -DskipTests
#
#
#WORKDIR /workspace/app/api
#RUN ./mvnw clean package -DskipTests
#RUN ls
#
#FROM openjdk:18-jdk-slim-bullseye
#
#WORKDIR /cf-rosetta-api/
#
#RUN addgroup --system spring && adduser --system --ingroup spring spring
#RUN chown spring:spring /cf-rosetta-api
#USER spring:spring
#
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} /cf-rosetta-api/app.jar
#
#COPY run_backend.sh /cf-rosetta-api/run_backend.sh
#
#ENTRYPOINT ["/bin/sh", "/cf-rosetta-api/run_backend.sh"]

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
