# Liquibase 4.33.0 Extension Implementation - AI-Optimized Sequential Guide V2
## Updated with Field-Tested Improvements

## EXECUTION PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
IMPROVEMENTS: "Test harness scope validation, automated builds, error patterns"
```

## CRITICAL LEARNINGS INTEGRATED
```yaml
NEW_IN_V2:
  - TEST_HARNESS_SCOPE_VALIDATION: "Step 6.0 added"
  - BUILD_AUTOMATION: "Automated workflow scripts"
  - ERROR_PATTERN_LIBRARY: "Common failures documented"
  - REALISTIC_SUCCESS_CRITERIA: "Based on framework limitations"
  - SYSTEMATIC_DEBUGGING: "5-layer analysis framework"
  - CROSS_REFERENCE_NAVIGATION: "Clear document linkage"
  - AI_READABILITY_IMPROVEMENTS: "Structured metadata throughout"
```

## DOCUMENT_NAVIGATION
```yaml
FOLDER_STRUCTURE:
  BASE_PATH: "claude_guide/implementation_guides/snapshot_diff_implementation/"
  DOCUMENTS:
    - README.md: "Navigation guide for all snapshot/diff documents"
    - ai_quickstart.md: "This document - sequential execution guide"
    - main_guide.md: "Overview, prerequisites, common pitfalls"
    - part1_object_model.md: "Database object model implementation"
    - part2_snapshot_implementation.md: "SnapshotGenerator patterns"
    - part3_diff_implementation.md: "Comparator and DiffGenerator"
    - part4_testing_guide.md: "Testing with harness limitations"
    - part5_reference_implementation.md: "Complete Snowflake example"
    - error_patterns_guide.md: "Comprehensive error debugging"

RELATED_CHANGETYPE_GUIDES:
  BASE_PATH: "claude_guide/implementation_guides/changetype_implementation/"
  KEY_DOCUMENTS:
    - master_process_loop.md: "Overall development process"
    - requirements_creation.md: "Detailed requirements specification"
```

## REQUIRED_DOCUMENTS
```yaml
DOCUMENT_1: 
  NAME: "main_guide.md"
  VERSION: "V2 - Updated with retrospective"
  CRITICAL_SECTIONS:
    - "Common Pitfalls and Solutions"
    - "Systematic Debugging Framework"
    - "Build Process Optimization"
  
DOCUMENT_2: 
  NAME: "part1_object_model.md"
  PURPOSE: "Object implementation patterns, property handling, testing"
  
DOCUMENT_3: 
  NAME: "part2_snapshot_implementation.md"
  PURPOSE: "SnapshotGenerator patterns, SQL queries, ResultSet parsing"
  
DOCUMENT_4: 
  NAME: "part3_diff_implementation.md"
  PURPOSE: "Comparator and DiffGenerator patterns"
  
DOCUMENT_5: 
  NAME: "part4_testing_guide.md"
  VERSION: "V2 - Critical test harness limitations added"
  CRITICAL_SECTIONS:
    - "Test Harness - CRITICAL LIMITATIONS"
    - "Common Error Patterns and Solutions"
    - "Systematic Debugging Framework"
  
DOCUMENT_6: 
  NAME: "part5_reference_implementation.md"
  PURPOSE: "Complete working implementation example"

COMPANION_ERROR_GUIDE:
  NAME: "error_patterns_guide.md"
  PURPOSE: "Comprehensive error pattern library and debugging framework"
```

## BUILD_AUTOMATION_SETUP
```yaml
CREATE_SCRIPT:
  FILE: "scripts/dev-workflow.sh"
  CONTENT: |
    #!/bin/bash
    set -e
    
    echo "=== Liquibase Extension Development Workflow ==="
    echo "1. Building extension..."
    cd liquibase-$1
    mvn clean install -DskipTests
    
    echo "2. Installing to local repository..."
    echo "   (Required for test harness dependency resolution)"
    
    echo "3. Changing to test harness..."
    cd ../liquibase-test-harness
    
    echo "4. Running test harness..."
    mvn test -Dtest=SnapshotObjectTests -DdbName=$1 -DsnapshotObjects="$2"
    
    echo "=== Workflow Complete ==="

