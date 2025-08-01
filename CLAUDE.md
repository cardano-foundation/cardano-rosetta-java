# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This project is a Java implementation of the Rosetta specification for the Cardano blockchain, consisting of multiple Maven modules for API services, blockchain indexing, and testing utilities.

## Build and Development Commands

### Maven Commands
```bash
# Build entire project (from root)
mvn clean install

# Build specific module
mvn clean install -pl api
mvn clean install -pl yaci-indexer
mvn clean install -pl test-data-generator

# Run tests
mvn test                    # All tests
mvn test -pl api           # API module tests only
mvn test -Dtest=ClassName   # Single test class

# Code generation (happens automatically during build)
mvn generate-sources       # Generates OpenAPI code from api.yaml

# Run application (API module)
cd api && mvn spring-boot:run

# Package for deployment
mvn clean package
```

### Docker Commands
```bash
# Build from source
docker build -t rosetta-java -f ./docker/Dockerfile .

# Run with docker-compose (full stack)
docker-compose up -d

# Run specific services
docker-compose up -d api indexer postgres

# View logs
docker logs rosetta -f
docker exec rosetta tail -f /logs/node.log
docker exec rosetta tail -f /logs/indexer.log
```

## Architecture Overview

### Multi-Module Structure
- **`api/`** - Main Rosetta API implementation (Spring Boot)
- **`yaci-indexer/`** - Blockchain data indexer using Yaci Store libraries
- **`test-data-generator/`** - Testing utility for transaction test data

### Key Technologies
- **Java 24** with preview features
- **Spring Boot 3.5.0** with Spring Security and Web
- **Maven** multi-module build
- **OpenAPI 3.0** code generation from `/api/src/main/resources/rosetta-specifications-1.4.15/api.yaml`
- **MapStruct** for object mapping
- **Lombok** for boilerplate reduction
- **JUnit 5** with `@Nested` test organization
- **AssertJ** for test assertions
- **PostgreSQL** (production) / **H2** (development/testing)

### Code Generation Pattern
- All API endpoints and DTOs are generated from `api.yaml`
- Generated code located in `/target/generated-sources/openapi/`
- **NEVER** manually modify controller classes - edit `api.yaml` instead
- Controller implementations in `api/{domain}/controller/` implement generated interfaces
- Always use @Nullable annotation in case of optional fields for function methods parameter inputs and outputs, records, DTOs, and entities
- Avoid if { return } else {} , if we already have a return statement, we can just return the value, no need for else block

### Database Architecture
- **Hibernate JPA** for standard ORM operations
- **Custom entities** with JSON storage for UTXO data using Hypersistence Utils
- **Yaci-Store** handles blockchain data synchronization in separate indexer module
- Database migration handled through Yaci Store's built-in Flyway integration

### API Layer Organization
Each Rosetta endpoint has its own package under `api/src/main/java/org/cardanofoundation/rosetta/api/`:
- `account/` - Account balances and UTXO operations
- `block/` - Block and transaction retrieval  
- `construction/` - Transaction building and signing
- `mempool/` - Mempool operations
- `network/` - Network status and configuration
- `search/` - Transaction search functionality

Each package contains:
- `controller/` - REST endpoints implementing generated OpenAPI interfaces
- `service/` - Business logic layer
- `mapper/` - MapStruct mappers for entity/DTO conversion
- `model/` - Domain objects and entities

### Testing Patterns
- Use `@Nested` classes to group related tests
- Extend `BaseSpringMvcSetup` for integration tests
- Extend `BaseMapperSetup` for mapper tests
- Test data stored in `/src/test/resources/testdata/`
- Use AssertJ for fluent assertions: `assertThat(result).isNotNull().satisfies(...)`

### Configuration Management
- Main config: `application.yaml`
- Environment profiles: `application-{profile}.yaml` (h2, offline, online, staging, test)
- Spring profiles control database backend and operational mode
- Environment variables documented in README.md and Docker configs

### Development Workflow
1. **API Changes**: Edit `api.yaml` → run build → implement in controller classes
2. **Database Changes**: Modify entities → Yaci Store handles migrations automatically  
3. **New Features**: Follow domain package structure → add controller, service, mapper
4. **Testing**: Write nested test classes → use appropriate base setup class

### Common Gotchas
- OpenAPI code generation requires clean build when `api.yaml` changes
- Yaci-indexer must be running for API integration tests
- Use correct Spring profile for your database backend (h2/postgres)
- Generated OpenAPI models use different package (`org.openapitools.client.model`)
- MapStruct mappers require annotation processor to be enabled

### Module Dependencies
- **API module** communicates with yaci-indexer via HTTP (`YaciHttpGateway`)
- **Yaci-indexer** provides REST endpoints for blockchain data queries
- **Test-data-generator** creates realistic transaction scenarios for testing
- All modules share common configuration from parent POM