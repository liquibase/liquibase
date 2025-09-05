# ALTER SCHEMA Requirements
## AI-Optimized Requirements for Snowflake ALTER SCHEMA Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "SQL_Generator_Override"
OBJECT_TYPE: "Schema"
OPERATION: "ALTER"
ESTIMATED_TIME: "5-6 hours"
COMPLEXITY: "MEDIUM"
ATTRIBUTES_COUNT: 30
OPERATION_GROUPS: 3
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Operation Groups (Mutually Exclusive)
| Group | Operations | SQL Pattern | Attributes |
|-------|------------|-------------|------------|
| **RENAME** | Schema rename | `ALTER SCHEMA [IF EXISTS] name RENAME TO new_name` | 2 attributes |
| **PROPERTIES** | SET/UNSET properties | `ALTER SCHEMA [IF EXISTS] name SET/UNSET properties` | 22 attributes |
| **ACCESS** | Enable/disable managed access | `ALTER SCHEMA [IF EXISTS] name ENABLE/DISABLE MANAGED ACCESS` | 2 attributes |

### Quick Implementation Pattern
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake-specific syntax differs from standard SQL"
IMPLEMENTATION: "Override generateSql() in SnowflakeAlterSchemaGenerator"
VALIDATION: "Mutual exclusivity + property constraints"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### 🏗️ Liquibase Architectural Mapping
```yaml
CRITICAL_UNDERSTANDING: "Snowflake DATABASE maps to Liquibase CATALOG"
SNOWFLAKE_HIERARCHY: "DATABASE.SCHEMA.OBJECT"
LIQUIBASE_HIERARCHY: "CATALOG.SCHEMA.OBJECT"
ATTRIBUTE_MAPPING: "Use catalogName (not databaseName) for parent database context"
```

**Context Clarification**:
- **Schema Operations**: Use `catalogName` to specify the parent database
- **Database Operations**: Use `databaseName` to specify the database being created/modified
- **Implementation**: Java classes use `catalogName` for consistency with Liquibase architecture

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/alter-schema"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ Complete syntax, properties, constraints, examples"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "3 operation groups cannot be combined"
PROPERTY_DEPENDENCIES: "MAX_DATA_EXTENSION_TIME >= DATA_RETENTION_TIME"
IF_EXISTS_SUPPORT: "All operations support IF EXISTS clause"
IMMEDIATE_EFFECT: "Non-transactional, immediate application"
```

## 🎯 SQL SYNTAX TEMPLATES

### Group 1: RENAME (Mutually Exclusive)
```sql
ALTER SCHEMA [IF EXISTS] schema_name RENAME TO new_schema_name;
```

### Group 2: PROPERTIES (Mutually Exclusive)
```sql
-- SET Properties
ALTER SCHEMA [IF EXISTS] schema_name SET
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
  [PIPE_EXECUTION_PAUSED = TRUE|FALSE]
  [COMMENT = 'string' (≤256 chars)];

