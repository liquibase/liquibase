---
REQUIREMENTS_METADATA:
  REQUIREMENTS_VERSION: "3.0"
  PHASE: "PHASE_2_COMPLETE"
  STATUS: "IMPLEMENTATION_READY"
  RESEARCH_COMPLETION_DATE: "2025-08-01"
  IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
  DATABASE_TYPE: "Snowflake"
  OBJECT_TYPE: "Sequence"
  OPERATION: "CREATE"
  NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
  ESTIMATED_IMPLEMENTATION_TIME: "3-4 hours"
---

# CreateSequence Enhanced Requirements (Snowflake Namespace Attributes)

## Executive Summary

This enhancement extends Liquibase's existing `createSequence` changetype with Snowflake-specific namespace attributes to support the ORDER/NOORDER sequence behavior, which is unique to Snowflake and affects sequence value generation performance and ordering guarantees.

## Snowflake SQL Research

### Official Documentation
- **Reference**: https://docs.snowflake.com/en/sql-reference/sql/create-sequence
- **Version**: Snowflake 2024
- **Key Snowflake Features**: ORDER/NOORDER behavior for sequence value generation

### Full Snowflake CREATE SEQUENCE Syntax
```sql
CREATE [ OR REPLACE ] SEQUENCE [ IF NOT EXISTS ] <name>
  [ WITH ]
  [ START [ WITH ] [ = ] <initial_value> ]
  [ INCREMENT [ BY ] [ = ] <sequence_interval> ]
  [ { ORDER | NOORDER } ]
  [ COMMENT = '<string_literal>' ]
```

### Snowflake-Specific Differences
1. **ORDER/NOORDER**: Unique to Snowflake - controls sequence value ordering across sessions
2. **Simplified Options**: No MINVALUE, MAXVALUE, CYCLE options (always 64-bit range)
3. **Performance Trade-off**: ORDER guarantees sequential values but impacts performance

## Namespace Attribute Specification

### Required Snowflake Namespace Attributes

| Attribute | Description | Data Type | Default | Valid Values | Business Priority |
|-----------|-------------|-----------|---------|--------------|------------------|
| `ordered` | Generate ordered sequence values | Boolean | false | true/false | HIGH |
| `noOrder` | Explicitly set NOORDER (performance) | Boolean | false | true/false | HIGH |

### Attribute Behavior Definitions

#### ORDER Behavior (`ordered="true"`)
- **Function**: Guarantees monotonically increasing sequence values across all sessions
- **Performance**: Slight performance decrease due to coordination overhead
- **Use Cases**: Audit trails, time-ordered data, sequential document numbers
- **SQL Generated**: `ORDER` keyword added to CREATE SEQUENCE statement

#### NOORDER Behavior (`noOrder="true"`)
- **Function**: Better performance, values unique but may have gaps between sessions
- **Performance**: Optimal for high-concurrency scenarios
- **Use Cases**: General ID generation where ordering is not critical
- **SQL Generated**: `NOORDER` keyword added to CREATE SEQUENCE statement

#### Default Behavior (no attributes)
- **Function**: Uses Snowflake default (NOORDER)
- **SQL Generated**: Standard CREATE SEQUENCE without ORDER/NOORDER keywords

## Business Rules and Validation

### Mutual Exclusivity Rules
- **Rule**: Cannot specify both `ordered="true"` and `noOrder="true"`
- **Validation**: Pre-execution validation failure with clear error message
- **Rationale**: These options are mutually exclusive in Snowflake SQL

### Implementation Validation Logic
```java
public void validate(Map<String, String> attributes) {
    boolean ordered = "true".equals(attributes.get("ordered"));
    boolean noOrder = "true".equals(attributes.get("noOrder"));
    
    if (ordered && noOrder) {
        throw new ValidationFailedException(
            "Cannot specify both ordered='true' and noOrder='true' for createSequence"
        );
    }
}
```

## XML Schema Examples

### Example 1: Ordered Sequence for Audit Trail
```xml
<createSequence sequenceName="audit_log_seq"
                startValue="1000"
                incrementBy="1"
                snowflake:ordered="true"/>
```

**Generated SQL:**
```sql
CREATE SEQUENCE audit_log_seq
  START WITH 1000
  INCREMENT BY 1
  ORDER;
```

### Example 2: High-Performance Unordered Sequence
```xml
<createSequence sequenceName="session_id_seq"
                startValue="1"
                incrementBy="1"
                snowflake:noOrder="true"/>
```

**Generated SQL:**
```sql
CREATE SEQUENCE session_id_seq
  START WITH 1
  INCREMENT BY 1
  NOORDER;
```

### Example 3: Standard Sequence (No Snowflake Extensions)
```xml
<createSequence sequenceName="basic_seq"
                startValue="1"
                incrementBy="1"/>
```

**Generated SQL:**
```sql
CREATE SEQUENCE basic_seq
  START WITH 1
  INCREMENT BY 1;
```

## Technical Implementation Approach

### Implementation Pattern
Following the **Existing Changetype Extension Pattern**:

1. **Storage**: Utilize existing `SnowflakeNamespaceAttributeStorage` infrastructure
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to capture createSequence attributes
3. **Generator**: Create `CreateSequenceGeneratorSnowflake` extending base generator
4. **Integration**: Seamless integration with existing createSequence changetype

