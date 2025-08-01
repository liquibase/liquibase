# Snowflake CreateWarehouse Requirements Document
## Implementation-Ready Requirements for CreateWarehouse Changetype Development

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "PHASE_2_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "New_Changetype"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Warehouse"
OPERATION: "CREATE"
NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
ESTIMATED_IMPLEMENTATION_TIME: "6-8 hours"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "Complete CREATE WAREHOUSE changetype supporting all Snowflake warehouse creation operations"
KEY_OPERATIONS:
  - "Basic warehouse creation with size and type configuration"
  - "OR REPLACE and IF NOT EXISTS conditional operations"
  - "Multi-cluster warehouse configuration (Enterprise Edition)"
  - "Query acceleration and resource constraint settings"
  - "Auto-suspend/resume and resource monitoring configuration"
COMPLEXITY_ASSESSMENT: "HIGH - Complex parameter interactions, Enterprise Edition dependencies, and resource constraint validation"
SUCCESS_CRITERIA: "All warehouse creation scenarios implemented with comprehensive validation and Enterprise Edition feature support"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/create-warehouse
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/user-guide/warehouses-overview (for warehouse concepts)

### Documentation Analysis and Key Insights
- **Complex Parameter Set**: CREATE WAREHOUSE supports extensive configuration options
- **Mutual Exclusivity**: OR REPLACE and IF NOT EXISTS cannot be combined
- **Enterprise Dependencies**: Multi-cluster and query acceleration require Enterprise Edition
- **Resource Constraints**: New feature with compatibility requirements across warehouse types
- **Cost Implications**: Many parameters directly impact billing and performance

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake CREATE WAREHOUSE Syntax
```sql
-- Minimal syntax
CREATE WAREHOUSE warehouse_name;

-- Full syntax with all options
CREATE [ OR REPLACE ] WAREHOUSE [ IF NOT EXISTS ] <name>
  [ WITH ]
  [ WAREHOUSE_TYPE = { STANDARD | SNOWPARK-OPTIMIZED } ]
  [ WAREHOUSE_SIZE = { XSMALL | SMALL | MEDIUM | LARGE | XLARGE | XXLARGE | XXXLARGE | X4LARGE | X5LARGE | X6LARGE } ]
  [ RESOURCE_CONSTRAINT = { STANDARD_GEN_1 | STANDARD_GEN_2 | MEMORY_1X | MEMORY_1X_x86 | MEMORY_16X | MEMORY_16X_x86 | MEMORY_64X | MEMORY_64X_x86 } ]
  [ MAX_CLUSTER_COUNT = <num> ]
  [ MIN_CLUSTER_COUNT = <num> ]
  [ SCALING_POLICY = { STANDARD | ECONOMY } ]
  [ AUTO_SUSPEND = <num> | NULL ]
  [ AUTO_RESUME = { TRUE | FALSE } ]
  [ INITIALLY_SUSPENDED = { TRUE | FALSE } ]
  [ RESOURCE_MONITOR = <monitor_name> ]
  [ COMMENT = '<string_literal>' ]
  [ ENABLE_QUERY_ACCELERATION = { TRUE | FALSE } ]
  [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
  [ MAX_CONCURRENCY_LEVEL = <num> ]
  [ STATEMENT_QUEUED_TIMEOUT_IN_SECONDS = <num> ]
  [ STATEMENT_TIMEOUT_IN_SECONDS = <num> ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [, ...] ) ];
```

