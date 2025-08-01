# Detailed Requirements Creation Guide
## AI-Optimized Requirements Research and Documentation Protocol

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: REQUIREMENTS_CREATION
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition through systematic research"
  - "Complete SQL test statements through comprehensive examples"
  - "Unit tests complete string comparison through exact specifications"
  - "Integration tests ALL generated SQL through comprehensive test scenarios"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/requirements_creation.md"
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - master_process_loop.md: "Master process for all implementations"
  - changetype_patterns.md: "Implementation patterns using requirements"
  - sql_generator_overrides.md: "SQL syntax requirements"
  - test_harness_guide.md: "Testing requirements validation"

RELATED_GUIDES:
  - "../snapshot_diff_implementation/ai_quickstart.md": "Sequential execution patterns"
  - "../snapshot_diff_implementation/error_patterns_guide.md": "Requirements validation debugging"
```

## Overview

Before implementing any database change type, you MUST create a detailed requirements document by researching the database vendor's official documentation. This guide shows you how to create comprehensive requirements that will drive your implementation and prevent goalpost changing.

## When to Create Requirements

```yaml
CREATE_REQUIREMENTS_WHEN:
  1. "Implementing new change type that doesn't exist"
  2. "Adding database-specific features to existing change type"
  3. "Discovering undocumented behavior during implementation"
  4. "Before ANY coding begins"
  
DO_NOT_PROCEED_WITHOUT:
  - "Complete requirements document"
  - "Official documentation research"
  - "Comprehensive SQL syntax examples"
  - "Complete attribute analysis"
```

## Requirements Document Location

**Mandatory Location Pattern:**
```
claude_guide/[project]/requirements/[changeTypeName]_requirements.md
```

**Examples:**
- `createSchema_requirements.md`
- `dropWarehouse_requirements.md` 
- `alterSequence_requirements.md`

## Sequential Blocking Requirements Creation Process

### STEP 1: Official Documentation Research - **COMPLETE SYNTAX DEFINITION**
```yaml
STEP_ID: REQ_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

#### BLOCKING_VALIDATION_1.1: Official Documentation Located and Analyzed
```yaml
REQUIREMENT: "Official database vendor documentation researched and documented"
VALIDATION_CRITERIA:
  - "Official documentation URL included"
  - "Database version specified"
  - "Complete syntax documented from official source"
  - "All optional clauses identified"
FAILURE_ACTION: "STOP - Complete official documentation research"
```

**Research Process:**
1. **Find Official Documentation**
   - Search for "[database] CREATE [object] syntax"
   - Look for vendor's official SQL reference
   - Check version-specific documentation
   - Note the documentation URL and version

2. **Document Complete Syntax**
   - Copy the full BNF/syntax diagram
   - Include both minimal and complete syntax
   - Note all optional clauses and their order
   - Identify any version-specific features

**Research Commands:**
```bash
# Search for existing documentation patterns
find . -name "*requirements*.md" | head -5
grep -A 10 -B 10 "Official Documentation" */requirements/*.md
```

### STEP 2: Complete SQL Syntax Documentation - **COMPLETE SQL TEST STATEMENTS**
```yaml
STEP_ID: REQ_2.0
STATUS: BLOCKED
PREREQUISITES: [REQ_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

#### BLOCKING_VALIDATION_2.1: Complete SQL Syntax Documented
```yaml
REQUIREMENT: "All SQL syntax variations documented with complete examples"
VALIDATION_CRITERIA:
  - "Minimal syntax example provided"
  - "Complete syntax with all options provided"
  - "All SQL operations documented (CREATE, ALTER, DROP, SHOW)"
  - "Edge cases and special syntax noted"