USAGE: "./scripts/dev-workflow.sh [database] [testname]"
BENEFIT: "Single command to build, install, and test"
```

## EXECUTION_STATE
```yaml
CURRENT_STEP: 1.1
CURRENT_STATUS: NOT_STARTED
LAST_COMPLETED_STEP: None
FAILED_VALIDATIONS: []
KNOWN_LIMITATIONS: []
```

---

## STEP 1.1: Document ALL Database Object Properties
```yaml
STEP_ID: 1.1
STATUS: NOT_STARTED
PREREQUISITES: []
BLOCKS: [1.2, 2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4, 6.0, 6.1, 6.2, 6.3, 6.4, 7.1, 7.2]
ADDRESSES_CORE_ISSUE: "Complete syntax definition - ensures all properties documented"
```

### INPUTS_REQUIRED
```yaml
- DATABASE_TYPE: "[PostgreSQL|MySQL|Oracle|Snowflake|SQLServer|Other]"
- OBJECT_TYPE: "[name of database object to implement]"
```

### ACTIONS
```yaml
- ACTION_1:
    DESCRIPTION: "Locate official database documentation"
    TARGET_URL: "[vendor documentation site]"
    CROSS_REFERENCE: "See requirements_creation.md for research process"
    
- ACTION_2:
    DESCRIPTION: "Find and document SQL syntax"
    REQUIRED_SYNTAX:
      - "CREATE [OBJECT_TYPE]"
      - "ALTER [OBJECT_TYPE]"
      - "DROP [OBJECT_TYPE]"
      - "SHOW/DESCRIBE [OBJECT_TYPE]"
    ADDRESSES_CORE_ISSUE: "Complete syntax definition"
    
- ACTION_3:
    DESCRIPTION: "Create requirements file"
    FILE_PATH: "requirements/[ObjectType]_requirements.md"
    TEMPLATE: |
      # [ObjectType] Requirements Specification
      
      ## Total Property Count: [NUMBER]
      
      ## Properties Table
      | Property Name | Data Type | Required | Default Value | Valid Values | Constraints |
      |--------------|-----------|----------|---------------|--------------|-------------|
      | [name]       | [type]    | [Y/N]    | [default]     | [values]     | [limits]    |
      
      ## Complete SQL Syntax Examples
      | Operation | SQL Statement | Test Required |
      |-----------|---------------|---------------|
      | CREATE    | [complete statement] | YES |
      | ALTER     | [complete statement] | YES |
      | DROP      | [complete statement] | YES |
```

### VALIDATIONS
```yaml
- VALIDATION_1:
    TYPE: "FILE_EXISTS"
    PATH: "requirements/[ObjectType]_requirements.md"
    
- VALIDATION_2:
    TYPE: "FILE_CONTAINS"
    PATH: "requirements/[ObjectType]_requirements.md"
    REQUIRED_CONTENT: "Total Property Count:"
    
- VALIDATION_3:
    TYPE: "FILE_CONTAINS"
    PATH: "requirements/[ObjectType]_requirements.md"
    REQUIRED_CONTENT: "Properties Table"
    
- VALIDATION_4:
    TYPE: "PROPERTY_COUNT"
    MINIMUM: 1
    
- VALIDATION_5:
    TYPE: "FILE_CONTAINS"
    PATH: "requirements/[ObjectType]_requirements.md"
    REQUIRED_CONTENT: "Complete SQL Syntax Examples"
    ADDRESSES_CORE_ISSUE: "Complete syntax definition"
    
- VALIDATION_6:
    TYPE: "SYNTAX_COMPLETENESS"
    VERIFY: "All SQL operations documented with complete statements"
    ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

### ON_SUCCESS
```yaml
UPDATE_STATE:
  CURRENT_STEP: 1.2
  LAST_COMPLETED_STEP: 1.1
PROCEED_TO: STEP_1.2
```

### ON_FAILURE
```yaml
REPORT_ERROR: "[VALIDATION_ID] failed: [ERROR_MESSAGE]"
UPDATE_STATE:
  FAILED_VALIDATIONS: "[VALIDATION_ID]"
STOP_EXECUTION: true
CONSULT_ERROR_GUIDE: "See error_patterns_guide.md for systematic debugging"
```

