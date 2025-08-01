# Snowflake Integration Tests
## AI-Optimized Parallel Execution Protocol

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: INTEGRATION_TEST_EXECUTION
EXECUTION_MODE: PARALLEL_OPTIMIZED
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Integration test performance optimization for time savings"
  - "Account-level object naming conflicts preventing parallel execution"
  - "Test-name-based unique naming strategy implementation"
  - "ALL integration tests for all changetypes parallel execution capability"
SUCCESS_METRICS:
  - "60% performance improvement (53s vs 131s sequential)"
  - "37/37 warehouse integration tests passing in parallel"
  - "Schema isolation + unique naming enables conflict-free execution"
```

## 🚨 CRITICAL: Parallel Execution Prerequisites

```yaml
MANDATORY_PRECONDITIONS:
  ACCOUNT_LEVEL_NAMING: "Account-level objects (warehouses, databases, roles) require unique naming across tests"
  SCHEMA_ISOLATION_ENABLED: "Schema-level objects automatically isolated via test harness"
  TEST_NAME_UNIQUENESS: "Test method names provide guaranteed unique identifiers"
  MAVEN_PARALLEL_SUPPORT: "Maven surefire plugin configured for parallel execution"
ADDRESSES_CORE_ISSUE: "Integration test performance optimization for time savings"
```

## CreateWarehouseGeneratorSnowflakeIntegrationTest

This integration test validates all CREATE WAREHOUSE SQL variations against a live Snowflake database to ensure the generated SQL is syntactically correct and executes successfully.

### 🏆 PARALLEL EXECUTION SUCCESS PATTERN

```yaml
PARALLEL_EXECUTION_STRATEGY:
  NAMING_PATTERN: "TEST_{CLASS_PREFIX}_{METHOD_NAME}"
  IMPLEMENTATION: "getUniqueWarehouseName(String methodName) helper method"
  EXAMPLES:
    - "testBasicRequiredOnly → TEST_CREATE_testBasicRequiredOnly"
    - "testWithWarehouseType → TEST_CREATE_testWithWarehouseType"
    - "testMultiClusterBasic → TEST_CREATE_testMultiClusterBasic"
  
PERFORMANCE_RESULTS:
  SEQUENTIAL_TIME: "131 seconds (approximate)"
  PARALLEL_TIME: "53 seconds (measured)"
  IMPROVEMENT: "60% time savings"
  CONFLICT_RESOLUTION: "Zero naming conflicts with test-name-based strategy"

ADDRESSES_CORE_ISSUE: "ALL integration tests for all changetypes parallel execution capability"
```

### Prerequisites

1. **Snowflake Account**: Access to a Snowflake account with appropriate permissions
2. **Database/Schema**: A database and schema where you can create and drop warehouses
3. **Warehouse**: An existing warehouse to connect through

### Environment Variables

Set the following environment variables before running the integration tests:

```bash
export SNOWFLAKE_URL="jdbc:snowflake://your-account.snowflakecomputing.com/"
export SNOWFLAKE_USER="your_username"
export SNOWFLAKE_PASSWORD="your_password"
export SNOWFLAKE_DATABASE="your_database"
export SNOWFLAKE_SCHEMA="your_schema"
export SNOWFLAKE_WAREHOUSE="your_warehouse"
```

### Running the Tests

#### Option 1: Run all integration tests (SEQUENTIAL - LEGACY)
```bash
mvn test -Dtest="*IntegrationTest"
```

#### Option 2: Run only warehouse integration tests (SEQUENTIAL - LEGACY)
```bash
mvn test -Dtest="CreateWarehouseGeneratorSnowflakeIntegrationTest"
```

#### Option 3: Run specific integration test method
```bash
mvn test -Dtest="CreateWarehouseGeneratorSnowflakeIntegrationTest#testBasicRequiredOnly"
```

#### 🚀 Option 4: PARALLEL EXECUTION (RECOMMENDED - 60% FASTER)
```bash
# Run all warehouse integration tests in parallel
mvn test -Dtest="*WarehouseGeneratorSnowflakeIntegrationTest" -DforkCount=3 -DreuseForks=true

# Run specific warehouse integration tests in parallel  
mvn test -Dtest="CreateWarehouseGeneratorSnowflakeIntegrationTest,AlterWarehouseGeneratorSnowflakeIntegrationTest,DropWarehouseGeneratorSnowflakeIntegrationTest" -DforkCount=3 -DreuseForks=true

