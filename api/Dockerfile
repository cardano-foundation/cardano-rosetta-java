FROM openjdk:18-jdk-slim-bullseye

WORKDIR /cf-rosetta-api/

RUN addgroup --system spring && adduser --system --ingroup spring spring
RUN chown spring:spring /cf-rosetta-api
USER spring:spring

COPY --from=cardano-rosetta-java-base /app/api/target/*.jar /cf-rosetta-api/app.jar

COPY ./api/run_backend.sh /cf-rosetta-api/run_backend.sh

COPY ./api/src/main/resources/config/* /cf-rosetta-api/config/

ENTRYPOINT ["/bin/sh", "/cf-rosetta-api/run_backend.sh"]
