# Snowflake AlterDatabase Requirements Document
## Implementation-Ready Requirements for AlterDatabase Changetype Development

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "4.0"
PHASE: "VALIDATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_COMPLETION_DATE: "2025-08-01"
VALIDATION_COMPLETION_DATE: "2025-08-01"
VALIDATION_METHOD: "Simple INFORMATION_SCHEMA + Manual Documentation Review"
COMPLETENESS_VALIDATION: "COMPLETE - XSD parameters validated via SnowflakeParameterValidationTest"
IMPLEMENTATION_PATTERN: "New_Changetype"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Database"
OPERATION: "ALTER"
NEXT_PHASE: "Phase 3 - TDD Implementation"
ESTIMATED_IMPLEMENTATION_TIME: "8-10 hours (validation complete)"
```

## EXECUTIVE_SUMMARY
```yaml
IMPLEMENTATION_SCOPE: "Complete ALTER DATABASE changetype supporting all Snowflake database modification operations"
KEY_OPERATIONS:
  - "RENAME TO operations with IF EXISTS support"
  - "SET property operations for retention, collation, task management"
  - "UNSET property operations for removing configurations"
  - "Replication and failover management operations"
  - "Database refresh operations for shared databases"
COMPLEXITY_ASSESSMENT: "HIGH - Multiple mutually exclusive operation types with complex parameter interactions"
SUCCESS_CRITERIA: "All 5 operation types implemented with comprehensive validation and cross-account feature support"
```

## OFFICIAL_DOCUMENTATION_ANALYSIS

### Primary Documentation Sources
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/alter-database
- **Version**: Snowflake 2024/2025
- **Last Updated**: 2025-08-01
- **Cross-Reference**: https://docs.snowflake.com/en/user-guide/database-replication-intro (for replication operations)

### Documentation Analysis and Key Insights
- **Multiple Operation Types**: ALTER DATABASE supports 5 distinct operation types that are mutually exclusive
- **Complex Property Management**: Extensive configuration options for Time Travel, task management, and logging
- **Cross-Account Features**: Replication and failover capabilities requiring Enterprise Edition
- **Session Context Restrictions**: Cannot rename the current database in active session

## COMPLETE_SQL_SYNTAX_DEFINITION

### Full Snowflake ALTER DATABASE Syntax
```sql
-- Operation Type 1: RENAME
ALTER DATABASE [ IF EXISTS ] <name> RENAME TO <new_db_name>;

-- Operation Type 2: SET Properties
ALTER DATABASE [ IF EXISTS ] <name> SET
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
  [ COMMENT = '<string_literal>' ];

-- Operation Type 3: UNSET Properties
ALTER DATABASE [ IF EXISTS ] <name> UNSET
  [ DATA_RETENTION_TIME_IN_DAYS ]
  [ MAX_DATA_EXTENSION_TIME_IN_DAYS ]
  [ DEFAULT_DDL_COLLATION ]
  [ LOG_LEVEL ]
  [ TRACE_LEVEL ]
  [ SUSPEND_TASK_AFTER_NUM_FAILURES ]
  [ TASK_AUTO_RETRY_ATTEMPTS ]
  [ USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE ]
  [ USER_TASK_TIMEOUT_MS ]
  [ USER_TASK_MINIMUM_TRIGGER_INTERVAL_IN_SECONDS ]
  [ QUOTED_IDENTIFIERS_IGNORE_CASE ]
  [ ENABLE_CONSOLE_OUTPUT ]
  [ COMMENT ];

-- Operation Type 4a: Enable Replication
ALTER DATABASE <name> ENABLE REPLICATION TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ];

-- Operation Type 4b: Disable Replication
ALTER DATABASE <name> DISABLE REPLICATION [ TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ] ];

-- Operation Type 5a: Enable Failover
ALTER DATABASE <name> ENABLE FAILOVER TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ];

-- Operation Type 5b: Disable Failover
ALTER DATABASE <name> DISABLE FAILOVER [ TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ] ];

