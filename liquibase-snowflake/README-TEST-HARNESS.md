# Liquibase Snowflake Test Harness Integration

This document describes how to integrate and test the Liquibase Snowflake extension with the Liquibase Test Harness.

## Prerequisites

1. Build and install the liquibase-snowflake extension:
   ```bash
   mvn clean install
   ```

2. Ensure the Snowflake database is configured with proper credentials in the test harness configuration.

## Common Issues and Solutions

### XSD Resolution Error

**Problem**: When running tests, you may encounter:
```
Unable to resolve xml entity http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
```

**Cause**: The liquibase-snowflake JAR is not on the test classpath. The XSD file is packaged inside the JAR at `/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd` and needs to be accessible for XML validation.

**Solution**: Ensure the liquibase-snowflake dependency is included in the test harness POM:
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-snowflake</artifactId>
    <version>0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### How XSD Resolution Works

1. **SnowflakeNamespaceDetails** class implements the `NamespaceDetails` interface to map the namespace URL to the XSD location
2. The class is registered via service discovery in `META-INF/services/liquibase.parser.NamespaceDetails`
3. When Liquibase encounters the Snowflake namespace in XML, it uses this mapping to find the XSD in the classpath
4. The XSD must be at the exact path: `/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd`

### Database Cleanup (Following Aurora Pattern)

The test harness uses init changelogs to ensure a clean database state before tests run. The Snowflake init file (`src/test/resources/init-changelogs/snowflake/snowflake.sql`) includes:

1. **Warehouse Cleanup**: Drops test warehouses with `runAlways:true`
2. **Warehouse Resume**: Resumes the main test warehouse if it's suspended
3. **Table Creation**: Creates standard test tables (authors, posts) 
4. **Test Data**: Inserts sample data for testing

All changesets use `runAlways:true` to ensure consistent test execution.

### Rollback Strategies

The test harness supports two rollback strategies:
- `rollbackByTag` (recommended for cloud databases): Tags database state and rolls back to that tag
- `rollbackToDate` (default): Records timestamp and rolls back changes after that time

Usage:
```bash
mvn test -DrollbackStrategy=rollbackByTag -DdbName=snowflake
```

## Running Tests

1. Clean the database (see above)
2. Run specific tests:
   ```bash
   mvn test -Dtest=ChangeObjectTests -DchangeObjects=createWarehouse -DdbName=snowflake
   ```

## Test Development Tips

1. **Expected SQL Format**: Snowflake generates SQL without quotes around certain values (e.g., `XSMALL` not `'XSMALL'`, `true` not `TRUE`)
2. **Test Phases**: Tests run in two phases - `updateSql` (generates SQL) and `update` (executes changes). Object conflicts can occur between phases.
3. **Warehouse States**: Warehouses may be suspended and need to be resumed for certain operations.