# DropDatabase Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/drop-database
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
DROP DATABASE database_name;

-- Full syntax with all options
DROP DATABASE [ IF EXISTS ] <name> [ CASCADE | RESTRICT ]
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| databaseName | Name of the database to drop | String | - | Valid Snowflake identifier | Yes |
| ifExists | Only drop if database exists | Boolean | false | true/false | No |
| cascade | Drop all contained schemas and objects | Boolean | false | true/false | No |
| restrict | Fail if database contains schemas | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **cascade** and **restrict** - Cannot use both in same statement
   - `DROP DATABASE mydb CASCADE` - Valid
   - `DROP DATABASE mydb RESTRICT` - Valid
   - `DROP DATABASE mydb CASCADE RESTRICT` - Invalid

### Default Behavior
- If neither CASCADE nor RESTRICT is specified, the default behavior is RESTRICT
- Database must be empty (only contain INFORMATION_SCHEMA and PUBLIC schemas)

## 4. SQL Examples for Testing

### Example 1: Basic Drop
```sql
DROP DATABASE basic_database;
```

### Example 2: Drop If Exists
```sql
DROP DATABASE IF EXISTS nonexistent_database;
```

### Example 3: Drop with Cascade
```sql
DROP DATABASE database_with_schemas CASCADE;
```

### Example 4: Drop with Restrict (Explicit)
```sql
DROP DATABASE empty_database RESTRICT;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **dropDatabase.xml** - Basic drop and IF EXISTS variations
2. **dropDatabaseCascade.xml** - CASCADE variations (if mutually exclusive behavior)

## 6. Validation Rules

1. **Required Attributes**:
   - databaseName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If cascade=true and restrict=true, throw: "Cannot use both CASCADE and RESTRICT"

3. **Connection Context**:
   - Cannot drop the current database
   - Must switch to a different database first

## 7. Expected Behaviors

1. **IF EXISTS behavior**:
   - Succeeds silently if database doesn't exist
   - No error thrown

2. **CASCADE behavior**:
   - Drops all schemas in the database
   - Drops all objects in all schemas
   - Includes user-created schemas and their contents

3. **RESTRICT behavior**:
   - Fails if database contains any user-created schemas
   - Only succeeds if database contains just INFORMATION_SCHEMA and PUBLIC
   - This is the default if neither CASCADE nor RESTRICT specified

## 8. Error Conditions

1. Database doesn't exist (without IF EXISTS)
2. Database contains schemas (with RESTRICT or default)
3. Insufficient privileges
4. Database is currently in use (current database)
5. Active connections to the database

## 9. Implementation Notes

- Database names are automatically converted to uppercase unless quoted
- Current database cannot be dropped
- Dropped databases can be recovered using UNDROP DATABASE within retention period
- All privileges on the database are also dropped
- Consider connection management in Liquibase when dropping databases