-- Operation Type 6: Refresh from Share
ALTER DATABASE <name> REFRESH;
```

### Parameter Dependencies and Constraints
- **Retention Relationship**: MAX_DATA_EXTENSION_TIME_IN_DAYS must be ≥ DATA_RETENTION_TIME_IN_DAYS
- **Replication Requirements**: Account identifiers must be valid and accessible
- **Failover Dependencies**: Requires Enterprise Edition and proper account setup
- **Session Context**: Cannot rename database that is currently in use by session

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

| Attribute | Description | Data Type | Required/Optional | Default | Valid Values | Constraints | Mutual Exclusivity | Implementation Priority | Implementation Notes |
|-----------|-------------|-----------|------------------|---------|--------------|-------------|-------------------|----------------------|-------------------|
| databaseName | Target database name for alteration | String | Required | - | Valid Snowflake identifier | Cannot be null/empty, cannot be current session database for rename | None | HIGH | Case-insensitive, auto-converted to uppercase |
| ifExists | Skip errors if database doesn't exist | Boolean | Optional | false | true/false | None | None | HIGH | Applies to all operation types |
| newDatabaseName | New database name (rename operation) | String | Optional | null | Valid Snowflake identifier | Required for RENAME operations, cannot be existing database name | Mutually exclusive with all SET/UNSET operations | HIGH | Only used in RENAME operations |
| dataRetentionTimeInDays | Set Time Travel retention period | String | Optional | null | 0-90 | Must be ≤ maxDataExtensionTimeInDays | Cannot combine with RENAME/UNSET operations | MEDIUM | Controls Time Travel availability |
| newMaxDataExtensionTimeInDays | Set maximum Time Travel extension | String | Optional | null | 0-90 | Must be ≥ dataRetentionTimeInDays | Cannot combine with RENAME/UNSET operations | MEDIUM | Maximum extension for retention |
| maxDataExtensionTimeInDays | Maximum Time Travel extension | String | Optional | null | 0-90 | Must be ≥ dataRetentionTimeInDays | Cannot combine with RENAME/UNSET operations | MEDIUM | Alternative parameter for max extension |
| newDefaultDdlCollation | Set default database collation | String | Optional | null | Valid collation specification | Must be valid collation identifier | Cannot combine with RENAME/UNSET operations | LOW | Database-level collation setting |
| defaultDdlCollation | Default database collation | String | Optional | null | Valid collation specification | Must be valid collation identifier | Cannot combine with RENAME/UNSET operations | LOW | Alternative parameter for collation |
| comment | Set database description | String | Optional | null | String ≤ 256 chars | Length validation | Cannot combine with RENAME/UNSET operations | LOW | Metadata only |
| replaceComment | Whether to replace existing comment | Boolean | Optional | false | true/false | None | Cannot combine with dropComment | LOW | Comment operation flag |
| dropComment | Whether to drop existing comment | Boolean | Optional | false | true/false | None | Cannot combine with replaceComment | LOW | Comment operation flag |
| unsetDataRetentionTimeInDays | Remove retention configuration | Boolean | Optional | false | true/false | None | Cannot combine with RENAME/SET operations | MEDIUM | Separate UNSET operation |
| unsetMaxDataExtensionTimeInDays | Remove max extension configuration | Boolean | Optional | false | true/false | None | Cannot combine with RENAME/SET operations | MEDIUM | Separate UNSET operation |
| unsetDefaultDdlCollation | Remove collation configuration | Boolean | Optional | false | true/false | None | Cannot combine with RENAME/SET operations | LOW | Separate UNSET operation |
| unsetComment | Remove database comment | Boolean | Optional | false | true/false | None | Cannot combine with RENAME/SET operations | LOW | Separate UNSET operation |
| enableReplication | Enable database replication | String | Optional | null | Valid replication settings | Replication configuration | Cannot combine with RENAME/SET/UNSET operations | MEDIUM | Cross-account feature (implemented as String) |
| replicationAccounts | Target accounts for replication | String | Optional | null | Comma-separated account identifiers | Required when enableReplication is set | Cannot combine with RENAME/SET/UNSET operations | MEDIUM | Format: org1.account1,org2.account2 |
| operationType | Type of ALTER operation | String | Optional | null | RENAME, SET, UNSET | Must match operation being performed | None | HIGH | Operation classification parameter |
| swapWith | Database to swap with | String | Optional | null | Valid database identifier | Used for database swap operations | Cannot combine with other operations | LOW | Advanced database operation |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Operations
1. **RENAME TO** cannot be combined with other operations in same statement
2. **SET** and **UNSET** for same property cannot be in same statement
3. **enableReplication** and **disableReplication** cannot both be true
4. **enableFailover** and **disableFailover** cannot both be true
5. **REFRESH** must be used alone

### Operation Groups
1. **Rename Operation**: Uses only databaseName, newName, ifExists
2. **Property Operations**: Uses SET/UNSET attributes
3. **Replication Operations**: Uses enable/disable replication with accounts
4. **Failover Operations**: Uses enable/disable failover with accounts
5. **Refresh Operation**: Uses only databaseName and refresh

## COMPREHENSIVE_SQL_EXAMPLES

### Example 1: Basic Database Rename with IF EXISTS
```sql
-- Rename database with error handling
ALTER DATABASE IF EXISTS old_database RENAME TO new_database;
```
**Expected Behavior**: Renames database if it exists, no error if it doesn't exist
**Test Validation**: Verify database name change in system views, verify grants preserved

### Example 2: Complex Property SET Operation
```sql
-- Set multiple database properties in single operation
ALTER DATABASE production_database SET
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  DEFAULT_DDL_COLLATION = 'en-ci'
  COMMENT = 'Production database with extended retention';
