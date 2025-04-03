# Stage 1: Build with GraalVM Native Image
    FROM vegardit/graalvm-maven:latest-java21 AS build-common
WORKDIR /build

# # Install Maven and dependencies
# RUN apt update --fix-missing \
#     && apt install -y --no-install-recommends maven curl \
#     && apt clean

# Install GraalVM Native Image tool
#RUN gu install native-image

# Copy project files
COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

#WORKDIR /build/api

# Build the project as a GraalVM native binary
#RUN --mount=type=cache,target=/root/.m2 mvn -Pnative -DskipTests clean package
#RUN mvn -Pnative -DskipTests clean package
RUN mvn -Pnative spring-boot:build-image -Dspring-boot.build-image.imagePlatform=linux/amd64 -pl api -am

# Stage 2: Create minimal runtime image
FROM ubuntu:22.04 AS runtime
WORKDIR /app

# Copy the compiled native binary from the build stage
COPY --from=build-common /build/api/target/*-native /app/api

# Expose API port
EXPOSE 8080