-- UNSET Properties
ALTER SCHEMA [IF EXISTS] schema_name UNSET property_list;
```

### Group 3: ACCESS (Mutually Exclusive)
```sql
ALTER SCHEMA [IF EXISTS] schema_name {ENABLE|DISABLE} MANAGED ACCESS;
```

### Critical Constraints
```yaml
DATA_RETENTION_CONSTRAINT: "MAX_DATA_EXTENSION_TIME >= DATA_RETENTION_TIME"
VALUE_RANGES: "Both retention times: 0-90 days"
COMMENT_LIMIT: "256 characters maximum"
ENUM_VALIDATION: "LOG_LEVEL, TRACE_LEVEL, warehouse sizes have specific values"
```

## 📊 COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Attributes (All Operations)
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **schemaName** | String | Required | N/A | Valid identifier | Must exist | None | HIGH | Primary schema identifier |
| **ifExists** | Boolean | Optional | false | true/false | None | None | MEDIUM | Error prevention for non-existent schemas |
| **catalogName** | String | Optional | Current DB | Valid identifier | Must exist | None | MEDIUM | Parent database name (Liquibase maps Snowflake DATABASE → CATALOG) |
| **operationType** | String | Optional | Detected | RENAME/SET/UNSET/ACCESS | Single operation type | Mutually exclusive | HIGH | Operation type detection |

### Group 1: RENAME Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **newName** | String | Required (for RENAME) | N/A | Valid identifier | Must be unique | RENAME operation only | HIGH | Target schema name |

### Group 2: PROPERTY Attributes (SET/UNSET)
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **dataRetentionTimeInDays** | Integer | Optional | Current | 0-90 | ≤ maxDataExtension | SET/UNSET operations | MEDIUM | Time Travel retention period |
| **maxDataExtensionTimeInDays** | Integer | Optional | Current | 0-90 | ≥ dataRetention | SET/UNSET operations | MEDIUM | Maximum Time Travel extension |
| **defaultDdlCollation** | String | Optional | Current | Valid collation | Must be valid | SET/UNSET operations | LOW | Default collation for objects |
| **comment** | String | Optional | Current | String ≤256 chars | Length limit | SET/UNSET operations | HIGH | Schema description |
| **pipeExecutionPaused** | Boolean | Optional | false | true/false | None | SET/UNSET operations | MEDIUM | Pipe execution control |
| **newPipeExecutionPaused** | Boolean | Optional | Current | true/false | None | SET operations only | MEDIUM | New pipe execution state |
| **unsetDataRetentionTimeInDays** | Boolean | Optional | false | true/false | None | UNSET operations only | MEDIUM | Remove retention setting |
| **newDataRetentionTimeInDays** | Integer | Optional | Current | 0-90 | ≤ maxDataExtension | SET operations only | MEDIUM | New retention period |
| **newComment** | String | Optional | Current | String ≤256 chars | Length limit | SET operations only | HIGH | New schema comment |
| **dropComment** | Boolean | Optional | false | true/false | None | UNSET operations only | MEDIUM | Remove comment |
| **managedAccess** | Boolean | Optional | Current | true/false | None | SET operations only | MEDIUM | Managed access control |
| **disableManagedAccess** | Boolean | Optional | false | true/false | None | Operations only | MEDIUM | Disable managed access |
| **unsetComment** | Boolean | Optional | false | true/false | None | UNSET operations only | MEDIUM | Remove comment setting |
| **unsetMaxDataExtensionTimeInDays** | Boolean | Optional | false | true/false | None | UNSET operations only | MEDIUM | Remove max extension setting |
| **newDefaultDdlCollation** | String | Optional | Current | Valid collation | Must be valid | SET operations only | LOW | New default collation |
| **unsetPipeExecutionPaused** | Boolean | Optional | false | true/false | None | UNSET operations only | MEDIUM | Remove pipe execution setting |
| **enableManagedAccess** | Boolean | Optional | false | true/false | None | ACCESS operations only | MEDIUM | Enable managed access |
| **newMaxDataExtensionTimeInDays** | Integer | Optional | Current | 0-90 | ≥ dataRetention | SET operations only | MEDIUM | New max extension period |
| **unsetDefaultDdlCollation** | Boolean | Optional | false | true/false | None | UNSET operations only | LOW | Remove collation setting |

### Mutual Exclusivity Rules
```yaml
GROUP_EXCLUSIVITY: "Cannot combine RENAME + PROPERTIES + ACCESS operations"
SET_UNSET_EXCLUSIVITY: "Cannot SET and UNSET same property"
ACCESS_EXCLUSIVITY: "Cannot enable and disable managed access together"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### RENAME Examples
```sql
-- Basic rename
ALTER SCHEMA old_schema RENAME TO new_schema;

-- Safe rename with IF EXISTS
ALTER SCHEMA IF EXISTS old_schema RENAME TO new_schema;
```

