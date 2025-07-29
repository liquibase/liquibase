# DropSequence Enhanced Requirements (Snowflake Namespace Attributes)

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: https://docs.snowflake.com/en/sql-reference/sql/drop-sequence
- Version: Snowflake 2024
- Last Updated: 2024-01-01

### Snowflake DROP SEQUENCE Syntax
```sql
DROP SEQUENCE [ IF EXISTS ] <sequence_name> [ CASCADE | RESTRICT ]
```

### ⚠️ CRITICAL: CASCADE and RESTRICT Are Non-Functional!

**Important**: While Snowflake accepts CASCADE and RESTRICT keywords for syntax consistency, **they have no actual effect**:
- **CASCADE**: Does NOT drop tables that use the sequence
- **RESTRICT**: Does NOT issue warnings if tables still use the sequence

This is documented behavior in Snowflake, unlike other databases where these options are functional.

## 2. Namespace Attribute Analysis

Attributes to add via `snowflake:` namespace to standard Liquibase `dropSequence`:

| Attribute | Description | Data Type | Default | Valid Values | Priority |
|-----------|-------------|-----------|---------|--------------|----------|
| cascade | Include CASCADE keyword (non-functional) | Boolean | false | true/false | LOW |
| restrict | Include RESTRICT keyword (non-functional) | Boolean | false | true/false | LOW |

### Why Include Non-Functional Attributes?
1. **Syntax Consistency** - Matches other Snowflake DROP operations
2. **Future Proofing** - If Snowflake implements functionality later
3. **Documentation** - Makes limitations explicit in changelogs
4. **Migration Compatibility** - Eases porting from other databases

## 3. Mutual Exclusivity Rules

### CASCADE vs RESTRICT
- Cannot specify both cascade=true and restrict=true
- If neither specified, uses Snowflake default behavior
- Both generate keywords but have no functional effect

## 4. SQL Generation Examples

### Example 1: Basic Drop
```xml
<dropSequence sequenceName="order_seq"/>
```

Generates:
```sql
DROP SEQUENCE order_seq;
```

### Example 2: Drop with CASCADE (Non-functional)
```xml
<dropSequence sequenceName="user_id_seq"
              snowflake:cascade="true"/>
```

Generates:
```sql
DROP SEQUENCE user_id_seq CASCADE;
-- WARNING: CASCADE has no effect in Snowflake
```

### Example 3: Drop with RESTRICT (Non-functional)
```xml
<dropSequence sequenceName="audit_seq"
              snowflake:restrict="true"/>
```

Generates:
```sql
DROP SEQUENCE audit_seq RESTRICT;
-- WARNING: RESTRICT has no effect in Snowflake
```

### Example 4: With IF EXISTS (standard Liquibase)
```xml
<dropSequence sequenceName="maybe_exists"
              snowflake:cascade="true"/>
```

Generates:
```sql
DROP SEQUENCE IF EXISTS maybe_exists CASCADE;
```

## 5. Implementation Approach

Using NAMESPACE_ATTRIBUTE_PATTERN_2.md:

1. **Storage**: Use existing `SnowflakeNamespaceAttributeStorage`
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to handle dropSequence
3. **Generator**: Create `DropSequenceGeneratorSnowflake` to add CASCADE/RESTRICT
4. **Documentation**: Add warnings about non-functionality

## 6. Test Scenarios

### Unit Tests
1. **Basic Tests**:
   - cascade=true generates CASCADE
   - restrict=true generates RESTRICT
   - No attributes uses default
   - Mutual exclusivity validation

2. **Warning Tests**:
   - Verify warning comments are added
   - Test documentation of limitations

### Test Harness Tests
1. **dropSequenceCascade.xml** - Drop with CASCADE (verify no effect)
2. **dropSequenceRestrict.xml** - Drop with RESTRICT (verify no effect)
3. **dropSequenceDefault.xml** - Default behavior test
4. **dropSequenceReferences.xml** - Test with table actually using sequence

## 7. Validation Rules

```java
public void validate(Map<String, String> attributes) {
    boolean cascade = "true".equals(attributes.get("cascade"));
    boolean restrict = "true".equals(attributes.get("restrict"));
    
    if (cascade && restrict) {
        throw new ValidationFailedException(
            "Cannot specify both cascade='true' and restrict='true'"
        );
    }
}
```

## 8. Expected Behaviors

### Default Behavior
- Sequence is dropped if it exists
- No checks for tables using the sequence
- No warnings about references
- Sequence can be undropped if within retention period

### CASCADE Behavior (Non-functional)
- Generates CASCADE keyword
- **Does NOT** drop tables using the sequence
- **Does NOT** cascade to any dependent objects
- Functionally identical to default drop

### RESTRICT Behavior (Non-functional)
- Generates RESTRICT keyword
- **Does NOT** check for table references
- **Does NOT** issue warnings about usage
- Functionally identical to default drop

## 9. Error Conditions

1. **Sequence Doesn't Exist**: Error unless IF EXISTS used
2. **Insufficient Privileges**: Need OWNERSHIP privilege
3. **Mutual Exclusivity**: Error if both cascade and restrict are true
4. **Invalid Sequence Name**: Standard identifier validation

## 10. Documentation Requirements

### Critical User Warning
```xml
<!-- WARNING: CASCADE and RESTRICT have no effect in Snowflake! -->
<dropSequence sequenceName="my_seq" 
              snowflake:cascade="true"/>
<!-- Tables using this sequence will NOT be affected -->
```

### Migration Notes
- When porting from other databases, CASCADE/RESTRICT may need manual handling
- Review all tables using sequences before dropping
- Consider using comments to document intended behavior

## 11. Comparison with Other Databases

### PostgreSQL
- CASCADE: Drops dependent objects (tables with DEFAULT)
- RESTRICT: Fails if dependent objects exist

### Oracle
- No CASCADE/RESTRICT for sequences
- Must manually handle dependencies

### Snowflake
- Accepts keywords for compatibility
- No functional implementation
- Manual dependency management required

## 12. Best Practices

### Before Dropping Sequences
1. **Identify Usage**:
   ```sql
   -- Find tables using the sequence
   SHOW TABLES;
   DESC TABLE table_name; -- Check for sequence references
   ```

2. **Document Dependencies**:
   - List tables using the sequence
   - Note if sequence is used in DEFAULT values
   - Plan migration strategy

3. **Safe Drop Process**:
   - Remove sequence references from tables first
   - Then drop the sequence
   - Use transactions where possible

## 13. Implementation Priority

This is **LOW priority** because:
1. CASCADE/RESTRICT are non-functional
2. Standard dropSequence works fine
3. Mainly for syntax consistency
4. Easy to implement when other namespace attributes are done

## 14. Testing Notes

### Functional Testing
- Verify CASCADE doesn't affect dependent tables
- Confirm RESTRICT doesn't prevent drop with dependencies
- Test that keywords are generated correctly

### Documentation Testing
- Ensure warnings are clear about non-functionality
- Verify migration documentation is accurate
- Test that examples work as expected

## 15. Future Considerations

### If Snowflake Implements Functionality
- Attributes already in place
- Would need to update documentation
- Remove "non-functional" warnings
- Add proper dependency handling

### Alternative Approaches
- Could implement custom dependency checking
- Add warnings for detected usage
- Provide migration assistance

**Note**: This enhancement is primarily for completeness and future-proofing rather than functional necessity.