# ALTER WAREHOUSE Requirements
## AI-Optimized Requirements for Snowflake ALTER WAREHOUSE Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "New_Changetype"
OBJECT_TYPE: "Warehouse"
OPERATION: "ALTER"
ESTIMATED_TIME: "6-8 hours"
COMPLEXITY: "HIGH"
ATTRIBUTES_COUNT: 18
OPERATION_GROUPS: 6
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Operation Groups (Mutually Exclusive)
| Group | Operations | SQL Pattern | Key Features |
|-------|------------|-------------|--------------|
| **RENAME** | Rename warehouse | `ALTER WAREHOUSE [IF EXISTS] name RENAME TO new_name` | Error-safe renaming |
| **SET** | Configure properties | `ALTER WAREHOUSE [IF EXISTS] name SET properties` | Size, clustering, auto-suspend |
| **UNSET** | Remove properties | `ALTER WAREHOUSE [IF EXISTS] name UNSET properties` | Reset to defaults |
| **SUSPEND** | Suspend warehouse | `ALTER WAREHOUSE [IF EXISTS] name SUSPEND` | Stop billing |
| **RESUME** | Resume warehouse | `ALTER WAREHOUSE [IF EXISTS] name RESUME [IF SUSPENDED]` | Start operations |
| **ABORT** | Terminate queries | `ALTER WAREHOUSE name ABORT ALL QUERIES` | Admin operation |

### Quick Implementation Pattern
```yaml
PATTERN: "New Changetype"
REASON: "ALTER WAREHOUSE doesn't exist in core Liquibase"
IMPLEMENTATION: "Complete new changetype with operation type detection"
VALIDATION: "6 mutually exclusive operations + Enterprise Edition features"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/alter-warehouse"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ All 6 operation types + Enterprise Edition features documented"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "6 operation groups cannot be combined"
CLUSTER_CONSTRAINT: "MIN_CLUSTER_COUNT <= MAX_CLUSTER_COUNT"
AUTO_SUSPEND_RULES: "Must be 0 (disabled), NULL (never), or >= 60 seconds"
ENTERPRISE_FEATURES: "Multi-cluster and query acceleration require Enterprise Edition"
IMMEDIATE_EFFECT: "Non-transactional, immediate application"
```

## 🎯 SQL SYNTAX TEMPLATES

### Group 1: RENAME (Mutually Exclusive)
```sql
ALTER WAREHOUSE [IF EXISTS] warehouse_name RENAME TO new_warehouse_name;
```

### Group 2: SET PROPERTIES (Mutually Exclusive)
```sql
ALTER WAREHOUSE [IF EXISTS] warehouse_name SET
  [WAREHOUSE_SIZE = {XSMALL|SMALL|MEDIUM|LARGE|XLARGE|XXLARGE|XXXLARGE|X4LARGE|X5LARGE|X6LARGE}]
  [MAX_CLUSTER_COUNT = num] [MIN_CLUSTER_COUNT = num]
  [SCALING_POLICY = {STANDARD|ECONOMY}]
  [AUTO_SUSPEND = num|NULL] [AUTO_RESUME = {TRUE|FALSE}]
  [RESOURCE_MONITOR = monitor_name]
  [COMMENT = 'string' (≤256 chars)]
  [ENABLE_QUERY_ACCELERATION = {TRUE|FALSE}]
  [QUERY_ACCELERATION_MAX_SCALE_FACTOR = num]
  [STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = num]
  [STATEMENT_TIMEOUT_IN_SECONDS = num]
  [MAX_CONCURRENCY_LEVEL = num]
  [WAREHOUSE_TYPE = {STANDARD|SNOWPARK-OPTIMIZED}];
```

### Group 3: UNSET PROPERTIES (Mutually Exclusive)
```sql
ALTER WAREHOUSE [IF EXISTS] warehouse_name UNSET
  [RESOURCE_MONITOR] [COMMENT];
```

### Group 4: SUSPEND (Mutually Exclusive)
```sql
ALTER WAREHOUSE [IF EXISTS] warehouse_name SUSPEND;
```

### Group 5: RESUME (Mutually Exclusive)
```sql
ALTER WAREHOUSE [IF EXISTS] warehouse_name RESUME [IF SUSPENDED];
```

### Group 6: ABORT QUERIES (Mutually Exclusive)
```sql
ALTER WAREHOUSE warehouse_name ABORT ALL QUERIES;
```

