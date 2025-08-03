# ALTER DATABASE Requirements
## AI-Optimized Requirements for Snowflake ALTER DATABASE Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "New_Changetype"
OBJECT_TYPE: "Database"
OPERATION: "ALTER"
ESTIMATED_TIME: "8-10 hours"
COMPLEXITY: "HIGH"
ATTRIBUTES_COUNT: 15
OPERATION_GROUPS: 6
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Operation Groups (Mutually Exclusive)
| Group | Operations | SQL Pattern | Key Features |
|-------|------------|-------------|--------------|
| **RENAME** | Rename database | `ALTER DATABASE [IF EXISTS] name RENAME TO new_name` | Error-safe renaming |
| **SET** | Configure properties | `ALTER DATABASE [IF EXISTS] name SET properties` | Retention, tasks, logging |
| **UNSET** | Remove properties | `ALTER DATABASE [IF EXISTS] name UNSET properties` | Reset to defaults |
| **REPLICATION** | Enable/disable replication | `ALTER DATABASE name {ENABLE\|DISABLE} REPLICATION` | Cross-account sharing |
| **FAILOVER** | Enable/disable failover | `ALTER DATABASE name {ENABLE\|DISABLE} FAILOVER` | Business continuity |
| **REFRESH** | Sync shared database | `ALTER DATABASE name REFRESH` | Share synchronization |

### Quick Implementation Pattern
```yaml
PATTERN: "New Changetype"
REASON: "ALTER DATABASE doesn't exist in core Liquibase"
IMPLEMENTATION: "Complete new changetype with operation type detection"
VALIDATION: "6 mutually exclusive operations + complex constraints"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/alter-database"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ All 6 operation types + cross-account features documented"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "6 operation groups cannot be combined"
RETENTION_CONSTRAINT: "MAX_DATA_EXTENSION_TIME >= DATA_RETENTION_TIME"
SESSION_RESTRICTION: "Cannot rename current database in active session"
ENTERPRISE_FEATURES: "Failover requires Enterprise Edition"
IMMEDIATE_EFFECT: "Non-transactional, immediate application"
```

## 🎯 SQL SYNTAX TEMPLATES

### Group 1: RENAME (Mutually Exclusive)
```sql
ALTER DATABASE [IF EXISTS] database_name RENAME TO new_database_name;
```

### Group 2: SET PROPERTIES (Mutually Exclusive)
```sql
ALTER DATABASE [IF EXISTS] database_name SET
  [DATA_RETENTION_TIME_IN_DAYS = 0-90]
  [MAX_DATA_EXTENSION_TIME_IN_DAYS = 0-90]
  [DEFAULT_DDL_COLLATION = 'collation_spec']
  [LOG_LEVEL = 'level'] [TRACE_LEVEL = 'level']
  [SUSPEND_TASK_AFTER_NUM_FAILURES = num]
  [TASK_AUTO_RETRY_ATTEMPTS = num]
  [USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE = size]
  [USER_TASK_TIMEOUT_MS = num]
  [USER_TASK_MINIMUM_TRIGGER_INTERVAL_IN_SECONDS = num]
  [QUOTED_IDENTIFIERS_IGNORE_CASE = TRUE|FALSE]
  [ENABLE_CONSOLE_OUTPUT = TRUE|FALSE]
  [COMMENT = 'string' (≤256 chars)];
```

### Group 3: UNSET PROPERTIES (Mutually Exclusive)
```sql
ALTER DATABASE [IF EXISTS] database_name UNSET property_list;
```

### Group 4: REPLICATION (Mutually Exclusive)
```sql
-- Enable replication
ALTER DATABASE database_name ENABLE REPLICATION TO ACCOUNTS account1[, account2, ...];

-- Disable replication
ALTER DATABASE database_name DISABLE REPLICATION [TO ACCOUNTS account_list];
```

