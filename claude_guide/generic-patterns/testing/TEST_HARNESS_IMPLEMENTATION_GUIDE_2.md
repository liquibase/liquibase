# TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md - Comprehensive Test Harness Implementation Guide

## 🚨 CRITICAL: STOP BEFORE PROCEEDING

**YOU CANNOT RUN TEST HARNESS WITHOUT THESE STEPS:**
1. Build JAR: `cd liquibase-snowflake && mvn clean package -DskipTests`
2. Copy JAR: `cp target/*.jar ../liquibase-test-harness/lib/`
3. Change directory: `cd ../liquibase-test-harness`
4. Verify location: `pwd` must show `liquibase-test-harness`
5. ⚠️ **NEW: VERIFY ENVIRONMENT CLEANLINESS**: No external XSD files should exist in test harness directory

**IF YOU SKIP ANY STEP, TESTS WILL FAIL OR USE OLD CODE!**

## 🔥 RETROSPECTIVE LEARNINGS INTEGRATED

**CRITICAL DISCOVERY**: External XSD files can override JAR-embedded XSD definitions, causing mysterious validation failures even when implementation is correct.

**XSD Environment Rule**: The ONLY XSD should be in the liquibase-snowflake JAR file. Any external XSD files in the test harness directory will cause conflicts.

## 🔧 SYSTEMATIC DEBUGGING METHODOLOGY (PROVEN EFFECTIVE)

When facing complex validation errors, use this **layer-by-layer verification approach**:

### 1. Verify Implementation Layer by Layer
```java
// Test Change class in isolation
AlterSchemaChange change = new AlterSchemaChange();
change.setSchemaName("TEST");
change.setUnsetComment(true);
// Verify: change.getUnsetComment() returns true

// Test Statement class in isolation  
AlterSchemaStatement statement = new AlterSchemaStatement("TEST");
statement.setUnsetComment(true);
// Verify: statement generates correct object

// Test SQL generation in isolation
AlterSchemaGeneratorSnowflake generator = new AlterSchemaGeneratorSnowflake();
Sql[] sql = generator.generateSql(statement, database, null);
// Verify: SQL contains "UNSET COMMENT"
```

### 2. Test XSD Validation Separately
```xml
<!-- Create minimal test XML to isolate XSD issues -->
<changeSet id="debug-unset" author="debug">
    <snowflake:alterSchema schemaName="TEST" unsetComment="true"/>
</changeSet>
```

### 3. Environment Verification Checklist
- [ ] No external XSD files in test harness directory
- [ ] JAR rebuilt after any code changes
- [ ] Running from correct directory (test harness, not extension)
- [ ] Extension JAR properly copied to lib directory

**Key Insight**: Most "implementation bugs" are actually environment or XSD conflicts. Verify environment first, then systematically test each layer.

## 📌 QUICK REFERENCE (What I Always Forget)

```xml
<!-- REQUIRED STRUCTURE (in this exact order) -->
<include file=".../init.xml"/>                    <!-- 1. FIRST -->
<changeSet id="cleanup" runAlways="true">...</>   <!-- 2. Cleanup YOUR objects -->
<changeSet id="test-1">...</>                     <!-- 3. Your actual tests -->
<include file=".../cleanup.xml"/>                 <!-- 4. LAST -->
```

**Common Mistakes:**
- ❌ Forgetting `runAlways="true"` on cleanup
- ❌ Missing init.xml or cleanup.xml includes  
- ❌ Wrong order (cleanup must be BEFORE tests)
- ❌ Running in wrong directory (must be in test-harness, not snowflake)

**Run Command:**
```bash
cd liquibase-test-harness  # NOT liquibase-snowflake!
mvn test -Dtest=ChangeObjectTests -DchangeObjects=myTest -DdbName=snowflake
```

## 🚀 Quick Start Guide (Critical Information)

### ⚠️ STOP AND READ: Test Harness Pre-Flight Checklist

**BEFORE ATTEMPTING ANY TEST HARNESS WORK, VERIFY:**
```bash
# 1. Am I in the RIGHT directory?
pwd
# MUST show: liquibase-test-harness
# NOT: liquibase-snowflake

# 2. Does the test harness directory exist?
ls ../liquibase-test-harness
# If not found, STOP - test harness is not set up

# 3. Is the JAR up to date?
ls -la ../liquibase-test-harness/lib/liquibase-snowflake*.jar
# Check timestamp - if older than your last code change, YOU MUST REBUILD
```

