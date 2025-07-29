# DropTable Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/drop-table
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake DROP TABLE Syntax
```sql
DROP TABLE [ IF EXISTS ] <table_name> [ CASCADE | RESTRICT ]
```

### Key Differences from Standard SQL
1. **Default Behavior**: 
   - Standard tables: Default is CASCADE
   - Hybrid tables: Default is RESTRICT
2. **Foreign Key Handling**: CASCADE/RESTRICT specifically handles foreign key dependencies
3. **No PURGE Option**: Snowflake uses Time Travel instead

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `dropTable`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| cascade | Force drop even with foreign key references | Boolean | null | true/false | HIGH |
| restrict | Prevent drop if foreign key references exist | Boolean | null | true/false | HIGH |

### Why These Attributes?
- Standard Liquibase dropTable doesn't expose CASCADE/RESTRICT options
- Snowflake's default behavior differs by table type
- Explicit control needed for foreign key handling

## 3. Mutual Exclusivity Rules

### CASCADE vs RESTRICT
- Cannot specify both cascade=true and restrict=true
- If neither specified, Snowflake uses table-type default
- If cascade=true, generates CASCADE keyword
- If restrict=true, generates RESTRICT keyword

## 4. SQL Generation Examples

### Example 1: Force CASCADE
```xml
<dropTable tableName="parent_table"
           snowflake:cascade="true"/>
```

Generates:
```sql
DROP TABLE parent_table CASCADE;
```

### Example 2: Explicit RESTRICT
```xml
<dropTable tableName="referenced_table"
           snowflake:restrict="true"/>
```

Generates:
```sql
DROP TABLE referenced_table RESTRICT;
```

### Example 3: Default Behavior
```xml
<dropTable tableName="simple_table"/>
```

Generates:
```sql
DROP TABLE simple_table;
-- Uses Snowflake default: CASCADE for standard, RESTRICT for hybrid
```

### Example 4: With IF EXISTS (standard Liquibase)
```xml
<dropTable tableName="maybe_exists"
           cascadeConstraints="false"
           snowflake:cascade="true"/>
```

Generates:
```sql
DROP TABLE IF EXISTS maybe_exists CASCADE;
```

## 5. Implementation Approach

Using NAMESPACE_ATTRIBUTE_PATTERN_2.md:

1. **Storage**: Use existing `SnowflakeNamespaceAttributeStorage`
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to handle dropTable
3. **Generator**: Create `DropTableGeneratorSnowflake` to add CASCADE/RESTRICT
4. **Priority**: Higher than standard generator to override

## 6. Test Scenarios

### Unit Tests
1. **Basic Tests**:
   - cascade=true generates CASCADE
   - restrict=true generates RESTRICT
   - No attributes uses default
   - Mutual exclusivity validation

2. **Integration Tests**:
   - Works with standard cascadeConstraints attribute
   - Handles table name escaping
   - IF EXISTS compatibility

### Test Harness Tests
1. **dropTableCascade.xml** - Drop with CASCADE
2. **dropTableRestrict.xml** - Drop with RESTRICT (should fail if FK exists)
3. **dropTableDefault.xml** - Default behavior test
4. **dropTableForeignKeys.xml** - Test with actual FK relationships

## 7. Validation Rules

```java
public void validate(Map<String, String> attributes) {
    boolean cascade = "true".equals(attributes.get("cascade"));
    boolean restrict = "true".equals(attributes.get("restrict"));
    
    if (cascade && restrict) {
        throw new ValidationFailedException(
            "Cannot specify both cascade='true' and restrict='true'"
        );
    }
}
```

## 8. Expected Behaviors

### CASCADE Behavior
- Drops table even if referenced by foreign keys
- All referencing foreign keys are also dropped
- Cannot be undone (foreign keys not restored on UNDROP)

### RESTRICT Behavior  
- Fails if table is referenced by foreign keys
- Error message lists referencing constraints
- Safe option to prevent accidental constraint loss

### Default Behavior
- Standard tables: Acts like CASCADE
- Hybrid tables: Acts like RESTRICT
- Important to know table type when relying on default

## 9. Error Conditions

1. **RESTRICT with Dependencies**:
   - Error: "Table cannot be dropped because it is referenced by foreign key"
   - Lists all referencing foreign keys

2. **Mutual Exclusivity**:
   - Error if both cascade and restrict are true

3. **Table Doesn't Exist**:
   - Without IF EXISTS: Error
   - With IF EXISTS: Silent success

## 10. Interaction with Standard Attributes

### cascadeConstraints (Liquibase standard)
- Different meaning than Snowflake CASCADE
- cascadeConstraints: drops constraints on THIS table
- snowflake:cascade: drops foreign keys in OTHER tables
- Can use both together

## 11. Recovery Considerations

### Time Travel
- Dropped tables can be recovered with UNDROP TABLE
- But CASCADE-dropped foreign keys are NOT restored
- Document this limitation for users

### Best Practices
- Use RESTRICT when unsure about dependencies
- Document CASCADE operations carefully
- Consider backing up constraint definitions

## 12. Documentation Requirements

### User Documentation
```xml
<!-- Example: Safe drop with dependency check -->
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <changeSet id="1" author="developer">
        <!-- This will fail if other tables reference customer -->
        <dropTable tableName="customer"
                  snowflake:restrict="true"/>
    </changeSet>
    
    <changeSet id="2" author="developer">
        <!-- This will drop orders table and any FKs referencing it -->
        <dropTable tableName="orders"
                  snowflake:cascade="true"/>
    </changeSet>
</databaseChangeLog>
```

### Warning Documentation
- CASCADE drops cannot be fully undone
- Foreign keys are permanently lost
- RESTRICT is safer for production

## 13. Implementation Priority

This is a HIGH priority enhancement because:
1. Snowflake defaults differ from standard SQL
2. CASCADE can cause data relationship loss
3. Simple to implement with namespace pattern
4. Provides important safety controls

## 14. Testing Notes

### Functional Testing
1. Create tables with foreign key relationships
2. Test RESTRICT fails appropriately
3. Test CASCADE removes foreign keys
4. Verify UNDROP TABLE behavior

### Edge Cases
- Circular foreign key references
- Self-referencing tables
- Multiple foreign keys to same table
- Hybrid vs standard table defaults