### Parameter Dependencies and Constraints
- **Mutual Exclusivity**: OR REPLACE and IF NOT EXISTS cannot be combined
- **Enterprise Dependencies**: Multi-cluster and query acceleration require Enterprise Edition
- **Resource Constraint Compatibility**: MEMORY_* constraints only valid with SNOWPARK-OPTIMIZED warehouses
- **Cluster Count Relationships**: MIN_CLUSTER_COUNT must be ≤ MAX_CLUSTER_COUNT
- **Auto-Suspend Values**: Must be 0 (disabled), NULL (never), or ≥ 60 seconds

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| warehouseName | Target warehouse name for creation | String | Required | - | Valid Snowflake identifier | Cannot be null/empty | None | HIGH | Case-insensitive, auto-converted to uppercase |
| orReplace | Replace existing warehouse | Boolean | Optional | false | true/false | Cannot combine with ifNotExists | Mutually exclusive with ifNotExists | HIGH | Drops existing warehouse completely |
| ifNotExists | Create only if doesn't exist | Boolean | Optional | false | true/false | Cannot combine with orReplace | Mutually exclusive with orReplace | HIGH | Idempotent operation support |
| warehouseType | Type of warehouse | String | Optional | STANDARD | STANDARD, SNOWPARK-OPTIMIZED | Must be valid enum | None | MEDIUM | Affects resource constraint compatibility |
| warehouseSize | Size of warehouse | String | Optional | XSMALL | XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE | Must be valid size enum | None | HIGH | Directly impacts cost and performance |
| resourceConstraint | Resource constraint for warehouse | String | Optional | varies by provider | STANDARD_GEN_1, STANDARD_GEN_2, MEMORY_1X, MEMORY_1X_x86, MEMORY_16X, MEMORY_16X_x86, MEMORY_64X, MEMORY_64X_x86 | Must be compatible with warehouse type | Type-dependent constraints | MEDIUM | GA feature as of March 2025 |
| maxClusterCount | Maximum clusters for multi-cluster | Integer | Optional | 1 | 1-10 | Must be ≥ minClusterCount, requires Enterprise Edition | None | MEDIUM | Enterprise Edition feature |
| minClusterCount | Minimum clusters for multi-cluster | Integer | Optional | 1 | 1-10 | Must be ≤ maxClusterCount, requires Enterprise Edition | None | MEDIUM | Enterprise Edition feature |
| scalingPolicy | How to scale clusters | String | Optional | STANDARD | STANDARD, ECONOMY | Requires Enterprise Edition for multi-cluster | None | MEDIUM | Enterprise Edition feature |
| autoSuspend | Seconds before auto-suspend | Integer | Optional | 600 | 0, NULL, or ≥60 | Special validation for 0/NULL values | None | HIGH | 0=disabled, NULL=never, impacts cost |
| autoResume | Auto-resume when queried | Boolean | Optional | true | true/false | None | None | HIGH | Common warehouse setting |
| initiallySuspended | Start in suspended state | Boolean | Optional | false | true/false | None | None | MEDIUM | Cost optimization feature |
| resourceMonitor | Resource monitor to assign | String | Optional | null | Valid monitor name | Monitor must exist | None | MEDIUM | Requires monitor existence validation |
| comment | Description of warehouse | String | Optional | null | String ≤ 256 chars | Length validation | None | LOW | Metadata only |
| enableQueryAcceleration | Enable query acceleration service | Boolean | Optional | false | true/false | Requires Enterprise Edition | None | LOW | Enterprise Edition feature |
| queryAccelerationMaxScaleFactor | Max scale factor for acceleration | Integer | Optional | 8 | 0-100 | Only valid if enableQueryAcceleration=true | Requires enableQueryAcceleration=true | LOW | Conditional feature dependency |
| maxConcurrencyLevel | Maximum concurrent queries | Integer | Optional | varies by size | Positive integer | Size-dependent defaults | None | LOW | Performance tuning parameter |
| statementQueuedTimeoutInSeconds | Timeout for queued statements | Integer | Optional | null | Positive integer | None | None | LOW | Performance tuning parameter |
| statementTimeoutInSeconds | Timeout for statement execution | Integer | Optional | null | Positive integer | None | None | LOW | Performance tuning parameter |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE WAREHOUSE` - Valid
   - `CREATE WAREHOUSE IF NOT EXISTS` - Valid
   - `CREATE OR REPLACE WAREHOUSE IF NOT EXISTS` - Invalid

### Resource Constraint Compatibility
1. **STANDARD warehouses**: Only STANDARD_GEN_1, STANDARD_GEN_2 allowed
2. **SNOWPARK-OPTIMIZED warehouses**: Only MEMORY_* variants allowed
3. **Size constraints**: X5LARGE and X6LARGE only supported with MEMORY_16X constraint

### Enterprise Edition Requirements
1. **Multi-cluster features**: maxClusterCount > 1, minClusterCount > 1, scalingPolicy require Enterprise Edition
2. **Query acceleration**: enableQueryAcceleration, queryAccelerationMaxScaleFactor require Enterprise Edition

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Warehouse Creation
```sql
-- Simple warehouse with default settings
CREATE WAREHOUSE basic_warehouse;
```
**Expected Behavior**: Warehouse created with XSMALL size, single cluster, 600s auto-suspend
**Test Validation**: Verify warehouse exists with default properties in system views

### Example 2: Sized Warehouse with Auto-Suspend Configuration
```sql
-- Warehouse with specific size and auto-suspend settings
CREATE WAREHOUSE sized_warehouse
  WITH WAREHOUSE_SIZE = MEDIUM
  AUTO_SUSPEND = 300
  AUTO_RESUME = TRUE
  COMMENT = 'Medium warehouse with 5-minute auto-suspend';
