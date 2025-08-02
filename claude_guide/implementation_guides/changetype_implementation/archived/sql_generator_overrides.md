# SQL Generator Override Implementation Guide
## AI-Optimized Sequential Execution Protocol

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: SQL_OVERRIDE_IMPLEMENTATION
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition through database-specific SQL research"
  - "Complete SQL test statements through comprehensive testing"
  - "Unit tests complete string comparison through exact SQL validation"
  - "Integration tests ALL generated SQL through harness verification"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/sql_generator_overrides.md"
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - master_process_loop.md: "Master process for all implementations"
  - changetype_patterns.md: "New and existing changetype patterns"
  - test_harness_guide.md: "Testing with harness protocols"
  - requirements_creation.md: "Detailed requirements specification"

RELATED_GUIDES:
  - "../snapshot_diff_implementation/error_patterns_guide.md": "Systematic debugging"
  - "../snapshot_diff_implementation/main_guide.md": "5-layer debugging framework"
```

## When to Use This Guide

```yaml
USE_THIS_GUIDE_WHEN:
  - "Need to change SQL syntax for existing Liquibase change type"
  - "Change type works correctly but generates wrong SQL for your database"
  - "Database has different SQL syntax than standard SQL"
  EXAMPLES:
    - "Snowflake column operations need COLUMN keyword"
    - "Database case sensitivity differences"
    - "Vendor-specific SQL syntax requirements"
    
DO_NOT_USE_WHEN:
  - "Creating entirely new change type (use changetype_patterns.md)"
  - "Adding new attributes to existing type (use changetype_patterns.md)"
  - "Working with snapshot/diff capabilities (use snapshot_diff_implementation/)"
```

## Sequential Blocking Implementation Steps

### STEP 1: Pre-Flight Validation - **COMPLETE SYNTAX DEFINITION**
```yaml
STEP_ID: 1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

#### BLOCKING_VALIDATION_1.1: Database Syntax Research Complete
```yaml
REQUIREMENT: "Exact SQL syntax documented for target database"
VALIDATION_CRITERIA:
  - "Official database documentation referenced"
  - "SQL syntax differences documented"
  - "Complete SQL examples provided"
FAILURE_ACTION: "STOP - Complete syntax research before implementation"
```

**Pre-Flight Checklist:**
- [ ] **MANDATORY**: Research exact SQL syntax your database requires
- [ ] **MANDATORY**: Verify the change type exists in core Liquibase  
- [ ] **MANDATORY**: Check if generator override already exists
- [ ] **MANDATORY**: Have test database connection ready
- [ ] **MANDATORY**: Document complete SQL syntax differences

**Research Validation Commands:**
```bash
# Check for existing generator - REQUIRED
find . -name "*<ChangeType>*Generator*.java"
grep -r "<ChangeType>Generator" src/main/java/

# Check service registration - REQUIRED
grep -r "<ChangeType>Generator" src/main/resources/META-INF/services/

# CRITICAL: Check what SQL the default generator produces
# Create a simple test changeset and run updateSQL to see current output
```

