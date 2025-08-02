# Snowflake CreateSchema Requirements Document
## Implementation-Ready Requirements for CreateSchema Changetype Extension

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "4.0"
PHASE: "VALIDATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
VALIDATION_COMPLETION_DATE: "2025-08-01"
VALIDATION_METHOD: "Simple INFORMATION_SCHEMA + Manual Documentation Review"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Schema"
OPERATION: "CREATE"
NEXT_PHASE: "Phase 3 - TDD Implementation"
ESTIMATED_IMPLEMENTATION_TIME: "5-6 hours (validation complete)"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "SQL Generator override for CREATE SCHEMA with Snowflake-specific enhancements"
KEY_OPERATIONS:
  - "Enhanced CREATE SCHEMA with OR REPLACE and IF NOT EXISTS support"
  - "Transient schema creation with Time Travel constraints"
  - "Schema cloning with point-in-time recovery options"
  - "Managed access configuration for centralized access control"
  - "Advanced features: pipe execution control, collation settings"
COMPLEXITY_ASSESSMENT: "MEDIUM - SQL generation override with complex parameter validation and mutual exclusivity rules"
SUCCESS_CRITERIA: "All Snowflake CREATE SCHEMA features implemented with comprehensive validation and proper SQL generation"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/create-schema
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/sql-reference/sql/clone (for cloning operations)

### Documentation Analysis and Key Insights
- **Extended CREATE SCHEMA**: Snowflake supports numerous options beyond standard SQL
- **Complex Parameter Interactions**: OR REPLACE vs IF NOT EXISTS, transient vs retention settings
- **Cloning Capabilities**: Zero-copy cloning with point-in-time recovery support
- **Managed Access**: Centralized privilege management unique to Snowflake
- **Time Travel Integration**: Schema-level Time Travel and Fail-safe configuration

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake CREATE SCHEMA Syntax
```sql
-- Enhanced CREATE SCHEMA with all Snowflake options
CREATE [ OR REPLACE ] [ TRANSIENT ] SCHEMA [ IF NOT EXISTS ] <name>
  [ CLONE <source_schema_name>
        [ { AT | BEFORE } ( { TIMESTAMP => <timestamp> | OFFSET => <time_difference> | STATEMENT => <id> } ) ] ]
  [ WITH MANAGED ACCESS ]
  [ DATA_RETENTION_TIME_IN_DAYS = <integer> ]
  [ MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer> ]
  [ DEFAULT_DDL_COLLATION = '<collation_specification>' ]
  [ LOG_LEVEL = '<log_level>' ]
  [ TRACE_LEVEL = '<trace_level>' ]
  [ SUSPEND_TASK_AFTER_NUM_FAILURES = <num> ]
  [ TASK_AUTO_RETRY_ATTEMPTS = <num> ]
  [ USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE = <warehouse_size> ]
  [ USER_TASK_TIMEOUT_MS = <num> ]
  [ USER_TASK_MINIMUM_TRIGGER_INTERVAL_IN_SECONDS = <num> ]
  [ QUOTED_IDENTIFIERS_IGNORE_CASE = { TRUE | FALSE } ]
  [ ENABLE_CONSOLE_OUTPUT = { TRUE | FALSE } ]
  [ PIPE_EXECUTION_PAUSED = { TRUE | FALSE } ]
  [ COMMENT = '<string_literal>' ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ) ];
```

