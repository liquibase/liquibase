# Alter File Format Requirements

## METADATA
```yaml
OBJECT_TYPE: "FileFormat"
CHANGETYPE_PATTERN: "NEW_CHANGETYPE"
IMPLEMENTATION_TIME: "3-4 hours"
SNOWFLAKE_DOCS: "https://docs.snowflake.com/en/sql-reference/sql/alter-file-format"
VALIDATION_SOURCE: "Manual Documentation Review"
LAST_UPDATED: "2025-08-03"
STATUS: "READY_FOR_IMPLEMENTATION"
ATTRIBUTES_COUNT: 12
DEPENDS_ON: "createFileFormat_requirements.md"
```

## QUICK_ACCESS

### Business Case
ALTER FILE FORMAT allows modification of existing file format properties without recreating the object. This is essential for maintaining data loading pipelines while adjusting format specifications.

### Pattern Decision
**NEW CHANGETYPE** - Part of File Format lifecycle management, requires dedicated ALTER operation.

### Implementation Components
- `AlterFileFormatChange.java`
- `AlterFileFormatStatement.java`
- `AlterFileFormatGeneratorSnowflake.java`
- XSD element: `<alterFileFormat>`

## OFFICIAL_SYNTAX

### Snowflake Documentation
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/alter-file-format
- **Version**: Current (2025)

### Complete SQL Syntax
```sql
ALTER FILE FORMAT [ IF EXISTS ] <name> SET
  [ TYPE = { CSV | JSON | AVRO | ORC | PARQUET | XML | CUSTOM } [ formatTypeOptions ] ]
  [ COMMENT = '<string_literal>' ]

-- OR rename the file format
ALTER FILE FORMAT [ IF EXISTS ] <name> RENAME TO <new_name>

-- OR set individual format options (format-type specific)
ALTER FILE FORMAT [ IF EXISTS ] <name> SET <formatOption> = <value> [ , ... ]

-- OR unset specific options
ALTER FILE FORMAT [ IF EXISTS ] <name> UNSET { <formatOption> [ , ... ] | COMMENT }
```

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Properties

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| fileFormatName | Name of file format to alter | String | N/A | Valid identifier | Yes | Must exist |
| catalogName | Catalog (database) name | String | Current database | Valid database name | No | File format must exist in this catalog |
| schemaName | Schema name | String | Current schema | Valid schema name | No | File format must exist in this schema |
| ifExists | Use IF EXISTS clause | Boolean | false | true/false | No | Prevents error if not exists |
| newFileFormatName | New name for rename operation | String | null | Valid identifier | No | For RENAME TO operation |

### Operation Types (Mutually Exclusive)

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| operationType | Type of alter operation | Enum | SET | SET, RENAME, UNSET | No | Determines available options |

### SET Operations - Format Type Change

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| newFileFormatType | New format type | Enum | null | CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM | No | Can change format type |
| newComment | New comment | String | null | Any string | No | SET operation |

### SET Operations - Format Options (inherits from CREATE)
All format-specific options from CREATE FILE FORMAT can be SET:
- CSV: fieldDelimiter, recordDelimiter, skipHeader, etc.
- JSON: stripOuterArray, allowDuplicate, etc.
- PARQUET: binaryAsText, useLogicalType, etc.
- XML: preserveSpace, stripOuterElement, etc.
- Common: compression, dateFormat, timeFormat, etc.

### UNSET Operations

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| unsetComment | Remove comment | Boolean | false | true/false | No | UNSET operation |
| unsetCompression | Reset compression to default | Boolean | false | true/false | No | UNSET operation |
| unsetDateFormat | Reset date format to AUTO | Boolean | false | true/false | No | UNSET operation |
| unsetTimeFormat | Reset time format to AUTO | Boolean | false | true/false | No | UNSET operation |
| unsetTimestampFormat | Reset timestamp format to AUTO | Boolean | false | true/false | No | UNSET operation |
| unsetBinaryFormat | Reset binary format to default | Boolean | false | true/false | No | UNSET operation |
| unsetTrimSpace | Reset trim space to default | Boolean | false | true/false | No | UNSET operation |
| unsetNullIf | Remove NULL conversion strings | Boolean | false | true/false | No | UNSET operation |
| unsetFileExtension | Remove file extension hint | Boolean | false | true/false | No | UNSET operation |

