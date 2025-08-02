# Test Harness Implementation Guide
## AI-Optimized Sequential Testing Protocol with Schema Isolation

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: TEST_HARNESS_IMPLEMENTATION
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Integration tests not testing ALL generated SQL"
  - "Complete SQL test statements through harness validation"
  - "Realistic success criteria based on framework limitations"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/test_harness_guide.md"
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - master_process_loop.md: "Master process for all implementations"
  - changetype_patterns.md: "New and existing changetype patterns"
  - sql_generator_overrides.md: "SQL syntax override implementations"
  - requirements_creation.md: "Detailed requirements specification"

RELATED_GUIDES:
  - "../snapshot_diff_implementation/error_patterns_guide.md": "Systematic debugging"
  - "../snapshot_diff_implementation/part4_testing_guide.md": "Advanced testing strategies"
```

## 🚨 CRITICAL: Setup Before Proceeding

```yaml
MANDATORY_PRECONDITIONS:
  JAR_BUILD_INSTALL: "Extension MUST be built with 'mvn clean install -DskipTests'"
  DIRECTORY_VERIFICATION: "MUST be in liquibase-test-harness directory"
  SCHEMA_ISOLATION_ENABLED: "Lifecycle hooks enabled in harness-config.yml"
  DEPENDENCY_RESOLUTION: "Test harness loads extension via Maven dependencies, NOT file copying"
```

**⚠️ CRITICAL SETUP COMMANDS:**
```bash
# 1. Build and install JAR - MANDATORY
cd liquibase-[database]
mvn clean install -DskipTests

# 2. Change directory - MANDATORY  
cd ../liquibase-test-harness

# 3. Verify location - MANDATORY
pwd  # Must show liquibase-test-harness

# 4. Ensure lifecycle hooks enabled
grep -A 5 "lifecycleHooks:" src/test/resources/harness-config.yml
```

**Why This Matters:**
- Test harness has Maven dependency: `<groupId>org.liquibase</groupId><artifactId>liquibase-[database]</artifactId>`
- Dependency loads from local Maven repository (~/.m2/repository/), NOT file paths
- `mvn package` only builds JAR but doesn't install where Maven can find it
- `mvn install` builds AND installs to local repository
- Manual file copying was incorrect and caused mysterious test failures

## 🎉 Schema Isolation Simplifications

### What's New with Schema Isolation?
```yaml
SIMPLIFICATIONS:
  - AUTOMATIC_SCHEMA_ISOLATION: "Each test runs in own TEST_<TESTNAME> schema"
  - NO_INIT_CLEANUP_MANAGEMENT: "Schema isolation handles cleanup automatically"
  - SIMPLER_TEST_FILES: "Focus on actual test cases, not boilerplate"
  - TEST_LEVEL_INIT_SCRIPTS: "Optional per-test setup available"
  - PREDICTABLE_SCHEMA_NAMES: "Makes debugging easier"
  
BENEFITS:
  - NO_TEST_POLLUTION: "Tests can run in parallel without conflicts"
  - ISOLATED_DEBUGGING: "Failed tests leave schemas for inspection"
  - SIMPLER_STRUCTURE: "Less boilerplate code required"
  
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL with isolated validation"
```

## Sequential Blocking Test Implementation

### STEP 1: Schema Isolation Configuration - **ENVIRONMENT SETUP**
```yaml
STEP_ID: HARNESS_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework capabilities"
```

#### BLOCKING_VALIDATION_1.1: Schema Isolation Enabled
```yaml
REQUIREMENT: "Schema isolation properly configured and verified"
VALIDATION_CRITERIA:
  - "harness-config.yml has lifecycleHooks enabled"
  - "useSchemaIsolation set to true for cloud databases"
  - "Standard test passes to verify configuration"
FAILURE_ACTION: "STOP - Fix schema isolation configuration"
```

**Configuration Check:**
```yaml
# src/test/resources/harness-config.yml
lifecycleHooks:
  enabled: true

databasesUnderTest:
  - name: [database]
    useSchemaIsolation: true  # Enable for cloud databases
    # ... other config
```

**Validation Commands:**
```bash
# MANDATORY: Verify configuration
grep -A 5 "lifecycleHooks:" src/test/resources/harness-config.yml
grep -A 10 "useSchemaIsolation:" src/test/resources/harness-config.yml