### Parameter Dependencies and Constraints
- **Mutual Exclusivity**: OR REPLACE and IF NOT EXISTS cannot be combined
- **Transient Restrictions**: Transient schemas must have dataRetentionTimeInDays = 0
- **Retention Relationship**: maxDataExtensionTimeInDays must be ≥ dataRetentionTimeInDays
- **Cloning Requirements**: Source schema must exist for cloning operations

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| schemaName | Target schema name for creation | String | Required | - | Valid Snowflake identifier | Cannot be null/empty | None | HIGH | Case-insensitive, auto-converted to uppercase |
| orReplace | Replace existing schema | Boolean | Optional | false | true/false | Cannot combine with ifNotExists | Mutually exclusive with ifNotExists | HIGH | Drops existing schema completely |
| transient | Create transient schema | Boolean | Optional | false | true/false | If true, dataRetentionTimeInDays must be 0 | None | MEDIUM | No Time Travel or Fail-safe |
| ifNotExists | Create only if doesn't exist | Boolean | Optional | false | true/false | Cannot combine with orReplace | Mutually exclusive with orReplace | HIGH | Idempotent operation support |
| cloneFrom | Source schema for cloning | String | Optional | null | Existing schema name | Source must exist | None | MEDIUM | Zero-copy clone operation |
| managedAccess | Enable managed access | Boolean | Optional | false | true/false | None | None | MEDIUM | Centralized privilege management |
| dataRetentionTimeInDays | Time Travel retention period | Integer | Optional | 1 | 0-90 | Must be 0 if transient=true, ≤ maxDataExtensionTimeInDays | Cannot be >0 if transient=true | MEDIUM | Controls Time Travel availability |
| maxDataExtensionTimeInDays | Maximum Time Travel extension | Integer | Optional | 14 | 0-90 | Must be ≥ dataRetentionTimeInDays | None | LOW | Maximum extension for retention |
| defaultDdlCollation | Default string collation | String | Optional | null | Valid collation specification | Must be valid collation | None | LOW | Schema-level collation setting |
| pipeExecutionPaused | Pause pipe execution in schema | Boolean | Optional | false | true/false | None | None | LOW | Controls data pipeline behavior |
| comment | Schema description | String | Optional | null | String ≤ 256 chars | Length validation | None | LOW | Metadata only |
| databaseName | Database name for schema | String | Optional | null | Valid database identifier | Must exist if specified | None | HIGH | Schema location identifier |
| externalVolume | External volume for schema | String | Optional | null | Valid external volume name | Must exist if specified | None | LOW | External storage reference |
| catalog | Catalog name for schema | String | Optional | null | Valid catalog identifier | Must exist if specified | None | LOW | Catalog location identifier |
| classificationProfile | Data classification profile | String | Optional | null | Valid profile name | Must exist if specified | None | LOW | Data governance feature |
| tag | Schema tag assignment | String | Optional | null | Valid tag syntax | Must be valid tag format | None | LOW | Object tagging |
| replaceInvalidCharacters | Replace invalid characters in names | Boolean | Optional | false | true/false | None | None | LOW | Character validation control |
| storageSerializationPolicy | Storage serialization policy | String | Optional | null | Valid policy name | Must exist if specified | None | LOW | Storage optimization setting |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE SCHEMA` - Valid
   - `CREATE SCHEMA IF NOT EXISTS` - Valid  
   - `CREATE OR REPLACE SCHEMA IF NOT EXISTS` - Invalid

2. **transient** and **dataRetentionTimeInDays > 0** - Transient schemas must have 0 retention
   - `CREATE TRANSIENT SCHEMA` with `DATA_RETENTION_TIME_IN_DAYS = 0` - Valid
   - `CREATE TRANSIENT SCHEMA` with `DATA_RETENTION_TIME_IN_DAYS = 7` - Invalid

### Required Combinations
1. If **cloneSource** is specified, schema must not exist (unless using OR REPLACE)
2. If **transient** is true, **dataRetentionTimeInDays** must be 0 or omitted

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Schema Creation
```sql
-- Simple schema creation
CREATE SCHEMA basic_schema;
```
**Expected Behavior**: Schema created with default settings
**Test Validation**: Verify schema exists in system views with default properties

### Example 2: Transient Schema with Zero Retention
```sql
-- Transient schema for temporary data
CREATE TRANSIENT SCHEMA transient_schema
  COMMENT = 'Temporary schema with no Time Travel';
