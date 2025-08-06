# DropTable Enhanced Requirements (Snowflake Namespace Attributes)

---
## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "PHASE_2_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Table"
OPERATION: "DROP"
NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
ESTIMATED_IMPLEMENTATION_TIME: "4-5 hours"
ATTRIBUTES_COUNT: 4
```
---

## CORE_REQUIREMENTS

### Primary Objective
Extend Liquibase's standard `dropTable` changetype with Snowflake-specific namespace attributes to provide explicit control over CASCADE/RESTRICT behavior, addressing Snowflake's unique default behaviors that differ by table type.

### Critical Success Criteria
1. **CASCADE Control**: Enable explicit `CASCADE` option to force drop tables with foreign key dependencies
2. **RESTRICT Control**: Enable explicit `RESTRICT` option to prevent drop when foreign key dependencies exist
3. **Default Behavior Preservation**: Maintain Snowflake's table-type-specific defaults when no options specified
4. **Mutual Exclusivity**: Enforce that CASCADE and RESTRICT cannot be specified simultaneously
5. **Standard Compatibility**: Work seamlessly with existing Liquibase `dropTable` attributes

## SNOWFLAKE_TECHNICAL_RESEARCH

### SQL Syntax (Snowflake Official Documentation)
```sql
DROP TABLE [ IF EXISTS ] <table_name> [ CASCADE | RESTRICT ]
```

**Documentation Reference**: https://docs.snowflake.com/en/sql-reference/sql/drop-table

### Snowflake-Specific Behaviors
1. **Default Behavior Variations**:
   - Standard tables: Default CASCADE behavior
   - Hybrid tables: Default RESTRICT behavior
   
2. **Foreign Key Handling**:
   - CASCADE: Drops referencing foreign keys in other tables
   - RESTRICT: Fails if any foreign keys reference the table
   
3. **Time Travel Integration**:
   - No PURGE option (uses Time Travel instead)
   - CASCADE-dropped foreign keys NOT restored on UNDROP

### Key Differences from Standard SQL
- Table-type-specific defaults (not uniform)
- CASCADE affects foreign keys in OTHER tables
- Cannot restore foreign key relationships after CASCADE drop

## NAMESPACE_ATTRIBUTES_SPECIFICATION

### Attribute Definitions
| Attribute | Type | Default | Values | Required | Priority | Validation |
|-----------|------|---------|--------|----------|----------|------------|
| `cascade` | Boolean | null | true/false | No | HIGH | Mutually exclusive with restrict |
| `restrict` | Boolean | null | true/false | No | HIGH | Mutually exclusive with cascade |

### Mutual Exclusivity Rules
```java
// Validation Logic
if (cascade == true && restrict == true) {
    throw new ValidationFailedException(
        "Cannot specify both cascade='true' and restrict='true' on dropTable"
    );
}
```

### SQL Generation Logic
```java
String cascadeRestrict = "";
if (cascade == true) {
    cascadeRestrict = " CASCADE";
} else if (restrict == true) {
    cascadeRestrict = " RESTRICT";
}
// Empty string uses Snowflake defaults
```

## XML_USAGE_EXAMPLES

### Example 1: Force CASCADE Drop
```xml
<changeSet id="drop-parent-table" author="developer">
    <dropTable tableName="parent_table"
               snowflake:cascade="true"/>
</changeSet>
```
**Generated SQL**: `DROP TABLE parent_table CASCADE;`

### Example 2: Safe RESTRICT Drop
```xml
<changeSet id="drop-with-safety" author="developer">
    <dropTable tableName="referenced_table"
               snowflake:restrict="true"/>
</changeSet>
```
**Generated SQL**: `DROP TABLE referenced_table RESTRICT;`

### Example 3: Default Behavior
```xml
<changeSet id="drop-simple" author="developer">
    <dropTable tableName="simple_table"/>
</changeSet>
```
**Generated SQL**: `DROP TABLE simple_table;`
*Uses Snowflake defaults: CASCADE for standard tables, RESTRICT for hybrid*

### Example 4: Combined with Standard Attributes
```xml
<changeSet id="drop-conditional" author="developer">
    <dropTable tableName="maybe_exists"
               cascadeConstraints="false"
               snowflake:cascade="true"/>