# Future: Run ALL changetype integration tests in parallel (when all implement naming strategy)
mvn test -Dtest="*IntegrationTest" -DforkCount=5 -DreuseForks=true
```

#### BLOCKING_VALIDATION: Parallel Execution Readiness
```yaml
REQUIREMENT: "Integration tests must implement unique naming strategy for account-level objects"
VALIDATION_CRITERIA:
  - "getUniqueWarehouseName(methodName) helper method implemented"
  - "All test methods use unique warehouse names based on method names"
  - "Zero naming conflicts demonstrated in parallel execution"
  - "All tests pass individually and in parallel"
FAILURE_ACTION: "STOP - Implement naming strategy before parallel execution"
ADDRESSES_CORE_ISSUE: "Integration test performance optimization for time savings"
```

### What the Tests Do

1. **Schema Isolation**: Creates a temporary schema with unique name for test isolation
2. **SQL Generation**: Uses the actual generator to create SQL statements
3. **Live Execution**: Executes each SQL statement against Snowflake
4. **Verification**: Confirms successful execution without syntax errors
5. **Cleanup**: Automatically drops all created warehouses and schemas

### Test Coverage

The integration tests cover all requirements variations:

- ✅ Basic Required Only: `CREATE WAREHOUSE TEST_WAREHOUSE`
- ✅ OR REPLACE: `CREATE OR REPLACE WAREHOUSE TEST_WAREHOUSE`
- ✅ IF NOT EXISTS: `CREATE WAREHOUSE IF NOT EXISTS TEST_WAREHOUSE`
- ✅ With Warehouse Size: `CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_SIZE = LARGE`
- ✅ With Warehouse Type: `CREATE WAREHOUSE TEST_WAREHOUSE WITH WAREHOUSE_TYPE = SNOWPARK-OPTIMIZED`
- ✅ Multi-cluster Basic: `CREATE WAREHOUSE TEST_WAREHOUSE WITH MAX_CLUSTER_COUNT = 3 MIN_CLUSTER_COUNT = 1 SCALING_POLICY = ECONOMY`
- ✅ Auto Settings: `CREATE WAREHOUSE TEST_WAREHOUSE WITH AUTO_SUSPEND = 300 AUTO_RESUME = true`
- ✅ Initially Suspended: `CREATE WAREHOUSE TEST_WAREHOUSE WITH INITIALLY_SUSPENDED = true`
- ✅ With Comment: `CREATE WAREHOUSE TEST_WAREHOUSE WITH COMMENT = 'Test warehouse for development'`
- ✅ Special Characters in Comment: Quote escaping validation
- ✅ Query Acceleration: `CREATE WAREHOUSE TEST_WAREHOUSE WITH ENABLE_QUERY_ACCELERATION = true QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10`
- ✅ WITH Clause Format: Space-separated properties validation
- ✅ All Properties: Comprehensive configuration test
- ✅ Validation: Missing warehouse name error handling
- ✅ Schema Isolation: Temporary schema verification

## 🔧 IMPLEMENTATION TEMPLATE: Test-Name-Based Unique Naming

### STEP 1: Helper Method Implementation
```yaml
STEP_ID: PARALLEL_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Account-level object naming conflicts preventing parallel execution"
```

**Add to your integration test class:**
```java
/**
 * Generates unique warehouse name based on test method name.
 * ADDRESSES_CORE_ISSUE: Account-level object naming conflicts.
 * 
 * @param methodName The test method name (use getMethodName() or pass manually)
 * @return Unique warehouse name for parallel execution
 */
private String getUniqueWarehouseName(String methodName) {
    return "TEST_CREATE_" + methodName;  // Adjust prefix per test class
}