```
**Expected Behavior**: All properties updated atomically, affects new objects in database
**Test Validation**: Verify all properties set correctly in system views

### Example 3: UNSET Operations for Property Removal
```sql
-- Remove multiple database configurations
ALTER DATABASE my_database UNSET
  DATA_RETENTION_TIME_IN_DAYS
  DEFAULT_DDL_COLLATION
  COMMENT;
```
**Expected Behavior**: Properties reset to system defaults or null
**Test Validation**: Verify properties are unset in system views

### Example 4: Enable Replication to Multiple Accounts
```sql
-- Enable cross-account replication
ALTER DATABASE shared_database ENABLE REPLICATION TO ACCOUNTS org1.account1, org2.account2, org3.account3;
```
**Expected Behavior**: Database enabled for replication to specified accounts
**Test Validation**: Verify replication configuration in system views

### Example 5: Disable Replication with Account Specification
```sql
-- Disable replication to specific accounts
ALTER DATABASE shared_database DISABLE REPLICATION TO ACCOUNTS org1.account1;
```
**Expected Behavior**: Replication disabled for specified accounts only
**Test Validation**: Verify replication status updated correctly

### Example 6: Enable Database Failover (Enterprise Edition)
```sql
-- Configure database for failover
ALTER DATABASE critical_database ENABLE FAILOVER TO ACCOUNTS backup_org.backup_account;
```
**Expected Behavior**: Database configured for business continuity failover
**Test Validation**: Verify failover configuration (requires Enterprise Edition)

### Example 7: Refresh Shared Database
```sql
-- Refresh database created from share
ALTER DATABASE shared_data_source REFRESH;
```
**Expected Behavior**: Database synchronized with latest data from share
**Test Validation**: Verify refresh timestamp updated, data synchronized

### Example 8: Time Travel Configuration with Constraints
```sql
-- Configure Time Travel with proper constraints
ALTER DATABASE analytics_database SET
  DATA_RETENTION_TIME_IN_DAYS = 14
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 60;
```
**Expected Behavior**: Time Travel configured with extension capability
**Test Validation**: Verify retention settings, verify constraint relationship

## TEST_SCENARIO_MATRIX

### Unit Test Scenarios for TDD Implementation

#### Scenario Group 1: RENAME Operations
**Test File**: `AlterDatabaseRenameTest.java`
- **Test 1.1**: Basic rename operation without IF EXISTS
- **Test 1.2**: Rename with IF EXISTS when database exists
- **Test 1.3**: Rename with IF EXISTS when database doesn't exist
- **Test 1.4**: Rename to existing database name (error case)
- **Test 1.5**: Rename current session database (error case)
- **Test 1.6**: Rename with quoted identifiers
- **Test 1.7**: Validation: newName cannot be null when rename requested
- **Test 1.8**: SQL Generation: Verify exact SQL format for all rename variations

#### Scenario Group 2: SET Property Operations
**Test File**: `AlterDatabaseSetPropertiesTest.java`
- **Test 2.1**: Single property SET (data retention)
- **Test 2.2**: Multiple property SET operation (retention + collation + comment)
- **Test 2.3**: Time Travel configuration (retention + max extension)
- **Test 2.4**: Task management properties (timeout + retry settings)
- **Test 2.5**: Console and logging configuration
- **Test 2.6**: Validation: maxDataExtensionTimeInDays ≥ dataRetentionTimeInDays constraint
- **Test 2.7**: Validation: retention values within 0-90 range
- **Test 2.8**: Validation: comment length constraint (≤ 256 chars)
- **Test 2.9**: SQL Generation: Verify exact SQL format for all SET combinations

#### Scenario Group 3: UNSET Property Operations
**Test File**: `AlterDatabaseUnsetPropertiesTest.java`
- **Test 3.1**: UNSET single property (data retention)
- **Test 3.2**: UNSET multiple properties (retention + collation + comment)
- **Test 3.3**: UNSET with IF EXISTS protection
- **Test 3.4**: UNSET all supported properties in single operation
- **Test 3.5**: SQL Generation: Verify exact SQL format for UNSET operations

#### Scenario Group 4: Replication Management Operations
**Test File**: `AlterDatabaseReplicationTest.java`
- **Test 4.1**: Enable replication to single account
- **Test 4.2**: Enable replication to multiple accounts
- **Test 4.3**: Disable replication completely
- **Test 4.4**: Disable replication to specific accounts
- **Test 4.5**: Validation: Cannot enable and disable replication simultaneously
- **Test 4.6**: Validation: replicationAccounts required when enabling
- **Test 4.7**: Validation: account identifier format validation
- **Test 4.8**: SQL Generation: Verify exact SQL format for replication operations

#### Scenario Group 5: Failover Management Operations
**Test File**: `AlterDatabaseFailoverTest.java`
- **Test 5.1**: Enable failover to single account (Enterprise Edition)
- **Test 5.2**: Enable failover to multiple accounts
- **Test 5.3**: Disable failover completely
- **Test 5.4**: Disable failover to specific accounts
- **Test 5.5**: Validation: Cannot enable and disable failover simultaneously
- **Test 5.6**: Validation: failoverAccounts required when enabling
- **Test 5.7**: Edition requirement validation (Enterprise Edition)
- **Test 5.8**: SQL Generation: Verify exact SQL format for failover operations

#### Scenario Group 6: Refresh Operations
**Test File**: `AlterDatabaseRefreshTest.java`
- **Test 6.1**: Basic refresh operation for shared database
- **Test 6.2**: Refresh with IF EXISTS protection
- **Test 6.3**: Validation: refresh only valid for databases created from share
- **Test 6.4**: SQL Generation: Verify exact SQL format for refresh operation

#### Scenario Group 7: Mutual Exclusivity Validation
**Test File**: `AlterDatabaseMutualExclusivityTest.java`
- **Test 7.1**: Cannot combine RENAME with SET operations
- **Test 7.2**: Cannot combine RENAME with UNSET operations
- **Test 7.3**: Cannot combine RENAME with REPLICATION operations
- **Test 7.4**: Cannot combine SET with UNSET operations
- **Test 7.5**: Cannot combine REPLICATION with FAILOVER operations
- **Test 7.6**: Cannot combine any operations with REFRESH
- **Test 7.7**: Validation: Error messages for invalid combinations

#### Scenario Group 8: Error Conditions and Edge Cases
**Test File**: `AlterDatabaseErrorConditionsTest.java`
- **Test 8.1**: Database doesn't exist without IF EXISTS (error)
- **Test 8.2**: Invalid retention time values (error)
- **Test 8.3**: Constraint violation: maxExtension < retention (error)
- **Test 8.4**: Invalid account identifier format (error)
- **Test 8.5**: Current session database rename attempt (error)
- **Test 8.6**: Comment too long (error)
- **Test 8.7**: Invalid collation specification (error)
- **Test 8.8**: Verify all error messages match Snowflake behavior

### Integration Test Scenarios
**Test File**: `alterDatabase_integration_test.xml`
- **Integration 1**: End-to-end RENAME operation with database validation
- **Integration 2**: Complex SET operation with property verification
- **Integration 3**: Replication configuration with cross-account setup
- **Integration 4**: Error handling with non-existent databases
- **Integration 5**: Time Travel configuration with constraint testing

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **alterDatabase.xml** - Basic operations (rename, set/unset properties)
2. **alterDatabaseReplication.xml** - Replication management operations
3. **alterDatabaseFailover.xml** - Failover operations (Enterprise Edition)
4. **alterDatabaseRefresh.xml** - Refresh operations for shared databases

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - For rename: newName cannot be null or empty
   - For replication/failover: accounts list cannot be empty when enabling

2. **Mutual Exclusivity**:
   - Cannot combine rename with other operations
   - Cannot SET and UNSET same property
   - Cannot enable and disable same feature

3. **Value Constraints**:
   - dataRetentionTimeInDays must be 0-90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

## 7. Expected Behaviors

1. **Rename behavior**:
   - All references to the database are updated
   - Grants on the database are preserved
   - Cannot rename current database

2. **Property changes**:
   - Take effect immediately
   - Affect all new objects created in database

3. **Replication**:
   - Enables sharing of database to other accounts
   - Requires appropriate privileges

4. **Failover**:
   - Configures database for business continuity
   - Requires Enterprise Edition or higher

## 8. Error Conditions

1. Database doesn't exist (without IF EXISTS)
2. New name already exists (for rename)
3. Invalid property values
4. Insufficient privileges
5. Current database (for rename)
6. Invalid account identifiers
7. Feature not available in edition (failover)

## IMPLEMENTATION_GUIDANCE

### TDD Implementation Plan
```yaml
RED_PHASE_PRIORITIES:
  HIGH_PRIORITY_TESTS:
    - "Basic operation type detection (RENAME/SET/UNSET/REPLICATION/FAILOVER/REFRESH)"
    - "Parameter validation for all attributes"
    - "Mutual exclusivity validation between operation types"
    - "SQL generation for all operation types"
    
  MEDIUM_PRIORITY_TESTS:
    - "Complex property combinations and constraints"
    - "Cross-account replication and failover features"
    - "Error condition handling and messaging"
    
  LOW_PRIORITY_TESTS:
    - "Enterprise Edition feature validation"
    - "Edge cases and boundary conditions"

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
**RATIONALE**: ALTER DATABASE doesn't exist in core Liquibase and requires complete implementation
**COMPLEXITY**: High - Multiple mutually exclusive operation types requiring sophisticated validation

### Service Registration Requirements
```yaml
REQUIRED_SERVICES:
  - "liquibase.change.Change → AlterDatabaseChange"
  - "liquibase.statement.SqlStatement → AlterDatabaseStatement"
  - "liquibase.sqlgenerator.SqlGenerator → AlterDatabaseSqlGenerator"

REQUIRED_XSD_ELEMENTS:
  - "alterDatabase element with all operation attributes"
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
- [✓] Cross-account features documented with edition requirements

### Handoff to Phase 3 Validation
- [✓] Requirements marked IMPLEMENTATION_READY
- [✓] All quality gates passed
- [✓] TDD implementation plan specified
- [✓] Success criteria defined and measurable
- [✓] Implementation pattern selected with rationale

## 9. Implementation Notes

- ALTER DATABASE is not transactional
- Changes take effect immediately
- Some operations require ACCOUNTADMIN role
- Replication and failover require cross-account setup
- Consider connection context when renaming databases
- Enterprise Edition required for failover operations
- Share-based databases required for refresh operations