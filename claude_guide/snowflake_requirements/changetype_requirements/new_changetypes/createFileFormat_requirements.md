# Create File Format Requirements

## METADATA
```yaml
OBJECT_TYPE: "FileFormat"
CHANGETYPE_PATTERN: "NEW_CHANGETYPE"
IMPLEMENTATION_TIME: "6-8 hours"
SNOWFLAKE_DOCS: "https://docs.snowflake.com/en/sql-reference/sql/create-file-format"
VALIDATION_SOURCE: "INFORMATION_SCHEMA.FILE_FORMATS + Manual Documentation Review"
LAST_UPDATED: "2025-08-03"
STATUS: "READY_FOR_IMPLEMENTATION"
ATTRIBUTES_COUNT: 17
```

## QUICK_ACCESS

### Business Case
File Formats define how data files are structured and formatted in Snowflake. They are essential for loading data from external files using COPY INTO commands and creating external tables.

### Pattern Decision
**NEW CHANGETYPE** - File Format objects do not exist in Liquibase core and require complete lifecycle management (CREATE/ALTER/DROP).

### Implementation Components
- `CreateFileFormatChange.java`
- `CreateFileFormatStatement.java` 
- `CreateFileFormatGeneratorSnowflake.java`
- XSD element: `<createFileFormat>`
- Service registrations
- Database object model

## OFFICIAL_SYNTAX

### Snowflake Documentation
- **URL**: https://docs.snowflake.com/en/sql-reference/sql/create-file-format
- **Version**: Current (2025)

### Complete SQL Syntax
```sql
CREATE [ OR REPLACE ] [ { TEMP | TEMPORARY | VOLATILE } ] FILE FORMAT [ IF NOT EXISTS ] <name>
  [ TYPE = { CSV | JSON | AVRO | ORC | PARQUET | XML | CUSTOM } [ formatTypeOptions ] ]
  [ COMMENT = '<string_literal>' ]
```

### Format Type Options

#### CSV Format Options
```sql
TYPE = CSV
  [ COMPRESSION = { AUTO | GZIP | BZ2 | BROTLI | ZSTD | DEFLATE | RAW_DEFLATE | NONE } ]
  [ RECORD_DELIMITER = { <string> | NONE } ]
  [ FIELD_DELIMITER = <string> ]
  [ FILE_EXTENSION = <string> ]
  [ PARSE_HEADER = { TRUE | FALSE } ]
  [ SKIP_HEADER = <integer> ]
  [ SKIP_BLANK_LINES = { TRUE | FALSE } ]
  [ DATE_FORMAT = <string> ]
  [ TIME_FORMAT = <string> ]
  [ TIMESTAMP_FORMAT = <string> ]
  [ BINARY_FORMAT = { HEX | BASE64 | UTF8 } ]
  [ ESCAPE = { <string> | NONE } ]
  [ ESCAPE_UNENCLOSED_FIELD = { <string> | NONE } ]
  [ TRIM_SPACE = { TRUE | FALSE } ]
  [ FIELD_OPTIONALLY_ENCLOSED_BY = { <string> | NONE } ]
  [ NULL_IF = ( '<string>' [ , '<string>' ... ] ) ]
  [ ERROR_ON_COLUMN_COUNT_MISMATCH = { TRUE | FALSE } ]
  [ REPLACE_INVALID_CHARACTERS = { TRUE | FALSE } ]
  [ VALIDATE_UTF8 = { TRUE | FALSE } ]
  [ EMPTY_FIELD_AS_NULL = { TRUE | FALSE } ]
  [ SKIP_BYTE_ORDER_MARK = { TRUE | FALSE } ]
  [ ENCODING = { 'UTF8' | 'ISO-8859-1' | 'WINDOWS-1252' } ]
```

#### JSON Format Options
```sql
TYPE = JSON
  [ COMPRESSION = { AUTO | GZIP | BZ2 | BROTLI | ZSTD | DEFLATE | RAW_DEFLATE | NONE } ]
  [ DATE_FORMAT = <string> ]
  [ TIME_FORMAT = <string> ]
  [ TIMESTAMP_FORMAT = <string> ]
  [ BINARY_FORMAT = { HEX | BASE64 | UTF8 } ]
  [ TRIM_SPACE = { TRUE | FALSE } ]
  [ NULL_IF = ( '<string>' [ , '<string>' ... ] ) ]
  [ FILE_EXTENSION = <string> ]
  [ ENABLE_OCTAL = { TRUE | FALSE } ]
  [ ALLOW_DUPLICATE = { TRUE | FALSE } ]
  [ STRIP_OUTER_ARRAY = { TRUE | FALSE } ]
  [ STRIP_NULL_VALUES = { TRUE | FALSE } ]
  [ REPLACE_INVALID_CHARACTERS = { TRUE | FALSE } ]
  [ IGNORE_UTF8_ERRORS = { TRUE | FALSE } ]
```