---

## STEP 1.2: Categorize Properties
```yaml
STEP_ID: 1.2
STATUS: BLOCKED
PREREQUISITES: [1.1]
BLOCKS: [2.1, 2.2, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4, 6.0, 6.1, 6.2, 6.3, 6.4, 7.1, 7.2]
```

### CRITICAL_LEARNING
```yaml
IMPORTANCE: "State properties MUST be excluded from comparisons"
EVIDENCE: "Warehouse implementation showed state properties causing false differences"
ACTION: "Carefully categorize every property"
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL - only relevant properties"
```

### ACTIONS
```yaml
- ACTION_1:
    DESCRIPTION: "Open requirements file"
    FILE_PATH: "requirements/[ObjectType]_requirements.md"
    
- ACTION_2:
    DESCRIPTION: "Add property categories section"
    APPEND_TO_FILE: true
    CONTENT: |
      ## Property Categories
      
      ### Required Properties
      Count: [N]
      - [property_name]: [description]
      
      ### Optional Configuration Properties  
      Count: [N]
      - [property_name]: [description]
      
      ### State Properties (Read-Only)
      Count: [N]
      - [property_name]: [description]
      
      ## SQL Test Requirements
      ### Required for Unit Tests (Complete String Comparison)
      - All Required Properties must generate complete SQL
      - All Optional Configuration Properties must generate complete SQL
      - State Properties excluded from SQL generation
      
      ### Required for Integration Tests (ALL Generated SQL)
      - Test matrix covering all property combinations
      - Verify every generated SQL statement executes successfully
      - Validate final database state matches expected
      
- ACTION_3:
    DESCRIPTION: "Categorize each property"
    RULES:
      - "Required: Must be specified at object creation"
      - "Optional Configuration: Can be set/changed by user"
      - "State: Runtime information, read-only, EXCLUDE FROM COMPARISONS"
    ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

### VALIDATIONS
```yaml
- VALIDATION_1:
    TYPE: "FILE_CONTAINS"
    PATH: "requirements/[ObjectType]_requirements.md"
    REQUIRED_CONTENT: "## Property Categories"
    
- VALIDATION_2:
    TYPE: "SECTION_EXISTS"
    PATH: "requirements/[ObjectType]_requirements.md"
    SECTIONS:
      - "### Required Properties"
      - "### Optional Configuration Properties"
      - "### State Properties (Read-Only)"
      
- VALIDATION_3:
    TYPE: "COUNT_VERIFICATION"
    VERIFY: "Sum of category counts equals Total Property Count"
    
- VALIDATION_4:
    TYPE: "NO_DUPLICATES"
    VERIFY: "Each property appears in exactly one category"

- VALIDATION_5:
    TYPE: "SQL_TEST_REQUIREMENTS_DOCUMENTED"
    VERIFY: "Complete string comparison requirements specified"
    ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
    
- VALIDATION_6:
    TYPE: "INTEGRATION_TEST_REQUIREMENTS_DOCUMENTED"
    VERIFY: "ALL generated SQL test requirements specified"
    ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

### ON_SUCCESS
```yaml
UPDATE_STATE:
  CURRENT_STEP: 2.1
  LAST_COMPLETED_STEP: 1.2
PROCEED_TO: STEP_2.1
```

---

## STEP 6.0: Validate Test Harness Compatibility
```yaml
STEP_ID: 6.0
STATUS: BLOCKED
PREREQUISITES: [5.4]
BLOCKS: [6.1, 6.2, 6.3, 6.4, 7.1, 7.2]
NEW_IN_V2: true
CRITICAL: "Must verify framework capabilities before writing tests"
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
```

### RATIONALE
```yaml
LEARNING: "Test harness may not include custom objects in snapshot scope"
IMPACT: "Tests may fail for wrong reasons if scope not understood"
SOLUTION: "Verify scope and adjust success criteria before implementation"
PREVENTS_GOALPOST_CHANGING: "Establishes realistic criteria upfront"
```

