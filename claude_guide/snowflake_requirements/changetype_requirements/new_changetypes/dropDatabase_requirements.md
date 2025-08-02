# Snowflake DropDatabase Requirements Document
## Implementation-Ready Requirements for DropDatabase Changetype Development

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
OBJECT_TYPE: "Database"
OPERATION: "DROP"
NEXT_PHASE: "Phase 3 - TDD Implementation"
ESTIMATED_IMPLEMENTATION_TIME: "4-5 hours (validation complete)"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "Complete DROP DATABASE changetype supporting Snowflake database deletion operations"
KEY_OPERATIONS:
  - "Basic DROP DATABASE operation with validation"
  - "IF EXISTS conditional dropping with error handling"
  - "CASCADE and RESTRICT modes for schema handling"
  - "Connection context validation and safety checks"
COMPLEXITY_ASSESSMENT: "MEDIUM - Simple operation with important safety constraints and cascade behavior"
SUCCESS_CRITERIA: "Clean database deletion with proper error handling, cascade behavior, and safety validation"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/drop-database
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/sql-reference/sql/undrop-database (for recovery options)

### Documentation Analysis and Key Insights
- **Simple Core Operation**: DROP DATABASE is straightforward but has important safety constraints
- **CASCADE vs RESTRICT**: Critical distinction for handling databases with user-created schemas
- **Connection Context Sensitivity**: Cannot drop the current session database
- **Recovery Options**: Dropped databases can be recovered with UNDROP within retention period
- **Privilege Implications**: All grants on database are also dropped

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake DROP DATABASE Syntax
```sql
-- Basic DROP DATABASE operation
DROP DATABASE <database_name>;

-- DROP DATABASE with IF EXISTS error handling
DROP DATABASE IF EXISTS <database_name>;

-- DROP DATABASE with CASCADE (drop all schemas and contents)
DROP DATABASE <database_name> CASCADE;

-- DROP DATABASE with RESTRICT (fail if schemas exist)
DROP DATABASE <database_name> RESTRICT;

-- Combined: IF EXISTS with CASCADE/RESTRICT
DROP DATABASE IF EXISTS <database_name> CASCADE;
DROP DATABASE IF EXISTS <database_name> RESTRICT;
```

### Syntax Variations and Constraints
- **Database Name**: Must be valid Snowflake identifier (quoted or unquoted)
- **IF EXISTS**: Optional clause to suppress errors if database doesn't exist
- **CASCADE**: Drops database and all contained schemas/objects
- **RESTRICT**: Fails if database contains user-created schemas (default behavior)
- **Case Sensitivity**: Database names are case-insensitive unless quoted

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| databaseName | Target database name for deletion | String | Required | - | Valid Snowflake identifier | Cannot be null/empty, cannot be current session database | None | HIGH | Case-insensitive, auto-converted to uppercase |
| ifExists | Suppress errors if database doesn't exist | Boolean | Optional | false | true/false | None | None | HIGH | Prevents errors for idempotent operations |
| cascade | Drop database and all contained schemas/objects | Boolean | Optional | false | true/false | Cannot be used with restrict=true | Mutually exclusive with restrict | HIGH | Forces deletion of all contents |
| restrict | Fail if database contains user-created schemas | Boolean | Optional | false | true/false | Cannot be used with cascade=true | Mutually exclusive with cascade | HIGH | Default behavior when neither specified |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **cascade** and **restrict** - Cannot use both in same statement
   - `DROP DATABASE mydb CASCADE` - Valid
   - `DROP DATABASE mydb RESTRICT` - Valid  
   - `DROP DATABASE mydb CASCADE RESTRICT` - Invalid

### Default Behavior
- If neither CASCADE nor RESTRICT is specified, the default behavior is RESTRICT
- Database must be empty (only contain INFORMATION_SCHEMA and PUBLIC schemas)

### Required Combinations
1. If **cascade** is true, all schemas and their contents will be dropped
2. If **restrict** is true, operation fails if user-created schemas exist
3. If neither is specified, **restrict** behavior is applied by default

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Database Drop
```sql
-- Drop database (error if schemas exist or database doesn't exist)
DROP DATABASE production_database;
```
**Expected Behavior**: Database deleted if empty, error if contains schemas or doesn't exist
**Test Validation**: Verify database removed from system views, verify error handling

### Example 2: Conditional Drop with IF EXISTS
```sql
-- Drop database only if it exists (no error if missing)
DROP DATABASE IF EXISTS temporary_database;
```
**Expected Behavior**: Database deleted if exists, no error if doesn't exist
**Test Validation**: Verify idempotent behavior, verify no error when database missing

