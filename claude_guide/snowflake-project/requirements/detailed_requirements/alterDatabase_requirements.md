# AlterDatabase Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-database
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Rename database
ALTER DATABASE [ IF EXISTS ] <name> RENAME TO <new_db_name>;

-- Set/unset properties
ALTER DATABASE [ IF EXISTS ] <name> SET
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
  [ COMMENT = '<string_literal>' ];

ALTER DATABASE [ IF EXISTS ] <name> UNSET
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
  [ COMMENT ];

-- Tagging
ALTER DATABASE [ IF EXISTS ] <name> SET TAG <tag_name> = '<tag_value>' [ , <tag_name> = '<tag_value>' , ... ];
ALTER DATABASE [ IF EXISTS ] <name> UNSET TAG <tag_name> [ , <tag_name> , ... ];

-- Enable/disable replication
ALTER DATABASE <name> ENABLE REPLICATION TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ];
ALTER DATABASE <name> DISABLE REPLICATION [ TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ] ];

-- Enable/disable failover
ALTER DATABASE <name> ENABLE FAILOVER TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ];
ALTER DATABASE <name> DISABLE FAILOVER [ TO ACCOUNTS <account_identifier> [ , <account_identifier> , ... ] ];

-- Refresh from share
ALTER DATABASE <name> REFRESH;
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| databaseName | Name of the database to alter | String | - | Valid Snowflake identifier | Yes |
| newName | New name for database (rename) | String | null | Valid Snowflake identifier | No |
| ifExists | Only alter if database exists | Boolean | false | true/false | No |
| setDataRetentionTimeInDays | Set Time Travel retention | Integer | null | 0-90 | No |
| setMaxDataExtensionTimeInDays | Set max Time Travel extension | Integer | null | 0-90 | No |
| setDefaultDdlCollation | Set default collation | String | null | Valid collation | No |
| setComment | Set database comment | String | null | String up to 256 chars | No |
| unsetDataRetentionTimeInDays | Remove retention setting | Boolean | false | true/false | No |
| unsetMaxDataExtensionTimeInDays | Remove max extension setting | Boolean | false | true/false | No |
| unsetDefaultDdlCollation | Remove collation setting | Boolean | false | true/false | No |
| unsetComment | Remove comment | Boolean | false | true/false | No |
| enableReplication | Enable replication to accounts | Boolean | false | true/false | No |
| disableReplication | Disable replication | Boolean | false | true/false | No |
| replicationAccounts | Account identifiers for replication | String | null | Comma-separated account list | No |
| enableFailover | Enable failover to accounts | Boolean | false | true/false | No |
| disableFailover | Disable failover | Boolean | false | true/false | No |
| failoverAccounts | Account identifiers for failover | String | null | Comma-separated account list | No |
| refresh | Refresh database from share | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Operations
1. **RENAME TO** cannot be combined with other operations in same statement
2. **SET** and **UNSET** for same property cannot be in same statement
3. **enableReplication** and **disableReplication** cannot both be true
4. **enableFailover** and **disableFailover** cannot both be true
5. **REFRESH** must be used alone

### Operation Groups
1. **Rename Operation**: Uses only databaseName, newName, ifExists
2. **Property Operations**: Uses SET/UNSET attributes
3. **Replication Operations**: Uses enable/disable replication with accounts
4. **Failover Operations**: Uses enable/disable failover with accounts
5. **Refresh Operation**: Uses only databaseName and refresh

## 4. SQL Examples for Testing

### Example 1: Rename Database
```sql
ALTER DATABASE old_db_name RENAME TO new_db_name;
```

### Example 2: Set Properties
```sql
ALTER DATABASE my_database SET
  DATA_RETENTION_TIME_IN_DAYS = 7
  COMMENT = 'Production database';
```

### Example 3: Unset Properties
```sql
ALTER DATABASE my_database UNSET
  DATA_RETENTION_TIME_IN_DAYS
  COMMENT;
```

### Example 4: Enable Replication
```sql
ALTER DATABASE my_database ENABLE REPLICATION TO ACCOUNTS org1.account1, org2.account2;
```

### Example 5: If Exists with Set
```sql
ALTER DATABASE IF EXISTS my_database SET
  DEFAULT_DDL_COLLATION = 'en-ci';
```

## 5. Test Scenarios

Based on the operation types, we need the following test approaches:
1. **alterDatabaseRename.xml** - Rename operations
2. **alterDatabaseProperties.xml** - SET/UNSET property operations
3. **alterDatabaseReplication.xml** - Replication enable/disable operations
4. **alterDatabaseFailover.xml** - Failover operations (if supported)

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - For rename: newName cannot be null or empty
   - For replication/failover: accounts list cannot be empty when enabling

2. **Mutual Exclusivity**:
   - Cannot combine rename with other operations
   - Cannot SET and UNSET same property
   - Cannot enable and disable same feature

3. **Value Constraints**:
   - dataRetentionTimeInDays must be 0-90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

## 7. Expected Behaviors

1. **Rename behavior**:
   - All references to the database are updated
   - Grants on the database are preserved
   - Cannot rename current database

2. **Property changes**:
   - Take effect immediately
   - Affect all new objects created in database

3. **Replication**:
   - Enables sharing of database to other accounts
   - Requires appropriate privileges

4. **Failover**:
   - Configures database for business continuity
   - Requires Enterprise Edition or higher

## 8. Error Conditions

1. Database doesn't exist (without IF EXISTS)
2. New name already exists (for rename)
3. Invalid property values
4. Insufficient privileges
5. Current database (for rename)
6. Invalid account identifiers
7. Feature not available in edition (failover)

## 9. Implementation Notes

- ALTER DATABASE is not transactional
- Changes take effect immediately
- Some operations require ACCOUNTADMIN role
- Replication and failover require cross-account setup
- Consider connection context when renaming databases