# AlterTable Enhanced Requirements (Snowflake Namespace Attributes)

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
OPERATION: "ALTER"
NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
ESTIMATED_IMPLEMENTATION_TIME: "4-5 hours"
```
---

## CORE_REQUIREMENTS

### Primary Objective
Extend Liquibase's standard `alterTable` changetype with Snowflake-specific namespace attributes to enable Snowflake-exclusive table features including clustering, schema evolution, change tracking, data retention, and performance optimization capabilities.

### Critical Success Criteria
1. **Clustering Control**: Enable clustering key management (add, drop, suspend/resume reclustering)
2. **Schema Evolution**: Control schema evolution settings for flexible data loading
3. **Change Tracking**: Enable/disable change tracking for data integration scenarios
4. **Data Retention**: Configure Time Travel retention periods
5. **Performance Features**: Control search optimization and reclustering behavior
6. **Standard Compatibility**: Work seamlessly with existing Liquibase `alterTable` operations

## SNOWFLAKE_TECHNICAL_RESEARCH

### SQL Syntax (Snowflake Official Documentation)
**Documentation Reference**: https://docs.snowflake.com/en/sql-reference/sql/alter-table

### Core Snowflake ALTER TABLE Extensions
```sql
-- Clustering Management
ALTER TABLE <name> CLUSTER BY ( <expr> [ , <expr> , ... ] );
ALTER TABLE <name> DROP CLUSTERING KEY;
ALTER TABLE <name> SUSPEND | RESUME RECLUSTER;

-- Schema and Data Management  
ALTER TABLE <name> SET ENABLE_SCHEMA_EVOLUTION = { TRUE | FALSE };
ALTER TABLE <name> SET DATA_RETENTION_TIME_IN_DAYS = <integer>;
ALTER TABLE <name> SET MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer>;
ALTER TABLE <name> SET CHANGE_TRACKING = { TRUE | FALSE };

-- Performance Optimization
ALTER TABLE <name> ADD SEARCH OPTIMIZATION;
ALTER TABLE <name> DROP SEARCH OPTIMIZATION;

-- Security and Governance (Future Phase)
ALTER TABLE <name> ADD ROW ACCESS POLICY <policy_name> ON (<col_list>);
ALTER TABLE <name> SET AGGREGATION POLICY <policy_name>;
```

### Snowflake-Specific Behaviors
1. **Clustering Keys**:
   - Automatic reclustering runs in background
   - Can be suspended during bulk operations
   - Significant performance impact on large tables

2. **Schema Evolution**:
   - Affects Snowpipe and external stage loading
   - Allows automatic schema changes during data ingestion
   - Critical for semi-structured data scenarios

3. **Change Tracking**:
   - Enables efficient incremental data processing
   - Required for STREAM objects
   - Minimal performance overhead

4. **Data Retention**:
   - Controls Time Travel retention (0-90 days)
   - Extension time controls fail-safe period
   - Affects storage costs

## NAMESPACE_ATTRIBUTES_SPECIFICATION

### High Priority Attributes (Phase 2)
| Attribute | Type | Default | Values | Validation | Description |
|-----------|------|---------|--------|------------|-------------|
| `clusterBy` | String | null | Comma-separated column list | Columns must exist | Set clustering key |
| `dropClusteringKey` | Boolean | false | true/false | Mutually exclusive with clusterBy | Remove clustering key |
| `suspendRecluster` | Boolean | false | true/false | Mutually exclusive with resumeRecluster | Suspend automatic reclustering |
| `resumeRecluster` | Boolean | false | true/false | Mutually exclusive with suspendRecluster | Resume automatic reclustering |
| `setEnableSchemaEvolution` | Boolean | null | true/false | None | Enable/disable schema evolution |
| `setChangeTracking` | Boolean | null | true/false | None | Enable/disable change tracking |
| `setDataRetentionTimeInDays` | Integer | null | 0-90 | Valid range | Set Time Travel retention |

### Medium Priority Attributes (Future Enhancement)
| Attribute | Type | Default | Values | Validation | Description |
|-----------|------|---------|--------|------------|-------------|
| `setMaxDataExtensionTimeInDays` | Integer | null | 0-90 | >= dataRetentionTimeInDays | Set fail-safe extension |
| `addSearchOptimization` | Boolean | false | true/false | None | Enable search optimization |
| `dropSearchOptimization` | Boolean | false | true/false | None | Disable search optimization |

### Mutual Exclusivity Rules
```java
// Clustering Operations (only one per ALTER)
Set<String> clusteringOps = Set.of("clusterBy", "dropClusteringKey", 
                                  "suspendRecluster", "resumeRecluster");
