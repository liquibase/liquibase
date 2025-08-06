# CREATE SCHEMA Requirements
## AI-Optimized Requirements for Snowflake CREATE SCHEMA Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "SQL_Generator_Override"
OBJECT_TYPE: "Schema"
OPERATION: "CREATE"
ESTIMATED_TIME: "5-6 hours"
COMPLEXITY: "MEDIUM"
ATTRIBUTES_COUNT: 23
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Core Operation Types
| Type | SQL Pattern | Key Features | Notes |
|------|-------------|--------------|-------|
| **BASIC** | `CREATE [TRANSIENT] SCHEMA name` | Standard creation | Core functionality |
| **CONDITIONAL** | `CREATE [OR REPLACE\|IF NOT EXISTS] SCHEMA` | Safe operations | Mutual exclusivity |
| **CLONE** | `CREATE SCHEMA name CLONE source` | Zero-copy cloning | Point-in-time options |
| **ADVANCED** | With managed access, retention, tasks | Full configuration | Enterprise features |

### Quick Implementation Pattern
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake CREATE SCHEMA syntax differs significantly from standard SQL"
IMPLEMENTATION: "Override generateSql() in SnowflakeCreateSchemaGenerator"
VALIDATION: "Mutual exclusivity + transient constraints + cloning validation"
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
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/create-schema"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ Extended CREATE SCHEMA with all Snowflake-specific features"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "OR REPLACE and IF NOT EXISTS cannot be combined"
TRANSIENT_CONSTRAINT: "Transient schemas must have dataRetentionTimeInDays = 0"
RETENTION_CONSTRAINT: "MAX_DATA_EXTENSION_TIME >= DATA_RETENTION_TIME"
CLONING_SUPPORT: "Zero-copy cloning with point-in-time recovery options"
MANAGED_ACCESS: "Centralized privilege management unique to Snowflake"
```

## 🎯 SQL SYNTAX TEMPLATES

### Basic Creation
```sql
CREATE [TRANSIENT] SCHEMA schema_name;
```

### Conditional Creation (Mutually Exclusive)
```sql
-- Safe replacement
CREATE OR REPLACE SCHEMA schema_name;

-- Idempotent creation
CREATE SCHEMA IF NOT EXISTS schema_name;
```

### Cloning
```sql
-- Basic clone
CREATE SCHEMA new_schema CLONE source_schema;

-- Point-in-time clone
CREATE SCHEMA new_schema CLONE source_schema
  AT (TIMESTAMP => '2024-01-01 00:00:00');
```

### Full Configuration
```sql
CREATE [OR REPLACE] [TRANSIENT] SCHEMA [IF NOT EXISTS] schema_name
  [CLONE source_schema [AT|BEFORE (options)]]
  [WITH MANAGED ACCESS]
  [DATA_RETENTION_TIME_IN_DAYS = 0-90]
  [MAX_DATA_EXTENSION_TIME_IN_DAYS = 0-90]
  [DEFAULT_DDL_COLLATION = 'collation_spec']
  [COMMENT = 'description']
  [task_management_parameters]
  [logging_parameters];