### REQUIRED_DOCUMENTS
```yaml
CONSULT_BEFORE_ACTION:
  - DOCUMENT_5:
      NAME: "part4_testing_guide.md"
      SECTION: "Test Harness - CRITICAL LIMITATIONS"
      CRITICAL_INFO: "Custom objects may not be in snapshot scope"
      ACTION: "Read entire section before proceeding"
  - ERROR_GUIDE:
      NAME: "error_patterns_guide.md"
      SECTION: "Pattern #3: Objects Not in Snapshot"
      ACTION: "Understand scope validation process"
```

### ACTIONS
```yaml
- ACTION_1:
    DESCRIPTION: "Build and install extension first"
    COMMANDS:
      - "cd liquibase-[database]"
      - "mvn clean install -DskipTests"
    REASON: "Test harness needs extension in local repository"
    
- ACTION_2:
    DESCRIPTION: "Test with standard objects"
    COMMAND: "mvn test -Dtest=SnapshotObjectTests -DdbName=[database] -DsnapshotObjects=createTable"
    EXPECTED: "Test should pass for standard table creation"
    
- ACTION_3:
    DESCRIPTION: "Check snapshot scope"
    EXAMINE_OUTPUT: "Look for 'includedType' in test output"
    CHECK_FOR: "Your custom object type in the list"
    
- ACTION_4:
    DESCRIPTION: "Set realistic success criteria"
    IF_CUSTOM_OBJECTS_INCLUDED:
      SUCCESS_CRITERIA: "Full test harness pass with snapshot validation"
    IF_CUSTOM_OBJECTS_NOT_INCLUDED:
      SUCCESS_CRITERIA: "Changesets execute + objects created in DB"
      ADJUST_EXPECTATIONS: "Focus on execution, not snapshot comparison"
    PREVENTS_GOALPOST_CHANGING: "Criteria fixed based on framework capabilities"
```

### VALIDATIONS
```yaml
- VALIDATION_1:
    TYPE: "STANDARD_TEST_WORKS"
    VERIFY: "createTable test passes"
    
- VALIDATION_2:
    TYPE: "SCOPE_DOCUMENTED"
    CREATE_FILE: "requirements/test_harness_scope.md"
    CONTENT: "Document whether custom objects are included"
    
- VALIDATION_3:
    TYPE: "SUCCESS_CRITERIA_SET"
    VERIFY: "Clear criteria established based on scope"
    PREVENTS_GOALPOST_CHANGING: "Success criteria locked and documented"
```

### ON_SUCCESS
```yaml
UPDATE_STATE:
  CURRENT_STEP: 6.1
  LAST_COMPLETED_STEP: 6.0
  KNOWN_LIMITATIONS: "[Document any scope limitations]"
PROCEED_TO: STEP_6.1
```

---

## STEP 6.3: Create Test Harness Tests
```yaml
STEP_ID: 6.3
STATUS: BLOCKED
PREREQUISITES: [6.2]
BLOCKS: [6.4, 7.1, 7.2]
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

### CRITICAL_UPDATES
```yaml
NEW_REQUIREMENT: "Add runAlways cleanup changesets"
REASON: "DATABASECHANGELOG persists between test runs"
SOLUTION: "Ensure fresh state for each test"
ADDRESSES_CORE_ISSUE: "Integration tests must test complete SQL execution cycle"
```

### REQUIRED_DOCUMENTS
```yaml
CONSULT_BEFORE_ACTION:
  - DOCUMENT_5:
      NAME: "part4_testing_guide.md"
      SECTION: "Common Test Harness Issues and Solutions"
      FIND: "Issue 1: Database is up to date"
      CRITICAL_PATTERN: 'runAlways="true" for cleanup'
  - ERROR_GUIDE:
      NAME: "error_patterns_guide.md"
      SECTION: "Pattern #2: Database State Persistence"
      ACTION: "Understand cleanup requirements"
```

### ACTIONS
```yaml
- ACTION_1:
    DESCRIPTION: "Create harness directory"
    COMMAND: "mkdir -p src/test/groovy/.../harness/"
    
