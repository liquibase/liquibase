# AlterTable Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-table
- Version: Snowflake 2024
- Last Updated: 2025-07-29

### Basic Syntax
```sql
-- Core Liquibase alterTable operations (already handled by core)
ALTER TABLE <name> ADD COLUMN <column_definition>;
ALTER TABLE <name> DROP COLUMN <column_name>;
ALTER TABLE <name> RENAME COLUMN <old_name> TO <new_name>;
ALTER TABLE <name> MODIFY COLUMN <column_name> <new_definition>;

-- Snowflake-specific alterTable operations (our focus)
ALTER TABLE <name> CLUSTER BY ( <expr> [ , <expr> , ... ] );
ALTER TABLE <name> DROP CLUSTERING KEY;
ALTER TABLE <name> SUSPEND RECLUSTER;
ALTER TABLE <name> RESUME RECLUSTER;
ALTER TABLE <name> SET DATA_RETENTION_TIME_IN_DAYS = <integer>;
ALTER TABLE <name> SET CHANGE_TRACKING = { TRUE | FALSE };
ALTER TABLE <name> SET ENABLE_SCHEMA_EVOLUTION = { TRUE | FALSE };
```

## 2. Attribute Analysis

Our alterTable change type will focus on Snowflake-specific operations via namespace attributes:

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| tableName | Name of the table to alter | String | - | Valid identifier | Yes |
| clusterBy | Set clustering key columns | String | null | Comma-separated column expressions | No |
| dropClusteringKey | Remove clustering key | Boolean | false | true/false | No |
| suspendRecluster | Suspend automatic reclustering | Boolean | false | true/false | No |
| resumeRecluster | Resume automatic reclustering | Boolean | false | true/false | No |
| setDataRetentionTimeInDays | Set Time Travel retention | Integer | null | 0-90 | No |
| setChangeTracking | Enable/disable change tracking | Boolean | null | true/false | No |
| setEnableSchemaEvolution | Enable/disable schema evolution | Boolean | null | true/false | No |

## 3. Mutual Exclusivity Rules

### Clustering Operations
Only ONE clustering operation per change:
1. `clusterBy` cannot be combined with `dropClusteringKey`, `suspendRecluster`, or `resumeRecluster`
2. `dropClusteringKey` cannot be combined with other clustering operations
3. `suspendRecluster` and `resumeRecluster` are mutually exclusive

### Property Operations
- Multiple SET operations can be combined in a single ALTER statement
- Boolean attributes with null value are ignored (no SQL generated)

## 4. SQL Examples for Testing

### Example 1: Basic Clustering
```sql
ALTER TABLE sales_data CLUSTER BY (region, sale_date);
```

### Example 2: Drop Clustering
```sql
ALTER TABLE old_table DROP CLUSTERING KEY;
```

### Example 3: Multiple Properties
```sql
ALTER TABLE customer_data SET 
    DATA_RETENTION_TIME_IN_DAYS = 30,
    CHANGE_TRACKING = TRUE,
    ENABLE_SCHEMA_EVOLUTION = TRUE;
```

### Example 4: Suspend Reclustering
```sql
ALTER TABLE large_table SUSPEND RECLUSTER;
```

### Example 5: Resume Reclustering
```sql
ALTER TABLE large_table RESUME RECLUSTER;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:

1. **alterTable.xml** - Basic clustering and property combinations
2. **alterTableClustering.xml** - All clustering operations separately  
3. **alterTableProperties.xml** - Property setting combinations

## 6. Validation Rules

1. **Required Attributes**:
   - tableName cannot be null or empty
   - Must be valid identifier (alphanumeric, underscore, dollar sign)

2. **Mutual Exclusivity**:
   - If multiple clustering operations specified, throw: "Only one clustering operation allowed per alterTable"
   - If suspendRecluster=true and resumeRecluster=true, throw: "Cannot both suspend and resume reclustering"

3. **Value Constraints**:
   - setDataRetentionTimeInDays must be between 0 and 90
   - clusterBy column expressions must be valid SQL expressions

4. **Combination Rules**:
   - clusterBy and dropClusteringKey cannot both be true
   - At least one Snowflake-specific operation must be specified

## 7. Expected Behaviors

1. **CLUSTER BY behavior**:
   - Defines micro-partition clustering for better query performance
   - Columns are clustered in the order specified
   - Automatic reclustering maintains clustering over time

2. **DROP CLUSTERING KEY behavior**:
   - Removes clustering definition from table
   - Does not reorganize existing data immediately
   - Stops automatic reclustering

3. **SUSPEND/RESUME RECLUSTER behavior**:
   - Controls automatic background reclustering
   - Useful during bulk loading operations
   - Does not affect existing clustering definition

4. **Property Setting behavior**:
   - Multiple SET operations combined into single ALTER statement
   - Properties take effect immediately
   - Some properties affect future operations only

## 8. Error Conditions

1. Table does not exist
2. Column referenced in clusterBy does not exist
3. Insufficient privileges to alter table
4. Invalid retention time values (outside 0-90 range)
5. Conflicting clustering operations specified
6. Invalid SQL expressions in clusterBy

## 9. Implementation Notes

- This change type focuses on Snowflake-specific table alterations
- Standard column operations (add, drop, modify) handled by core Liquibase
- Operations may generate multiple SQL statements if needed
- Clustering operations are resource-intensive and should be used judiciously
- Some operations require table-level privileges beyond standard ALTER
- Consider performance impact of clustering on large tables

## 10. Integration with Core Liquibase

- This change type complements but does not replace core alterTable
- Can be used alongside standard alterTable in same changeset
- Snowflake-specific operations execute after standard operations
- Follows namespace attribute pattern for clean separation