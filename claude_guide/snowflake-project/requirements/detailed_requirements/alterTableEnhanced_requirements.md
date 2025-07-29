# AlterTable Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-table
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake-Specific ALTER TABLE Features
```sql
-- Clustering
ALTER TABLE <name> CLUSTER BY ( <expr> [ , <expr> , ... ] );
ALTER TABLE <name> DROP CLUSTERING KEY;
ALTER TABLE <name> SUSPEND | RESUME RECLUSTER;

-- Schema Evolution
ALTER TABLE <name> SET ENABLE_SCHEMA_EVOLUTION = { TRUE | FALSE };

-- Time Travel and Data Retention
ALTER TABLE <name> SET DATA_RETENTION_TIME_IN_DAYS = <integer>;
ALTER TABLE <name> SET MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer>;

-- Change Tracking
ALTER TABLE <name> SET CHANGE_TRACKING = { TRUE | FALSE };

-- Search Optimization
ALTER TABLE <name> ADD SEARCH OPTIMIZATION [ ON <search_method_with_target> [ , ... ] ];
ALTER TABLE <name> DROP SEARCH OPTIMIZATION [ ON <search_method_with_target> [ , ... ] ];

-- Row Access Policy
ALTER TABLE <name> ADD ROW ACCESS POLICY <policy_name> ON (<col_name> [ , ... ]);
ALTER TABLE <name> DROP ROW ACCESS POLICY <policy_name>;
ALTER TABLE <name> DROP ALL ROW ACCESS POLICIES;

-- Aggregation Policy
ALTER TABLE <name> SET AGGREGATION POLICY <policy_name> [ FORCE ];
ALTER TABLE <name> UNSET AGGREGATION POLICY;

-- Projection Policy
ALTER TABLE <name> SET PROJECTION POLICY <policy_name> [ FORCE ];
ALTER TABLE <name> UNSET PROJECTION POLICY;

-- Tags
ALTER TABLE <name> SET TAG <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ];
ALTER TABLE <name> UNSET TAG <tag_name> [ , <tag_name> , ... ];
```

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `alterTable`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| clusterBy | Set clustering key columns | String | null | Comma-separated column list | HIGH |
| dropClusteringKey | Remove clustering key | Boolean | false | true/false | HIGH |
| suspendRecluster | Suspend automatic reclustering | Boolean | false | true/false | MEDIUM |
| resumeRecluster | Resume automatic reclustering | Boolean | false | true/false | MEDIUM |
| setEnableSchemaEvolution | Enable schema evolution | Boolean | null | true/false | HIGH |
| setDataRetentionTimeInDays | Set Time Travel retention | Integer | null | 0-90 | HIGH |
| setMaxDataExtensionTimeInDays | Set max Time Travel extension | Integer | null | 0-90 | MEDIUM |
| setChangeTracking | Enable change tracking | Boolean | null | true/false | HIGH |
| addSearchOptimization | Add search optimization | String | null | Search method specification | LOW |
| dropSearchOptimization | Remove search optimization | Boolean | false | true/false | LOW |
| addRowAccessPolicy | Add row access policy | String | null | Policy name and columns | LOW |
| dropRowAccessPolicy | Drop row access policy | String | null | Policy name | LOW |
| setAggregationPolicy | Set aggregation policy | String | null | Policy name | LOW |
| unsetAggregationPolicy | Remove aggregation policy | Boolean | false | true/false | LOW |

## 3. Mutual Exclusivity Rules

### Clustering Operations
Only ONE clustering operation per ALTER statement:
- clusterBy
- dropClusteringKey  
- suspendRecluster
- resumeRecluster

### Policy Operations
Cannot set and unset same policy type in one operation:
- setAggregationPolicy vs unsetAggregationPolicy
- addRowAccessPolicy vs dropRowAccessPolicy

### Property Operations
Can combine multiple SET operations but not SET and UNSET for same property.

## 4. SQL Generation Examples

### Example 1: Add/Modify Clustering
```xml
<alterTable tableName="sales_data"
            snowflake:clusterBy="region,sale_date">
    <!-- Standard alterTable changes if any -->
</alterTable>
```

Generates:
```sql
ALTER TABLE sales_data CLUSTER BY (region, sale_date);
```

