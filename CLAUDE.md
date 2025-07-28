# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CRITICAL: Snowflake Extension Development Pattern Library

**When working on Liquibase Snowflake Extension, ALWAYS consult these guides in order:**

### 1. Implementation Guide (Start Here)
- Location: `claude_guide/roles/developer/patterns/NEW_CHANGETYPE_PATTERN_2.md`
- Purpose: Step-by-step implementation with integrated testing
- Includes: Change class, Statement, Generator, Service registration, XSD, Unit tests

### 2. Test Harness Guide (After Implementation)
- Location: `claude_guide/roles/qa/patterns/TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`
- Purpose: End-to-end database testing against real Snowflake
- Prerequisite: All unit tests passing

### 3. Detailed Requirements (For Each Change Type)
- Location: `claude_guide/project/requirements/detailed_requirements/<changeType>_requirements.md`
- Purpose: Specific requirements, mutual exclusivity rules, SQL variations
- Example: `createSchema_requirements.md`

### Development Philosophy
1. **Pattern-First Development**: Use proven patterns from the guides
2. **Test-Driven**: Write tests at each step, not just at the end
3. **Document As You Go**: Update patterns when discovering new insights
4. **Copy Don't Create**: Use working examples as templates

### Knowledge Base Structure
```
claude_guide/
├── roles/
│   ├── developer/patterns/
│   │   ├── NEW_CHANGETYPE_PATTERN_2.md          # Implementation guide
│   │   └── CHANGE_CLASS_CHECKLIST.md            # Quick reference
│   └── qa/patterns/
│       ├── TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md # Test harness guide
│       └── INTEGRATION_TEST_CHECKLIST.md         # Testing checklist
├── project/requirements/
│   └── detailed_requirements/                     # Per-feature requirements
└── examples/                                      # Reference implementations
    └── postgresql/                                # CREATE DOMAIN example
```

## Build Commands

```bash
# Full build with tests
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Run unit tests only
./mvnw test

# Run integration tests
./mvnw verify

# Run a single test class
./mvnw test -Dtest=YourTestClass

# Run a single test method
./mvnw test -Dtest=YourTestClass#yourTestMethod
```

## Architecture Overview

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

## Testing

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

## Snowflake Extension Development

### Project Context
Working on the Liquibase Snowflake Extension in `liquibase-snowflake/` directory. This is a separate Maven module that extends Liquibase with Snowflake-specific functionality.

### Key Requirements
Focus on these 5 objects (from original requirements):
- TABLE: Additional features (INT-155, INT-149, INT-150, INT-148)
- SEQUENCE: ORDER support (INT-151)
- DATABASE: Full object support (INT-1244)
- SCHEMA: Full object support (INT-1245) - ✅ CreateSchema implemented
- WAREHOUSE: Full object support (INT-1287)

### Build Commands for Snowflake Extension

```bash
# Build Snowflake extension only
cd liquibase-snowflake
mvn clean package -DskipTests

# Run unit tests
mvn test

# Run specific test
mvn test -Dtest=CreateSchemaChangeTest

# Copy JAR to test harness
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/
```

### Test Infrastructure

#### Unit/Integration Tests (in liquibase-snowflake)
- Location: `src/test/java/liquibase/`
- Framework: JUnit 5 (Jupiter)
- Structure: See `src/test/java/README_TEST_STRUCTURE.md`
- Purpose: Test Java code without database connection

#### Test Harness Tests (in liquibase-test-harness)
- Location: `liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/`
- Purpose: End-to-end testing against real Snowflake database
- Prerequisites: All unit tests passing, JAR deployed to test harness

### Current Status
- ✅ CreateSchema fully implemented with tests
- ✅ DropSchema implemented (example for rollback support)
- ✅ Comprehensive pattern documentation created
- 🔄 Next: Implement remaining change types following patterns

### Implementation Workflow
1. **Create Requirements**: Document in `detailed_requirements/<changeType>_requirements.md`
2. **Implement Code**: Follow `NEW_CHANGETYPE_PATTERN_2.md`
   - Change class → Statement → Generator → Service registration → XSD
3. **Unit Tests**: Write tests at each step
4. **Test Harness**: Follow `TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`
   - Only after all unit tests pass

### Important Notes
- **Namespace**: All Snowflake changes use `snowflake:` namespace
- **Testing**: JUnit 5 only (no JUnit 4 dependencies)
- **SQL Generation**: Snowflake uses uppercase identifiers by default
- **Documentation**: Update patterns when discovering new insights