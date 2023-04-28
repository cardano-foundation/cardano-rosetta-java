FROM openjdk:18-jdk-slim-bullseye

WORKDIR /cf-rosetta-api/

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring /cf-rosetta-api
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /cf-rosetta-api/app.jar

COPY run_backend.sh /cf-rosetta-api/run_backend.sh

ENTRYPOINT ["/bin/sh", "/cf-rosetta-api/run_backend.sh"]