### PROPERTY Examples
```sql
-- Time Travel configuration
ALTER SCHEMA my_schema SET
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 14
  COMMENT = 'Production schema';

-- Task management
ALTER SCHEMA task_schema SET
  SUSPEND_TASK_AFTER_NUM_FAILURES = 3
  TASK_AUTO_RETRY_ATTEMPTS = 5
  USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE = 'MEDIUM';

-- Remove properties
ALTER SCHEMA my_schema UNSET
  DATA_RETENTION_TIME_IN_DAYS
  COMMENT;

-- Logging configuration
ALTER SCHEMA debug_schema SET
  LOG_LEVEL = 'DEBUG'
  TRACE_LEVEL = 'ON_EVENT'
  ENABLE_CONSOLE_OUTPUT = TRUE;

-- Complete example with all attributes
ALTER SCHEMA comprehensive_schema SET
  newPipeExecutionPaused = "true"
  catalogName = "test_db"
  unsetDataRetentionTimeInDays = "false"
  newDataRetentionTimeInDays = "30"
  newComment = "Updated schema comment"
  dropComment = "false"
  schemaName = "test_schema"
  managedAccess = "true"
  disableManagedAccess = "false"
  unsetComment = "false"
  unsetMaxDataExtensionTimeInDays = "false"
  newName = "renamed_schema"
  newDefaultDdlCollation = "utf8"
  unsetPipeExecutionPaused = "false"
  ifExists = "true"
  dataRetentionTimeInDays = "7"
  enableManagedAccess = "true"
  comment = "Schema comment"
  operationType = "SET"
  newMaxDataExtensionTimeInDays = "90"
  unsetDefaultDdlCollation = "false";
```

### ACCESS Examples
```sql
-- Enable managed access
ALTER SCHEMA secure_schema ENABLE MANAGED ACCESS;

-- Disable with safety
ALTER SCHEMA IF EXISTS secure_schema DISABLE MANAGED ACCESS;
```

### Constraint Examples
```sql
-- Valid: max >= retention
ALTER SCHEMA schema SET
  DATA_RETENTION_TIME_IN_DAYS = 30
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 45;

-- Invalid: max < retention (will fail)
ALTER SCHEMA schema SET
  DATA_RETENTION_TIME_IN_DAYS = 45
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30;
```

### Validation Points
```yaml
RENAME_VALIDATION: "Schema exists with new name, old name gone, grants preserved"
PROPERTY_VALIDATION: "DESCRIBE SCHEMA shows updated values"
ACCESS_VALIDATION: "Privilege grants restricted/unrestricted as expected"
CONSTRAINT_VALIDATION: "Property dependencies enforced"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
RENAME_TESTS:
  - "Basic rename: old_schema → new_schema"
  - "IF EXISTS: existing schema rename"
  - "IF EXISTS: non-existing schema (no error)"
  - "Rename to existing name (error validation)"
  - "Special characters in names"
  - "Mutual exclusivity: rename + properties (error)"

PROPERTY_TESTS:
  - "Single property SET: data retention"
  - "Multiple properties SET: retention + comment + logging"
  - "All properties SET: comprehensive test"
  - "Invalid values: constraint validation"
  - "Property dependencies: max >= retention"
  - "SET with IF EXISTS protection"
  - "UNSET single/multiple properties"
  - "SET + UNSET same property (error)"

ACCESS_TESTS:
  - "Enable managed access"
  - "Disable managed access"
  - "Enable + disable together (error)"
  - "Access + properties together (error)"

VALIDATION_TESTS:
  - "Required schemaName validation"
  - "Required newName for rename"
  - "Data retention constraints (0-90, max >= retention)"
  - "Comment length (≤256 chars)"
  - "Enum validation (log levels, warehouse sizes)"
  - "Mutual exclusivity enforcement"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Schema lifecycle: create → set properties → rename → access → cleanup"
  - "Property inheritance: schema properties → contained objects"
  - "Access control: enable → test grants → disable"
  - "Time Travel: set retention → test queries"
  - "Multi-user: different privileges → schema operations"

PERFORMANCE_TESTS:
  - "Large schema rename"
  - "Multiple property changes"
  - "Schema with many objects"
```

