# CreateSequence Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/create-sequence
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake-Specific Sequence Features
```sql
-- Full Snowflake CREATE SEQUENCE syntax
CREATE [ OR REPLACE ] SEQUENCE [ IF NOT EXISTS ] <name>
  [ WITH ]
  [ START [ WITH ] [ = ] <initial_value> ]
  [ INCREMENT [ BY ] [ = ] <sequence_interval> ]
  [ { ORDER | NOORDER } ]
  [ COMMENT = '<string_literal>' ]
```

### Key Snowflake Differences
1. **ORDER/NOORDER**: Snowflake-specific feature for ordered sequence generation
2. **Simplified syntax**: No MINVALUE, MAXVALUE, CYCLE options in Snowflake
3. **Always 64-bit**: Sequences use full 64-bit range

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `createSequence`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| ordered | Generate ordered sequence values | Boolean | false | true/false | HIGH |
| noOrder | Explicitly set NOORDER | Boolean | false | true/false | HIGH |

### Why These Attributes?
- **ORDER**: Guarantees sequence values are generated in increasing order across sessions
- **NOORDER**: Better performance but values may not be sequential across sessions
- This is the primary Snowflake-specific feature for sequences (INT-151)

## 3. Mutual Exclusivity Rules

### Order Settings
- Cannot set both ordered=true and noOrder=true
- If neither specified, Snowflake default is NOORDER
- If ordered=true, generates ORDER keyword
- If noOrder=true, generates NOORDER keyword (explicit)

## 4. SQL Generation Examples

### Example 1: Ordered Sequence
```xml
<createSequence sequenceName="order_id_seq"
                startValue="1000"
                incrementBy="1"
                snowflake:ordered="true"/>
```

Generates:
```sql
CREATE SEQUENCE order_id_seq
  START WITH 1000
  INCREMENT BY 1
  ORDER;
```

### Example 2: Explicitly Unordered Sequence
```xml
<createSequence sequenceName="session_id_seq"
                startValue="1"
                incrementBy="1"
                snowflake:noOrder="true"/>
```

Generates:
```sql
CREATE SEQUENCE session_id_seq
  START WITH 1
  INCREMENT BY 1
  NOORDER;
```

### Example 3: Standard Sequence (No Snowflake Attributes)
```xml
<createSequence sequenceName="basic_seq"
                startValue="1"
                incrementBy="1"/>
```

Generates (standard Liquibase):
```sql
CREATE SEQUENCE basic_seq
  START WITH 1
  INCREMENT BY 1;
```

## 5. Implementation Approach

Using EXISTING_CHANGETYPE_EXTENSION_PATTERN.md:

1. **Storage**: Use existing `SnowflakeNamespaceAttributeStorage`
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to handle createSequence
3. **Generator**: Create `CreateSequenceGeneratorSnowflake` to add ORDER/NOORDER
4. **Testing**: Focus on ORDER behavior and mutual exclusivity

## 6. Test Scenarios

### Unit Tests
1. **Parser Tests**: 
   - Capture snowflake:ordered attribute
   - Capture snowflake:noOrder attribute
   - Handle missing attributes

2. **Generator Tests**:
   - Add ORDER when ordered=true
   - Add NOORDER when noOrder=true
   - Standard SQL when no attributes
   - Error when both attributes true

3. **Integration Tests**:
   - Full flow from XML to SQL
   - Multiple sequences with different settings

### Test Harness Tests
1. **createSequenceOrdered.xml** - Ordered sequence creation
2. **createSequenceNoOrder.xml** - Explicitly unordered sequence
3. **createSequenceMixed.xml** - Multiple sequences with different ORDER settings

## 7. Validation Rules

```java
public void validate(Map<String, String> attributes) {
    boolean ordered = "true".equals(attributes.get("ordered"));
    boolean noOrder = "true".equals(attributes.get("noOrder"));
    
    if (ordered && noOrder) {
        throw new ValidationFailedException(
            "Cannot specify both ordered='true' and noOrder='true'"
        );
    }
}
```

## 8. Expected Behaviors

### ORDER Behavior
- Guarantees monotonically increasing values
- May have performance impact
- Values are sequential even across sessions
- Useful for time-ordered data

### NOORDER Behavior
- Better performance
- Values unique but may have gaps
- Default Snowflake behavior
- Suitable for most use cases

### Liquibase Integration
- Works with standard createSequence attributes
- Ignores Snowflake attributes on non-Snowflake databases
- Maintains backward compatibility

## 9. Differences from Standard SQL

### Not Supported in Snowflake
- MINVALUE/MAXVALUE (always uses full 64-bit range)
- CYCLE/NOCYCLE (sequences don't cycle)
- CACHE (caching is automatic)
- OWNED BY (no ownership concept)

### Liquibase Handling
- These standard attributes are ignored for Snowflake
- Warning logged if unsupported attributes used
- Focus on Snowflake-specific ORDER feature

## 10. Error Conditions

1. **Mutual Exclusivity**: Both ordered and noOrder set to true
2. **Invalid Values**: Non-boolean values for ordered/noOrder
3. **Sequence Exists**: Without IF NOT EXISTS (standard error)

## 11. Performance Considerations

### ORDER Impact
- Slight performance decrease for ordered sequences
- Trade-off for guaranteed ordering
- Document in user guide

### Best Practices
- Use NOORDER (default) unless ordering required
- ORDER useful for audit trails, timestamps
- Consider application-level ordering instead

## 12. Documentation Requirements

### User Documentation
```xml
<!-- Example: Adding ORDER to sequences -->
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    
    <changeSet id="1" author="developer">
        <createSequence sequenceName="ordered_seq"
                       snowflake:ordered="true"/>
    </changeSet>
</databaseChangeLog>
```

### Migration Guide
- How to add ORDER to existing sequences
- When to use ORDER vs NOORDER
- Performance implications

## 13. Implementation Priority

This is a HIGH priority enhancement because:
1. Specifically requested in requirements (INT-151)
2. ORDER is unique to Snowflake
3. Affects sequence behavior significantly
4. Simple to implement with namespace pattern

## 14. Testing Notes

### Functional Testing
- Verify ORDER sequences maintain order across sessions
- Test high-concurrency sequence generation
- Validate sequence values are monotonic with ORDER

### Performance Testing
- Measure impact of ORDER on sequence generation
- Document performance differences
- Provide guidance on when to use ORDER