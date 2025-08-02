# Snowflake DropWarehouse Requirements Document
## Implementation-Ready Requirements for DropWarehouse Changetype Development

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
OPERATION: "DROP"
NEXT_PHASE: "Phase 3 - TDD Implementation"
ESTIMATED_IMPLEMENTATION_TIME: "3-4 hours (validation complete)"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "Complete DROP WAREHOUSE changetype supporting Snowflake warehouse deletion operations"
KEY_OPERATIONS:
  - "Basic DROP WAREHOUSE operation"
  - "IF EXISTS conditional dropping with error handling"
  - "Comprehensive validation and error messaging"
COMPLEXITY_ASSESSMENT: "LOW - Simple operation with minimal attributes and straightforward validation"
SUCCESS_CRITERIA: "Clean warehouse deletion with proper error handling and validation"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/drop-warehouse
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/sql-reference/sql/create-warehouse (for understanding warehouse lifecycle)

### Documentation Analysis and Key Insights
- **Simple Operation**: DROP WAREHOUSE is one of the simplest Snowflake DDL operations
- **No CASCADE Options**: Unlike schemas/databases, warehouses don't support CASCADE/RESTRICT
- **Immediate Effect**: Warehouse deletion is immediate and irreversible
- **Running Warehouse Handling**: Snowflake automatically suspends running warehouses before dropping

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake DROP WAREHOUSE Syntax
```sql
-- Basic DROP WAREHOUSE operation
DROP WAREHOUSE <warehouse_name>;

-- DROP WAREHOUSE with IF EXISTS error handling
DROP WAREHOUSE IF EXISTS <warehouse_name>;
```

### Syntax Variations and Constraints
- **Warehouse Name**: Must be valid Snowflake identifier (quoted or unquoted)
- **IF EXISTS**: Optional clause to suppress errors if warehouse doesn't exist
- **No CASCADE/RESTRICT**: Unlike other objects, warehouses don't support cascade options
- **Case Sensitivity**: Warehouse names are case-insensitive unless quoted

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| warehouseName | Target warehouse name for deletion | String | Required | - | Valid Snowflake identifier | Cannot be null/empty, must exist (unless IF EXISTS) | None | HIGH | Case-insensitive, auto-converted to uppercase |
| ifExists | Suppress errors if warehouse doesn't exist | Boolean | Optional | false | true/false | None | None | HIGH | Prevents errors for idempotent operations |

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Warehouse Drop
```sql
-- Drop warehouse (error if doesn't exist)
DROP WAREHOUSE production_warehouse;
```
**Expected Behavior**: Warehouse deleted immediately, error if doesn't exist
**Test Validation**: Verify warehouse removed from system views, verify error handling

### Example 2: Conditional Drop with IF EXISTS
```sql
-- Drop warehouse only if it exists (no error if missing)
DROP WAREHOUSE IF EXISTS temporary_warehouse;
```
**Expected Behavior**: Warehouse deleted if exists, no error if doesn't exist
**Test Validation**: Verify idempotent behavior, verify no error when warehouse missing

### Example 3: Drop Running Warehouse
```sql
-- Drop warehouse that may be currently running
DROP WAREHOUSE IF EXISTS active_warehouse;
```
**Expected Behavior**: Warehouse automatically suspended then deleted, running queries terminated
**Test Validation**: Verify automatic suspension, verify query termination

### Example 4: Drop with Quoted Identifier
```sql
-- Drop warehouse with case-sensitive name
DROP WAREHOUSE IF EXISTS "Mixed_Case_Warehouse";
```
**Expected Behavior**: Respects exact case for quoted identifiers
**Test Validation**: Verify case-sensitive handling of quoted names

### Example 5: Drop Non-Existent Warehouse (Error Case)
```sql
-- Attempt to drop warehouse that doesn't exist
DROP WAREHOUSE does_not_exist;
```
**Expected Behavior**: Error thrown with specific message about warehouse not existing
**Test Validation**: Verify exact error message matches Snowflake behavior

### Example 6: Drop with Special Characters
```sql
-- Drop warehouse with special characters in name
DROP WAREHOUSE IF EXISTS "warehouse-with_special.chars";
```
**Expected Behavior**: Handles special characters in quoted identifiers correctly
**Test Validation**: Verify special character handling in warehouse names

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: Basic Drop Operations
**Test File**: `DropWarehouseBasicTest.java`
- **Test 1.1**: Basic drop operation without IF EXISTS
- **Test 1.2**: Drop with IF EXISTS when warehouse exists
- **Test 1.3**: Drop with IF EXISTS when warehouse doesn't exist
- **Test 1.4**: Drop non-existent warehouse without IF EXISTS (error case)
- **Test 1.5**: SQL Generation: Verify exact SQL format for basic drop
- **Test 1.6**: SQL Generation: Verify exact SQL format for IF EXISTS drop

