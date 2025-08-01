---
REQUIREMENTS_METADATA:
  REQUIREMENTS_VERSION: "3.0"
  PHASE: "PHASE_2_COMPLETE"
  STATUS: "IMPLEMENTATION_READY"
  RESEARCH_COMPLETION_DATE: "2025-08-01"
  IMPLEMENTATION_PATTERN: "Existing_Changetype_Extension"
  DATABASE_TYPE: "Snowflake"
  OBJECT_TYPE: "Sequence"
  OPERATION: "ALTER"
  NEXT_PHASE: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"
  ESTIMATED_IMPLEMENTATION_TIME: "3-4 hours"
---

# AlterSequence Enhanced Requirements (Snowflake Namespace Attributes)

## Executive Summary

This enhancement extends Liquibase's existing `alterSequence` changetype with Snowflake-specific namespace attributes to support sequence modification operations including the critical one-way ORDER to NOORDER transition and comment management, which are unique to Snowflake's sequence implementation.

## Snowflake SQL Research

### Official Documentation
- **Reference**: https://docs.snowflake.com/en/sql-reference/sql/alter-sequence
- **Version**: Snowflake 2024
- **Key Limitations**: One-way ORDER to NOORDER transition, limited modification options

### Full Snowflake ALTER SEQUENCE Syntax
```sql
-- Rename sequence
ALTER SEQUENCE [ IF EXISTS ] <name> RENAME TO <new_name>;

-- Set properties
ALTER SEQUENCE [ IF EXISTS ] <name> SET
  [ INCREMENT [ BY ] <sequence_interval> ]
  [ { ORDER | NOORDER } ]
  [ COMMENT = '<comment>' ];

-- Unset properties
ALTER SEQUENCE [ IF EXISTS ] <name> UNSET COMMENT;
```

### Critical Snowflake Limitation
**⚠️ ONE-WAY OPERATION**: You can change from ORDER to NOORDER, but you **CANNOT** change from NOORDER back to ORDER. This is a permanent, irreversible operation that requires careful consideration.

## Namespace Attribute Specification

### Required Snowflake Namespace Attributes

| Attribute | Description | Data Type | Default | Valid Values | Business Priority |
|-----------|-------------|-----------|---------|--------------|------------------|
| `setNoOrder` | Change sequence to NOORDER (IRREVERSIBLE) | Boolean | false | true/false | HIGH |
| `setComment` | Set sequence comment | String | null | Any string | MEDIUM |
| `unsetComment` | Remove sequence comment | Boolean | false | true/false | LOW |

### Attribute Behavior Definitions

#### NOORDER Transition (`setNoOrder="true"`)
- **Function**: Changes sequence from ORDER to NOORDER for better performance
- **⚠️ CRITICAL**: This is a **permanent, irreversible** operation
- **Performance**: Improves concurrency and sequence generation speed
- **Rollback**: **IMPOSSIBLE** - sequence cannot be changed back to ORDER
- **SQL Generated**: `SET NOORDER` in ALTER SEQUENCE statement

#### Comment Management (`setComment` and `unsetComment`)
- **setComment**: Adds or updates sequence comment for documentation
- **unsetComment**: Removes existing sequence comment
- **Use Cases**: Documentation, change tracking, metadata management
- **SQL Generated**: `SET COMMENT = 'value'` or `UNSET COMMENT`

## Business Rules and Validation

### Mutual Exclusivity Rules
- **Comment Rule**: Cannot specify both `setComment` and `unsetComment="true"`
- **Validation**: Pre-execution validation failure with clear error message
- **Rationale**: These operations are mutually exclusive in single ALTER statement

### Critical User Warnings
- **NOORDER Warning**: Must clearly communicate irreversible nature of ORDER to NOORDER change
- **Documentation Requirement**: All changelogs using `setNoOrder` must include warnings
- **Rollback Strategy**: Only option is DROP and recreate sequence

### Implementation Validation Logic
```java
public void validate(Map<String, String> attributes) {
    boolean setComment = attributes.get("setComment") != null;
    boolean unsetComment = "true".equals(attributes.get("unsetComment"));
    
    if (setComment && unsetComment) {
        throw new ValidationFailedException(
            "Cannot both set and unset comment in same alterSequence operation"
        );
    }
    
    // Log warning for irreversible NOORDER operation
    if ("true".equals(attributes.get("setNoOrder"))) {
        LogService.getLog(getClass()).warning(
            "WARNING: Setting NOORDER is IRREVERSIBLE. Sequence cannot be changed back to ORDER."
        );
    }
}
```

## XML Schema Examples