int activeClusteringOps = countActiveAttributes(attributes, clusteringOps);
if (activeClusteringOps > 1) {
    throw new ValidationFailedException(
        "Only one clustering operation allowed per alterTable"
    );
}

// Recluster Operations
if (suspendRecluster && resumeRecluster) {
    throw new ValidationFailedException(
        "Cannot suspend and resume reclustering in same operation"
    );
}
```

## XML_USAGE_EXAMPLES

### Example 1: Add Clustering Key
```xml
<changeSet id="add-clustering" author="developer">
    <alterTable tableName="sales_data"
                snowflake:clusterBy="region,sale_date">
        <addColumn>
            <column name="channel" type="VARCHAR(50)"/>
        </addColumn>
    </alterTable>
</changeSet>
```
**Generated SQL**:
```sql
ALTER TABLE sales_data ADD COLUMN channel VARCHAR(50);
ALTER TABLE sales_data CLUSTER BY (region, sale_date);
```

### Example 2: Enable Multiple Features
```xml
<changeSet id="enable-features" author="developer">
    <alterTable tableName="customer_data"
                snowflake:setEnableSchemaEvolution="true"
                snowflake:setChangeTracking="true"
                snowflake:setDataRetentionTimeInDays="30"/>
</changeSet>
```
**Generated SQL**:
```sql
ALTER TABLE customer_data SET 
    ENABLE_SCHEMA_EVOLUTION = TRUE,
    CHANGE_TRACKING = TRUE,
    DATA_RETENTION_TIME_IN_DAYS = 30;
```

### Example 3: Suspend Reclustering During Bulk Load
```xml
<changeSet id="prepare-bulk-load" author="developer">
    <alterTable tableName="large_table"
                snowflake:suspendRecluster="true"/>
</changeSet>

<!-- Bulk data operations here -->

<changeSet id="resume-clustering" author="developer">
    <alterTable tableName="large_table"
                snowflake:resumeRecluster="true"/>
</changeSet>
```

### Example 4: Drop Clustering Key
```xml
<changeSet id="remove-clustering" author="developer">
    <alterTable tableName="old_table"
                snowflake:dropClusteringKey="true">
        <modifyDataType tableName="old_table" columnName="id" newDataType="BIGINT"/>
    </alterTable>
</changeSet>
```
**Generated SQL**:
```sql
ALTER TABLE old_table ALTER COLUMN id SET DATA TYPE BIGINT;
ALTER TABLE old_table DROP CLUSTERING KEY;
```

## IMPLEMENTATION_ARCHITECTURE

### Pattern: Existing Changetype Extension
Following `/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/implementation_patterns/EXISTING_CHANGETYPE_EXTENSION_PATTERN.md`

### Core Components

#### 1. Storage Component
**File**: `SnowflakeNamespaceAttributeStorage.java` (existing)
- Store clustering, schema evolution, and tracking attributes
- Provide validation for mutual exclusivity and value ranges

#### 2. Parser Extension
**File**: `SnowflakeNamespaceAwareXMLParser.java` (existing)
```java
// Add alterTable handling
if ("alterTable".equals(localName)) {
    extractSnowflakeAttributes(attributes, SUPPORTED_ALTER_TABLE_ATTRIBUTES);
}