### Critical Constraints
```yaml
CLUSTER_CONSTRAINT: "MIN_CLUSTER_COUNT <= MAX_CLUSTER_COUNT (both 1-10)"
AUTO_SUSPEND_CONSTRAINT: "0 (disabled), NULL (never), or >= 60 seconds"
QUERY_ACCELERATION_CONSTRAINT: "Scale factor only valid when acceleration enabled"
SIZE_VALIDATION: "Valid warehouse sizes from XSMALL to X6LARGE"
ENTERPRISE_REQUIREMENTS: "Multi-cluster and query acceleration need Enterprise Edition"
```

## 📊 ATTRIBUTES QUICK REFERENCE

### Core Attributes (All Operations)
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **warehouseName** | String | ✅ | Valid identifier | Primary key |
| **ifExists** | Boolean | ❌ | true/false | Error prevention |

### Group 1: RENAME Attributes
| Attribute | Type | Required | Values | Constraints |
|-----------|------|----------|--------|-------------|
| **newWarehouseName** | String | ✅ | Valid identifier | Must be unique |

### Group 2: SET Property Attributes
| Attribute | Type | Values | Constraint | Priority |
|-----------|------|--------|------------|----------|
| **warehouseSize** | String | Size enums | Valid size | HIGH |
| **autoSuspend** | Integer | 0, NULL, ≥60 | Special validation | HIGH |
| **autoResume** | Boolean | true/false | None | HIGH |
| **minClusterCount** | Integer | 1-10 | ≤ maxClusterCount | MEDIUM |
| **maxClusterCount** | Integer | 1-10 | ≥ minClusterCount | MEDIUM |
| **scalingPolicy** | String | STANDARD/ECONOMY | Enterprise Edition | MEDIUM |
| **resourceMonitor** | String | Valid monitor | Must exist | MEDIUM |
| **comment** | String | ≤256 chars | Length limit | LOW |
| **enableQueryAcceleration** | Boolean | true/false | Enterprise Edition | LOW |
| **queryAccelerationMaxScaleFactor** | Integer | 0-100 | Requires acceleration enabled | LOW |
| **statementQueuedTimeoutInSeconds** | Integer | Positive | Query management | MEDIUM |
| **statementTimeoutInSeconds** | Integer | Positive | Query management | MEDIUM |
| **maxConcurrencyLevel** | Integer | Positive | Concurrency control | MEDIUM |
| **warehouseType** | String | STANDARD/SNOWPARK-OPTIMIZED | Architecture type | MEDIUM |

### Group 3: UNSET Property Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **unsetResourceMonitor** | Boolean | ❌ | true/false | Remove monitor assignment |
| **unsetComment** | Boolean | ❌ | true/false | Remove comment |

### Group 4-6: State Management Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **suspend** | Boolean | ❌ | true/false | Suspend warehouse |
| **resume** | Boolean | ❌ | true/false | Resume warehouse |
| **resumeIfSuspended** | Boolean | ❌ | true/false | Conditional resume |
| **abortAllQueries** | Boolean | ❌ | true/false | Terminate all queries |

### Mutual Exclusivity Rules
```yaml
OPERATION_EXCLUSIVITY: "Cannot combine any operation groups together"
CLUSTER_CONSTRAINT: "MIN_CLUSTER_COUNT <= MAX_CLUSTER_COUNT"
AUTO_SUSPEND_RULES: "0 (disabled), NULL (never), or >= 60 seconds"
QUERY_ACCELERATION_DEPENDENCY: "Scale factor requires acceleration enabled"
SET_UNSET_EXCLUSIVITY: "Cannot SET and UNSET same property"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### RENAME Examples
```sql
-- Basic rename
ALTER WAREHOUSE old_warehouse RENAME TO new_warehouse;

-- Safe rename with IF EXISTS
ALTER WAREHOUSE IF EXISTS old_warehouse RENAME TO new_warehouse;
```

### SET PROPERTY Examples
```sql
-- Basic configuration
ALTER WAREHOUSE my_warehouse SET
  WAREHOUSE_SIZE = LARGE
  AUTO_SUSPEND = 300
  AUTO_RESUME = TRUE
  COMMENT = 'Production ETL warehouse';

-- Multi-cluster configuration (Enterprise Edition)
ALTER WAREHOUSE enterprise_warehouse SET
  MIN_CLUSTER_COUNT = 2
  MAX_CLUSTER_COUNT = 5
  SCALING_POLICY = STANDARD
  ENABLE_QUERY_ACCELERATION = TRUE
  QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8;

-- Query management
ALTER WAREHOUSE query_warehouse SET
  STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = 120
  STATEMENT_TIMEOUT_IN_SECONDS = 3600
  MAX_CONCURRENCY_LEVEL = 10;