#### PARQUET Format Options
```sql
TYPE = PARQUET
  [ COMPRESSION = { AUTO | LZO | SNAPPY | NONE } ]
  [ SNAPPY_COMPRESSION = { TRUE | FALSE } ]
  [ BINARY_AS_TEXT = { TRUE | FALSE } ]
  [ USE_LOGICAL_TYPE = { TRUE | FALSE } ]
  [ USE_VECTORIZED_SCANNER = { TRUE | FALSE } ]
```

#### XML Format Options
```sql
TYPE = XML
  [ COMPRESSION = { AUTO | GZIP | BZ2 | BROTLI | ZSTD | DEFLATE | RAW_DEFLATE | NONE } ]
  [ IGNORE_UTF8_ERRORS = { TRUE | FALSE } ]
  [ PRESERVE_SPACE = { TRUE | FALSE } ]
  [ STRIP_OUTER_ELEMENT = { TRUE | FALSE } ]
  [ DISABLE_SNOWFLAKE_DATA = { TRUE | FALSE } ]
  [ DISABLE_AUTO_CONVERT = { TRUE | FALSE } ]
  [ SKIP_BYTE_ORDER_MARK = { TRUE | FALSE } ]
```

#### AVRO Format Options
```sql
TYPE = AVRO
  [ COMPRESSION = { AUTO | GZIP | BZ2 | BROTLI | ZSTD | DEFLATE | RAW_DEFLATE | NONE } ]
  [ TRIM_SPACE = { TRUE | FALSE } ]
  [ REPLACE_INVALID_CHARACTERS = { TRUE | FALSE } ]
  [ NULL_IF = ( '<string>' [ , '<string>' ... ] ) ]
```

#### ORC Format Options
```sql
TYPE = ORC
  [ TRIM_SPACE = { TRUE | FALSE } ]
  [ REPLACE_INVALID_CHARACTERS = { TRUE | FALSE } ]
  [ NULL_IF = ( '<string>' [ , '<string>' ... ] ) ]
```

## COMPREHENSIVE_ATTRIBUTE_ANALYSIS

### Core Properties (Required/Common)

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| fileFormatName | Name of the file format | String | N/A | Valid identifier | Yes | Must be unique within schema |
| catalogName | Catalog (database) name | String | Current database | Valid database name | No | Must exist |
| schemaName | Schema name | String | Current schema | Valid schema name | No | Must exist |
| fileFormatType | Type of file format | Enum | CSV | CSV, JSON, AVRO, ORC, PARQUET, XML, CUSTOM | No | Determines available options |
| orReplace | Use CREATE OR REPLACE | Boolean | false | true/false | No | Mutually exclusive with ifNotExists |
| ifNotExists | Use IF NOT EXISTS | Boolean | false | true/false | No | Mutually exclusive with orReplace |
| temporary | Create temporary file format | Boolean | false | true/false | No | Mutually exclusive with volatile |
| volatile | Create volatile file format | Boolean | false | true/false | No | Mutually exclusive with temporary |
| comment | Comment for the file format | String | null | Any string | No | Must be quoted |

### Common Format Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| compression | Compression algorithm | String | AUTO | AUTO, GZIP, BZ2, BROTLI, ZSTD, DEFLATE, RAW_DEFLATE, NONE | No | Format-specific availability |
| dateFormat | Date format string | String | AUTO | Date format pattern | No | Must be valid format |
| timeFormat | Time format string | String | AUTO | Time format pattern | No | Must be valid format |
| timestampFormat | Timestamp format string | String | AUTO | Timestamp format pattern | No | Must be valid format |
| binaryFormat | Binary data format | String | HEX | HEX, BASE64, UTF8 | No | Format-specific |
| trimSpace | Trim leading/trailing spaces | Boolean | false | true/false | No | Not available for all formats |
| nullIf | Strings to convert to NULL | String | null | Comma-separated list | No | Must be quoted strings |
| replaceInvalidCharacters | Replace invalid UTF-8 chars | Boolean | false | true/false | No | Format-specific |
| fileExtension | Expected file extension | String | null | Valid extension | No | Optional hint |

