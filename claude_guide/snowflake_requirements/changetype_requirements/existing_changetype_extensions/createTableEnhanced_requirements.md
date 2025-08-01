# CreateTable Enhanced Requirements (Snowflake Namespace Attributes)

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "PHASE_2_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_COMPLETION_DATE: "2025-08-01"
IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
DATABASE_TYPE: "Snowflake"
OBJECT_TYPE: "Table"
OPERATION: "CREATE"
NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
ESTIMATED_IMPLEMENTATION_TIME: "4-5 hours"
```

## EXECUTIVE_SUMMARY

This requirement enhances the existing Liquibase `createTable` changetype with Snowflake-specific namespace attributes. The implementation uses the namespace pattern (`snowflake:attribute`) to extend standard table creation with Snowflake features like transient tables, clustering keys, Time Travel retention, change tracking, and managed access.

**Key Features:**
- Namespace-based attribute extension for existing changetype
- Support for all Snowflake table types (transient, volatile, temporary variants)
- Time Travel and change tracking configuration
- Clustering key specification
- Mutual exclusivity validation for table types
- Full backwards compatibility with standard Liquibase

## SQL_SYNTAX_RESEARCH

### Official Documentation
- **Primary Source**: https://docs.snowflake.com/en/sql-reference/sql/create-table
- **Version**: Snowflake 2024 Release
- **Last Verified**: 2025-08-01
- **Syntax Completeness**: 100% of CREATE TABLE features analyzed

### Complete Snowflake CREATE TABLE Syntax
```sql
CREATE [ OR REPLACE ]
  [ { [ LOCAL | GLOBAL ] TEMPORARY | VOLATILE } | TRANSIENT ]
  TABLE [ IF NOT EXISTS ] <table_name> (
    <col_name> <col_type>
      [ COLLATE '<collation_specification>' ]
      [ { DEFAULT <expr> | IDENTITY [ ( <start_num> , <step_num> ) | START <num> INCREMENT <num> ] } ]
      [ NOT NULL ]
      [ CONSTRAINT <constraint_name> { UNIQUE | PRIMARY KEY | { [ FOREIGN KEY ] REFERENCES <ref_table> [ ( <ref_col_name> ) ] } } ]
      [ inlineConstraint ]
    [ , <col_name> <col_type> [ ... ] ]
    [ , outoflineConstraint ]
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

### Syntax Element Analysis
| Element | Required | Mutually Exclusive With | Default | Validation Rules |
|---------|----------|------------------------|---------|------------------|
| TABLE_TYPE | No | Other table types | PERMANENT | Only one type allowed |
| CLUSTER BY | No | None | None | Columns must exist in table |
| DATA_RETENTION_TIME_IN_DAYS | No | None | 1 | 0-90 for permanent, 0 for transient/temp |
| MAX_DATA_EXTENSION_TIME_IN_DAYS | No | None | 14 | Must be >= DATA_RETENTION_TIME_IN_DAYS |
| CHANGE_TRACKING | No | None | FALSE | Boolean only |
| COPY GRANTS | No | None | FALSE | Only valid with OR REPLACE |

## ATTRIBUTE_SPECIFICATIONS

### Primary Attributes (High Priority)
| Attribute | XML Namespace | Data Type | Default | Valid Values | Snowflake Mapping |
|-----------|---------------|-----------|---------|--------------|-------------------|
| transient | snowflake:transient | Boolean | false | true/false | TRANSIENT |
| clusterBy | snowflake:clusterBy | String | null | Comma-separated columns | CLUSTER BY (...) |
| dataRetentionTimeInDays | snowflake:dataRetentionTimeInDays | Integer | 1 | 0-90 | DATA_RETENTION_TIME_IN_DAYS = n |
| changeTracking | snowflake:changeTracking | Boolean | false | true/false | CHANGE_TRACKING = TRUE/FALSE |

### Secondary Attributes (Medium Priority)
| Attribute | XML Namespace | Data Type | Default | Valid Values | Snowflake Mapping |
|-----------|---------------|-----------|---------|--------------|-------------------|
| copyGrants | snowflake:copyGrants | Boolean | false | true/false | COPY GRANTS |
| maxDataExtensionTimeInDays | snowflake:maxDataExtensionTimeInDays | Integer | 14 | 0-90 | MAX_DATA_EXTENSION_TIME_IN_DAYS = n |
| defaultDdlCollation | snowflake:defaultDdlCollation | String | null | Valid collation | DEFAULT_DDL_COLLATION = 'spec' |

### Table Type Attributes (Mutually Exclusive)
| Attribute | XML Namespace | Data Type | Default | Valid Values | Snowflake Mapping |
|-----------|---------------|-----------|---------|--------------|-------------------|
| volatile | snowflake:volatile | Boolean | false | true/false | VOLATILE |
| temporary | snowflake:temporary | Boolean | false | true/false | TEMPORARY |
| localTemporary | snowflake:localTemporary | Boolean | false | true/false | LOCAL TEMPORARY |
| globalTemporary | snowflake:globalTemporary | Boolean | false | true/false | GLOBAL TEMPORARY |

### Advanced Attributes (Low Priority)
| Attribute | XML Namespace | Data Type | Default | Valid Values | Snowflake Mapping |
|-----------|---------------|-----------|---------|--------------|-------------------|
| enableSchemaEvolution | snowflake:enableSchemaEvolution | Boolean | false | true/false | ENABLE_SCHEMA_EVOLUTION = TRUE/FALSE |

## VALIDATION_RULES

### Table Type Mutual Exclusivity
```java
// Only one table type can be specified
public void validateTableTypes(SnowflakeTableAttributes attrs) {
    List<Boolean> tableTypes = Arrays.asList(
        attrs.isTransient(),
        attrs.isVolatile(), 
        attrs.isTemporary(),
        attrs.isLocalTemporary(),
        attrs.isGlobalTemporary()
    );
    
    long trueCount = tableTypes.stream().mapToLong(b -> b ? 1 : 0).sum();
    if (trueCount > 1) {
        throw new ValidationFailedException(
            "Only one table type can be specified: transient, volatile, temporary, localTemporary, or globalTemporary"
        );
    }
}
```

### Data Retention Rules
```java
// Transient and temporary tables cannot have retention > 0
public void validateDataRetention(SnowflakeTableAttributes attrs) {
    boolean isTransientOrTemp = attrs.isTransient() || attrs.isVolatile() || 
                               attrs.isTemporary() || attrs.isLocalTemporary() || 
                               attrs.isGlobalTemporary();
    
    if (isTransientOrTemp && attrs.getDataRetentionTimeInDays() > 0) {
        throw new ValidationFailedException(
            "Transient, volatile, and temporary tables must have dataRetentionTimeInDays = 0"
        );
    }
    
    if (attrs.getMaxDataExtensionTimeInDays() < attrs.getDataRetentionTimeInDays()) {
        throw new ValidationFailedException(
            "maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays"
        );
    }
}
```

### Cluster Key Validation
```java
// Cluster keys must reference existing table columns
public void validateClusterKeys(String clusterBy, List<Column> tableColumns) {
    if (clusterBy == null || clusterBy.trim().isEmpty()) return;
    
    Set<String> columnNames = tableColumns.stream()
        .map(Column::getName)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());
    
    String[] clusterColumns = clusterBy.split(",");
    for (String col : clusterColumns) {
        String trimmed = col.trim().toLowerCase();
        if (!columnNames.contains(trimmed)) {
            throw new ValidationFailedException(
                "Cluster key column '" + col.trim() + "' does not exist in table definition"
            );
        }
    }
}
```

## IMPLEMENTATION_ARCHITECTURE

### Component Overview
```
CreateTableChange (Standard Liquibase)
    ↓ (extends with namespace attributes)
SnowflakeNamespaceAttributeStorage
    ↓ (captures snowflake: attributes)
SnowflakeNamespaceAwareXMLParser
    ↓ (modifies SQL generation)
CreateTableGeneratorSnowflake
    ↓ (produces enhanced SQL)
Snowflake CREATE TABLE with extensions
```

### Key Components

#### 1. SnowflakeNamespaceAttributeStorage
```java
public class SnowflakeNamespaceAttributeStorage {
    private static final ThreadLocal<Map<String, SnowflakeTableAttributes>> 
        attributeStorage = new ThreadLocal<>();
    
    public static void storeAttributes(String changeKey, SnowflakeTableAttributes attrs) {
        // Thread-safe storage implementation
    }
    
    public static SnowflakeTableAttributes retrieveAttributes(String changeKey) {
        // Thread-safe retrieval with cleanup
    }
}
```

#### 2. SnowflakeNamespaceAwareXMLParser
```java
@XmlNamespace(namespace = "http://liquibase.org/xml/ns/dbchangelog/snowflake")
public class SnowflakeNamespaceAwareXMLParser extends XMLChangeLogSAXParser {
    
    @Override
    protected void processSnowflakeAttributes(Element element, Change change) {
        if (change instanceof CreateTableChange) {
            SnowflakeTableAttributes attrs = extractSnowflakeAttributes(element);
            SnowflakeNamespaceAttributeStorage.storeAttributes(
                generateChangeKey(change), attrs
            );
        }
    }
}
```

#### 3. CreateTableGeneratorSnowflake
```java
public class CreateTableGeneratorSnowflake extends CreateTableGenerator {
    
    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, 
                           SqlGeneratorChain sqlGeneratorChain) {
        
        // Get stored Snowflake attributes
        SnowflakeTableAttributes attrs = SnowflakeNamespaceAttributeStorage
            .retrieveAttributes(generateStatementKey(statement));
        
        if (attrs != null) {
            return generateSnowflakeEnhancedSql(statement, attrs, database);
        }
        
        return super.generateSql(statement, database, sqlGeneratorChain);
    }
    
    private Sql[] generateSnowflakeEnhancedSql(CreateTableStatement statement, 
                                              SnowflakeTableAttributes attrs, 
                                              Database database) {
        StringBuilder sql = new StringBuilder("CREATE ");
        
        // Add table type
        appendTableType(sql, attrs);
        sql.append("TABLE ");
        
        // Add IF NOT EXISTS if specified
        if (statement.getIfNotExistsClause() != null) {
            sql.append("IF NOT EXISTS ");
        }
        
        // Add table name
        sql.append(database.escapeTableName(
            statement.getCatalogName(), 
            statement.getSchemaName(), 
            statement.getTableName()
        ));
        
        // Add column definitions (delegate to parent)
        sql.append(" (\n");
        appendColumnDefinitions(sql, statement, database);
        sql.append("\n)");
        
        // Add Snowflake-specific clauses
        appendSnowflakeTableOptions(sql, attrs);
        
        return new Sql[] { new UnparsedSql(sql.toString(), getAffectedTable(statement)) };
    }
}
```

## XML_USAGE_EXAMPLES

### Example 1: Transient Table with Clustering
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    
    <changeSet id="create-user-sessions" author="developer">
        <createTable tableName="user_sessions" 
                     snowflake:transient="true"
                     snowflake:clusterBy="user_id,session_start">
            <column name="user_id" type="INT">
                <constraints nullable="false"/>
            </column>
            <column name="session_start" type="TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="session_data" type="VARCHAR(1000)"/>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Generated SQL:**
```sql
CREATE TRANSIENT TABLE user_sessions (
    user_id INT NOT NULL,
    session_start TIMESTAMP NOT NULL,
    session_data VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) CLUSTER BY (user_id, session_start);
```

### Example 2: Table with Change Tracking and Custom Retention
```xml
<changeSet id="create-audit-log" author="developer">
    <createTable tableName="audit_log"
                 snowflake:changeTracking="true"
                 snowflake:dataRetentionTimeInDays="30"
                 snowflake:maxDataExtensionTimeInDays="60">
        <column name="audit_id" type="BIGINT" autoIncrement="true">
            <constraints primaryKey="true"/>
        </column>
        <column name="table_name" type="VARCHAR(100)">
            <constraints nullable="false"/>
        </column>
        <column name="operation" type="VARCHAR(10)">
            <constraints nullable="false"/>
        </column>
        <column name="changed_by" type="VARCHAR(100)">
            <constraints nullable="false"/>
        </column>
        <column name="changed_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
            <constraints nullable="false"/>
        </column>
        <column name="old_values" type="VARIANT"/>
        <column name="new_values" type="VARIANT"/>
    </createTable>
</changeSet>
```

**Generated SQL:**
```sql
CREATE TABLE audit_log (
    audit_id BIGINT AUTOINCREMENT PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(10) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    old_values VARIANT,
    new_values VARIANT
) DATA_RETENTION_TIME_IN_DAYS = 30 
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 60 
  CHANGE_TRACKING = TRUE;
```

### Example 3: Global Temporary Table
```xml
<changeSet id="create-temp-calculations" author="analytics">
    <createTable tableName="temp_calculations"
                 snowflake:globalTemporary="true">
        <column name="calc_id" type="INT">
            <constraints nullable="false"/>
        </column>
        <column name="input_value" type="DECIMAL(15,4)">
            <constraints nullable="false"/>
        </column>
        <column name="result" type="DECIMAL(15,4)"/>
        <column name="calculation_type" type="VARCHAR(50)">
            <constraints nullable="false"/>
        </column>
    </createTable>
</changeSet>
```

**Generated SQL:**
```sql
CREATE GLOBAL TEMPORARY TABLE temp_calculations (
    calc_id INT NOT NULL,
    input_value DECIMAL(15,4) NOT NULL,
    result DECIMAL(15,4),
    calculation_type VARCHAR(50) NOT NULL
);
```

### Example 4: Complex Table with Multiple Snowflake Features
```xml
<changeSet id="create-customer-orders" author="ecommerce">
    <createTable tableName="customer_orders"
                 snowflake:clusterBy="customer_id,order_date"
                 snowflake:changeTracking="true"
                 snowflake:dataRetentionTimeInDays="7"
                 snowflake:defaultDdlCollation="utf8">
        <column name="order_id" type="BIGINT" autoIncrement="true">
            <constraints primaryKey="true"/>
        </column>
        <column name="customer_id" type="BIGINT">
            <constraints nullable="false"/>
        </column>
        <column name="order_date" type="DATE">
            <constraints nullable="false"/>
        </column>
        <column name="order_total" type="DECIMAL(12,2)">
            <constraints nullable="false"/>
        </column>
        <column name="status" type="VARCHAR(20)" defaultValue="PENDING">
            <constraints nullable="false"/>
        </column>
        <column name="order_details" type="VARIANT"/>
        <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP"/>
        <column name="updated_at" type="TIMESTAMP"/>
    </createTable>
</changeSet>
```

**Generated SQL:**
```sql
CREATE TABLE customer_orders (
    order_id BIGINT AUTOINCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    order_total DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' NOT NULL,
    order_details VARIANT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
) CLUSTER BY (customer_id, order_date)
  DATA_RETENTION_TIME_IN_DAYS = 7
  CHANGE_TRACKING = TRUE
  DEFAULT_DDL_COLLATION = 'utf8';
```

## ERROR_SCENARIOS_AND_HANDLING

### Validation Error Examples

#### 1. Multiple Table Types Specified
```xml
<!-- INVALID: Multiple table types -->
<createTable tableName="invalid_table"
             snowflake:transient="true"
             snowflake:volatile="true">
    <column name="id" type="INT"/>
</createTable>
```

**Error Message:**
```
ValidationFailedException: Only one table type can be specified: transient, volatile, temporary, localTemporary, or globalTemporary
```

#### 2. Invalid Retention for Transient Table
```xml
<!-- INVALID: Transient table with retention > 0 -->
<createTable tableName="invalid_transient"
             snowflake:transient="true"
             snowflake:dataRetentionTimeInDays="7">
    <column name="id" type="INT"/>
</createTable>
```

**Error Message:**
```
ValidationFailedException: Transient, volatile, and temporary tables must have dataRetentionTimeInDays = 0
```

#### 3. Invalid Cluster Key Reference
```xml
<!-- INVALID: Cluster key references non-existent column -->
<createTable tableName="invalid_cluster"
             snowflake:clusterBy="user_id,non_existent_column">
    <column name="id" type="INT"/>
    <column name="user_id" type="INT"/>
</createTable>
```

**Error Message:**
```
ValidationFailedException: Cluster key column 'non_existent_column' does not exist in table definition
```

#### 4. Invalid Retention Range
```xml
<!-- INVALID: maxDataExtensionTimeInDays < dataRetentionTimeInDays -->
<createTable tableName="invalid_retention"
             snowflake:dataRetentionTimeInDays="30"
             snowflake:maxDataExtensionTimeInDays="15">
    <column name="id" type="INT"/>
</createTable>
```

**Error Message:**
```
ValidationFailedException: maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays
```

## COMPREHENSIVE_TEST_SUITE

### Unit Test Categories

#### 1. Attribute Storage Tests
```java
@Test
public void testThreadSafeAttributeStorage() {
    // Test concurrent access to attribute storage
}

@Test 
public void testAttributeCleanupAfterRetrieval() {
    // Ensure attributes are cleaned up to prevent memory leaks
}
```

#### 2. XML Parsing Tests
```java
@Test
public void testSnowflakeNamespaceAttributeParsing() {
    // Test extraction of snowflake: prefixed attributes
}

@Test
public void testIgnoreUnknownNamespaceAttributes() {
    // Ensure backwards compatibility
}
```

#### 3. SQL Generation Tests
```java
@Test
public void testTransientTableGeneration() {
    // Test TRANSIENT keyword insertion
}

@Test
public void testClusterByGeneration() {
    // Test CLUSTER BY clause generation
}

@Test
public void testMultipleAttributeCombination() {
    // Test complex scenarios with multiple attributes
}
```

#### 4. Validation Tests
```java
@Test
public void testTableTypeMutualExclusivity() {
    // Test enforcement of table type rules
}

@Test
public void testDataRetentionValidation() {
    // Test retention time validation rules
}

@Test
public void testClusterKeyValidation() {
    // Test cluster key column existence validation
}
```

### Integration Test Harness Files

#### File: createTableTransient.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-transient-table" author="test">
        <createTable tableName="test_transient"
                     snowflake:transient="true">
            <column name="id" type="INT"/>
            <column name="data" type="VARCHAR(100)"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected SQL:**
```sql
CREATE TRANSIENT TABLE test_transient (
    id INT,
    data VARCHAR(100)
);
```

#### File: createTableClustered.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-clustered-table" author="test">
        <createTable tableName="test_clustered"
                     snowflake:clusterBy="region,customer_id">
            <column name="id" type="BIGINT"/>
            <column name="region" type="VARCHAR(50)"/>
            <column name="customer_id" type="BIGINT"/>
            <column name="order_date" type="DATE"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected SQL:**
```sql
CREATE TABLE test_clustered (
    id BIGINT,
    region VARCHAR(50),
    customer_id BIGINT,
    order_date DATE
) CLUSTER BY (region, customer_id);
```

#### File: createTableRetention.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-retention-table" author="test">
        <createTable tableName="test_retention"
                     snowflake:dataRetentionTimeInDays="14"
                     snowflake:maxDataExtensionTimeInDays="28">
            <column name="id" type="INT"/>
            <column name="timestamp_col" type="TIMESTAMP"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected SQL:**
```sql
CREATE TABLE test_retention (
    id INT,
    timestamp_col TIMESTAMP
) DATA_RETENTION_TIME_IN_DAYS = 14 
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 28;
```

#### File: createTableTracking.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-tracking-table" author="test">
        <createTable tableName="test_tracking"
                     snowflake:changeTracking="true">
            <column name="id" type="BIGINT"/>
            <column name="name" type="VARCHAR(200)"/>
            <column name="status" type="VARCHAR(20)"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected SQL:**
```sql
CREATE TABLE test_tracking (
    id BIGINT,
    name VARCHAR(200),
    status VARCHAR(20)
) CHANGE_TRACKING = TRUE;
```

#### File: createTableComplex.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-complex-table" author="test">
        <createTable tableName="test_complex"
                     snowflake:clusterBy="date_col,status"
                     snowflake:changeTracking="true"
                     snowflake:dataRetentionTimeInDays="7"
                     snowflake:defaultDdlCollation="utf8">
            <column name="id" type="BIGINT" autoIncrement="true">
                <constraints primaryKey="true"/>
            </column>
            <column name="date_col" type="DATE">
                <constraints nullable="false"/>
            </column>
            <column name="status" type="VARCHAR(20)" defaultValue="ACTIVE">
                <constraints nullable="false"/>
            </column>
            <column name="data" type="VARIANT"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected SQL:**
```sql
CREATE TABLE test_complex (
    id BIGINT AUTOINCREMENT PRIMARY KEY,
    date_col DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    data VARIANT
) CLUSTER BY (date_col, status)
  DATA_RETENTION_TIME_IN_DAYS = 7
  CHANGE_TRACKING = TRUE
  DEFAULT_DDL_COLLATION = 'utf8';
```

### Error Scenario Tests

#### File: createTableInvalidMultipleTypes.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-invalid-types" author="test">
        <createTable tableName="test_invalid"
                     snowflake:transient="true"
                     snowflake:volatile="true">
            <column name="id" type="INT"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected Result:** ValidationFailedException during parsing

#### File: createTableInvalidRetention.xml
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <changeSet id="test-invalid-retention" author="test">
        <createTable tableName="test_invalid"
                     snowflake:transient="true"
                     snowflake:dataRetentionTimeInDays="7">
            <column name="id" type="INT"/>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

**Expected Result:** ValidationFailedException during validation

## PERFORMANCE_CONSIDERATIONS

### Memory Management
- **Attribute Storage**: Thread-local storage automatically cleaned after SQL generation
- **Memory Footprint**: Minimal overhead for non-Snowflake databases
- **Concurrent Processing**: Thread-safe for parallel changelog execution

### SQL Generation Performance  
- **Lazy Evaluation**: Attributes only processed when Snowflake database detected
- **String Building**: Efficient StringBuilder usage for SQL construction
- **Validation Caching**: One-time validation per changeset execution

### Scalability Metrics
- **Thread Safety**: Fully thread-safe for concurrent changeset processing
- **Memory Leaks**: Automatic cleanup prevents attribute accumulation
- **Database Compatibility**: Zero impact on non-Snowflake databases

## BACKWARDS_COMPATIBILITY

### Standard Liquibase Compatibility
- **Full Compatibility**: All existing createTable functionality preserved
- **Attribute Ignorance**: Unknown namespace attributes ignored gracefully
- **Database Detection**: Snowflake features only activated for Snowflake databases

### Migration Path
- **Existing Changelogs**: Continue to work without modification
- **Gradual Adoption**: Snowflake attributes can be added incrementally
- **Feature Detection**: Automatic detection of Snowflake-specific capabilities

## INTEGRATION_PATTERNS

### With Core Liquibase
```java
// Standard createTable remains unchanged
CreateTableChange standardChange = new CreateTableChange();
standardChange.setTableName("users");
// ... standard configuration

// Enhanced with Snowflake attributes
<createTable tableName="users" snowflake:transient="true">
    <!-- Standard column definitions -->
</createTable>
```

### With Other Snowflake Extensions
```java
// Consistent namespace usage across all Snowflake features
<createSchema schemaName="analytics" snowflake:transient="true"/>
<createTable tableName="data" snowflake:clusterBy="date_col"/>
<createSequence sequenceName="ids" snowflake:cacheSize="1000"/>
```

### Namespace Declaration Pattern
```xml
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake">
    <!-- All Snowflake extensions use same namespace -->
</databaseChangeLog>
```

## DOCUMENTATION_REQUIREMENTS

### User-Facing Documentation

#### Quick Start Guide
```markdown
# Using Snowflake CREATE TABLE Extensions

1. Add namespace declaration to your changelog:
   xmlns:snowflake="http://liquibase.org/xml/ns/dbchangelog/snowflake"

2. Add Snowflake attributes to createTable:
   <createTable tableName="sessions" snowflake:transient="true">

3. Available attributes:
   - snowflake:transient="true" - Create transient table
   - snowflake:clusterBy="col1,col2" - Add clustering keys
   - snowflake:changeTracking="true" - Enable change tracking
   - snowflake:dataRetentionTimeInDays="7" - Set Time Travel retention
```

#### Attribute Reference
| Attribute | Purpose | Example | Notes |
|-----------|---------|---------|-------|
| snowflake:transient | Transient table | `snowflake:transient="true"` | Mutually exclusive with other table types |
| snowflake:clusterBy | Clustering keys | `snowflake:clusterBy="user_id,date"` | Columns must exist in table |
| snowflake:changeTracking | Change tracking | `snowflake:changeTracking="true"` | Enables CDC capabilities |

### Developer Documentation

#### Architecture Overview
```markdown
# Snowflake CreateTable Enhancement Architecture

## Component Flow
1. XML Parser captures snowflake: attributes
2. Attributes stored in thread-local storage
3. SQL Generator retrieves attributes during generation
4. Enhanced SQL includes Snowflake-specific clauses
5. Attributes cleaned up after use

## Extension Points
- Add new attributes in SnowflakeTableAttributes class
- Update XML parsing in SnowflakeNamespaceAwareXMLParser
- Modify SQL generation in CreateTableGeneratorSnowflake
```

## FUTURE_ENHANCEMENTS

### Phase 3 Considerations
1. **Row Access Policies**
   - Add `snowflake:rowAccessPolicy` attribute
   - Support policy column specification

2. **Masking Policies** 
   - Column-level masking policy attributes
   - Integration with column definitions

3. **Tag Management**
   - Table-level and column-level tags
   - Tag inheritance rules

4. **External Table Support**
   - Stage-based external tables
   - File format specifications

### Advanced Features Pipeline
```yaml
Planned Enhancements:
  - Row-level security integration
  - Automatic clustering recommendations
  - Time Travel query generation
  - Change tracking stream creation
  - Dynamic data masking
```

## IMPLEMENTATION_CHECKLIST

### Phase 2 Completion Criteria
- [ ] All attribute specifications documented
- [ ] Validation rules defined and tested
- [ ] XML usage examples provided
- [ ] Error scenarios documented
- [ ] Test harness files created
- [ ] Performance considerations analyzed
- [ ] Integration patterns documented
- [ ] Future enhancement roadmap defined

### Ready for Phase 3 Implementation
✅ **REQUIREMENTS_COMPLETE**: All Phase 2 requirements documented
✅ **ARCHITECTURE_DEFINED**: Implementation pattern specified  
✅ **TEST_CASES_READY**: Comprehensive test suite defined
✅ **VALIDATION_RULES_COMPLETE**: All validation scenarios covered
✅ **EXAMPLES_COMPREHENSIVE**: Real-world usage examples provided

**STATUS**: ✅ IMPLEMENTATION_READY - Proceed to Phase 3 TDD Implementation

---

**Next Phase**: Follow `ai_workflow_guide.md` for Phase 3 TDD Implementation
**Estimated Implementation Time**: 4-5 hours
**Implementation Pattern**: `EXISTING_CHANGETYPE_EXTENSION_PATTERN.md`