#### Scenario Group 2: Warehouse Name Handling
**Test File**: `DropWarehouseNameHandlingTest.java`
- **Test 2.1**: Drop with unquoted identifier (case conversion)
- **Test 2.2**: Drop with quoted identifier (case preservation)
- **Test 2.3**: Drop with special characters in quoted name
- **Test 2.4**: Validation: Empty/null warehouse name (error)
- **Test 2.5**: Validation: Invalid warehouse name format (error)

#### Scenario Group 3: Error Conditions and Validation
**Test File**: `DropWarehouseErrorConditionsTest.java`
- **Test 3.1**: Warehouse doesn't exist without IF EXISTS (error)
- **Test 3.2**: Invalid warehouse name characters (error)
- **Test 3.3**: Null warehouse name (error)
- **Test 3.4**: Empty warehouse name (error)
- **Test 3.5**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `dropWarehouse_integration_test.xml`
- **Integration 1**: End-to-end warehouse drop with database validation
- **Integration 2**: Drop running warehouse and verify suspension
- **Integration 3**: IF EXISTS behavior with non-existent warehouse
- **Integration 4**: Error handling with invalid warehouse names
- **Integration 5**: Case sensitivity testing with quoted/unquoted names

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic warehouse name validation and processing"
    - "IF EXISTS conditional logic implementation"
    - "SQL generation for both drop variations"
    - "Error handling and message generation"
    
  MEDIUM_PRIORITY_TESTS:
    - "Case sensitivity handling for quoted/unquoted names"
    - "Special character support in warehouse names"
    - "Integration with Snowflake identifier validation"
    
  LOW_PRIORITY_TESTS:
    - "Edge cases with very long warehouse names"
    - "Performance testing with rapid drop operations"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "Simple validation + SQL generation pattern"
  VALIDATION_STRATEGY: "Early warehouse name validation and existence checking"
  SQL_GENERATION_STRATEGY: "Template-based with IF EXISTS conditional logic"

REFACTOR_PHASE_FOCUS:
  - "Extract common warehouse name validation logic"
  - "Optimize SQL generation templates"
  - "Standardize error message formatting"
  - "Performance optimization for validation"
```

### Implementation Pattern Selection
**PATTERN**: New Changetype Pattern
**RATIONALE**: DROP WAREHOUSE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: Low - Simple operation with minimal validation requirements

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.change.Change → DropWarehouseChange"
  - "liquibase.statement.SqlStatement → DropWarehouseStatement"
  - "liquibase.sqlgenerator.SqlGenerator → DropWarehouseSqlGenerator"

REQUIRED_XSD_ELEMENTS:
  - "dropWarehouse element with warehouseName and ifExists attributes"
  - "Simple type definitions for warehouse name validation"
  - "Boolean type for ifExists flag"
```

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 6 comprehensive SQL examples provided
- [✓] Test scenario matrix covering all operation scenarios
- [✓] Implementation guidance with TDD approach specified
- [✓] Error conditions and validation requirements specified
- [✓] Case sensitivity and identifier handling documented

### Implementation Readiness Assessment
- [✓] Simple operation clearly defined with minimal complexity
- [✓] Validation rules specified with exact error conditions
- [✓] SQL generation requirements clear for both variations
- [✓] Test scenarios comprehensive and actionable
- [✓] Implementation complexity appropriate for estimated time

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale

1. **Required Attributes**:
   - warehouseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Operational Constraints**:
   - Cannot drop warehouse that is currently in use by active sessions
   - Warehouse must be suspended or drop will suspend it first

## 7. Expected Behaviors

1. **Basic drop**:
   - Warehouse is suspended if running
   - All active queries are terminated
   - Warehouse is removed from the system
   - Cannot be undropped

2. **IF EXISTS behavior**:
   - Succeeds silently if warehouse doesn't exist
   - No error thrown

3. **Active sessions**:
   - Sessions using the warehouse are interrupted
   - Queries fail with warehouse not found error

## 8. Error Conditions

1. Warehouse doesn't exist (without IF EXISTS)
2. Insufficient privileges (requires OWNERSHIP or higher)
3. System warehouses cannot be dropped

## 9. Implementation Notes

- Warehouse names are automatically converted to uppercase unless quoted
- Dropping a warehouse is immediate and cannot be rolled back
- No UNDROP available for warehouses
- Active queries are terminated immediately
- Consider impact on running workloads
- Resource monitor associations are automatically removed