### CSV-Specific UNSET Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| unsetRecordDelimiter | Reset record delimiter | Boolean | false | true/false | No | CSV only |
| unsetFieldDelimiter | Reset field delimiter | Boolean | false | true/false | No | CSV only |
| unsetParseHeader | Reset parse header flag | Boolean | false | true/false | No | CSV only |
| unsetSkipHeader | Reset skip header count | Boolean | false | true/false | No | CSV only |
| unsetEscape | Remove escape character | Boolean | false | true/false | No | CSV only |
| unsetFieldOptionallyEnclosedBy | Remove field enclosure | Boolean | false | true/false | No | CSV only |

### JSON-Specific UNSET Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| unsetStripOuterArray | Reset strip outer array | Boolean | false | true/false | No | JSON only |
| unsetStripNullValues | Reset strip null values | Boolean | false | true/false | No | JSON only |
| unsetAllowDuplicate | Reset allow duplicate | Boolean | false | true/false | No | JSON only |

## MUTUAL_EXCLUSIVITY_ANALYSIS

### Operation Type Exclusivity
- **SET**, **RENAME**, and **UNSET** operations are mutually exclusive
- Cannot perform multiple operation types in single ALTER statement

### SET Operation Rules
- Setting new format type resets all format-specific options to defaults
- Cannot SET and UNSET the same property in one statement
- Format-specific options only valid when setting compatible format type

### UNSET Operation Rules
- Cannot UNSET required properties (like file format name)
- Can UNSET multiple properties in single statement
- UNSETting returns property to its default value

## SQL_EXAMPLES

### Example 1: Change Format Type
```sql
ALTER FILE FORMAT my_format SET TYPE = JSON;
```

### Example 2: Rename File Format
```sql
ALTER FILE FORMAT old_format RENAME TO new_format;
```

### Example 3: Modify CSV Options
```sql
ALTER FILE FORMAT my_csv_format SET
  FIELD_DELIMITER = '|',
  SKIP_HEADER = 2,
  TRIM_SPACE = TRUE,
  COMMENT = 'Updated CSV format';
```

### Example 4: Change Compression and Date Format
```sql
ALTER FILE FORMAT my_format SET
  COMPRESSION = GZIP,
  DATE_FORMAT = 'MM/DD/YYYY',
  TIMESTAMP_FORMAT = 'MM/DD/YYYY HH24:MI:SS';
```

### Example 5: UNSET Multiple Properties
```sql
ALTER FILE FORMAT my_format UNSET
  COMPRESSION,
  DATE_FORMAT,
  COMMENT;
```

### Example 6: IF EXISTS with SET
```sql
ALTER FILE FORMAT IF EXISTS my_format SET
  TYPE = PARQUET,
  COMPRESSION = SNAPPY;
```

### Example 7: Complex JSON Format Update
```sql
ALTER FILE FORMAT my_json_format SET
  STRIP_OUTER_ARRAY = TRUE,
  ALLOW_DUPLICATE = FALSE,
  NULL_IF = ('null', 'NULL', ''),
  COMPRESSION = BROTLI;
```

## VALIDATION_RULES

### Required Field Validation
- `fileFormatName` must be provided and non-empty
- For RENAME operation, `newFileFormatName` must be provided
- Cannot specify both SET and UNSET for same property

### Operation Type Validation
- Must specify exactly one operation type (SET, RENAME, or UNSET)
- RENAME requires `newFileFormatName`
- SET requires at least one property to set
- UNSET requires at least one property to unset

### Format Compatibility Validation
- Format-specific SET options only valid for compatible format types
- Changing format type resets all format-specific options
- Cannot set incompatible compression types for format

### Property Validation
- All SET values must pass same validation as CREATE FILE FORMAT
- UNSET properties must be valid unset-able properties
- Cannot UNSET properties that don't exist or aren't optional

## IMPLEMENTATION_NOTES

### SQL Generation Strategy
- Generate different SQL based on operation type
- Handle multiple SET properties with comma separation
- Handle multiple UNSET properties with comma separation
- Validate format-type compatibility before generating SQL

### Operation Type Handling
```java
public enum AlterFileFormatOperationType {
    SET,      // SET property = value
    RENAME,   // RENAME TO new_name  
    UNSET     // UNSET property
}
```

### Testing Requirements
- **Unit tests**: Test each operation type separately
- **Integration tests**: Verify actual ALTER operations work
- **Property combinations**: Test setting multiple properties
- **Error conditions**: Test invalid combinations and non-existent formats

### Service Registration
Same pattern as CREATE FILE FORMAT with Alter prefix for classes.

## PRIORITY
**HIGH** - Essential for maintaining and updating file format configurations in production environments.