### Example 1: Irreversible Performance Optimization
```xml
<!-- ⚠️ WARNING: This change CANNOT be undone! -->
<alterSequence sequenceName="high_volume_seq"
               snowflake:setNoOrder="true"/>
```

**Generated SQL:**
```sql
ALTER SEQUENCE high_volume_seq SET NOORDER;
-- WARNING: This change is IRREVERSIBLE
```

### Example 2: Add Documentation Comment
```xml
<alterSequence sequenceName="customer_id_seq"
               snowflake:setComment="Customer ID generator - production use only"/>
```

**Generated SQL:**
```sql
ALTER SEQUENCE customer_id_seq SET COMMENT = 'Customer ID generator - production use only';
```

### Example 3: Combined Standard and Snowflake Operations
```xml
<alterSequence sequenceName="order_seq"
               incrementBy="10"
               snowflake:setNoOrder="true"
               snowflake:setComment="Changed to NOORDER for performance - 2025-08-01"/>
```

**Generated SQL:**
```sql
ALTER SEQUENCE order_seq SET 
  INCREMENT BY 10,
  NOORDER,
  COMMENT = 'Changed to NOORDER for performance - 2025-08-01';
```

### Example 4: Remove Comment
```xml
<alterSequence sequenceName="temp_seq"
               snowflake:unsetComment="true"/>
```

**Generated SQL:**
```sql
ALTER SEQUENCE temp_seq UNSET COMMENT;
```

## Technical Implementation Approach

### Implementation Pattern
Following the **Existing Changetype Extension Pattern**:

1. **Storage**: Utilize existing `SnowflakeNamespaceAttributeStorage` infrastructure
2. **Parser**: Extend `SnowflakeNamespaceAwareXMLParser` to capture alterSequence attributes
3. **Generator**: Create `AlterSequenceGeneratorSnowflake` extending base generator
4. **Validation**: Implement strict validation and warning system for irreversible operations

### Class Implementation Structure
```java
public class AlterSequenceGeneratorSnowflake extends AlterSequenceGenerator {
    
    @Override
    public Sql[] generateSql(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Get base SQL
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        // Add Snowflake-specific SET/UNSET operations
        return enhanceWithSnowflakeAttributes(baseSql, statement);
    }
    
    private Sql[] enhanceWithSnowflakeAttributes(Sql[] baseSql, AlterSequenceStatement statement) {
        // Implementation to add NOORDER, comment operations
        // Include warnings for irreversible operations
    }
}
```

## Testing Strategy

### Unit Test Coverage
1. **NOORDER Operation Tests**
   - Verify `setNoOrder="true"` generates correct SQL
   - Test warning generation for irreversible operation
   - Validate no reverse operation available

2. **Comment Management Tests**
   - SET COMMENT with various string values including special characters
   - UNSET COMMENT operation
   - Mutual exclusivity validation between set and unset

3. **Integration Tests**
   - Combination with standard `incrementBy` attribute
   - Multiple SET operations in single ALTER statement
   - Full XML-to-SQL generation flow

### Test Harness Integration Tests
1. **alterSequenceNoOrder.xml** - Verify NOORDER transition and performance impact
2. **alterSequenceComment.xml** - Comment addition and removal operations
3. **alterSequenceCombined.xml** - Mixed standard and Snowflake attributes
4. **alterSequenceWarnings.xml** - Verify warning generation and documentation

### Critical Testing Scenarios
- **Irreversibility Testing**: Verify NOORDER change cannot be reversed
- **Performance Testing**: Measure performance improvement after NOORDER change
- **Warning System**: Ensure all irreversible operations generate appropriate warnings

## Database Compatibility

### Snowflake-Specific Limitations
- **ORDER to NOORDER**: One-way operation only, cannot be reversed
- **Limited ALTER Options**: Cannot change START WITH, MIN/MAX values
- **No Sequence Reset**: Cannot reset sequence to specific value
- **Comment Length**: No specified limit but practical constraints apply

### Cross-Database Compatibility
- **Non-Snowflake Databases**: Snowflake namespace attributes are ignored
- **Migration Considerations**: Other databases may support bidirectional ORDER changes
- **Documentation**: Clear warnings about Snowflake-specific limitations

## Error Handling and Edge Cases

### Validation Errors
1. **Comment Conflicts**: Both `setComment` and `unsetComment` specified
2. **Invalid Comment Values**: Extremely long comments or invalid characters
3. **Sequence Not Found**: Standard error unless IF EXISTS used
4. **Permission Issues**: Insufficient privileges for sequence modification

