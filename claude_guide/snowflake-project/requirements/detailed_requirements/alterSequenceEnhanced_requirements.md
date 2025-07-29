# AlterSequence Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/alter-sequence
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake ALTER SEQUENCE Syntax
```sql
ALTER SEQUENCE [ IF EXISTS ] <name> RENAME TO <new_name>;

ALTER SEQUENCE [ IF EXISTS ] <name> SET
  [ INCREMENT [ BY ] <sequence_interval> ]
  [ { ORDER | NOORDER } ]
  [ COMMENT = '<comment>' ];

ALTER SEQUENCE [ IF EXISTS ] <name> UNSET COMMENT;
```

### Critical Limitation
**⚠️ IMPORTANT**: You can change from ORDER to NOORDER, but you **CANNOT** change from NOORDER back to ORDER. This is a one-way operation!

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `alterSequence`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| setNoOrder | Change sequence to NOORDER (one-way!) | Boolean | false | true/false | HIGH |
| setComment | Set sequence comment | String | null | Any string | LOW |
| unsetComment | Remove sequence comment | Boolean | false | true/false | LOW |

### Why Limited Attributes?
- Snowflake ALTER SEQUENCE is very limited compared to other objects
- Main feature is ORDER/NOORDER change (one-way only)
- Standard Liquibase handles INCREMENT changes
- No support for changing START WITH, MIN/MAX VALUES, etc.

## 3. Mutual Exclusivity Rules

### Comment Operations
- Cannot both setComment and unsetComment in same operation
- Either set a new comment or remove it, not both

### ORDER/NOORDER
- Only setNoOrder is provided (no setOrder)
- This enforces the one-way limitation in the API

## 4. SQL Generation Examples

### Example 1: Change to NOORDER (Performance Optimization)
```xml
<alterSequence sequenceName="high_volume_seq"
               snowflake:setNoOrder="true"/>
```

Generates:
```sql
ALTER SEQUENCE high_volume_seq SET NOORDER;
```

### Example 2: Add Comment
```xml
<alterSequence sequenceName="customer_id_seq"
               snowflake:setComment="Customer ID generator - do not modify"/>
```

Generates:
```sql
ALTER SEQUENCE customer_id_seq SET COMMENT = 'Customer ID generator - do not modify';
```

### Example 3: Standard + Snowflake Changes
```xml
<alterSequence sequenceName="order_seq"
               incrementBy="10"
               snowflake:setNoOrder="true"
               snowflake:setComment="Changed to NOORDER for performance"/>
```

Generates:
```sql
ALTER SEQUENCE order_seq SET 
  INCREMENT BY 10,
  NOORDER,
  COMMENT = 'Changed to NOORDER for performance';
```

### Example 4: Remove Comment
```xml
<alterSequence sequenceName="temp_seq"
               snowflake:unsetComment="true"/>
```

Generates:
```sql
ALTER SEQUENCE temp_seq UNSET COMMENT;
```

## 5. Implementation Approach

Using NAMESPACE_ATTRIBUTE_PATTERN_2.md:

1. **Storage**: Use existing `SnowflakeNamespaceAttributeStorage`
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to handle alterSequence
3. **Generator**: Create `AlterSequenceGeneratorSnowflake`
4. **Validation**: Enforce one-way NOORDER rule

## 6. Test Scenarios

### Unit Tests
1. **NOORDER Tests**:
   - setNoOrder=true generates NOORDER
   - Validate no reverse operation exists
   
2. **Comment Tests**:
   - Set comment with special characters
   - Unset comment
   - Mutual exclusivity validation

3. **Integration Tests**:
   - Combine with standard incrementBy
   - Multiple SET operations in one statement

### Test Harness Tests
1. **alterSequenceNoOrder.xml** - Change to NOORDER
2. **alterSequenceComment.xml** - Add/remove comments
3. **alterSequenceCombined.xml** - Mix standard and Snowflake attributes

## 7. Validation Rules

```java
public void validate(Map<String, String> attributes) {
    boolean setComment = attributes.get("setComment") != null;
    boolean unsetComment = "true".equals(attributes.get("unsetComment"));
    
    if (setComment && unsetComment) {
        throw new ValidationFailedException(
            "Cannot both set and unset comment in same operation"
        );
    }
    
    // Note: No validation needed for setNoOrder - it's one-way only
}
```

## 8. Critical User Warning

### ⚠️ NOORDER is Irreversible!
```xml
<!-- WARNING: This change CANNOT be undone! -->
<alterSequence sequenceName="my_seq" 
               snowflake:setNoOrder="true"/>
```

Once a sequence is changed to NOORDER:
- Cannot change back to ORDER
- Must DROP and recreate sequence to get ORDER back
- Document this clearly in changelogs

## 9. Expected Behaviors

### NOORDER Behavior
- Better performance for concurrent operations
- Values still unique but not guaranteed sequential
- Gaps in sequence more likely
- Cannot be reversed

### Comment Behavior
- Stored with sequence metadata
- Visible in SHOW SEQUENCES output
- Can be updated or removed
- No length limit specified

## 10. Error Conditions

1. **Sequence Doesn't Exist**: Error unless IF EXISTS used
2. **Invalid Comment**: Very long comments may fail
3. **Set and Unset Comment**: Validation error
4. **Attempting ORDER**: No API provided (prevents error)

## 11. Limitations to Document

### What You CAN'T Do with ALTER SEQUENCE
- Change START WITH value
- Modify MIN/MAX values (N/A in Snowflake anyway)
- Change from NOORDER to ORDER
- Reset sequence to specific value
- Change ownership

### Workarounds
- Drop and recreate for major changes
- Use separate SQL for special operations

## 12. Integration with Standard alterSequence

### Standard Attributes Supported
- `incrementBy` - Works normally
- `startWith` - NOT supported in ALTER
- `minValue/maxValue` - NOT supported in Snowflake

### Combined Operations
```sql
ALTER SEQUENCE my_seq SET
  INCREMENT BY 5,      -- Standard
  NOORDER,            -- Snowflake namespace
  COMMENT = 'Updated'; -- Snowflake namespace
```

## 13. Documentation Requirements

### User Guide Example
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <changeSet id="1" author="developer">
        <!-- ⚠️ WARNING: NOORDER cannot be reversed! -->
        <alterSequence sequenceName="high_concurrency_seq"
                      incrementBy="1"
                      snowflake:setNoOrder="true"
                      snowflake:setComment="Changed to NOORDER for performance"/>
    </changeSet>
</databaseChangeLog>
```

### Migration Guide
- Review sequences for ORDER/NOORDER decision
- NOORDER good for high-concurrency scenarios
- ORDER important for time-series or audit data
- Plan carefully - changes are permanent

## 14. Implementation Priority

This is MEDIUM priority because:
1. Limited functionality (few attributes)
2. One-way NOORDER is a footgun if not documented
3. Comments are nice-to-have
4. Most sequences work fine with defaults

## 15. Best Practices

### When to Use NOORDER
- High-concurrency inserts
- Performance is critical
- Gaps in sequence are acceptable
- Order doesn't matter for business logic

### When to Keep ORDER  
- Time-ordered data requirements
- Audit trail sequences
- Sequential invoice/document numbers
- Regulatory compliance needs

### Documentation in Changelog
- Always warn about NOORDER irreversibility
- Document WHY changing to NOORDER
- Consider rollback strategy (none!)