### CSV-Specific Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| recordDelimiter | Record separator | String | \\n | String or NONE | No | Can be multi-character |
| fieldDelimiter | Field separator | String | , | Single character | No | Cannot be same as escape |
| parseHeader | Parse first line as header | Boolean | false | true/false | No | CSV only |
| skipHeader | Number of header lines to skip | Integer | 0 | 0 or positive | No | CSV only |
| skipBlankLines | Skip empty lines | Boolean | false | true/false | No | CSV only |
| escape | Escape character | String | null | Single char or NONE | No | Cannot be same as delimiter |
| escapeUnenclosedField | Escape for unenclosed fields | String | null | Single char or NONE | No | CSV only |
| fieldOptionallyEnclosedBy | Field enclosure character | String | null | Single char or NONE | No | CSV only |
| errorOnColumnCountMismatch | Error on column count mismatch | Boolean | true | true/false | No | CSV only |
| validateUtf8 | Validate UTF-8 encoding | Boolean | true | true/false | No | CSV only |
| emptyFieldAsNull | Treat empty fields as NULL | Boolean | true | true/false | No | CSV only |
| skipByteOrderMark | Skip BOM | Boolean | true | true/false | No | CSV/XML only |
| encoding | Character encoding | String | UTF8 | UTF8, ISO-8859-1, WINDOWS-1252 | No | CSV only |

### JSON-Specific Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| enableOctal | Allow octal values | Boolean | false | true/false | No | JSON only |
| allowDuplicate | Allow duplicate field names | Boolean | false | true/false | No | JSON only |
| stripOuterArray | Remove outer array | Boolean | false | true/false | No | JSON only |
| stripNullValues | Remove null fields | Boolean | false | true/false | No | JSON only |
| ignoreUtf8Errors | Ignore UTF-8 errors | Boolean | false | true/false | No | JSON/XML only |

### PARQUET-Specific Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| snappyCompression | Use Snappy compression | Boolean | true | true/false | No | PARQUET only |
| binaryAsText | Interpret binary as text | Boolean | false | true/false | No | PARQUET only |
| useLogicalType | Use Parquet logical types | Boolean | true | true/false | No | PARQUET only |
| useVectorizedScanner | Use vectorized scanner | Boolean | true | true/false | No | PARQUET only |

### XML-Specific Options

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| preserveSpace | Preserve whitespace | Boolean | false | true/false | No | XML only |
| stripOuterElement | Remove outer element | Boolean | false | true/false | No | XML only |
| disableSnowflakeData | Disable Snowflake data | Boolean | false | true/false | No | XML only |
| disableAutoConvert | Disable auto conversion | Boolean | false | true/false | No | XML only |

## MUTUAL_EXCLUSIVITY_ANALYSIS

### Format Type Exclusivity
- **CSV options** cannot be used with non-CSV types
- **JSON options** cannot be used with non-JSON types
- **PARQUET options** cannot be used with non-PARQUET types
- **XML options** cannot be used with non-XML types
- **AVRO/ORC options** limited to their respective types

### Creation Mode Exclusivity
- `orReplace` and `ifNotExists` are mutually exclusive
- `temporary` and `volatile` are mutually exclusive

### Compression Format Availability
- **PARQUET**: Only AUTO, LZO, SNAPPY, NONE
- **Other formats**: Full compression option set available

## SQL_EXAMPLES

### Example 1: Basic CSV File Format
```sql
CREATE FILE FORMAT my_csv_format
TYPE = CSV
FIELD_DELIMITER = ','
RECORD_DELIMITER = '\n'
SKIP_HEADER = 1
TRIM_SPACE = TRUE;
```

### Example 2: JSON File Format with Options
```sql
CREATE OR REPLACE FILE FORMAT my_json_format
TYPE = JSON
COMPRESSION = GZIP
STRIP_OUTER_ARRAY = TRUE
NULL_IF = ('null', 'NULL')
COMMENT = 'JSON format for data loading';
```

### Example 3: Parquet File Format
```sql
CREATE FILE FORMAT IF NOT EXISTS my_parquet_format
TYPE = PARQUET
COMPRESSION = SNAPPY
BINARY_AS_TEXT = FALSE
USE_LOGICAL_TYPE = TRUE;
```

