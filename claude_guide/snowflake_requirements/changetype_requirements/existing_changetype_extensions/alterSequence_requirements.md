# ALTER SEQUENCE Requirements
## AI-Optimized Requirements for Snowflake ALTER SEQUENCE Implementation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
STATUS: "IMPLEMENTATION_READY"
OPTIMIZATION_DATE: "2025-08-03"
IMPLEMENTATION_PATTERN: "SQL_Generator_Override"
OBJECT_TYPE: "Sequence"
OPERATION: "ALTER"
ESTIMATED_TIME: "3-4 hours"
COMPLEXITY: "MEDIUM"
ATTRIBUTES_COUNT: 3
PRIORITY: "READY"
```

## ⚡ INSTANT REQUIREMENTS ACCESS

### Core Operation Types
| Type | SQL Pattern | Key Features | Critical Notes |
|------|-------------|--------------|----------------|
| **RENAME** | `ALTER SEQUENCE [IF EXISTS] name RENAME TO new_name` | Sequence renaming | Standard operation |
| **SET PROPERTIES** | `ALTER SEQUENCE [IF EXISTS] name SET properties` | Modify increment, order, comment | NOORDER is irreversible |
| **UNSET PROPERTIES** | `ALTER SEQUENCE [IF EXISTS] name UNSET property` | Remove comment | Comment management |

### Quick Implementation Pattern
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake ALTER SEQUENCE syntax differs from standard SQL"
IMPLEMENTATION: "Override generateSql() in SnowflakeAlterSequenceGenerator"
VALIDATION: "NOORDER irreversibility warning + comment mutual exclusivity"
```

## 📋 CORE IMPLEMENTATION REQUIREMENTS

### Documentation Reference
```yaml
SOURCE: "https://docs.snowflake.com/en/sql-reference/sql/alter-sequence"
VERSION: "Snowflake 2024"
COMPLETENESS: "✅ Enhanced ALTER SEQUENCE with Snowflake-specific features"
```

### Critical Implementation Points
```yaml
IRREVERSIBLE_OPERATION: "ORDER to NOORDER is permanent and cannot be undone"
COMMENT_EXCLUSIVITY: "Cannot SET and UNSET comment in same operation"
PERFORMANCE_BENEFIT: "NOORDER improves concurrency and speed"
WARNING_REQUIRED: "Must warn users about irreversible NOORDER operation"
```

## 🎯 SQL SYNTAX TEMPLATES

### Rename Sequence
```sql
ALTER SEQUENCE [IF EXISTS] sequence_name RENAME TO new_sequence_name;
```

### Set Properties
```sql
ALTER SEQUENCE [IF EXISTS] sequence_name SET
  [INCREMENT [BY] sequence_interval]
  [{ORDER | NOORDER}]
  [COMMENT = 'comment_text'];
```

### Unset Properties
```sql
ALTER SEQUENCE [IF EXISTS] sequence_name UNSET COMMENT;
```

### Critical Constraints
```yaml
NOORDER_WARNING: "⚠️ IRREVERSIBLE: ORDER to NOORDER cannot be undone"
COMMENT_EXCLUSIVITY: "Cannot set and unset comment simultaneously"
INCREMENT_VALIDATION: "Must be positive integer"
```

## 📊 ATTRIBUTES QUICK REFERENCE

### Snowflake-Specific Namespace Attributes
| Attribute | Type | Required | Values | Constraints | Priority |
|-----------|------|----------|--------|-------------|----------|
| **setNoOrder** | Boolean | ❌ | true/false | ⚠️ IRREVERSIBLE operation | HIGH |
| **setComment** | String | ❌ | Any string | Cannot use with unsetComment | MEDIUM |
| **unsetComment** | Boolean | ❌ | true/false | Cannot use with setComment | LOW |

### Existing Liquibase Attributes (Enhanced)
| Attribute | Type | Values | Notes |
|-----------|------|--------|-------|
| **sequenceName** | String | Valid identifier | Required for all operations |
| **schemaName** | String | Valid schema | Optional, uses current schema |
| **incrementBy** | Integer | Positive integer | Standard Liquibase attribute |

### Mutual Exclusivity Rules
```yaml
COMMENT_EXCLUSIVITY: "Cannot specify both setComment and unsetComment=true"
NOORDER_IRREVERSIBILITY: "Once NOORDER is set, sequence cannot return to ORDER"
SINGLE_OPERATION: "Each ALTER statement can only perform one logical operation"
```

## 🚀 SQL EXAMPLES (Validation Ready)