```
**Expected Behavior**: MEDIUM warehouse created with 300-second auto-suspend, auto-resume enabled
**Test Validation**: Verify size, auto-suspend, and auto-resume settings in system views

### Example 3: Multi-Cluster Warehouse (Enterprise Edition)
```sql
-- Multi-cluster warehouse with scaling policy
CREATE WAREHOUSE multi_cluster_warehouse
  WITH WAREHOUSE_SIZE = LARGE
  MIN_CLUSTER_COUNT = 1
  MAX_CLUSTER_COUNT = 3
  SCALING_POLICY = STANDARD
  COMMENT = 'Multi-cluster warehouse for variable workloads';
```
**Expected Behavior**: Multi-cluster warehouse created with scaling capabilities
**Test Validation**: Verify cluster count settings and scaling policy configuration

### Example 4: Initially Suspended Warehouse for Cost Control
```sql
-- Warehouse that starts suspended to minimize costs
CREATE WAREHOUSE suspended_warehouse
  WITH WAREHOUSE_SIZE = XLARGE
  INITIALLY_SUSPENDED = TRUE
  AUTO_SUSPEND = 60
  COMMENT = 'Start suspended to save costs until first use';
```
**Expected Behavior**: Warehouse created in suspended state, ready for on-demand activation
**Test Validation**: Verify warehouse state is SUSPENDED after creation

### Example 5: Warehouse with Resource Monitor Assignment
```sql
-- Warehouse with cost monitoring
CREATE WAREHOUSE monitored_warehouse
  WITH WAREHOUSE_SIZE = SMALL
  RESOURCE_MONITOR = monthly_budget_monitor
  AUTO_SUSPEND = 120
  COMMENT = 'Cost-controlled warehouse with budget monitoring';
```
**Expected Behavior**: Warehouse created with resource monitor for cost control
**Test Validation**: Verify resource monitor assignment and cost tracking

### Example 6: Query Acceleration Warehouse (Enterprise Edition)
```sql
-- High-performance warehouse with query acceleration
CREATE WAREHOUSE accelerated_warehouse
  WITH WAREHOUSE_SIZE = XLARGE
  ENABLE_QUERY_ACCELERATION = TRUE
  QUERY_ACCELERATION_MAX_SCALE_FACTOR = 10
  MAX_CONCURRENCY_LEVEL = 16
  COMMENT = 'High-performance warehouse with query acceleration';
```
**Expected Behavior**: Warehouse created with query acceleration service enabled
**Test Validation**: Verify query acceleration settings and performance configurations

### Example 7: Snowpark-Optimized Warehouse with Memory Constraint
```sql
-- Warehouse optimized for Snowpark workloads
CREATE WAREHOUSE snowpark_warehouse
  WITH WAREHOUSE_TYPE = SNOWPARK-OPTIMIZED
  WAREHOUSE_SIZE = LARGE
  RESOURCE_CONSTRAINT = MEMORY_16X
  AUTO_SUSPEND = 0
  COMMENT = 'Always-on Snowpark-optimized warehouse with enhanced memory';
```
**Expected Behavior**: Snowpark-optimized warehouse with memory constraint and no auto-suspend
**Test Validation**: Verify warehouse type, resource constraint, and auto-suspend disabled

### Example 8: Conditional Creation with OR REPLACE
```sql
-- Replace existing warehouse with new configuration
CREATE OR REPLACE WAREHOUSE replacement_warehouse
  WITH WAREHOUSE_SIZE = XXLARGE
  AUTO_SUSPEND = NULL
  AUTO_RESUME = FALSE
  COMMENT = 'Replaced warehouse - never suspends, manual resume only';
```
**Expected Behavior**: Existing warehouse replaced with new configuration, never auto-suspends
**Test Validation**: Verify warehouse replacement and never-suspend configuration

### Example 9: IF NOT EXISTS for Idempotent Operations
```sql
-- Create warehouse only if it doesn't exist
CREATE WAREHOUSE IF NOT EXISTS conditional_warehouse
  WITH WAREHOUSE_SIZE = MEDIUM
  INITIALLY_SUSPENDED = FALSE
  COMMENT = 'Created only if not already present';