### Example 3: Force Drop with CASCADE
```sql
-- Drop database and all contained schemas/objects
DROP DATABASE complex_database CASCADE;
```
**Expected Behavior**: Database and all contents deleted regardless of schemas present
**Test Validation**: Verify all schemas/objects removed, verify cascade behavior

### Example 4: Safe Drop with RESTRICT
```sql
-- Drop database only if empty (explicit RESTRICT)
DROP DATABASE empty_database RESTRICT;
```
**Expected Behavior**: Database deleted only if no user-created schemas exist
**Test Validation**: Verify fails with schemas present, succeeds when empty

### Example 5: Conditional CASCADE Drop
```sql
-- Drop database with all contents if it exists
DROP DATABASE IF EXISTS development_database CASCADE;
```
**Expected Behavior**: Database and contents deleted if exists, no error if missing
**Test Validation**: Verify idempotent cascade behavior

### Example 6: Drop with Quoted Identifier
```sql
-- Drop database with case-sensitive name
DROP DATABASE IF EXISTS "Mixed_Case_Database" RESTRICT;
```
**Expected Behavior**: Respects exact case for quoted identifiers
**Test Validation**: Verify case-sensitive handling of quoted names

### Example 7: Error Case - Non-Empty Database
```sql
-- Attempt to drop database with schemas (default RESTRICT behavior)
DROP DATABASE database_with_schemas;
```
**Expected Behavior**: Error thrown about database containing schemas
**Test Validation**: Verify exact error message matches Snowflake behavior

### Example 8: Current Session Database Drop Attempt
```sql
-- Attempt to drop currently connected database
DROP DATABASE current_session_database;
```
**Expected Behavior**: Error thrown about cannot drop current database
**Test Validation**: Verify connection context validation error

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: Basic Drop Operations
**Test File**: `DropDatabaseBasicTest.java`
- **Test 1.1**: Basic drop operation without IF EXISTS
- **Test 1.2**: Drop with IF EXISTS when database exists
- **Test 1.3**: Drop with IF EXISTS when database doesn't exist
- **Test 1.4**: Drop non-existent database without IF EXISTS (error case)
- **Test 1.5**: SQL Generation: Verify exact SQL format for basic drop
- **Test 1.6**: SQL Generation: Verify exact SQL format for IF EXISTS drop

#### Scenario Group 2: CASCADE Operations
**Test File**: `DropDatabaseCascadeTest.java`
- **Test 2.1**: Drop empty database with CASCADE
- **Test 2.2**: Drop database with schemas using CASCADE
- **Test 2.3**: Drop database with complex object hierarchy using CASCADE
- **Test 2.4**: Conditional CASCADE with IF EXISTS
- **Test 2.5**: SQL Generation: Verify exact SQL format for CASCADE operations

#### Scenario Group 3: RESTRICT Operations
**Test File**: `DropDatabaseRestrictTest.java`
- **Test 3.1**: Drop empty database with RESTRICT (success)
- **Test 3.2**: Drop database with schemas using RESTRICT (error)
- **Test 3.3**: Drop database with only INFORMATION_SCHEMA and PUBLIC (success)
- **Test 3.4**: Conditional RESTRICT with IF EXISTS
- **Test 3.5**: SQL Generation: Verify exact SQL format for RESTRICT operations

#### Scenario Group 4: Database Name Handling
**Test File**: `DropDatabaseNameHandlingTest.java`
- **Test 4.1**: Drop with unquoted identifier (case conversion)
- **Test 4.2**: Drop with quoted identifier (case preservation)
- **Test 4.3**: Drop with special characters in quoted name
- **Test 4.4**: Validation: Empty/null database name (error)
- **Test 4.5**: Validation: Invalid database name format (error)

#### Scenario Group 5: Mutual Exclusivity Validation
**Test File**: `DropDatabaseMutualExclusivityTest.java`
- **Test 5.1**: Cannot specify both CASCADE and RESTRICT
- **Test 5.2**: Validation: Error message for mutual exclusivity violation
- **Test 5.3**: Default behavior when neither CASCADE nor RESTRICT specified
- **Test 5.4**: Verify RESTRICT is default behavior

#### Scenario Group 6: Connection Context Validation
**Test File**: `DropDatabaseContextTest.java`
- **Test 6.1**: Attempt to drop current session database (error)
- **Test 6.2**: Drop non-current database (success)
- **Test 6.3**: Validation: Current database detection logic
- **Test 6.4**: Error message for current database drop attempt