FAILURE_ACTION: "STOP - Document complete SQL syntax examples"
```

### STEP 3: Comprehensive Attribute Analysis - **COMPLETE SYNTAX DEFINITION**
```yaml
STEP_ID: REQ_3.0
STATUS: BLOCKED
PREREQUISITES: [REQ_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

#### BLOCKING_VALIDATION_3.1: Complete Attribute Table Created
```yaml
REQUIREMENT: "All parameters documented in comprehensive attribute table"
VALIDATION_CRITERIA:
  - "All parameters from syntax included"
  - "Data types specified for each parameter"
  - "Default values documented"
  - "Valid values or ranges specified"
  - "Required/optional status indicated"
  - "Constraints and limitations noted"
FAILURE_ACTION: "STOP - Complete attribute analysis table"
```

**Attribute Analysis Process:**
For each parameter in the syntax:
1. **Attribute**: The parameter name as it will appear in Liquibase
2. **Description**: What the parameter does
3. **Data Type**: String, Boolean, Integer, etc.
4. **Default**: Database default value if not specified
5. **Valid Values**: Acceptable values or ranges
6. **Required**: Yes/No
7. **Constraints**: Any limitations or business rules

### STEP 4: Property Categorization - **UNIT TESTS COMPLETE STRING COMPARISON**
```yaml
STEP_ID: REQ_4.0
STATUS: BLOCKED
PREREQUISITES: [REQ_3.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

#### BLOCKING_VALIDATION_4.1: Properties Categorized for Testing
```yaml
REQUIREMENT: "All properties categorized for appropriate testing strategies"
VALIDATION_CRITERIA:
  - "Required properties identified"
  - "Optional configuration properties identified"  
  - "State properties (read-only) identified and marked for exclusion"
  - "SQL test requirements specified for each category"
FAILURE_ACTION: "STOP - Complete property categorization"
```

**Property Categories:**

**Required Properties:**
- Must be specified at object creation
- MUST be included in all SQL generation
- MUST be tested in unit tests with complete string comparison

**Optional Configuration Properties:**
- Can be set/changed by user
- MUST be included in SQL when specified
- MUST be tested in all combinations

**State Properties (Read-Only):**
- Runtime information, read-only
- **CRITICAL**: EXCLUDE FROM COMPARISONS
- Do not affect SQL generation
- Should not be in unit test comparisons

### STEP 5: Mutual Exclusivity Analysis - **INTEGRATION TESTS ALL SQL**
```yaml
STEP_ID: REQ_5.0
STATUS: BLOCKED
PREREQUISITES: [REQ_4.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_5.1: Mutual Exclusivity Rules Documented
```yaml
REQUIREMENT: "All mutual exclusivity and combination rules identified"
VALIDATION_CRITERIA:
  - "Mutually exclusive combinations identified"
  - "Required combinations identified"
  - "Test scenarios planned based on exclusivity rules"
  - "Separate test files planned for incompatible features"
FAILURE_ACTION: "STOP - Complete mutual exclusivity analysis"
```

**Mutual Exclusivity Analysis:**
1. Look for "cannot be used with" in documentation
2. Test combinations in database to verify
3. Document which attributes conflict
4. Plan separate test files for incompatible features

### STEP 6: Complete SQL Examples Creation - **COMPLETE SQL TEST STATEMENTS**
```yaml
STEP_ID: REQ_6.0
STATUS: BLOCKED
PREREQUISITES: [REQ_5.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

#### BLOCKING_VALIDATION_6.1: Comprehensive SQL Examples Provided
```yaml
REQUIREMENT: "Complete SQL examples covering all scenarios"
VALIDATION_CRITERIA:
  - "At least 5 SQL examples provided"
  - "Examples cover all property combinations"
  - "Examples include edge cases"
  - "Examples are complete and executable"
  - "Examples demonstrate mutual exclusivity rules"
FAILURE_ACTION: "STOP - Create comprehensive SQL examples"
```

**SQL Example Requirements:**
1. Start with the simplest valid SQL
2. Add examples for each feature
3. Create examples that combine compatible features
4. Include edge cases and maximum complexity
5. **CRITICAL**: All examples must be complete and executable

### STEP 7: Test Scenario Planning - **INTEGRATION TESTS ALL SQL**
```yaml
STEP_ID: REQ_7.0
STATUS: BLOCKED
PREREQUISITES: [REQ_6.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

#### BLOCKING_VALIDATION_7.1: Complete Test Scenarios Planned
```yaml
REQUIREMENT: "Test scenarios cover all SQL generation possibilities"
VALIDATION_CRITERIA:
  - "Test scenarios based on mutual exclusivity rules"
  - "Separate test files planned for incompatible features"
  - "Positive and negative test cases identified"
  - "Edge cases and boundary conditions included"
  - "Complete SQL coverage ensured"
FAILURE_ACTION: "STOP - Plan comprehensive test scenarios"
```

**Test Scenario Planning:**
Based on mutual exclusivity:
1. Group compatible features together
2. Create separate test files for incompatible features
3. Plan for both positive and negative test cases
4. Ensure every SQL generation path is tested

### STEP 8: Validation Rules Definition - **COMPLETE VALIDATION**
```yaml
STEP_ID: REQ_8.0
STATUS: BLOCKED
PREREQUISITES: [REQ_7.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition through comprehensive validation"
```

#### BLOCKING_VALIDATION_8.1: Complete Validation Rules Documented
```yaml
REQUIREMENT: "All validation rules comprehensively documented"
VALIDATION_CRITERIA:
  - "Required field validation rules specified"
  - "Value range validation rules specified"
  - "Mutual exclusivity validation rules specified"
  - "Format/pattern validation rules specified"
  - "Business rule validation specified"
FAILURE_ACTION: "STOP - Document complete validation rules"
```

**Validation Rule Types:**
1. **Required field validation**: What fields cannot be null/empty
2. **Value range validation**: Numeric ranges, string lengths
3. **Mutual exclusivity validation**: Conflicting attribute combinations
4. **Format/pattern validation**: Valid identifiers, formats
5. **Business rule validation**: Database-specific constraints

## Requirements Document Template

```markdown
# [ChangeType] Detailed Requirements Specification

## METADATA
```yaml
DOCUMENT_VERSION: "1.0"
DATABASE_TYPE: "[Database Name]"
CHANGE_TYPE: "[changeTypeName]"
CREATED_DATE: "[Date]"
OFFICIAL_DOCUMENTATION_URL: "[URL]"
DOCUMENTATION_VERSION: "[Version]"
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition: [How this addresses it]"
  - "Complete SQL test statements: [How this addresses it]"
  - "Unit tests complete string comparison: [How this addresses it]"
  - "Integration tests ALL generated SQL: [How this addresses it]"
```

## 1. SQL Syntax Research

### Official Documentation Reference
- **URL**: [Link to vendor documentation]
- **Version**: [Database version]
- **Last Updated**: [Date]
- **Section**: [Specific section/page reference]

### Basic Syntax
```sql
-- Minimal syntax
CREATE [OBJECT_TYPE] object_name;

-- Complete syntax with all options
CREATE [OR REPLACE] [TRANSIENT] [OBJECT_TYPE] [IF NOT EXISTS] 
  object_name
  [PROPERTY1 = value1]
  [PROPERTY2 = value2]
  [WITH option1 = value1, option2 = value2]
  [COMMENT = 'comment_text'];
```

### Alternative Operations
```sql
-- ALTER syntax
ALTER [OBJECT_TYPE] object_name SET property = value;

-- DROP syntax  
DROP [OBJECT_TYPE] [IF EXISTS] object_name [CASCADE | RESTRICT];

-- SHOW/DESCRIBE syntax
SHOW [OBJECT_TYPE]S [LIKE 'pattern'];
DESCRIBE [OBJECT_TYPE] object_name;
```

## 2. Complete Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required | Constraints |
|-----------|-------------|-----------|---------|--------------|----------|-------------|
| objectName | Name of the object | String | - | Valid identifier | Yes | Must be unique |
| orReplace | Replace if exists | Boolean | false | true/false | No | Conflicts with ifNotExists |
| transient | Create transient object | Boolean | false | true/false | No | Affects retention |
| ifNotExists | Only create if not exists | Boolean | false | true/false | No | Conflicts with orReplace |
| property1 | First configuration property | String | null | [valid values] | No | [constraints] |
| property2 | Second configuration property | Integer | 0 | 0-100 | No | Must be positive |
| comment | Object comment | String | null | Any string | No | Max 255 characters |

**Total Property Count**: [N]

## 3. Property Categories

### Required Properties
**Count**: [N]
- objectName: Must be specified at creation
- [other required properties]

### Optional Configuration Properties  
**Count**: [N]
- orReplace: Affects creation behavior
- property1: Configurable feature
- [other optional properties]

### State Properties (Read-Only)
**Count**: [N] 
- createdDate: Database-managed timestamp
- [other state properties]

**CRITICAL**: State properties MUST be excluded from SQL generation and unit test comparisons.

## 4. SQL Test Requirements

### Required for Unit Tests (Complete String Comparison)
- All Required Properties must generate complete SQL
- All Optional Configuration Properties must generate complete SQL when specified
- State Properties excluded from SQL generation
- **MANDATORY**: Unit tests must compare complete SQL strings exactly

### Required for Integration Tests (ALL Generated SQL)
- Test matrix covering all property combinations
- Verify every generated SQL statement executes successfully
- Validate final database state matches all expected properties
- **MANDATORY**: All SQL generation paths must be tested

## 5. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. `orReplace` and `ifNotExists` - Cannot be used together
   - **Error Message**: "Cannot use both OR REPLACE and IF NOT EXISTS"
   - **Test Strategy**: Separate test files

2. `transient` and `property1 > 0` - Transient objects have restrictions
   - **Error Message**: "Transient objects cannot have property1 > 0"
   - **Test Strategy**: Separate validation test

### Required Combinations
1. If `property2` is specified, `property1` must also be specified
   - **Error Message**: "property1 is required when property2 is specified"

## 6. Complete SQL Examples for Testing

### Example 1: Basic Object
```sql
CREATE [OBJECT_TYPE] basic_object;
```
**Properties**: Only required properties
**Test File**: [changeType].xml

### Example 2: Object with Optional Properties
```sql
CREATE [OBJECT_TYPE] enhanced_object
  PROPERTY1 = 'value1'
  PROPERTY2 = 42
  COMMENT = 'Enhanced object with properties';
```
**Properties**: Required + optional properties
**Test File**: [changeType].xml

### Example 3: Conditional Creation
```sql
CREATE [OBJECT_TYPE] IF NOT EXISTS conditional_object;
```
**Properties**: Required + ifNotExists
**Test File**: [changeType]IfNotExists.xml (separate due to mutual exclusivity)

### Example 4: Replace Existing
```sql
CREATE OR REPLACE [OBJECT_TYPE] replacement_object
  PROPERTY1 = 'replacement_value';
```
**Properties**: Required + orReplace + optional
**Test File**: [changeType]OrReplace.xml (separate due to mutual exclusivity)

### Example 5: Maximum Complexity
```sql
CREATE TRANSIENT [OBJECT_TYPE] complex_object
  PROPERTY1 = 'complex_value'
  PROPERTY2 = 100
  COMMENT = 'Maximum complexity example with all compatible options';
```
**Properties**: All compatible properties combined
**Test File**: [changeType].xml

## 7. Test Scenarios Planning

Based on mutual exclusivity rules, we need the following test files:

1. **[changeType].xml** - Basic functionality and compatible options
   - Required properties only
   - Required + optional properties
   - Required + optional + transient
   - Maximum compatible complexity

2. **[changeType]OrReplace.xml** - OR REPLACE variations (mutually exclusive)
   - Basic OR REPLACE
   - OR REPLACE with optional properties
   - OR REPLACE with maximum compatible options

3. **[changeType]IfNotExists.xml** - IF NOT EXISTS variations (mutually exclusive)
   - Basic IF NOT EXISTS
   - IF NOT EXISTS with optional properties
   - IF NOT EXISTS with maximum compatible options

**CRITICAL**: Each test file must cover ALL SQL generation scenarios for its category.

## 8. Comprehensive Validation Rules

### 1. Required Attributes
- `objectName` cannot be null or empty
- Must be valid identifier (alphanumeric, underscore, dollar sign)
- **Error Message**: "objectName is required and must be a valid identifier"

### 2. Mutual Exclusivity
- If `orReplace=true` and `ifNotExists=true`, throw validation error
- **Error Message**: "Cannot use both OR REPLACE and IF NOT EXISTS"

### 3. Value Constraints
- `property2` must be between 0 and 100 when specified
- **Error Message**: "property2 must be between 0 and 100, got: [value]"

### 4. Combination Rules
- If `transient=true` and `property1` is specified, validate compatibility
- **Error Message**: "Transient objects have restrictions on property1"

### 5. Format Validation
- `objectName` must match pattern: `^[a-zA-Z][a-zA-Z0-9_$]*$`
- **Error Message**: "objectName must start with letter and contain only alphanumeric, underscore, or dollar sign characters"

## 9. Expected Behaviors and Side Effects

### OR REPLACE Behavior
- Preserves grants on the object
- Drops dependent objects if CASCADE specified
- Maintains object ownership
- **Impact**: May affect dependent objects

### IF NOT EXISTS Behavior
- Succeeds silently if object exists
- Does not modify existing object
- Returns success status regardless
- **Impact**: No changes to existing objects

### TRANSIENT Behavior
- Reduced storage and performance optimizations
- May have limitations on certain properties
- **Impact**: Different lifecycle management

## 10. Error Conditions and Handling

### Common Error Conditions
1. Object already exists (without OR REPLACE or IF NOT EXISTS)
   - **Database Error**: "Object '[name]' already exists"
   - **Handling**: Validate before creation or use conditional options

2. Invalid object name
   - **Database Error**: "Invalid identifier '[name]'"
   - **Handling**: Validate identifier format

3. Insufficient privileges
   - **Database Error**: "Access denied for operation CREATE [OBJECT_TYPE]"
   - **Handling**: Check user permissions

4. Invalid property values
   - **Database Error**: "Invalid value '[value]' for property '[property]'"
   - **Handling**: Validate against constraints

5. Mutual exclusivity violations
   - **Validation Error**: Custom validation messages
   - **Handling**: Prevent invalid combinations in validation

## 11. Implementation Notes

### Database-Specific Considerations
- Object names are automatically converted to uppercase unless quoted
- Comments are stored in system catalog tables
- Some properties cannot be changed after creation
- **Rollback Support**: Consider rollback requirements for each operation

### Performance Considerations
- Large objects may take significant time to create
- Transient objects have different performance characteristics
- Consider timeout settings for long operations

### Security Considerations
- Object creation requires specific privileges
- Comments may be visible to other users
- Consider data security implications

## 12. Cross-Reference Information

### Related Change Types
- [relatedChangeType1]: Similar functionality
- [relatedChangeType2]: Complementary operations

### Database Documentation References
- [Official documentation section 1]
- [Official documentation section 2]
- [Best practices guide]

### Implementation Dependencies
- Requires [specific database version]
- Depends on [system privileges]
- May interact with [other features]

## Quality Checklist

Before considering requirements complete:

- [ ] Official documentation URL included with version
- [ ] Complete syntax documented with all variations
- [ ] All attributes in table with full details (minimum 8 columns)
- [ ] Property categories clearly defined
- [ ] Mutual exclusivity rules identified and documented
- [ ] At least 5 complete SQL examples provided
- [ ] Test scenarios planned with separate files for mutually exclusive features
- [ ] Comprehensive validation rules documented
- [ ] Special behaviors and side effects documented
- [ ] Error conditions and handling specified
- [ ] Implementation notes include database-specific considerations
- [ ] All four core issues explicitly addressed in metadata

## Validation Commands

```bash
# Verify requirements document completeness
grep -c "ADDRESSES_CORE_ISSUES" [changeType]_requirements.md  # Should be >= 1
grep -c "Example.*:" [changeType]_requirements.md  # Should be >= 5
grep -c "Error Message" [changeType]_requirements.md  # Should be >= 3
wc -l [changeType]_requirements.md  # Should be substantial (300+ lines)
```
```

## Common Patterns by Object Type

### Schema Objects
```yaml
COMMON_FEATURES:
  - "IF NOT EXISTS and OR REPLACE options"
  - "Storage properties (TRANSIENT, TEMPORARY)"
  - "Comments/descriptions support"
  - "Security features (MANAGED ACCESS)"
  
TESTING_FOCUS:
  - "Mutual exclusivity of IF NOT EXISTS / OR REPLACE"
  - "Storage property combinations"
  - "Security feature interactions"
```

### Table Objects
```yaml
COMMON_FEATURES:
  - "Complex column definitions"
  - "Multiple constraint types"
  - "Storage and performance options"
  - "Partitioning and clustering"
  
TESTING_FOCUS:
  - "Column definition completeness"
  - "Constraint combinations"
  - "Performance option interactions"
```

### Warehouse Objects
```yaml
COMMON_FEATURES:
  - "Size specifications"
  - "Auto-suspend and auto-resume"
  - "Resource monitors"
  - "Scaling policies"
  
TESTING_FOCUS:
  - "Size and resource constraint compatibility"
  - "Auto-suspend/resume behavior"
  - "Scaling policy combinations"
```

### Database Objects
```yaml
COMMON_FEATURES:
  - "Replication settings"
  - "Default schemas"
  - "Retention policies"
  - "Cross-region options"
  
TESTING_FOCUS:
  - "Replication configuration completeness"
  - "Policy interactions"
  - "Regional setting compatibility"
```

## Requirements Validation Process

### Automated Validation Script
```bash
#!/bin/bash
# scripts/validate-requirements.sh
set -e

CHANGETYPE=$1
REQ_FILE="requirements/${CHANGETYPE}_requirements.md"

echo "=== Requirements Document Validation ==="

if [ ! -f "$REQ_FILE" ]; then
    echo "ERROR: Requirements file not found: $REQ_FILE"
    exit 1
fi

echo "1. Checking document structure..."

# Check for required sections
REQUIRED_SECTIONS=(
    "Official Documentation Reference"
    "Complete Attribute Analysis"
    "Property Categories"
    "Mutual Exclusivity Rules"
    "Complete SQL Examples"
    "Test Scenarios Planning"
    "Comprehensive Validation Rules"
)

for section in "${REQUIRED_SECTIONS[@]}"; do
    if ! grep -q "$section" "$REQ_FILE"; then
        echo "ERROR: Missing required section: $section"
        exit 1
    fi
done

echo "2. Checking SQL examples..."
EXAMPLE_COUNT=$(grep -c "### Example [0-9]" "$REQ_FILE")
if [ "$EXAMPLE_COUNT" -lt 5 ]; then
    echo "ERROR: Need at least 5 SQL examples, found: $EXAMPLE_COUNT"
    exit 1
fi

echo "3. Checking attribute table..."
if ! grep -q "| Attribute | Description | Data Type |" "$REQ_FILE"; then
    echo "ERROR: Attribute analysis table missing or malformed"
    exit 1
fi

echo "4. Checking core issue addressing..."
if ! grep -q "ADDRESSES_CORE_ISSUES" "$REQ_FILE"; then
    echo "ERROR: Core issues not explicitly addressed"
    exit 1
fi

echo "5. Checking validation completeness..."
VALIDATION_COUNT=$(grep -c "Error Message" "$REQ_FILE")
if [ "$VALIDATION_COUNT" -lt 3 ]; then
    echo "ERROR: Need at least 3 validation rules with error messages, found: $VALIDATION_COUNT"
    exit 1
fi

echo "SUCCESS: Requirements document validation passed!"
echo "  - All required sections present"
echo "  - $EXAMPLE_COUNT SQL examples provided"
echo "  - Attribute analysis table complete"
echo "  - Core issues explicitly addressed"
echo "  - $VALIDATION_COUNT validation rules documented"

echo "=== Requirements Validation Complete ==="
```

## Tips for Success

### Research Best Practices
```yaml
BE_THOROUGH: "Missing requirements cause rework later"
TEST_IN_DATABASE: "Verify mutual exclusivity by testing combinations"
THINK_ABOUT_TESTS: "Plan test scenarios while documenting"
CONSIDER_EDGE_CASES: "What happens with special characters, long names, etc."
DOCUMENT_ASSUMPTIONS: "If something is unclear, note your assumption"
```

### Documentation Quality
```yaml
COMPLETE_SYNTAX: "Document every possible SQL variation"
EXECUTABLE_EXAMPLES: "All SQL examples must be complete and runnable"
COMPREHENSIVE_VALIDATION: "Cover all error conditions and edge cases"
CLEAR_CATEGORIZATION: "Properly categorize properties for testing strategies"
EXPLICIT_CORE_ISSUES: "Clearly address how requirements prevent the four core issues"
```

### Validation Rigor
```yaml
OFFICIAL_SOURCES: "Always use official vendor documentation"
VERSION_SPECIFIC: "Document which database versions are supported"
TESTED_COMBINATIONS: "Verify mutual exclusivity rules in actual database"
COMPLETE_COVERAGE: "Every attribute must be documented and tested"
REALISTIC_CONSTRAINTS: "Document actual database limitations"
```

## Cross-Reference Links
```yaml
RELATED_DOCUMENTS:
  MASTER_PROCESS: "master_process_loop.md - Process requiring requirements first"
  CHANGETYPE_PATTERNS: "changetype_patterns.md - Implementation using requirements"
  SQL_OVERRIDES: "sql_generator_overrides.md - SQL syntax from requirements"
  TEST_HARNESS: "test_harness_guide.md - Testing based on requirements"
  ERROR_DEBUGGING: "../snapshot_diff_implementation/error_patterns_guide.md"
  
EXAMPLES: "See existing *_requirements.md files for reference patterns"
NAVIGATION: "README.md - Complete navigation guide"
```

Remember: Good requirements make implementation straightforward and prevent goalpost changing. Invest time here to save time later and ensure all four core issues are properly addressed!