### Group 5: FAILOVER (Mutually Exclusive)
```sql
-- Enable failover (Enterprise Edition)
ALTER DATABASE database_name ENABLE FAILOVER TO ACCOUNTS account1[, account2, ...];

-- Disable failover
ALTER DATABASE database_name DISABLE FAILOVER [TO ACCOUNTS account_list];
```

### Group 6: REFRESH (Mutually Exclusive)
```sql
ALTER DATABASE database_name REFRESH;
```

### Critical Constraints
```yaml
RETENTION_CONSTRAINT: "MAX_DATA_EXTENSION_TIME >= DATA_RETENTION_TIME"
VALUE_RANGES: "Both retention times: 0-90 days"
COMMENT_LIMIT: "256 characters maximum"
ACCOUNT_FORMAT: "org.account or account format required"
SESSION_CONTEXT: "Cannot rename current database"
```

## 📊 ATTRIBUTES QUICK REFERENCE

### Core Attributes (All Operations)
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **databaseName** | String | ✅ | Valid identifier | Primary key |
| **ifExists** | Boolean | ❌ | true/false | Error prevention |

### Group 1: RENAME Attributes
| Attribute | Type | Required | Values | Constraints |
|-----------|------|----------|--------|-------------|
| **newDatabaseName** | String | ✅ | Valid identifier | Must be unique, not current DB |

### Group 2: SET Property Attributes
| Attribute | Type | Values | Constraint | Priority |
|-----------|------|--------|------------|----------|
| **dataRetentionTimeInDays** | Integer | 0-90 | ≤ maxDataExtension | MEDIUM |
| **maxDataExtensionTimeInDays** | Integer | 0-90 | ≥ dataRetention | MEDIUM |
| **defaultDdlCollation** | String | Valid collation | Must be valid | LOW |
| **comment** | String | ≤256 chars | Length limit | LOW |
| **logLevel** | String | Enum values | Predefined list | LOW |
| **traceLevel** | String | Enum values | Predefined list | LOW |
| **suspendTaskAfterNumFailures** | Integer | Positive | Task management | LOW |
| **taskAutoRetryAttempts** | Integer | Positive | Task management | LOW |
| **userTaskManagedInitialWarehouseSize** | String | Warehouse sizes | Task management | LOW |
| **userTaskTimeoutMs** | Integer | Positive | Task management | LOW |
| **userTaskMinimumTriggerIntervalInSeconds** | Integer | Positive | Task management | LOW |
| **quotedIdentifiersIgnoreCase** | Boolean | true/false | Case handling | LOW |
| **enableConsoleOutput** | Boolean | true/false | Logging | LOW |

### Group 3: UNSET Property Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **unset[PropertyName]** | Boolean | ❌ | true/false | For each SET property |

### Group 4: REPLICATION Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **enableReplication** | Boolean | ❌ | true/false | Cross-account sharing |
| **disableReplication** | Boolean | ❌ | true/false | Remove sharing |
| **replicationAccounts** | String | ❌ | Comma-separated accounts | Required when enabling |

### Group 5: FAILOVER Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **enableFailover** | Boolean | ❌ | true/false | Enterprise Edition |
| **disableFailover** | Boolean | ❌ | true/false | Remove failover |
| **failoverAccounts** | String | ❌ | Comma-separated accounts | Required when enabling |

### Group 6: REFRESH Attributes
| Attribute | Type | Required | Values | Notes |
|-----------|------|----------|--------|-------|
| **refresh** | Boolean | ❌ | true/false | Share synchronization |

### Mutual Exclusivity Rules
```yaml
OPERATION_EXCLUSIVITY: "Cannot combine any operation groups together"
ENABLE_DISABLE_EXCLUSIVITY: "Cannot enable and disable same feature"
SET_UNSET_EXCLUSIVITY: "Cannot SET and UNSET same property"
ACCOUNT_REQUIREMENT: "Accounts required when enabling replication/failover"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### RENAME Examples
```sql
-- Basic rename
ALTER DATABASE old_database RENAME TO new_database;