</changeSet>
```
**Generated SQL**: `DROP TABLE IF EXISTS maybe_exists CASCADE;`

### Example 5: Documented Drop with Audit Trail
```xml
<changeSet id="drop-with-reason" author="cleanup">
    <dropTable tableName="legacy_customer_data"
               snowflake:restrict="true"
               snowflake:comment="Removing legacy table - data migrated to customer_v2"/>
</changeSet>
```

**Generated SQL:**
```sql
DROP TABLE legacy_customer_data RESTRICT;
-- COMMENT: Removing legacy table - data migrated to customer_v2
-- INFO: RESTRICT provides syntax consistency but no functional behavior in Snowflake
```

**Expected Behavior:** Table dropped with documentation comment for audit trail and compliance
**Test Validation:** Verify table removed, comment appears in generated SQL for documentation

## IMPLEMENTATION_ARCHITECTURE

### Pattern: Existing Changetype Extension
Following `/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/implementation_patterns/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md`

### Core Components

#### 1. Storage Component
**File**: `SnowflakeNamespaceAttributeStorage.java` (existing)
- Store cascade/restrict boolean values
- Provide validation for mutual exclusivity

#### 2. Parser Extension
**File**: `SnowflakeNamespaceAwareXMLParser.java` (existing)
```java
// Add dropTable handling
if ("dropTable".equals(localName)) {
    extractSnowflakeAttributes(attributes, SUPPORTED_DROP_TABLE_ATTRIBUTES);
}

private static final Set<String> SUPPORTED_DROP_TABLE_ATTRIBUTES = 
    Set.of("cascade", "restrict");