# MANDATORY: Test with standard objects first
mvn test -Dtest=ChangeObjectTests -DdbName=[database] -DchangeObjects=createTable
```

### STEP 2: Test Harness File Creation - **COMPLETE SQL COVERAGE**
```yaml
STEP_ID: HARNESS_2.0
STATUS: BLOCKED
PREREQUISITES: [HARNESS_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_2.1: Test Files Complete
```yaml
REQUIREMENT: "All test harness files created with comprehensive SQL coverage"
VALIDATION_CRITERIA:
  - "Changelog covers ALL SQL generation scenarios"
  - "Expected SQL matches complete generated SQL"
  - "Expected snapshot reflects final database state"
  - "All property combinations tested"
FAILURE_ACTION: "STOP - Create comprehensive test harness files"
```

### Test File 1: Changelog - **ALL SQL SCENARIOS**

**Location:** `src/main/resources/liquibase/harness/change/changelogs/[database]/[changeType].xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:[database]="http://www.liquibase.org/xml/ns/[database]"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
                      http://www.liquibase.org/xml/ns/[database]
                      http://www.liquibase.org/xml/ns/[database]/liquibase-[database]-latest.xsd">

    <!-- CRITICAL: Test basic functionality -->
    <changeSet id="test-basic-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_BASIC"/>
    </changeSet>

    <!-- CRITICAL: Test with optional attributes -->
    <changeSet id="test-optional-attributes-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_OPTIONAL"
                                 optionalAttribute="OPTIONAL_VALUE"/>
    </changeSet>

    <!-- CRITICAL: Test with namespace attributes (if applicable) -->
    <changeSet id="test-namespace-attributes-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_NAMESPACE"
                                 [database]:specialAttribute="SPECIAL_VALUE"/>
    </changeSet>

    <!-- CRITICAL: Test edge cases from requirements -->
    <changeSet id="test-edge-cases-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_EDGE_CASE"
                                 optionalAttribute="BOUNDARY_VALUE"/>
    </changeSet>

    <!-- CRITICAL: Test maximum complexity scenario -->
    <changeSet id="test-complete-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_COMPLETE"
                                 optionalAttribute="COMPLETE_VALUE"
                                 [database]:specialAttribute="COMPLETE_SPECIAL"
                                 [database]:anotherAttribute="ANOTHER_VALUE"/>
    </changeSet>

    <!-- NO CLEANUP CHANGESETS HERE - Schema isolation handles cleanup -->

</databaseChangeLog>
```

### Test File 2: Expected SQL - **COMPLETE SQL VALIDATION**

**Location:** `src/main/resources/liquibase/harness/change/expectedSql/[database]/[changeType].sql`

```sql
-- CRITICAL: Expected SQL must use isolated schema name TEST_[CHANGETYPE]
-- ADDRESSES_CORE_ISSUE: Integration tests testing ALL generated SQL

-- Basic functionality test SQL
[COMPLETE_SQL_FOR_BASIC_TEST_WITH_SCHEMA_PREFIX];

-- Optional attributes test SQL  
[COMPLETE_SQL_FOR_OPTIONAL_ATTRIBUTES_WITH_SCHEMA_PREFIX];

-- Namespace attributes test SQL
[COMPLETE_SQL_FOR_NAMESPACE_ATTRIBUTES_WITH_SCHEMA_PREFIX];

-- Edge cases test SQL
[COMPLETE_SQL_FOR_EDGE_CASES_WITH_SCHEMA_PREFIX];

-- Complete functionality test SQL
[COMPLETE_SQL_FOR_COMPLETE_TEST_WITH_SCHEMA_PREFIX];
```

**CRITICAL REQUIREMENTS:**
- SQL must use isolated schema name: `TEST_[CHANGETYPE]`
- SQL must be complete and executable
- SQL must match exactly what generator produces
- All property combinations must be represented

### Test File 3: Expected Snapshot - **FINAL STATE VALIDATION**

**Location:** `src/main/resources/liquibase/harness/change/expectedSnapshot/[changeType].json`

```json
{
  "snapshot": {
    "objects": {
      "liquibase.ext.[database].database.object.[ObjectType]": [
        {
          "[objectType]": {
            "name": "TEST_BASIC",
            "schema": "TEST_[CHANGETYPE]",
            "requiredAttribute": "TEST_BASIC"
          }
        },
        {
          "[objectType]": {
            "name": "TEST_OPTIONAL", 
            "schema": "TEST_[CHANGETYPE]",
            "requiredAttribute": "TEST_OPTIONAL",
            "optionalAttribute": "OPTIONAL_VALUE"
          }
        },
        {
          "[objectType]": {
            "name": "TEST_NAMESPACE",
            "schema": "TEST_[CHANGETYPE]", 
            "requiredAttribute": "TEST_NAMESPACE",
            "specialAttribute": "SPECIAL_VALUE"
          }
        },
        {
          "[objectType]": {
            "name": "TEST_EDGE_CASE",
            "schema": "TEST_[CHANGETYPE]",
            "requiredAttribute": "TEST_EDGE_CASE",
            "optionalAttribute": "BOUNDARY_VALUE"
          }
        },
        {
          "[objectType]": {
            "name": "TEST_COMPLETE",
            "schema": "TEST_[CHANGETYPE]",
            "requiredAttribute": "TEST_COMPLETE",
            "optionalAttribute": "COMPLETE_VALUE",
            "specialAttribute": "COMPLETE_SPECIAL",
            "anotherAttribute": "ANOTHER_VALUE"
          }
        }
      ]
    }
  }
}
```

**CRITICAL REQUIREMENTS:**
- Use isolated schema name: `TEST_[CHANGETYPE]`
- Include all objects created by all changesets
- Only include configuration properties, not state properties
- No database metadata fields
- No "IGNORE" values

### STEP 3: Test Execution - **SYSTEMATIC VALIDATION**
```yaml
STEP_ID: HARNESS_3.0
STATUS: BLOCKED
PREREQUISITES: [HARNESS_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_3.1: All Tests Pass
```yaml
REQUIREMENT: "Test harness executes successfully with all SQL verified"
VALIDATION_CRITERIA:
  - "All changesets execute without errors"
  - "Generated SQL matches expected SQL exactly"
  - "Database state matches expected snapshot"
  - "All property combinations validated"
FAILURE_ACTION: "STOP - Debug test failures systematically"
```

**Test Execution Commands:**
```bash
# MANDATORY: Run test harness
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=[changeType] -DdbName=[database]

# CRITICAL: Verify all changesets executed
grep "Running Changeset" target/surefire-reports/*.xml | wc -l

# CRITICAL: Check for SQL execution errors
grep -i "error\|exception\|failed" target/surefire-reports/*.xml

# CRITICAL: Verify schema isolation working
grep -i "TEST_[CHANGETYPE]" target/surefire-reports/*.xml
```

### STEP 4: Systematic Debugging - **ERROR PATTERN APPLICATION**
```yaml
STEP_ID: HARNESS_4.0
STATUS: CONDITIONAL
PREREQUISITES: [HARNESS_3.0_FAILED]
VALIDATION_MODE: SYSTEMATIC
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
```

#### Common Test Harness Issues - Systematic Solutions

**Issue 1: "Schema 'TEST_[CHANGETYPE]' does not exist"**
```yaml
ROOT_CAUSE: "Schema isolation not enabled or configured incorrectly"
SYSTEMATIC_SOLUTION:
  1. "Verify useSchemaIsolation: true in harness-config.yml"
  2. "Ensure lifecycleHooks: enabled: true"
  3. "Check database supports schema isolation"
CONSULT: "../snapshot_diff_implementation/error_patterns_guide.md - Pattern #3"
DO_NOT_CHANGE_REQUIREMENTS: "Fix configuration, don't disable isolation"
```

**Issue 2: "Expected SQL doesn't match"**
```yaml
ROOT_CAUSE: "Expected SQL uses wrong schema name or incomplete SQL"
SYSTEMATIC_SOLUTION:
  1. "Update expected SQL to use TEST_[CHANGETYPE] schema"
  2. "Ensure SQL is complete and matches generator output exactly"
  3. "Verify all property combinations represented"
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
DO_NOT_CHANGE_REQUIREMENTS: "Fix expected SQL, don't change generator"
```

**Issue 3: "Test hangs after completion"**
```yaml
ROOT_CAUSE: "Connection state issues with schema switching"
SYSTEMATIC_SOLUTION:
  1. "Already resolved in current schema isolation implementation"
  2. "If still occurs, check database connection pooling settings"
  3. "Verify proper schema cleanup in lifecycle hooks"
ADDRESSES_CORE_ISSUE: "Realistic success criteria"
```

**Issue 4: "Object already exists"**
```yaml
ROOT_CAUSE: "Previous test run didn't clean up (shouldn't happen with isolation)"
SYSTEMATIC_SOLUTION:
  1. "Manually drop the test schema: DROP SCHEMA TEST_[CHANGETYPE] CASCADE"
  2. "Re-run test to verify isolation working"
  3. "If repeated, check lifecycle hook configuration"
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

**Issue 5: "Some SQL statements not executed"**
```yaml
ROOT_CAUSE: "Test harness not executing all changesets"
SYSTEMATIC_SOLUTION:
  1. "Verify all changesets have unique IDs"
  2. "Check DATABASECHANGELOG for execution records"
  3. "Ensure all changesets are in proper order"
  4. "Add verification to confirm all SQL executed"
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
DO_NOT_CHANGE_REQUIREMENTS: "Fix test execution, don't reduce test coverage"
```

## Optional: Test-Level Init Scripts

### When to Use Test-Level Init Scripts
```yaml
USE_WHEN:
  - "Test needs special database setup"
  - "Custom DATABASECHANGELOG/DATABASECHANGELOGLOCK required"
  - "Database-specific initialization needed"
  
LOCATION: "src/test/resources/harness/changeObjects/[database]/[testName].init.sql"
```

**Example Init Script:**
```sql
-- src/test/resources/harness/changeObjects/[database]/[changeType].init.sql
-- This runs before your test in the isolated schema

-- Create custom changelog tables if needed
CREATE OR REPLACE TABLE DATABASECHANGELOG (
  ID VARCHAR(255) NOT NULL,
  AUTHOR VARCHAR(255) NOT NULL,
  FILENAME VARCHAR(255) NOT NULL,
  -- ... other required columns
);

CREATE OR REPLACE TABLE DATABASECHANGELOGLOCK (
  ID INT NOT NULL,
  LOCKED BOOLEAN NOT NULL DEFAULT FALSE,
  -- ... other required columns
);

-- Any other test-specific setup
```

## Schema Isolation Technical Details

### How Schema Isolation Works
```yaml
BEFORE_TEST:
  1. "Creates schema TEST_<TESTNAME> (e.g., TEST_CREATEWAREHOUSE)"
  2. "Switches database context to the new schema"
  3. "Runs test-level init script if present"
  
DURING_TEST:
  4. "All operations happen in the isolated schema"
  5. "No interference with other tests"
  6. "Clean, predictable environment"
  
AFTER_TEST:
  7. "Switches back to original schema"
  8. "Drops the test schema completely"
  9. "No manual cleanup needed"
```

### Schema Naming Conventions
```yaml
NAMING_RULES:
  - "Test name: createWarehouse.xml → Schema: TEST_CREATEWAREHOUSE"
  - "Test name: alterTableAddColumn.xml → Schema: TEST_ALTERTABLEADDCOLUMN"
  - "Special characters replaced with underscores"
  - "Schema names are deterministic and predictable"
```

## Account-Level Objects Considerations

### Objects Requiring Manual Cleanup
```yaml
ACCOUNT_LEVEL_OBJECTS:
  - "Warehouses, databases, roles, users"
  - "Cannot be isolated to schemas"
  - "Still need cleanup in test file"
  
SOLUTION_PATTERN:
  - "Add cleanup changesets with runAlways='true'"
  - "Restore proper context after cleanup"
```

**Example for Account-Level Objects:**
```xml
<changeSet id="cleanup-warehouse" author="test-harness" runAlways="true">
    <sql>
        DROP WAREHOUSE IF EXISTS TEST_WH CASCADE;
        USE WAREHOUSE LTHDB_TEST_WH;  -- Restore context
    </sql>
</changeSet>
```

## Migration from Old Pattern

### Old Pattern (Complex)
```yaml
OLD_PATTERN_PROBLEMS:
  - "Include init.xml and cleanup.xml"
  - "Add cleanup changesets with runAlways='true'"
  - "Worry about state pollution between tests"
  - "Complex ordering requirements"
  - "Manual cleanup management"
```

### New Pattern (Simple)
```yaml
NEW_PATTERN_BENEFITS:
  - "Just write your test changesets"
  - "Update expected SQL to use TEST_<TESTNAME>"
  - "Update snapshot to use TEST_<TESTNAME>"
  - "Run test - cleanup automatic"
  - "Focus on actual test logic"
```

### Migration Steps
```yaml
MIGRATION_PROCESS:
  1. "Remove include file='init.xml' statements"
  2. "Remove cleanup changesets (unless account-level objects)"
  3. "Remove include file='cleanup.xml' statements"
  4. "Update expected SQL schema references to TEST_<TESTNAME>"
  5. "Update snapshot schema references to TEST_<TESTNAME>"
  6. "Run test and verify isolation working"
```

## Automated Test Harness Workflow

```bash
#!/bin/bash
# scripts/test-harness-workflow.sh - Complete workflow with validation
set -e

echo "=== Test Harness Development Workflow ==="

# Validate inputs
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 [database] [changetype]"
    echo "Example: $0 snowflake createWarehouse"
    exit 1
fi

DATABASE=$1
CHANGETYPE=$2

echo "1. Pre-flight validation..."
echo "   - Checking test harness files exist..."
CHANGELOG_FILE="src/main/resources/liquibase/harness/change/changelogs/$DATABASE/$CHANGETYPE.xml"
EXPECTED_SQL_FILE="src/main/resources/liquibase/harness/change/expectedSql/$DATABASE/$CHANGETYPE.sql"
EXPECTED_SNAPSHOT_FILE="src/main/resources/liquibase/harness/change/expectedSnapshot/$CHANGETYPE.json"

if [ ! -f "$CHANGELOG_FILE" ]; then
    echo "   ERROR: Changelog file missing: $CHANGELOG_FILE"
    exit 1
fi

if [ ! -f "$EXPECTED_SQL_FILE" ]; then
    echo "   ERROR: Expected SQL file missing: $EXPECTED_SQL_FILE"
    exit 1
fi

if [ ! -f "$EXPECTED_SNAPSHOT_FILE" ]; then
    echo "   ERROR: Expected snapshot file missing: $EXPECTED_SNAPSHOT_FILE"
    exit 1
fi

echo "2. Building extension for $DATABASE..."
cd ../liquibase-$DATABASE
mvn clean install -DskipTests

echo "3. Installing to local repository..."
echo "   (Required for test harness dependency resolution)"

echo "4. Returning to test harness..."
cd ../liquibase-test-harness

echo "5. Verifying schema isolation configuration..."
if ! grep -q "useSchemaIsolation: true" src/test/resources/harness-config.yml; then
    echo "   WARNING: Schema isolation may not be enabled"
fi

echo "6. Running test harness for $CHANGETYPE..."
mvn test -Dtest=ChangeObjectTests -DdbName=$DATABASE -DchangeObjects="$CHANGETYPE"

if [ $? -eq 0 ]; then
    echo "7. SUCCESS: All tests passed!"
    echo "   - All changesets executed successfully"
    echo "   - Generated SQL matched expected SQL"
    echo "   - Database state matched expected snapshot"
else
    echo "7. FAILURE: Tests failed - check output above"
    echo "   - Review systematic debugging in test_harness_guide.md"
    echo "   - Check error_patterns_guide.md for common solutions"
    exit 1
fi

echo "=== Test Harness Workflow Complete ==="
```

## Quick Checklist for Test Harness Success

### Before Running Tests
```yaml
MANDATORY_CHECKLIST:
  - [ ] "JAR built and installed with 'mvn clean install -DskipTests'"
  - [ ] "In liquibase-test-harness directory (verify with pwd)"
  - [ ] "Schema isolation enabled in harness-config.yml"
  - [ ] "Test changelog created with ALL SQL scenarios"
  - [ ] "Expected SQL uses TEST_<TESTNAME> schema"
  - [ ] "Expected snapshot uses TEST_<TESTNAME> schema"
  - [ ] "All property combinations covered in tests"
```

### During Test Execution
```yaml
VALIDATION_POINTS:
  - [ ] "All changesets execute without errors"
  - [ ] "Schema isolation creates and switches to test schema"
  - [ ] "Generated SQL matches expected SQL exactly"
  - [ ] "Database state matches expected snapshot"
  - [ ] "Test schema is cleaned up after completion"
```

### After Test Success
```yaml
COMPLETION_VALIDATION:
  - [ ] "All SQL generation scenarios validated"
  - [ ] "All property combinations tested"
  - [ ] "Integration test covers complete functionality"
  - [ ] "Realistic success criteria met based on framework capabilities"
```

## 🚀 PARALLEL INTEGRATION TEST EXECUTION - AI-OPTIMIZED PROTOCOL

### Overview of Parallel Execution Benefits
```yaml
PARALLEL_EXECUTION_PROTOCOL:
  EXECUTION_MODE: PARALLEL_OPTIMIZED_INTEGRATION_TESTING
  ADDRESSES_CORE_ISSUES:
    - "Integration test performance optimization for time savings"
    - "Account-level object naming conflicts preventing parallel execution"
    - "ALL integration tests for all changetypes parallel execution capability"
  SUCCESS_METRICS:
    - "60% performance improvement demonstrated (warehouse tests: 53s vs 131s)"
    - "Zero naming conflicts with test-name-based strategy"
    - "Schema isolation + unique naming = conflict-free parallel execution"
```

### STEP 1: Parallel Execution Prerequisites - **ACCOUNT-LEVEL OBJECT STRATEGY**
```yaml
STEP_ID: PARALLEL_HARNESS_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Account-level object naming conflicts preventing parallel execution"
```

#### BLOCKING_VALIDATION_PARALLEL_1.1: Account vs Schema Object Classification
```yaml
REQUIREMENT: "Understand difference between account-level and schema-level objects for parallel strategy"
VALIDATION_CRITERIA:
  - "Account-level objects identified (warehouses, databases, roles, users)"
  - "Schema-level objects identified (tables, views, sequences, procedures)"
  - "Naming strategy selected based on object isolation capabilities"
  - "Test harness schema isolation limitations understood"
FAILURE_ACTION: "STOP - Classify objects before implementing parallel strategy"
```

**Object Classification for Parallel Execution:**
```yaml
ACCOUNT_LEVEL_OBJECTS:
  EXAMPLES: ["Warehouses", "Databases", "Roles", "Users", "Resource Monitors"]
  ISOLATION: "CANNOT be isolated to schemas - exist at account level"
  NAMING_STRATEGY: "REQUIRED - Test-name-based unique naming"
  PARALLEL_RISK: "HIGH - Same names cause conflicts across parallel tests"
  
SCHEMA_LEVEL_OBJECTS:
  EXAMPLES: ["Tables", "Views", "Sequences", "Procedures", "Stages"]
  ISOLATION: "AUTOMATIC - Test harness schema isolation handles these"
  NAMING_STRATEGY: "OPTIONAL - Schema isolation prevents conflicts"
  PARALLEL_RISK: "LOW - Isolated by test harness automatically"
```

### STEP 2: Integration Test Naming Strategy Implementation - **TEST-NAME-BASED UNIQUENESS**
```yaml
STEP_ID: PARALLEL_HARNESS_2.0
STATUS: BLOCKED
PREREQUISITES: [PARALLEL_HARNESS_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "ALL integration tests for all changetypes parallel execution capability"
```

#### BLOCKING_VALIDATION_PARALLEL_2.1: Naming Strategy Implementation Complete
```yaml
REQUIREMENT: "All integration tests implement test-name-based unique naming for account-level objects"
VALIDATION_CRITERIA:
  - "getUniqueObjectName(methodName) helper methods implemented"
  - "All test methods use unique object names based on method names"
  - "Naming pattern follows TEST_{CLASS_PREFIX}_{METHOD_NAME} convention"
  - "No hardcoded object names remain that could cause conflicts"
FAILURE_ACTION: "STOP - Implement complete naming strategy before parallel execution"
```

**Integration Test Naming Strategy Template:**
```java
/**
 * AI-OPTIMIZED: Integration test with parallel execution support.
 * ADDRESSES_CORE_ISSUE: Account-level object naming conflicts.
 */
public class [ChangeType]GeneratorSnowflakeIntegrationTest {
    
    /**
     * CRITICAL: Generates unique object name based on test method name.
     * ADDRESSES_CORE_ISSUE: Account-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique object name for parallel execution
     */
    private String getUniqueObjectName(String methodName) {
        return "TEST_[OBJECT_PREFIX]_" + methodName;
    }
    
    /**
     * EXAMPLE: Parallel-execution-ready integration test.
     * ADDRESSES_CORE_ISSUE: ALL integration tests for all changetypes parallel execution capability.
     */
    @Test
    public void testBasicRequiredOnly() throws Exception {
        // CRITICAL: Use unique naming to prevent parallel conflicts
        String objectName = getUniqueObjectName("testBasicRequiredOnly");
        // Result: "TEST_[OBJECT_PREFIX]_testBasicRequiredOnly"
        
        try {
            // Test implementation with unique object name
            executeTest(objectName);
            
        } finally {
            // MANDATORY: Cleanup using the same unique name
            cleanupObject(objectName);
        }
    }
}
```

**Naming Pattern Examples by Object Type:**
```yaml
NAMING_PATTERNS:
  WAREHOUSES:
    CREATE: "TEST_CREATE_testBasicRequiredOnly"
    ALTER: "TEST_ALTER_testRenameWarehouse"  
    DROP: "TEST_DROP_testBasicDrop"
    
  DATABASES:
    CREATE: "TEST_CREATE_DB_testWithComment"
    ALTER: "TEST_ALTER_DB_testRenameDatabase"
    DROP: "TEST_DROP_DB_testCascadeDrop"
    
  ROLES:
    CREATE: "TEST_CREATE_ROLE_testBasicRole"
    GRANT: "TEST_GRANT_ROLE_testGrantToUser"
    DROP: "TEST_DROP_ROLE_testDropRole"
```

### STEP 3: Maven Parallel Execution Configuration - **PERFORMANCE OPTIMIZATION**
```yaml
STEP_ID: PARALLEL_HARNESS_3.0
STATUS: BLOCKED
PREREQUISITES: [PARALLEL_HARNESS_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration test performance optimization for time savings"
```

#### BLOCKING_VALIDATION_PARALLEL_3.1: Parallel Execution Verified
```yaml
REQUIREMENT: "Maven parallel execution configured and validated"
VALIDATION_CRITERIA:
  - "Sequential execution still works (baseline validation)"
  - "Parallel execution works without conflicts"
  - "Performance improvement demonstrated (40-60% expected)"
  - "All tests pass individually and in parallel"
FAILURE_ACTION: "STOP - Fix parallel execution issues before proceeding"
```

**Parallel Execution Commands:**
```bash
# MANDATORY: Test sequential execution first (baseline)
cd liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DdbName=[database] -DchangeObjects="[changeType]"

# CRITICAL: Test parallel execution for account-level objects
mvn test -Dtest="*[ObjectType]*IntegrationTest" -DforkCount=3 -DreuseForks=true

# EXAMPLE: Warehouse integration tests in parallel
mvn test -Dtest="*WarehouseGeneratorSnowflakeIntegrationTest" -DforkCount=3 -DreuseForks=true

# FUTURE: All integration tests in parallel (when all implement naming strategy)
mvn test -Dtest="*IntegrationTest" -DforkCount=5 -DreuseForks=true
```

**Performance Validation:**
```yaml
PERFORMANCE_EXPECTATIONS:
  BASELINE_IMPROVEMENT: "40-60% time savings for account-level object tests"
  WAREHOUSE_EXAMPLE: "37 tests: 131s sequential → 53s parallel (60% improvement)"
  SCALING_FACTOR: "More tests = greater absolute time savings"
  RESOURCE_CONSIDERATION: "forkCount should not exceed available CPU cores + database connections"
```

### STEP 4: Troubleshooting Parallel Execution - **SYSTEMATIC PROBLEM RESOLUTION**
```yaml
STEP_ID: PARALLEL_HARNESS_4.0
STATUS: CONDITIONAL
PREREQUISITES: [PARALLEL_HARNESS_3.0_FAILED]
VALIDATION_MODE: SYSTEMATIC
ADDRESSES_CORE_ISSUE: "Integration test performance optimization troubleshooting"
```

#### Common Parallel Execution Issues - Systematic Solutions

**Issue 1: "Object already exists" errors in parallel execution**
```yaml
ROOT_CAUSE: "Multiple tests using same object name simultaneously"
SYSTEMATIC_SOLUTION:
  1. "Verify getUniqueObjectName() method implemented correctly"
  2. "Search for hardcoded object names: grep -r 'TEST_OBJECT_NAME' src/test/"
  3. "Ensure all test methods use unique naming pattern"
  4. "Check cleanup logic uses same unique names"
ADDRESSES_CORE_ISSUE: "Account-level object naming conflicts preventing parallel execution"
DO_NOT_CHANGE_REQUIREMENTS: "Fix naming strategy, don't disable parallel execution"
```

**Issue 2: "Tests pass individually but fail in parallel"**
```yaml
ROOT_CAUSE: "Race conditions or resource contention"
SYSTEMATIC_SOLUTION:
  1. "Review object cleanup logic - ensure proper cleanup in finally blocks"
  2. "Check for shared state between test methods"
  3. "Verify database connection handling"
  4. "Reduce forkCount if resource constraints detected"
ADDRESSES_CORE_ISSUE: "Integration test performance optimization"
```

**Issue 3: "Parallel execution slower than sequential"**
```yaml
ROOT_CAUSE: "Resource contention or configuration issues"
SYSTEMATIC_SOLUTION:
  1. "Check database connection pool size"
  2. "Verify adequate CPU resources (forkCount ≤ CPU cores)"
  3. "Monitor database performance during parallel execution"
  4. "Consider database-specific connection limits"
ADDRESSES_CORE_ISSUE: "Integration test performance optimization for time savings"
```

**Issue 4: "Some integration tests not compatible with parallel execution"**
```yaml
ROOT_CAUSE: "Legacy integration tests not implementing naming strategy"
SYSTEMATIC_SOLUTION:
  1. "Identify all integration test classes: find . -name '*IntegrationTest.java'"
  2. "Migrate each class to use getUniqueObjectName() pattern"
  3. "Test each class individually after migration"
  4. "Add to parallel execution suite after validation"
DO_NOT_CHANGE_REQUIREMENTS: "Migrate tests to naming strategy, don't exclude from parallel execution"
```

## Test Harness Schema Isolation + Parallel Execution Combined Benefits

### Why This Combination Is Powerful
```yaml
COMBINED_BENEFITS:
  SCHEMA_ISOLATION_HANDLES:
    - "Tables, views, sequences, procedures"
    - "Automatic cleanup after test completion"  
    - "Predictable naming (TEST_<TESTNAME>)"
    
  UNIQUE_NAMING_HANDLES:
    - "Warehouses, databases, roles, users"
    - "Manual cleanup with unique names"
    - "Predictable naming (TEST_<PREFIX>_<METHOD>)"
    
  RESULT:
    - "Complete isolation for all object types"
    - "Zero conflicts in parallel execution"
    - "Maximum performance improvement"
    - "Clean, maintainable test code"
    
ADDRESSES_CORE_ISSUE: "ALL integration tests for all changetypes parallel execution capability"
```

## Cross-Reference Links
```yaml
RELATED_DOCUMENTS:
  MASTER_PROCESS: "master_process_loop.md - Overall development process"
  CHANGETYPE_PATTERNS: "changetype_patterns.md - Implementation patterns"
  SQL_OVERRIDES: "sql_generator_overrides.md - SQL syntax modifications"
  ERROR_DEBUGGING: "../snapshot_diff_implementation/error_patterns_guide.md"
  ADVANCED_TESTING: "../snapshot_diff_implementation/part4_testing_guide.md"
  PARALLEL_EXECUTION: "integration_test_parallel_execution_best_practices.md - Comprehensive parallel testing guide"
  
NAVIGATION: "README.md - Complete navigation guide"
```