# Snowflake Features Implemented

This document provides a comprehensive overview of all Snowflake features implemented in the Liquibase extension.

## Object Types and Change Operations

### 1. WAREHOUSE Operations

#### CreateWarehouseChange
**Purpose**: Creates a new Snowflake warehouse

**Properties**:
- `warehouseName` (required) - Name of the warehouse
- `warehouseSize` - Size (XSMALL, SMALL, MEDIUM, LARGE, XLARGE, etc.)
- `warehouseType` - STANDARD or SNOWPARK-OPTIMIZED
- `maxClusterCount` - Maximum clusters for multi-cluster warehouses
- `minClusterCount` - Minimum clusters for multi-cluster warehouses
- `scalingPolicy` - STANDARD or ECONOMY
- `autoSuspend` - Auto-suspend time in seconds
- `autoResume` - Enable auto-resume (true/false)
- `initialSuspend` - Create in suspended state
- `comment` - Warehouse comment
- `resourceMonitor` - Resource monitor name
- `enableQueryAcceleration` - Enable query acceleration
- `queryAccelerationMaxScaleFactor` - Max scale factor for query acceleration

**Example**:
```xml
<snowflake:createWarehouse 
    warehouseName="MY_WAREHOUSE"
    warehouseSize="MEDIUM"
    warehouseType="STANDARD"
    maxClusterCount="3"
    minClusterCount="1"
    scalingPolicy="STANDARD"
    autoSuspend="300"
    autoResume="true"
    comment="Analytics warehouse"
    resourceMonitor="MY_MONITOR"
    enableQueryAcceleration="true"
    queryAccelerationMaxScaleFactor="8"/>
```

#### AlterWarehouseChange
**Purpose**: Modifies an existing warehouse

**Properties**: Same as CreateWarehouse plus:
- `newWarehouseName` - For renaming
- `suspend` - Suspend the warehouse
- `resume` - Resume the warehouse
- `abortAllQueries` - Abort all running queries

**Example**:
```xml
<snowflake:alterWarehouse 
    warehouseName="MY_WAREHOUSE"
    warehouseSize="LARGE"
    autoSuspend="600"/>
```

#### DropWarehouseChange
**Purpose**: Drops a warehouse

**Properties**:
- `warehouseName` (required) - Name to drop
- `ifExists` - Use IF EXISTS clause

**Example**:
```xml
<snowflake:dropWarehouse 
    warehouseName="MY_WAREHOUSE"
    ifExists="true"/>
```

### 2. DATABASE Operations

#### CreateDatabaseChange
**Purpose**: Creates a new Snowflake database

**Properties**:
- `databaseName` (required) - Database name
- `cloneDatabase` - Source database to clone from
- `comment` - Database comment
- `dataRetentionTimeInDays` - Time Travel retention (0-90)
- `transient` - Create as transient database

**Example**:
```xml
<snowflake:createDatabase 
    databaseName="MY_DATABASE"
    dataRetentionTimeInDays="7"
    comment="Production database"/>
```

#### AlterDatabaseChange
**Purpose**: Modifies database properties

**Properties**:
- `databaseName` (required) - Database to alter
- `newDatabaseName` - For renaming
- `dataRetentionTimeInDays` - Update retention
- `comment` - Update comment
- `enableReplication` - Enable replication to accounts
- `disableReplication` - Disable replication from accounts

**Example**:
```xml
<snowflake:alterDatabase 
    databaseName="MY_DATABASE"
    dataRetentionTimeInDays="14"
    enableReplication="ACCOUNT1,ACCOUNT2"/>
```

#### DropDatabaseChange
**Purpose**: Drops a database

**Properties**:
- `databaseName` (required) - Database to drop
- `ifExists` - Use IF EXISTS
- `cascade` - Drop with CASCADE
- `restrict` - Drop with RESTRICT

**Example**:
```xml
<snowflake:dropDatabase 
    databaseName="MY_DATABASE"
    ifExists="true"
    cascade="true"/>
```

### 3. SCHEMA Operations

#### CreateSchemaChange
**Purpose**: Creates a new schema

**Properties**:
- `schemaName` (required) - Schema name
- `databaseName` - Target database
- `cloneSchema` - Source schema to clone
- `comment` - Schema comment
- `dataRetentionTimeInDays` - Time Travel retention
- `transient` - Create as transient
- `managedAccess` - Enable managed access

**Example**:
```xml
<snowflake:createSchema 
    schemaName="MY_SCHEMA"
    databaseName="MY_DATABASE"
    managedAccess="true"
    comment="Application schema"/>
```

#### AlterSchemaChange
**Purpose**: Modifies schema properties

**Properties**:
- `schemaName` (required) - Schema to alter
- `databaseName` - Database containing schema
- `newSchemaName` - For renaming
- `swapWithSchema` - Swap with another schema
- `dataRetentionTimeInDays` - Update retention
- `comment` - Update comment
- `enableManagedAccess` - Enable managed access
- `disableManagedAccess` - Disable managed access

**Example**:
```xml
<snowflake:alterSchema 
    schemaName="MY_SCHEMA"
    databaseName="MY_DATABASE"
    dataRetentionTimeInDays="30"/>
```

#### DropSchemaChange
**Purpose**: Drops a schema

**Properties**:
- `schemaName` (required) - Schema to drop
- `databaseName` - Database containing schema
- `ifExists` - Use IF EXISTS
- `cascade` - Drop with CASCADE
- `restrict` - Drop with RESTRICT

**Example**:
```xml
<snowflake:dropSchema 
    schemaName="MY_SCHEMA"
    databaseName="MY_DATABASE"
    ifExists="true"
    cascade="true"/>
```

### 4. TABLE Enhancements

#### CreateTableSnowflakeChange
**Purpose**: Enhanced table creation with Snowflake features

**Properties**: Extends standard CreateTable plus:
- `clusteringKeys` - Comma-separated clustering columns
- `dataRetentionTimeInDays` - Time Travel retention
- `transient` - Create as transient table

**Implementation Note**: Uses remarks field to store clustering keys

**Example**:
```xml
<snowflake:createTableEnhanced tableName="MY_TABLE">
    <column name="id" type="NUMBER">
        <constraints primaryKey="true"/>
    </column>
    <column name="created_date" type="DATE"/>
    <column name="region" type="VARCHAR(50)"/>
</snowflake:createTableEnhanced>
<!-- Then set clustering -->
<setTableRemarks tableName="MY_TABLE" 
    remarks="CLUSTER BY (created_date, region)"/>
```

### 5. SEQUENCE Enhancements

#### Enhanced CreateSequenceGenerator
**Purpose**: Adds ORDER/NOORDER support for Snowflake sequences

**Additional Properties**:
- `ordered` - Create as ORDERED sequence (default: false/NOORDER)

**Example**:
```xml
<createSequence 
    sequenceName="MY_SEQUENCE"
    startValue="1"
    incrementBy="1"
    ordered="true"/>
```

## Supporting Infrastructure

### Snapshot Generators

#### WarehouseSnapshotGeneratorSnowflake
- Captures warehouse information
- Stores as schema attributes
- Lists all warehouses in database

### SQL Generators

Each change type has a corresponding Snowflake-specific SQL generator:
- `CreateWarehouseGeneratorSnowflake`
- `AlterWarehouseGeneratorSnowflake`
- `DropWarehouseGeneratorSnowflake`
- `CreateDatabaseGeneratorSnowflake`
- `AlterDatabaseGeneratorSnowflake`
- `DropDatabaseGeneratorSnowflake`
- `CreateSchemaGeneratorSnowflake`
- `AlterSchemaGeneratorSnowflake`
- `DropSchemaGeneratorSnowflake`
- `CreateSequenceGeneratorSnowflake` (enhanced)

### XML Namespace Support

**Namespace**: `http://www.liquibase.org/xml/ns/snowflake`
**Handler**: `SnowflakeNamespaceDetails`
**XSD**: `liquibase-snowflake-latest.xsd`

## Usage Examples

### Complete Warehouse Lifecycle
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake">

    <!-- Create warehouse -->
    <changeSet id="1" author="admin">
        <snowflake:createWarehouse 
            warehouseName="ETL_WAREHOUSE"
            warehouseSize="LARGE"
            autoSuspend="60"
            autoResume="true"/>
    </changeSet>

    <!-- Scale up for heavy processing -->
    <changeSet id="2" author="admin">
        <snowflake:alterWarehouse 
            warehouseName="ETL_WAREHOUSE"
            warehouseSize="XLARGE"
            maxClusterCount="4"/>
    </changeSet>

    <!-- Scale down after processing -->
    <changeSet id="3" author="admin">
        <snowflake:alterWarehouse 
            warehouseName="ETL_WAREHOUSE"
            warehouseSize="MEDIUM"
            maxClusterCount="1"/>
    </changeSet>
</databaseChangeLog>
```

### Database and Schema Setup
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake">

    <!-- Create database -->
    <changeSet id="1" author="admin">
        <snowflake:createDatabase 
            databaseName="ANALYTICS"
            dataRetentionTimeInDays="30"
            comment="Analytics database"/>
    </changeSet>

    <!-- Create schemas -->
    <changeSet id="2" author="admin">
        <snowflake:createSchema 
            schemaName="RAW"
            databaseName="ANALYTICS"
            comment="Raw data ingestion"/>
    </changeSet>

    <changeSet id="3" author="admin">
        <snowflake:createSchema 
            schemaName="STAGING"
            databaseName="ANALYTICS"
            managedAccess="true"
            comment="Data staging area"/>
    </changeSet>
</databaseChangeLog>
```

## Testing Coverage

All implemented features have:
1. Unit tests for change validation
2. Unit tests for SQL generation
3. Integration tests with real Snowflake database
4. Rollback testing where applicable
5. Test changelogs demonstrating usage

## Known Limitations

1. **Warehouse as DatabaseObject**: Warehouses are not true DatabaseObjects, stored as schema attributes
2. **Limited Diff Support**: No automatic detection of Snowflake-specific object changes
3. **Encoding Workarounds**: Some features use creative field mappings (e.g., tablespace for transient)
4. **No Custom Types**: File formats, stages, pipes not yet implemented

## Future Enhancement Opportunities

1. Implement remaining Snowflake objects (stages, pipes, file formats)
2. Add proper DatabaseObject support for warehouses
3. Enhance diff/compare functionality
4. Add support for tags and dynamic tables
5. Implement masking policies and row access policies
6. Add support for external functions
7. Enhance snapshot capabilities for all object types