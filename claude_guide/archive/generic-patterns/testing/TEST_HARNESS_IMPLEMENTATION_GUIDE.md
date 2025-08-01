# Test Harness Implementation Guide - Step-by-Step Process

## Overview

This guide provides a formulaic, error-proof process for implementing test harness tests AFTER all unit and integration tests pass.

## Prerequisites

- [ ] All Step 1-6 unit tests passing
- [ ] Extension JAR builds successfully
- [ ] Detailed requirements document exists
- [ ] Understanding of mutual exclusivity rules

## Implementation Process

### Step 1: Plan Test Files

Based on your requirements document, determine how many test files you need:

1. **Single file** if all features can be combined
2. **Multiple files** if you have mutually exclusive options

Example for createSchema:
- `createSchema.xml` - Basic features, transient, managed access
- `createOrReplaceSchema.xml` - OR REPLACE (mutually exclusive with IF NOT EXISTS)
- `createSchemaIfNotExists.xml` - IF NOT EXISTS (mutually exclusive with OR REPLACE)

### Step 2: Create Changelog File

Location: `liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/<changeType>.xml`

#### Template Structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/snowflake
        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- STEP 2.1: ALWAYS include init.xml first -->
    <include file="liquibase/harness/change/changelogs/snowflake/init.xml"/>
    
    <!-- STEP 2.2: Add cleanup changeset with runAlways="true" -->
    <changeSet id="cleanup" author="test-harness" runAlways="true">
        <sql>
            <!-- List ALL objects your test will create -->
            <!-- Use IF EXISTS to prevent errors -->
        </sql>
    </changeSet>

    <!-- STEP 2.3: Add test changesets (one per variation) -->
    <!-- DO NOT use runAlways on these -->
    
</databaseChangeLog>
```

#### Cleanup Changeset Rules:

1. **MUST** have `runAlways="true"`
2. **MUST** use `IF EXISTS` clause
3. **MUST** list every object the test creates
4. **MUST** use CASCADE if objects have dependencies

Example cleanup:
```xml
<changeSet id="cleanup" author="test-harness" runAlways="true">
    <sql>
        DROP SCHEMA IF EXISTS TEST_BASIC CASCADE;
        DROP SCHEMA IF EXISTS TEST_TRANSIENT CASCADE;
        DROP SCHEMA IF EXISTS TEST_MANAGED CASCADE;
        DROP SCHEMA IF EXISTS TEST_RETENTION CASCADE;
    </sql>
</changeSet>
```

#### Test Changeset Rules:

1. **DO NOT** use `runAlways="true"`
2. **Use unique IDs** (1, 2, 3... or descriptive like "basic", "transient")
3. **Test one concept per changeset** when possible
4. **Add comments** to describe what you're testing

Example test changesets:
```xml
<!-- Test 1: Basic schema creation -->
<changeSet id="1" author="test-harness">
    <snowflake:createSchema schemaName="TEST_BASIC"/>
</changeSet>

<!-- Test 2: Transient schema -->
<changeSet id="2" author="test-harness">
    <snowflake:createSchema schemaName="TEST_TRANSIENT"
                           transient="true"/>
</changeSet>

<!-- Test 3: Schema with all common options -->
<changeSet id="3" author="test-harness">
    <snowflake:createSchema schemaName="TEST_MANAGED"
                           managedAccess="true"
                           dataRetentionTimeInDays="7"
                           comment="Test schema with managed access"/>
</changeSet>
```

### Step 3: Generate Expected SQL

#### Step 3.1: Run updateSql to capture output

```bash
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

The test will fail but will show you the generated SQL.

#### Step 3.2: Create expectedSql file

Location: `liquibase-test-harness/src/main/resources/liquibase/harness/change/expectedSql/snowflake/<changeType>.sql`

**CRITICAL**: The file MUST include:
1. ALL SQL from init.xml (the schema reset)
2. The cleanup SQL
3. The actual test SQL
4. NO semicolons at line ends
5. NO extra blank lines

Template:
```sql
-- Liquibase Snowflake SQL
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE
CREATE SCHEMA TESTHARNESS
USE SCHEMA TESTHARNESS
GRANT ALL PRIVILEGES ON SCHEMA TESTHARNESS TO ROLE LIQUIBASE_TEST_HARNESS_ROLE
CREATE TABLE DATABASECHANGELOG (
ID VARCHAR(255) NOT NULL,
AUTHOR VARCHAR(255) NOT NULL,
FILENAME VARCHAR(255) NOT NULL,
DATEEXECUTED TIMESTAMP NOT NULL,
ORDEREXECUTED INT NOT NULL,
EXECTYPE VARCHAR(10) NOT NULL,
MD5SUM VARCHAR(35),
DESCRIPTION VARCHAR(255),
COMMENTS VARCHAR(255),
TAG VARCHAR(255),
LIQUIBASE VARCHAR(20),
CONTEXTS VARCHAR(255),
LABELS VARCHAR(255),
DEPLOYMENT_ID VARCHAR(10)
)
CREATE TABLE DATABASECHANGELOGLOCK (
ID INT NOT NULL,
LOCKED BOOLEAN NOT NULL,
LOCKGRANTED TIMESTAMP,
LOCKEDBY VARCHAR(255),
PRIMARY KEY (ID)
)
INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE)
-- Your cleanup SQL here
DROP SCHEMA IF EXISTS TEST_BASIC CASCADE
DROP SCHEMA IF EXISTS TEST_TRANSIENT CASCADE
-- Your actual test SQL here
CREATE SCHEMA TEST_BASIC
CREATE TRANSIENT SCHEMA TEST_TRANSIENT
```