```
**Expected Behavior**: Warehouse created if missing, no action if exists
**Test Validation**: Verify idempotent behavior and no modification of existing warehouse

### Example 10: Error Case - Mutual Exclusivity Violation
```sql
-- Invalid: Cannot use both OR REPLACE and IF NOT EXISTS
CREATE OR REPLACE WAREHOUSE IF NOT EXISTS invalid_warehouse;
```
**Expected Behavior**: Error thrown about mutual exclusivity violation
**Test Validation**: Verify exact error message matches Snowflake behavior

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: Basic CREATE WAREHOUSE Operations
**Test File**: `CreateWarehouseBasicTest.java`
- **Test 1.1**: Basic warehouse creation without additional options
- **Test 1.2**: Warehouse creation with size specification
- **Test 1.3**: Warehouse creation with comment
- **Test 1.4**: Validation: Warehouse name cannot be null/empty
- **Test 1.5**: SQL Generation: Verify exact SQL format for basic creation
- **Test 1.6**: SQL Generation: Verify proper identifier handling

#### Scenario Group 2: OR REPLACE Operations
**Test File**: `CreateWarehouseOrReplaceTest.java`
- **Test 2.1**: OR REPLACE warehouse creation
- **Test 2.2**: OR REPLACE with comprehensive configuration
- **Test 2.3**: OR REPLACE with resource monitoring
- **Test 2.4**: Validation: Cannot combine with IF NOT EXISTS
- **Test 2.5**: SQL Generation: Verify exact SQL format for OR REPLACE

#### Scenario Group 3: IF NOT EXISTS Operations
**Test File**: `CreateWarehouseIfNotExistsTest.java`
- **Test 3.1**: IF NOT EXISTS warehouse creation
- **Test 3.2**: IF NOT EXISTS with configuration options
- **Test 3.3**: Validation: Cannot combine with OR REPLACE
- **Test 3.4**: SQL Generation: Verify exact SQL format for IF NOT EXISTS

#### Scenario Group 4: Warehouse Type and Size Operations
**Test File**: `CreateWarehouseTypeSizeTest.java`
- **Test 4.1**: STANDARD warehouse type creation
- **Test 4.2**: SNOWPARK-OPTIMIZED warehouse type creation
- **Test 4.3**: All warehouse sizes (XSMALL through X6LARGE)
- **Test 4.4**: Validation: Invalid warehouse type (error)
- **Test 4.5**: Validation: Invalid warehouse size (error)
- **Test 4.6**: SQL Generation: Verify type and size SQL format

#### Scenario Group 5: Resource Constraint Operations
**Test File**: `CreateWarehouseResourceConstraintTest.java`
- **Test 5.1**: STANDARD warehouse with STANDARD_GEN_1 constraint
- **Test 5.2**: STANDARD warehouse with STANDARD_GEN_2 constraint
- **Test 5.3**: SNOWPARK-OPTIMIZED with MEMORY_16X constraint
- **Test 5.4**: X5LARGE/X6LARGE with MEMORY_16X constraint requirement
- **Test 5.5**: Validation: MEMORY_* constraint with STANDARD warehouse (error)
- **Test 5.6**: Validation: STANDARD_GEN_* constraint with SNOWPARK-OPTIMIZED warehouse (error)
- **Test 5.7**: SQL Generation: Verify resource constraint SQL format

#### Scenario Group 6: Multi-Cluster Operations (Enterprise Edition)
**Test File**: `CreateWarehouseMultiClusterTest.java`
- **Test 6.1**: Single cluster warehouse (default)
- **Test 6.2**: Multi-cluster with min/max cluster counts
- **Test 6.3**: Scaling policy configuration (STANDARD, ECONOMY)
- **Test 6.4**: Validation: minClusterCount ≤ maxClusterCount constraint
- **Test 6.5**: Validation: Cluster count range validation (1-10)
- **Test 6.6**: Edition requirement validation (Enterprise Edition)
- **Test 6.7**: SQL Generation: Verify multi-cluster SQL format

#### Scenario Group 7: Auto-Suspend and Resume Operations
**Test File**: `CreateWarehouseAutoSuspendResumeTest.java`
- **Test 7.1**: Default auto-suspend behavior (600 seconds)
- **Test 7.2**: Custom auto-suspend timeout (≥60 seconds)
- **Test 7.3**: Auto-suspend disabled (0 value)
- **Test 7.4**: Never auto-suspend (NULL value)
- **Test 7.5**: Auto-resume enabled/disabled
- **Test 7.6**: Initially suspended warehouse creation
- **Test 7.7**: Validation: Auto-suspend timeout constraints
- **Test 7.8**: SQL Generation: Verify auto-suspend/resume SQL format

#### Scenario Group 8: Query Acceleration Operations (Enterprise Edition)
**Test File**: `CreateWarehouseQueryAccelerationTest.java`
- **Test 8.1**: Query acceleration disabled (default)
- **Test 8.2**: Query acceleration enabled with scale factor
- **Test 8.3**: Maximum scale factor validation (0-100)
- **Test 8.4**: Validation: Scale factor only valid when acceleration enabled
- **Test 8.5**: Edition requirement validation (Enterprise Edition)
- **Test 8.6**: SQL Generation: Verify query acceleration SQL format

#### Scenario Group 9: Resource Monitor and Performance Settings
**Test File**: `CreateWarehouseResourceMonitorTest.java`
- **Test 9.1**: Warehouse without resource monitor
- **Test 9.2**: Warehouse with resource monitor assignment
- **Test 9.3**: Max concurrency level configuration
- **Test 9.4**: Statement timeout configurations
- **Test 9.5**: Validation: Resource monitor existence (if applicable)
- **Test 9.6**: SQL Generation: Verify resource monitor and performance SQL format

#### Scenario Group 10: Error Conditions and Edge Cases
**Test File**: `CreateWarehouseErrorConditionsTest.java`
- **Test 10.1**: Warehouse already exists without OR REPLACE/IF NOT EXISTS (error)
- **Test 10.2**: Invalid warehouse name format (error)
- **Test 10.3**: Invalid parameter combinations (error)
- **Test 10.4**: Mutual exclusivity violations (error)
- **Test 10.5**: Enterprise Edition feature validation (error)
- **Test 10.6**: Resource constraint compatibility validation (error)
- **Test 10.7**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `createWarehouse_integration_test.xml`
- **Integration 1**: End-to-end warehouse creation with all feature combinations
- **Integration 2**: OR REPLACE behavior with existing warehouse
- **Integration 3**: Multi-cluster warehouse creation (if Enterprise Edition available)
- **Integration 4**: Error handling with invalid configurations
- **Integration 5**: Resource constraint and warehouse type compatibility testing

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic warehouse name validation and processing"
    - "OR REPLACE and IF NOT EXISTS mutual exclusivity validation"
    - "Warehouse type and size enumeration validation"
    - "SQL generation for all warehouse configurations"
    
  MEDIUM_PRIORITY_TESTS:
    - "Resource constraint compatibility validation"
    - "Multi-cluster configuration and Enterprise Edition requirements"
    - "Auto-suspend/resume parameter validation"
    - "Query acceleration dependency validation"
    
  LOW_PRIORITY_TESTS:
    - "Performance parameter edge cases"
    - "Complex configuration combinations"
    - "Enterprise Edition feature detection"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "Parameter validation with conditional SQL generation"
  VALIDATION_STRATEGY: "Early validation of mutual exclusivity, constraints, and edition requirements"
  SQL_GENERATION_STRATEGY: "Template-based with conditional clause generation for optional parameters"

REFACTOR_PHASE_FOCUS:
  - "Extract common warehouse parameter validation logic"
  - "Optimize SQL generation templates for performance"
  - "Improve Enterprise Edition feature detection"
  - "Standardize error message formatting"
```