- ACTION_2:
    DESCRIPTION: "Create changelog with cleanup"
    FILE_PATH: "src/main/resources/liquibase/harness/snapshot/changelogs/[database]/[test].xml"
    CRITICAL_PATTERN: |
      <changeSet id="cleanup-[object]" author="test-harness" runAlways="true">
          <sql>DROP [OBJECT_TYPE] IF EXISTS TEST_[OBJECT] CASCADE;</sql>
      </changeSet>
      
      <changeSet id="create-[object]" author="test-harness">
          <!-- Your creation logic with ALL properties -->
          <!-- MUST test complete SQL generation -->
      </changeSet>
      
      <changeSet id="verify-all-sql" author="test-harness">
          <!-- Additional verification steps -->
          <!-- Test ALL generated SQL statements -->
      </changeSet>
    ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
    
- ACTION_3:
    DESCRIPTION: "Create expected JSON"
    FILE_PATH: "src/main/resources/liquibase/harness/snapshot/expectedSnapshot/[database]/[test].json"
    CRITICAL_FORMAT: |
      {
        "snapshot": {
          "objects": {
            "liquibase.ext.[database].database.object.[ObjectType]": [
              {
                "[objectType]": {
                  "name": "TEST_[OBJECT]",
                  // Only include configuration properties
                  // NO database metadata
                  // NO "IGNORE" values
                  // MUST match complete SQL string comparison
                }
              }
            ]
          }
        }
      }
    ADDRESSES_CORE_ISSUE: "Unit tests must compare complete SQL strings"
```

### VALIDATIONS
```yaml
- VALIDATION_1:
    TYPE: "CLEANUP_CHANGESET_EXISTS"
    VERIFY: "Changeset with runAlways='true' exists"
    
- VALIDATION_2:
    TYPE: "JSON_FORMAT_VALID"
    VERIFY: "No database metadata fields"
    VERIFY: "No IGNORE values"
    VERIFY: "Format supports complete SQL string comparison"
    ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
    
- VALIDATION_3:
    TYPE: "FILE_LOCATIONS_CORRECT"
    VERIFY: "Files in test harness repo, not extension repo"
    
- VALIDATION_4:
    TYPE: "COMPLETE_SQL_COVERAGE"
    VERIFY: "All property combinations tested"
    VERIFY: "All generated SQL statements verified"
    ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

### ON_SUCCESS
```yaml
UPDATE_STATE:
  CURRENT_STEP: 6.4
  LAST_COMPLETED_STEP: 6.3
PROCEED_TO: STEP_6.4
```

---

## ERROR_PATTERN_LIBRARY
```yaml
PATTERN_1:
  ERROR: "No snapshot generator found for type"
  CAUSE: "Service registration missing or incorrect"
  SOLUTION:
    - "Check META-INF/services files"
    - "Verify class names match exactly"
    - "Rebuild JAR and check contents"
  ADDRESSES_CORE_ISSUE: "Incomplete syntax definition"
    
PATTERN_2:
  ERROR: "Database is up to date"
  CAUSE: "DATABASECHANGELOG has execution history"
  SOLUTION: "Add runAlways='true' to cleanup changesets"
  ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
  
PATTERN_3:
  ERROR: "Expected: [ObjectType] but none found"
  CAUSE: "Object not in snapshot scope OR not created"
  SOLUTION:
    - "Check if changeset executed"
    - "Verify object in database manually"
    - "CRITICAL: Check META-INF/services/liquibase.structure.DatabaseObject registration"
    - "Objects must be registered to appear in default snapshot scope"
    - "Adjust success criteria if not in scope"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
    
PATTERN_4:
  ERROR: "Column '[name]' not found"
  CAUSE: "Case sensitivity in ResultSet"
  SOLUTION: "Try multiple case variations"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
PATTERN_5:
  ERROR: "JSONAssert mismatch"
  CAUSE: "Format doesn't match test harness expectations"
  SOLUTION:
    - "Remove database metadata"
    - "Remove IGNORE values"
    - "Match working examples exactly"
    - "Ensure complete SQL string format"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"

PATTERN_6:
  ERROR: "Test passes but SQL incomplete"
  CAUSE: "Unit test not validating complete SQL string"
  SOLUTION:
    - "Add explicit string comparison assertions"
    - "Test ALL property combinations"
    - "Verify exact SQL syntax matches requirements"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
  
PATTERN_7:
  ERROR: "Integration test skips some SQL statements"
  CAUSE: "Test harness not executing all changesets"
  SOLUTION:
    - "Verify all changesets have unique IDs"
    - "Check DATABASECHANGELOG for execution records"
    - "Add verification changesets to confirm all SQL ran"
  ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

## SYSTEMATIC_DEBUGGING_FRAMEWORK
```yaml
WHEN_TESTS_FAIL:
  LAYER_1_CODE_LOADING:
    CHECK: "Is JAR built? Are services registered?"
    VERIFY: "jar -tf target/*.jar | grep META-INF"
    ADDRESSES_CORE_ISSUE: "Incomplete syntax definition"
    
  LAYER_2_REGISTRATION:
    CHECK: "Is Liquibase finding components?"
    VERIFY: "Look for loading messages in logs"
    ADDRESSES_CORE_ISSUE: "Complete syntax definition"
    
  LAYER_3_EXECUTION:
    CHECK: "Are methods being called?"
    VERIFY: "Add debug logging to methods"
    ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
    
  LAYER_4_DATA_CREATION:
    CHECK: "Is data in database?"
    VERIFY: "Manual query: SHOW [OBJECTS] LIKE 'TEST_%'"
    ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
    
  LAYER_5_TEST_FRAMEWORK:
    CHECK: "Is test framework configured correctly?"
    VERIFY: "Check JSON format, snapshot scope"
    ADDRESSES_CORE_ISSUE: "Realistic success criteria"
    