-- Auto-suspend variations
ALTER WAREHOUSE never_suspend_warehouse SET AUTO_SUSPEND = NULL;
ALTER WAREHOUSE disabled_suspend_warehouse SET AUTO_SUSPEND = 0;
ALTER WAREHOUSE quick_suspend_warehouse SET AUTO_SUSPEND = 60;
```

### UNSET PROPERTY Examples
```sql
-- Remove properties
ALTER WAREHOUSE my_warehouse UNSET
  RESOURCE_MONITOR
  COMMENT;
```

### STATE MANAGEMENT Examples
```sql
-- Suspend warehouse
ALTER WAREHOUSE IF EXISTS operational_warehouse SUSPEND;

-- Resume warehouse
ALTER WAREHOUSE operational_warehouse RESUME;

-- Conditional resume
ALTER WAREHOUSE operational_warehouse RESUME IF SUSPENDED;

-- Abort all queries
ALTER WAREHOUSE heavy_workload_warehouse ABORT ALL QUERIES;
```

### Constraint Examples
```sql
-- Valid: min <= max clusters
ALTER WAREHOUSE warehouse SET
  MIN_CLUSTER_COUNT = 2
  MAX_CLUSTER_COUNT = 5;

-- Invalid: min > max clusters (will fail)
ALTER WAREHOUSE warehouse SET
  MIN_CLUSTER_COUNT = 5
  MAX_CLUSTER_COUNT = 2;

-- Invalid: auto-suspend < 60 and not 0/NULL (will fail)
ALTER WAREHOUSE warehouse SET AUTO_SUSPEND = 30;

-- Invalid: query acceleration scale without enabling (will fail)
ALTER WAREHOUSE warehouse SET
  ENABLE_QUERY_ACCELERATION = FALSE
  QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8;
```

### Validation Points
```yaml
RENAME_VALIDATION: "Warehouse renamed, grants preserved, old name gone"
PROPERTY_VALIDATION: "DESCRIBE WAREHOUSE shows updated values"
STATE_VALIDATION: "Warehouse state changed as expected"
CLUSTER_VALIDATION: "Multi-cluster settings applied correctly"
QUERY_VALIDATION: "All queries terminated for ABORT operation"
CONSTRAINT_VALIDATION: "Cluster count and auto-suspend rules enforced"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
RENAME_TESTS:
  - "Basic rename: old_warehouse → new_warehouse"
  - "IF EXISTS: existing warehouse rename"
  - "IF EXISTS: non-existing warehouse (no error)"
  - "Rename to existing name (error validation)"
  - "Mutual exclusivity: rename + properties (error)"

PROPERTY_TESTS:
  - "Single property SET: warehouse size"
  - "Multiple properties SET: size + auto-suspend + comment"
  - "Multi-cluster configuration: min/max counts + scaling"
  - "Query acceleration: enable + scale factor"
  - "Auto-suspend variations: 0, NULL, >= 60"
  - "Cluster count constraints: min <= max"
  - "Query acceleration dependency: scale requires enable"
  - "SET with IF EXISTS protection"
  - "UNSET single/multiple properties"
  - "Invalid auto-suspend values (error)"

STATE_TESTS:
  - "Basic SUSPEND operation"
  - "Basic RESUME operation"
  - "RESUME IF SUSPENDED conditional"
  - "SUSPEND + RESUME together (error)"
  - "State operations with IF EXISTS"

QUERY_TESTS:
  - "ABORT ALL QUERIES operation"
  - "ABORT with other operations (error)"
  - "Query termination verification"

VALIDATION_TESTS:
  - "Required warehouseName validation"
  - "Cluster count constraints (1-10, min <= max)"
  - "Auto-suspend validation (0, NULL, >= 60)"
  - "Warehouse size enum validation"
  - "Query acceleration dependency validation"
  - "Operation mutual exclusivity enforcement"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Warehouse lifecycle: create → configure → suspend → resume → cleanup"
  - "Size scaling: small → large → verify restart behavior"
  - "Multi-cluster: enable → test scaling → disable"
  - "Query management: set limits → test behavior → reset"
  - "State transitions: suspend → resume → abort queries"

PERFORMANCE_TESTS:
  - "Large warehouse configuration changes"
  - "Multi-cluster scaling behavior"
  - "Query termination timing"