```

## 📊 COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Attributes (All Operations)
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **schemaName** | String | Required | N/A | Valid identifier | Must be unique | None | HIGH | Primary schema identifier |
| **catalogName** | String | Optional | Current DB | Valid identifier | Must exist | None | MEDIUM | Parent database name (Liquibase maps Snowflake DATABASE → CATALOG) |
| **orReplace** | Boolean | Optional | false | true/false | None | Mutually exclusive with ifNotExists | MEDIUM | Replace existing schema |
| **ifNotExists** | Boolean | Optional | false | true/false | None | Mutually exclusive with orReplace | MEDIUM | Idempotent creation |
| **transient** | Boolean | Optional | false | true/false | Forces dataRetentionTimeInDays = 0 | None | MEDIUM | No Time Travel schema |

### Cloning Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **cloneFrom** | String | Optional | N/A | Existing schema | Must exist | None | LOW | Zero-copy clone source |
| **fromSchema** | String | Optional | N/A | Existing schema | Must exist | Alias for cloneFrom | LOW | Clone source (alias for cloneFrom) |

### Time Travel and Storage Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **dataRetentionTimeInDays** | Integer | Optional | 1 | 0-90 | Must be 0 if transient, ≤ maxDataExtension | None | MEDIUM | Time Travel retention period |
| **maxDataExtensionTimeInDays** | Integer | Optional | 14 | 0-90 | ≥ dataRetention | None | LOW | Maximum Time Travel extension |

### Access and Configuration Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **managedAccess** | Boolean | Optional | false | true/false | None | None | MEDIUM | Centralized privilege management |
| **managed** | Boolean | Optional | false | true/false | None | Alias for managedAccess | MEDIUM | Managed access control (alias for managedAccess) |
| **defaultDdlCollation** | String | Optional | Database default | Valid collation | Must be valid | None | LOW | String handling collation |
| **comment** | String | Optional | N/A | String ≤256 chars | Length limit | None | LOW | Schema description |
| **pipeExecutionPaused** | Boolean | Optional | false | true/false | None | None | LOW | Pipeline control |

### Advanced Snowflake Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **catalog** | String | Optional | N/A | Valid identifier | Must exist | None | LOW | Catalog reference |
| **externalVolume** | String | Optional | N/A | Valid volume name | Must exist | None | LOW | External storage volume |
| **classificationProfile** | String | Optional | N/A | Valid profile name | Must exist | None | LOW | Data governance profile |
| **replaceInvalidCharacters** | Boolean | Optional | false | true/false | None | None | LOW | Character validation |
| **storageSerializationPolicy** | String | Optional | N/A | Valid policy name | Must exist | None | LOW | Storage optimization |
| **tag** | String | Optional | N/A | Valid tag format | Tag format validation | None | LOW | Schema tagging |

### Mutual Exclusivity Rules
```yaml
CONDITIONAL_EXCLUSIVITY: "Cannot combine OR REPLACE + IF NOT EXISTS"
TRANSIENT_CONSTRAINT: "Transient schemas must have dataRetentionTimeInDays = 0"
RETENTION_CONSTRAINT: "maxDataExtensionTimeInDays >= dataRetentionTimeInDays"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### Basic Creation Examples
```sql
-- Simple schema
CREATE SCHEMA basic_schema;

-- Transient schema (no Time Travel)
CREATE TRANSIENT SCHEMA temp_schema;

-- With configuration
CREATE SCHEMA configured_schema
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  COMMENT = 'Production schema';
```

### Conditional Creation Examples
```sql
-- Safe replacement
CREATE OR REPLACE SCHEMA replacement_schema
  COMMENT = 'Replaces existing schema';

-- Idempotent creation
CREATE SCHEMA IF NOT EXISTS safe_schema;
```

### Cloning Examples
```sql
-- Basic clone
CREATE SCHEMA dev_schema CLONE prod_schema;

-- Point-in-time clone
CREATE SCHEMA restore_schema CLONE prod_schema
  AT (TIMESTAMP => '2024-01-01 12:00:00');

-- Clone with configuration
CREATE SCHEMA cloned_schema CLONE source_schema
  WITH MANAGED ACCESS
  COMMENT = 'Development clone';
```

### Managed Access Examples
```sql
-- Centralized access control
CREATE SCHEMA secure_schema
  WITH MANAGED ACCESS
  DATA_RETENTION_TIME_IN_DAYS = 30
  COMMENT = 'Schema with centralized access control';
```

### Advanced Configuration Examples
```sql
-- Complete configuration
CREATE SCHEMA enterprise_schema
  WITH MANAGED ACCESS
  DATA_RETENTION_TIME_IN_DAYS = 30
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 60
  DEFAULT_DDL_COLLATION = 'en-ci'
  PIPE_EXECUTION_PAUSED = FALSE
  COMMENT = 'Enterprise schema with full config';

-- Complete example with all attributes
CREATE SCHEMA comprehensive_schema WITH
  pipeExecutionPaused = "false"
  catalogName = "test_db"
  externalVolume = "external_vol"
  catalog = "test_catalog"
  cloneFrom = "source_schema"
  fromSchema = "source_schema"
  managed = "true"
  maxDataExtensionTimeInDays = "90"
  replaceInvalidCharacters = "true"
  schemaName = "test_schema"
  managedAccess = "true"
  defaultDdlCollation = "utf8"
  transient = "false"
  orReplace = "false"
  classificationProfile = "sensitive_profile"
  ifNotExists = "true"
  dataRetentionTimeInDays = "30"
  comment = "Comprehensive schema"
  tag = "production"
  storageSerializationPolicy = "optimized";
```

### Constraint Examples
```sql
-- Valid: max >= retention
CREATE SCHEMA valid_retention
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 14;

-- Invalid: transient with retention > 0 (will fail)
CREATE TRANSIENT SCHEMA invalid_transient
  DATA_RETENTION_TIME_IN_DAYS = 7;

-- Invalid: OR REPLACE + IF NOT EXISTS (will fail)
CREATE OR REPLACE SCHEMA IF NOT EXISTS invalid_combo;
```

