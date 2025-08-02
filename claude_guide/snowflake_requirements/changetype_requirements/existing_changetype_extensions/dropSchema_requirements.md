# Snowflake DropSchema Requirements Document
## Implementation-Ready Requirements for DropSchema Changetype Extension

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "PHASE_2_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Schema"
OPERATION: "DROP"
NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
ESTIMATED_IMPLEMENTATION_TIME: "3-4 hours"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "SQL Generator override for DROP SCHEMA with Snowflake-specific CASCADE/RESTRICT options"
KEY_OPERATIONS:
  - "Enhanced DROP SCHEMA with IF EXISTS conditional dropping"
  - "CASCADE mode for dropping schema with all contained objects"
  - "RESTRICT mode for safe schema dropping with validation"
  - "Connection context validation and safety checks"
COMPLEXITY_ASSESSMENT: "LOW-MEDIUM - Simple SQL generation override with important safety constraints"
SUCCESS_CRITERIA: "Clean schema deletion with proper CASCADE/RESTRICT behavior and comprehensive validation"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/drop-schema
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/sql-reference/sql/create-schema (for understanding schema lifecycle)

### Documentation Analysis and Key Insights
- **Simple Core Operation**: DROP SCHEMA is straightforward but has critical CASCADE/RESTRICT distinction
- **Safety-First Design**: RESTRICT is default behavior to prevent accidental data loss
- **No Recovery Options**: Unlike databases, schemas cannot be recovered with UNDROP
- **Connection Context Sensitivity**: Cannot drop schema that is currently in use by session

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake DROP SCHEMA Syntax
```sql
-- Basic DROP SCHEMA operation
DROP SCHEMA <schema_name>;

-- DROP SCHEMA with IF EXISTS error handling
DROP SCHEMA IF EXISTS <schema_name>;

-- DROP SCHEMA with CASCADE (drop all objects)
DROP SCHEMA <schema_name> CASCADE;

-- DROP SCHEMA with RESTRICT (fail if objects exist)
DROP SCHEMA <schema_name> RESTRICT;

-- Combined: IF EXISTS with CASCADE/RESTRICT
DROP SCHEMA IF EXISTS <schema_name> CASCADE;
DROP SCHEMA IF EXISTS <schema_name> RESTRICT;
```

### Parameter Dependencies and Constraints
- **CASCADE vs RESTRICT**: Mutually exclusive options for handling contained objects
- **Default Behavior**: RESTRICT behavior applied when neither CASCADE nor RESTRICT specified
- **Connection Context**: Cannot drop schema that is currently in use by session

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| schemaName | Target schema name for deletion | String | Required | - | Valid Snowflake identifier | Cannot be null/empty, cannot be current session schema | None | HIGH | Case-insensitive, auto-converted to uppercase |
| ifExists | Suppress errors if schema doesn't exist | Boolean | Optional | false | true/false | None | None | HIGH | Prevents errors for idempotent operations |
| cascade | Drop schema and all contained objects | Boolean | Optional | false | true/false | Cannot be used with restrict=true | Mutually exclusive with restrict | HIGH | Forces deletion of all contents |
| restrict | Fail if schema contains objects | Boolean | Optional | false | true/false | Cannot be used with cascade=true | Mutually exclusive with cascade | HIGH | Default behavior when neither specified |
| databaseName | Database name for schema | String | Optional | null | Valid database identifier | Must exist if specified | None | HIGH | Schema location identifier |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **cascade** and **restrict** - Cannot use both in same statement
   - `DROP SCHEMA myschema CASCADE` - Valid
   - `DROP SCHEMA myschema RESTRICT` - Valid
   - `DROP SCHEMA myschema CASCADE RESTRICT` - Invalid

### Default Behavior
- If neither CASCADE nor RESTRICT is specified, the default behavior is RESTRICT
- Schema must be empty to be dropped with default/RESTRICT behavior

### Required Combinations
1. If **cascade** is true, all contained objects will be dropped
2. If **restrict** is true, operation fails if any objects exist in schema
3. If neither is specified, **restrict** behavior is applied by default

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Schema Drop
```sql
-- Drop schema (error if objects exist or schema doesn't exist)
DROP SCHEMA production_schema;
```
**Expected Behavior**: Schema deleted if empty, error if contains objects or doesn't exist
**Test Validation**: Verify schema removed from system views, verify error handling

### Example 2: Conditional Drop with IF EXISTS
```sql
-- Drop schema only if it exists (no error if missing)
DROP SCHEMA IF EXISTS temporary_schema;
```
**Expected Behavior**: Schema deleted if exists, no error if doesn't exist
**Test Validation**: Verify idempotent behavior, verify no error when schema missing