private static final Set<String> SUPPORTED_ALTER_TABLE_ATTRIBUTES = Set.of(
    "clusterBy", "dropClusteringKey", "suspendRecluster", "resumeRecluster",
    "setEnableSchemaEvolution", "setChangeTracking", "setDataRetentionTimeInDays"
);
```

#### 3. Generator Implementation
**File**: `AlterTableGeneratorSnowflake.java` (new)
```java
@DatabaseChangeProperty(requiredForDatabase = "snowflake")
public class AlterTableGeneratorSnowflake extends AlterTableGenerator {
    
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Higher than standard
    }
    
    @Override
    public Sql[] generateSql(AlterTableStatement statement, Database database, 
                           SqlGeneratorChain sqlGeneratorChain) {
        
        List<Sql> sqlStatements = new ArrayList<>();
        
        // Generate standard ALTER TABLE operations first
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        sqlStatements.addAll(Arrays.asList(baseSql));
        
        // Add Snowflake-specific operations
        sqlStatements.addAll(generateSnowflakeOperations(statement, database));
        
        return sqlStatements.toArray(new Sql[0]);
    }
    
    private List<Sql> generateSnowflakeOperations(AlterTableStatement statement, 
                                                 Database database) {
        List<Sql> snowflakeOps = new ArrayList<>();
        
        // Clustering operations (mutually exclusive)
        addClusteringOperations(snowflakeOps, statement, database);
        
        // Property SET operations (can be combined)
        addPropertyOperations(snowflakeOps, statement, database);
        
        return snowflakeOps;
    }
}
```

#### 4. SQL Generation Strategy
1. **Standard Operations First**: Execute standard Liquibase alterTable changes
2. **Snowflake Operations Second**: Apply Snowflake-specific enhancements
3. **Multiple Statements**: Some operations require separate ALTER statements
4. **Validation**: Enforce mutual exclusivity and value ranges

## TESTING_REQUIREMENTS

### Unit Test Coverage
**File**: `AlterTableGeneratorSnowflakeTest.java`

#### Test Categories
1. **Clustering Tests**
   - Add clustering key with valid column list
   - Drop clustering key generation
   - Suspend/resume reclustering
   - Mutual exclusivity validation

2. **Property Tests**
   - Schema evolution enable/disable
   - Change tracking enable/disable
   - Data retention time validation (0-90 days)
   - Multiple property combinations

3. **Integration Tests**
   - Combined with standard alterTable operations
   - Multiple Snowflake operations
   - Table name escaping and schema qualification

4. **Validation Tests**
   - Clustering operation mutual exclusivity
   - Data retention time range validation
   - Invalid column names in clusterBy

### Test Harness Integration
**Directory**: `/src/test/resources/changelogs/snowflake/alterTable/`

#### Test Files
1. **alterTableClustering.xml**
   ```xml
   <changeSet id="1" author="test">
       <alterTable tableName="test_table" snowflake:clusterBy="id,created_at"/>
   </changeSet>
   ```

2. **alterTableProperties.xml**
   ```xml
   <changeSet id="1" author="test">  
       <alterTable tableName="test_table"
                   snowflake:setEnableSchemaEvolution="true"
                   snowflake:setChangeTracking="true"
                   snowflake:setDataRetentionTimeInDays="7"/>
   </changeSet>
   ```

3. **alterTableCombined.xml**
   ```xml
   <changeSet id="1" author="test">
       <alterTable tableName="test_table" snowflake:clusterBy="region">
           <addColumn>
               <column name="new_col" type="VARCHAR(100)"/>
           </addColumn>
       </alterTable>
   </changeSet>
   ```

4. **alterTableReclustering.xml**
   ```xml
   <changeSet id="1" author="test">
       <alterTable tableName="large_table" snowflake:suspendRecluster="true"/>
   </changeSet>
   <changeSet id="2" author="test">
       <alterTable tableName="large_table" snowflake:resumeRecluster="true"/>
   </changeSet>
   ```

### Functional Test Scenarios
1. **Clustering Performance Testing**
   - Create large table with clustering key
   - Measure query performance with/without clustering
   - Test suspend/resume during bulk operations

2. **Schema Evolution Testing**
   - Enable schema evolution on table
   - Test automatic schema changes during data loading
   - Verify semi-structured data handling

3. **Change Tracking Testing**
   - Enable change tracking
   - Perform table modifications
   - Verify change tracking functionality with STREAM objects

## ERROR_HANDLING

### Validation Errors
1. **Clustering Mutual Exclusivity**
   ```
   Error: Only one clustering operation allowed per alterTable
   Operations: clusterBy, dropClusteringKey, suspendRecluster, resumeRecluster
   Location: changeSet 'id' in file.xml
   ```

2. **Data Retention Range**
   ```
   Error: setDataRetentionTimeInDays must be between 0 and 90
   Provided value: 120
   Location: changeSet 'id' in file.xml  
   ```

3. **Invalid Column Names**
   ```
   Error: Column 'nonexistent_col' in clusterBy does not exist in table 'test_table'
   Location: changeSet 'id' in file.xml
   ```

### Runtime Errors
1. **Insufficient Privileges**
   ```sql
   -- Snowflake Error  
   SQL access control error: Insufficient privileges to operate on table 'TEST_TABLE'
   ```

2. **Invalid Clustering Columns**
   ```sql
   -- Snowflake Error
   SQL compilation error: Invalid clustering key column 'invalid_col'
   ```

## COMPATIBILITY_MATRIX

### Standard Liquibase Attributes
| Standard Attribute | Snowflake Compatibility | Execution Order |
|-------------------|------------------------|-----------------|
| `addColumn` | COMPATIBLE | Before Snowflake operations |
| `dropColumn` | COMPATIBLE | Before Snowflake operations |
| `modifyDataType` | COMPATIBLE | Before Snowflake operations |
| `addForeignKeyConstraint` | COMPATIBLE | Before Snowflake operations |
| `addPrimaryKey` | COMPATIBLE | Before Snowflake operations |

### Operation Sequencing
1. **Standard Operations**: Execute all standard Liquibase alterTable operations
2. **Snowflake Clustering**: Apply clustering changes (if any)
3. **Snowflake Properties**: Apply property changes (can be combined in single statement)

### Multi-Statement Generation
Some combinations require multiple ALTER statements:
```sql
-- Standard operations
ALTER TABLE mytable ADD COLUMN new_col VARCHAR(100);

