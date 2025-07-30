# CreateSchema Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/create-schema
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
CREATE SCHEMA schema_name;

-- Full syntax with all options
CREATE [ OR REPLACE ] [ TRANSIENT ] SCHEMA [ IF NOT EXISTS ] <name>
  [ CLONE <source_schema_name>
        [ { AT | BEFORE } ( { TIMESTAMP => <timestamp> | OFFSET => <time_difference> | STATEMENT => <id> } ) ] ]
  [ WITH MANAGED ACCESS ]
  [ DATA_RETENTION_TIME_IN_DAYS = <integer> ]
  [ MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer> ]
  [ DEFAULT_DDL_COLLATION = '<collation_specification>' ]
  [ LOG_LEVEL = '<log_level>' ]
  [ TRACE_LEVEL = '<trace_level>' ]
  [ SUSPEND_TASK_AFTER_NUM_FAILURES = <num> ]
  [ TASK_AUTO_RETRY_ATTEMPTS = <num> ]
  [ USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE = <warehouse_size> ]
  [ USER_TASK_TIMEOUT_MS = <num> ]
  [ USER_TASK_MINIMUM_TRIGGER_INTERVAL_IN_SECONDS = <num> ]
  [ QUOTED_IDENTIFIERS_IGNORE_CASE = { TRUE | FALSE } ]
  [ ENABLE_CONSOLE_OUTPUT = { TRUE | FALSE } ]
  [ PIPE_EXECUTION_PAUSED = { TRUE | FALSE } ]
  [ COMMENT = '<string_literal>' ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ) ]
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| schemaName | Name of the schema to create | String | - | Valid Snowflake identifier | Yes |
| orReplace | Replace schema if it exists | Boolean | false | true/false | No |
| transient | Create as transient schema | Boolean | false | true/false | No |
| ifNotExists | Only create if schema doesn't exist | Boolean | false | true/false | No |
| cloneSource | Source schema to clone from | String | null | Existing schema name | No |
| managedAccess | Enable managed access | Boolean | false | true/false | No |
| dataRetentionTimeInDays | Time Travel retention period | Integer | 1 | 0-90 (0 for transient) | No |
| maxDataExtensionTimeInDays | Maximum extension for Time Travel | Integer | 14 | 0-90 | No |
| defaultDdlCollation | Default collation for string columns | String | null | Valid collation spec | No |
| pipeExecutionPaused | Pause pipe execution in schema | Boolean | false | true/false | No |
| comment | Comment/description for schema | String | null | Any string up to 256 chars | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE SCHEMA` - Valid
   - `CREATE SCHEMA IF NOT EXISTS` - Valid  
   - `CREATE OR REPLACE SCHEMA IF NOT EXISTS` - Invalid

2. **transient** and **dataRetentionTimeInDays > 0** - Transient schemas must have 0 retention
   - `CREATE TRANSIENT SCHEMA` with `DATA_RETENTION_TIME_IN_DAYS = 0` - Valid
   - `CREATE TRANSIENT SCHEMA` with `DATA_RETENTION_TIME_IN_DAYS = 7` - Invalid

### Required Combinations
1. If **cloneSource** is specified, schema must not exist (unless using OR REPLACE)
2. If **transient** is true, **dataRetentionTimeInDays** must be 0 or omitted

## 4. SQL Examples for Testing

### Example 1: Basic Schema
```sql
CREATE SCHEMA basic_schema;
```

### Example 2: Transient Schema
```sql
CREATE TRANSIENT SCHEMA transient_schema;
```

### Example 3: Schema with Managed Access and Comment
```sql
CREATE SCHEMA managed_schema
  WITH MANAGED ACCESS
  COMMENT = 'Schema with centralized access control';
```

### Example 4: Schema with Time Travel Settings
```sql
CREATE SCHEMA retention_schema
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30;
```

### Example 5: Replace Existing Schema
```sql
CREATE OR REPLACE SCHEMA replacement_schema
  COMMENT = 'This replaces any existing schema';
```

### Example 6: Conditional Creation
```sql
CREATE SCHEMA IF NOT EXISTS conditional_schema
  COMMENT = 'Only created if it does not exist';
```

### Example 7: Schema with Pipe Execution Paused
```sql
CREATE SCHEMA paused_pipes_schema
  PIPE_EXECUTION_PAUSED = TRUE
  COMMENT = 'All pipes in this schema start paused';
```

## 5. Test Scenarios

Based on the mutual exclusivity rules and features, we need the following test files:

1. **createSchema.xml** - Tests basic creation, transient, managed access, retention settings, comments
2. **createOrReplaceSchema.xml** - Tests OR REPLACE functionality (separate due to mutual exclusivity with IF NOT EXISTS)
3. **createSchemaIfNotExists.xml** - Tests IF NOT EXISTS functionality (separate due to mutual exclusivity with OR REPLACE)
4. **createSchemaAdvanced.xml** - Tests advanced features like pipeExecutionPaused, defaultDdlCollation

## 6. Validation Rules

1. **schemaName** validation:
   - Cannot be null or empty
   - Must be valid Snowflake identifier (alphanumeric, underscore, dollar sign)
   - Maximum 255 characters
   - Case-insensitive unless quoted

2. **Mutual exclusivity validation**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"

3. **Transient schema validation**:
   - If transient=true and dataRetentionTimeInDays > 0, throw: "Transient schemas must have DATA_RETENTION_TIME_IN_DAYS = 0"

4. **Retention time validation**:
   - dataRetentionTimeInDays must be between 0 and 90
   - maxDataExtensionTimeInDays must be between 0 and 90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

5. **Comment validation**:
   - Maximum 256 characters

## 7. Expected Behaviors

1. **OR REPLACE behavior**:
   - Preserves grants on the schema
   - Drops all objects within the schema
   - Maintains schema ownership

2. **IF NOT EXISTS behavior**:
   - Succeeds silently if schema exists
   - Does not modify existing schema

3. **TRANSIENT behavior**:
   - No Time Travel (0 day retention)
   - No Fail-safe (objects purged after 1 day)
   - Lower storage costs

4. **MANAGED ACCESS behavior**:
   - Only schema owner can grant privileges
   - Centralizes access control

## 8. Error Conditions

1. Schema already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid schema name
3. Insufficient privileges
4. Clone source schema doesn't exist
5. Invalid retention time values

## 9. Implementation Notes

- Schema names are automatically converted to uppercase unless quoted
- Comments are stored in INFORMATION_SCHEMA.SCHEMATA
- Transient property cannot be changed after creation
- Managed access can be enabled/disabled with ALTER SCHEMA