```

### Test File Organization
```yaml
ALTER_WAREHOUSE_TESTS:
  - "AlterWarehouseRenameTest.java: RENAME operations"
  - "AlterWarehouseSetPropertiesTest.java: SET operations"
  - "AlterWarehouseUnsetPropertiesTest.java: UNSET operations"
  - "AlterWarehouseStateTest.java: SUSPEND/RESUME operations"
  - "AlterWarehouseAdminTest.java: ABORT operations"
  - "AlterWarehouseValidationTest.java: Constraint validation"
  - "AlterWarehouseMutualExclusivityTest.java: Operation exclusivity"
  - "alterWarehouse_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "Operation type detection (RENAME/SET/UNSET/SUSPEND/RESUME/ABORT)"
    - "Mutual exclusivity validation between operation groups"
    - "SQL generation for each operation type"
    - "Complex constraint validation (clusters, auto-suspend)"
    
  TEST_STRUCTURE:
    - "AlterWarehouseRenameTest: RENAME operations"
    - "AlterWarehouseSetPropertiesTest: SET operations"
    - "AlterWarehouseStateTest: State management"
    - "AlterWarehouseValidationTest: Constraints"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeAlterWarehouseChange extends AbstractChange"
    - "Operation type detection logic"
    - "Separate SQL generation for each operation type"
    - "Comprehensive validation with clear error messages"

REFACTOR_PHASE:
  IMPROVEMENTS:
    - "Extract operation-specific helper methods"
    - "Optimize SQL generation templates"
    - "Enhance constraint validation logic"
    - "Performance optimization"
```

### Pattern Implementation
```yaml
PATTERN: "New Changetype"
REASON: "ALTER WAREHOUSE doesn't exist in core Liquibase"
KEY_POINTS:
  - "Operation detection: determine active operation group"
  - "Mutual exclusivity: validate single operation group"
  - "SQL generation: operation-specific templates"
  - "Enterprise features: handle multi-cluster and query acceleration"
```

### Service Registration
```java
// SnowflakeAlterWarehouseChange
public class SnowflakeAlterWarehouseChange extends AbstractChange {
    private String warehouseName;
    private Boolean ifExists;
    private String newWarehouseName;  // RENAME
    // SET properties
    private String warehouseSize;
    private Integer autoSuspend;
    private Boolean autoResume;
    private Integer minClusterCount;
    private Integer maxClusterCount;
    // UNSET properties
    private Boolean unsetResourceMonitor;
    private Boolean unsetComment;
    // STATE operations
    private Boolean suspend;
    private Boolean resume;
    private Boolean resumeIfSuspended;
    private Boolean abortAllQueries;
    // ... other attributes
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new AlterWarehouseStatement(this)
        };
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "Core operations (RENAME, basic SET properties, state management)"
MEDIUM_PRIORITY: "Multi-cluster features, query management"
LOW_PRIORITY: "Enterprise features (query acceleration, advanced properties)"
```

## ✅ IMPLEMENTATION STATUS

### Requirements Quality
```yaml
COMPLETENESS: "✅ All 6 operation types, attributes, constraints documented"
SQL_SYNTAX: "✅ Complete with all variations and examples"
VALIDATION: "✅ Mutual exclusivity and constraints defined"
TEST_COVERAGE: "✅ Comprehensive scenarios for all operations"
IMPLEMENTATION_GUIDE: "✅ TDD approach with clear phases"
```

### Readiness Assessment
```yaml
TECHNICAL_READINESS: "✅ READY - All attributes and SQL syntax documented"
COMPLEXITY: "✅ MANAGEABLE - High complexity, well-defined patterns"
RISK_LEVEL: "✅ MEDIUM - Complex validation, comprehensive documentation"
IMPLEMENTATION_TIME: "6-8 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeAlterWarehouseChange class"
  - "Operation type detection logic"
  - "Mutual exclusivity validation"
  - "SQL generation for all operation types"
  - "Enterprise Edition feature support"

TEST_SUITE:
  - "35+ unit tests covering all scenarios"
  - "Integration tests for complex workflows"
  - "Error condition validation"
  - "Performance verification"

SUCCESS_CRITERIA:
  - "100% test coverage"
  - "Real Snowflake behavior validation"
  - "Clear error messages"
  - "Performance standards met"
```

### Implementation Timeline
```yaml
PHASE_BREAKDOWN:
  - "Setup and planning: 1 hour"
  - "Core implementation: 3-4 hours"
  - "Enterprise features: 1-2 hours"
  - "Testing and validation: 2 hours"
  - "Documentation: 1 hour"
  
TOTAL_ESTIMATE: "6-8 hours"
PRIORITY: "HIGH - Core warehouse management"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*