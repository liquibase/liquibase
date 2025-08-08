# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dependency Versions

### Programming Language
- **Java**: 1.8 (source and target)
- **Maven Compiler Release**: 8 (for non-JDK 8 profiles)

### Build Tools
- **Maven**: 3.9.5 (via wrapper)
- **Required Maven Version**: 3.6+
- **Maven Wrapper**: 3.2.0

### Core Frameworks
- **Spring Framework**: 5.3.39
- **Picocli (CLI Framework)**: 4.7.7
- **Apache Commons Lang3**: 3.18.0
- **Apache Commons IO**: 2.20.0
- **Apache Commons Collections4**: 4.5.0
- **Apache Commons Text**: 1.14.0
- **SnakeYAML**: 2.4
- **OpenCSV**: 5.12.0
- **Lombok**: 1.18.38
- **JAXB API**: 2.3.1
- **JavaCC**: 7.0.13

### Testing Frameworks
- **JUnit Jupiter**: 5.13.4
- **Groovy**: 4.0.28
- **Spock**: 2.3-groovy-4.0
- **Mockito**: 4.11.0 (5.8.0 for non-JDK 8)
- **Hamcrest**: 3.0
- **AssertJ**: 3.27.3
- **CGLib**: 3.3.0
- **Objenesis**: 3.4

### Database Drivers (Test Dependencies)
- **H2 Database**: 2.2.224
- **HSQLDB**: 2.7.4
- **MySQL Connector/J**: 9.4.0
- **Microsoft SQL Server JDBC**: 12.10.1.jre8
- **PostgreSQL JDBC**: 42.7.7
- **MariaDB Java Client**: 3.5.4
- **IBM DB2 JCC**: 11.5.9.0
- **Oracle JDBC (ojdbc8)**: 19.28.0.0
- **SQLite JDBC**: 3.50.3.0
- **Snowflake JDBC**: 3.25.1
- **Firebird Jaybird**: 5.0.8.java8

### Build Plugins
- **Maven Compiler Plugin**: 3.14.0
- **Maven Resources Plugin**: 3.3.1
- **Maven Enforcer Plugin**: 3.6.1
- **GMavenPlus Plugin**: 4.2.1
- **Maven Surefire Plugin**: 3.5.3
- **Maven Failsafe Plugin**: 3.5.3
- **JaCoCo Maven Plugin**: 0.8.13
- **Sonar Maven Plugin**: 5.1.0.4751
- **Maven Deploy Plugin**: 3.1.4
- **Maven Source Plugin**: 3.3.1
- **Maven Javadoc Plugin**: 3.11.2
- **Flatten Maven Plugin**: 1.7.2
- **Maven Install Plugin**: 3.1.4
- **Maven Bundle Plugin (OSGi)**: 5.1.9
- **Liquibase SDK Maven Plugin**: 0.10.25

### Other Dependencies
- **Apache Ant**: 1.10.15
- **JAXB Core/Runtime**: 4.0.5
- **OSGi Core**: 8.0.0
- **NanoHTTPD (test)**: 2.3.1
- **Javax Servlet API**: 3.1.0
- **Jakarta Servlet API**: 5.0.0

### Files Reviewed
- `/home/wesley/workspace/liquibase/pom.xml` (root POM)
- `/home/wesley/workspace/liquibase/liquibase-standard/pom.xml`
- `/home/wesley/workspace/liquibase/liquibase-core/pom.xml`
- `/home/wesley/workspace/liquibase/.mvn/wrapper/maven-wrapper.properties`

## Build System & Common Commands

Liquibase is a Maven-based Java project targeting Java 8+ with a multi-module structure.

### Key Build Commands
```bash
# Build the entire project
mvn clean compile

# Run unit tests
mvn test

# Run integration tests
mvn failsafe:integration-test

# Build and install locally
mvn clean install

# Build specific module
mvn clean install -pl liquibase-core

# Run tests without failing on missing tests (some modules)
mvn test -Dsurefire.failIfNoTests=false

# Run with code coverage
mvn clean test jacoco:report

# Build distribution package
mvn clean install -pl liquibase-dist
```

### Test Execution
- **Unit Tests**: Use `mvn test` or `mvn surefire:test`
- **Integration Tests**: Use `mvn failsafe:integration-test` or `mvn verify`
- **Test Framework**: Tests are written in Spock (Groovy) for new tests, with legacy JUnit tests
- **Test Patterns**: Test files match `*Test.java`, `*Tests.java`, `*TestCase.java`, `*Spec.java`

