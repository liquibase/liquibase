# FileFormat Snapshot/Diff Requirements

## Official Documentation URLs
- CREATE: https://docs.snowflake.com/en/sql-reference/sql/create-fileformat
- SHOW: https://docs.snowflake.com/en/sql-reference/sql/show-fileformats
- DESCRIBE: https://docs.snowflake.com/en/sql-reference/sql/describe-fileformat

## Complete Property List
| Property | Type | Required | Default | Constraints | Comparison | Notes |
|----------|------|----------|---------|-------------|------------|-------|
| name | String | Yes | - | Non-empty | Full | Primary identifier |
| formatType | String | Yes | - | CSV,JSON,PARQUET,etc | Full | Format type |
| compression | String | No | AUTO | AUTO,GZIP,BZ2,etc | Full | Compression type |
| recordDelimiter | String | No | \\n | Single char | Full | Record separator |
| fieldDelimiter | String | No | , | Single char | Full | Field separator |
| quoteCharacter | String | No | " | Single char | Full | Quote character |
| escapeCharacter | String | No | \\\\ | Single char | Full | Escape character |
| dateFormat | String | No | AUTO | Format string | Full | Date format |
| timestampFormat | String | No | AUTO | Format string | Full | Timestamp format |
| binaryFormat | String | No | HEX | HEX,BASE64 | Full | Binary encoding |
| nullIf | String | No | - | Pattern | Full | Null representation |
| errorOnColumnCountMismatch | Boolean | No | true | true/false | Full | Validation flag |
| skipHeader | Integer | No | 0 | >= 0 | Full | Skip header lines |
| skipBlankLines | Boolean | No | false | true/false | Full | Skip empty lines |
| trimSpace | Boolean | No | false | true/false | Full | Trim whitespace |
| emptyFieldAsNull | Boolean | No | true | true/false | Full | Empty field handling |

## TDD Implementation Plan
### Unit Test Requirements:
- Constructor tests (valid/invalid inputs)
- Property getter/setter tests
- Validation constraint tests
- equals/hashCode/toString tests
