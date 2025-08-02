# Snowflake AlterWarehouse Requirements Document
## Implementation-Ready Requirements for AlterWarehouse Changetype Development

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "4.0"
PHASE: "VALIDATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
VALIDATION_COMPLETION_DATE: "2025-08-01"
VALIDATION_METHOD: "Simple INFORMATION_SCHEMA + Manual Documentation Review"
COMPLETENESS_VALIDATION: "COMPLETE - XSD parameters validated via SnowflakeParameterValidationTest"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "New_Changetype"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Warehouse"
OPERATION: "ALTER"
NEXT_PHASE: "Phase 3 - TDD Implementation"
ESTIMATED_IMPLEMENTATION_TIME: "6-8 hours (validation complete)"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "Complete ALTER WAREHOUSE changetype supporting all Snowflake warehouse modification operations"
KEY_OPERATIONS:
  - "RENAME TO operations with IF EXISTS support"
  - "SET property operations for size, clustering, auto-suspend/resume"
  - "UNSET property operations for resource monitor and comments"
  - "SUSPEND/RESUME state management operations"
  - "ABORT ALL QUERIES administrative operations"
COMPLEXITY_ASSESSMENT: "HIGH - Multiple mutually exclusive operation types requiring separate execution paths"
SUCCESS_CRITERIA: "All 7 operation types implemented with comprehensive validation and error handling"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/alter-warehouse
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/sql-reference/sql/create-warehouse (for property validation)

### Documentation Analysis and Key Insights
- **Multiple Operation Types**: ALTER WAREHOUSE supports 7 distinct operation types that are mutually exclusive
- **Complex Validation**: Property dependencies and constraints require careful validation (min/max cluster counts, auto-suspend values)
- **State-Dependent Operations**: Some operations (like RENAME) require specific warehouse states
- **Edition Dependencies**: Multi-cluster and query acceleration features require Enterprise Edition

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake ALTER WAREHOUSE Syntax
```sql
-- Operation Type 1: RENAME
ALTER WAREHOUSE [ IF EXISTS ] <name> RENAME TO <new_warehouse_name>;

-- Operation Type 2: SET Properties
ALTER WAREHOUSE [ IF EXISTS ] <name> SET
  [ WAREHOUSE_SIZE = { XSMALL | SMALL | MEDIUM | LARGE | XLARGE | XXLARGE | XXXLARGE | X4LARGE | X5LARGE | X6LARGE } ]
  [ MAX_CLUSTER_COUNT = <num> ]
  [ MIN_CLUSTER_COUNT = <num> ]
  [ SCALING_POLICY = { STANDARD | ECONOMY } ]
  [ AUTO_SUSPEND = <num> | NULL ]
  [ AUTO_RESUME = { TRUE | FALSE } ]
  [ RESOURCE_MONITOR = <monitor_name> ]
  [ COMMENT = '<string_literal>' ]
  [ ENABLE_QUERY_ACCELERATION = { TRUE | FALSE } ]
  [ QUERY_ACCELERATION_MAX_SCALE_FACTOR = <num> ]
  [ TAG <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ];

-- Operation Type 3: UNSET Properties
ALTER WAREHOUSE [ IF EXISTS ] <name> UNSET
  [ RESOURCE_MONITOR ]
  [ COMMENT ]
  [ TAG <tag_name> [ , <tag_name> , ... ] ];

-- Operation Type 4: SUSPEND
ALTER WAREHOUSE [ IF EXISTS ] <name> SUSPEND;

-- Operation Type 5: RESUME
ALTER WAREHOUSE [ IF EXISTS ] <name> RESUME [ IF SUSPENDED ];

-- Operation Type 6: ABORT ALL QUERIES
ALTER WAREHOUSE <name> ABORT ALL QUERIES;
```

