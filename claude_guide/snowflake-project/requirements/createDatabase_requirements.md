# CreateDatabase Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/create-database
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
CREATE DATABASE database_name;

-- Full syntax with all options
CREATE [ OR REPLACE ] [ TRANSIENT ] DATABASE [ IF NOT EXISTS ] <name>
  [ CLONE <source_db_name>
        [ { AT | BEFORE } ( { TIMESTAMP => <timestamp> | OFFSET => <time_difference> | STATEMENT => <id> } ) ] ]
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
  [ COMMENT = '<string_literal>' ]
  [ [ WITH ] TAG ( <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ] ) ]
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| databaseName | Name of the database to create | String | - | Valid Snowflake identifier | Yes |
| orReplace | Replace database if it exists | Boolean | false | true/false | No |
| transient | Create as transient database | Boolean | false | true/false | No |
| ifNotExists | Only create if database doesn't exist | Boolean | false | true/false | No |
| cloneSource | Source database to clone from | String | null | Existing database name | No |
| dataRetentionTimeInDays | Time Travel retention period | Integer | 1 | 0-90 (0 for transient) | No |
| maxDataExtensionTimeInDays | Maximum extension for Time Travel | Integer | 14 | 0-90 | No |
| defaultDdlCollation | Default collation for string columns | String | null | Valid collation spec | No |
| comment | Comment/description for database | String | null | Any string up to 256 chars | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **orReplace** and **ifNotExists** - Cannot use both in same statement
   - `CREATE OR REPLACE DATABASE` - Valid
   - `CREATE DATABASE IF NOT EXISTS` - Valid
   - `CREATE OR REPLACE DATABASE IF NOT EXISTS` - Invalid

2. **transient** and **dataRetentionTimeInDays > 0** - Transient databases must have 0 retention
   - `CREATE TRANSIENT DATABASE` with `DATA_RETENTION_TIME_IN_DAYS = 0` - Valid
   - `CREATE TRANSIENT DATABASE` with `DATA_RETENTION_TIME_IN_DAYS = 7` - Invalid

### Required Combinations
1. If **cloneSource** is specified, database must not exist (unless using OR REPLACE)
2. If **transient** is true, **dataRetentionTimeInDays** must be 0 or omitted

## 4. SQL Examples for Testing

### Example 1: Basic Database
```sql
CREATE DATABASE basic_database;
```

### Example 2: Transient Database
```sql
CREATE TRANSIENT DATABASE transient_database;
```

### Example 3: Database with Time Travel Settings
```sql
CREATE DATABASE retention_database
  DATA_RETENTION_TIME_IN_DAYS = 7
  MAX_DATA_EXTENSION_TIME_IN_DAYS = 30
  COMMENT = 'Database with custom retention';
```

### Example 4: Replace Existing Database
```sql
CREATE OR REPLACE DATABASE replacement_database
  COMMENT = 'This replaces any existing database';
```

### Example 5: Conditional Creation
```sql
CREATE DATABASE IF NOT EXISTS conditional_database;
```

### Example 6: Clone Database
```sql
CREATE DATABASE cloned_database CLONE source_database;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **createDatabase.xml** - Basic creation, transient, and property settings
2. **createOrReplaceDatabase.xml** - OR REPLACE variations
3. **createDatabaseIfNotExists.xml** - IF NOT EXISTS variations
4. **createDatabaseClone.xml** - CLONE operations (if significantly different)

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"
   - If transient=true and dataRetentionTimeInDays > 0, throw: "Transient databases must have DATA_RETENTION_TIME_IN_DAYS = 0"

3. **Value Constraints**:
   - dataRetentionTimeInDays must be 0-90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays and <= 90

4. **Clone Validation**:
   - If cloneSource specified, it must be a valid existing database

## 7. Expected Behaviors

1. **OR REPLACE behavior**:
   - Drops existing database and all contents
   - Creates new empty database
   - Does NOT preserve grants

2. **IF NOT EXISTS behavior**:
   - Succeeds silently if database exists
   - Does not modify existing database

3. **TRANSIENT behavior**:
   - No Time Travel (0 day retention)
   - No Fail-safe
   - All contained objects are also transient

4. **CLONE behavior**:
   - Creates zero-copy clone of source database
   - Includes all schemas and objects
   - Independent from source after creation

## 8. Error Conditions

1. Database already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid database name
3. Insufficient privileges
4. Clone source database doesn't exist
5. Invalid retention time values

## 9. Implementation Notes

- Database names are automatically converted to uppercase unless quoted
- Creating a database automatically creates INFORMATION_SCHEMA and PUBLIC schemas
- Current database context is not changed by CREATE DATABASE
- Dropped databases can be recovered with UNDROP within retention period
- Consider impact on connection context for Liquibase