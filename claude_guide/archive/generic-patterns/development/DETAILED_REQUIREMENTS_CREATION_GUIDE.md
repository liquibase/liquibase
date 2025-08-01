# Detailed Requirements Creation Guide

## Overview

Before implementing any database change type, you MUST create a detailed requirements document by researching the database vendor's official documentation. This guide shows you how to create comprehensive requirements that will drive your implementation.

## When to Create Requirements

Create a requirements document when:
1. Implementing a new change type that doesn't exist
2. Adding database-specific features to an existing change type
3. Discovering undocumented behavior during implementation

## Requirements Document Location

All requirements documents must be stored in:
```
claude_guide/project/requirements/detailed_requirements/<changeTypeName>_requirements.md
```

Example: `createSchema_requirements.md`, `dropWarehouse_requirements.md`

## Requirements Document Template

```markdown
# <ChangeType> Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: [Link to vendor documentation]
- Version: [Database version]
- Last Updated: [Date]

### Basic Syntax
```sql
-- Minimal syntax
CREATE SCHEMA schema_name;

-- Full syntax with all options
CREATE [OR REPLACE] [TRANSIENT] SCHEMA [IF NOT EXISTS] schema_name
  [CLONE source_schema_name]
  [WITH MANAGED ACCESS]
  [DATA_RETENTION_TIME_IN_DAYS = <integer>]
  [MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer>]
  [DEFAULT_DDL_COLLATION = '<collation_specification>']
  [COMMENT = '<string_literal>'];
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| schemaName | Name of the schema | String | - | Valid identifier | Yes |
| orReplace | Replace if exists | Boolean | false | true/false | No |
| transient | Create transient schema | Boolean | false | true/false | No |
| ifNotExists | Only create if not exists | Boolean | false | true/false | No |
| managedAccess | Enable managed access | Boolean | false | true/false | No |
| dataRetentionTimeInDays | Data retention period | Integer | 1 | 0-90 | No |
| comment | Schema comment | String | null | Any string | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. `orReplace` and `ifNotExists` - Cannot be used together
2. `transient` and `dataRetentionTimeInDays > 0` - Transient schemas have 0 retention

### Required Combinations
1. If `cloneSource` is specified, schema must not exist (unless using OR REPLACE)

## 4. SQL Examples for Testing

### Example 1: Basic Schema
```sql
CREATE SCHEMA basic_schema;
```

### Example 2: Transient Schema
```sql
CREATE TRANSIENT SCHEMA transient_schema;
```

### Example 3: Schema with All Compatible Options
```sql
CREATE SCHEMA full_schema
  WITH MANAGED ACCESS
  DATA_RETENTION_TIME_IN_DAYS = 7
  COMMENT = 'Full featured schema';
```

### Example 4: Conditional Creation
```sql
CREATE SCHEMA IF NOT EXISTS conditional_schema;
```

### Example 5: Replace Existing
```sql
CREATE OR REPLACE SCHEMA replacement_schema;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:

1. **<changeType>.xml** - Basic functionality and compatible options
2. **<changeType>OrReplace.xml** - OR REPLACE variations (if mutually exclusive)
3. **<changeType>IfNotExists.xml** - IF NOT EXISTS variations (if mutually exclusive)

## 6. Validation Rules

1. **Required Attributes**:
   - schemaName cannot be null or empty
   - Must be valid identifier (alphanumeric, underscore, dollar sign)

2. **Mutual Exclusivity**:
   - If orReplace=true and ifNotExists=true, throw: "Cannot use both OR REPLACE and IF NOT EXISTS"

3. **Value Constraints**:
   - dataRetentionTimeInDays must be between 0 and 90
   - maxDataExtensionTimeInDays must be >= dataRetentionTimeInDays

4. **Combination Rules**:
   - If transient=true and dataRetentionTimeInDays > 0, throw: "Transient schemas must have DATA_RETENTION_TIME_IN_DAYS = 0"

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
   - No Fail-safe
   - Lower storage costs

## 8. Error Conditions

1. Schema already exists (without OR REPLACE or IF NOT EXISTS)
2. Invalid schema name
3. Insufficient privileges
4. Clone source schema doesn't exist
5. Invalid retention time values

## 9. Implementation Notes

- Schema names are automatically converted to uppercase unless quoted
- Comments are stored in INFORMATION_SCHEMA
- Some properties cannot be changed after creation (e.g., TRANSIENT)
- Consider rollback support requirements
```

