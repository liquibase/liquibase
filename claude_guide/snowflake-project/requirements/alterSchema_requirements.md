# AlterSchema Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-schema
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Rename schema
ALTER SCHEMA [ IF EXISTS ] <name> RENAME TO <new_name>;

-- Set/unset properties
ALTER SCHEMA [ IF EXISTS ] <name> SET
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
  [ COMMENT = '<string_literal>' ];

ALTER SCHEMA [ IF EXISTS ] <name> UNSET
  [ DATA_RETENTION_TIME_IN_DAYS ]
  [ MAX_DATA_EXTENSION_TIME_IN_DAYS ]
  [ DEFAULT_DDL_COLLATION ]
  [ LOG_LEVEL ]
  [ TRACE_LEVEL ]
  [ SUSPEND_TASK_AFTER_NUM_FAILURES ]
  [ TASK_AUTO_RETRY_ATTEMPTS ]
  [ USER_TASK_MANAGED_INITIAL_WAREHOUSE_SIZE ]
  [ USER_TASK_TIMEOUT_MS ]
  [ USER_TASK_MINIMUM_TRIGGER_INTERVAL_IN_SECONDS ]
  [ QUOTED_IDENTIFIERS_IGNORE_CASE ]
  [ ENABLE_CONSOLE_OUTPUT ]
  [ PIPE_EXECUTION_PAUSED ]
  [ COMMENT ];

-- Manage access
ALTER SCHEMA [ IF EXISTS ] <name> { ENABLE | DISABLE } MANAGED ACCESS;

-- Tagging
ALTER SCHEMA [ IF EXISTS ] <name> SET TAG <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ];
ALTER SCHEMA [ IF EXISTS ] <name> UNSET TAG <tag_name> [ , <tag_name> , ... ];
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| schemaName | Name of the schema to alter | String | - | Valid Snowflake identifier | Yes |
| newName | New name for schema (rename) | String | null | Valid Snowflake identifier | No |
| ifExists | Only alter if schema exists | Boolean | false | true/false | No |
| setDataRetentionTimeInDays | Set Time Travel retention | Integer | null | 0-90 | No |
| setMaxDataExtensionTimeInDays | Set max Time Travel extension | Integer | null | 0-90 | No |
| setDefaultDdlCollation | Set default collation | String | null | Valid collation | No |
| setPipeExecutionPaused | Set pipe execution state | Boolean | null | true/false | No |
| setComment | Set schema comment | String | null | String up to 256 chars | No |
| unsetDataRetentionTimeInDays | Remove retention setting | Boolean | false | true/false | No |
| unsetMaxDataExtensionTimeInDays | Remove max extension setting | Boolean | false | true/false | No |
| unsetDefaultDdlCollation | Remove collation setting | Boolean | false | true/false | No |
| unsetPipeExecutionPaused | Remove pipe execution setting | Boolean | false | true/false | No |
| unsetComment | Remove comment | Boolean | false | true/false | No |
| enableManagedAccess | Enable managed access | Boolean | false | true/false | No |
| disableManagedAccess | Disable managed access | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Operations
1. **RENAME TO** cannot be combined with SET/UNSET operations in same statement
2. **SET** and **UNSET** for same property cannot be in same statement
3. **enableManagedAccess** and **disableManagedAccess** cannot both be true

### Operation Groups
1. **Rename Operation**: Uses only schemaName, newName, ifExists
2. **Property Operations**: Uses SET/UNSET attributes
3. **Access Operations**: Uses enable/disable managed access

## 4. SQL Examples for Testing

### Example 1: Rename Schema
```sql
ALTER SCHEMA old_name RENAME TO new_name;
```

### Example 2: Set Properties
```sql
ALTER SCHEMA my_schema SET
  DATA_RETENTION_TIME_IN_DAYS = 7
  COMMENT = 'Updated schema description';
```

### Example 3: Unset Properties
```sql
ALTER SCHEMA my_schema UNSET
  DATA_RETENTION_TIME_IN_DAYS
  COMMENT;
```

### Example 4: Enable Managed Access
```sql
ALTER SCHEMA my_schema ENABLE MANAGED ACCESS;
```

### Example 5: If Exists with Set
```sql
ALTER SCHEMA IF EXISTS my_schema SET
  PIPE_EXECUTION_PAUSED = TRUE;
```

## 5. Test Scenarios

Based on the operation types, we need the following test approaches:
1. **alterSchemaRename.xml** - Rename operations
2. **alterSchemaProperties.xml** - SET/UNSET property operations
3. **alterSchemaAccess.xml** - Managed access operations

## 6. Validation Rules

1. **Required Attributes**:
   - schemaName cannot be null or empty
   - For rename: newName cannot be null or empty

2. **Mutual Exclusivity**:
   - Cannot combine rename with other operations
   - Cannot SET and UNSET same property
   - Cannot enable and disable managed access

3. **Value Constraints**:
   - dataRetentionTimeInDays must be 0-90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

## 7. Expected Behaviors

1. **Rename behavior**:
   - All references to the schema are updated
   - Grants on the schema are preserved

2. **Property changes**:
   - Take effect immediately
   - May affect existing objects in schema

3. **Managed Access**:
   - When enabled, only schema owner can grant privileges
   - Existing grants are preserved

## 8. Error Conditions

1. Schema doesn't exist (without IF EXISTS)
2. New name already exists (for rename)
3. Invalid property values
4. Insufficient privileges
5. Schema is in use by active sessions (for some operations)

## 9. Implementation Notes

- ALTER SCHEMA is not transactional
- Changes take effect immediately
- Some properties may require specific privileges
- Consider how to handle multiple operations in Liquibase (may need multiple changesets)