### Testing with Different Databases
- Default tests run against H2, HSQLDB, and SQLite (in-memory databases)
- Configure `liquibase.sdk.local.yml` in `liquibase-extension-testing/src/main/resources/` to test against other databases
- Use `liquibase.sdk.testSystem.test` property to specify database systems (e.g., `mysql,postgresql,mssql`)

## Architecture Overview

### Core Module Structure
1. **liquibase-standard**: Core Liquibase functionality, database-agnostic logic
2. **liquibase-core**: Main public API, depends on liquibase-standard and CLI modules
3. **liquibase-cli**: Command-line interface components
4. **liquibase-integration-tests**: Cross-database integration tests
5. **liquibase-extension-testing**: Test framework for database integration testing
6. **liquibase-maven-plugin**: Maven plugin for Liquibase
7. **liquibase-snowflake**: Snowflake-specific database support
8. **liquibase-dist**: Distribution and packaging

### Key Architecture Concepts

#### Service Locator Pattern
Liquibase uses a service locator pattern with `@LiquibaseService` annotations for plugin discovery and dependency injection.

#### Database Abstraction
- **Database**: Core interface representing different database platforms
- **DatabaseConnection**: Abstracts database connections
- **Executor**: Handles SQL statement execution
- **SqlGenerator**: Generates database-specific SQL from abstract statements

#### Change Management
- **Change**: Represents a database modification (AddColumnChange, CreateTableChange, etc.)
- **ChangeSet**: Groups related changes with metadata (author, id, contexts)
- **DatabaseChangeLog**: Root container for all changes
- **ChangeLogParser**: Parses changelog files (XML, YAML, JSON, SQL)

#### Core Processing Flow
1. **Parser Layer**: `liquibase.parser.*` - Parses changelog files
2. **Change Layer**: `liquibase.change.*` - Represents database modifications
3. **Statement Layer**: `liquibase.statement.*` - Database-agnostic SQL statements
4. **SQL Generator Layer**: `liquibase.sqlgenerator.*` - Database-specific SQL generation
5. **Executor Layer**: `liquibase.executor.*` - SQL execution

#### Configuration System
- Uses `liquibase.configuration.*` for hierarchical configuration
- Supports environment variables, system properties, config files
- Configuration providers are pluggable via service locator

#### Resource Management
- `liquibase.resource.*` handles file and classpath resource access
- Supports multiple resource accessors (filesystem, classpath, zip)
- Path handlers for different resource types

### Module Dependencies
```
liquibase-core
├── liquibase-standard (core logic)
├── liquibase-cli (command line)
└── picocli (CLI framework)

liquibase-integration-tests
└── liquibase-extension-testing (test framework)
```

### Important Directories
- `/liquibase-standard/src/main/java/liquibase/` - Core Liquibase classes
- `/liquibase-integration-tests/src/test/` - Database integration tests
- `/liquibase-extension-testing/src/main/resources/` - Test configuration
- `/.github/workflows/` - CI/CD workflows

## Development Guidelines

### Code Conventions
- Java 8 compatibility (source/target)
- Maven build lifecycle
- Spock/Groovy for new tests, JUnit for legacy
- Service locator pattern for extensibility

### Database Support
- Core supports H2, PostgreSQL, MySQL, MSSQL, Oracle, DB2, SQLite
- Extension modules for specialized databases (Snowflake, etc.)
- Use integration testing framework for database-specific features

### Release Process
- Version format: semantic versioning
- Maven coordinates: `org.liquibase:liquibase-core`
- GitHub releases with automated workflows
- Sonatype deployment for Maven Central

## Coding Patterns and Conventions

### Package Naming Conventions
- Base package: `liquibase.*`
- Core functionality: `liquibase.change`, `liquibase.database`, `liquibase.command`
- Support/utilities: `liquibase.util`, `liquibase.exception`, `liquibase.configuration`
- Implementation packages: `liquibase.*.core` (e.g., `liquibase.change.core`, `liquibase.database.core`)
- Service/factory pattern: `liquibase.*Factory`, `liquibase.*Service`

### Exception Handling Patterns
- Base exception: `LiquibaseException` - all custom exceptions extend this
- Common exception types:
  - `ValidationFailedException` - for validation errors
  - `DatabaseException` - for database-related errors
  - `ChangeLogParseException` - for parsing errors
  - `CommandExecutionException` - for command execution failures
