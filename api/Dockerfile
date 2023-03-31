FROM --platform=linux/arm64 arm64v8/openjdk:18-jdk-slim-bullseye

WORKDIR /cf-metadata-api/

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring /cf-metadata-api
USER spring:spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /cf-metadata-api/app.jar

COPY run_backend.sh /cf-metadata-api/run_backend.sh

ENTRYPOINT ["/bin/sh", "/cf-metadata-api/run_backend.sh"]