### Example 3: Force Drop with CASCADE
```sql
-- Drop schema and all contained objects
DROP SCHEMA complex_schema CASCADE;
```
**Expected Behavior**: Schema and all contents deleted regardless of objects present
**Test Validation**: Verify all objects removed, verify cascade behavior

### Example 4: Safe Drop with RESTRICT
```sql
-- Drop schema only if empty (explicit RESTRICT)
DROP SCHEMA empty_schema RESTRICT;
```
**Expected Behavior**: Schema deleted only if no objects exist
**Test Validation**: Verify fails with objects present, succeeds when empty

### Example 5: Conditional CASCADE Drop
```sql
-- Drop schema with all contents if it exists
DROP SCHEMA IF EXISTS development_schema CASCADE;
```
**Expected Behavior**: Schema and contents deleted if exists, no error if missing
**Test Validation**: Verify idempotent cascade behavior

### Example 6: Drop with Quoted Identifier
```sql
-- Drop schema with case-sensitive name
DROP SCHEMA IF EXISTS "Mixed_Case_Schema" RESTRICT;
```
**Expected Behavior**: Respects exact case for quoted identifiers
**Test Validation**: Verify case-sensitive handling of quoted names

### Example 7: Error Case - Non-Empty Schema
```sql
-- Attempt to drop schema with objects (default RESTRICT behavior)
DROP SCHEMA schema_with_tables;
```
**Expected Behavior**: Error thrown about schema containing objects
**Test Validation**: Verify exact error message matches Snowflake behavior

### Example 8: Current Session Schema Drop Attempt
```sql
-- Attempt to drop currently active schema
DROP SCHEMA current_session_schema;
```
**Expected Behavior**: Error thrown about cannot drop current schema
**Test Validation**: Verify connection context validation error

### Example 9: Drop Schema with Database Name
```xml
<dropSchema schemaName="target_schema"
            databaseName="my_database"
            ifExists="true"
            cascade="true"/>
```
**Expected Behavior**: Drop schema in specific database with CASCADE behavior
**Test Validation**: Verify database-qualified schema deletion

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: Basic Drop Operations
**Test File**: `DropSchemaBasicTest.java`
- **Test 1.1**: Basic drop operation without IF EXISTS
- **Test 1.2**: Drop with IF EXISTS when schema exists
- **Test 1.3**: Drop with IF EXISTS when schema doesn't exist
- **Test 1.4**: Drop non-existent schema without IF EXISTS (error case)
- **Test 1.5**: SQL Generation: Verify exact SQL format for basic drop
- **Test 1.6**: SQL Generation: Verify exact SQL format for IF EXISTS drop

#### Scenario Group 2: CASCADE Operations
**Test File**: `DropSchemaCascadeTest.java`
- **Test 2.1**: Drop empty schema with CASCADE
- **Test 2.2**: Drop schema with tables using CASCADE
- **Test 2.3**: Drop schema with complex object hierarchy using CASCADE
- **Test 2.4**: Conditional CASCADE with IF EXISTS
- **Test 2.5**: SQL Generation: Verify exact SQL format for CASCADE operations

#### Scenario Group 3: RESTRICT Operations
**Test File**: `DropSchemaRestrictTest.java`
- **Test 3.1**: Drop empty schema with RESTRICT (success)
- **Test 3.2**: Drop schema with objects using RESTRICT (error)
- **Test 3.3**: Drop schema with only system objects (success)
- **Test 3.4**: Conditional RESTRICT with IF EXISTS
- **Test 3.5**: SQL Generation: Verify exact SQL format for RESTRICT operations

#### Scenario Group 4: Schema Name Handling
**Test File**: `DropSchemaNameHandlingTest.java`
- **Test 4.1**: Drop with unquoted identifier (case conversion)
- **Test 4.2**: Drop with quoted identifier (case preservation)
- **Test 4.3**: Drop with special characters in quoted name
- **Test 4.4**: Validation: Empty/null schema name (error)
- **Test 4.5**: Validation: Invalid schema name format (error)

#### Scenario Group 5: Mutual Exclusivity Validation
**Test File**: `DropSchemaMutualExclusivityTest.java`
- **Test 5.1**: Cannot specify both CASCADE and RESTRICT
- **Test 5.2**: Validation: Error message for mutual exclusivity violation
- **Test 5.3**: Default behavior when neither CASCADE nor RESTRICT specified
- **Test 5.4**: Verify RESTRICT is default behavior

