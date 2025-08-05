# DROP SCHEMA Requirements
## AI-Optimized Requirements for Snowflake DROP SCHEMA Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "SQL_Generator_Override"
OBJECT_TYPE: "Schema"
OPERATION: "DROP"
ESTIMATED_TIME: "3-4 hours"
COMPLEXITY: "MEDIUM"
ATTRIBUTES_COUNT: 5
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Drop Operation Types
| Type | SQL Pattern | Key Features | Safety Level |
|------|-------------|--------------|--------------|
| **BASIC** | `DROP SCHEMA name` | Simple deletion | RESTRICT (safe) |
| **CONDITIONAL** | `DROP SCHEMA IF EXISTS name` | Error-safe deletion | Idempotent |
| **CASCADE** | `DROP SCHEMA name CASCADE` | Force deletion with contents | Destructive |
| **RESTRICT** | `DROP SCHEMA name RESTRICT` | Safe deletion (explicit) | Empty schema only |

### Quick Implementation Pattern
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake DROP SCHEMA syntax differs from standard SQL with CASCADE/RESTRICT"
IMPLEMENTATION: "Override generateSql() in SnowflakeDropSchemaGenerator"
VALIDATION: "CASCADE/RESTRICT mutual exclusivity + session context validation"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/drop-schema"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ All drop variations + CASCADE/RESTRICT behaviors documented"
```

### Critical Implementation Points
```yaml
MUTUAL_EXCLUSIVITY: "CASCADE and RESTRICT cannot be combined"
SESSION_VALIDATION: "Cannot drop current schema in active session"
DEFAULT_BEHAVIOR: "RESTRICT is default when neither CASCADE nor RESTRICT specified"
NO_RECOVERY: "Dropped schemas cannot be UNDROPped (unlike databases)"
```

## 🎯 SQL SYNTAX TEMPLATES

### Basic Drop
```sql
DROP SCHEMA schema_name;
```

### Conditional Drop
```sql
DROP SCHEMA IF EXISTS schema_name;
```

### CASCADE Drop (Force Deletion)
```sql
DROP SCHEMA schema_name CASCADE;
DROP SCHEMA IF EXISTS schema_name CASCADE;
```

### RESTRICT Drop (Safe Deletion)
```sql
DROP SCHEMA schema_name RESTRICT;
DROP SCHEMA IF EXISTS schema_name RESTRICT;
```

### Critical Constraints
```yaml
CASCADE_RESTRICT_EXCLUSIVITY: "Cannot use both CASCADE and RESTRICT"
SESSION_CONTEXT: "Cannot drop current schema"
DEFAULT_BEHAVIOR: "RESTRICT behavior when neither CASCADE nor RESTRICT specified"
CASE_SENSITIVITY: "Unquoted names converted to uppercase, quoted names preserved"
```

## 📊 COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Attributes
| Attribute | DataType | Required/Optional | Default | ValidValues | Constraints | MutualExclusivity | Priority | Notes |
|-----------|----------|-------------------|---------|-------------|-------------|-------------------|----------|-------|
| **schemaName** | String | Required | N/A | Valid identifier | Must exist | None | HIGH | Primary schema identifier |
| **databaseName** | String | Optional | Current DB | Valid identifier | Must exist | None | MEDIUM | Schema location specification |
| **cascade** | Boolean | Optional | false | true/false | None | Mutually exclusive with restrict | MEDIUM | Drop with dependent objects |
| **ifExists** | Boolean | Optional | false | true/false | None | None | MEDIUM | Error prevention for non-existent schemas |
| **restrict** | Boolean | Optional | false | true/false | None | Mutually exclusive with cascade | MEDIUM | Fail if dependent objects exist |

### Mutual Exclusivity Rules
```yaml
CASCADE_RESTRICT_EXCLUSIVITY: "Cannot set both cascade=true and restrict=true"
DEFAULT_BEHAVIOR: "When neither cascade nor restrict specified, RESTRICT behavior applies"
SESSION_PROTECTION: "Cannot drop current schema (connection context validation)"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### Basic Drop Examples
```sql
-- Simple drop (RESTRICT behavior)
DROP SCHEMA simple_schema;

-- Conditional drop
DROP SCHEMA IF EXISTS temporary_schema;
```

