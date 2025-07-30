# DropSchema Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/drop-schema
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Basic Syntax
```sql
-- Minimal syntax
DROP SCHEMA schema_name;

-- Full syntax with all options
DROP SCHEMA [ IF EXISTS ] <name> [ CASCADE | RESTRICT ]
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| schemaName | Name of the schema to drop | String | - | Valid Snowflake identifier | Yes |
| ifExists | Only drop if schema exists | Boolean | false | true/false | No |
| cascade | Drop all contained objects | Boolean | false | true/false | No |
| restrict | Fail if schema contains objects | Boolean | false | true/false | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. **cascade** and **restrict** - Cannot use both in same statement
   - `DROP SCHEMA myschema CASCADE` - Valid
   - `DROP SCHEMA myschema RESTRICT` - Valid
   - `DROP SCHEMA myschema CASCADE RESTRICT` - Invalid

### Default Behavior
- If neither CASCADE nor RESTRICT is specified, the default behavior is RESTRICT

## 4. SQL Examples for Testing

### Example 1: Basic Drop
```sql
DROP SCHEMA basic_schema;
```

### Example 2: Drop If Exists
```sql
DROP SCHEMA IF EXISTS nonexistent_schema;
```

### Example 3: Drop with Cascade
```sql
DROP SCHEMA schema_with_objects CASCADE;
```

### Example 4: Drop with Restrict (Explicit)
```sql
DROP SCHEMA empty_schema RESTRICT;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:
1. **dropSchema.xml** - Basic drop and IF EXISTS variations
2. **dropSchemaCascade.xml** - CASCADE variations (if mutually exclusive with other features)

## 6. Validation Rules

1. **Required Attributes**:
   - schemaName cannot be null or empty
   - Must be valid Snowflake identifier

2. **Mutual Exclusivity**:
   - If cascade=true and restrict=true, throw: "Cannot use both CASCADE and RESTRICT"

3. **Default Values**:
   - ifExists defaults to false
   - cascade defaults to false
   - restrict defaults to false (but RESTRICT is the default behavior)

## 7. Expected Behaviors

1. **IF EXISTS behavior**:
   - Succeeds silently if schema doesn't exist
   - No error thrown

2. **CASCADE behavior**:
   - Drops all contained database objects
   - Includes tables, views, sequences, functions, procedures, etc.

3. **RESTRICT behavior**:
   - Fails if schema contains any objects
   - This is the default if neither CASCADE nor RESTRICT specified

## 8. Error Conditions

1. Schema doesn't exist (without IF EXISTS)
2. Schema contains objects (with RESTRICT or default)
3. Insufficient privileges
4. Schema is currently in use

## 9. Implementation Notes

- Schema names are automatically converted to uppercase unless quoted
- Current schema cannot be dropped while in use
- Dropped schemas cannot be recovered (no UNDROP for schemas)
- Consider rollback support: Would need to capture all contained objects for CASCADE