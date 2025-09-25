# Contributing to Cardano Rosetta Java

Thank you for your interest in contributing to Cardano Rosetta Java! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Project Structure](#project-structure)
- [Making Contributions](#making-contributions)
- [Testing](#testing)
- [Pull Request Process](#pull-request-process)
- [Code Style Guidelines](#code-style-guidelines)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

Please read and follow our [Code of Conduct](CODE-OF-CONDUCT.md). We are committed to providing a welcoming and inclusive environment for all contributors.

## Getting Started

1. **Fork the Repository**: Start by forking the [cardano-rosetta-java repository](https://github.com/cardano-foundation/cardano-rosetta-java) to your GitHub account.

2. **Clone Your Fork**:
   ```bash
   git clone https://github.com/YOUR-USERNAME/cardano-rosetta-java.git
   cd cardano-rosetta-java
   ```

3. **Add Upstream Remote**:
   ```bash
   git remote add upstream https://github.com/cardano-foundation/cardano-rosetta-java.git
   ```

## Development Environment Setup

### Prerequisites

- **Java 24** with preview features enabled
- **Maven 3.9+**
- **Docker and Docker Compose**
- **Git**
- **IDE** with Java support (IntelliJ IDEA recommended)

### Local Development Setup

1. **Install Java 24**:
   ```bash
   # Using SDKMAN
   sdk install java 24-open
   sdk use java 24-open
   ```

2. **Build the Project**:
   ```bash
   # Build all modules
   mvn clean install
   
   # Build specific module
   mvn clean install -pl api
   ```

3. **Run Tests**:
   ```bash
   # Run all tests
   mvn test
   
   # Run specific test class
   mvn test -Dtest=ClassName
   ```

4. **Run Locally with H2 Database** (for development):
   ```bash
   # Set H2 profile
   export SPRING_PROFILES_ACTIVE=h2
   
   # Run the API module
   cd api
   mvn spring-boot:run
   ```

5. **Run with Docker Compose** (full stack):
   ```bash
   docker compose --env-file .env.docker-compose-preprod \
     --env-file .env.docker-compose-profile-mid-level \
     -f docker-compose.yaml up -d
   ```

## Project Structure

```
cardano-rosetta-java/
├── api/                    # Main Rosetta API implementation
│   ├── src/main/java/      # API controllers, services, mappers
│   └── src/test/           # API tests
├── yaci-indexer/           # Blockchain indexer module
├── test-data-generator/    # Test data generation utility
├── docker/                 # Docker configuration files
├── docs/                   # Documentation (Docusaurus)
└── pom.xml                # Parent POM
```

### Key Packages in API Module

- `account/` - Account balance operations
- `block/` - Block and transaction retrieval
- `construction/` - Transaction building
- `network/` - Network status and configuration
- `mempool/` - Mempool operations

## Making Contributions

### Branch Naming Convention

- `feature/description` - New features
- `fix/description` - Bug fixes
- `docs/description` - Documentation updates
- `refactor/description` - Code refactoring
- `test/description` - Test additions or fixes

### Commit Message Format

Follow the conventional commits specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Test additions or modifications
- `chore`: Build process or auxiliary tool changes

**Example**:
```
feat(api): add support for governance operations

Implements the new Conway era governance operations including
DRep delegation and voting operations.

Closes #123
```

## Testing

### Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -P integration-tests

# Test with coverage
mvn test jacoco:report
```

### Writing Tests

- Extend `BaseSpringMvcSetup` for integration tests
- Extend `BaseMapperSetup` for mapper tests
- Use `@Nested` classes to group related tests
- Use AssertJ for assertions

**Example Test Structure**:
```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest extends BaseSpringMvcSetup {
    
    @Nested
    class GetBalance {
        @Test
        void shouldReturnCorrectBalance() {
            // Given
            var address = "addr1...";
            
            // When
            var result = accountService.getBalance(address);
            
            // Then
            assertThat(result)
                .isNotNull()
                .satisfies(balance -> {
                    assertThat(balance.getValue()).isEqualTo("1000000");
                });
        }
    }
}
```

### Test Data

- Place test data files in `src/test/resources/testdata/`
- Use meaningful names for test data files
- Document complex test scenarios

## Pull Request Process

1. **Update Your Fork**:
   ```bash
   git fetch upstream
   git checkout main
   git merge upstream/main
   ```

2. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make Your Changes**:
   - Write clean, documented code
   - Add/update tests as needed
   - Update documentation if applicable

4. **Run Quality Checks**:
   ```bash
   # Format code
   mvn spotless:apply
   
   # Run tests
   mvn clean test
   
   # Check for common issues
   mvn clean verify
   ```

5. **Commit Your Changes**:
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

6. **Push to Your Fork**:
   ```bash
   git push origin feature/your-feature-name
   ```

7. **Create Pull Request**:
   - Go to the original repository
   - Click "New Pull Request"
   - Select your fork and branch
   - Fill in the PR template
   - Link related issues

### PR Requirements

- [ ] Tests pass locally
- [ ] Code follows project style guidelines
- [ ] Documentation updated (if applicable)
- [ ] Commit messages follow convention
- [ ] PR description clearly explains changes
- [ ] Related issues are linked

## Code Style Guidelines

### Java Code Style

- Use **Lombok** annotations to reduce boilerplate
- Use **MapStruct** for object mapping
- Follow standard Java naming conventions
- Maximum line length: 120 characters
- Use `@Nullable` for optional parameters

### Best Practices

1. **Never modify generated code** - OpenAPI controllers are generated
2. **Use dependency injection** - Avoid static methods where possible
3. **Write testable code** - Keep methods focused and testable
4. **Document complex logic** - Add JavaDoc for non-obvious code
5. **Handle errors gracefully** - Use proper exception handling

### Example Code Style

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final AccountRepository repository;
    private final AccountMapper mapper;
    
    /**
     * Retrieves account balance for the given address.
     * 
     * @param address The Cardano address
     * @return Account balance information
     */
    public AccountBalance getBalance(@Nullable String address) {
        if (address == null) {
            throw new InvalidAddressException("Address cannot be null");
        }
        
        return repository.findByAddress(address)
            .map(mapper::toDto)
            .orElseThrow(() -> new AccountNotFoundException(address));
    }
}
```

## Reporting Issues

### Before Creating an Issue

1. Check [existing issues](https://github.com/cardano-foundation/cardano-rosetta-java/issues)
2. Search [discussions](https://github.com/cardano-foundation/cardano-rosetta-java/discussions)
3. Review the [documentation](https://cardano-foundation.github.io/cardano-rosetta-java/docs/intro)

### Creating a Good Issue Report

**Bug Reports** should include:
- Clear, descriptive title
- Steps to reproduce
- Expected behavior
- Actual behavior
- System information (OS, Java version, etc.)
- Relevant logs or error messages

**Feature Requests** should include:
- Clear use case
- Proposed solution
- Alternative solutions considered
- Impact on existing functionality

### Issue Templates

Use the provided issue templates when creating new issues:
- 🐛 Bug Report
- ✨ Feature Request
- 📚 Documentation Update

## Getting Help

- **Discord**: [Join our community](https://discord.gg/cardanofoundation)
- **GitHub Discussions**: Ask questions and share ideas
- **Documentation**: [Project docs](https://cardano-foundation.github.io/cardano-rosetta-java/docs/intro)

## Recognition

Contributors will be recognized in:
- Release notes
- Project documentation
- GitHub contributors page

Thank you for contributing to Cardano Rosetta Java! Your efforts help make Cardano integration easier and more reliable for everyone.