### CASCADE Examples (Force Deletion)
```sql
-- Force drop with all contents
DROP SCHEMA complex_schema CASCADE;

-- Conditional force drop
DROP SCHEMA IF EXISTS development_schema CASCADE;
```

### RESTRICT Examples (Safe Deletion)
```sql
-- Explicit safe drop
DROP SCHEMA empty_schema RESTRICT;

-- Conditional safe drop
DROP SCHEMA IF EXISTS backup_schema RESTRICT;
```

### Database-Qualified Examples
```sql
-- Drop schema from specific database
DROP SCHEMA database_name.schema_name;

-- With IF EXISTS and CASCADE
DROP SCHEMA IF EXISTS database_name.schema_name CASCADE;
```

### Complete Example with All Attributes
```sql
-- Complete example with all attributes
DROP SCHEMA IF EXISTS comprehensive_schema CASCADE
  databaseName = "test_db"
  cascade = "true"
  ifExists = "true"
  restrict = "false"
  schemaName = "test_schema";
```

### Error Scenarios
```sql
-- Error: Schema contains objects (RESTRICT behavior)
DROP SCHEMA schema_with_tables;

-- Error: Cannot drop current schema
DROP SCHEMA current_session_schema;

-- Error: Schema doesn't exist
DROP SCHEMA non_existent_schema;

-- Success: Using IF EXISTS
DROP SCHEMA IF EXISTS non_existent_schema;
```

### Validation Points
```yaml
DROP_VALIDATION: "Schema removed from system views"
CASCADE_VALIDATION: "All contained objects removed"
RESTRICT_VALIDATION: "Fails if objects exist in schema"
IF_EXISTS_VALIDATION: "No error when schema doesn't exist"
CONTEXT_VALIDATION: "Cannot drop current session schema"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
BASIC_TESTS:
  - "Basic drop: schema exists and is empty"
  - "IF EXISTS: existing schema drop"
  - "IF EXISTS: non-existing schema (no error)"
  - "Drop non-existing schema without IF EXISTS (error)"
  - "Required schemaName validation"
  - "Database-qualified schema names"

CASCADE_TESTS:
  - "CASCADE: drop empty schema"
  - "CASCADE: drop schema with tables and objects"
  - "CASCADE with IF EXISTS: conditional force drop"
  - "CASCADE behavior verification: all contents removed"

RESTRICT_TESTS:
  - "RESTRICT: drop empty schema (success)"
  - "RESTRICT: drop schema with objects (error)"
  - "Default RESTRICT behavior when neither specified"
  - "RESTRICT with IF EXISTS: conditional safe drop"

VALIDATION_TESTS:
  - "CASCADE + RESTRICT together (error)"
  - "Current schema drop attempt (error)"
  - "Case sensitivity: quoted vs unquoted names"
  - "Invalid schema name validation"
  - "Null/empty schema name validation"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Schema lifecycle: create → populate → drop CASCADE"
  - "Safety workflow: create → populate → drop RESTRICT (error) → drop CASCADE (success)"
  - "Database context: switch schema → drop previous"
  - "Multi-object schema: tables, views, procedures → CASCADE drop"

PERFORMANCE_TESTS:
  - "CASCADE drop with large object hierarchy"
  - "Multiple schema drops"
```

