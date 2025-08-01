# Liquibase Extension Development - Common Error Patterns and Debugging Guide
## AI-Optimized with Core Issue Prevention

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: ERROR_REFERENCE
VALIDATION_MODE: DIAGNOSTIC
FAILURE_BEHAVIOR: SYSTEMATIC_DEBUG
PURPOSE: "Prevent Claude Code from skipping steps and changing goalposts"
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition"
  - "Complete SQL test statements"
  - "Unit tests complete string comparison" 
  - "Integration tests ALL generated SQL"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/snapshot_diff_implementation/error_patterns_guide.md"
COMPANION_DOCUMENTS:
  - main_guide.md: "Overview and systematic debugging framework"
  - ai_quickstart.md: "Sequential execution with validation checkpoints"
  - part4_testing_guide.md: "Testing strategies and harness limitations"
  
RELATED_CHANGETYPE_GUIDES:
  - "../changetype_implementation/master_process_loop.md": "Process debugging"
  - "../changetype_implementation/sql_generator_overrides.md": "SQL generation debugging"
```

## Overview
This guide consolidates field-tested error patterns and solutions discovered during Liquibase extension development. Use this as a reference when encountering issues to prevent goalpost changing and maintain systematic debugging approach.

---

## Quick Diagnosis Flowchart

```
Test Failure
├── Build/Compilation Error?
│   └── Check: Service Registration (Pattern #1) → CORE ISSUE: Complete syntax definition
├── Test Not Executing?
│   └── Check: Database State (Pattern #2) → CORE ISSUE: Integration tests ALL SQL
├── Objects Not Found?
│   └── Check: Scope & Creation (Pattern #3) → CORE ISSUE: Realistic success criteria
├── SQL/Database Errors?
│   └── Check: Case Sensitivity (Pattern #4) → CORE ISSUE: Complete syntax definition
├── JSON Comparison Failure?
│   └── Check: Format Issues (Pattern #5) → CORE ISSUE: Unit tests complete string comparison
├── Incomplete SQL Testing?
│   └── Check: String Comparison (Pattern #6) → CORE ISSUE: Unit tests complete string comparison
└── Missing SQL Statements?
    └── Check: Integration Coverage (Pattern #7) → CORE ISSUE: Integration tests ALL SQL
```

---

## Common Error Patterns

### Pattern #1: Service Registration Issues
```yaml
ERROR_CLASSIFICATION: "CRITICAL - Blocks all functionality"
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
PREVENTION: "Systematic validation before implementation"
```

**Error Messages:**
- "No snapshot generator found for type [ObjectType]"
- "Cannot find implementation of [Interface]"
- "No extension found for database [database]"

**Root Cause:** Liquibase cannot discover your components

**Diagnostic Steps:**
```bash
# 1. Check service files exist
ls -la src/main/resources/META-INF/services/

# 2. Verify service file contents
cat src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator

# 3. Check JAR contains service files
jar -tf target/liquibase-*.jar | grep META-INF/services

# 4. Verify class names match exactly
grep "class.*SnapshotGenerator" src/main/java/**/*.java

# 5. CRITICAL: Check DatabaseObject registration for snapshot scope
cat src/main/resources/META-INF/services/liquibase.structure.DatabaseObject
```

**Solution:**
1. Create missing service files
2. Ensure exact package.ClassName match
3. **CRITICAL**: Register DatabaseObject for snapshot scope inclusion
4. Rebuild with `mvn clean install`
5. **DO NOT** redefine requirements to work around missing registration

**Validation Checkpoint:**
```yaml
BEFORE_PROCEEDING:
  - "All 4 service files exist and contain correct class names"
  - "JAR contains all service files after build"
  - "DatabaseObject registered for snapshot scope"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition requires proper registration"
```

---

### Pattern #2: Database State Persistence
```yaml
ERROR_CLASSIFICATION: "HIGH - Prevents test re-execution"
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
PREVENTION: "Proper cleanup changeset design"
```

**Error Messages:**
- "liquibase.changelog.ChangeSet - ChangeSet [id] has already been executed"
- "Database is up to date, no changesets to execute"
- "Skipping changeset [id]"

**Root Cause:** DATABASECHANGELOG table retains execution history

**Diagnostic Steps:**
```sql
-- Check changelog history
SELECT * FROM DATABASECHANGELOG WHERE ID LIKE '%test%';

-- See what Liquibase thinks is executed  
SELECT ID, AUTHOR, FILENAME FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;

-- Check if ALL expected changesets executed
SELECT COUNT(*) FROM DATABASECHANGELOG WHERE FILENAME LIKE '%[testname]%';
```

**Solution:**
```xml
<!-- REQUIRED: Cleanup changeset with runAlways="true" -->
<changeSet id="cleanup-test-objects" author="test-harness" runAlways="true">
    <sql>
        DROP [OBJECT_TYPE] IF EXISTS TEST_[OBJECT] CASCADE;
    </sql>
</changeSet>

<changeSet id="create-test-object" author="test-harness">
    <!-- Your creation logic here -->
    <!-- MUST test complete SQL generation -->
</changeSet>

<!-- REQUIRED: Verify ALL SQL executed -->
<changeSet id="verify-all-sql-executed" author="test-harness">
    <sql>
        -- Verification that all expected objects exist
        SELECT COUNT(*) FROM [OBJECT_CATALOG] WHERE [NAME] LIKE 'TEST_%';
    </sql>
</changeSet>
```

**Validation Checkpoint:**
```yaml
BEFORE_PROCEEDING:
  - "Cleanup changeset with runAlways='true' exists"
  - "All test changesets have unique IDs"
  - "Verification changeset confirms ALL SQL executed"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

---

### Pattern #3: Objects Not in Snapshot
```yaml
ERROR_CLASSIFICATION: "CRITICAL - Framework limitation"
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
PREVENTION: "Scope validation before implementation (Step 6.0)"
```

**Error Messages:**
- "Expected: [ObjectType] objects but none found"
- "Snapshot does not contain [ObjectType]"  
- "AssertionError: expected [1] but found [0]"

**Root Cause:** Either objects not created OR not in snapshot scope

**Diagnostic Steps:**
```bash
# 1. Check if changeset executed
grep "Running Changeset.*create-test" test-output.log

# 2. Verify object exists in database
# For Snowflake:
SHOW WAREHOUSES LIKE 'TEST_%';

# For PostgreSQL:
SELECT * FROM information_schema.tables WHERE table_name LIKE 'test_%';

# 3. CRITICAL: Check snapshot scope
grep -i "includedType" test-output.log | grep -i [ObjectType]

# 4. Verify DatabaseObject service registration
jar -tf target/*.jar | grep META-INF/services/liquibase.structure.DatabaseObject
```

**Solution - Systematic Approach:**

**If object exists in DB but not in snapshot:**
1. **FIRST CHECK**: Verify `META-INF/services/liquibase.structure.DatabaseObject` registration
2. Ensure custom DatabaseObject class is registered for service discovery
3. Rebuild JAR and verify service file is included
4. **CRITICAL**: If registration correct, accept as framework limitation
5. **DO NOT** redefine requirements - adjust success criteria to changeset execution
6. Add manual database verification as success measure

**If object doesn't exist in DB:**
1. Check changeset SQL syntax against requirements document
2. Verify database permissions
3. Look for SQL execution errors
4. **DO NOT** simplify SQL to make tests pass

**Validation Checkpoint:**
```yaml
REALISTIC_SUCCESS_CRITERIA_SET:
  IF_IN_SNAPSHOT_SCOPE:
    SUCCESS: "Full test harness pass with snapshot validation"
  IF_NOT_IN_SNAPSHOT_SCOPE:  
    SUCCESS: "Changesets execute + objects created in DB + manual verification"
    DOCUMENT: "Create test_harness_scope.md with limitations"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria - no goalpost changing"
```

---

### Pattern #4: Case Sensitivity Issues  
```yaml
ERROR_CLASSIFICATION: "MEDIUM - SQL syntax problem"
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
PREVENTION: "Comprehensive requirements documentation"
```

**Error Messages:**
- "Column 'name' not found"
- "SQLException: Invalid column name"
- "Unknown column '[column]' in result set"

**Root Cause:** Database returns columns in different case than expected

**Diagnostic Steps:**
```java
// Add debug logging to see actual column names
ResultSetMetaData metadata = rs.getMetaData();
for (int i = 1; i <= metadata.getColumnCount(); i++) {
    System.out.println("Column " + i + ": " + metadata.getColumnName(i));
}
```

**Solution:**
```java
// Handle multiple case variations - COMPLETE implementation
private String getColumnValue(ResultSet rs, String... possibleNames) {
    for (String name : possibleNames) {
        try {
            return rs.getString(name);
        } catch (SQLException e) {
            // Try next variation
        }
    }
    throw new DatabaseException("Column not found: " + Arrays.toString(possibleNames));
}

// Usage - test ALL possible variations
String name = getColumnValue(rs, "name", "NAME", "Name");
String size = getColumnValue(rs, "warehouse_size", "WAREHOUSE_SIZE", "WarehouseSize");
```

**Validation Checkpoint:**
```yaml
COMPLETE_CASE_HANDLING:
  - "All possible case variations documented in requirements"
  - "ResultSet parsing handles all case variations"
  - "Unit tests verify all case scenarios"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition includes case handling"
```

---

### Pattern #5: JSON Format Mismatches
```yaml
ERROR_CLASSIFICATION: "HIGH - Test framework integration"
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
PREVENTION: "Strict JSON format validation"
```

**Error Messages:**
- "JSONAssert.assertEquals() failed"
- "Expected: {...} but was: {...}"
- "Unexpected: database"

**Root Cause:** Test harness expects specific JSON structure

**Working Format:**
```json
{
  "snapshot": {
    "objects": {
      "liquibase.ext.snowflake.database.object.Warehouse": [
        {
          "warehouse": {
            "name": "TEST_WAREHOUSE",
            "warehouseSize": "SMALL",
            "autoSuspend": 600
            // ONLY configuration properties
            // NO state properties
            // NO database metadata
            // Format must support complete SQL string comparison
          }
        }
      ]
    }
  }
}
```

**Common Mistakes:**
```json
// ❌ BAD - Includes database metadata
{
  "snapshot": {
    "database": {  // Remove this entire section
      "productName": "Snowflake"
    },
    "objects": {...}
  }
}

// ❌ BAD - Uses IGNORE values  
{
  "warehouse": {
    "name": "TEST_WAREHOUSE",
    "state": "IGNORE"  // Remove IGNORE values - prevents string comparison
  }
}

// ❌ BAD - Missing properties for complete SQL
{
  "warehouse": {
    "name": "TEST_WAREHOUSE"
    // Missing optional properties that should be tested
    // This prevents complete SQL string comparison
  }
}

// ❌ BAD - Wrong structure
{
  "warehouses": [  // Should be under snapshot.objects.[FullClassName]
    {...}
  ]
}
```

**Validation Checkpoint:**
```yaml
JSON_FORMAT_COMPLETE:
  - "No database metadata fields"
  - "No IGNORE values"
  - "All configuration properties included for complete SQL testing"
  - "Format supports complete SQL string comparison"
  ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
```

---

### Pattern #6: Incomplete SQL Testing ⚠️ NEW
```yaml
ERROR_CLASSIFICATION: "CRITICAL - Core requirement violation"
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
PREVENTION: "Explicit string comparison requirements in unit tests"
```

**Error Messages:**
- "Test passes but SQL incomplete"
- "Generated SQL missing required clauses"
- "Partial SQL validation allows incomplete implementation"

**Root Cause:** Unit tests not validating complete SQL string

**Diagnostic Steps:**
```java
// Add complete SQL string logging
@Test
public void testGenerateSQL() {
    String actualSQL = generator.generateSQL(statement, database);
    System.out.println("=== COMPLETE GENERATED SQL ===");
    System.out.println(actualSQL);
    System.out.println("=== END SQL ===");
    
    // REQUIRED: Complete string comparison
    String expectedSQL = "CREATE WAREHOUSE test_wh WITH SIZE = 'SMALL' AUTO_SUSPEND = 600";
    assertEquals("Complete SQL must match exactly", expectedSQL, actualSQL);
}
```

**Solution:**
```java
// REQUIRED: Complete SQL string comparison in ALL unit tests
@Test
public void testGenerateCompleteSQL() {
    // Test ALL property combinations
    CreateWarehouseStatement statement = new CreateWarehouseStatement();
    statement.setWarehouseName("TEST_WH");
    statement.setSize("SMALL");
    statement.setAutoSuspend(600);
    statement.setResourceConstraint("STANDARD_GEN_1");
    
    String actualSQL = generator.generateSQL(statement, database);
    
    // REQUIRED: Complete expected SQL with ALL properties
    String expectedSQL = "CREATE WAREHOUSE TEST_WH WITH " +
                        "SIZE = 'SMALL' " +
                        "AUTO_SUSPEND = 600 " +
                        "RESOURCE_CONSTRAINT = 'STANDARD_GEN_1'";
    
    // CRITICAL: Exact string comparison required
    assertEquals("SQL must include ALL properties in correct syntax", 
                expectedSQL, actualSQL);
    
    // REQUIRED: Test that SQL is complete and executable
    assertTrue("SQL must be complete and valid", 
              actualSQL.contains("CREATE WAREHOUSE"));
    assertTrue("SQL must include all required clauses",
              actualSQL.contains("WITH SIZE"));
}

// REQUIRED: Test matrix for ALL property combinations
@Test
public void testAllPropertyCombinations() {
    // Test every possible combination
    // This prevents incomplete SQL generation
}
```

**Validation Checkpoint:**
```yaml
COMPLETE_SQL_TESTING:
  - "All unit tests compare complete SQL strings"
  - "No partial string matching allowed"
  - "All property combinations tested"
  - "Generated SQL verified as complete and executable"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

---

### Pattern #7: Missing SQL Statements ⚠️ NEW
```yaml
ERROR_CLASSIFICATION: "CRITICAL - Integration testing gap"
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
PREVENTION: "Comprehensive integration test coverage"
```

**Error Messages:**
- "Integration test skips some SQL statements"
- "Some changesets never executed"
- "Database state doesn't match all property changes"

**Root Cause:** Integration tests not executing and verifying ALL generated SQL

**Diagnostic Steps:**
```bash
# Check ALL changesets executed
grep "Running Changeset" test-output.log | wc -l

# Verify expected changeset count
grep -c "changeSet id=" test-changeset.xml

# Check database state matches ALL properties
echo "DESCRIBE WAREHOUSE TEST_WH;" | database-cli
```

**Solution:**
```xml
<!-- REQUIRED: Test ALL SQL generation scenarios -->
<changeSet id="test-minimal-properties" author="test-harness">
    <snowflake:createWarehouse warehouseName="TEST_WH_MIN"/>
    <!-- Verify minimal SQL generation -->
</changeSet>

<changeSet id="test-all-properties" author="test-harness">
    <snowflake:createWarehouse warehouseName="TEST_WH_FULL"
                              size="LARGE"
                              autoSuspend="1800"
                              resourceConstraint="STANDARD_GEN_2"/>
    <!-- Verify complete SQL generation -->
</changeSet>

<changeSet id="verify-all-sql-executed" author="test-harness">
    <sql>
        -- REQUIRED: Verify ALL properties were applied
        SELECT COUNT(*) as warehouse_count 
        FROM INFORMATION_SCHEMA.WAREHOUSES 
        WHERE WAREHOUSE_NAME IN ('TEST_WH_MIN', 'TEST_WH_FULL');
        
        -- REQUIRED: Verify specific property values
        SELECT WAREHOUSE_SIZE, AUTO_SUSPEND, RESOURCE_CONSTRAINT
        FROM INFORMATION_SCHEMA.WAREHOUSES 
        WHERE WAREHOUSE_NAME = 'TEST_WH_FULL';
    </sql>
</changeSet>

<changeSet id="test-edge-cases" author="test-harness">
    <!-- REQUIRED: Test boundary conditions -->
    <snowflake:createWarehouse warehouseName="TEST_WH_EDGE"
                              autoSuspend="60"/>  <!-- Minimum value -->
</changeSet>
```

**Validation Checkpoint:**
```yaml
INTEGRATION_TEST_COMPLETENESS:
  - "All property combinations have integration tests"
  - "Database verification confirms ALL properties applied"
  - "Edge cases and boundary conditions tested"
  - "Every generated SQL statement verified in database"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

---

## Systematic Debugging Framework

### The 5-Layer Analysis Approach
**CRITICAL**: Apply layers in order. Do not skip layers or redefine requirements.

```yaml
DEBUGGING_PROTOCOL:
  RULE: "Fix current layer completely before proceeding to next"
  PREVENTION: "Prevents goalpost changing and step skipping"
  ADDRESSES_ALL_CORE_ISSUES: "Systematic validation of each component"
```

#### Layer 1: Code Compilation and Loading
```yaml
QUESTION: "Is the code being compiled and loaded correctly?"
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

```bash
# Check compilation
mvn compile

# Check JAR contents  
jar -tf target/liquibase-*.jar | grep [YourClass]

# CRITICAL: Check service registration
jar -tf target/liquibase-*.jar | grep META-INF/services

# Verify all expected services exist
ls target/liquibase-*.jar && jar -tf target/liquibase-*.jar | grep META-INF/services | sort
```

**Validation Before Layer 2:**
- JAR builds without errors
- All classes present in JAR  
- All 4 service files present in JAR
- Service file contents match class names exactly

#### Layer 2: Component Registration
```yaml
QUESTION: "Is Liquibase finding and registering your components?"
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

```bash
# Look for loading messages
mvn test -X | grep -i "loading\|found\|registered"

# Check for your specific classes
mvn test -X | grep -i [YourClass]

# CRITICAL: Check DatabaseObject registration for snapshot scope
mvn test -X | grep -i "DatabaseObject.*[YourObjectType]"
```

**Validation Before Layer 3:**
- Component loading messages appear in logs
- Priority methods being called
- DatabaseObject appears in registered types

#### Layer 3: Execution Flow  
```yaml
QUESTION: "Are your methods being called with correct parameters?"
ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
```

```java
// Add comprehensive debug logging
public class MySnapshotGenerator extends SnapshotGenerator {
    @Override
    public int getPriority(Class<?> objectType, Database database) {
        System.out.println("DEBUG: getPriority called for " + objectType.getName() + 
                          " with database " + database.getClass().getSimpleName());
        return PRIORITY_DATABASE;
    }
    
    @Override
    protected DatabaseObject snapshotObject(...) {
        System.out.println("DEBUG: snapshotObject called with SQL: " + sql);
        // CRITICAL: Log complete SQL being executed
        System.out.println("DEBUG: Complete SQL statement: " + completeSQL);
        // implementation
    }
}
```

**Validation Before Layer 4:**
- All implemented methods being called
- Method parameters are correct
- Complete SQL logged and verified

#### Layer 4: Data Creation
```yaml
QUESTION: "Is data being created correctly in the database?"
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

```sql
-- Manual verification queries
-- Snowflake:
SHOW WAREHOUSES;
SELECT * FROM INFORMATION_SCHEMA.WAREHOUSES WHERE WAREHOUSE_NAME LIKE 'TEST_%';

-- PostgreSQL:
\dt
SELECT * FROM pg_tables WHERE schemaname = 'public';

-- CRITICAL: Verify ALL properties were applied
SELECT [ALL_PROPERTY_COLUMNS] FROM [OBJECT_TABLE] WHERE [NAME] LIKE 'TEST_%';

-- Verify property values match expected
SELECT COUNT(*) FROM [OBJECT_TABLE] 
WHERE [NAME] = 'TEST_OBJECT' 
AND [PROPERTY1] = 'EXPECTED_VALUE1'
AND [PROPERTY2] = 'EXPECTED_VALUE2';
```

**Validation Before Layer 5:**
- All test objects exist in database
- All properties have correct values
- Database state matches ALL expected changes

#### Layer 5: Test Framework Integration
```yaml
QUESTION: "Is the test framework configured correctly?"
ADDRESSES_CORE_ISSUE: "Realistic success criteria"
```

Check:
- JSON format matches expected structure exactly
- Object types included in snapshot scope (or success criteria adjusted)
- File locations correct (test harness vs extension repo)
- **CRITICAL**: Success criteria realistic based on framework capabilities

**Final Validation:**
- Test framework configured correctly
- Success criteria documented and realistic
- No goalpost changing occurred during debugging

---

## Build Process Issues

### Issue: Changes Not Reflected in Tests
```yaml
SYMPTOM: "Code changes don't affect test behavior"
ROOT_CAUSE: "Test harness using stale JAR from local repository"
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

**Solution:** Automated build script with validation
```bash
#!/bin/bash
# scripts/snapshot-diff-debug-workflow.sh
set -e

echo "=== Liquibase Snapshot/Diff Debug Workflow ==="

# Validate inputs
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 [database] [testname]"
    exit 1
fi

DATABASE=$1
TESTNAME=$2

echo "1. Clean build..."
cd liquibase-$DATABASE
mvn clean compile

echo "2. Verify service files..."
ls -la src/main/resources/META-INF/services/
echo "Service files found: $(ls src/main/resources/META-INF/services/ | wc -l)"

echo "3. Build and install..."
mvn clean install -DskipTests

echo "4. Verify JAR contents..."
jar -tf target/liquibase-*.jar | grep META-INF/services | sort
echo "JAR service files: $(jar -tf target/liquibase-*.jar | grep META-INF/services | wc -l)"

echo "5. Running test harness..."
cd ../liquibase-test-harness
mvn test -Dtest=SnapshotObjectTests -DdbName=$DATABASE -DsnapshotObjects="$TESTNAME"

echo "=== Debug Workflow Complete ==="
```

### Issue: Dependency Resolution Failures
```yaml
SYMPTOM: "Could not resolve dependencies"
ROOT_CAUSE: "Extension not properly installed in local repository"
```

**Solution:**
```bash
# Ensure extension is in local repository
cd liquibase-[database]
mvn clean install -DskipTests

# Verify installation
ls ~/.m2/repository/org/liquibase/ext/liquibase-[database]/

# Check version matches
grep -A 1 -B 1 "version>" pom.xml
```

---

## Test Harness Specific Issues

### Understanding Scope Limitations ⚠️ CRITICAL
```yaml
KEY_LEARNING: "Test harness may not include custom objects in snapshot scope"
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
PREVENTION: "Scope validation (Step 6.0) before implementation"
```

**Verification:**
```bash
# Run with standard objects first
mvn test -Dtest=SnapshotObjectTests -DdbName=[database] -DsnapshotObjects=createTable

# Check what's included in scope
grep -i "includedType" test-output.log

# Look for your custom object type
grep -i "includedType.*[YourObjectType]" test-output.log
```

**Adjusted Success Criteria - NO GOALPOST CHANGING:**
```yaml
IF_CUSTOM_OBJECTS_IN_SCOPE:
  SUCCESS_CRITERIA: "Full test harness pass with snapshot validation"
  VALIDATION: "JSON comparison must pass"
  
IF_CUSTOM_OBJECTS_NOT_IN_SCOPE:
  SUCCESS_CRITERIA: 
    - "Changesets execute successfully"
    - "Objects created in database"
    - "Manual verification confirms ALL properties"
  VALIDATION: "Database state verification required"
  DOCUMENT: "Create test_harness_scope.md documenting limitation"
  
CRITICAL_RULE: "Do not redefine requirements to fit framework limitations"
```

---

## Prevention Strategies

### 1. Add Comprehensive Logging
```java
private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);

public void criticalMethod() {
    LOG.info("=== criticalMethod called with parameters: {} ===", parameters);
    try {
        // implementation
        LOG.info("criticalMethod completed successfully");
        // CRITICAL: Log complete SQL generated
        LOG.info("Generated complete SQL: {}", completeSQL);
    } catch (Exception e) {
        LOG.error("criticalMethod failed", e);
        throw e;
    }
}
```

### 2. Create Diagnostic Utilities
```java
public class DiagnosticUtils {
    public static void printResultSetMetadata(ResultSet rs) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        System.out.println("=== ResultSet Columns ===");
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            System.out.println(i + ": " + metadata.getColumnName(i) + 
                             " (" + metadata.getColumnTypeName(i) + ")");
        }
    }
    
    // CRITICAL: Validate complete SQL syntax
    public static void validateCompleteSQL(String sql, String objectType) {
        if (!sql.toUpperCase().contains("CREATE " + objectType.toUpperCase())) {
            throw new IllegalStateException("SQL missing CREATE clause: " + sql);
        }
        // Add more complete SQL validation
    }
}
```

### 3. Implement Defensive Coding with Complete Validation
```java
// Comprehensive case handling
private String safeGetString(ResultSet rs, String column) {
    String[] variations = {column, column.toUpperCase(), column.toLowerCase()};
    
    for (String variation : variations) {
        try {
            String value = rs.getString(variation);
            if (value != null && !"null".equalsIgnoreCase(value)) {
                return value;
            }
        } catch (SQLException e) {
            // Try next variation
        }
    }
    
    // CRITICAL: Don't ignore missing columns - they may be required
    LOG.warn("Column {} not found in any case variation", column);
    return null; // Only return null if truly optional
}

// CRITICAL: Validate complete property coverage
private void validateAllPropertiesSet(DatabaseObject object) {
    // Ensure all required properties are set
    // Ensure all optional properties handled
    // This prevents incomplete SQL generation
}
```

---

## Quick Reference Card

### When You See This Error... Try This First:

| Error | Core Issue | First Check | Quick Fix |
|-------|------------|-------------|-----------|
| "No generator found" | Complete syntax definition | Service files | Check META-INF/services |
| "Already executed" | Integration tests ALL SQL | Changelog history | Add runAlways="true" |
| "Objects not found" | Realistic success criteria | Database query | Manual verification + scope check |
| "Column not found" | Complete syntax definition | Case sensitivity | Try all case variations |
| "JSON mismatch" | Unit tests complete string comparison | Format structure | Remove metadata, add all properties |
| "Test passes but SQL incomplete" | Unit tests complete string comparison | String assertions | Add complete SQL validation |
| "Some SQL not executed" | Integration tests ALL SQL | Changeset coverage | Add verification changesets |

### Debug Commands Cheatsheet:

```bash
# Check complete service registration
jar -tf target/*.jar | grep META-INF/services | sort

# Check execution with complete logging
mvn test -X | grep -i [YourClass]

# Check database state completely
echo "SHOW [OBJECTS]; DESCRIBE [OBJECT] TEST_NAME;" | database-cli

# Clean rebuild with validation
mvn clean install -DskipTests && jar -tf target/*.jar | grep META-INF

# Full workflow with complete validation
./scripts/snapshot-diff-debug-workflow.sh [database] [test]

# Verify ALL properties in database
echo "SELECT * FROM [OBJECT_CATALOG] WHERE [NAME] LIKE 'TEST_%';" | database-cli
```

### Core Issue Prevention Checklist:

```yaml
BEFORE_IMPLEMENTATION:
  COMPLETE_SYNTAX_DEFINITION:
    - "Requirements document complete with all properties"
    - "All SQL syntax examples provided"
    - "Service registration documented"
    
  COMPLETE_SQL_TEST_STATEMENTS:
    - "All SQL examples complete and executable"
    - "Property combinations documented"
    - "Edge cases identified"
    
  UNIT_TESTS_COMPLETE_STRING_COMPARISON:
    - "Unit test strategy requires complete SQL string comparison"
    - "No partial matching allowed"
    - "All property combinations tested"
    
  INTEGRATION_TESTS_ALL_SQL:
    - "Integration test matrix covers all SQL generation scenarios"
    - "Database verification confirms ALL properties"
    - "Edge cases included in integration tests"
    
  REALISTIC_SUCCESS_CRITERIA:
    - "Test harness scope validated"
    - "Success criteria documented based on framework capabilities"
    - "Goalpost changing prevention measures documented"
```

---

This guide will be updated as new patterns are discovered. Always check the latest version before debugging and maintain systematic approach to prevent goalpost changing.