### Test Harness File Locations

**Test XML Files**:
- **Directory**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/<database>/`
- **Example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/snowflake/createWarehouse.xml`

**Expected SQL Files**:
- **Directory**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSql/<database>/`
- **Example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSql/snowflake/createWarehouse.sql`

**Expected Snapshot Files**:
- **Directory**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSnapshot/<database>/`
- **Example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSnapshot/snowflake/createWarehouse.json`

**Initialization & Cleanup**:
- **Init**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/<database>/init.xml`
- **Cleanup**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/<database>/cleanup.xml`

### Step 1: MANDATORY - Build and Deploy JAR First
```bash
# ALWAYS start here after ANY code change
cd /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake
mvn clean package -DskipTests

# CRITICAL: Copy JAR (if you skip this, tests will use old code!)
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/

# VERIFY the copy worked
ls -la ../liquibase-test-harness/lib/liquibase-snowflake*.jar
```

### Step 2: Navigate to Test Harness (NOT OPTIONAL)
```bash
# YOU MUST BE IN THIS DIRECTORY TO RUN TESTS
cd /Users/kevinchappell/Documents/GitHub/liquibase-test-harness
pwd  # MUST show liquibase-test-harness, not liquibase-snowflake
```

### Step 3: Create Test Files IN THE RIGHT PLACE
```bash
# WRONG: /liquibase/liquibase-snowflake/src/main/resources/...
# RIGHT: /liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/

# Verify you're creating files in the right place:
pwd  # Must show liquibase-test-harness
ls src/main/resources/liquibase/harness/change/changelogs/snowflake/
```

