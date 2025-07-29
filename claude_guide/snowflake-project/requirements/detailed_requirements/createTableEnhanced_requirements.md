# CreateTable Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/create-table
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake-Specific Table Features
```sql
-- Full Snowflake CREATE TABLE syntax with specific features
CREATE [ OR REPLACE ]
  [ { [ LOCAL | GLOBAL ] TEMPORARY | VOLATILE } | TRANSIENT ]
  TABLE [ IF NOT EXISTS ] <table_name> (
    -- column definitions
  )
  [ CLUSTER BY ( <expr> [ , <expr> , ... ] ) ]
  [ ENABLE_SCHEMA_EVOLUTION = { TRUE | FALSE } ]
  [ STAGE_FILE_FORMAT = ( { FORMAT_NAME = '<file_format_name>' | TYPE = { CSV | JSON | AVRO | ORC | PARQUET | XML } [ formatTypeOptions ] } ) ]
  [ STAGE_COPY_OPTIONS = ( copyOptions ) ]
  [ DATA_RETENTION_TIME_IN_DAYS = <integer> ]
  [ MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer> ]
  [ CHANGE_TRACKING = { TRUE | FALSE } ]
  [ DEFAULT_DDL_COLLATION = '<collation_specification>' ]
  [ COPY GRANTS ]
  [ COMMENT = '<string_literal>' ]
  [ [ WITH ] ROW ACCESS POLICY <policy_name> ON ( <col_name> [ , <col_name> ... ] ) ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ) ]
```

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `createTable`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| transient | Create as transient table | Boolean | false | true/false | HIGH |
| clusterBy | Cluster key columns | String | null | Comma-separated column list | HIGH |
| dataRetentionTimeInDays | Time Travel retention | Integer | 1 | 0-90 | HIGH |
| changeTracking | Enable change tracking | Boolean | false | true/false | MEDIUM |
| copyGrants | Copy grants from replaced table | Boolean | false | true/false | MEDIUM |
| volatile | Create as volatile table | Boolean | false | true/false | LOW |
| temporary | Create as temporary table | Boolean | false | true/false | LOW |
| localTemporary | Create as local temporary | Boolean | false | true/false | LOW |
| globalTemporary | Create as global temporary | Boolean | false | true/false | LOW |
| enableSchemaEvolution | Enable schema evolution | Boolean | false | true/false | LOW |
| maxDataExtensionTimeInDays | Max Time Travel extension | Integer | 14 | 0-90 | LOW |
| defaultDdlCollation | Default collation | String | null | Valid collation spec | LOW |

## 3. Mutual Exclusivity Rules

### Table Type Exclusivity
Only ONE of these can be true:
- transient
- volatile
- temporary
- localTemporary
- globalTemporary

If none specified, creates a permanent table.

### Retention Rules
- If transient=true, dataRetentionTimeInDays must be 0
- If volatile=true or temporary=true, dataRetentionTimeInDays must be 0
- maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