- Exception constructors typically support:
  - Message only: `new LiquibaseException("message")`
  - Message and cause: `new LiquibaseException("message", cause)`
  - Cause only: `new LiquibaseException(cause)`

### Validation Patterns
- Primary validation class: `ValidationErrors`
- Common validation methods:
  ```java
  ValidationErrors errors = new ValidationErrors();
  errors.checkRequiredField("fieldName", value);
  errors.checkRequiredField("fieldName", value, postfix, allowEmptyValue);
  errors.checkDisallowedField("fieldName", value, database, disallowedDatabases);
  if (errors.hasErrors()) {
      throw new ValidationFailedException(errors);
  }
  ```
- Changes implement validation via `validate(Database database)` method
- Validation errors include both errors and warnings

### Testing Patterns
- Test framework: Spock (Groovy) for new tests, JUnit for legacy
- Test naming conventions:
  - Unit tests: `*Test.groovy` or `*Test.java`
  - Specification tests: `*Spec.groovy`
  - Integration tests: Located in `liquibase-integration-tests` module
- Test structure:
  - Spock tests use `given:`, `when:`, `then:` blocks
  - Abstract test classes like `AbstractChangeTest` provide common test infrastructure
  - Data-driven tests use `@Unroll` with `where:` blocks
- Common test utilities:
  - `MockDatabase` for database mocking
  - `TestUtil` for common test operations

### Liquibase Changeset Structure
- XML namespace: `http://www.liquibase.org/xml/ns/dbchangelog`
- Basic changeset structure:
  ```xml
  <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog 
                                         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
      
      <changeSet id="unique-id" author="author-name">
          <!-- Changes go here -->
      </changeSet>
  </databaseChangeLog>
  ```
- Common elements:
  - Properties: `<property name="name" value="value"/>`
  - Preconditions: `<preConditions>...</preConditions>`
  - Include files: `<include file="path/to/file.xml"/>`
  - Context/labels: `context="dev"`, `labels="v1.0"`
  - Rollback: `<rollback>...</rollback>`

### Custom Annotations
- `@LiquibaseService` - Marks classes for service locator discovery
- `@DatabaseChange` - Declares change metadata:
  ```java
  @DatabaseChange(name = "createTable", 
                  description = "Creates a new table", 
                  priority = ChangeMetaData.PRIORITY_DEFAULT)
  ```
- `@DatabaseChangeProperty` - Marks change properties with metadata
- `@Beta` - Marks experimental features
- `@DataTypeInfo` - Provides data type metadata
- `@ExtensibleObjectAttribute` - For extensible object attributes

### Frequently Used Utilities
- **StringUtil**: String manipulation utilities
  - `trimToNull()`, `trimToEmpty()` (deprecated in favor of Apache Commons)
  - `processMultiLineSQL()` - SQL parsing
  - `join()`, `pad()`, `repeat()`
- **ObjectUtil**: Reflection and property utilities
  - `getProperty()`, `setProperty()` - Bean property access
  - `convert()` - Type conversion
  - `hasProperty()`, `hasWriteProperty()`
- **CollectionUtil**: Collection utilities
  - `createIfNull()` - Null-safe collection creation
  - `flatten()` - Flattens nested collections
- **Validate**: Validation utilities
  - `notNull()` - Null checks with exceptions
  - `isTrue()` - Condition validation
- **SqlUtil**: SQL-related utilities
- **FileUtil**: File system operations

### Common Design Patterns
1. **Service Locator Pattern**: Used extensively for plugin discovery
   - Services implement interfaces and are annotated with `@LiquibaseService`
   - `ServiceLocator` finds implementations at runtime
   
2. **Factory Pattern**: Object creation
   - `ChangeFactory`, `DatabaseFactory`, `CommandFactory`
   - Factories use service locator to find implementations
   
3. **Chain of Responsibility**: SQL generation
   - `SqlGeneratorChain` passes requests through generators
   - Generators can handle or delegate
   
4. **Visitor Pattern**: Changelog processing
   - `ChangeSetVisitor` for traversing changesets
   - `ChangeExecListener` for execution events

### Code Style Guidelines
- Use dependency injection via Scope
- Prefer composition over inheritance
- Implement proper `equals()`, `hashCode()`, and `toString()`
- Use `ValidationErrors` for validation, not direct exceptions
- Follow existing patterns in the module you're working in
- No comments unless specifically requested
- Use existing utilities from `liquibase.util.*` package