# Snowflake Extension Implementation Insights

This document captures the key insights and learnings from implementing the Snowflake extension for Liquibase.

## Architecture Overview

### Three-Layer Pattern

The Snowflake extension follows Liquibase's standard three-layer architecture:

1. **Change Layer** (`liquibase.change.core.*`)
   - Entry point for change operations
   - Handles XML/YAML parsing and validation
   - Generates Statement objects

2. **Statement Layer** (`liquibase.statement.core.*`)
   - Simple data holders (POJOs)
   - No business logic
   - Passed to SQL generators

3. **SQL Generator Layer** (`liquibase.sqlgenerator.core.snowflake.*`)
   - Converts statements to SQL
   - Database-specific logic
   - Handles validation at SQL level

### Key Implementation Patterns

#### 1. Change Type Implementation
```java
@DatabaseChange(name = "createWarehouse", description = "Creates a warehouse", priority = ChangeMetaData.PRIORITY_DEFAULT)
public class CreateWarehouseChange extends AbstractChange {
    @DatabaseChangeProperty(description = "Name of the warehouse")
    private String warehouseName;
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateWarehouseStatement(warehouseName, warehouseSize, /*...*/)
        };
    }
}
```

#### 2. Service Registration
All components must be registered in `META-INF/services/`:
- `liquibase.change.Change` - for change types
- `liquibase.sqlgenerator.SqlGenerator` - for SQL generators
- `liquibase.snapshot.SnapshotGenerator` - for snapshot support

#### 3. XML Namespace Support
Custom namespace handler enables Snowflake-specific XML elements:
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake">
    <changeSet id="1" author="liquibase">
        <snowflake:createWarehouse warehouseName="MY_WH" warehouseSize="XSMALL"/>
    </changeSet>
</databaseChangeLog>
```

## Snowflake-Specific Considerations

### 1. Object Types Implemented

| Object Type | Changes Implemented | Key Features |
|------------|-------------------|--------------|
| WAREHOUSE | Create, Alter, Drop | Multi-cluster, auto-suspend, scaling policy |
| DATABASE | Create, Alter, Drop | Retention, transient, cloning |
| SCHEMA | Create, Alter, Drop | Managed access, cloning |
| TABLE | Enhanced Create | Clustering, retention, transient |
| SEQUENCE | Enhanced Create | ORDER/NOORDER support |

### 2. Unique Snowflake Features

**Transient Objects**
- Don't contribute to storage costs
- No fail-safe period
- Implemented via boolean flags

**Clustering Keys**
- Optimize micro-partitions
- Implemented as comma-separated column list
- Critical for query performance

**Data Retention**
- Time Travel configuration
- 0-90 days for permanent tables
- 0-1 day for transient tables

**Multi-Cluster Warehouses**
- Scaling MIN/MAX clusters
- Auto-scale vs auto-suspend
- Scaling policy configuration

### 3. Implementation Challenges and Solutions

#### Challenge: No Custom DatabaseObject Types
**Solution**: Store warehouse info as schema attributes in snapshots
```java
@Override
protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) {
    if (foundObject instanceof Schema) {
        Schema schema = (Schema) foundObject;
        // Store warehouse info as schema attributes
        schema.setAttribute("warehouses", warehouses);
    }
}
```

#### Challenge: Encoding Snowflake-Specific Options
**Solution**: Creative use of existing fields
- Table remarks for clustering keys
- Tablespace for transient flag
- Custom attributes in XML

#### Challenge: Case Sensitivity
**Solution**: Consistent uppercase handling
```java
private String normalizeIdentifier(String identifier) {
    return identifier == null ? null : identifier.toUpperCase();
}
```

## Testing Strategy

### 1. Test Structure

**Unit Tests** (in snowflake module)
- Test change validation
- Test SQL generation
- Test statement creation

**Integration Tests** (in liquibase-integration-tests)
- Real Snowflake connection
- Full lifecycle testing
- Rollback verification

### 2. Test Patterns

#### Direct Change Testing
```groovy
def "create warehouse test"() {
    given:
    def change = new CreateWarehouseChange()
    change.warehouseName = "TEST_WH"
    
    when:
    changeSet.addChange(change)
    changelog.execute(database, contexts)
    
    then:
    warehouseExists("TEST_WH")
}
```

#### Changelog-Based Testing
```groovy
def "execute warehouse changelog"() {
    when:
    liquibase.update(contexts)
    
    then:
    // Verify changes applied
}
```

### 3. Test Organization

```
liquibase-integration-tests/
└── src/test/
    ├── groovy/liquibase/test/snowflake/
    │   ├── WarehouseSnowflakeIntegrationTest.groovy
    │   └── WarehouseChangelogSnowflakeIntegrationTest.groovy
    └── resources/changelogs/snowflake/
        └── warehouse/
            ├── warehouse.test.changelog.xml
            └── warehouse.rollback.changelog.xml
```

## Best Practices

### 1. Change Implementation
- Always extend `AbstractChange`
- Use `@DatabaseChangeProperty` for all properties
- Implement comprehensive validation
- Support rollback where possible

### 2. SQL Generation
- Check database type before generating
- Use `ValidationErrors` for clear error messages
- Handle quoted vs unquoted identifiers
- Follow Snowflake SQL syntax exactly

### 3. Testing
- Test both success and failure cases
- Verify rollback functionality
- Use consistent naming (LB_TEST_*)
- Clean up test objects

### 4. Documentation
- Document all change properties
- Provide clear descriptions
- Include examples in XSD
- Update namespace version

## Common Pitfalls

1. **Forgetting Service Registration**
   - Changes won't be discovered without META-INF/services entry

2. **Case Sensitivity Issues**
   - Snowflake uppercases unquoted identifiers
   - Be consistent with identifier handling

3. **Missing XSD Updates**
   - New changes need XSD definition
   - Update namespace version when adding features

4. **Incomplete Validation**
   - Validate at both change and generator levels
   - Check for conflicting options

5. **Test Pollution**
   - Always clean up test objects
   - Use unique names with timestamps

## Future Enhancements

### Potential Improvements
1. Full snapshot support for all object types
2. Diff/compare functionality
3. More sophisticated rollback support
4. Performance optimization settings
5. Tag and stream object support

### Extension Points
- Custom DatabaseObject implementations
- Enhanced snapshot generators
- Specialized diff generators
- Custom validation rules

## Quick Reference

### Adding a New Change Type
1. Create change class extending `AbstractChange`
2. Create statement class implementing `SqlStatement`
3. Create SQL generator extending `AbstractSqlGenerator`
4. Add to META-INF/services files
5. Update XSD schema
6. Write unit and integration tests

### Maven Commands
```bash
# Build Snowflake extension
./mvnw clean install -pl liquibase-snowflake -am

# Run Snowflake tests
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake

# Skip problematic modules
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'
```

### Debugging Tips
- Enable SQL logging: `-Dliquibase.sql.logLevel=FINE`
- Check service registration files
- Verify XSD namespace resolution
- Use integration tests for real validation