## Research Process

### Step 1: Find Official Documentation
1. Search for "<database> CREATE <object> syntax"
2. Look for vendor's official SQL reference
3. Check version-specific documentation
4. Note the documentation URL and version

### Step 2: Document Complete Syntax
1. Copy the full BNF/syntax diagram
2. Include both minimal and complete syntax
3. Note all optional clauses and their order
4. Identify any version-specific features

### Step 3: Create Attribute Table
For each parameter in the syntax:
1. **Attribute**: The parameter name as it will appear in Liquibase
2. **Description**: What the parameter does
3. **Data Type**: String, Boolean, Integer, etc.
4. **Default**: Database default value if not specified
5. **Valid Values**: Acceptable values or ranges
6. **Required**: Yes/No

### Step 4: Identify Mutual Exclusivity
1. Look for "cannot be used with" in documentation
2. Test combinations in database to verify
3. Document which attributes conflict
4. Plan separate test files for incompatible features

### Step 5: Create SQL Examples
1. Start with the simplest valid SQL
2. Add examples for each feature
3. Create examples that combine compatible features
4. Include edge cases and maximum complexity

### Step 6: Define Test Scenarios
Based on mutual exclusivity:
1. Group compatible features together
2. Create separate test files for incompatible features
3. Plan for both positive and negative test cases

### Step 7: Document Validation Rules
1. Required field validation
2. Value range validation
3. Mutual exclusivity validation
4. Format/pattern validation
5. Business rule validation

### Step 8: Note Special Behaviors
1. Default behaviors
2. Side effects
3. Performance implications
4. Security considerations
5. Rollback possibilities

## Common Patterns by Object Type

### Schema Objects
- Often have IF NOT EXISTS and OR REPLACE options
- May have storage properties (TRANSIENT, TEMPORARY)
- Usually support comments/descriptions
- May have security features (MANAGED ACCESS)

### Table Objects
- Complex column definitions
- Multiple constraint types
- Storage and performance options
- Partitioning and clustering

### Warehouse Objects
- Size specifications
- Auto-suspend and auto-resume
- Resource monitors
- Scaling policies

### Database Objects
- Replication settings
- Default schemas
- Retention policies
- Cross-region options

## Quality Checklist

Before considering requirements complete:

- [ ] Official documentation URL included
- [ ] Complete syntax documented
- [ ] All attributes in table with full details
- [ ] Mutual exclusivity rules identified
- [ ] At least 5 SQL examples provided
- [ ] Test scenarios planned
- [ ] Validation rules comprehensive
- [ ] Special behaviors documented
- [ ] Error conditions listed
- [ ] Implementation notes helpful

## Example Requirements Documents

See these examples for reference:
- `createSchema_requirements.md` - Shows complex mutual exclusivity
- `dropSchema_requirements.md` - Shows simpler drop operation
- `createWarehouse_requirements.md` - Shows warehouse-specific attributes

## Tips for Success

1. **Be Thorough**: Missing requirements cause rework later
2. **Test in Database**: Verify mutual exclusivity by testing
3. **Think About Tests**: Plan test scenarios while documenting
4. **Consider Edge Cases**: What happens with special characters, long names, etc.
5. **Document Assumptions**: If something is unclear, note your assumption

Remember: Good requirements make implementation straightforward. Invest time here to save time later!