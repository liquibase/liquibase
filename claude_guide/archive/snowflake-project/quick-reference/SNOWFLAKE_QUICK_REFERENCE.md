# Snowflake Extension Quick Reference

## Essential Commands

### Build and Deploy
```bash
# Build extension (from liquibase-snowflake directory)
mvn clean package -DskipTests

# Full build with tests
./mvnw clean install

# Install to Maven repo (test harness loads via Maven dependencies)
mvn install -DskipTests

# Run specific unit test
mvn test -Dtest=CreateSchemaChangeTest

# Run specific test method
mvn test -Dtest=YourTestClass#yourTestMethod

# Run all unit tests
mvn test

# Run integration tests
mvn verify
```

### Test Harness Commands
```bash
# Run specific test (from liquibase-test-harness directory)
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema -DdbName=snowflake

# Run multiple tests
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema,dropSchema -DdbName=snowflake

# Run with debug output
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema -DdbName=snowflake -X
```

### Common File Locations
```
# Change implementations
liquibase-snowflake/src/main/java/liquibase/change/core/

# SQL Generators
liquibase-snowflake/src/main/java/liquibase/sqlgenerator/core/snowflake/

# Unit tests
liquibase-snowflake/src/test/java/liquibase/change/core/
liquibase-snowflake/src/test/java/liquibase/sqlgenerator/core/snowflake/

# Service registration
liquibase-snowflake/src/main/resources/META-INF/services/

# XSD Schema
liquibase-snowflake/src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd

# Test harness tests
liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/
liquibase-test-harness/src/main/resources/liquibase/harness/change/expectedSql/snowflake/
liquibase-test-harness/src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/
```

### Debugging Tips

#### Check what SQL was generated
```bash
# Look for "Generated SQL:" in test output
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema -DdbName=snowflake | grep -A 10 "Generated SQL"
```

#### View test harness properties
```bash
cat src/test/resources/liquibase/liquibase.integrationtest.properties
```

#### Manual database cleanup
```bash
# Connect to Snowflake and run
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE;
USE DATABASE LTHDB;
source ../manual-cleanup.sql
```

## Common Patterns

### Adding a New Change Type
1. Create Change class: `Create<Object>Change.java`
2. Create Statement class: `Create<Object>Statement.java`
3. Create Generator: `Create<Object>GeneratorSnowflake.java`
4. Register in META-INF/services
5. Update XSD
6. Create unit tests
7. Build and deploy JAR
8. Create test harness tests

### Test File Structure
```xml
<!-- 1. Init -->
<include file="liquibase/harness/change/changelogs/snowflake/init.xml"/>

<!-- 2. Test-specific cleanup -->
<changeSet id="cleanup-test-objects" author="test-harness" runAlways="true">
    <sql>DROP SCHEMA IF EXISTS MY_SCHEMA CASCADE;</sql>
</changeSet>

<!-- 3. Test changesets -->
<changeSet id="1" author="test-harness">
    <snowflake:createSchema schemaName="MY_SCHEMA"/>
</changeSet>

<!-- 4. Account-level cleanup (if needed) -->
<changeSet id="cleanup-account-objects" author="test-harness" runAlways="true">
    <sql>
        DROP WAREHOUSE IF EXISTS MY_WAREHOUSE CASCADE;
        USE WAREHOUSE LTHDB_TEST_WH;
    </sql>
</changeSet>

<!-- 5. Global cleanup -->
<include file="liquibase/harness/change/changelogs/snowflake/cleanup.xml"/>
```

## Validation Checklist

Before running tests:
- [ ] JAR built with latest changes
- [ ] JAR copied to test harness lib/
- [ ] Test files follow 5-step structure
- [ ] Expected SQL includes ALL SQL (including cleanup)
- [ ] Object names are UPPERCASE in expected files
- [ ] No trailing spaces in expected SQL

## Common Errors and Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "No changesets to execute" | Already run | Check cleanup is working |
| "Class not found" | JAR not updated | Rebuild and copy JAR |
| "Expected sql doesn't match" | Whitespace/format | Copy actual output exactly |
| "Object already exists" | Cleanup failed | Run manual cleanup |
| "Previously run" | Dirty DATABASECHANGELOG | Ensure cleanup.xml is last |