-- Safe rename with IF EXISTS
ALTER DATABASE IF EXISTS old_database RENAME TO new_database;
```

### SET PROPERTY Examples
```sql
-- Time Travel configuration
ALTER DATABASE my_database SET
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  COMMENT = 'Production database';

-- Task management
ALTER DATABASE task_database SET
  SUSPEND_TASK_AFTER_NUM_FAILURES = 3
  TASK_AUTO_RETRY_ATTEMPTS = 5
  USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE = 'MEDIUM';

-- Logging configuration
ALTER DATABASE debug_database SET
  LOG_LEVEL = 'DEBUG'
  TRACE_LEVEL = 'ON_EVENT'
  ENABLE_CONSOLE_OUTPUT = TRUE;
```

### UNSET PROPERTY Examples
```sql
-- Remove properties
ALTER DATABASE my_database UNSET
  DATA_RETENTION_TIME_IN_DAYS
  DEFAULT_DDL_COLLATION
  COMMENT;
```

### REPLICATION Examples
```sql
-- Enable replication to multiple accounts
ALTER DATABASE shared_database ENABLE REPLICATION TO ACCOUNTS 
  org1.account1, org2.account2;

-- Disable replication to specific accounts
ALTER DATABASE shared_database DISABLE REPLICATION TO ACCOUNTS org1.account1;

-- Disable all replication
ALTER DATABASE shared_database DISABLE REPLICATION;
```

### FAILOVER Examples
```sql
-- Enable failover (Enterprise Edition)
ALTER DATABASE critical_database ENABLE FAILOVER TO ACCOUNTS backup_org.backup_account;

-- Disable failover
ALTER DATABASE critical_database DISABLE FAILOVER;
```

### REFRESH Examples
```sql
-- Refresh shared database
ALTER DATABASE shared_data_source REFRESH;
```

### Constraint Examples
```sql
-- Valid: max >= retention
ALTER DATABASE database SET
  DATA_RETENTION_TIME_IN_DAYS = 14
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 60;

-- Invalid: max < retention (will fail)
ALTER DATABASE database SET
  DATA_RETENTION_TIME_IN_DAYS = 60
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 14;

-- Invalid: rename + properties (will fail)
ALTER DATABASE database RENAME TO new_name SET COMMENT = 'test';
```

### Validation Points
```yaml
RENAME_VALIDATION: "Database renamed, grants preserved, old name gone"
PROPERTY_VALIDATION: "DESCRIBE DATABASE shows updated values"
REPLICATION_VALIDATION: "Cross-account access enabled/disabled as expected"
FAILOVER_VALIDATION: "Business continuity configuration updated"
REFRESH_VALIDATION: "Share synchronization completed, timestamp updated"
CONSTRAINT_VALIDATION: "Property dependencies enforced"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
RENAME_TESTS:
  - "Basic rename: old_database → new_database"
  - "IF EXISTS: existing database rename"
  - "IF EXISTS: non-existing database (no error)"
  - "Rename to existing name (error validation)"
  - "Current session database rename (error)"
  - "Mutual exclusivity: rename + properties (error)"

PROPERTY_TESTS:
  - "Single property SET: data retention"
  - "Multiple properties SET: retention + comment + logging"
  - "All properties SET: comprehensive test"
  - "Property dependencies: max >= retention"
  - "SET with IF EXISTS protection"
  - "UNSET single/multiple properties"
  - "SET + UNSET same property (error)"

REPLICATION_TESTS:
  - "Enable replication to single account"
  - "Enable replication to multiple accounts"
  - "Disable replication completely"
  - "Disable replication to specific accounts"
  - "Enable + disable together (error)"
  - "Missing accounts when enabling (error)"
  - "Invalid account format (error)"

