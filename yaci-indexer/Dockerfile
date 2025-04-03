# Stage 1: Build with GraalVM Native Image
FROM ghcr.io/graalvm/graalvm-ce:21 AS build-common
WORKDIR /build

# Install required dependencies
RUN apt update --fix-missing \
    && apt install -y --no-install-recommends maven curl \
    && apt clean

# Install GraalVM Native Image tool
RUN gu install native-image

# Copy project files
COPY ./pom.xml /build/pom.xml
COPY ./api /build/api
COPY ./yaci-indexer /build/yaci-indexer
COPY ./test-data-generator /build/test-data-generator
COPY ./.git .git

# Build yaci-indexer as a GraalVM native binary
RUN --mount=type=cache,target=/root/.m2 mvn -Pnative -DskipTests clean package

# Stage 2: Create minimal runtime image
FROM ubuntu:22.04 AS runtime
WORKDIR /app

# Copy the native binary from the build stage
COPY --from=build-common /build/yaci-indexer/target/*-native /app/yaci-indexer

# Expose necessary ports (if needed)
EXPOSE 8080

# Run the native binary (no JVM required)
ENTRYPOINT ["/app/yaci-indexer"]

CMD ["/bin/sh", "-c", "bash"]