### Parameter Dependencies and Constraints
- **Cluster Count Relationship**: MIN_CLUSTER_COUNT must be ≤ MAX_CLUSTER_COUNT
- **Auto-Suspend Values**: Must be 0 (disabled), NULL (never), or ≥ 60 seconds
- **Query Acceleration Dependency**: QUERY_ACCELERATION_MAX_SCALE_FACTOR only valid when ENABLE_QUERY_ACCELERATION = TRUE
- **Edition Requirements**: Multi-cluster and query acceleration require Enterprise Edition

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| warehouseName | Target warehouse name | String | Required | - | Valid Snowflake identifier | Cannot be null/empty | None | HIGH | Always required, case-insensitive |
| ifExists | Skip errors if not exists | Boolean | Optional | false | true/false | None | None | HIGH | Applies to all operation types |
| newWarehouseName | New warehouse name (rename) | String | Optional | null | Valid Snowflake identifier | Required for RENAME operations | Mutually exclusive with all SET operations | HIGH | Only used in RENAME operations |
| scalingPolicy | Scaling policy for multi-cluster | String | Optional | null | STANDARD, ECONOMY | Must be valid enum | Cannot combine with RENAME operations | MEDIUM | Enterprise Edition required |
| warehouseSize | Warehouse compute size | String | Optional | null | XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE | Must be valid size enum | Cannot combine with RENAME operations | HIGH | Triggers warehouse restart |
| minClusterCount | Minimum cluster count | Integer | Optional | null | 1-10 | Must be ≤ maxClusterCount | Cannot combine with RENAME operations | MEDIUM | Enterprise Edition required |
| resourceMonitor | Resource monitor assignment | String | Optional | null | Valid monitor name | Monitor must exist | Cannot combine with RENAME operations | MEDIUM | Requires monitor existence |
| queryAccelerationMaxScaleFactor | Query acceleration scale factor | Integer | Optional | null | 0-100 | Only valid if enableQueryAcceleration=true | Cannot combine with RENAME operations | LOW | Requires acceleration enabled |
| maxClusterCount | Maximum cluster count | Integer | Optional | null | 1-10 | Must be ≥ minClusterCount | Cannot combine with RENAME operations | MEDIUM | Enterprise Edition required |
| enableQueryAcceleration | Enable query acceleration service | Boolean | Optional | null | true/false | Enterprise Edition required | Cannot combine with RENAME operations | LOW | Enterprise feature |
| statementQueuedTimeoutInSeconds | Statement queue timeout | Integer | Optional | null | Positive integers | Must be > 0 | Cannot combine with RENAME operations | MEDIUM | Query management |
| statementTimeoutInSeconds | Statement execution timeout | Integer | Optional | null | Positive integers | Must be > 0 | Cannot combine with RENAME operations | MEDIUM | Query management |
| maxConcurrencyLevel | Maximum concurrent statements | Integer | Optional | null | Positive integers | Must be > 0 | Cannot combine with RENAME operations | MEDIUM | Concurrency control |
| autoResume | Auto-resume warehouse behavior | Boolean | Optional | null | true/false | None | Cannot combine with RENAME operations | HIGH | Common warehouse setting |
| newWarehouseName | New warehouse name for rename | String | Optional | null | Valid Snowflake identifier | Required for RENAME operations | Mutually exclusive with all SET operations | HIGH | Only used in RENAME operations |
| action | Warehouse action to perform | String | Optional | null | Various action types | Must be valid action | Operation type specification | HIGH | Action classification |
| comment | Warehouse comment | String | Optional | null | String ≤ 256 chars | Length validation | Cannot combine with RENAME operations | LOW | Metadata only |
| operationType | Type of ALTER operation | String | Optional | null | RENAME, SET, UNSET | Must match operation being performed | None | HIGH | Operation classification parameter |
| warehouseType | Type of warehouse | String | Optional | null | STANDARD, SNOWPARK-OPTIMIZED | Must be valid type | Cannot combine with RENAME operations | MEDIUM | Warehouse architecture type |
| warehouseTag | Warehouse tag assignment | String | Optional | null | Valid tag syntax | Must be valid tag format | Cannot combine with RENAME operations | LOW | Object tagging |
| autoSuspend | Auto-suspend timeout | Integer | Optional | null | 0, NULL, or ≥60 | Special validation rules | Cannot combine with RENAME operations | HIGH | 0=disabled, NULL=never |
| unsetResourceMonitor | Remove resource monitor assignment | Boolean | Optional | false | true/false | None | Cannot combine with RENAME operations | MEDIUM | UNSET operation for resource monitor |
| unsetComment | Remove warehouse comment | Boolean | Optional | false | true/false | None | Cannot combine with RENAME operations | LOW | UNSET operation for comment |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Operations
1. **Different operation types cannot be combined**:
   - RENAME TO
   - SET properties
   - UNSET properties
   - SUSPEND
   - RESUME
   - ABORT ALL QUERIES

2. **Within SET operations**:
   - minClusterCount must be <= maxClusterCount
   - queryAccelerationMaxScaleFactor only valid if enableQueryAcceleration = true

3. **SUSPEND vs RESUME**:
   - Cannot suspend and resume in same statement
   - These are separate operations

4. **SET vs UNSET**:
   - Cannot SET and UNSET same property

### Operation Groups
1. **Rename Operation**: Uses only warehouseName, newName, ifExists
2. **Property Operations**: Uses SET/UNSET attributes
3. **State Operations**: SUSPEND, RESUME
4. **Query Operations**: ABORT ALL QUERIES

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Warehouse Rename with IF EXISTS
```sql
-- Rename warehouse with error handling
ALTER WAREHOUSE IF EXISTS old_warehouse RENAME TO new_warehouse;
```
**Expected Behavior**: Renames warehouse if it exists, no error if it doesn't exist
**Test Validation**: Verify warehouse name change in system views, verify grants preserved