### Test File Organization
```yaml
ALTER_SCHEMA_TESTS:
  - "AlterSchemaRenameTest.java: RENAME operations"
  - "AlterSchemaPropertiesTest.java: SET/UNSET operations"
  - "AlterSchemaAccessTest.java: MANAGED ACCESS operations"
  - "AlterSchemaValidationTest.java: Constraint validation"
  - "AlterSchemaMutualExclusivityTest.java: Operation exclusivity"
  - "alterSchema_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "Operation detection (RENAME/SET/UNSET/ACCESS)"
    - "Mutual exclusivity validation"
    - "SQL generation for each operation group"
    - "Property constraint validation"
    
  TEST_STRUCTURE:
    - "AlterSchemaRenameTest: RENAME operations"
    - "AlterSchemaPropertiesTest: SET/UNSET operations"
    - "AlterSchemaAccessTest: MANAGED ACCESS operations"
    - "AlterSchemaValidationTest: Constraints"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeAlterSchemaGenerator extends AbstractSqlGenerator"
    - "supports() returns true for AlterSchemaStatement"
    - "generateSql() with operation group logic"
    - "Validation for mutual exclusivity + constraints"

REFACTOR_PHASE:
  IMPROVEMENTS:
    - "Extract operation-specific helper methods"
    - "Comprehensive validation with clear error messages"
    - "SQL formatting and clause ordering"
    - "Performance optimization"
```

### Pattern Implementation
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake ALTER SCHEMA syntax differs from standard SQL"
KEY_POINTS:
  - "Operation detection: determine active operation group"
  - "Mutual exclusivity: validate single operation group"
  - "Property handling: generate SET/UNSET clauses"
  - "IF EXISTS: add clause across all operations"
```

### Service Registration
```java
// SnowflakeAlterSchemaGenerator
public class SnowflakeAlterSchemaGenerator extends AbstractSqlGenerator<AlterSchemaStatement> {
    @Override
    public boolean supports(AlterSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(AlterSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Implementation with operation group detection
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "Core operations (RENAME, basic SET/UNSET)"
MEDIUM_PRIORITY: "Property constraints and validation"
LOW_PRIORITY: "Advanced properties (task management, logging)"
```

## ✅ IMPLEMENTATION STATUS

### Requirements Quality
```yaml
COMPLETENESS: "✅ All operations, attributes, constraints documented"
SQL_SYNTAX: "✅ Complete with all variations and examples"
VALIDATION: "✅ Mutual exclusivity and constraints defined"
TEST_COVERAGE: "✅ Comprehensive scenarios for all operations"
IMPLEMENTATION_GUIDE: "✅ TDD approach with clear phases"
```

### Readiness Assessment
```yaml
TECHNICAL_READINESS: "✅ READY - All attributes and SQL syntax documented"
COMPLEXITY: "✅ MANAGEABLE - Medium complexity, well-defined rules"
RISK_LEVEL: "✅ LOW - Comprehensive documentation, clear patterns"
IMPLEMENTATION_TIME: "5-6 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeAlterSchemaGenerator class"
  - "Operation group detection logic"
  - "Mutual exclusivity validation"
  - "SQL generation for all operation types"

TEST_SUITE:
  - "25+ unit tests covering all scenarios"
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
  - "Core implementation: 2-3 hours"
  - "Testing and validation: 2 hours"
  - "Documentation: 1 hour"
  
TOTAL_ESTIMATE: "5-6 hours"
PRIORITY: "HIGH - Core schema management"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*