#### Scenario Group 7: Error Conditions and Edge Cases
**Test File**: `DropDatabaseErrorConditionsTest.java`
- **Test 7.1**: Database doesn't exist without IF EXISTS (error)
- **Test 7.2**: Database contains schemas with default/RESTRICT behavior (error)
- **Test 7.3**: Invalid database name characters (error)
- **Test 7.4**: Null database name (error)
- **Test 7.5**: Empty database name (error)
- **Test 7.6**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `dropDatabase_integration_test.xml`
- **Integration 1**: End-to-end database drop with validation
- **Integration 2**: CASCADE drop with complex schema hierarchy
- **Integration 3**: IF EXISTS behavior with non-existent database
- **Integration 4**: Error handling with RESTRICT on non-empty database
- **Integration 5**: Case sensitivity testing with quoted/unquoted names

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **dropDatabase.xml** - Basic drop and IF EXISTS variations
2. **dropDatabaseCascade.xml** - CASCADE variations with schema handling
3. **dropDatabaseRestrict.xml** - RESTRICT variations with validation

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If cascade=true and restrict=true, throw: "Cannot use both CASCADE and RESTRICT"

3. **Connection Context**:
   - Cannot drop the current database
   - Must switch to a different database first

4. **Schema Validation**:
   - Default/RESTRICT behavior fails if user-created schemas exist
   - CASCADE behavior succeeds regardless of schema content

## 7. Expected Behaviors

1. **IF EXISTS behavior**:
   - Succeeds silently if database doesn't exist
   - No error thrown

2. **CASCADE behavior**:
   - Drops all schemas in the database
   - Drops all objects in all schemas
   - Includes user-created schemas and their contents

3. **RESTRICT behavior**:
   - Fails if database contains any user-created schemas
   - Only succeeds if database contains just INFORMATION_SCHEMA and PUBLIC
   - This is the default if neither CASCADE nor RESTRICT specified

4. **Recovery capability**:
   - Dropped databases can be recovered using UNDROP DATABASE
   - Recovery available within retention period

## 8. Error Conditions

1. Database doesn't exist (without IF EXISTS)
2. Database contains schemas (with RESTRICT or default)
3. Insufficient privileges
4. Database is currently in use (current database)
5. Active connections to the database

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic database name validation and processing"
    - "IF EXISTS conditional logic implementation"
    - "CASCADE vs RESTRICT mutual exclusivity validation"
    - "SQL generation for all drop variations"
    
  MEDIUM_PRIORITY_TESTS:
    - "Connection context validation (current database)"
    - "Case sensitivity handling for quoted/unquoted names"
    - "Schema existence validation for RESTRICT behavior"
    
  LOW_PRIORITY_TESTS:
    - "Edge cases with very long database names"
    - "Performance testing with complex CASCADE operations"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "Validation-first with conditional SQL generation"
  VALIDATION_STRATEGY: "Early database name and context validation"
  SQL_GENERATION_STRATEGY: "Template-based with CASCADE/RESTRICT conditional logic"

REFACTOR_PHASE_FOCUS:
  - "Extract common database name validation logic"
  - "Optimize SQL generation templates"
  - "Standardize error message formatting"
  - "Performance optimization for validation"
```

### Implementation Pattern Selection
**PATTERN**: New Changetype Pattern
**RATIONALE**: DROP DATABASE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: Medium - Simple operation with important safety constraints and cascade behavior

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.change.Change → DropDatabaseChange"
  - "liquibase.statement.SqlStatement → DropDatabaseStatement"
  - "liquibase.sqlgenerator.SqlGenerator → DropDatabaseSqlGenerator"

REQUIRED_XSD_ELEMENTS:
  - "dropDatabase element with databaseName, ifExists, cascade, restrict attributes"
  - "Simple type definitions for database name validation"
  - "Boolean types for conditional flags"
```

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 8 comprehensive SQL examples provided
- [✓] Test scenario matrix covering all operation scenarios
- [✓] Implementation guidance with TDD approach specified
- [✓] Error conditions and validation requirements specified
- [✓] CASCADE vs RESTRICT behavior clearly documented

### Implementation Readiness Assessment
- [✓] Simple operation clearly defined with safety constraints
- [✓] Validation rules specified with exact error conditions
- [✓] SQL generation requirements clear for all variations
- [✓] Test scenarios comprehensive and actionable
- [✓] Implementation complexity appropriate for estimated time
- [✓] Connection context validation requirements specified

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale

## 9. Implementation Notes

- Database names are automatically converted to uppercase unless quoted
- Current database cannot be dropped
- Dropped databases can be recovered using UNDROP DATABASE within retention period
- All privileges on the database are also dropped
- Consider connection management in Liquibase when dropping databases
- CASCADE behavior permanently deletes all contained objects
- RESTRICT is the default behavior for safety