#### BLOCKING_VALIDATION_1.2: SQL Syntax Differences Documented
```yaml
REQUIREMENT: "Complete comparison table of SQL syntax differences"
TEMPLATE: |
  | Operation | Standard SQL | [Database] SQL | Required Change |
  |-----------|--------------|----------------|-----------------|
  | [Operation] | [Standard] | [Database-specific] | [What needs to change] |
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

**Example for Snowflake Column Operations:**
| Operation | Standard SQL | Snowflake SQL | Required Change |
|-----------|--------------|---------------|-----------------|
| Add Column | `ALTER TABLE t ADD c TYPE` | `ALTER TABLE t ADD COLUMN c TYPE` | Add COLUMN keyword |
| Drop Column | `ALTER TABLE t DROP c` | `ALTER TABLE t DROP COLUMN c` | Add COLUMN keyword |
| Rename Column | `ALTER TABLE t RENAME c TO n` | `ALTER TABLE t RENAME COLUMN c TO n` | Add COLUMN keyword |
| Modify Type | `ALTER TABLE t ALTER c TYPE` | `ALTER TABLE t ALTER COLUMN c SET DATA TYPE TYPE` | Add COLUMN, SET DATA TYPE |

### STEP 2: Discovery Phase - **PREVENT GOALPOST CHANGING**
```yaml
STEP_ID: 2.0
STATUS: BLOCKED
PREREQUISITES: [1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Prevents changing requirements to fit existing code"
```

#### BLOCKING_VALIDATION_2.1: Existing Implementation Assessment
```yaml
REQUIREMENT: "Complete assessment of existing generator implementation"
VALIDATION_CRITERIA:
  - "Existing generator functionality documented"
  - "Current SQL output captured and analyzed"
  - "Gaps identified against requirements"
FAILURE_ACTION: "STOP - Complete discovery before implementation"
```

**Discovery Commands:**
```bash
# MANDATORY: Check for existing generator
find . -name "*AddColumn*Generator*.java"
find . -name "*[ChangeType]*Generator*.java"

# MANDATORY: Search for any related implementations
grep -r "[ChangeType]Generator" src/main/java/

# MANDATORY: Check service registration status
grep -r "[ChangeType]Generator" src/main/resources/META-INF/services/

# CRITICAL: Test current SQL generation
# Create test changeset and run: liquibase updateSQL
```

### STEP 3: SQL Generator Override Implementation - **COMPLETE SQL GENERATION**
```yaml
STEP_ID: 3.0
STATUS: BLOCKED
PREREQUISITES: [2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

#### BLOCKING_VALIDATION_3.1: Generator Class Implementation Complete
```yaml
REQUIREMENT: "SQL generator class implements all required methods with complete SQL"
VALIDATION_CRITERIA:
  - "Priority method returns correct value"
  - "Supports method correctly identifies target database"
  - "GenerateSQL method produces complete, valid SQL"
FAILURE_ACTION: "STOP - Complete generator implementation"
```

**File Location Pattern:**
```
src/main/java/liquibase/sqlgenerator/core/[ChangeType]Generator[Database].java
```

**Complete Implementation Template:**
```java
package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.[Database]Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.[ChangeType]Statement;
import liquibase.structure.core.Table;

/**
 * Generates [Database]-specific SQL for [ChangeType] operations.
 * 
 * [Database] syntax: [Document specific syntax requirements]
 * Standard syntax: [Document standard syntax]
 * Key differences: [Document what changes and why]
 */
public class [ChangeType]Generator[Database] extends [ChangeType]Generator {
    
    @Override
    public int getPriority() {
        // Higher than default to override standard generator
        return PRIORITY_DATABASE + 5;
    }
    
    @Override
    public boolean supports([ChangeType]Statement statement, Database database) {
        return database instanceof [Database]Database;
    }
    
    @Override
    public Sql[] generateSql([ChangeType]Statement statement, Database database, 
                            SqlGeneratorChain sqlGeneratorChain) {
        
        // CRITICAL: Build complete SQL statement
        String sql = buildCompleteSql(statement, database);
        
        // CRITICAL: Validate SQL is complete and syntactically correct
        validateCompleteSql(sql, statement);
        
        // Return with affected objects for proper tracking
        return new Sql[]{
            new UnparsedSql(sql, getAffectedTable(statement))
        };
    }
    
    /**
     * Builds complete SQL statement with all required elements.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements
     */
    private String buildCompleteSql([ChangeType]Statement statement, Database database) {
        StringBuilder sql = new StringBuilder();
        
        // MANDATORY: Include all required SQL elements
        sql.append("ALTER TABLE ");
        sql.append(database.escapeTableName(statement.getCatalogName(), 
                                           statement.getSchemaName(), 
                                           statement.getTableName()));
        
        // CRITICAL: Add database-specific syntax
        sql.append(" ADD COLUMN ");  // Example for Snowflake
        sql.append(database.escapeColumnName(statement.getCatalogName(),
                                           statement.getSchemaName(),
                                           statement.getTableName(),
                                           statement.getColumnName()));
        sql.append(" ");
        sql.append(statement.getColumnDataType());
        
        // MANDATORY: Handle all optional elements
        if (statement.getConstraints() != null) {
            sql.append(" ").append(statement.getConstraints().toString());
        }
        
        return sql.toString();
    }
    
    /**
     * Validates that generated SQL is complete and correct.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements
     */
    private void validateCompleteSql(String sql, [ChangeType]Statement statement) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalStateException("Generated SQL cannot be null or empty");
        }
        
        // CRITICAL: Validate SQL contains all required elements
        if (!sql.toUpperCase().contains("ALTER TABLE")) {
            throw new IllegalStateException("SQL missing ALTER TABLE clause: " + sql);
        }
        
        if (!sql.toUpperCase().contains("ADD COLUMN")) {
            throw new IllegalStateException("SQL missing ADD COLUMN clause: " + sql);
        }
        
        // Add more validation based on requirements
    }
    
    private Table getAffectedTable([ChangeType]Statement statement) {
        return new Table(statement.getCatalogName(), 
                        statement.getSchemaName(), 
                        statement.getTableName());
    }
}
```

### STEP 4: Service Registration - **COMPONENT DISCOVERY**
```yaml
STEP_ID: 4.0
STATUS: BLOCKED
PREREQUISITES: [3.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition requires proper registration"
```

#### BLOCKING_VALIDATION_4.1: Service Registration Complete
```yaml
REQUIREMENT: "Generator registered for Liquibase service discovery"
VALIDATION_CRITERIA:
  - "Service file exists with correct content"
  - "Class name matches exactly"
  - "JAR includes service file after build"
FAILURE_ACTION: "STOP - Fix service registration"
```

**Service Registration File:**
`src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`

**Content (append to existing file):**
```
liquibase.sqlgenerator.core.[ChangeType]Generator[Database]
```

**Validation Commands:**
```bash
# MANDATORY: Verify service file exists
ls -la src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator

# MANDATORY: Check content includes your class
cat src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator | grep [ChangeType]Generator[Database]

# MANDATORY: After build, verify JAR includes service file
mvn clean compile
jar -tf target/liquibase-*.jar | grep META-INF/services/liquibase.sqlgenerator.SqlGenerator
```

### STEP 5: Unit Testing - **COMPLETE SQL STRING COMPARISON**
```yaml
STEP_ID: 5.0
STATUS: BLOCKED
PREREQUISITES: [4.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

#### BLOCKING_VALIDATION_5.1: Unit Tests with Complete SQL Validation
```yaml
REQUIREMENT: "All unit tests validate complete SQL strings exactly"
VALIDATION_CRITERIA:
  - "Tests compare entire SQL statement, not partial matches"
  - "All property combinations tested"
  - "Edge cases and boundary conditions covered"
FAILURE_ACTION: "STOP - Add complete SQL string comparison to all tests"
```

**Unit Test File:**
`src/test/java/liquibase/sqlgenerator/core/[ChangeType]Generator[Database]Test.java`

**Complete Unit Test Template:**
```java
package liquibase.sqlgenerator.core;

import liquibase.database.core.[Database]Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.[ChangeType]Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for [ChangeType]Generator[Database].
 * ADDRESSES_CORE_ISSUE: Unit tests must compare complete SQL strings.
 */
public class [ChangeType]Generator[Database]Test {
    
    private [ChangeType]Generator[Database] generator;
    private [Database]Database database;
    
    @BeforeEach
    public void setUp() {
        generator = new [ChangeType]Generator[Database]();
        database = new [Database]Database();
    }
    
    /**
     * CRITICAL: Test complete SQL generation with all required elements.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    @Test
    public void testGeneratesCompleteSql() {
        // Arrange
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setTableName("test_table");
        statement.setColumnName("test_column");
        statement.setColumnDataType("VARCHAR(100)");
        
        // Act
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Assert - MANDATORY: Complete SQL string comparison
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        String expectedSQL = "ALTER TABLE test_table ADD COLUMN test_column VARCHAR(100)";
        
        // CRITICAL: Exact string comparison required
        assertEquals(expectedSQL, actualSQL, 
            "Generated SQL must match expected SQL exactly. " +
            "Expected: '" + expectedSQL + "', " +
            "Actual: '" + actualSQL + "'");
        
        // MANDATORY: Verify SQL is complete and valid
        assertTrue(actualSQL.contains("ALTER TABLE"), 
            "SQL must contain ALTER TABLE clause");
        assertTrue(actualSQL.contains("ADD COLUMN"), 
            "SQL must contain database-specific ADD COLUMN clause");
        assertTrue(actualSQL.contains("test_table"), 
            "SQL must contain table name");
        assertTrue(actualSQL.contains("test_column"), 
            "SQL must contain column name");
        assertTrue(actualSQL.contains("VARCHAR(100)"), 
            "SQL must contain complete data type");
    }
    
    /**
     * CRITICAL: Test all property combinations with complete SQL validation.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    @Test
    public void testAllPropertyCombinations() {
        // Test with constraints
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setTableName("test_table");
        statement.setColumnName("test_column");
        statement.setColumnDataType("INT");
        statement.setConstraints("NOT NULL PRIMARY KEY");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        String actualSQL = sqls[0].toSql();
        String expectedSQL = "ALTER TABLE test_table ADD COLUMN test_column INT NOT NULL PRIMARY KEY";
        
        // MANDATORY: Complete SQL string comparison
        assertEquals(expectedSQL, actualSQL,
            "SQL with constraints must match exactly");
    }
    
    /**
     * CRITICAL: Test edge cases and boundary conditions.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    @Test
    public void testEdgeCases() {
        // Test with schema and catalog
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setCatalogName("test_catalog");
        statement.setSchemaName("test_schema");
        statement.setTableName("test_table");
        statement.setColumnName("test_column");
        statement.setColumnDataType("VARCHAR(255)");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        String actualSQL = sqls[0].toSql();
        
        // MANDATORY: Verify complete SQL with all elements
        assertTrue(actualSQL.contains("test_catalog") || 
                  actualSQL.contains("test_schema") ||
                  actualSQL.contains("test_table"),
            "SQL must properly handle catalog/schema/table names");
        
        // CRITICAL: Exact pattern matching
        assertTrue(actualSQL.matches(".* ADD COLUMN .*"),
            "SQL must follow database-specific syntax pattern");
    }
    
    @Test
    public void testSupports[Database]Only() {
        [ChangeType]Statement statement = new [ChangeType]Statement();
        
        // MANDATORY: Supports target database
        assertTrue(generator.supports(statement, new [Database]Database()),
            "Generator must support [Database] database");
        
        // MANDATORY: Does not support other databases
        assertFalse(generator.supports(statement, new PostgresDatabase()),
            "Generator must not support other databases");
    }
    
    @Test
    public void testPriorityHigherThanDefault() {
        // MANDATORY: Priority must be higher than default
        assertTrue(generator.getPriority() > PRIORITY_DATABASE,
            "Generator priority must be higher than default to override");
    }
    
    /**
     * CRITICAL: Test SQL validation catches incomplete SQL.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    @Test
    public void testSqlValidationCatchesIncompleteSQL() {
        [ChangeType]Statement statement = new [ChangeType]Statement();
        // Intentionally incomplete statement
        statement.setTableName("test_table");
        // Missing column name - should fail validation
        
        // MANDATORY: Should throw exception for incomplete SQL
        assertThrows(IllegalStateException.class, () -> {
            generator.generateSql(statement, database, null);
        }, "Generator must validate SQL completeness");
    }
}
```

### STEP 6: Build and Installation Validation
```yaml
STEP_ID: 6.0
STATUS: BLOCKED
PREREQUISITES: [5.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition requires working build"
```

#### BLOCKING_VALIDATION_6.1: Clean Build Success
```yaml
REQUIREMENT: "Extension builds cleanly and installs to local repository"
VALIDATION_CRITERIA:
  - "mvn clean install completes without errors"
  - "All unit tests pass"
  - "JAR contains all required components"
FAILURE_ACTION: "STOP - Fix build issues before proceeding"
```

**Build Commands:**
```bash
# MANDATORY: Clean build with unit test validation
cd liquibase-[database]
mvn clean install -DskipTests=false

# MANDATORY: Verify JAR contents
jar -tf target/liquibase-*.jar | grep [ChangeType]Generator[Database]
jar -tf target/liquibase-*.jar | grep META-INF/services

# MANDATORY: Run specific unit tests
mvn test -Dtest=[ChangeType]Generator[Database]Test

# MANDATORY: Verify installation in local repository
ls ~/.m2/repository/org/liquibase/ext/liquibase-[database]/
```

### STEP 7: Test Harness Integration - **ALL GENERATED SQL TESTING**
```yaml
STEP_ID: 7.0
STATUS: BLOCKED
PREREQUISITES: [6.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_7.1: Test Harness Files Complete
```yaml
REQUIREMENT: "Test harness files cover ALL SQL generation scenarios"
VALIDATION_CRITERIA:
  - "Changelog includes all property combinations"
  - "Expected SQL matches complete generated SQL"
  - "Expected snapshot reflects final database state"
FAILURE_ACTION: "STOP - Create comprehensive test harness files"
```

**Test Harness File 1: Changelog**
`test-harness/src/main/resources/liquibase/harness/change/changelogs/[database]/[changeType].xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    
    <!-- MANDATORY: Setup changeset -->
    <changeSet id="setup-test-table" author="testharness">
        <createTable tableName="test_table">
            <column name="id" type="int">
                <constraints primaryKey="true"/>
            </column>
        </createTable>
    </changeSet>
    
    <!-- CRITICAL: Test basic functionality -->
    <changeSet id="test-basic-[changeType]" author="testharness">
        <[changeType] tableName="test_table" 
                     columnName="basic_column" 
                     columnDataType="VARCHAR(100)"/>
    </changeSet>
    
    <!-- CRITICAL: Test with all properties -->
    <changeSet id="test-complete-[changeType]" author="testharness">
        <[changeType] tableName="test_table" 
                     columnName="complete_column" 
                     columnDataType="INT">
            <constraints nullable="false"/>
        </[changeType]>
    </changeSet>
    
    <!-- MANDATORY: Test edge cases -->
    <changeSet id="test-edge-case-[changeType]" author="testharness">
        <[changeType] catalogName="test_catalog"
                     schemaName="test_schema"
                     tableName="test_table" 
                     columnName="edge_column" 
                     columnDataType="VARCHAR(255)"/>
    </changeSet>
    
    <!-- NO CLEANUP CHANGESETS HERE - Let harness handle cleanup -->
</databaseChangeLog>
```

**Test Harness File 2: Expected SQL**
`test-harness/src/main/resources/liquibase/harness/change/expectedSql/[database]/[changeType].sql`

```sql
-- CRITICAL: Only changeset SQL, NO init.xml SQL
-- ADDRESSES_CORE_ISSUE: Integration tests testing ALL generated SQL

-- Setup changeset SQL
CREATE TABLE test_table (id INT PRIMARY KEY);

-- Basic functionality test SQL
ALTER TABLE test_table ADD COLUMN basic_column VARCHAR(100);

-- Complete functionality test SQL  
ALTER TABLE test_table ADD COLUMN complete_column INT NOT NULL;

-- Edge case test SQL
ALTER TABLE test_catalog.test_schema.test_table ADD COLUMN edge_column VARCHAR(255);
```

**Test Harness File 3: Expected Snapshot**
`test-harness/src/main/resources/liquibase/harness/change/expectedSnapshot/[changeType].json`

```json
{
  "snapshot": {
    "objects": {
      "liquibase.structure.core.Table": [{
        "table": {
          "name": "test_table",
          "columns": [
            {
              "column": {
                "name": "id",
                "type": "INT"
              }
            },
            {
              "column": {
                "name": "basic_column", 
                "type": "VARCHAR(100)"
              }
            },
            {
              "column": {
                "name": "complete_column",
                "type": "INT"
              }
            },
            {
              "column": {
                "name": "edge_column",
                "type": "VARCHAR(255)"
              }
            }
          ]
        }
      }]
    }
  }
}
```

### STEP 8: Test Harness Execution - **INTEGRATION VALIDATION**
```yaml
STEP_ID: 8.0
STATUS: BLOCKED
PREREQUISITES: [7.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_8.1: Test Harness Passes
```yaml
REQUIREMENT: "Test harness executes successfully with all SQL verified"
VALIDATION_CRITERIA:
  - "All changesets execute without errors"
  - "Generated SQL matches expected SQL exactly"
  - "Database state matches expected snapshot"
FAILURE_ACTION: "STOP - Debug test harness failures systematically"
```

**Test Execution Commands:**
```bash
# MANDATORY: Ensure clean environment
cd liquibase-test-harness

# MANDATORY: Run test harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=[changeType] -DdbName=[database]

# CRITICAL: Verify all changesets executed
grep "Running Changeset" target/surefire-reports/*.xml | wc -l

# CRITICAL: Check for SQL execution errors
grep -i "error\|exception\|failed" target/surefire-reports/*.xml
```

### STEP 9: Systematic Debugging - **ERROR PATTERN APPLICATION**
```yaml
STEP_ID: 9.0
STATUS: CONDITIONAL
PREREQUISITES: [8.0_FAILED]
VALIDATION_MODE: SYSTEMATIC
ADDRESSES_CORE_ISSUE: "Prevents goalpost changing during debugging"
```

#### Common Issues and Systematic Solutions

**Issue 1: "Database is up to date"**
```yaml
ROOT_CAUSE: "DATABASECHANGELOG retains execution history"
SYSTEMATIC_SOLUTION:
  1. "Check changeset IDs are unique"
  2. "Add runAlways='true' to cleanup changesets if needed"
  3. "Clear DATABASECHANGELOG table if necessary"
CONSULT: "../snapshot_diff_implementation/error_patterns_guide.md - Pattern #2"
DO_NOT_CHANGE_REQUIREMENTS: "Fix execution, don't simplify tests"
```

**Issue 2: Generator not being used**
```yaml
ROOT_CAUSE: "Service registration or priority issues"
SYSTEMATIC_SOLUTION:
  1. "Verify mvn clean install completed in extension project"
  2. "Check service registration file exists and has correct content"
  3. "Verify package name matches exactly"
  4. "Check generator priority is higher than default"
CONSULT: "../snapshot_diff_implementation/error_patterns_guide.md - Pattern #1"
DO_NOT_CHANGE_REQUIREMENTS: "Fix registration, don't lower expectations"
```

**Issue 3: Expected SQL includes init SQL**
```yaml
ROOT_CAUSE: "Expected SQL file includes initialization SQL"
SYSTEMATIC_SOLUTION:
  1. "Remove all init.xml SQL from expected SQL file"
  2. "Only include changeset-specific SQL"
  3. "Verify SQL matches exactly what generator produces"
CONSULT: "../snapshot_diff_implementation/error_patterns_guide.md - Pattern #5"
DO_NOT_CHANGE_REQUIREMENTS: "Fix expected SQL, don't change generator"
```

**Issue 4: SQL syntax not complete**
```yaml
ROOT_CAUSE: "Generated SQL missing required elements"
SYSTEMATIC_SOLUTION:
  1. "Review requirements document for complete syntax"
  2. "Update generator to include all required SQL elements"  
  3. "Add complete SQL validation to unit tests"
ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
DO_NOT_CHANGE_REQUIREMENTS: "Fix generator, don't simplify requirements"
```

## Automated Workflow Script

```bash
#!/bin/bash
# scripts/sql-generator-workflow.sh - Complete workflow with validation
set -e

echo "=== SQL Generator Override Development Workflow ==="

# Validate inputs
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
    echo "Usage: $0 [database] [changetype] [operation]"
    echo "Example: $0 snowflake addColumn ADD_COLUMN"
    exit 1
fi

DATABASE=$1
CHANGETYPE=$2
OPERATION=$3

echo "1. Pre-flight validation..."
echo "   - Checking for existing generator..."
if find . -name "*${CHANGETYPE}*Generator*.java" | grep -q .; then
    echo "   WARNING: Existing generator found, review before proceeding"
fi

echo "2. Building extension for $DATABASE..."
cd liquibase-$DATABASE
mvn clean install -DskipTests=false

echo "3. Verifying JAR contents..."
if ! jar -tf target/liquibase-*.jar | grep -q "$CHANGETYPE"; then
    echo "   ERROR: Generator class not found in JAR!"
    exit 1
fi

if ! jar -tf target/liquibase-*.jar | grep -q "META-INF/services"; then
    echo "   ERROR: Service registration files not found in JAR!"
    exit 1
fi

echo "4. Running unit tests..."
mvn test -Dtest="*${CHANGETYPE}*Generator*Test"

echo "5. Installing to local repository..."
echo "   (Required for test harness dependency resolution)"

echo "6. Changing to test harness..."
cd ../liquibase-test-harness

echo "7. Running integration tests..."
mvn test -Dtest=ChangeObjectTests -DdbName=$DATABASE -DchangeObjects="$CHANGETYPE"

echo "8. Validation complete!"
echo "=== SQL Generator Override Workflow Complete ==="
```

## Quick Reference for Common Column Operations

```yaml
ADD_COLUMN:
  STANDARD_SQL: "ALTER TABLE t ADD c TYPE"
  SNOWFLAKE_SQL: "ALTER TABLE t ADD COLUMN c TYPE"
  ORACLE_SQL: "ALTER TABLE t ADD (c TYPE)"
  CHANGE_REQUIRED: "Add COLUMN keyword or parentheses"

DROP_COLUMN:
  STANDARD_SQL: "ALTER TABLE t DROP c"
  SNOWFLAKE_SQL: "ALTER TABLE t DROP COLUMN c"
  ORACLE_SQL: "ALTER TABLE t DROP COLUMN c"
  CHANGE_REQUIRED: "Add COLUMN keyword"

RENAME_COLUMN:
  STANDARD_SQL: "ALTER TABLE t RENAME c TO n"
  SNOWFLAKE_SQL: "ALTER TABLE t RENAME COLUMN c TO n"
  POSTGRES_SQL: "ALTER TABLE t RENAME COLUMN c TO n"
  CHANGE_REQUIRED: "Add COLUMN keyword"

MODIFY_DATA_TYPE:
  STANDARD_SQL: "ALTER TABLE t ALTER c TYPE"
  SNOWFLAKE_SQL: "ALTER TABLE t ALTER COLUMN c SET DATA TYPE TYPE"
  ORACLE_SQL: "ALTER TABLE t MODIFY (c TYPE)"
  CHANGE_REQUIRED: "Database-specific syntax variations"
```

## Cross-Reference Links
```yaml
RELATED_DOCUMENTS:
  MASTER_PROCESS: "master_process_loop.md - Overall development process"
  CHANGETYPE_PATTERNS: "changetype_patterns.md - For new change types"
  TEST_HARNESS: "test_harness_guide.md - Testing protocols"
  ERROR_DEBUGGING: "../snapshot_diff_implementation/error_patterns_guide.md"
  
NAVIGATION: "README.md - Complete navigation guide"
```