#### Scenario Group 6: Connection Context Validation
**Test File**: `DropSchemaContextTest.java`
- **Test 6.1**: Attempt to drop current session schema (error)
- **Test 6.2**: Drop non-current schema (success)
- **Test 6.3**: Validation: Current schema detection logic
- **Test 6.4**: Error message for current schema drop attempt

#### Scenario Group 7: Error Conditions and Edge Cases
**Test File**: `DropSchemaErrorConditionsTest.java`
- **Test 7.1**: Schema doesn't exist without IF EXISTS (error)
- **Test 7.2**: Schema contains objects with default/RESTRICT behavior (error)
- **Test 7.3**: Invalid schema name characters (error)
- **Test 7.4**: Null schema name (error)
- **Test 7.5**: Empty schema name (error)
- **Test 7.6**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `dropSchema_integration_test.xml`
- **Integration 1**: End-to-end schema drop with validation
- **Integration 2**: CASCADE drop with complex object hierarchy
- **Integration 3**: IF EXISTS behavior with non-existent schema
- **Integration 4**: Error handling with RESTRICT on non-empty schema
- **Integration 5**: Case sensitivity testing with quoted/unquoted names

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **dropSchema.xml** - Basic drop and IF EXISTS variations
2. **dropSchemaCascade.xml** - CASCADE variations with object handling
3. **dropSchemaRestrict.xml** - RESTRICT variations with validation

## 6. Validation Rules

1. **Required Attributes**:
   - schemaName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If cascade=true and restrict=true, throw: "Cannot use both CASCADE and RESTRICT"

3. **Connection Context**:
   - Cannot drop the current schema
   - Must switch to a different schema first

4. **Object Validation**:
   - Default/RESTRICT behavior fails if schema contains objects
   - CASCADE behavior succeeds regardless of object content

## 7. Expected Behaviors

1. **IF EXISTS behavior**:
   - Succeeds silently if schema doesn't exist
   - No error thrown

2. **CASCADE behavior**:
   - Drops all contained database objects
   - Includes tables, views, sequences, functions, procedures, etc.

3. **RESTRICT behavior**:
   - Fails if schema contains any objects
   - This is the default if neither CASCADE nor RESTRICT specified

4. **No Recovery**:
   - Dropped schemas cannot be recovered (no UNDROP available)
   - Consider impact on dependent objects

## 8. Error Conditions

1. Schema doesn't exist (without IF EXISTS)
2. Schema contains objects (with RESTRICT or default)
3. Insufficient privileges
4. Schema is currently in use (current schema)

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic schema name validation and processing"
    - "IF EXISTS conditional logic implementation"
    - "CASCADE vs RESTRICT mutual exclusivity validation"
    - "SQL generation for all drop variations"
    
  MEDIUM_PRIORITY_TESTS:
    - "Connection context validation (current schema)"
    - "Case sensitivity handling for quoted/unquoted names"
    - "Object existence validation for RESTRICT behavior"
    
  LOW_PRIORITY_TESTS:
    - "Edge cases with very long schema names"
    - "Performance testing with complex CASCADE operations"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "SQL Generator override with validation-first approach"
  VALIDATION_STRATEGY: "Early schema name and context validation"
  SQL_GENERATION_STRATEGY: "Template-based with CASCADE/RESTRICT conditional logic"

REFACTOR_PHASE_FOCUS:
  - "Extract common schema name validation logic"
  - "Optimize SQL generation templates"
  - "Standardize error message formatting"
  - "Performance optimization for validation"
```

### Implementation Pattern Selection
**PATTERN**: Existing Changetype Extension Pattern
**RATIONALE**: Core Liquibase DROP SCHEMA exists but needs Snowflake-specific CASCADE/RESTRICT SQL generation
**COMPLEXITY**: Low-Medium - Simple SQL generation override with important safety constraints

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.sqlgenerator.SqlGenerator → DropSchemaSqlGeneratorSnowflake"

REQUIRED_XSD_ELEMENTS:
  - "Enhancement to existing dropSchema XSD with Snowflake namespace attributes"
  - "Snowflake-specific cascade and restrict attributes in snowflake: namespace"
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
- [✓] Extension pattern clearly defined for existing changetype
- [✓] SQL generation requirements clear for all variations
- [✓] Validation rules specified with exact error conditions
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

- Schema names are automatically converted to uppercase unless quoted
- Current schema cannot be dropped while in use
- Dropped schemas cannot be recovered (no UNDROP for schemas)
- Consider rollback support: Would need to capture all contained objects for CASCADE
- CASCADE behavior permanently deletes all contained objects
- RESTRICT is the default behavior for safety