### Copy Grants
- copyGrants only valid when table is being replaced (standard Liquibase doesn't support OR REPLACE)

## 4. SQL Generation Examples

### Example 1: Transient Table with Clustering
```xml
<createTable tableName="user_sessions" 
             snowflake:transient="true"
             snowflake:clusterBy="user_id,session_start">
    <column name="user_id" type="INT"/>
    <column name="session_start" type="TIMESTAMP"/>
    <column name="session_data" type="VARCHAR(1000)"/>
</createTable>
```

Generates:
```sql
CREATE TRANSIENT TABLE user_sessions (
    user_id INT,
    session_start TIMESTAMP,
    session_data VARCHAR(1000)
) CLUSTER BY (user_id, session_start);
```

### Example 2: Table with Change Tracking and Retention
```xml
<createTable tableName="customer_orders"
             snowflake:changeTracking="true"
             snowflake:dataRetentionTimeInDays="7">
    <column name="order_id" type="INT"/>
    <column name="customer_id" type="INT"/>
    <column name="order_date" type="DATE"/>
</createTable>
```

Generates:
```sql
CREATE TABLE customer_orders (
    order_id INT,
    customer_id INT,
    order_date DATE
) DATA_RETENTION_TIME_IN_DAYS = 7 CHANGE_TRACKING = TRUE;
```

### Example 3: Temporary Table
```xml
<createTable tableName="temp_calculations"
             snowflake:temporary="true">
    <column name="calc_id" type="INT"/>
    <column name="result" type="DECIMAL(10,2)"/>
</createTable>
```

Generates:
```sql
CREATE TEMPORARY TABLE temp_calculations (
    calc_id INT,
    result DECIMAL(10,2)
);
```

## 5. Implementation Approach

Using EXISTING_CHANGETYPE_EXTENSION_PATTERN.md:

1. **Storage**: `SnowflakeNamespaceAttributeStorage` to capture attributes
2. **Parser**: `SnowflakeNamespaceAwareXMLParser` to intercept snowflake: attributes
3. **Generator**: `CreateTableGeneratorSnowflake` to modify SQL based on attributes
4. **Testing**: Unit tests for each component, integration tests for full flow

## 6. Test Scenarios

### Unit Tests
1. **Storage Tests**: Thread-safe attribute storage/retrieval
2. **Parser Tests**: Correct capture of snowflake: attributes
3. **Generator Tests**: SQL modification for each attribute
4. **Validation Tests**: Mutual exclusivity rules enforced

### Test Harness Tests
1. **createTableTransient.xml** - Transient table creation
2. **createTableClustered.xml** - Table with clustering keys
3. **createTableRetention.xml** - Time Travel settings
4. **createTableTracking.xml** - Change tracking enabled
5. **createTableTemporary.xml** - Various temporary table types

## 7. Validation Rules

1. **Table Type Validation**:
   ```java
   int typeCount = 0;
   if (transient) typeCount++;
   if (volatile) typeCount++;
   if (temporary || localTemporary || globalTemporary) typeCount++;
   if (typeCount > 1) {
       throw new ValidationFailedException("Only one table type can be specified");
   }
   ```

2. **Retention Validation**:
   ```java
   if ((transient || volatile || temporary) && dataRetentionTimeInDays > 0) {
       throw new ValidationFailedException("Transient/volatile/temporary tables must have 0 retention days");
   }
   ```

3. **Cluster Key Validation**:
   - Verify columns exist in table definition
   - Check for valid column names

## 8. Expected Behaviors

1. **Attribute Processing**:
   - Attributes captured during XML parsing
   - Stored temporarily in thread-safe storage
   - Applied during SQL generation
   - Cleaned up after use

2. **SQL Modification**:
   - Table type keywords inserted after CREATE
   - Properties appended after column definitions
   - Proper spacing and formatting maintained

3. **Error Handling**:
   - Invalid attribute combinations caught early
   - Clear error messages for validation failures
   - Graceful fallback if no attributes present

## 9. Integration Notes

### With Standard Liquibase
- Enhances existing createTable change
- Backwards compatible (ignores unknown namespaces)
- Works with all standard table features

### With Snowflake Extension
- Complements new change types (createSchema, etc.)
- Uses same namespace URL pattern
- Consistent attribute naming

## 10. Documentation Requirements

### User Documentation
- How to add snowflake namespace to changelog
- Available attributes and their effects
- Examples of common use cases

### Developer Documentation
- How the namespace pattern works
- Adding new attributes guide
- Testing approach for attributes

## 11. Future Enhancements

Potential additions based on Snowflake roadmap:
- Row access policies
- Masking policies
- Tag management
- Stage file format options
- External table support

## 12. Performance Considerations

- Attribute storage is in-memory only
- Cleanup after each use prevents memory leaks
- Thread-safe for concurrent changelog processing
- Minimal overhead for non-Snowflake databases