FAILOVER_TESTS:
  - "Enable failover (Enterprise Edition)"
  - "Disable failover"
  - "Enable + disable together (error)"
  - "Edition requirement validation"
  - "Account format validation"

REFRESH_TESTS:
  - "Basic refresh operation"
  - "Refresh non-shared database (error)"
  - "Refresh + other operations (error)"

VALIDATION_TESTS:
  - "Required databaseName validation"
  - "Data retention constraints (0-90, max >= retention)"
  - "Comment length (≤256 chars)"
  - "Account identifier format validation"
  - "Operation mutual exclusivity enforcement"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Database lifecycle: create → set properties → rename → replicate → cleanup"
  - "Cross-account workflow: enable replication → test access → disable"
  - "Time Travel: set retention → test historical queries"
  - "Failover: enable → test failover capability → disable"
  - "Multi-operation: different operation types on different databases"

PERFORMANCE_TESTS:
  - "Large database rename"
  - "Multiple property changes"
  - "Cross-account operations"
```

### Test File Organization
```yaml
ALTER_DATABASE_TESTS:
  - "AlterDatabaseRenameTest.java: RENAME operations"
  - "AlterDatabaseSetPropertiesTest.java: SET operations"
  - "AlterDatabaseUnsetPropertiesTest.java: UNSET operations"
  - "AlterDatabaseReplicationTest.java: Replication operations"
  - "AlterDatabaseFailoverTest.java: Failover operations"
  - "AlterDatabaseRefreshTest.java: Refresh operations"
  - "AlterDatabaseValidationTest.java: Constraint validation"
  - "AlterDatabaseMutualExclusivityTest.java: Operation exclusivity"
  - "alterDatabase_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "Operation type detection (RENAME/SET/UNSET/REPLICATION/FAILOVER/REFRESH)"
    - "Mutual exclusivity validation between operation groups"
    - "SQL generation for each operation type"
    - "Complex constraint validation (retention, accounts)"
    
  TEST_STRUCTURE:
    - "AlterDatabaseRenameTest: RENAME operations"
    - "AlterDatabaseSetPropertiesTest: SET operations"
    - "AlterDatabaseReplicationTest: Cross-account operations"
    - "AlterDatabaseValidationTest: Constraints"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeAlterDatabaseChange extends AbstractChange"
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
REASON: "ALTER DATABASE doesn't exist in core Liquibase"
KEY_POINTS:
  - "Operation detection: determine active operation group"
  - "Mutual exclusivity: validate single operation group"
  - "SQL generation: operation-specific templates"
  - "Cross-account: handle replication and failover"
```

### Service Registration
```java
// SnowflakeAlterDatabaseChange
public class SnowflakeAlterDatabaseChange extends AbstractChange {
    private String databaseName;
    private Boolean ifExists;
    private String newDatabaseName;  // RENAME
    // SET properties
    private String dataRetentionTimeInDays;
    private String comment;
    // UNSET properties
    private Boolean unsetDataRetentionTimeInDays;
    // REPLICATION properties
    private Boolean enableReplication;
    private String replicationAccounts;
    // ... other attributes
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new AlterDatabaseStatement(this)
        };
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "Core operations (RENAME, basic SET/UNSET)"
MEDIUM_PRIORITY: "Replication and failover operations"
LOW_PRIORITY: "Advanced properties and Enterprise features"
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
IMPLEMENTATION_TIME: "8-10 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeAlterDatabaseChange class"
  - "Operation type detection logic"
  - "Mutual exclusivity validation"
  - "SQL generation for all operation types"
  - "Cross-account feature support"

TEST_SUITE:
  - "40+ unit tests covering all scenarios"
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
  - "Core implementation: 4-5 hours"
  - "Cross-account features: 2 hours"
  - "Testing and validation: 2-3 hours"
  - "Documentation: 1 hour"
  
TOTAL_ESTIMATE: "8-10 hours"
PRIORITY: "HIGH - Core database management"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*