```
**Expected Behavior**: Schema created with no Time Travel or Fail-safe capabilities
**Test Validation**: Verify transient property and zero retention in system views

### Example 3: Schema with Managed Access and Properties
```sql
-- Schema with centralized access control and custom settings
CREATE SCHEMA managed_schema
  WITH MANAGED ACCESS
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  COMMENT = 'Schema with centralized access control';
```
**Expected Behavior**: Schema created with managed access and custom retention
**Test Validation**: Verify managed access flag and retention settings

### Example 4: OR REPLACE Schema with Advanced Settings
```sql
-- Replace existing schema with comprehensive configuration
CREATE OR REPLACE SCHEMA replacement_schema
  DEFAULT_DDL_COLLATION = 'en-ci'
  PIPE_EXECUTION_PAUSED = TRUE
  QUOTED_IDENTIFIERS_IGNORE_CASE = FALSE
  COMMENT = 'Replaced schema with advanced settings';
```
**Expected Behavior**: Existing schema replaced with new configuration
**Test Validation**: Verify schema replaced and all properties set correctly

### Example 5: Conditional Schema Creation
```sql
-- Create schema only if it doesn't exist
CREATE SCHEMA IF NOT EXISTS conditional_schema
  DATA_RETENTION_TIME_IN_DAYS = 14
  COMMENT = 'Only created if it does not exist';
```
**Expected Behavior**: Schema created if missing, no action if exists
**Test Validation**: Verify idempotent behavior and no modification of existing schema

### Example 6: Schema Clone with Point-in-Time Recovery
```sql
-- Clone schema from specific point in time
CREATE SCHEMA cloned_schema CLONE source_schema
  AT (TIMESTAMP => '2024-01-01 12:00:00'::TIMESTAMP)
  COMMENT = 'Schema cloned from specific timestamp';
```
**Expected Behavior**: Schema created as copy of source at specified time
**Test Validation**: Verify clone operation and timestamp-based recovery

**XML Example with additional attributes**:
```xml
<createSchema schemaName="my_schema"
              databaseName="my_database"
              catalog="my_catalog"
              tag="environment=prod"
              classificationProfile="pii_profile"
              externalVolume="s3_volume"
              replaceInvalidCharacters="true"
              storageSerializationPolicy="optimized"
              cloneFrom="source_schema"
              comment="Advanced schema with multiple attributes"/>
```

### Example 7: Task Management Configuration
```sql
-- Schema with task management settings
CREATE SCHEMA task_managed_schema
  SUSPEND_TASK_AFTER_NUM_FAILURES = 5
  TASK_AUTO_RETRY_ATTEMPTS = 3
  USER_TASK_TIMEOUT_MS = 300000
  COMMENT = 'Schema with custom task management settings';