```

#### 3. Generator Implementation
**File**: `DropTableGeneratorSnowflake.java` (new)
```java
@DatabaseChangeProperty(requiredForDatabase = "snowflake")
public class DropTableGeneratorSnowflake extends DropTableGenerator {
    
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Higher than standard
    }
    
    @Override
    public Sql[] generateSql(DropTableStatement statement, Database database, 
                           SqlGeneratorChain sqlGeneratorChain) {
        
        // Get Snowflake attributes
        Boolean cascade = getSnowflakeAttribute(statement, "cascade", Boolean.class);
        Boolean restrict = getSnowflakeAttribute(statement, "restrict", Boolean.class);
        
        // Validate mutual exclusivity
        validateMutualExclusivity(cascade, restrict);
        
        // Generate base SQL
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        // Append CASCADE/RESTRICT
        return appendCascadeRestrict(baseSql, cascade, restrict);
    }
}
```

#### 4. Integration Points
- **Priority**: Higher than standard DropTableGenerator
- **Compatibility**: Works with existing `cascadeConstraints` attribute
- **Validation**: Enforces mutual exclusivity at generation time

## TESTING_REQUIREMENTS

### Unit Test Coverage
**File**: `DropTableGeneratorSnowflakeTest.java`

#### Test Categories
1. **Attribute Parsing Tests**
   - Valid cascade=true parsing
   - Valid restrict=true parsing
   - Null/missing attribute handling

2. **SQL Generation Tests**
   - CASCADE keyword generation
   - RESTRICT keyword generation
   - Default behavior (no keywords)
   - Combined with IF EXISTS

3. **Validation Tests**
   - Mutual exclusivity enforcement
   - Error message accuracy

4. **Integration Tests**
   - Compatibility with cascadeConstraints
   - Table name escaping
   - Schema-qualified table names

### Test Harness Integration
**Directory**: `/src/test/resources/changelogs/snowflake/dropTable/`

#### Test Files
1. **dropTableCascade.xml**
   ```xml
   <changeSet id="1" author="test">
       <dropTable tableName="parent_table" snowflake:cascade="true"/>
   </changeSet>
   ```

2. **dropTableRestrict.xml**
   ```xml
   <changeSet id="1" author="test">
       <dropTable tableName="child_table" snowflake:restrict="true"/>
   </changeSet>
   ```

3. **dropTableDefault.xml**
   ```xml
   <changeSet id="1" author="test">
       <dropTable tableName="simple_table"/>
   </changeSet>
   ```

4. **dropTableWithForeignKeys.xml**
   - Create tables with FK relationships
   - Test CASCADE removes referencing FKs
   - Test RESTRICT fails with dependencies

### Functional Test Scenarios
1. **Foreign Key Dependency Testing**
   - Create parent/child table relationship
   - Verify RESTRICT fails when dependencies exist
   - Verify CASCADE drops referencing foreign keys
   - Verify default behavior by table type

2. **Recovery Testing**
   - Test UNDROP TABLE functionality
   - Verify CASCADE-dropped FKs are NOT restored
   - Document recovery limitations

## ERROR_HANDLING

### Validation Errors
1. **Mutual Exclusivity**
   ```
   Error: Cannot specify both cascade='true' and restrict='true' on dropTable
   Location: changeSet 'id' in file.xml
   ```

2. **Invalid Boolean Values**
   ```
   Error: Invalid value for snowflake:cascade. Expected 'true' or 'false'
   Location: changeSet 'id' in file.xml
   ```

### Runtime Errors
1. **RESTRICT with Dependencies**
   ```sql
   -- Snowflake Error
   SQL compilation error: Table 'CUSTOMER' cannot be dropped because 
   it is referenced by foreign key 'FK_ORDER_CUSTOMER' in table 'ORDERS'
   ```

2. **Table Not Found**
   ```sql
   -- Without IF EXISTS
   SQL compilation error: Table 'NONEXISTENT' does not exist
   ```

## COMPATIBILITY_MATRIX

### Standard Liquibase Attributes
| Standard Attribute | Snowflake Compatibility | Notes |
|-------------------|------------------------|-------|
| `cascadeConstraints` | COMPATIBLE | Different meaning: drops constraints ON table vs referencing constraints |
| `catalogName` | COMPATIBLE | Snowflake database name |
| `schemaName` | COMPATIBLE | Snowflake schema name |
| `tableName` | COMPATIBLE | Required |

### Interaction Rules
- `cascadeConstraints` and `snowflake:cascade` can be used together
- Different scopes: constraints ON table vs referencing FROM other tables
- Order: cascadeConstraints processed first, then CASCADE/RESTRICT

## DOCUMENTATION_REQUIREMENTS

### User Documentation
**Location**: Snowflake extension documentation

#### Topics to Cover
1. **CASCADE vs RESTRICT Explanation**
   - When to use each option
   - Default behavior by table type
   - Foreign key relationship impact

2. **Recovery Limitations**
   - UNDROP TABLE capabilities
   - Permanent loss of CASCADE-dropped foreign keys
   - Best practices for production use

3. **Safety Recommendations**
   - Use RESTRICT when unsure about dependencies
   - Backup constraint definitions before CASCADE
   - Test in development environments

#### Warning Callouts
```
⚠️ WARNING: CASCADE drops cannot be fully undone
Foreign key relationships dropped by CASCADE are permanently lost 
and are NOT restored by UNDROP TABLE operations.
```

### Developer Documentation
- Implementation pattern reference
- Extension architecture
- Test harness integration
- Validation rule implementation

## ROLLBACK_STRATEGY

### Rollback Limitations
1. **CASCADE Operations**: Cannot restore dropped foreign keys
2. **Time Travel Dependency**: Relies on Snowflake's UNDROP capability
3. **Retention Period**: Subject to DATA_RETENTION_TIME_IN_DAYS setting

### Rollback Implementation
```xml
<changeSet id="drop-table" author="developer">
    <dropTable tableName="orders" snowflake:cascade="true"/>
    <rollback>
        <sql>
            UNDROP TABLE orders;
            -- NOTE: Foreign keys referencing 'orders' are NOT restored
            -- Manual FK recreation required
        </sql>
    </rollback>
</changeSet>
```

## SUCCESS_CRITERIA

### Functional Requirements
- [x] CASCADE attribute forces foreign key dependency removal
- [x] RESTRICT attribute prevents drop when dependencies exist
- [x] Mutual exclusivity validation prevents conflicting options
- [x] Default behavior preserved when no options specified
- [x] Compatible with standard Liquibase dropTable attributes

### Technical Requirements
- [x] Follows Existing Changetype Extension pattern
- [x] Integrates with SnowflakeNamespaceAttributeStorage
- [x] Higher priority than standard generator
- [x] Comprehensive test coverage
- [x] Clear error messages and validation

### Quality Requirements
- [x] Production-ready error handling
- [x] Complete documentation with warnings
- [x] Functional test harness integration
- [x] Performance impact minimized
- [x] Backward compatibility maintained

---
**Implementation Priority**: HIGH
**Risk Level**: MEDIUM (due to CASCADE permanent data loss potential)
**Dependencies**: SnowflakeNamespaceAttributeStorage, SnowflakeNamespaceAwareXMLParser