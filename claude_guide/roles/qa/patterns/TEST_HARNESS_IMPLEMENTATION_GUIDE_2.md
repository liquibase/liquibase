# TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md - Comprehensive Test Harness Implementation Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Understanding Test Harness vs Unit Tests](#understanding-test-harness-vs-unit-tests)
3. [Pre-Implementation Planning](#pre-implementation-planning)
4. [Step-by-Step Implementation](#step-by-step-implementation)
5. [Troubleshooting Guide](#troubleshooting-guide)
6. [Common Patterns and Best Practices](#common-patterns-and-best-practices)
7. [Real Examples](#real-examples)

---

## Prerequisites

### Required Before Starting Test Harness

1. **Completed Implementation**:
   - [ ] Change class implemented and tested
   - [ ] Statement class implemented and tested
   - [ ] SQL Generator implemented and tested
   - [ ] Service registration completed
   - [ ] XSD schema created and tested
   - [ ] All unit/integration tests passing in `liquibase-snowflake`

2. **Requirements Document**:
   - [ ] Detailed requirements exist in `claude_guide/project/requirements/detailed_requirements/<changeType>_requirements.md`
   - [ ] Mutual exclusivity rules documented
   - [ ] SQL syntax variations identified
   - [ ] Test scenarios planned

3. **Build Artifacts**:
   - [ ] Extension JAR builds successfully: `mvn package -DskipTests`
   - [ ] JAR copied to test harness: `cp target/*.jar ../liquibase-test-harness/lib/`

4. **Database Access**:
   - [ ] Snowflake test database credentials configured
   - [ ] Test harness can connect to Snowflake

---

## Understanding Test Harness vs Unit Tests

### Test Harness Tests ARE:
- **Database integration tests** that execute real SQL
- **End-to-end validation** from XML changelog to database objects
- **Comparison tests** between expected and actual SQL/snapshots
- **Stateful tests** that depend on database state

### Test Harness Tests ARE NOT:
- Unit tests of Java code
- Mock-based tests
- Tests that run without database
- Part of the liquibase-snowflake build

### Key Differences:

| Aspect | Unit/Integration Tests | Test Harness Tests |
|--------|----------------------|-------------------|
| Project | liquibase-snowflake | liquibase-test-harness |
| Database | No real connection | Real Snowflake connection |
| Speed | Fast (seconds) | Slower (database I/O) |
| Purpose | Test Java code | Test database behavior |
| Failure Mode | Compilation/logic errors | SQL execution/comparison errors |

---

## Pre-Implementation Planning

### Step 1: Analyze Mutual Exclusivity

Based on your requirements document, identify mutually exclusive options:

```markdown
## Example from createSchema_requirements.md

### Mutually Exclusive Combinations
1. `orReplace` and `ifNotExists` - Cannot be used together
2. `transient` and `dataRetentionTimeInDays > 0` - Transient schemas have 0 retention
```

**Decision**: Create separate test files for each mutually exclusive combination:
- `createSchema.xml` - Basic features, transient, managed access
- `createOrReplaceSchema.xml` - OR REPLACE variations
- `createSchemaIfNotExists.xml` - IF NOT EXISTS variations

### Step 2: Plan Test Coverage

Create a test matrix:

| Feature | Test File | Changeset ID | Description |
|---------|-----------|--------------|-------------|
| Basic | createSchema.xml | 1 | Minimal required attributes |
| Transient | createSchema.xml | 2 | transient="true" |
| Managed Access | createSchema.xml | 3 | managedAccess="true" |
| Full Options | createSchema.xml | 4 | All compatible options |
| OR REPLACE | createOrReplaceSchema.xml | 1 | orReplace="true" |
| IF NOT EXISTS | createSchemaIfNotExists.xml | 1 | ifNotExists="true" |

### Step 3: Identify Database Objects

Determine what objects your change creates for the snapshot:

- **Schema operations**: Create `schemas` array
- **Table operations**: Create `tables` array  
- **Database operations**: May not appear in snapshot (account-level)
- **Warehouse operations**: Create `warehouses` array

---

## Step-by-Step Implementation

### Phase 1: Create Changelog Files

#### Location
```
liquibase-test-harness/
└── src/main/resources/liquibase/harness/change/changelogs/snowflake/
    ├── <changeType>.xml
    ├── <changeType>OrReplace.xml     (if needed)
    └── <changeType>IfNotExists.xml   (if needed)
```

#### Required Structure

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

    <!-- CRITICAL: ALWAYS include init.xml first -->
    <include file="liquibase/harness/change/changelogs/snowflake/init.xml"/>
    
    <!-- CRITICAL: Cleanup changeset with runAlways="true" -->
    <changeSet id="cleanup" author="test-harness" runAlways="true">
        <sql>
            <!-- Drop ALL objects this test will create -->
            <!-- MUST use IF EXISTS -->
            DROP <OBJECT> IF EXISTS TEST_OBJECT_1 CASCADE;
            DROP <OBJECT> IF EXISTS TEST_OBJECT_2 CASCADE;
        </sql>
    </changeSet>

    <!-- Test changesets - NO runAlways -->
    <changeSet id="1" author="test-harness">
        <snowflake:<changeType> <requiredAttribute>="TEST_OBJECT_1"/>
    </changeSet>

    <changeSet id="2" author="test-harness">
        <snowflake:<changeType> <requiredAttribute>="TEST_OBJECT_2"
                               <optionalAttribute>="value"/>
    </changeSet>
</databaseChangeLog>
```

#### Critical Rules

1. **init.xml Include**:
   - MUST be first include
   - Resets database state
   - Creates DATABASECHANGELOG tables

2. **Cleanup Changeset**:
   - MUST have `runAlways="true"`
   - MUST use `IF EXISTS`
   - MUST list EVERY object created
   - MUST use CASCADE for dependent objects

3. **Test Changesets**:
   - NEVER use `runAlways="true"`
   - Use descriptive IDs
   - Test one concept per changeset
   - Add XML comments for clarity

### Phase 2: Generate Expected SQL

#### Step 1: Run Test to Capture SQL

```bash
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

The test will fail, but you'll see:
```
FAIL! Expected sql doesn't match generated sql!
EXPECTED SQL:
[empty or wrong]
GENERATED SQL:
[copy this entire block]
```

#### Step 2: Create Expected SQL File

Location: `src/main/resources/liquibase/harness/change/expectedSql/snowflake/<changeType>.sql`

**CRITICAL FORMAT RULES**:
1. NO semicolons at end of lines
2. NO extra blank lines
3. MUST include ALL SQL from init.xml
4. MUST include cleanup SQL
5. MUST match generated SQL exactly

#### Expected SQL Structure

```sql
-- Always starts with these lines from init.xml
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
-- Cleanup SQL from your cleanup changeset
DROP <OBJECT> IF EXISTS TEST_OBJECT_1 CASCADE
DROP <OBJECT> IF EXISTS TEST_OBJECT_2 CASCADE
-- Actual test SQL
CREATE <OBJECT> TEST_OBJECT_1
CREATE <OBJECT> TEST_OBJECT_2 WITH <OPTION> value
```

### Phase 3: Create Expected Snapshot

#### Location
`src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/<changeType>.json`

#### Structure Based on Object Type

**For Schemas**:
```json
{
  "schemas": [
    {
      "schema": {
        "name": "TEST_SCHEMA_1"
      }
    },
    {
      "schema": {
        "name": "TEST_SCHEMA_2",
        "remarks": "Comment becomes remarks"
      }
    }
  ]
}
```

**For Tables**:
```json
{
  "tables": [
    {
      "table": {
        "name": "TEST_TABLE",
        "schema": "TESTHARNESS",
        "columns": [
          {
            "column": {
              "name": "ID",
              "type": "NUMBER(38,0)"
            }
          }
        ]
      }
    }
  ]
}
```

**For Warehouses**:
```json
{
  "warehouses": [
    {
      "warehouse": {
        "name": "TEST_WAREHOUSE",
        "size": "XSMALL"
      }
    }
  ]
}
```

#### Snapshot Rules

1. **Object names**: ALWAYS UPPERCASE (Snowflake default)
2. **Comments**: Appear as `"remarks"` in snapshot
3. **Order**: Must match creation order
4. **Attributes**: Only include what Snowflake actually captures
5. **Schema context**: Objects created in TESTHARNESS schema

### Phase 4: Test and Debug

#### Step 1: Ensure JAR is Updated

```bash
cd liquibase-snowflake
mvn clean package -DskipTests
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/
```

#### Step 2: Run Single Test

```bash
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

#### Step 3: Interpret Results

**SQL Mismatch**:
```
FAIL! Expected sql doesn't match generated sql!
```
- Copy GENERATED SQL exactly (including whitespace)
- Update expectedSql file
- Look for extra/missing spaces, newlines

**Snapshot Mismatch**:
```
Expected: "remarks" = "Test comment"
     but: was null
```
- Check if attribute is actually captured
- Verify JSON structure
- Check case sensitivity

**Execution Error**:
```
Migration Failed: SQL State: 42710
Object 'TEST_OBJECT' already exists
```
- Cleanup didn't work
- Check IF EXISTS clause
- Manually clean database if needed

---

## Troubleshooting Guide

### Common Issues and Solutions

#### 1. "Unknown change type 'snowflake:<changeType>'"

**Cause**: Extension not loaded or namespace issue

**Solutions**:
- Verify JAR in `lib/` directory
- Check service registration files
- Verify namespace in XSD

#### 2. "Object already exists"

**Cause**: Cleanup changeset not working

**Checklist**:
- [ ] Cleanup has `runAlways="true"`
- [ ] Using `IF EXISTS` in cleanup
- [ ] All objects listed in cleanup
- [ ] CASCADE used for dependencies

#### 3. "Expected sql doesn't match"

**Common Differences**:
- Extra/missing spaces
- Newline differences
- Parameter order
- Missing init.xml SQL

**Debug Process**:
1. Copy GENERATED SQL to text editor
2. Show whitespace characters
3. Update expectedSql exactly
4. No trailing spaces or extra newlines

#### 4. "Snapshot comparison failed"

**Common Issues**:
- Wrong case (should be UPPERCASE)
- Missing/extra attributes
- Wrong JSON structure
- Objects in wrong array

#### 5. "No tests found"

**Cause**: File naming or location issue

**Verify**:
- File in correct directory
- Named exactly `<changeType>.xml`
- No typos in changeType

### Database State Issues

#### When Snowflake Gets Into Bad State

1. **Check current objects**:
```sql
SHOW SCHEMAS IN DATABASE <test_db>;
SHOW TABLES IN SCHEMA TESTHARNESS;
```

2. **Manual cleanup**:
```sql
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE;
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE;
-- Drop any test objects
DROP SCHEMA IF EXISTS TEST_% CASCADE;
```

3. **Reset DATABASECHANGELOG**:
```sql
DELETE FROM DATABASECHANGELOG 
WHERE FILENAME LIKE '%<changeType>%';
```

---

## Common Patterns and Best Practices

### Pattern 1: Testing Mutual Exclusivity

**Approach**: Separate files for incompatible options

```
createSchema.xml                 # Basic + compatible options
createOrReplaceSchema.xml        # OR REPLACE tests
createSchemaIfNotExists.xml      # IF NOT EXISTS tests
```

**Why**: Prevents validation errors and clearly separates concerns

### Pattern 2: Progressive Complexity

**Approach**: Start simple, add complexity

```xml
<!-- Changeset 1: Minimal -->
<changeSet id="1" author="test-harness">
    <snowflake:createSchema schemaName="TEST_BASIC"/>
</changeSet>

<!-- Changeset 2: Add one feature -->
<changeSet id="2" author="test-harness">
    <snowflake:createSchema schemaName="TEST_TRANSIENT"
                           transient="true"/>
</changeSet>

<!-- Changeset 3: Multiple features -->
<changeSet id="3" author="test-harness">
    <snowflake:createSchema schemaName="TEST_FULL"
                           transient="true"
                           managedAccess="true"
                           comment="Full featured"/>
</changeSet>
```

### Pattern 3: Boolean Testing

**Approach**: Explicitly test both true and false

```xml
<!-- Important for SQL generation differences -->
<changeSet id="bool-true" author="test-harness">
    <snowflake:createSchema schemaName="TEST_BOOL_TRUE"
                           transient="true"/>
</changeSet>

<changeSet id="bool-false" author="test-harness">
    <snowflake:createSchema schemaName="TEST_BOOL_FALSE"
                           transient="false"/>
</changeSet>
```

### Pattern 4: Edge Cases

**Test These Scenarios**:
- Maximum length names
- Special characters (if supported)
- Null/empty values
- Boundary values (0, max retention days)

### Best Practices

1. **Always Clear State**: Cleanup changeset is not optional
2. **One Concept Per Test**: Makes debugging easier
3. **Document Tests**: XML comments explain what you're testing
4. **Match Production Usage**: Test realistic scenarios
5. **Version Control**: Commit working tests immediately

---

## Real Examples

### Example 1: CreateSchema Implementation

**Files Created**:
1. `createSchema.xml` - Tests basic, transient, managed, retention
2. `createOrReplaceSchema.xml` - Tests OR REPLACE option
3. `createSchemaIfNotExists.xml` - Tests IF NOT EXISTS option

**Key Lessons**:
- Transient schemas appear as regular schemas in snapshot
- Comments become "remarks" in snapshot  
- OR REPLACE and IF NOT EXISTS are mutually exclusive

### Example 2: DropSchema Implementation

**Files Created**:
1. `dropSchema.xml` - Tests basic drop, CASCADE, RESTRICT, IF EXISTS

**Key Lessons**:
- CASCADE and RESTRICT are mutually exclusive
- Generator prioritizes CASCADE over RESTRICT
- IF EXISTS prevents errors on missing objects

### Integration with Requirements

Always reference the requirements document:

```xml
<!-- Based on requirements in detailed_requirements/createSchema_requirements.md -->
<!-- Testing mutual exclusivity rule: orReplace + ifNotExists -->
<!-- This should be in a separate file per the requirements -->
```

---

## Summary Checklist

Before considering test harness complete:

### Implementation
- [ ] Created changelog file(s) based on mutual exclusivity rules
- [ ] Included init.xml as first include
- [ ] Added cleanup changeset with runAlways="true"
- [ ] Created test changesets without runAlways
- [ ] Tested all required attributes
- [ ] Tested all optional attributes
- [ ] Tested mutual exclusivity in separate files

### Expected Files
- [ ] Created expectedSql with exact formatting
- [ ] Included ALL SQL from init.xml
- [ ] Created expectedSnapshot with correct structure
- [ ] Used UPPERCASE for object names
- [ ] Matched actual Snowflake behavior

### Testing
- [ ] Built and deployed latest JAR
- [ ] All tests pass locally
- [ ] No manual database cleanup needed
- [ ] Tests are repeatable

### Documentation
- [ ] Added comments in test XML
- [ ] Referenced requirements document
- [ ] Documented any special behavior

---

## Key Success Factors

1. **State Management is Critical**: The cleanup changeset prevents test pollution
2. **Exact Matching Required**: SQL comparison is character-by-character
3. **Understand Snowflake Behavior**: Not all attributes appear in snapshots
4. **Test Isolation**: Each file must be runnable independently
5. **Requirements Drive Tests**: Always refer back to the requirements document

Remember: Test harness tests validate that your implementation works correctly with a real Snowflake database. They are the final proof that your change type works end-to-end.