// Alternative implementation using reflection (if needed)
private String getUniqueWarehouseName() {
    String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
    return "TEST_CREATE_" + methodName;
}
```

### STEP 2: Update All Test Methods
```yaml
STEP_ID: PARALLEL_2.0
STATUS: BLOCKED
PREREQUISITES: [PARALLEL_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "ALL integration tests for all changetypes parallel execution capability"
```

**Before (CONFLICT-PRONE):**
```java
@Test
public void testBasicRequiredOnly() throws Exception {
    String warehouseName = "TEST_WAREHOUSE_ISOLATION";  // ❌ CAUSES CONFLICTS
    // ... test implementation
}
```

**After (PARALLEL-READY):**
```java
@Test
public void testBasicRequiredOnly() throws Exception {
    String warehouseName = getUniqueWarehouseName("testBasicRequiredOnly");  // ✅ CONFLICT-FREE
    // Result: "TEST_CREATE_testBasicRequiredOnly"
    // ... test implementation
}
```

### STEP 3: Validation and Testing
```yaml
STEP_ID: PARALLEL_3.0
STATUS: BLOCKED
PREREQUISITES: [PARALLEL_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration test performance optimization for time savings"
```

```bash
# MANDATORY: Test sequential execution still works
mvn test -Dtest="YourIntegrationTest"

# MANDATORY: Test parallel execution works
mvn test -Dtest="YourIntegrationTest" -DforkCount=2 -DreuseForks=true

# CRITICAL: Validate performance improvement
# Expected: 40-60% time savings for account-level object tests
```

### Notes

- **Enterprise Features**: Some tests (multi-cluster, query acceleration) may require Snowflake Enterprise Edition
- **Resource Monitors**: Tests skip resource monitor assignment as test monitors may not exist
- **Automatic Cleanup**: All created warehouses are automatically dropped after tests
- **Unique Names**: Each test creates warehouses with unique names to avoid conflicts (PARALLEL-EXECUTION-READY)
- **Error Handling**: Tests fail immediately if SQL execution fails, providing clear error messages
- **Parallel Execution**: Test-name-based naming strategy enables conflict-free parallel execution

### Troubleshooting

#### Sequential Execution Issues
1. **Connection Issues**: Verify environment variables and network connectivity
2. **Permission Errors**: Ensure user has CREATE WAREHOUSE and DROP WAREHOUSE privileges
3. **Enterprise Features**: Some tests may fail on Standard Edition (expected behavior)
4. **Resource Limits**: Account may have limits on number of warehouses

#### Parallel Execution Issues
```yaml
SYSTEMATIC_TROUBLESHOOTING:
  ADDRESSES_CORE_ISSUE: "Integration test performance optimization troubleshooting"
```

**Issue 1: "Warehouse 'TEST_WAREHOUSE_ISOLATION' already exists"**
```yaml
ROOT_CAUSE: "Multiple tests using same warehouse name in parallel"
SYSTEMATIC_SOLUTION:
  1. "Verify getUniqueWarehouseName() method implemented"
  2. "Check all test methods use unique naming pattern"
  3. "Ensure no hardcoded warehouse names remain"
ADDRESSES_CORE_ISSUE: "Account-level object naming conflicts preventing parallel execution"
```

**Issue 2: "Tests pass individually but fail in parallel"**
```yaml
ROOT_CAUSE: "Race conditions or resource conflicts"
SYSTEMATIC_SOLUTION:
  1. "Review warehouse cleanup logic"
  2. "Check for shared state between tests"
  3. "Verify each test uses unique warehouse names"
  4. "Reduce forkCount if resource constraints"
```

**Issue 3: "Parallel execution slower than sequential"**
```yaml
ROOT_CAUSE: "Resource contention or overhead"
SYSTEMATIC_SOLUTION:
  1. "Verify Snowflake account has sufficient compute resources"
  2. "Adjust forkCount based on available resources"
  3. "Check for database connection pooling issues"
  4. "Monitor Snowflake query performance during parallel execution"
```

**Issue 4: "Some tests not using unique names"**
```yaml
ROOT_CAUSE: "Incomplete migration to naming strategy"
SYSTEMATIC_SOLUTION:
  1. "Search codebase for 'TEST_WAREHOUSE_ISOLATION'"
  2. "Replace all hardcoded names with getUniqueWarehouseName() calls"
  3. "Validate all test methods updated"
  4. "Run parallel execution test to verify no conflicts"
```

### Example Output

```
Testing Basic Required Only: CREATE WAREHOUSE TEST_WAREHOUSE_BASIC
✅ SUCCESS: Basic Required Only

Testing OR REPLACE: CREATE OR REPLACE WAREHOUSE TEST_WAREHOUSE_OR_REPLACE
✅ SUCCESS: OR REPLACE

...

Cleaned up warehouse: TEST_WAREHOUSE_BASIC
Cleaned up warehouse: TEST_WAREHOUSE_OR_REPLACE
Cleaned up schema: WAREHOUSE_TEST_A1B2C3D4
```