### NOORDER Performance Optimization (Irreversible)
```xml
<!-- ⚠️ WARNING: This change CANNOT be undone! -->
<alterSequence sequenceName="high_volume_seq"
               snowflake:setNoOrder="true"/>
```
```sql
-- Generated SQL with warning
ALTER SEQUENCE high_volume_seq SET NOORDER;
-- WARNING: This change is IRREVERSIBLE
```

### Comment Management Examples
```xml
<!-- Add documentation comment -->
<alterSequence sequenceName="customer_id_seq"
               snowflake:setComment="Customer ID generator - production use only"/>

<!-- Remove comment -->
<alterSequence sequenceName="temp_seq"
               snowflake:unsetComment="true"/>
```
```sql
-- Set comment
ALTER SEQUENCE customer_id_seq SET COMMENT = 'Customer ID generator - production use only';

-- Remove comment
ALTER SEQUENCE temp_seq UNSET COMMENT;
```

### Combined Operations Examples
```xml
<!-- Standard + Snowflake operations -->
<alterSequence sequenceName="order_seq"
               incrementBy="10"
               snowflake:setNoOrder="true"
               snowflake:setComment="Changed to NOORDER for performance - 2025-08-01"/>
```
```sql
ALTER SEQUENCE order_seq SET 
  INCREMENT BY 10,
  NOORDER,
  COMMENT = 'Changed to NOORDER for performance - 2025-08-01';
```

### Error Scenarios
```xml
<!-- Invalid: Cannot set and unset comment together -->
<alterSequence sequenceName="invalid_seq"
               snowflake:setComment="New comment"
               snowflake:unsetComment="true"/>
<!-- This will generate a validation error -->
```

### Validation Points
```yaml
NOORDER_VALIDATION: "Sequence changed to NOORDER, cannot be reversed"
COMMENT_VALIDATION: "Comment set/unset as specified"
INCREMENT_VALIDATION: "Increment value updated correctly"
RENAME_VALIDATION: "Sequence renamed, old name no longer exists"
IRREVERSIBILITY_WARNING: "User warned about NOORDER irreversibility"
```

## 🧪 TEST SCENARIOS (TDD Ready)

### Unit Test Matrix
```yaml
NOORDER_TESTS:
  - "Set NOORDER: irreversible performance optimization"
  - "NOORDER warning: user notification about irreversibility"
  - "Combined NOORDER + increment: multiple property changes"

COMMENT_TESTS:
  - "Set comment: add sequence documentation"
  - "Unset comment: remove sequence documentation"
  - "Set + unset comment together: validation error"
  - "Comment with special characters: string handling"

RENAME_TESTS:
  - "Basic rename: sequence name change"
  - "Rename with IF EXISTS: conditional renaming"

VALIDATION_TESTS:
  - "Required sequenceName validation"
  - "Comment mutual exclusivity validation"
  - "Increment value validation (positive integer)"
  - "NOORDER irreversibility warning generation"
```

### Integration Tests
```yaml
WORKFLOW_TESTS:
  - "Sequence lifecycle: create → alter → use → rename → drop"
  - "Performance optimization: ORDER → NOORDER transition"
  - "Documentation workflow: add comment → update → remove"
  - "Combined operations: increment + NOORDER + comment"

PERFORMANCE_TESTS:
  - "NOORDER impact: sequence generation speed"
  - "Multiple sequence alterations"
```

### Test File Organization
```yaml
ALTER_SEQUENCE_TESTS:
  - "AlterSequenceNoOrderTest.java: NOORDER operations and warnings"
  - "AlterSequenceCommentTest.java: Comment management"
  - "AlterSequenceRenameTest.java: Rename operations"
  - "AlterSequenceValidationTest.java: Constraint validation"
  - "AlterSequenceCombinedTest.java: Multiple property operations"
  - "alterSequence_integration_test.xml: End-to-end scenarios"
```

## ⚙️ IMPLEMENTATION GUIDE

### TDD Implementation Strategy
```yaml
RED_PHASE:
  PRIORITY_TESTS:
    - "NOORDER operation with irreversibility warning"
    - "Comment mutual exclusivity validation"
    - "SQL generation for Snowflake-specific attributes"
    - "Combined standard + Snowflake operations"
    
  TEST_STRUCTURE:
    - "AlterSequenceNoOrderTest: NOORDER operations"
    - "AlterSequenceCommentTest: Comment management"
    - "AlterSequenceValidationTest: Constraints and warnings"

GREEN_PHASE:
  IMPLEMENTATION:
    - "SnowflakeAlterSequenceGenerator extends AbstractSqlGenerator"
    - "supports() returns true for AlterSequenceStatement"
    - "generateSql() with Snowflake-specific attribute handling"
    - "Validation logic with irreversibility warnings"

REFACTOR_PHASE:
  IMPROVEMENTS:
    - "Extract Snowflake-specific attribute processing"
    - "Optimize SQL generation templates"
    - "Enhance warning message clarity"
    - "Performance optimization"
```