### Example 2: Complex Property SET Operation
```sql
-- Set multiple warehouse properties in single operation
ALTER WAREHOUSE my_warehouse SET
  WAREHOUSE_SIZE = LARGE
  AUTO_SUSPEND = 300
  AUTO_RESUME = TRUE
  COMMENT = 'Production warehouse for ETL operations';
```
**Expected Behavior**: All properties updated atomically, warehouse restarted if size changed
**Test Validation**: Verify all properties set correctly, verify restart behavior for size changes

### Example 3: Multi-Cluster Configuration (Enterprise Edition)
```sql
-- Configure multi-cluster warehouse with scaling policy
ALTER WAREHOUSE enterprise_warehouse SET
  MIN_CLUSTER_COUNT = 2
  MAX_CLUSTER_COUNT = 5
  SCALING_POLICY = STANDARD
  ENABLE_QUERY_ACCELERATION = TRUE
  QUERY_ACCELERATION_MAX_SCALE_FACTOR = 8;
```
**Expected Behavior**: Multi-cluster configuration applied, query acceleration enabled
**Test Validation**: Verify cluster count constraints, verify query acceleration settings

### Example 4: UNSET Operations for Property Removal
```sql
-- Remove resource monitor and comment  
ALTER WAREHOUSE my_warehouse UNSET
  RESOURCE_MONITOR
  COMMENT;
```
**Expected Behavior**: Resource monitor assignment removed, comment cleared
**Test Validation**: Verify properties are null/unset in system views

**XML Example with UNSET attributes**:
```xml
<alterWarehouse warehouseName="my_warehouse"
                unsetResourceMonitor="true"
                unsetComment="true"/>
```

### Example 5: Warehouse State Management - Suspend
```sql
-- Suspend warehouse with IF EXISTS protection
ALTER WAREHOUSE IF EXISTS operational_warehouse SUSPEND;
```
**Expected Behavior**: Warehouse suspended, running queries terminated, billing stopped
**Test Validation**: Verify warehouse state change, verify query termination

### Example 6: Conditional Resume Operation
```sql
-- Resume warehouse only if currently suspended
ALTER WAREHOUSE IF EXISTS operational_warehouse RESUME IF SUSPENDED;
```
**Expected Behavior**: Warehouse resumed only if suspended, no error if already running
**Test Validation**: Verify conditional logic, verify state change behavior

### Example 7: Administrative Query Termination
```sql
-- Abort all running queries on warehouse
ALTER WAREHOUSE heavy_workload_warehouse ABORT ALL QUERIES;
```
**Expected Behavior**: All running queries terminated, warehouse remains running
**Test Validation**: Verify query termination, verify warehouse continues accepting new queries

### Example 8: Edge Case - Auto-Suspend Disabled
```sql
-- Disable auto-suspend (set to 0)
ALTER WAREHOUSE always_on_warehouse SET
  AUTO_SUSPEND = 0
  AUTO_RESUME = FALSE;
```
**Expected Behavior**: Auto-suspend disabled, warehouse never automatically suspends
**Test Validation**: Verify auto-suspend behavior disabled

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: RENAME Operations
**Test File**: `AlterWarehouseRenameTest.java`
- **Test 1.1**: Basic rename operation without IF EXISTS
- **Test 1.2**: Rename with IF EXISTS when warehouse exists  
- **Test 1.3**: Rename with IF EXISTS when warehouse doesn't exist
- **Test 1.4**: Rename to existing warehouse name (error case)
- **Test 1.5**: Rename with quoted identifiers
- **Test 1.6**: Validation: newName cannot be null when rename requested
- **Test 1.7**: SQL Generation: Verify exact SQL format for all rename variations

#### Scenario Group 2: SET Property Operations  
**Test File**: `AlterWarehouseSetPropertiesTest.java`
- **Test 2.1**: Single property SET (warehouse size)
- **Test 2.2**: Multiple property SET operation (size + auto-suspend + comment)
- **Test 2.3**: Multi-cluster configuration (min/max cluster counts + scaling policy)
- **Test 2.4**: Query acceleration properties (enable + scale factor)
- **Test 2.5**: Resource monitor assignment
- **Test 2.6**: Validation: minClusterCount ≤ maxClusterCount constraint
- **Test 2.7**: Validation: autoSuspend value constraints (0, NULL, ≥60)
- **Test 2.8**: Validation: queryAccelerationMaxScaleFactor only valid when acceleration enabled
- **Test 2.9**: SQL Generation: Verify exact SQL format for all SET combinations

#### Scenario Group 3: UNSET Property Operations
**Test File**: `AlterWarehouseUnsetPropertiesTest.java`
- **Test 3.1**: UNSET single property (resource monitor)
- **Test 3.2**: UNSET multiple properties (resource monitor + comment)
- **Test 3.3**: UNSET with IF EXISTS protection
- **Test 3.4**: SQL Generation: Verify exact SQL format for UNSET operations

