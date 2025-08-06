# Drop File Format Requirements

## METADATA
```yaml
OBJECT_TYPE: "FileFormat"
CHANGETYPE_PATTERN: "NEW_CHANGETYPE"
IMPLEMENTATION_TIME: "2-3 hours"
SNOWFLAKE_DOCS: "https://docs.snowflake.com/en/sql-reference/sql/drop-file-format"
VALIDATION_SOURCE: "Manual Documentation Review"
LAST_UPDATED: "2025-08-03"
STATUS: "READY_FOR_IMPLEMENTATION"
ATTRIBUTES_COUNT: 2
DEPENDS_ON: "createFileFormat_requirements.md"
```

## QUICK_ACCESS

### Business Case
DROP FILE FORMAT removes file format objects from the Snowflake schema. This is essential for cleanup operations and lifecycle management of data loading configurations.

### Pattern Decision
**NEW CHANGETYPE** - Part of File Format lifecycle management, requires dedicated DROP operation.

### Implementation Components
- `DropFileFormatChange.java`
- `DropFileFormatStatement.java`
- `DropFileFormatGeneratorSnowflake.java`
- XSD element: `<dropFileFormat>`

## OFFICIAL_SYNTAX

### Snowflake Documentation
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/drop-file-format
- **Version**: Current (2025)

### Complete SQL Syntax
```sql
DROP FILE FORMAT [ IF EXISTS ] <name>
```

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Properties

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| fileFormatName | Name of file format to drop | String | N/A | Valid identifier | Yes | Must be valid identifier |
| catalogName | Catalog (database) name | String | Current database | Valid database name | No | File format must exist in this catalog |
| schemaName | Schema name | String | Current schema | Valid schema name | No | File format must exist in this schema |
| ifExists | Use IF EXISTS clause | Boolean | false | true/false | No | Prevents error if file format doesn't exist |

## MUTUAL_EXCLUSIVITY_ANALYSIS

### No Mutual Exclusivity
DROP FILE FORMAT is a simple operation with no mutually exclusive options. All attributes can be used together.

### Behavioral Notes
- `ifExists` prevents errors when file format doesn't exist
- Without `ifExists`, dropping non-existent file format raises error
- Dropping file format doesn't affect existing data loaded with that format
- External tables using the file format may become invalid

## SQL_EXAMPLES

### Example 1: Simple Drop
```sql
DROP FILE FORMAT my_csv_format;
```

### Example 2: Drop with IF EXISTS
```sql
DROP FILE FORMAT IF EXISTS my_json_format;
```

### Example 3: Drop from Specific Schema
```sql
DROP FILE FORMAT my_database.my_schema.my_parquet_format;
```

### Example 4: Conditional Drop (Safe)
```sql
DROP FILE FORMAT IF EXISTS my_temporary_format;
```

### Example 5: Multiple Drops (Separate Statements)
```sql
DROP FILE FORMAT IF EXISTS format_csv;
DROP FILE FORMAT IF EXISTS format_json;
DROP FILE FORMAT IF EXISTS format_parquet;
```

## VALIDATION_RULES

### Required Field Validation
- `fileFormatName` must be provided and non-empty
- `fileFormatName` must be a valid Snowflake identifier

### Existence Validation
- Without `ifExists`: file format must exist or error occurs
- With `ifExists`: operation succeeds regardless of existence
- Catalog and schema (if specified) must exist

### Dependency Validation
- **Warning**: Dropping file format may affect external tables
- **Warning**: May affect COPY INTO operations that reference the format
- No automatic CASCADE behavior - dependencies must be handled manually

## IMPLEMENTATION_NOTES

### SQL Generation Strategy
- Simple SQL generation: `DROP FILE FORMAT [IF EXISTS] <name>`
- Handle schema qualification appropriately
- No complex logic required compared to CREATE/ALTER

### Dependency Considerations
- Consider implementing dependency checking warnings
- May want to validate if file format is referenced by:
  - External tables
  - Active COPY INTO operations
  - Other database objects

### Testing Requirements
- **Unit tests**: SQL string generation validation
- **Integration tests**: Actual DROP operations against Snowflake
- **Error conditions**: Test dropping non-existent formats
- **IF EXISTS**: Test behavior with and without flag

### Rollback Strategy
- DROP FILE FORMAT is not automatically reversible
- Consider capturing file format definition before drop for rollback
- May need to implement rollback through CREATE FILE FORMAT with saved definition

### Service Registration
Same pattern as CREATE/ALTER FILE FORMAT with Drop prefix for classes.

## USAGE_PATTERNS

### Cleanup Operations
```xml
<changeSet id="cleanup-old-formats" author="dba">
    <ext:dropFileFormat fileFormatName="deprecated_csv_format" ifExists="true"/>
    <ext:dropFileFormat fileFormatName="old_json_format" ifExists="true"/>
</changeSet>
```

### Schema Migration
```xml
<changeSet id="migrate-file-formats" author="developer">
    <!-- Drop old format -->
    <ext:dropFileFormat fileFormatName="legacy_format" ifExists="true"/>
    
    <!-- Create new format with updated settings -->
    <ext:createFileFormat fileFormatName="new_format" 
                         fileFormatType="CSV"
                         fieldDelimiter="|"
                         skipHeader="1"/>
</changeSet>
```

### Conditional Cleanup
```xml
<changeSet id="conditional-cleanup" author="dba">
    <preConditions onFail="MARK_RAN">
        <sqlCheck expectedResult="1">
            SELECT COUNT(*) FROM INFORMATION_SCHEMA.FILE_FORMATS 
            WHERE FILE_FORMAT_NAME = 'TEMP_FORMAT'
        </sqlCheck>
    </preConditions>
    
    <ext:dropFileFormat fileFormatName="temp_format"/>
</changeSet>
```

## CROSS_DEPENDENCIES

### Affects External Tables
File formats are commonly referenced by external tables. Dropping a file format may invalidate external table definitions.

### Affects COPY Commands
COPY INTO operations may reference file formats. Dropping formats may break ETL processes.

### No Automatic CASCADE
Unlike some database objects, Snowflake doesn't provide CASCADE options for file formats. Dependencies must be managed manually.

## PRIORITY
**MEDIUM** - Essential for complete lifecycle management but typically used less frequently than CREATE/ALTER operations.