APPLY_IN_ORDER: "Start at Layer 1, work up to Layer 5"
PREVENT_GOALPOST_CHANGING: "Use structured debugging, don't redefine requirements"
```

## COMPLETION_CRITERIA
```yaml
ALL_STEPS_STATUS: "COMPLETE"
FINAL_JAR_VALID: true
TEST_SUITE_PASSING: true
CLI_VERIFICATION: true
DOCUMENTATION_COMPLETE: true
LIMITATIONS_DOCUMENTED: true
SUCCESS_CRITERIA_MET: "Based on realistic framework capabilities"

CORE_ISSUES_ADDRESSED:
  COMPLETE_SYNTAX_DEFINITION: "All properties and SQL syntax documented"
  COMPLETE_SQL_TEST_STATEMENTS: "All SQL examples complete and tested"
  UNIT_TESTS_COMPLETE_STRING_COMPARISON: "Explicit string comparison in all unit tests"
  INTEGRATION_TESTS_ALL_SQL: "All generated SQL verified in integration tests"
```

## ERROR_RECOVERY_PROTOCOL
```yaml
ON_VALIDATION_FAILURE:
  - STOP_EXECUTION: true
  - CHECK_ERROR_PATTERN_LIBRARY: true
  - APPLY_SYSTEMATIC_DEBUGGING: true
  - REPORT_FAILURE:
      STEP_ID: "[current_step]"
      VALIDATION_ID: "[failed_validation]"
      ERROR_MESSAGE: "[detailed_error]"
      PATTERN_MATCH: "[matching error pattern if found]"
      CORE_ISSUE_ADDRESSED: "[which of the 4 core issues this addresses]"
  - DO_NOT_PROCEED: true
  - FIX_ISSUE: true
  - RETRY_FROM_CURRENT_STEP: true
  - PREVENT_GOALPOST_CHANGING: "Do not redefine requirements to fit current code"
```

## CROSS_REFERENCE_LINKS
```yaml
RELATED_DOCUMENTS:
  MAIN_GUIDE: "main_guide.md - Prerequisites and common pitfalls"
  ERROR_PATTERNS: "error_patterns_guide.md - Comprehensive debugging"
  TESTING_GUIDE: "part4_testing_guide.md - Test harness limitations"
  
CHANGETYPE_GUIDES:
  REQUIREMENTS: "../changetype_implementation/requirements_creation.md"
  PROCESS: "../changetype_implementation/master_process_loop.md"
  
NAVIGATION:
  FOLDER_README: "README.md - Complete navigation guide"
```