### Class Implementation Structure
```java
public class CreateSequenceGeneratorSnowflake extends CreateSequenceGenerator {
    
    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Get base SQL
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        // Add Snowflake-specific ORDER/NOORDER keywords
        return enhanceWithSnowflakeAttributes(baseSql, statement);
    }
    
    private Sql[] enhanceWithSnowflakeAttributes(Sql[] baseSql, CreateSequenceStatement statement) {
        // Implementation to add ORDER/NOORDER keywords
    }
}
```

## Testing Strategy

### Unit Test Coverage
1. **Attribute Parsing Tests**
   - Verify `snowflake:ordered` attribute capture
   - Verify `snowflake:noOrder` attribute capture
   - Test missing attributes (default behavior)

2. **SQL Generation Tests**
   - ORDER keyword generation when `ordered="true"`
   - NOORDER keyword generation when `noOrder="true"`
   - Standard SQL when no Snowflake attributes
   - Validation error when both attributes specified

3. **Integration Tests**
   - Full XML-to-SQL generation flow
   - Multiple sequences with different ORDER settings
   - Integration with standard createSequence attributes

### Test Harness Integration Tests
1. **createSequenceOrdered.xml** - Verify ordered sequence creation and behavior
2. **createSequenceNoOrder.xml** - Verify explicitly unordered sequence
3. **createSequenceMixed.xml** - Multiple sequences with different ORDER configurations

### Functional Test Scenarios
- Verify ORDER sequences maintain monotonic ordering across concurrent sessions
- Test high-concurrency sequence generation performance differences
- Validate sequence behavior under load

## Database Compatibility

### Snowflake-Specific Behavior
- **ORDER**: Sequence values generated in strict ascending order across all sessions
- **NOORDER**: Better performance, values unique but gaps possible across sessions
- **Default**: Snowflake uses NOORDER behavior when neither specified

### Cross-Database Compatibility
- **Non-Snowflake Databases**: Snowflake namespace attributes are ignored
- **Backward Compatibility**: Existing changelogs work without modification
- **Migration Path**: Easy addition of ORDER behavior to existing sequences

## Error Handling and Edge Cases

### Validation Errors
1. **Mutual Exclusivity**: Both `ordered` and `noOrder` set to true
2. **Invalid Values**: Non-boolean values for ordered/noOrder attributes
3. **Sequence Exists**: Standard Liquibase error handling (without IF NOT EXISTS)

### Runtime Considerations
- **Performance Impact**: Document ORDER performance implications
- **Resource Usage**: ORDER sequences may use additional coordination resources
- **Monitoring**: Sequence generation rate monitoring for ORDER vs NOORDER

## Documentation Requirements

### User Documentation Template
```xml
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    
    <changeSet id="audit-sequence" author="developer">
        <createSequence sequenceName="audit_trail_seq"
                       startValue="1"
                       incrementBy="1"
                       snowflake:ordered="true"/>
    </changeSet>
</databaseChangeLog>
```

### Best Practices Guide
1. **When to Use ORDER**:
   - Audit trail sequences requiring strict ordering
   - Time-series data with ordering requirements
   - Sequential document/invoice numbering
   - Regulatory compliance scenarios

2. **When to Use NOORDER**:
   - High-concurrency insert scenarios
   - Performance-critical applications
   - General ID generation where gaps are acceptable
   - Default recommendation for most use cases

3. **Performance Considerations**:
   - ORDER adds coordination overhead
   - NOORDER provides better concurrency
   - Monitor sequence generation rates in production

## Business Impact Analysis

### High-Value Features
1. **Snowflake Optimization**: Leverages Snowflake-specific performance features
2. **Ordering Guarantees**: Provides strict ordering when business logic requires it
3. **Performance Tuning**: Allows optimization for high-concurrency scenarios

### Implementation Priority: HIGH
**Justification**:
1. Specifically requested in requirements (INT-151)
2. ORDER/NOORDER is unique to Snowflake
3. Significantly affects sequence behavior and performance
4. Relatively simple implementation using existing namespace pattern
5. Provides immediate value for Snowflake users

## Success Metrics

### Functional Success Criteria
- [ ] ORDER sequences maintain strict ascending order across sessions
- [ ] NOORDER sequences provide better performance in high-concurrency tests
- [ ] Mutual exclusivity validation prevents invalid configurations
- [ ] Integration with existing createSequence attributes works seamlessly

### Performance Success Criteria
- [ ] NOORDER sequences show measurable performance improvement over ORDER
- [ ] ORDER sequences maintain acceptable performance for typical use cases
- [ ] No performance regression for sequences without Snowflake attributes

### Quality Success Criteria
- [ ] 100% test coverage for new functionality
- [ ] All test harness scenarios pass
- [ ] Documentation covers all use cases and best practices
- [ ] Backward compatibility maintained

## Future Enhancement Opportunities

### Potential Expansions
1. **Sequence Monitoring**: Add metrics for ORDER vs NOORDER performance
2. **Migration Tools**: Utilities to convert between ORDER and NOORDER
3. **Best Practice Analysis**: Tools to recommend ORDER vs NOORDER based on usage patterns

### Integration Points
- **Liquibase Pro**: Potential for advanced sequence analysis features
- **Monitoring Tools**: Integration with database performance monitoring
- **Migration Utilities**: Support for bulk sequence optimization

This enhancement provides essential Snowflake-specific sequence functionality while maintaining full backward compatibility and following established Liquibase extension patterns.