### Pattern Implementation
```yaml
PATTERN: "SQL Generator Override"
REASON: "Snowflake ALTER SEQUENCE syntax includes unique features"
KEY_POINTS:
  - "Namespace attribute processing (snowflake:setNoOrder, etc.)"
  - "Irreversibility warning for NOORDER operations"
  - "Comment mutual exclusivity validation"
  - "SQL generation combining standard + Snowflake features"
```

### Service Registration
```java
// SnowflakeAlterSequenceGenerator
public class SnowflakeAlterSequenceGenerator extends AbstractSqlGenerator<AlterSequenceStatement> {
    @Override
    public boolean supports(AlterSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Process standard Liquibase attributes
        // Process Snowflake namespace attributes
        // Generate combined SQL with validation
        
        String sql = "ALTER SEQUENCE";
        if (statement.getIfExists()) {
            sql += " IF EXISTS";
        }
        sql += " " + database.escapeSequenceName(statement.getSequenceName());
        
        // Handle SET operations
        List<String> setClause = new ArrayList<>();
        if (statement.getIncrementBy() != null) {
            setClause.add("INCREMENT BY " + statement.getIncrementBy());
        }
        
        // Handle Snowflake-specific attributes
        if (statement.getSnowflakeSetNoOrder()) {
            setClause.add("NOORDER");
            // Log irreversibility warning
        }
        if (statement.getSnowflakeSetComment() != null) {
            setClause.add("COMMENT = '" + statement.getSnowflakeSetComment() + "'");
        }
        
        if (!setClause.isEmpty()) {
            sql += " SET " + String.join(", ", setClause);
        }
        
        // Handle UNSET operations
        if (statement.getSnowflakeUnsetComment()) {
            sql += " UNSET COMMENT";
        }
        
        return new Sql[]{new UnparsedSql(sql, getAffectedSequence(statement))};
    }
}
```

### Implementation Priority
```yaml
HIGH_PRIORITY: "NOORDER operation with warnings, core validation"
MEDIUM_PRIORITY: "Comment management, combined operations"
LOW_PRIORITY: "Advanced validation and error handling"
```

## ✅ IMPLEMENTATION STATUS

### Requirements Quality
```yaml
COMPLETENESS: "✅ All Snowflake-specific sequence operations documented"
SQL_SYNTAX: "✅ Complete with all variations and examples"
VALIDATION: "✅ Mutual exclusivity and irreversibility warnings defined"
TEST_COVERAGE: "✅ Comprehensive scenarios including edge cases"
IMPLEMENTATION_GUIDE: "✅ TDD approach with clear phases"
```

### Readiness Assessment
```yaml
TECHNICAL_READINESS: "✅ READY - All attributes and SQL syntax documented"
COMPLEXITY: "✅ MANAGEABLE - Medium complexity, clear patterns"
RISK_LEVEL: "✅ MEDIUM - Irreversible operations require careful warning handling"
IMPLEMENTATION_TIME: "3-4 hours estimated"
```

### Phase 3 Deliverables
```yaml
CORE_IMPLEMENTATION:
  - "SnowflakeAlterSequenceGenerator class"
  - "Snowflake namespace attribute processing"
  - "Irreversibility warning system"
  - "Comment mutual exclusivity validation"

TEST_SUITE:
  - "20+ unit tests covering all scenarios"
  - "Integration tests for combined operations"
  - "Validation tests for constraints and warnings"
  - "Performance impact verification"

SUCCESS_CRITERIA:
  - "100% test coverage"
  - "Clear irreversibility warnings"
  - "Proper validation error messages"
  - "Real Snowflake behavior validation"
```

### Implementation Timeline
```yaml
PHASE_BREAKDOWN:
  - "Setup and planning: 30 minutes"
  - "Core Snowflake attribute processing: 1-2 hours"
  - "Warning and validation system: 1 hour"
  - "SQL generation enhancement: 1 hour"
  - "Testing and validation: 1 hour"
  - "Documentation: 30 minutes"
  
TOTAL_ESTIMATE: "3-4 hours"
PRIORITY: "MEDIUM - Sequence management enhancement"
STATUS: "✅ IMPLEMENTATION_READY"
```

---
*Requirements optimized for AI rapid scanning and TDD implementation workflow*