### Example 4: Complex CSV with All Options
```sql
CREATE TEMPORARY FILE FORMAT complex_csv
TYPE = CSV
COMPRESSION = GZIP
FIELD_DELIMITER = '|'
RECORD_DELIMITER = '\r\n'
SKIP_HEADER = 2
FIELD_OPTIONALLY_ENCLOSED_BY = '"'
ESCAPE = '\\'
NULL_IF = ('NULL', '', 'N/A')
DATE_FORMAT = 'YYYY-MM-DD'
TIMESTAMP_FORMAT = 'YYYY-MM-DD HH24:MI:SS'
ERROR_ON_COLUMN_COUNT_MISMATCH = FALSE
TRIM_SPACE = TRUE
VALIDATE_UTF8 = TRUE
ENCODING = 'UTF8'
COMMENT = 'Complex CSV format with all options';
```

### Example 5: XML File Format
```sql
CREATE VOLATILE FILE FORMAT my_xml_format
TYPE = XML
COMPRESSION = AUTO
PRESERVE_SPACE = TRUE
STRIP_OUTER_ELEMENT = FALSE
IGNORE_UTF8_ERRORS = TRUE
COMMENT = 'XML format for semi-structured data';
```

## VALIDATION_RULES

### Required Field Validation
- `fileFormatName` must be provided and non-empty
- `fileFormatName` must be a valid Snowflake identifier

### Mutual Exclusivity Validation  
- Cannot specify both `orReplace` and `ifNotExists`
- Cannot specify both `temporary` and `volatile`
- Format-specific options only valid for their format type

### Value Range Validation
- `skipHeader` must be 0 or positive integer
- Format type must be valid enum value
- Compression type must be valid for the format type

### Format-Specific Validation
- CSV: `fieldDelimiter` and `escape` cannot be the same character
- CSV: `recordDelimiter` cannot be empty string (use NONE)
- JSON: `stripOuterArray` only meaningful for array-structured JSON
- PARQUET: Limited compression options (AUTO, LZO, SNAPPY, NONE)

## IMPLEMENTATION_NOTES

### Database Object Model
File Format objects should be included in snapshot/diff operations:
- Add `FileFormat` database object class
- Implement snapshot generator for File Formats
- Support in diff/comparison operations

### SQL Generation Strategy
- Use conditional SQL generation based on format type
- Group format-specific options appropriately
- Handle NULL values correctly (omit from SQL vs explicit NULL)
- Quote string values appropriately

### Testing Requirements
- **Unit tests**: Complete SQL string comparison for all format types
- **Integration tests**: Actual File Format creation in Snowflake
- **Separate test files**: One per format type due to mutual exclusivity
- **Edge cases**: Test all option combinations within each format type

### Service Registration
```
META-INF/services/liquibase.change.Change:
com.liquibase.ext.snowflake.change.CreateFileFormatChange

META-INF/services/liquibase.statement.SqlStatement:
com.liquibase.ext.snowflake.statement.CreateFileFormatStatement

META-INF/services/liquibase.sqlgenerator.SqlGenerator:
com.liquibase.ext.snowflake.sqlgenerator.CreateFileFormatGeneratorSnowflake

META-INF/services/liquibase.structure.DatabaseObject:
com.liquibase.ext.snowflake.structure.FileFormat
```

## VALIDATION_AGAINST_INFORMATION_SCHEMA

Based on INFORMATION_SCHEMA.FILE_FORMATS discovery, the following parameters were identified:
- BINARY_FORMAT ✓
- COMMENT ✓  
- COMPRESSION ✓
- DATE_FORMAT ✓
- ERROR_ON_COLUMN_COUNT_MISMATCH ✓
- ESCAPE ✓
- ESCAPE_UNENCLOSED_FIELD ✓
- FIELD_DELIMITER ✓
- FIELD_OPTIONALLY_ENCLOSED_BY ✓
- FILE_FORMAT_NAME ✓
- FILE_FORMAT_TYPE ✓
- NULL_IF ✓
- RECORD_DELIMITER ✓
- SKIP_HEADER ✓
- TIMESTAMP_FORMAT ✓
- TIME_FORMAT ✓
- TRIM_SPACE ✓

**Coverage**: Excellent alignment with INFORMATION_SCHEMA. Additional parameters from documentation (format-specific options) not in INFORMATION_SCHEMA but documented in official Snowflake docs.

## PRIORITY
**HIGH** - File Formats are essential for data loading operations and external table creation in Snowflake.