### Implementation Pattern Selection
**PATTERN**: New Changetype Pattern
**RATIONALE**: CREATE WAREHOUSE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: High - Complex parameter interactions, Enterprise Edition dependencies, and resource constraint validation

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.change.Change → CreateWarehouseChange"
  - "liquibase.statement.SqlStatement → CreateWarehouseStatement"
  - "liquibase.sqlgenerator.SqlGenerator → CreateWarehouseSqlGenerator"

REQUIRED_XSD_ELEMENTS:
  - "createWarehouse element with all warehouse attributes"
  - "Complex type definitions for warehouse configuration"
  - "Validation annotations for parameter constraints and compatibility"
```

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 10 comprehensive SQL examples provided
- [✓] Test scenario matrix covering all feature combinations
- [✓] Implementation guidance with TDD approach specified
- [✓] Mutual exclusivity rules clearly documented
- [✓] Error conditions and validation requirements specified
- [✓] Enterprise Edition dependencies clearly documented

### Implementation Readiness Assessment
- [✓] New changetype pattern clearly defined
- [✓] Complex parameter validation rules specified with exact constraints
- [✓] SQL generation requirements clear for all configurations
- [✓] Test scenarios comprehensive and actionable
- [✓] Implementation complexity acknowledged and planned for
- [✓] Enterprise Edition feature dependencies documented
- [✓] Resource constraint compatibility rules specified

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale

## 9. Implementation Notes

- Warehouse names are automatically converted to uppercase unless quoted
- Resource constraints became generally available in March 2025
- Enterprise Edition features require proper license validation
- Multi-cluster warehouses can scale automatically based on workload
- Query acceleration is a separate service with additional costs
- Auto-suspend settings directly impact billing and cost optimization
- SNOWPARK-OPTIMIZED warehouses have different resource constraint options
- Resource monitors provide cost control and usage tracking capabilities