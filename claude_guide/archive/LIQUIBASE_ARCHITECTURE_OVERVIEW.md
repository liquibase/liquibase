# Liquibase Architecture Overview

## Project Structure

Liquibase is a Maven multi-module project for database schema change management:

### Core Modules
- **liquibase-standard**: Main implementation with all core functionality (liquibase-core is deprecated)
- **liquibase-cli**: Command-line interface
- **liquibase-maven-plugin**: Maven plugin integration
- **liquibase-extension-testing**: Testing framework for extensions
- **liquibase-integration-tests**: Integration tests for various databases

### Key Architecture Components

**Change Management** (`/liquibase-standard/src/main/java/liquibase/`)
- `change/`: Database change implementations using visitor pattern
- `changelog/`: Change log parsing and management
- `changelogparser/`: Parsers for XML, YAML, JSON, SQL formats
- `changeset/`: Individual change set processing

**Database Abstraction**
- `database/`: Database abstraction layer with factory pattern
- `sqlgenerator/`: Database-specific SQL generation
- `executor/`: SQL execution framework
- `snapshot/`: Database structure snapshot functionality

**Command Architecture**
- `command/`: Command pattern implementations for CLI operations
- Plugin architecture with service discovery (ServiceLocator)

## Testing Framework

Tests use Spock (Groovy) and JUnit 5. Integration tests use TestContainers for database instances.

```bash
# Configure which databases to test against
# Create liquibase-extension-testing/src/main/resources/liquibase.sdk.local.yml:
liquibase:
  sdk:
    testSystem:
      test: h2,mysql,postgresql  # Comma-separated list of databases
```

## Database Support

Extensible architecture for database support through JDBC. Database-specific implementations in:
- `/liquibase-standard/src/main/java/liquibase/database/core/`
- SQL generation in `/liquibase-standard/src/main/java/liquibase/sqlgenerator/core/`

## Development Notes

- Java 8 minimum, Maven 3.6+ required
- Follow existing code conventions and patterns
- Write new tests in Spock, migrate old JUnit tests when possible
- Use existing abstractions (Database, Change, SqlGenerator factories)
- Never commit sensitive information