### Test File Organization
```yaml
DROP_SCHEMA_TESTS:
  - "DropSchemaBasicTest.java: Basic drop operations"
  - "DropSchemaCascadeTest.java: CASCADE operations"
  - "DropSchemaRestrictTest.java: RESTRICT operations"
  - "DropSchemaValidationTest.java: Constraint validation"
  - "DropSchemaMutualExclusivityTest.java: CASCADE/RESTRICT exclusivity"
  - "DropSchemaContextTest.java: Session context validation"
  - "dropSchema_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "Schema name validation and processing"
    - "IF EXISTS conditional logic"
    - "CASCADE vs RESTRICT mutual exclusivity"
    - "SQL generation for all variations"
    
  TEST_STRUCTURE:
    - "DropSchemaBasicTest: Core functionality"
    - "DropSchemaCascadeTest: Force deletion"
    - "DropSchemaRestrictTest: Safe deletion"
    - "DropSchemaValidationTest: Constraints and errors"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeDropSchemaGenerator extends AbstractSqlGenerator"
    - "supports() returns true for DropSchemaStatement"
    - "generateSql() with CASCADE/RESTRICT conditional logic"
    - "Session context validation"

REFACTOR_PHASE:
  IMPROVEMENTS:
    - "Extract common schema name validation"
    - "Optimize SQL generation templates"
    - "Standardize error message formatting"
    - "Performance optimization"
```

### Pattern Implementation
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake DROP SCHEMA syntax differs from standard SQL"
KEY_POINTS:
  - "Validation-first approach for safety"
  - "Session context validation (current schema)"
  - "CASCADE/RESTRICT conditional SQL generation"
  - "Database-qualified schema name handling"
```

### Service Registration
```java
// SnowflakeDropSchemaGenerator
public class SnowflakeDropSchemaGenerator extends AbstractSqlGenerator<DropSchemaStatement> {
    @Override
    public boolean supports(DropSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(DropSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Implementation with CASCADE/RESTRICT logic
        StringBuilder query = new StringBuilder("DROP SCHEMA");
        
        if (statement.getIfExists()) {
            query.append(" IF EXISTS");
        }
        
        query.append(" ").append(database.escapeObjectName(statement.getSchemaName(), Schema.class));
        
        if (statement.getCascade()) {
            query.append(" CASCADE");
        } else if (statement.getRestrict()) {
            query.append(" RESTRICT");
        }
        // Default is RESTRICT behavior
        
        return new Sql[]{new UnparsedSql(query.toString(), getAffectedSchema(statement))};
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "Core functionality (basic drop, IF EXISTS, CASCADE/RESTRICT)"
MEDIUM_PRIORITY: "Session context validation, database-qualified names"
LOW_PRIORITY: "Advanced validation and error handling"
```

## ✅ IMPLEMENTATION STATUS

### Requirements Quality
```yaml
COMPLETENESS: "✅ All drop variations, constraints, and behaviors documented"
SQL_SYNTAX: "✅ Complete with all variations and examples"
VALIDATION: "✅ Mutual exclusivity and safety constraints defined"
TEST_COVERAGE: "✅ Comprehensive scenarios for all operations"
IMPLEMENTATION_GUIDE: "✅ TDD approach with clear phases"
```

### Readiness Assessment
```yaml
TECHNICAL_READINESS: "✅ READY - All attributes and SQL syntax documented"
COMPLEXITY: "✅ MANAGEABLE - Medium complexity, clear patterns"
RISK_LEVEL: "✅ LOW - Simple operation with well-defined safety constraints"
IMPLEMENTATION_TIME: "3-4 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeDropSchemaGenerator class"
  - "Schema name validation logic"
  - "Session context validation"
  - "SQL generation for all drop types"
  - "CASCADE/RESTRICT mutual exclusivity validation"

TEST_SUITE:
  - "25+ unit tests covering all scenarios"
  - "Integration tests for complex workflows"
  - "Error condition validation"
  - "Safety validation verification"

SUCCESS_CRITERIA:
  - "100% test coverage"
  - "Real Snowflake behavior validation"
  - "Clear error messages"
  - "Safety constraints enforced"
```

### Implementation Timeline
```yaml
PHASE_BREAKDOWN:
  - "Setup and planning: 30 minutes"
  - "Core implementation: 1-2 hours"
  - "CASCADE/RESTRICT logic: 1 hour"
  - "Session validation: 30 minutes"
  - "Testing and validation: 1 hour"
  - "Documentation: 30 minutes"
  
TOTAL_ESTIMATE: "3-4 hours"
PRIORITY: "HIGH - Core schema management"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*