### Step 4: Run Tests FROM TEST HARNESS DIRECTORY
```bash
# VERIFY LOCATION FIRST
pwd  # MUST show: liquibase-test-harness

# NOW run the test
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

### 🛑 COMMON FAILURES AND THEIR CAUSES:
1. **"Unknown change type"** → JAR not copied or old JAR
2. **"No tests found"** → Wrong directory or wrong test name
3. **"File not found"** → Created test files in wrong repo
4. **"Using old code"** → Didn't run `mvn clean package` or didn't copy JAR
5. **"Database connection failed"** → NOT YOUR PROBLEM - database is pre-configured

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
   - [ ] Extension JAR builds successfully: `mvn clean package -DskipTests`
   - [ ] JAR copied to test harness: `cp target/*.jar ../liquibase-test-harness/lib/`
   - [ ] Note: Use `mvn clean package` to ensure fresh build

4. **Database Access**:
   - [ ] Snowflake test database credentials configured in test harness
   - [ ] Test harness can connect to Snowflake
   - [ ] Note: Database is already configured in the liquibase-test-harness repo

5. **Test Harness Location**:
   - [ ] Test harness located at: `/Users/kevinchappell/Documents/GitHub/liquibase-test-harness`
   - [ ] NOT in the liquibase-snowflake directory
   - [ ] Navigate there before running tests

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

#### File Locations

**Test Changelog Files**:
- **Directory**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/snowflake/`
- **File naming**: `<changeType>.xml`, `<changeType>OrReplace.xml`, `<changeType>IfNotExists.xml`
- **Full path example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/snowflake/createWarehouse.xml`

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
    
    <!-- Test-specific cleanup changeset with runAlways="true" -->
    <changeSet id="cleanup-test-objects" author="test-harness" runAlways="true">
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

    <!-- Account-level cleanup (second to last) - ONLY if needed -->
    <changeSet id="cleanup-account-objects" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
        </preConditions>
        <sql>
            <!-- Clean up account-level objects like WAREHOUSE, DATABASE -->
            <!-- These are NOT covered by cleanup.xml schema reset -->
            DROP WAREHOUSE IF EXISTS TEST_WAREHOUSE CASCADE;
            DROP DATABASE IF EXISTS TEST_DATABASE CASCADE;
            <!-- Restore context after cleanup -->
            USE WAREHOUSE LTHDB_TEST_WH;
            USE DATABASE LTHDB;
        </sql>
    </changeSet>

    <!-- CRITICAL: ALWAYS include cleanup.xml as LAST changeset -->
    <include file="liquibase/harness/change/changelogs/snowflake/cleanup.xml"/>
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

**File Location**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSql/snowflake/<changeType>.sql`
**Full path example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSql/snowflake/createWarehouse.sql`

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

**File Location**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSnapshot/snowflake/<changeType>.json`
**Full path example**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSnapshot/snowflake/createWarehouse.json`

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

**CRITICAL**: After ANY code change in liquibase-snowflake:
1. Build with `mvn clean package -DskipTests`
2. Copy JAR to test harness
3. THEN run test harness tests

#### Step 2: Run Single Test

```bash
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

**Note**: The test name in `-DchangeObjects` must match your XML filename exactly (without .xml extension)

#### Step 3: Interpret Results

**✅ PASSING TEST LOOKS LIKE:**
```
[INFO] Running liquibase.harness.change.ChangeObjectTests
...
[INFO] Tests run: X, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**❌ FAILING TEST SCENARIOS:**

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

**⚠️ DO NOT MARK TASK COMPLETE UNLESS YOU SEE "BUILD SUCCESS"!**

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
- **Missing actual test SQL** (only shows cleanup)

**Debug Process**:
1. Copy GENERATED SQL to text editor
2. Show whitespace characters
3. Update expectedSql exactly
4. No trailing spaces or extra newlines

**If only cleanup SQL appears**:
- Check if changesets have been previously run
- Verify changesets don't have `runAlways="true"`
- Check DATABASECHANGELOG table for existing executions
- Consider adding unique context or labels to force re-execution

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

#### Test Harness Execution Order Problem

**Critical Issue**: The test harness runs commands in this order:
1. `updateSql` - Generates SQL by checking DATABASECHANGELOG
2. `update` - Actually executes the changes

This means cleanup that happens during `update` is too late - `updateSql` has already checked the old state!

**Solution**: Comprehensive cleanup strategy with proper ordering.

---

## Comprehensive Cleanup Strategy

### Required Test File Structure

Every test MUST follow this exact structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...>

    <!-- 1. ALWAYS start with init.xml -->
    <include file="liquibase/harness/change/changelogs/snowflake/init.xml"/>
    
    <!-- 2. Test-specific cleanup (what THIS test creates) -->
    <changeSet id="cleanup-test-objects" author="test-harness" runAlways="true">
        <sql>
            -- Drop all objects this test will create
            DROP TABLE IF EXISTS MY_TEST_TABLE CASCADE;
            DROP SCHEMA IF EXISTS MY_TEST_SCHEMA CASCADE;
        </sql>
    </changeSet>
    
    <!-- 3. Your test changesets -->
    <changeSet id="1" author="test-harness">
        <snowflake:createSchema schemaName="MY_TEST_SCHEMA"/>
    </changeSet>
    
    <!-- 4. Account-level cleanup (ONLY if creating WAREHOUSE/DATABASE) -->
    <changeSet id="cleanup-account-objects" author="test-harness" runAlways="true">
        <preConditions onFail="CONTINUE">
            <sqlCheck expectedResult="1">SELECT 1</sqlCheck>
        </preConditions>
        <sql>
            -- Clean up account-level objects
            DROP WAREHOUSE IF EXISTS MY_TEST_WAREHOUSE CASCADE;
            DROP DATABASE IF EXISTS MY_TEST_DATABASE CASCADE;
            -- CRITICAL: Restore context after cleanup
            USE WAREHOUSE LTHDB_TEST_WH;
            USE DATABASE LTHDB;
            USE SCHEMA TESTHARNESS;
        </sql>
    </changeSet>
    
    <!-- 5. ALWAYS end with cleanup.xml -->
    <include file="liquibase/harness/change/changelogs/snowflake/cleanup.xml"/>
</databaseChangeLog>
```

### Object Cleanup Responsibilities

#### Schema-Level Objects (handled by cleanup.xml)
These exist within the test schema and are cleaned by the schema reset:
- Tables, Views, Sequences
- Procedures, Functions
- Constraints, Indexes
- Any object created within the test schema

#### Database-Level Objects (require explicit cleanup)
These exist at database level and need manual cleanup:
- **SCHEMA** objects (created by createSchema)
- **ROLE** grants at database level
- Any object scoped to the database

#### Account-Level Objects (require explicit cleanup)
These exist at account level and MUST be explicitly cleaned:
- **WAREHOUSE** objects (compute resources)
- **DATABASE** objects (if creating new databases)
- **ROLE** objects (security)
- **USER** objects (security)
- **RESOURCE MONITOR** objects (governance)
- **NETWORK POLICY** objects (security)

### Why This Structure Works

1. **init.xml** - Sets up basic test environment
2. **Test-specific cleanup** - Ensures clean state before test runs
3. **Test changesets** - Execute the actual test
4. **Account-level cleanup** - Removes objects outside schema scope
5. **cleanup.xml** - Complete schema reset including DATABASECHANGELOG

This ensures:
- `updateSql` sees a clean state
- Tests are repeatable
- No manual cleanup needed
- Account-level objects don't accumulate

---

## Integration Test Database Setup (Snowflake-Specific)

### Database Isolation Strategy
- **Integration Tests**: Use separate `LIQUIBASE_INTEGRATION_TEST` database
- **Test Harness**: Uses `LTHDB` database  
- **Purpose**: Prevent test collisions and enable parallel development

### Required Setup Steps

#### 1. Create Integration Database
```sql
CREATE DATABASE IF NOT EXISTS LIQUIBASE_INTEGRATION_TEST
```

#### 2. Configure Properties File
```properties
# /liquibase-integration-tests/src/test/resources/liquibase/sdk/test/local.properties
snowflake.url=jdbc:snowflake://SERVER/?db=LIQUIBASE_INTEGRATION_TEST&warehouse=COMPUTE_WH&schema=PUBLIC&role=ACCOUNTADMIN
snowflake.username=USERNAME
snowflake.password=PASSWORD
```

#### 3. Integration Test Cleanup Pattern
```java
// Use fully qualified names for cleanup operations
try (Statement stmt = connection.createStatement()) {
    stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_SCHEMA CASCADE");
    stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_MANAGED_SCHEMA CASCADE");
}
```

### Key Integration Test Learnings
- **Database Context**: DROP operations require fully qualified names when using separate database
- **Properties File**: Must exist before running integration tests or `MissingResourceException` occurs
- **Database Creation**: Integration database must be created before first test run
- **Cleanup Isolation**: Integration test cleanup doesn't affect test harness database state
- **Parallel Safety**: Different databases allow integration and test harness tests to run simultaneously

### Integration vs Test Harness Comparison

| Aspect | Integration Tests | Test Harness Tests |
|--------|------------------|-------------------|
| **Location** | `liquibase-integration-tests/` | `liquibase-test-harness/` |
| **Database** | `LIQUIBASE_INTEGRATION_TEST` | `LTHDB` |
| **Purpose** | Test Java code with real DB | Test SQL generation & DB state |
| **Framework** | JUnit/TestNG | Spock/Groovy |
| **Execution** | `mvn test` in integration project | `mvn test -DchangeObjects=X` |
| **Isolation** | Per-test cleanup | Complete schema reset |

---

## Efficiency Patterns for Subsequent Change Types

### Performance Metrics (Based on Experience)
- **First Change Type**: 2-3 hours (pattern establishment, tool setup, learning)
- **Subsequent Change Types**: 30 minutes (pattern application)
- **Efficiency Gain**: 6x improvement after workflow establishment
- **Quality Maintained**: 100% requirements coverage and comprehensive test suites

### Integration Test Strategy (Refined)
- **Focus**: 1-2 key scenarios that validate real database behavior
- **Avoid**: Comprehensive coverage in integration tests (test harness handles this)
- **Examples**: 
  - IF EXISTS with non-existent entities
  - Edge cases hard to unit test
  - Database-specific error handling
  - Real constraint validation

### Test Harness File Review Checklist
Before creating new test harness files:
- [ ] Check if file already exists and is 90% correct
- [ ] Verify cleanup.xml include at end
- [ ] Verify expected SQL includes cleanup section  
- [ ] Verify snapshot file exists and is appropriate
- [ ] **Time Saver**: Enhance existing files rather than create from scratch

### Expected SQL Maintenance Pattern
When cleanup.xml changes, update expected SQL files by copying the cleanup section:
```sql
-- Add at end of expected SQL files
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE
CREATE SCHEMA TESTHARNESS
USE SCHEMA TESTHARNESS
GRANT ALL PRIVILEGES ON SCHEMA TESTHARNESS TO ROLE LIQUIBASE_TEST_HARNESS_ROLE
CREATE TABLE DATABASECHANGELOG (...)
CREATE TABLE DATABASECHANGELOGLOCK (...)
INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE)
```

### Knowledge Transfer Patterns
- **Requirements verification**: Direct comparison against detailed requirements
- **Unit test validation**: Look for comprehensive coverage (8-12 tests typical)
- **Integration test focus**: 1 key scenario that validates real database behavior
- **Test harness enhancement**: Review existing files, apply standard fixes
- **Problem recognition**: Common issues (imports, cleanup, expected SQL) quickly identified

---

### Common Mistakes to Avoid

❌ **DON'T** forget cleanup.xml at the end
❌ **DON'T** forget to restore context after dropping WAREHOUSE/DATABASE
❌ **DON'T** put test changesets before cleanup
❌ **DON'T** forget `runAlways="true"` on cleanup changesets
❌ **DON'T** forget `IF EXISTS` in cleanup SQL

✅ **DO** follow the 5-step structure exactly
✅ **DO** clean up ALL objects your test creates
✅ **DO** use CASCADE for dependent objects
✅ **DO** restore context after account-level cleanup
✅ **DO** test your cleanup by running the test twice
1. **Manual cleanup before test**: Run a cleanup script or init.xml separately
2. **Move cleanup to END of test**: Each test cleans up for the NEXT test
3. **Use unique object names**: Avoid conflicts between test runs

**Recommended Pattern**:
```xml
<!-- Test changesets FIRST -->
<changeSet id="1" author="test-harness">
    <snowflake:createSchema schemaName="TEST_SCHEMA"/>
</changeSet>

<!-- Test-specific cleanup -->
<changeSet id="cleanup" author="test-harness" runAlways="true">
    <sql>DROP SCHEMA IF EXISTS TEST_SCHEMA CASCADE;</sql>
</changeSet>

<!-- Global cleanup at END -->
<include file="liquibase/harness/change/changelogs/snowflake/cleanup.xml"/>
```

#### When Snowflake Gets Into Bad State

1. **Check current objects**:
```sql
SHOW SCHEMAS IN DATABASE <test_db>;
SHOW TABLES IN SCHEMA TESTHARNESS;
```

2. **Manual cleanup**:
```sql
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE;
USE DATABASE LTHDB;
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

## File Organization Guide

### Where Files Live (Critical to Understand!)

**Implementation Files** (liquibase-snowflake repo):
```
liquibase-snowflake/
├── src/main/java/liquibase/
│   ├── change/snowflake/         # Change classes
│   ├── statement/snowflake/      # Statement classes  
│   └── sqlgenerator/core/snowflake/  # SQL generators
├── src/main/resources/
│   ├── META-INF/services/        # Service registration
│   └── liquibase.snowflake.xsd   # XSD schema
└── src/test/java/                # Unit tests
```

**Test Harness Files** (liquibase-test-harness repo):
```
liquibase-test-harness/
├── lib/                          # Extension JARs go here!
└── src/main/resources/liquibase/harness/change/
    ├── changelogs/snowflake/     # Test XML files
    │   ├── <changeType>.xml
    │   ├── init.xml              # DO NOT MODIFY
    │   └── cleanup.xml           # DO NOT MODIFY
    ├── expectedSql/snowflake/    # Expected SQL output
    │   └── <changeType>.sql
    └── expectedSnapshot/snowflake/  # Expected database state
        └── <changeType>.json
```

### Key Points:
1. **Two separate repositories** - implementation vs test harness
2. **JAR must be copied** from one to the other after building
3. **Test files** created in test harness repo, not implementation repo
4. **Database already configured** in test harness - just run tests

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