```
**Expected Behavior**: Schema created with custom task execution parameters
**Test Validation**: Verify task management properties in system views

### Example 8: Error Case - Mutual Exclusivity Violation
```sql
-- Invalid: Cannot use both OR REPLACE and IF NOT EXISTS
CREATE OR REPLACE SCHEMA IF NOT EXISTS invalid_schema;
```
**Expected Behavior**: Error thrown about mutual exclusivity violation
**Test Validation**: Verify exact error message matches Snowflake behavior

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: Basic CREATE SCHEMA Operations
**Test File**: `CreateSchemaBasicTest.java`
- **Test 1.1**: Basic schema creation without additional options
- **Test 1.2**: Schema creation with comment
- **Test 1.3**: Schema creation with custom retention settings
- **Test 1.4**: Validation: Schema name cannot be null/empty
- **Test 1.5**: SQL Generation: Verify exact SQL format for basic creation
- **Test 1.6**: SQL Generation: Verify proper identifier quoting

#### Scenario Group 2: OR REPLACE Operations
**Test File**: `CreateSchemaOrReplaceTest.java`
- **Test 2.1**: OR REPLACE schema creation
- **Test 2.2**: OR REPLACE with comprehensive property settings
- **Test 2.3**: OR REPLACE with managed access
- **Test 2.4**: Validation: Cannot combine with IF NOT EXISTS
- **Test 2.5**: SQL Generation: Verify exact SQL format for OR REPLACE

#### Scenario Group 3: IF NOT EXISTS Operations
**Test File**: `CreateSchemaIfNotExistsTest.java`
- **Test 3.1**: IF NOT EXISTS schema creation
- **Test 3.2**: IF NOT EXISTS with property settings
- **Test 3.3**: Validation: Cannot combine with OR REPLACE
- **Test 3.4**: SQL Generation: Verify exact SQL format for IF NOT EXISTS

#### Scenario Group 4: Transient Schema Operations
**Test File**: `CreateSchemaTransientTest.java`
- **Test 4.1**: Basic transient schema creation
- **Test 4.2**: Transient schema with zero retention (explicit)
- **Test 4.3**: Validation: Transient schemas cannot have retention > 0
- **Test 4.4**: SQL Generation: Verify TRANSIENT keyword placement

#### Scenario Group 5: Clone Operations
**Test File**: `CreateSchemaCloneTest.java`
- **Test 5.1**: Basic schema clone operation
- **Test 5.2**: Schema clone with timestamp recovery
- **Test 5.3**: Schema clone with offset recovery
- **Test 5.4**: Validation: Source schema must exist
- **Test 5.5**: SQL Generation: Verify exact clone SQL format

#### Scenario Group 6: Advanced Feature Operations
**Test File**: `CreateSchemaAdvancedTest.java`
- **Test 6.1**: Managed access schema creation
- **Test 6.2**: Schema with pipe execution paused
- **Test 6.3**: Schema with custom collation settings
- **Test 6.4**: Schema with task management configuration
- **Test 6.5**: SQL Generation: Verify advanced features SQL format

#### Scenario Group 7: Validation and Error Conditions
**Test File**: `CreateSchemaValidationTest.java`
- **Test 7.1**: Invalid schema name format (error)
- **Test 7.2**: Retention time constraint violations (error)
- **Test 7.3**: Comment length validation (error)
- **Test 7.4**: Mutual exclusivity violations (error)
- **Test 7.5**: Invalid collation specification (error)
- **Test 7.6**: Verify all error messages match Snowflake behavior

#### Scenario Group 8: SQL Generation Integration
**Test File**: `CreateSchemaSqlGenerationTest.java`
- **Test 8.1**: SQL generation for all property combinations
- **Test 8.2**: Proper keyword ordering in generated SQL
- **Test 8.3**: Identifier quoting in generated SQL
- **Test 8.4**: Complex property combinations SQL format
- **Test 8.5**: Verification against actual Snowflake SQL syntax

### Integration Test Scenarios
**Test File**: `createSchema_integration_test.xml`
- **Integration 1**: End-to-end schema creation with property verification
- **Integration 2**: OR REPLACE behavior with existing schema
- **Integration 3**: Clone operation with point-in-time recovery
- **Integration 4**: Error handling with invalid parameters
- **Integration 5**: Managed access and privilege inheritance testing

## 5. Test Scenarios

Based on the mutual exclusivity rules and features, we need the following test files:
1. **createSchema.xml** - Tests basic creation, transient, managed access, retention settings, comments
2. **createOrReplaceSchema.xml** - Tests OR REPLACE functionality (separate due to mutual exclusivity with IF NOT EXISTS)
3. **createSchemaIfNotExists.xml** - Tests IF NOT EXISTS functionality (separate due to mutual exclusivity with OR REPLACE)
4. **createSchemaAdvanced.xml** - Tests advanced features like pipeExecutionPaused, defaultDdlCollation, cloning

## 6. Validation Rules

1. **schemaName** validation:
   - Cannot be null or empty
   - Must be valid Snowflake identifier (alphanumeric, underscore, dollar sign)
   - Maximum 255 characters
   - Case-insensitive unless quoted

2. **Mutual exclusivity validation**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"

3. **Transient schema validation**:
   - If transient=true and dataRetentionTimeInDays > 0, throw: "Transient schemas must have DATA_RETENTION_TIME_IN_DAYS = 0"

4. **Retention time validation**:
   - dataRetentionTimeInDays must be between 0 and 90
   - maxDataExtensionTimeInDays must be between 0 and 90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

5. **Comment validation**:
   - Maximum 256 characters

## 7. Expected Behaviors

1. **OR REPLACE behavior**:
   - Preserves grants on the schema
   - Drops all objects within the schema
   - Maintains schema ownership

2. **IF NOT EXISTS behavior**:
   - Succeeds silently if schema exists
   - Does not modify existing schema

3. **TRANSIENT behavior**:
   - No Time Travel (0 day retention)
   - No Fail-safe (objects purged after 1 day)
   - Lower storage costs

4. **MANAGED ACCESS behavior**:
   - Only schema owner can grant privileges
   - Centralizes access control

## 8. Error Conditions

1. Schema already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid schema name
3. Insufficient privileges
4. Clone source schema doesn't exist
5. Invalid retention time values

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "SQL Generator override for CREATE SCHEMA"
    - "Parameter validation for all Snowflake-specific attributes"
    - "Mutual exclusivity validation (OR REPLACE vs IF NOT EXISTS)"
    - "SQL generation for all feature combinations"
    
  MEDIUM_PRIORITY_TESTS:
    - "Transient schema constraints and validation"
    - "Clone operation parameter handling"
    - "Advanced feature integration (managed access, pipes)"
    
  LOW_PRIORITY_TESTS:
    - "Edge cases and boundary conditions"
    - "Complex property combination scenarios"

GREEN_PHASE_APPROACH:
  IMPLEMENTATION_STRATEGY: "SQL Generator override pattern with parameter validation"
  VALIDATION_STRATEGY: "Early validation of mutual exclusivity and constraints"
  SQL_GENERATION_STRATEGY: "Template-based with conditional clause generation"

REFACTOR_PHASE_FOCUS:
  - "Extract common validation logic"
  - "Optimize SQL generation templates"
  - "Improve parameter handling consistency"
  - "Performance optimization for complex schemas"
```