### Step 4: Create Expected Snapshot

Location: `liquibase-test-harness/src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/<changeType>.json`

#### Step 4.1: Determine snapshot structure

The structure depends on what database objects you're creating:

- Schemas go in `"schemas": []`
- Tables go in `"tables": []`
- Warehouses go in `"warehouses": []`
- etc.

#### Step 4.2: Create JSON file

Template for schemas:
```json
{
  "schemas": [
    {
      "schema": {
        "name": "TEST_BASIC"
      }
    },
    {
      "schema": {
        "name": "TEST_TRANSIENT"
      }
    },
    {
      "schema": {
        "name": "TEST_MANAGED",
        "remarks": "Test schema with managed access"
      }
    }
  ]
}
```

Rules:
1. Object names are UPPERCASE (Snowflake default)
2. Only include attributes that are captured in snapshots
3. Comments appear as "remarks"
4. Order should match creation order

### Step 5: Test Execution

#### Step 5.1: Build and deploy extension

```bash
cd liquibase-snowflake
mvn package -DskipTests
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/
```

#### Step 5.2: Run test

```bash
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

#### Step 5.3: Interpret results

**SQL Comparison Failure**:
```
FAIL! Expected sql doesn't match generated sql!
EXPECTED SQL: ...
GENERATED SQL: ...
```
- Copy the GENERATED SQL exactly
- Update expectedSql file
- Pay attention to whitespace and newlines

**Snapshot Comparison Failure**:
```
Expected: <attribute>
     but: was <different-value>
```
- Check if attribute is actually captured in snapshots
- Verify case sensitivity
- Check data type conversions

**Execution Failure**:
```
Migration failed for changeset
```
- Object might already exist (check cleanup)
- Syntax error in generated SQL
- Permission issues

### Step 6: Common Patterns

#### Pattern 1: Testing Mutually Exclusive Options

Create separate files:

```
createSchema.xml              - Basic features
createOrReplaceSchema.xml     - OR REPLACE tests  
createSchemaIfNotExists.xml   - IF NOT EXISTS tests
```

#### Pattern 2: Testing Boolean Attributes

```xml
<!-- Explicitly test both true and false -->
<changeSet id="1" author="test-harness">
    <snowflake:createSchema schemaName="TEST_TRANSIENT_TRUE"
                           transient="true"/>
</changeSet>

<changeSet id="2" author="test-harness">
    <snowflake:createSchema schemaName="TEST_TRANSIENT_FALSE"
                           transient="false"/>
</changeSet>
```

#### Pattern 3: Testing with Dependencies

```xml
<!-- Create dependency first -->
<changeSet id="setup" author="test-harness">
    <snowflake:createSchema schemaName="PARENT_SCHEMA"/>
</changeSet>

<!-- Then test the feature -->
<changeSet id="test" author="test-harness">
    <createTable schemaName="PARENT_SCHEMA" tableName="TEST_TABLE">
        <column name="id" type="int"/>
    </createTable>
</changeSet>
```

### Step 7: Troubleshooting Checklist

When tests fail:

1. **Check init.xml is included** - First line after databaseChangeLog
2. **Check cleanup has runAlways="true"** - Only cleanup, not test changesets  
3. **Check expectedSql includes ALL SQL** - Including init.xml output
4. **Check for Snowflake state** - Run cleanup SQL manually if needed
5. **Check JAR is updated** - Rebuild and copy after every change
6. **Check for typos in XML** - Namespace prefix, attribute names
7. **Check snapshot attributes** - Not all attributes are captured

### Step 8: Advanced Scenarios

#### Scenario 1: Testing Rollback

If your change supports rollback:

```xml
<changeSet id="test-rollback" author="test-harness">
    <snowflake:createSchema schemaName="ROLLBACK_TEST"/>
    <rollback>
        <snowflake:dropSchema schemaName="ROLLBACK_TEST"/>
    </rollback>
</changeSet>
```

#### Scenario 2: Testing Preconditions

```xml
<changeSet id="with-precondition" author="test-harness">
    <preConditions onFail="MARK_RAN">
        <not>
            <schemaExists schemaName="CONDITIONAL_SCHEMA"/>
        </not>
    </preConditions>
    <snowflake:createSchema schemaName="CONDITIONAL_SCHEMA"/>
</changeSet>
```

#### Scenario 3: Testing Updates vs UpdateSql

The test harness runs both:
1. `updateSql` - Captures SQL without executing
2. `update` - Actually executes the SQL

Both must succeed for the test to pass.

## Summary Checklist

Before submitting test harness tests:

- [ ] Created changelog with init.xml include
- [ ] Added cleanup changeset with runAlways="true"
- [ ] Created test changesets without runAlways
- [ ] Generated and verified expectedSql
- [ ] Created expectedSnapshot JSON
- [ ] Tested mutually exclusive options in separate files
- [ ] All tests pass locally
- [ ] JAR is built and deployed

## Key Success Factors

1. **Always include init.xml** - It's not optional
2. **Cleanup is critical** - Prevents state pollution
3. **Match SQL exactly** - Whitespace matters
4. **Test in isolation** - Each file should be independent
5. **Document your tests** - Comments help future debugging