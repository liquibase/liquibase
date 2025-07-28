# Snowflake Extension Test Structure

## Overview

This directory contains all unit and integration tests for the Liquibase Snowflake extension. Tests are organized by component type and follow a consistent naming pattern.

## Test Organization

```
src/test/java/
└── liquibase/
    ├── SnowflakeExtensionTestSuite.java    # Main test suite
    ├── ServiceRegistrationTest.java         # Service registration tests
    ├── change/
    │   └── core/
    │       ├── CreateSchemaChangeTest.java
    │       ├── DropSchemaChangeTest.java
    │       ├── AlterSchemaChangeTest.java
    │       ├── CreateDatabaseChangeTest.java
    │       ├── DropDatabaseChangeTest.java
    │       ├── AlterDatabaseChangeTest.java
    │       ├── CreateWarehouseChangeTest.java
    │       ├── DropWarehouseChangeTest.java
    │       └── AlterWarehouseChangeTest.java
    ├── statement/
    │   └── core/
    │       ├── CreateSchemaStatementTest.java
    │       ├── DropSchemaStatementTest.java
    │       ├── AlterSchemaStatementTest.java
    │       └── ... (other statement tests)
    └── sqlgenerator/
        └── core/
            └── snowflake/
                ├── CreateSchemaGeneratorSnowflakeTest.java
                ├── DropSchemaGeneratorSnowflakeTest.java
                ├── AlterSchemaGeneratorSnowflakeTest.java
                └── ... (other generator tests)
```

## Test Naming Convention

- **Change Tests**: `<ChangeType>ChangeTest.java`
- **Statement Tests**: `<ChangeType>StatementTest.java`
- **Generator Tests**: `<ChangeType>GeneratorSnowflakeTest.java`

## Test Coverage

Each change type should have:

1. **Change Test** - Tests the Change class:
   - Property getters/setters
   - supports() method
   - generateStatements()
   - validate()
   - Rollback support
   - Confirmation message

2. **Statement Test** - Tests the Statement class:
   - All property getters/setters
   - Initial state
   - Null handling

3. **Generator Test** - Tests the SQL Generator:
   - supports() method
   - Basic SQL generation
   - All SQL variations
   - Special character handling
   - Edge cases

4. **Integration Test** - In ServiceRegistrationTest:
   - Service registration
   - Factory lookup
   - Namespace registration

## Current Status

✅ **CreateSchema** - Complete (Change, Statement, Generator tests)
✅ **DropSchema** - Complete (Change, Statement, Generator tests)  
⭕ **AlterSchema** - Pending
⭕ **CreateDatabase** - Pending
⭕ **DropDatabase** - Pending
⭕ **AlterDatabase** - Pending
⭕ **CreateWarehouse** - Pending
⭕ **DropWarehouse** - Pending
⭕ **AlterWarehouse** - Pending

## Running Tests

### Run all tests:
```bash
mvn test
```

### Run test suite:
```bash
mvn test -Dtest=SnowflakeExtensionTestSuite
```

### Run tests for specific change type:
```bash
# All CreateSchema tests
mvn test -Dtest=*CreateSchema*

# Just the change test
mvn test -Dtest=CreateSchemaChangeTest
```

### Run with debugging:
```bash
mvn test -Dtest=CreateSchemaChangeTest -Dmaven.surefire.debug
```

## Adding New Tests

When implementing a new change type:

1. Create the three test classes following the naming convention
2. Add them to `SnowflakeExtensionTestSuite` in the correct section
3. Ensure all test methods have `@DisplayName` annotations
4. Follow the existing test patterns for consistency

## Test Data

- Use "TEST_" prefix for test object names
- Use descriptive names like "TEST_BASIC", "TEST_TRANSIENT"
- Clean up any created objects in tests

## Best Practices

1. **Test in isolation** - Each test should be independent
2. **Use @DisplayName** - Make test purposes clear
3. **Test edge cases** - Null values, empty strings, special characters
4. **Mock sparingly** - Prefer real objects where possible
5. **Assert specifically** - Use meaningful assertion messages

## Integration with Test Harness

These unit tests run independently of the Liquibase Test Harness. The test harness tests are located in a separate repository and test the full end-to-end functionality against a real Snowflake database.