# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CRITICAL: AI Development Pattern Library for Test Harness

**When working on Liquibase Test Harness (especially Snowflake tests), ALWAYS consult the AI Extension Developer Guide FIRST:**
- Location: `liquibase-test-harness/claude_guide/AI_EXTENSION_DEVELOPER_GUIDE.md`
- Purpose: Provides exact templates and patterns that MUST be followed
- Requirement: Do NOT deviate from documented patterns unless explicitly instructed

### Test Harness Development Philosophy
1. **Pattern-First Development**: Always use existing working patterns from the guide
2. **Document As You Go**: When discovering new patterns, immediately document them
3. **Test-Driven**: Write tests following the exact templates before implementation
4. **Copy Don't Create**: Use working examples as templates rather than creating from scratch

### Before ANY Snowflake Test Development
1. READ: `liquibase-test-harness/claude_guide/AI_EXTENSION_DEVELOPER_GUIDE.md`
2. IDENTIFY: Is it schema-level or account-level object?
3. COPY: Use the exact template for that type
4. FOLLOW: The validation checklist exactly
5. TEST: Using the documented test execution pattern

### Test Harness Knowledge Base Structure
```
liquibase-test-harness/claude_guide/
├── AI_EXTENSION_DEVELOPER_GUIDE.md     # START HERE - Main guide for all development
├── SNOWFLAKE_TEST_PATTERNS_COMPLETE.md # Detailed explanations
├── SNOWFLAKE_TEST_QUICK_REFERENCE.md   # Quick lookup
└── SNOWFLAKE_SCHEMA_VS_ACCOUNT_OBJECTS.md # Object categorization
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
Working on improving the Liquibase Snowflake Community Extension by implementing missing features for 5 specific object types as outlined in the requirements document.

### Key Requirements
Focus on these 5 objects only (from claude_folder/Snowflake Enhancements Needed.md):
- TABLE: Additional features (INT-155, INT-149, INT-150, INT-148)
- SEQUENCE: ORDER support (INT-151)
- DATABASE: Full object support (INT-1244)
- SCHEMA: Full object support (INT-1245)
- WAREHOUSE: Full object support (INT-1287)

### Build Commands for Snowflake Development
```bash
# Build entire Liquibase project (excluding problematic maven plugin)
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'

# Run Snowflake integration tests
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake
```

### Test Infrastructure
- Tests must be placed in `liquibase-integration-tests` module (not in snowflake module)
- Snowflake connection is configured in `liquibase-extension-testing/src/main/resources/liquibase.sdk.local.yaml`
- URL format must be: `LWMNXLH-AUB54519.snowflakecomputing.com` (not just account ID)
- See `claude_folder/snowflake-test-process.md` for detailed test patterns and troubleshooting

### Current Status
- ✅ Built Liquibase successfully
- ✅ Created integration test infrastructure
- ✅ Verified Snowflake connection works (version 11.13.0)
- ✅ Confirmed change types don't exist yet (ready for implementation)
- 🔄 Next: Implement WAREHOUSE object change types

### Implementation Pattern
1. Create change type classes extending `AbstractChange`
2. Create statement classes implementing `SqlStatement`
3. Create SQL generators extending `AbstractSqlGenerator<Statement>`
4. Register in META-INF/services files
5. Add snapshot support for object discovery
6. Write comprehensive tests

### Important Notes
- Use test-first development approach
- Always run tests with real Snowflake database (no mocks)
- Follow existing Liquibase patterns found in other database extensions
- Check existing Snowflake code for patterns before implementing new features