### Implementation Pattern Selection
**PATTERN**: Existing Changetype Extension Pattern
**RATIONALE**: Core Liquibase CREATE SCHEMA exists but needs Snowflake-specific SQL generation
**COMPLEXITY**: Medium - SQL generation override with complex parameter validation

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.sqlgenerator.SqlGenerator → CreateSchemaSqlGeneratorSnowflake"

REQUIRED_XSD_ELEMENTS:
  - "Enhancement to existing createSchema XSD with Snowflake namespace attributes"
  - "Snowflake-specific attributes in snowflake: namespace"
```

## QUALITY_VALIDATION

### Requirements Completeness Checklist
- [✓] YAML metadata headers complete
- [✓] Complete SQL syntax documented with all variations
- [✓] Comprehensive attribute analysis table with 8+ columns
- [✓] Minimum 8 comprehensive SQL examples provided
- [✓] Test scenario matrix covering all feature combinations
- [✓] Implementation guidance with TDD approach specified
- [✓] Mutual exclusivity rules clearly documented
- [✓] Error conditions and validation requirements specified

### Implementation Readiness Assessment
- [✓] Extension pattern clearly defined for existing changetype
- [✓] SQL generation requirements specified for all scenarios
- [✓] Complex parameter validation rules documented
- [✓] Test scenarios comprehensive and actionable
- [✓] Implementation complexity appropriate for estimated time

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale

## 9. Implementation Notes

- Schema names are automatically converted to uppercase unless quoted
- Comments are stored in INFORMATION_SCHEMA.SCHEMATA
- Transient property cannot be changed after creation
- Managed access can be enabled/disabled with ALTER SCHEMA
- Clone operations create zero-copy clones with independent lifecycle
- Task management settings affect all tasks created within the schema