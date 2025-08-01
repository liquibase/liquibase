# TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md - Simplified Test Harness Guide with Schema Isolation

## 🚨 CRITICAL: SETUP BEFORE PROCEEDING

**YOU CANNOT RUN TEST HARNESS WITHOUT THESE STEPS:**
1. Build and install JAR: `cd liquibase-snowflake && mvn clean install -DskipTests`
2. Change directory: `cd ../liquibase-test-harness`
3. Verify location: `pwd` must show `liquibase-test-harness`

**⚠️ CRITICAL:** Use `mvn install` not `mvn package`! The test harness loads the extension via Maven dependencies, not file copying. The `install` command puts the JAR in your local Maven repository where the test harness can find it.

**WHY THIS MATTERS:**
- The test harness POM has: `<dependency><groupId>org.liquibase</groupId><artifactId>liquibase-snowflake</artifactId><version>0-SNAPSHOT</version></dependency>`
- This dependency loads from your local Maven repository (~/.m2/repository/), NOT from file paths
- `mvn package` only builds the JAR, but doesn't install it where Maven can find it
- `mvn install` builds AND installs to local repository
- Copying JAR files manually was incorrect and caused mysterious test failures
5. Ensure lifecycle hooks enabled in `harness-config.yml`:
   ```yaml
   lifecycleHooks:
     enabled: true
   ```

**IF YOU SKIP ANY STEP, TESTS WILL FAIL OR USE OLD CODE!**

## 🎉 Major Simplifications with Schema Isolation

### What's New?
1. **Automatic Schema Isolation**: Each test runs in its own `TEST_<TESTNAME>` schema
2. **No More init.xml/cleanup.xml Management**: Schema isolation handles cleanup
3. **Simpler Test Files**: Focus on your actual test cases
4. **Test-Level Init Scripts**: Optional per-test setup
5. **Predictable Schema Names**: Makes debugging easier

### What This Means for You:
- ✅ No more worrying about test pollution
- ✅ Tests can run in parallel without conflicts
- ✅ Failed tests leave isolated schemas for debugging
- ✅ Simpler test file structure
- ✅ Less boilerplate code

## 📌 Quick Reference - New Simplified Structure

```xml
<!-- NEW STRUCTURE - Much simpler! -->
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/snowflake
                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- Just your test changesets - no includes needed! -->
    <changeSet id="1" author="test-harness">
        <snowflake:createTable tableName="TEST_TABLE">
            <column name="id" type="INT"/>
        </snowflake:createTable>
    </changeSet>

    <changeSet id="2" author="test-harness">
        <snowflake:alterTable tableName="TEST_TABLE">
            <addColumn>
                <column name="name" type="VARCHAR(100)"/>
            </addColumn>
        </snowflake:alterTable>
    </changeSet>

</databaseChangeLog>
```

**That's it! No init.xml, no cleanup.xml, no cleanup changesets!**

## 🚀 Quick Start Guide

### Step 1: Enable Schema Isolation

Check `src/test/resources/harness-config.yml`:
```yaml
lifecycleHooks:
  enabled: true

databasesUnderTest:
  - name: snowflake
    useSchemaIsolation: true  # Enable for cloud databases
    # ... other config
```

### Step 2: Create Your Test File

**Location**: `src/main/resources/liquibase/harness/change/changelogs/snowflake/<changeType>.xml`

**Simplified Structure**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog ...namespace declarations...>

    <!-- That's it - just your test changesets! -->
    <changeSet id="test-basic" author="test-harness">
        <snowflake:yourChange requiredAttribute="value"/>
    </changeSet>

    <changeSet id="test-with-options" author="test-harness">
        <snowflake:yourChange requiredAttribute="value"
                             optionalAttribute="value"/>
    </changeSet>

</databaseChangeLog>
```

### Step 3: Create Expected SQL

**IMPORTANT**: Expected SQL must use the isolated schema name!

**Location**: `src/main/resources/liquibase/harness/change/expectedSql/snowflake/<changeType>.sql`

```sql
-- For test "createTable", schema will be TEST_CREATETABLE
CREATE TABLE LTHDB.TEST_CREATETABLE.TEST_TABLE (id INT NOT NULL)
ALTER TABLE LTHDB.TEST_CREATETABLE.TEST_TABLE ADD COLUMN name VARCHAR(100)
```

### Step 4: Create Expected Snapshot

**Location**: `src/main/resources/liquibase/harness/change/expectedSnapshot/snowflake/<changeType>.json`

```json
{
  "tables": [
    {
      "table": {
        "name": "TEST_TABLE",
        "schema": "TEST_CREATETABLE",  // Note: isolated schema name
        "columns": [
          {
            "column": {
              "name": "ID",
              "type": "NUMBER(38,0)"
            }
          },
          {
            "column": {
              "name": "NAME", 
              "type": "VARCHAR(100)"
            }
          }
        ]
      }
    }
  ]
}
```

### Step 5: Run Your Test

```bash
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

## 🔧 Optional: Test-Level Init Scripts

For tests that need special setup, create an init script:

**Location**: `src/test/resources/harness/changeObjects/snowflake/<testName>.init.sql`

```sql
-- This runs before your test in the isolated schema
CREATE OR REPLACE TABLE DATABASECHANGELOG (...);
CREATE OR REPLACE TABLE DATABASECHANGELOGLOCK (...);
-- Any other test-specific setup
```

## 📊 Schema Isolation Details

### How It Works:

1. **Before Test**: 
   - Creates schema `TEST_<TESTNAME>` (e.g., `TEST_CREATETABLE`)
   - Switches context to the new schema
   - Runs test-level init script if present

2. **During Test**:
   - All operations happen in the isolated schema
   - No interference with other tests
   - Clean, predictable environment

3. **After Test**:
   - Switches back to original schema
   - Drops the test schema
   - No manual cleanup needed

### Schema Naming:
- Test name: `createTable.xml` → Schema: `TEST_CREATETABLE`
- Test name: `alterTableAddColumn.xml` → Schema: `TEST_ALTERTABLEADDCOLUMN`
- Special characters are replaced with underscores

## 🎯 Common Scenarios

### Scenario 1: Basic Change Type Test

```xml
<!-- createWarehouse.xml -->
<databaseChangeLog ...>
    <changeSet id="basic" author="test-harness">
        <snowflake:createWarehouse warehouseName="TEST_WH"/>
    </changeSet>
    
    <changeSet id="with-size" author="test-harness">
        <snowflake:createWarehouse warehouseName="TEST_WH_LARGE"
                                  size="LARGE"/>
    </changeSet>
</databaseChangeLog>
```

### Scenario 2: Testing Mutually Exclusive Options

Create separate files:
```
createWarehouse.xml              # Basic options
createOrReplaceWarehouse.xml     # OR REPLACE variation
createWarehouseIfNotExists.xml   # IF NOT EXISTS variation
```

### Scenario 3: Complex Multi-Step Test

```xml
<!-- alterTableScenarios.xml -->
<databaseChangeLog ...>
    <!-- Create base table -->
    <changeSet id="setup" author="test-harness">
        <createTable tableName="TEST_TABLE">
            <column name="id" type="INT"/>
        </createTable>
    </changeSet>
    
    <!-- Test various alterations -->
    <changeSet id="add-column" author="test-harness">
        <addColumn tableName="TEST_TABLE">
            <column name="name" type="VARCHAR(100)"/>
        </addColumn>
    </changeSet>
    
    <changeSet id="add-constraint" author="test-harness">
        <addPrimaryKey tableName="TEST_TABLE" 
                      columnNames="id"
                      constraintName="PK_TEST"/>
    </changeSet>
</databaseChangeLog>
```

## ⚠️ Important Considerations

### 1. Expected SQL Schema Names
**CRITICAL**: Your expected SQL must use the isolated schema name!

❌ **Wrong**:
```sql
CREATE TABLE LTHDB.TESTHARNESS.MY_TABLE (...)
```

✅ **Correct**:
```sql
CREATE TABLE LTHDB.TEST_CREATETABLE.MY_TABLE (...)
```

### 2. Account-Level Objects
Warehouses, databases, roles, etc. still need cleanup in the test file:

```xml
<changeSet id="cleanup-warehouse" author="test-harness" runAlways="true">
    <sql>
        DROP WAREHOUSE IF EXISTS TEST_WH CASCADE;
        USE WAREHOUSE LTHDB_TEST_WH;  -- Restore context
    </sql>
</changeSet>
```

### 3. Snapshot Schema References
Expected snapshots must reference the isolated schema:

```json
{
  "tables": [{
    "table": {
      "name": "MY_TABLE",
      "schema": "TEST_CREATETABLE"  // NOT "TESTHARNESS"
    }
  }]
}
```

## 🛠️ Troubleshooting

### Issue: "Schema 'TEST_CREATETABLE' does not exist"
**Cause**: Schema isolation not enabled
**Fix**: Ensure `useSchemaIsolation: true` in harness-config.yml

### Issue: "Expected SQL doesn't match"
**Cause**: Using wrong schema name in expected SQL
**Fix**: Update to use TEST_<TESTNAME> schema

### Issue: "Object already exists"
**Cause**: Previous test run didn't clean up (shouldn't happen with isolation)
**Fix**: Manually drop the test schema and re-run

### Issue: Test hangs after completion
**Cause**: Connection state issues with schema switching
**Fix**: Already resolved in current implementation

## 📈 Migration from Old Pattern

### Old Pattern (Complex):
- Include init.xml
- Add cleanup changesets with runAlways="true"
- Include cleanup.xml
- Worry about state pollution
- Complex ordering requirements

### New Pattern (Simple):
- Just write your test changesets
- Update expected SQL to use TEST_<TESTNAME>
- Update snapshot to use TEST_<TESTNAME>
- Run test

### Migration Steps:
1. Remove `<include file=".../init.xml"/>`
2. Remove cleanup changesets
3. Remove `<include file=".../cleanup.xml"/>`
4. Update expected SQL schema references
5. Update snapshot schema references
6. Run test

## 🎉 Benefits Summary

1. **Simplicity**: No more init/cleanup management
2. **Isolation**: Each test runs in its own schema
3. **Debugging**: Failed tests leave isolated schemas to inspect
4. **Parallel Execution**: Tests don't interfere with each other
5. **Predictability**: Schema names are deterministic
6. **Less Code**: Focus on actual test logic

## 📋 Quick Checklist

Before running tests:
- [ ] JAR built and copied to test harness lib/
- [ ] In liquibase-test-harness directory
- [ ] Schema isolation enabled in config
- [ ] Test file created (just changesets!)
- [ ] Expected SQL uses TEST_<TESTNAME> schema
- [ ] Expected snapshot uses TEST_<TESTNAME> schema

That's it! Much simpler than before. The schema isolation feature handles all the complexity of cleanup and state management for you.