### Runtime Considerations
- **Irreversible Operations**: Clear documentation and warnings required
- **Performance Impact**: Document performance changes after NOORDER transition
- **Monitoring**: Track sequence behavior changes post-modification

## Risk Management

### High-Risk Operations
1. **NOORDER Transition**: Permanent change requiring careful planning
   - **Mitigation**: Extensive documentation and warnings
   - **Rollback Plan**: DROP and recreate sequence (data loss possible)
   - **Testing**: Thorough testing in non-production environments

### Business Continuity Considerations
- **Change Planning**: NOORDER changes should be planned during maintenance windows
- **Documentation**: All changes must be documented with business justification
- **Monitoring**: Post-change monitoring for performance impact

## Documentation Requirements

### Critical User Warning Template
```xml
<!-- ⚠️ CRITICAL WARNING: NOORDER is IRREVERSIBLE! -->
<!-- This change CANNOT be undone without dropping and recreating the sequence -->
<alterSequence sequenceName="my_sequence" 
               snowflake:setNoOrder="true"
               snowflake:setComment="Changed to NOORDER for performance - contact DBA before modifying"/>
```

### Best Practices Guide
1. **When to Use NOORDER Transition**:
   - High-concurrency scenarios where performance is critical
   - Applications that can handle non-sequential ID gaps
   - After careful analysis of ordering requirements
   - When ORDER was mistakenly applied initially

2. **Before Making NOORDER Changes**:
   - Analyze application dependencies on sequence ordering
   - Test thoroughly in development environment
   - Document business justification for change
   - Plan rollback strategy (DROP/recreate if needed)
   - Coordinate with application teams

3. **Comment Management Best Practices**:
   - Document all sequence purpose and usage
   - Include change history in comments
   - Reference related applications or systems
   - Update comments when sequence behavior changes

## Business Impact Analysis

### High-Value Features
1. **Performance Optimization**: NOORDER transition for high-concurrency scenarios
2. **Documentation Enhancement**: Comment management for better sequence governance
3. **Change Tracking**: Ability to document sequence modifications

### Implementation Priority: HIGH
**Justification**:
1. NOORDER transition provides significant performance benefits
2. Limited Snowflake ALTER SEQUENCE functionality needs namespace support
3. Critical for sequences initially created with ORDER unnecessarily
4. Comment management improves sequence documentation and governance

### Risk Assessment: MEDIUM-HIGH
**Risk Factors**:
1. Irreversible NOORDER operation requires extensive user education
2. Potential for accidental performance-impacting changes
3. Need for clear documentation and warning systems

## Success Metrics

### Functional Success Criteria
- [ ] NOORDER transition generates correct SQL and warnings
- [ ] Comment management operations work correctly
- [ ] Mutual exclusivity validation prevents invalid configurations
- [ ] Integration with standard alterSequence attributes works seamlessly
- [ ] Warning system alerts users to irreversible operations

### Performance Success Criteria
- [ ] NOORDER transition shows measurable performance improvement
- [ ] No performance regression for sequences without Snowflake attributes
- [ ] Comment operations have minimal performance impact

### Quality Success Criteria
- [ ] 100% test coverage including edge cases
- [ ] All test harness scenarios pass
- [ ] Clear documentation with prominent warnings
- [ ] User education materials cover irreversible operations

## User Education and Change Management

### Required User Education
1. **Irreversible Operations**: Clear understanding of NOORDER permanence
2. **Performance Implications**: When and why to use NOORDER
3. **Rollback Strategies**: DROP/recreate as only option for ORDER restoration
4. **Best Practices**: Comment management and change documentation

### Change Management Process
1. **Pre-Change Analysis**: Review sequence usage and ordering requirements
2. **Impact Assessment**: Evaluate applications affected by sequence changes
3. **Testing Protocol**: Mandatory testing in non-production environments
4. **Documentation**: Required change documentation and approval process
5. **Monitoring**: Post-change performance and behavior monitoring

## Future Enhancement Opportunities

### Potential Expansions
1. **Sequence Analysis Tools**: Utilities to identify ORDER/NOORDER candidates
2. **Performance Monitoring**: Built-in metrics for sequence performance tracking
3. **Change Impact Analysis**: Tools to assess application impact of NOORDER changes
4. **Automated Testing**: Framework for testing sequence behavior changes

### Integration Points
- **Liquibase Pro**: Advanced sequence analysis and recommendations
- **Monitoring Systems**: Integration with database performance monitoring
- **Change Management**: Integration with enterprise change management systems

This enhancement provides essential Snowflake-specific sequence modification functionality while emphasizing the critical nature of irreversible operations and maintaining full backward compatibility.