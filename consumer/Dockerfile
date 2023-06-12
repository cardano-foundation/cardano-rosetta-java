FROM openjdk:18-jdk-slim-bullseye AS runtime-consumer

WORKDIR /cf-rosetta-consumer

COPY --from=cardano-rosetta-java-base /app/consumer/target/*.jar /cf-rosetta-consumer/cf-rosetta-consumer.jar
COPY --from=cardano-rosetta-java-base /app/consumer/src/main/resources/genesis /cf-rosetta-consumer/genesis
ENTRYPOINT ["java", "-jar", "cf-rosetta-consumer.jar"]
