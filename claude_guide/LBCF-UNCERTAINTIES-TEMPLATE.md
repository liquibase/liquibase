# LBCF Uncertainties and Questions Template

## Purpose

This document template helps AI assistants identify and communicate uncertainties during database extension development. When uncertain about implementation details, stop and document the uncertainty here rather than making assumptions that could lead to incorrect implementations.

---

## Database Extension: [Database Name]

**Date**: [Current Date]
**Target Version**: Liquibase 4.33.0+
**Database Version**: [Database Version]
**Documentation URL**: [Vendor Documentation Link]

---

## Time Check
**Time spent before documenting uncertainty**: [X minutes]
**Attempts made**: [Count]
**Following 3-strike rule**: Yes/No

## Critical Uncertainties Requiring Resolution

### 1. Pattern Uncertainties

#### No Existing Pattern Found
- **Object Type**: [e.g., WAREHOUSE, FUNCTION, etc.]
- **Issue**: No similar implementation pattern exists in Liquibase
- **Options Considered**:
  1. Option A: [Describe approach]
  2. Option B: [Describe approach]
- **Recommendation**: [If any]
- **Information Needed**: Example of similar object implementation

#### Pattern Compatibility Unknown
- **Pattern Source**: [Where pattern was found]
- **Concern**: [Why pattern might not apply]
- **Specific Questions**:
  - [ ] Will this pattern work for [specific feature]?
  - [ ] How should [specific aspect] be handled?

### 2. Database-Specific Uncertainties

#### Syntax Ambiguity
- **Feature**: [Feature name]
- **Documentation Says**: "[Quote from docs]"
- **Ambiguity**: [What's unclear]
- **Examples Needed**:
  ```sql
  -- What I think it might be:
  CREATE OBJECT ... WITH OPTION = ?
  
  -- But could also be:
  CREATE OBJECT ... SET OPTION ?
  ```

#### Version-Specific Features
- **Feature**: [Feature name]
- **Uncertainty**: Available in which database versions?
- **Impact**: May need conditional implementation
- **Questions**:
  - [ ] Minimum supported database version?
  - [ ] How to detect feature availability?
  - [ ] Fallback approach for older versions?

### 3. Attribute Mapping Uncertainties

#### Complex Attribute Types
- **Database Attribute**: [Name]
- **Database Type**: [Type description]
- **Uncertainty**: How to map to Java type?
- **Options**:
  1. `String` - Simple but may need parsing
  2. `Map<String,String>` - More complex but structured
  3. Custom type - Most accurate but more work
- **Example from Database**:
  ```sql
  CREATE OBJECT WITH OPTION = 'key1=value1,key2=value2'
  ```

#### Attribute Interactions
- **Attributes**: [Attr1] and [Attr2]
- **Uncertainty**: Are these mutually exclusive?
- **Documentation Gap**: Docs don't specify interaction
- **Test Needed**: Try both together and see what happens

### 4. Validation Uncertainties

#### Validation Rules Unknown
- **Attribute**: [Name]
- **Question**: What are valid values?
- **Documentation Says**: [Quote or "Not documented"]
- **Observed in Examples**:
  - Example 1: [Value]
  - Example 2: [Value]
- **Need to Determine**:
  - [ ] Minimum/maximum values?
  - [ ] Allowed characters?
  - [ ] Reserved values?

#### Error Handling Unclear
- **Scenario**: [Describe scenario]
- **Question**: Should this fail or warn?
- **Database Behavior**: Unknown
- **Options**:
  1. Fail fast - Safer but may be too strict
  2. Warn and continue - More flexible but may hide issues

### 5. Implementation Uncertainties

#### Liquibase API Usage
- **API Component**: [Class/Interface name]
- **Uncertainty**: Correct usage pattern?
- **What I'm Trying**: [Code snippet]
- **Concern**: [Why it might be wrong]
- **Alternative Approaches**:
  1. [Approach 1]
  2. [Approach 2]

#### Service Registration
- **Component**: [Component type]
- **Uncertainty**: Additional registration needed?
- **Standard Registration**: [What's being done]
- **Question**: Any database-specific requirements?

### 6. Testing Uncertainties

#### Test Environment Setup
- **Requirement**: [What's needed]
- **Uncertainty**: How to configure?
- **Tried**: [What was attempted]
- **Error**: [Error message if any]
- **Questions**:
  - [ ] Special connection parameters needed?
  - [ ] Required database permissions?
  - [ ] Test data prerequisites?

#### Expected Behavior Unknown
- **Test Case**: [Describe test]
- **Uncertainty**: What should happen?
- **Database Docs Say**: [Quote or "Not documented"]
- **Need to Verify**: Actual database behavior

---

## Information Gathering Actions

### Documentation Research Needed
1. [ ] Search for: [Specific topic]
2. [ ] Look in: [Where to look]
3. [ ] Contact: [Who might know]

### Experiments to Run
1. [ ] Test: [What to test]
   - Setup: [How to set up test]
   - Expected: [What we hope to learn]
   
2. [ ] Test: [What to test]
   - Setup: [How to set up test]
   - Expected: [What we hope to learn]

### Code Examples to Find
1. [ ] Similar implementation in: [Where to look]
2. [ ] Pattern example for: [What pattern]
3. [ ] Database-specific example of: [What feature]

---

## Risk Assessment

### High Risk Areas
1. **Area**: [Description]
   - **Risk**: [What could go wrong]
   - **Impact**: [How bad would it be]
   - **Mitigation**: [How to reduce risk]

### Medium Risk Areas
1. **Area**: [Description]
   - **Risk**: [What could go wrong]
   - **Impact**: [How bad would it be]
   - **Mitigation**: [How to reduce risk]

---

## Recommendations

### Proceed With Caution
- Can implement: [What can be done now]
- Must verify: [What needs checking]
- Should postpone: [What to wait on]

### Need Human Input
1. **Decision Needed**: [Describe decision]
   - **Options**: [List options]
   - **Trade-offs**: [Describe trade-offs]
   - **Recommendation**: [If any]

2. **Clarification Needed**: [What needs clarifying]
   - **Current Understanding**: [What we think]
   - **Specific Question**: [Exact question]

---

## Resolution Tracking

### Resolved Items
1. **Question**: [Original question]
   - **Answer**: [Resolution]
   - **Source**: [How resolved]
   - **Date**: [When resolved]

### Pending Items
1. **Question**: [Question]
   - **Status**: [Waiting on what]
   - **Blocker**: [Yes/No]
   - **Workaround**: [If any]

---

## Implementation Impact

### Changes Needed Based on Uncertainties
1. **If [uncertainty] resolves to [option A]**:
   - Change needed in: [Where]
   - Impact: [What changes]
   
2. **If [uncertainty] resolves to [option B]**:
   - Change needed in: [Where]
   - Impact: [What changes]

### Deferred Implementations
1. **Feature**: [What's being deferred]
   - **Reason**: [Why deferred]
   - **Dependency**: [What it's waiting on]
   - **Priority**: [High/Medium/Low]

---

## Summary

### Can Proceed With
- [List what can be implemented now]

### Blocked On
- [List what's blocking progress]

### Next Steps
1. [Specific next action]
2. [Specific next action]
3. [Specific next action]

---

## Template Usage Notes

1. **When to Use**: Whenever implementation approach is unclear
2. **How to Use**: Fill in relevant sections, delete irrelevant ones
3. **Resolution**: Update as uncertainties are resolved
4. **Communication**: Share with human developer for decisions
5. **Documentation**: Keep as project history for future reference

Remember: **It's better to document uncertainty than to implement incorrectly**. Quality and correctness are paramount in the LBCF framework.