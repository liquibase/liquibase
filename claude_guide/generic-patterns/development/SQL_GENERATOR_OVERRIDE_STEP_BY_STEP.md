# SQL Generator Override Step-by-Step Guide

## When to Use This Guide
Use this guide when:
- You need to change SQL syntax for an existing Liquibase change type
- The change type works correctly but generates wrong SQL for your database
- Example: Snowflake column operations have different syntax than standard SQL

## Pre-Flight Checklist
- [ ] Research exact SQL syntax your database requires
- [ ] Verify the change type exists in core Liquibase
- [ ] Check if a generator override already exists (`find . -name "*<ChangeType>*Generator*.java"`)
- [ ] Have test database connection ready

## Step 1: Discovery - Check What Already Exists

```bash
# Check for existing generator
find . -name "*AddColumn*Generator*.java"
grep -r "AddColumnGenerator" src/main/java/

# Check service registration
grep -r "AddColumnGenerator" src/main/resources/META-INF/services/

# Check what SQL the default generator produces
# Create a simple test changeset and run updateSQL
```

## Step 2: Research Database-Specific SQL Syntax

Example for common column operations:

| Operation | Standard SQL | Snowflake SQL |
|-----------|--------------|---------------|
| Add Column | `ALTER TABLE t ADD c TYPE` | `ALTER TABLE t ADD COLUMN c TYPE` |
| Drop Column | `ALTER TABLE t DROP c` | `ALTER TABLE t DROP COLUMN c` |
| Rename Column | `ALTER TABLE t RENAME c TO n` | `ALTER TABLE t RENAME COLUMN c TO n` |
| Modify Type | `ALTER TABLE t ALTER c TYPE` | `ALTER TABLE t ALTER COLUMN c SET DATA TYPE TYPE` |

## Step 3: Create the SQL Generator Override

### File Location
```
src/main/java/liquibase/sqlgenerator/core/<ChangeType>Generator<Database>.java
```

### Template
```java
package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.<ChangeType>Statement;

public class <ChangeType>GeneratorSnowflake extends <ChangeType>Generator {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5;  // Higher than default
    }
    
    @Override
    public boolean supports(<ChangeType>Statement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(<ChangeType>Statement statement, Database database, 
                            SqlGeneratorChain sqlGeneratorChain) {
        // Build your SQL here
        String sql = buildSql(statement, database);
        
        // Return with affected objects for proper tracking
        return new Sql[]{
            new UnparsedSql(sql, getAffectedTable(statement))
        };
    }
    
    private String buildSql(<ChangeType>Statement statement, Database database) {
        // Your database-specific SQL generation
        return "ALTER TABLE " + database.escapeTableName(...) + " ...";
    }
}
```

## Step 4: Register in Service Loader

### File: `src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`
```
liquibase.sqlgenerator.core.<ChangeType>GeneratorSnowflake
```

## Step 5: Create Unit Tests

### File: `src/test/java/liquibase/sqlgenerator/core/<ChangeType>GeneratorSnowflakeTest.java`
```java
@Test
public void testGeneratesSql() {
    <ChangeType>Statement statement = new <ChangeType>Statement(...);
    SnowflakeDatabase database = new SnowflakeDatabase();
    
    Sql[] sqls = generator.generateSql(statement, database, null);
    
    assertEquals(1, sqls.length);
    assertEquals("YOUR EXPECTED SQL", sqls[0].toSql());
}

@Test
public void testSupportsSnowflakeOnly() {
    assertTrue(generator.supports(statement, new SnowflakeDatabase()));
    assertFalse(generator.supports(statement, new PostgresDatabase()));
}

@Test
public void testPriority() {
    assertTrue(generator.getPriority() > PRIORITY_DATABASE);
}
```

## Step 6: Build and Install to Maven

```bash
# CRITICAL: Test harness loads from Maven, not lib/
cd liquibase-snowflake
mvn clean install -DskipTests  # Install first
mvn test -Dtest=<ChangeType>GeneratorSnowflakeTest  # Then test
```

## Step 7: Create Test Harness Files

### 7.1 Changelog: `test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/<changeType>.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    
    <changeSet id="1-<changeType>" author="testharness">
        <!-- Setup: Create test table -->
        <createTable tableName="test_table">
            <column name="id" type="int">
                <constraints primaryKey="true"/>
            </column>
        </createTable>
        
        <!-- Test: Your change type -->
        <<changeType> .../>
        
        <!-- NO CLEANUP HERE! -->
    </changeSet>
</databaseChangeLog>
```

### 7.2 Expected SQL: `test-harness/src/main/resources/liquibase/harness/change/expectedSql/snowflake/<changeType>.sql`
```sql
-- ONLY the changeset SQL, NO init.xml SQL!
CREATE TABLE test_table (id INT PRIMARY KEY)
YOUR EXPECTED SQL HERE
```

### 7.3 Expected Snapshot: `test-harness/src/main/resources/liquibase/harness/change/expectedSnapshot/<changeType>.json`
```json
{
  "snapshot": {
    "objects": {
      "liquibase.structure.core.Table": [{
        "table": {
          "name": "test_table"
          // Expected state after change
        }
      }]
    }
  }
}
```

## Step 8: Run Test Harness

```bash
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

## Step 9: Debug Common Issues

### Issue: "Database is up to date"
**Solution**: Change the changeset ID in your XML (e.g., "1-addColumn" → "1-addColumn-v2")

### Issue: Generator not being used
**Solution**: 
1. Verify `mvn clean install` was run in extension project
2. Check service registration file
3. Verify package name matches exactly

### Issue: Expected SQL includes init SQL
**Solution**: Remove all init.xml SQL from your expected SQL file

### Issue: Snapshot fails to find expected objects
**Solution**: Remove any cleanup changesets from your test XML

## Step 10: Document Your Implementation

Create a brief note about what SQL syntax your generator handles:
```java
/**
 * Generates Snowflake-specific SQL for <changeType>.
 * 
 * Snowflake syntax: ALTER TABLE ... ADD COLUMN ... (not just ADD)
 */
```

## Quick Reference for Column Change Types

| Change Type | Likely Needs Override? | Snowflake Syntax |
|-------------|------------------------|------------------|
| addColumn | Yes | ADD COLUMN |
| dropColumn | Yes | DROP COLUMN |
| renameColumn | Yes | RENAME COLUMN old TO new |
| modifyDataType | Yes | ALTER COLUMN c SET DATA TYPE |
| addPrimaryKey | Maybe | ADD PRIMARY KEY |
| dropPrimaryKey | Maybe | DROP PRIMARY KEY |
| addForeignKeyConstraint | Maybe | Check syntax |
| dropForeignKeyConstraint | Maybe | Check syntax |
| addUniqueConstraint | Maybe | Check syntax |
| dropUniqueConstraint | Maybe | Check syntax |

## Final Checklist
- [ ] Generator class created with correct priority
- [ ] Service registration added
- [ ] Unit tests pass
- [ ] Maven install completed
- [ ] Test harness files created (3 files)
- [ ] Test harness passes
- [ ] No cleanup in test XML file
- [ ] Expected SQL has only changeset SQL