### Example 2: Enable Features
```xml
<alterTable tableName="customer_orders"
            snowflake:setEnableSchemaEvolution="true"
            snowflake:setChangeTracking="true"
            snowflake:setDataRetentionTimeInDays="30">
    <!-- Standard alterTable changes if any -->
</alterTable>
```

Generates:
```sql
ALTER TABLE customer_orders SET 
    ENABLE_SCHEMA_EVOLUTION = TRUE,
    CHANGE_TRACKING = TRUE,
    DATA_RETENTION_TIME_IN_DAYS = 30;
```

### Example 3: Drop Clustering
```xml
<alterTable tableName="old_table"
            snowflake:dropClusteringKey="true">
    <!-- Standard alterTable changes if any -->
</alterTable>
```

Generates:
```sql
ALTER TABLE old_table DROP CLUSTERING KEY;
```

## 5. Implementation Approach

Using EXISTING_CHANGETYPE_EXTENSION_PATTERN.md:

1. **Storage**: Use existing `SnowflakeNamespaceAttributeStorage`
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to handle alterTable
3. **Generator**: Create `AlterTableGeneratorSnowflake` to apply Snowflake-specific changes
4. **Sequencing**: Apply Snowflake changes after standard alterTable operations

## 6. Test Scenarios

### Unit Tests
1. **Clustering Tests**:
   - Add clustering key
   - Drop clustering key
   - Suspend/resume reclustering
   - Mutual exclusivity validation

2. **Property Tests**:
   - Set schema evolution
   - Set retention times
   - Enable change tracking
   - Combine multiple properties

3. **Policy Tests**:
   - Add/drop row access policies
   - Set/unset aggregation policies

### Test Harness Tests
1. **alterTableClustering.xml** - Clustering operations
2. **alterTableProperties.xml** - Schema evolution, retention, tracking
3. **alterTablePolicies.xml** - Security policies (if supported)
4. **alterTableCombined.xml** - Mixed standard and Snowflake changes

## 7. Integration Considerations

### With Standard alterTable
- Snowflake operations append to standard ALTER TABLE
- Some operations may require separate statements
- Order matters: standard changes first, then Snowflake-specific

### Multiple Statements
Some combinations require multiple ALTER statements:
```sql
-- Standard column changes
ALTER TABLE mytable ADD COLUMN new_col VARCHAR(100);
-- Snowflake clustering (separate statement)
ALTER TABLE mytable CLUSTER BY (id, created_at);
```

## 8. Validation Rules

1. **Clustering Validation**:
   - Columns must exist in table
   - Cannot have multiple clustering operations

2. **Retention Validation**:
   - dataRetentionTimeInDays: 0-90
   - maxDataExtensionTimeInDays >= dataRetentionTimeInDays

3. **Boolean Conflicts**:
   - Only one clustering operation
   - Cannot set and unset same property

## 9. Error Conditions

1. Invalid column names in clusterBy
2. Retention days out of range
3. Policy doesn't exist
4. Insufficient privileges for policies
5. Conflicting operations

## 10. Performance Considerations

### Clustering Impact
- Reclustering runs in background
- Can be expensive for large tables
- suspendRecluster useful during bulk operations

### Schema Evolution
- Affects future data loads
- No immediate performance impact

### Change Tracking
- Maintains change history
- Slight overhead on DML operations

## 11. Documentation Requirements

### Usage Examples
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <changeSet id="1" author="developer">
        <!-- Enable Snowflake features on existing table -->
        <alterTable tableName="product_sales"
                   snowflake:clusterBy="product_id,sale_date"
                   snowflake:setChangeTracking="true">
            <addColumn>
                <column name="region" type="VARCHAR(50)"/>
            </addColumn>
        </alterTable>
    </changeSet>
</databaseChangeLog>
```

## 12. Priority Implementation Order

1. **HIGH Priority**:
   - clusterBy / dropClusteringKey
   - setEnableSchemaEvolution
   - setChangeTracking
   - setDataRetentionTimeInDays

2. **MEDIUM Priority**:
   - suspendRecluster / resumeRecluster
   - setMaxDataExtensionTimeInDays

3. **LOW Priority**:
   - Search optimization
   - Security policies
   - Tags

## 13. Future Enhancements

- Support for hybrid table specific features
- Dynamic table alterations
- External table modifications
- Masking policy support on columns