#### Scenario Group 4: State Management Operations
**Test File**: `AlterWarehouseStateTest.java`
- **Test 4.1**: Basic SUSPEND operation
- **Test 4.2**: SUSPEND with IF EXISTS protection
- **Test 4.3**: Basic RESUME operation
- **Test 4.4**: RESUME IF SUSPENDED conditional operation
- **Test 4.5**: Validation: Cannot combine SUSPEND and RESUME
- **Test 4.6**: SQL Generation: Verify exact SQL format for state operations

#### Scenario Group 5: Administrative Operations
**Test File**: `AlterWarehouseAdminTest.java`
- **Test 5.1**: ABORT ALL QUERIES operation
- **Test 5.2**: ABORT ALL QUERIES without IF EXISTS (note: not supported for abort)
- **Test 5.3**: SQL Generation: Verify exact SQL format for abort operation

#### Scenario Group 6: Mutual Exclusivity Validation
**Test File**: `AlterWarehouseMutualExclusivityTest.java`
- **Test 6.1**: Cannot combine RENAME with SET operations
- **Test 6.2**: Cannot combine RENAME with UNSET operations
- **Test 6.3**: Cannot combine RENAME with SUSPEND/RESUME operations
- **Test 6.4**: Cannot combine SET with UNSET operations
- **Test 6.5**: Cannot combine SET with SUSPEND/RESUME operations
- **Test 6.6**: Cannot combine any operations with ABORT ALL QUERIES
- **Test 6.7**: Validation: Error messages for invalid combinations

#### Scenario Group 7: Error Conditions and Edge Cases
**Test File**: `AlterWarehouseErrorConditionsTest.java`
- **Test 7.1**: Warehouse doesn't exist without IF EXISTS (error)
- **Test 7.2**: Invalid warehouse size value (error)
- **Test 7.3**: Invalid cluster count values (error)
- **Test 7.4**: Invalid auto-suspend value (error)
- **Test 7.5**: Query acceleration scale factor without acceleration enabled (error)
- **Test 7.6**: Empty/null warehouse name (error)
- **Test 7.7**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `alterWarehouse_integration_test.xml`
- **Integration 1**: End-to-end RENAME operation with database validation
- **Integration 2**: Complex SET operation with property verification
- **Integration 3**: SUSPEND/RESUME cycle with state verification
- **Integration 4**: Error handling with non-existent warehouses
- **Integration 5**: Multi-cluster configuration (if Enterprise Edition available)

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic operation type detection (RENAME/SET/UNSET/SUSPEND/RESUME/ABORT)"
    - "Parameter validation for all attributes"
    - "Mutual exclusivity validation between operation types"
    - "SQL generation for all operation types"
    
  MEDIUM_PRIORITY_TESTS:
    - "Complex property combinations and constraints"
    - "Enterprise Edition feature validation"
    - "Error condition handling and messaging"
    
  LOW_PRIORITY_TESTS:
    - "Edge cases and boundary conditions"
    - "Performance and optimization scenarios"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "Separate execution paths for each operation type"
  VALIDATION_STRATEGY: "Early validation of mutual exclusivity and constraints"
  SQL_GENERATION_STRATEGY: "Template-based generation with operation-specific logic"

REFACTOR_PHASE_FOCUS:
  - "Extract common validation logic"
  - "Optimize SQL generation templates"
  - "Improve error message consistency"
  - "Performance optimization for validation"
```

### Implementation Pattern Selection
**PATTERN**: New Changetype Pattern
**RATIONALE**: ALTER WAREHOUSE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: High - Multiple mutually exclusive operation types requiring sophisticated validation

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.change.Change → AlterWarehouseChange"
  - "liquibase.statement.SqlStatement → AlterWarehouseStatement"
  - "liquibase.sqlgenerator.SqlGenerator → AlterWarehouseSqlGenerator"

REQUIRED_XSD_ELEMENTS:
  - "alterWarehouse element with all operation attributes"
  - "Complex type definitions for operation exclusivity"
  - "Validation annotations for constraints"
```

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 8 comprehensive SQL examples provided
- [✓] Test scenario matrix covering all operation combinations
- [✓] Implementation guidance with TDD approach specified
- [✓] Mutual exclusivity rules clearly documented
- [✓] Error conditions and validation requirements specified

### Implementation Readiness Assessment
- [✓] All operation types clearly defined and separated
- [✓] Complex validation rules specified with exact constraints
- [✓] SQL generation requirements clear for all scenarios
- [✓] Test scenarios comprehensive and actionable
- [✓] Implementation complexity acknowledged and planned for

### Handoff to Phase 3 Validation  
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale