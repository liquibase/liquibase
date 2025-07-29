# Improvements to the Detailed Requirements Creation Guide

## Based on Real-World Experience (2025-01-29)

After creating 14 requirements documents, here are critical improvements needed:

## 1. Add Systematic Discovery Section

### The Problem
The current guide doesn't emphasize checking ALL related SQL commands for an object type. This led to missing:
- ALTER TABLE Snowflake-specific features
- DROP TABLE CASCADE/RESTRICT 
- ALTER SEQUENCE ORDER/NOORDER

### Proposed Addition: "Systematic SQL Command Discovery"

```markdown
## Systematic SQL Command Discovery (NEW SECTION)

Before creating requirements, you MUST check ALL SQL commands for the object type:

### For Each Object Type, Research:
1. **CREATE** - All creation options
2. **ALTER** - ALL modification capabilities
3. **DROP** - Cascade behaviors, restrictions
4. **SHOW/DESCRIBE** - Metadata implications

### Discovery Checklist
- [ ] CREATE <object> - Document all options
- [ ] ALTER <object> - Check EVERY alteration type
- [ ] DROP <object> - Note CASCADE/RESTRICT/PURGE options
- [ ] Related commands (e.g., UNDROP, CLONE)

### Example: TABLE Object Discovery
1. CREATE TABLE - Check for:
   - Storage options (TRANSIENT, TEMPORARY)
   - Clustering keys
   - Data retention
   - Schema evolution
   
2. ALTER TABLE - Check for:
   - ADD/DROP/MODIFY columns
   - Clustering changes
   - Policy attachments
   - Property modifications
   
3. DROP TABLE - Check for:
   - CASCADE vs RESTRICT
   - Default behaviors
   - Recovery options
```

## 2. Add "Namespace vs New Change Type" Decision Guide

### The Problem
It's not always clear when to use namespace attributes vs creating new change types.

### Proposed Addition:

```markdown
## Namespace Attributes vs New Change Types

### Use Namespace Attributes When:
1. Enhancing existing Liquibase change types
2. Adding database-specific options to standard SQL
3. The core SQL command is the same

Examples:
- snowflake:transient on createTable
- snowflake:cascade on dropTable
- snowflake:ordered on createSequence

### Create New Change Types When:
1. SQL command doesn't exist in standard Liquibase
2. Object type is database-specific
3. Significantly different syntax structure

Examples:
- createWarehouse (Snowflake-specific)
- createSchema (not in older Liquibase)
- alterWarehouse (unique operations)
```

## 3. Emphasize Cross-Checking Implementation

### The Problem
Requirements were created without checking existing implementation, missing attributes like:
- pipeExecutionPaused in CreateSchema
- maxConcurrencyLevel in CreateWarehouse

### Proposed Addition:

```markdown
## Implementation Cross-Check (CRITICAL)

BEFORE finalizing requirements:

1. **Check Existing Code**:
   ```bash
   # Search for existing implementations
   grep -r "class.*<ChangeType>.*Change" src/
   ```

2. **Compare Attributes**:
   - List all attributes in implementation
   - List all attributes in documentation
   - Identify gaps in both directions

3. **Update Requirements**:
   - Add missing documented attributes
   - Note implementation-specific attributes
   - Document why differences exist
```

## 4. Add Version-Aware Research

### The Problem
Snowflake features evolve rapidly. Requirements should note version-specific features.

### Proposed Addition:

```markdown
## Version-Specific Features

### Document Version Information
1. Minimum version required for each feature
2. Deprecated features and alternatives
3. Preview features that may change

### Example Format
| Feature | Since Version | Status | Notes |
|---------|--------------|---------|-------|
| ENABLE_SCHEMA_EVOLUTION | 2023.x | GA | Default false |
| maxConcurrencyLevel | 2024.x | GA | Enterprise only |
| CREATE OR REPLACE | 2022.x | GA | Not for all objects |
```

## 5. Add "Related Operations" Section

### The Problem
Focusing on one operation misses related operations that need similar treatment.

### Proposed Addition:

```markdown
## Related Operations Analysis

For each change type, document:

### Direct Relations
- CREATE → ALTER, DROP
- ALTER → What can/cannot be changed
- DROP → Recovery options, cascade effects

### Indirect Relations  
- Impact on child objects
- Permission requirements
- Rollback possibilities

### Example: createTable Relations
- ALTER TABLE - Which properties can change?
- DROP TABLE - CASCADE implications?
- TRUNCATE TABLE - Separate operation?
- CREATE OR REPLACE TABLE - Supported?
```

## 6. Improve Test Scenario Planning

### The Problem  
Test scenarios section is too generic. Need specific guidance.

### Proposed Addition:

```markdown
## Comprehensive Test Scenario Planning

### Test Categories
1. **Basic Operations** - Minimal syntax
2. **Feature Variations** - Each optional feature
3. **Mutual Exclusivity** - Conflicting options
4. **Edge Cases** - Limits, special characters
5. **Error Cases** - Invalid combinations
6. **Integration** - With other changes

### Test File Naming Convention
- Basic: <changeType>.xml
- Variations: <changeType><Feature>.xml
- Errors: <changeType>ValidationErrors.xml

### Coverage Checklist
- [ ] Every attribute tested at least once
- [ ] All mutual exclusions verified
- [ ] Error messages documented
- [ ] Rollback tested where applicable
```

## 7. Add Common Pitfalls Section

### Proposed Addition:

```markdown
## Common Requirements Pitfalls

### Pitfalls to Avoid
1. **Assuming Standard SQL** - Check vendor-specific syntax
2. **Missing Related Operations** - Always check ALTER/DROP
3. **Ignoring Defaults** - Document default behaviors
4. **Version Assumptions** - Note version requirements
5. **One-Way Operations** - Highlight irreversible changes

### Red Flags to Investigate
- "CASCADE" behavior differences
- "IF EXISTS" support variations  
- Default values that differ from standard
- Operations that can't be rolled back
```

## Summary of Improvements

1. **Systematic Discovery** - Check ALL SQL commands
2. **Decision Guide** - When to use namespaces vs new types
3. **Implementation Check** - Verify against existing code
4. **Version Awareness** - Track feature availability
5. **Related Operations** - Consider full lifecycle
6. **Better Test Planning** - Specific categories and coverage
7. **Common Pitfalls** - Learn from experience

These improvements would have caught the missing requirements earlier in the process.