-- Snowflake clustering (separate statement)  
ALTER TABLE mytable CLUSTER BY (id, new_col);

-- Snowflake properties (can be combined)
ALTER TABLE mytable SET CHANGE_TRACKING = TRUE, 
                        DATA_RETENTION_TIME_IN_DAYS = 30;
```

## PERFORMANCE_CONSIDERATIONS

### Clustering Impact
- **Large Tables**: Clustering changes can be expensive and time-consuming
- **Automatic Reclustering**: Runs in background, consumes compute resources
- **Best Practice**: Use suspend/resume during bulk data operations

### Change Tracking Overhead
- **DML Impact**: Slight overhead on INSERT/UPDATE/DELETE operations
- **Storage Impact**: Additional metadata storage required
- **Query Impact**: Minimal impact on SELECT operations

### Schema Evolution
- **Runtime Impact**: No immediate performance impact
- **Load Impact**: May affect data loading performance with schema changes
- **Storage Impact**: Flexible schema may use more storage

## ROLLBACK_STRATEGY

### Rollback Implementation
```xml
<changeSet id="enable-features" author="developer">
    <alterTable tableName="customer_data"
                snowflake:setEnableSchemaEvolution="true"
                snowflake:setChangeTracking="true"
                snowflake:clusterBy="customer_id,region"/>
    <rollback>
        <alterTable tableName="customer_data"
                    snowflake:setEnableSchemaEvolution="false"
                    snowflake:setChangeTracking="false"
                    snowflake:dropClusteringKey="true"/>
    </rollback>
</changeSet>
```

### Rollback Considerations
1. **Change Tracking**: Can be safely disabled, but history is lost
2. **Schema Evolution**: Can be disabled, but schema changes remain
3. **Clustering**: Dropping clustering key is reversible but affects performance
4. **Data Retention**: Reducing retention may affect Time Travel capabilities

## SUCCESS_CRITERIA

### Functional Requirements
- [x] Clustering key management (add, drop, suspend/resume)
- [x] Schema evolution control for flexible data loading  
- [x] Change tracking enable/disable for data integration
- [x] Data retention time configuration for Time Travel
- [x] Mutual exclusivity validation for conflicting operations
- [x] Compatible with standard Liquibase alterTable operations

### Technical Requirements
- [x] Follows Existing Changetype Extension pattern
- [x] Integrates with SnowflakeNamespaceAttributeStorage
- [x] Higher priority than standard generator
- [x] Multi-statement SQL generation capability
- [x] Comprehensive validation and error handling

### Quality Requirements
- [x] Production-ready performance considerations documented
- [x] Complete test coverage including functional scenarios
- [x] Clear documentation with usage examples
- [x] Backward compatibility maintained
- [x] Rollback strategies defined

## DOCUMENTATION_REQUIREMENTS

### User Documentation
**Location**: Snowflake extension documentation

#### Topics to Cover
1. **Clustering Strategy**
   - When to use clustering keys
   - Performance impact and best practices
   - Suspend/resume during bulk operations

2. **Schema Evolution**
   - Semi-structured data scenarios
   - Impact on data loading pipelines
   - Best practices for evolving schemas

3. **Change Tracking**
   - Integration with Snowflake STREAM objects
   - Performance considerations
   - Use cases for incremental processing

4. **Data Retention**
   - Time Travel implications
   - Storage cost considerations
   - Compliance and governance aspects

#### Performance Guidelines
```
📊 PERFORMANCE TIP: Clustering
Suspend automatic reclustering during bulk data operations to improve 
load performance, then resume clustering when operations complete.
```

### Developer Documentation
- Multi-statement generation architecture
- Validation rule implementation
- Test harness integration patterns
- Extension point documentation

---
**Implementation Priority**: HIGH
**Risk Level**: LOW (non-destructive operations, reversible changes)
**Dependencies**: SnowflakeNamespaceAttributeStorage, SnowflakeNamespaceAwareXMLParser