### Validation Points
```yaml
CREATION_VALIDATION: "Schema exists with correct properties"
TRANSIENT_VALIDATION: "No Time Travel or Fail-safe enabled"
CLONE_VALIDATION: "Exact copy with independent lifecycle"
MANAGED_ACCESS_VALIDATION: "Centralized privilege model applied"
CONSTRAINT_VALIDATION: "Mutual exclusivity and retention rules enforced"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
BASIC_TESTS:
  - "Simple schema creation: name validation"
  - "Transient schema: no Time Travel validation"
  - "Schema with retention: time travel configuration"
  - "Required schemaName validation"
  - "Database location validation"

CONDITIONAL_TESTS:
  - "OR REPLACE: drops existing schema"
  - "IF NOT EXISTS: idempotent creation"
  - "OR REPLACE + IF NOT EXISTS: mutual exclusivity error"
  - "Conditional creation with properties"

CLONING_TESTS:
  - "Basic clone: zero-copy operation"
  - "Point-in-time clone: timestamp/offset/statement"
  - "Clone non-existing source: error validation"
  - "Clone with additional properties"

VALIDATION_TESTS:
  - "Transient + retention > 0: constraint violation"
  - "maxDataExtension < dataRetention: constraint violation"
  - "Retention values outside 0-90 range: error"
  - "Comment length > 256 chars: validation error"
  - "Complex parameter combinations: integration test"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Schema lifecycle: create → use → clone → replace → cleanup"
  - "Database hierarchy: database → schema → objects"
  - "Time Travel: set retention → test historical queries"
  - "Managed access: create → test privilege model → modify"
  - "Multi-schema: creation with different configurations"

PERFORMANCE_TESTS:
  - "Large schema cloning"
  - "Multiple schema creation"
  - "Complex configuration application"
```

### Test File Organization
```yaml
CREATE_SCHEMA_TESTS:
  - "CreateSchemaBasicTest.java: Simple creation operations"
  - "CreateSchemaConditionalTest.java: OR REPLACE/IF NOT EXISTS"
  - "CreateSchemaCloneTest.java: Cloning operations"
  - "CreateSchemaValidationTest.java: Constraint validation"
  - "CreateSchemaTransientTest.java: Transient schema specifics"
  - "CreateSchemaManagedAccessTest.java: Access control features"
  - "createSchema_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "Operation type detection (basic/conditional/clone)"
    - "Mutual exclusivity validation (OR REPLACE vs IF NOT EXISTS)"
    - "Transient constraint validation (retention = 0)"
    - "SQL generation for all operation types"
    
  TEST_STRUCTURE:
    - "CreateSchemaBasicTest: Simple creation"
    - "CreateSchemaConditionalTest: OR REPLACE/IF NOT EXISTS"
    - "CreateSchemaCloneTest: Cloning operations"
    - "CreateSchemaValidationTest: Constraints"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeCreateSchemaGenerator extends AbstractSqlGenerator"
    - "supports() returns true for CreateSchemaStatement"
    - "generateSql() with operation type logic"
    - "Comprehensive validation with clear error messages"

REFACTOR_PHASE:
  IMPROVEMENTS:
    - "Extract operation-specific helper methods"
    - "Optimize SQL template generation"
    - "Enhance validation error messages"
    - "Performance optimization for cloning"
```

### Pattern Implementation
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake CREATE SCHEMA syntax differs significantly from standard SQL"
KEY_POINTS:
  - "Operation detection: basic/conditional/clone"
  - "Mutual exclusivity: validate conflicting parameters"
  - "SQL generation: template-based with conditional clauses"
  - "Cloning: handle point-in-time options"
```

### Service Registration
```java
// SnowflakeCreateSchemaGenerator
public class SnowflakeCreateSchemaGenerator extends AbstractSqlGenerator<CreateSchemaStatement> {
    @Override
    public boolean supports(CreateSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(CreateSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Implementation with operation type detection
        // Handle OR REPLACE, IF NOT EXISTS, CLONE, and properties
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "Basic creation, conditional operations, core validation"
MEDIUM_PRIORITY: "Cloning operations, managed access, retention configuration"
LOW_PRIORITY: "Advanced properties (catalog, external volume, governance)"
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
COMPLEXITY: "✅ MANAGEABLE - Medium complexity, well-defined patterns"
RISK_LEVEL: "✅ MEDIUM - Complex validation, comprehensive documentation"
IMPLEMENTATION_TIME: "5-6 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeCreateSchemaGenerator class"
  - "Operation type detection and validation"
  - "SQL generation for all creation types"
  - "Comprehensive parameter validation"

TEST_SUITE:
  - "30+ unit tests covering all scenarios"
  - "Integration tests for complex workflows"
  - "Error condition validation"
  - "Cloning operation verification"

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
  - "Cloning and advanced features: 1-2 hours"
  - "Testing and validation: 1-2 hours"
  - "Documentation: 30 minutes"
  
TOTAL_ESTIMATE: "5-6 hours"
PRIORITY: "HIGH - Core schema management"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*