# AI Workflow Guide - Changetype Implementation
## Phase 3: Test-Driven Development (TDD) Implementation with Decision Tree Navigation

## EXECUTION_PROTOCOL
```yaml
PHASE: 3_TDD_IMPLEMENTATION
PROTOCOL_VERSION: 3.0
EXECUTION_MODE: SEQUENTIAL_BLOCKING_TDD
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
INPUT_REQUIRED: "[object]_requirements.md (IMPLEMENTATION_READY from Phase 2)"
DELIVERABLE: "Complete tested changetype implementation with comprehensive coverage"
APPROACH: "RED_GREEN_REFACTOR"
```

## WORKFLOW_OVERVIEW
```yaml
PURPOSE: "Intelligent workflow guide that leads Claude through decision points to implement, enhance, repair, complete, or fix changetype implementations using strict TDD"
INPUT: "Complete requirements document from Phase 2 with IMPLEMENTATION_READY status"
OUTPUT: "Fully tested, working changetype implementation with comprehensive test coverage"
DURATION: "4-8 hours depending on complexity and scenario"
CRITICAL_SUCCESS_FACTOR: "Tests drive implementation - code exists only to make tests pass"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/ai_workflow_guide.md"
WORKFLOW_SEQUENCE:
  PHASE_1: "ai_requirements_research.md - Requirements Research (COMPLETED)"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation (COMPLETED)"
  PHASE_3: "THIS DOCUMENT - TDD Implementation"
  
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - changetype_patterns.md: "Implementation patterns referenced by this workflow"
  - sql_generator_overrides.md: "SQL override patterns referenced by this workflow"
  - test_harness_guide.md: "Integration testing referenced by this workflow"
  - ../snapshot_diff_implementation/ai_workflow_guide.md: "Parallel TDD workflow for snapshot/diff"
```

## 🎯 INTELLIGENT DECISION TREE - WHAT ARE YOU TRYING TO ACCOMPLISH?

```yaml
DECISION_POINT_1: "What is your implementation scenario?"

SCENARIO_A_NEW_IMPLEMENTATION:
  DESCRIPTION: "Implement a completely new changetype from scratch"
  REQUIREMENTS: "Must have [object]_requirements.md (IMPLEMENTATION_READY)"
  WORKFLOW: "Go to Section A: New Implementation TDD Workflow"
  DURATION: "6-8 hours"
  
SCENARIO_B_ENHANCE_EXISTING:
  DESCRIPTION: "Add database-specific attributes to existing changetype"
  REQUIREMENTS: "Must have [object]_requirements.md (IMPLEMENTATION_READY)"
  WORKFLOW: "Go to Section B: Enhancement TDD Workflow"
  DURATION: "4-6 hours"
  
SCENARIO_C_REVIEW_AND_REPAIR:
  DESCRIPTION: "Review and repair existing implementation with issues"
  REQUIREMENTS: "Access to existing implementation + problem description"
  WORKFLOW: "Go to Section C: Review and Repair TDD Workflow"
  DURATION: "2-4 hours"
  
SCENARIO_D_COMPLETE_INCOMPLETE:
  DESCRIPTION: "Complete an incomplete changetype implementation"
  REQUIREMENTS: "Access to existing partial implementation + requirements"
  WORKFLOW: "Go to Section D: Completion TDD Workflow"
  DURATION: "3-5 hours"
  
SCENARIO_E_FIX_BUGS:
  DESCRIPTION: "Fix bugs in existing changetype implementation"
  REQUIREMENTS: "Access to existing implementation + bug reports/test failures"
  WORKFLOW: "Go to Section E: Bug Fix TDD Workflow"
  DURATION: "1-3 hours"
  
SCENARIO_F_PERFORMANCE_OPTIMIZATION:
  DESCRIPTION: "Optimize performance of existing changetype implementation"
  REQUIREMENTS: "Access to existing implementation + performance requirements"
  WORKFLOW: "Go to Section F: Performance Optimization TDD Workflow"
  DURATION: "2-4 hours"
```

## 🏗️ ARCHITECTURE DECISION POINT - Choose Your Implementation Pattern

**CRITICAL**: Before proceeding to any workflow section, determine your architecture pattern:

```yaml
ARCHITECTURE_ASSESSMENT:
  DECISION_POINT_0: "What type of changetype architecture do you need?"
  
  ASSESSMENT_STEPS:
    STEP_1: "Review your requirements document for operation modes"
    STEP_2: "Count mutually exclusive operation modes"
    STEP_3: "Apply decision tree below"
    
  SINGLE_MODE_CHANGETYPE:
    INDICATORS:
      - "One primary operation (e.g., CREATE, DROP)"
      - "Minor variations only (IF EXISTS, CASCADE)"
      - "Shared validation across variations"
      - "Similar SQL generation pattern"
    EXAMPLES: ["DROP TABLE", "CREATE SEQUENCE", "CREATE INDEX"]
    ARCHITECTURE: "PROPERTY_BASED_IMPLEMENTATION"
    COMPLEXITY: "LOW-MEDIUM"
    SUCCESS_FACTORS: "Follow standard workflow phases"
    
  MULTI_MODE_CHANGETYPE:
    INDICATORS:
      - "2+ mutually exclusive operation modes"
      - "Different validation rules per mode"
      - "Different SQL generation per mode"
      - "Operations cannot be combined"
    EXAMPLES: 
      - "ALTER WAREHOUSE (RENAME/SET/UNSET/SUSPEND/RESUME/ABORT)"
      - "GRANT/REVOKE operations (different privilege types)"
      - "CREATE TABLE vs CREATE TABLE AS SELECT"
      - "MERGE operations (multiple match conditions)"
    ARCHITECTURE: "OPERATION_TYPE_DRIVEN_ARCHITECTURE"
    COMPLEXITY: "MEDIUM-HIGH"
    SUCCESS_FACTORS: "Follow enhanced workflow with OperationType patterns"
    PROVEN_SUCCESS_RATE: "71% quality improvement over property-based"
```

### 🎯 Architecture-Specific Workflow Selection

```yaml
IF_SINGLE_MODE:
  WORKFLOW: "Continue with standard Section A/B/C/D/E/F workflows below"
  IMPLEMENTATION: "Use property-based patterns from NEW_CHANGETYPE_PATTERN_2.md"
  
IF_MULTI_MODE:
  WORKFLOW: "Use ENHANCED workflows (A-Enhanced, B-Enhanced, etc.)"
  IMPLEMENTATION: "Use Operation-Type-Driven Architecture patterns"
  SPECIAL_CONSIDERATIONS:
    - "Add OperationType enum to Statement class"
    - "Implement operation-specific validation methods" 
    - "Use switch-based SQL generation"
    - "Add backward compatibility inference logic"
    - "Implement operation-specific test coverage"
```

## 🔍 SECTION A: NEW IMPLEMENTATION TDD WORKFLOW

### A.1: Pre-Implementation Validation
```yaml
BLOCKING_VALIDATION_A.1:
  TYPE: "REQUIREMENTS_READY"
  REQUIREMENT: "Requirements document exists, is complete, and marked IMPLEMENTATION_READY"
  FAILURE_ACTION: "STOP - Return to Phase 2 to complete requirements documentation"

VALIDATION_ACTIVITIES:
  REQUIREMENTS_CHECK:
    - "Verify [object]_requirements.md exists and is marked IMPLEMENTATION_READY"
    - "Confirm all sections are complete (SQL syntax, attribute analysis, test scenarios)"
    - "Validate implementation pattern is clearly specified"
    - "Check that all quality gates from Phase 2 are passed"
    - "Ensure test scenarios are comprehensive and actionable"

VALIDATION_CHECKPOINT_A.1:
  - [ ] Requirements document exists and marked IMPLEMENTATION_READY
  - [ ] All sections complete and comprehensive
  - [ ] Implementation pattern clearly specified
  - [ ] Test scenarios actionable and comprehensive
  - [ ] Ready to begin TDD implementation
```

### A.2: RED PHASE - Write Failing Tests First
```yaml
BLOCKING_VALIDATION_A.2:
  TYPE: "FAILING_TESTS_COMPLETE"
  REQUIREMENT: "Complete failing test suite written based on requirements before any implementation code"
  FAILURE_ACTION: "STOP - Must write all failing tests before implementation"

TDD_RED_ACTIVITIES:
  UNIT_TEST_CREATION:
    - "Create failing unit tests for all changetype properties and validations"
    - "Create failing unit tests for all SQL generation scenarios from requirements"
    - "Create failing unit tests for all error conditions and validation failures"
    - "Create failing unit tests for all parameter combinations and mutual exclusivity"
    - "Ensure ALL tests fail initially (no implementation exists yet)"
    
  TEST_VALIDATION:
    - "Run complete test suite and confirm ALL tests fail appropriately"
    - "Verify test failure messages are clear and informative"
    - "Confirm tests cover all scenarios from requirements document"
    - "Validate test structure follows TDD best practices"
    - "Ensure tests are independent and can run in any order"

VALIDATION_CHECKPOINT_A.2:
  - [ ] Complete failing test suite created for all requirements scenarios
  - [ ] All tests fail appropriately with clear failure messages
  - [ ] Tests cover all parameter combinations and error conditions
  - [ ] Test suite runs successfully (all failing as expected)
  - [ ] Ready to begin GREEN phase implementation
```

### A.3: GREEN PHASE - Minimal Implementation to Pass Tests
```yaml
BLOCKING_VALIDATION_A.3:
  TYPE: "TESTS_PASSING"
  REQUIREMENT: "Minimal implementation written to make all tests pass without over-engineering"
  FAILURE_ACTION: "STOP - Fix implementation until all tests pass"

TDD_GREEN_ACTIVITIES:
  MINIMAL_IMPLEMENTATION:
    - "Implement changetype class with minimal code to pass property tests"
    - "Implement statement class with minimal code to pass statement tests"
    - "Implement SQL generator with minimal code to pass SQL generation tests"
    - "Add service registration to pass framework integration tests"
    - "Focus on making tests pass, not on perfect code structure"
    
  TEST_VALIDATION:
    - "Run complete test suite and confirm ALL tests now pass"
    - "Verify no tests were modified to make them pass"
    - "Confirm implementation handles all scenarios from requirements"
    - "Validate SQL generation matches exact expected output from tests"
    - "Ensure error conditions produce exact expected error messages"

VALIDATION_CHECKPOINT_A.3:
  - [ ] Minimal implementation complete for all required components
  - [ ] ALL unit tests now pass without modification
  - [ ] SQL generation matches exact expected output
  - [ ] Error conditions produce expected error messages
  - [ ] Ready to begin REFACTOR phase optimization
```

### A.4: REFACTOR PHASE - Improve Code Quality While Maintaining Green Tests
```yaml
BLOCKING_VALIDATION_A.4:
  TYPE: "REFACTORED_WITH_PASSING_TESTS"
  REQUIREMENT: "Code quality improved through refactoring while maintaining all passing tests"
  FAILURE_ACTION: "STOP - Refactoring broke tests, must fix before proceeding"

TDD_REFACTOR_ACTIVITIES:
  CODE_QUALITY_IMPROVEMENT:
    - "Refactor changetype class for better structure and maintainability"
    - "Refactor SQL generator for better performance and readability"
    - "Extract common patterns and eliminate code duplication"
    - "Improve error handling and validation logic clarity"
    - "Add comprehensive documentation and comments"
    
  CONTINUOUS_VALIDATION:
    - "Run test suite after each refactoring change"
    - "Ensure ALL tests remain passing throughout refactoring"
    - "Verify refactoring improves code quality without changing behavior"
    - "Confirm performance improvements where applicable"
    - "Validate code follows project conventions and patterns"

VALIDATION_CHECKPOINT_A.4:
  - [ ] Code refactored for improved quality and maintainability
  - [ ] ALL tests remain passing after refactoring
  - [ ] Code duplication eliminated and patterns extracted
  - [ ] Performance optimized where applicable
  - [ ] Ready for integration testing phase
```

### A.5: Integration Testing and Harness Validation
```yaml
BLOCKING_VALIDATION_A.5:
  TYPE: "INTEGRATION_TESTS_PASSING"
  REQUIREMENT: "Integration tests passing with test harness validation"
  FAILURE_ACTION: "STOP - Fix integration issues using systematic debugging"

INTEGRATION_ACTIVITIES:
  HARNESS_TESTING:
    - "Create test harness files following test_harness_guide.md"
    - "Run integration tests against real database"
    - "Validate complete changeset execution and database state"
    - "Test all scenarios from requirements in integration environment"
    - "Verify schema isolation and cleanup working correctly"
    
  DEBUGGING_AND_VALIDATION:
    - "Use systematic debugging if integration tests fail"
    - "Apply error patterns from error_patterns_guide.md"
    - "Fix issues without changing requirements or lowering expectations"
    - "Validate complete end-to-end functionality"
    - "Confirm implementation meets all original requirements"

VALIDATION_CHECKPOINT_A.5:
  - [ ] Test harness files created and integration tests running
  - [ ] All integration tests passing against real database
  - [ ] Complete changeset execution validated
  - [ ] Schema isolation and cleanup working correctly
  - [ ] Implementation complete and fully validated
```

## 🔧 SECTION B: ENHANCEMENT TDD WORKFLOW

### B.1: Existing Implementation Analysis
```yaml
BLOCKING_VALIDATION_B.1:
  TYPE: "EXISTING_ANALYSIS_COMPLETE"
  REQUIREMENT: "Existing changetype implementation thoroughly analyzed and enhancement requirements validated"
  FAILURE_ACTION: "STOP - Must understand existing implementation before enhancement"

ANALYSIS_ACTIVITIES:
  EXISTING_CODE_REVIEW:
    - "Analyze existing changetype class structure and patterns"
    - "Review existing SQL generation and parameter handling"
    - "Understand current test coverage and validation patterns"
    - "Identify integration points and service registration"
    - "Map current functionality against enhancement requirements"
    
  ENHANCEMENT_VALIDATION:
    - "Validate enhancement requirements are complete and actionable"
    - "Confirm new attributes/features don't conflict with existing functionality"
    - "Identify implementation approach (extension vs SQL override)"
    - "Plan backward compatibility and migration strategy"
    - "Assess impact on existing tests and functionality"

VALIDATION_CHECKPOINT_B.1:
  - [ ] Existing implementation thoroughly analyzed and understood
  - [ ] Enhancement requirements validated against existing functionality
  - [ ] Implementation approach selected (extension vs SQL override)
  - [ ] Backward compatibility strategy planned
  - [ ] Ready to begin enhancement TDD implementation
```

### B.2: RED PHASE - Enhancement Tests First
```yaml
BLOCKING_VALIDATION_B.2:
  TYPE: "ENHANCEMENT_TESTS_FAILING"
  REQUIREMENT: "Failing tests written for all new enhancement features before implementation"
  FAILURE_ACTION: "STOP - Must write failing enhancement tests first"

TDD_RED_ACTIVITIES:
  ENHANCEMENT_TEST_CREATION:
    - "Create failing tests for all new attributes and parameters"
    - "Create failing tests for enhanced SQL generation scenarios"
    - "Create failing tests for new validation rules and error conditions"
    - "Create failing tests for backward compatibility requirements"
    - "Ensure existing tests still pass (no regression)"
    
  TEST_VALIDATION:
    - "Run existing test suite and confirm no regressions"
    - "Run new enhancement tests and confirm they fail appropriately"
    - "Verify enhancement tests cover all new requirements"
    - "Validate test isolation between existing and new functionality"
    - "Confirm test failure messages are clear and actionable"

VALIDATION_CHECKPOINT_B.2:
  - [ ] Failing tests created for all enhancement features
  - [ ] Existing tests still pass (no regressions introduced)
  - [ ] Enhancement tests fail appropriately with clear messages
  - [ ] Tests cover all new requirements comprehensively
  - [ ] Ready to implement enhancements to make tests pass
```

### B.3: GREEN PHASE - Minimal Enhancement Implementation
```yaml
BLOCKING_VALIDATION_B.3:
  TYPE: "ENHANCEMENT_TESTS_PASSING"
  REQUIREMENT: "Minimal enhancement implementation makes all tests pass"
  FAILURE_ACTION: "STOP - Fix enhancement implementation until all tests pass"

TDD_GREEN_ACTIVITIES:
  MINIMAL_ENHANCEMENT:
    - "Add new attributes to changetype class with minimal implementation"
    - "Enhance SQL generator to handle new parameters and syntax"
    - "Add validation logic for new attributes and rules"
    - "Update service registration if needed for new functionality"
    - "Focus on making tests pass without over-engineering"
    
  VALIDATION:
    - "Run complete test suite including existing and enhancement tests"
    - "Confirm ALL tests pass (existing + new)"
    - "Verify no existing functionality was broken"
    - "Validate new SQL generation matches expected output"
    - "Ensure backward compatibility is maintained"

VALIDATION_CHECKPOINT_B.3:
  - [ ] Enhancement implementation complete and minimal
  - [ ] ALL tests pass (existing + enhancement tests)
  - [ ] No existing functionality broken (no regressions)
  - [ ] New SQL generation matches expected output
  - [ ] Backward compatibility maintained
```

## 🩺 SECTION C: REVIEW AND REPAIR TDD WORKFLOW

### C.1: Implementation Assessment and Issue Identification
```yaml
BLOCKING_VALIDATION_C.1:
  TYPE: "ISSUES_IDENTIFIED"
  REQUIREMENT: "All implementation issues systematically identified and categorized"
  FAILURE_ACTION: "STOP - Must understand all issues before repair"

ASSESSMENT_ACTIVITIES:
  COMPREHENSIVE_REVIEW:
    - "Run complete test suite and document all failures"
    - "Review implementation against original requirements"
    - "Identify gaps in functionality and test coverage"
    - "Analyze code quality and architectural issues"
    - "Check service registration and framework integration"
    
  ISSUE_CATEGORIZATION:
    - "Categorize issues: Functional/Performance/Quality/Integration"
    - "Prioritize issues by severity and impact"
    - "Identify root causes vs symptoms"
    - "Plan repair approach for each category"
    - "Estimate effort and complexity for repairs"

VALIDATION_CHECKPOINT_C.1:
  - [ ] All implementation issues identified and documented
  - [ ] Issues categorized by type and prioritized by severity
  - [ ] Root causes identified for all major issues
  - [ ] Repair approach planned for each issue category
  - [ ] Ready to begin systematic repair using TDD
```

### C.2: RED PHASE - Write Tests for Missing/Broken Functionality
```yaml
BLOCKING_VALIDATION_C.2:
  TYPE: "REPAIR_TESTS_FAILING"
  REQUIREMENT: "Comprehensive failing tests written for all identified issues"
  FAILURE_ACTION: "STOP - Must write failing tests for all issues before repair"

TDD_RED_ACTIVITIES:
  MISSING_FUNCTIONALITY_TESTS:
    - "Write failing tests for all missing functionality identified"
    - "Write failing tests for all broken scenarios"
    - "Write failing tests for all edge cases not covered"
    - "Write failing tests for all integration issues"
    - "Ensure tests clearly demonstrate the problems"
    
  TEST_VALIDATION:
    - "Run test suite and confirm repair tests fail as expected"
    - "Verify test failures clearly indicate the issues"
    - "Confirm tests cover all identified problems"
    - "Validate test scenarios match issue descriptions"
    - "Ensure tests are specific and actionable"

VALIDATION_CHECKPOINT_C.2:
  - [ ] Failing tests written for all identified issues
  - [ ] Test failures clearly demonstrate the problems
  - [ ] Tests cover all missing and broken functionality
  - [ ] Test scenarios are specific and actionable
  - [ ] Ready to repair implementation to make tests pass
```

## 🐛 SECTION E: BUG FIX TDD WORKFLOW

### E.1: Bug Analysis and Reproduction
```yaml
BLOCKING_VALIDATION_E.1:
  TYPE: "BUGS_REPRODUCED"
  REQUIREMENT: "All reported bugs systematically analyzed and reproduced with failing tests"
  FAILURE_ACTION: "STOP - Must reproduce bugs with tests before fixing"

BUG_ANALYSIS_ACTIVITIES:
  BUG_REPRODUCTION:
    - "Create failing tests that reproduce each reported bug"
    - "Analyze bug reports and map to specific functionality"
    - "Identify root causes through systematic debugging"
    - "Document bug scenarios and expected vs actual behavior"
    - "Categorize bugs by type and severity"
    
  ROOT_CAUSE_ANALYSIS:
    - "Use systematic debugging from error_patterns_guide.md"
    - "Identify whether bugs are in logic, SQL generation, validation, or integration"
    - "Trace bug causes through the complete execution path"
    - "Document fix approach for each bug category"
    - "Plan regression prevention strategies"

VALIDATION_CHECKPOINT_E.1:
  - [ ] All bugs reproduced with failing tests
  - [ ] Root causes identified through systematic debugging
  - [ ] Bug scenarios documented with expected behavior
  - [ ] Fix approach planned for each bug
  - [ ] Ready to implement bug fixes using TDD
```

### E.2: RED PHASE - Failing Tests for Bug Scenarios
```yaml
BLOCKING_VALIDATION_E.2:
  TYPE: "BUG_TESTS_FAILING"
  REQUIREMENT: "Comprehensive failing tests created for all bug scenarios"
  FAILURE_ACTION: "STOP - Must have failing tests before implementing fixes"

TDD_RED_ACTIVITIES:
  BUG_TEST_CREATION:
    - "Create failing tests for each bug scenario"
    - "Create failing tests for edge cases that revealed bugs"
    - "Create failing tests for regression prevention"
    - "Ensure tests clearly demonstrate the bug behavior"
    - "Verify tests fail consistently and reliably"
    
  TEST_VALIDATION:
    - "Run bug tests and confirm they fail as expected"
    - "Verify test failures match reported bug behavior"
    - "Confirm tests are specific to the bug scenarios"
    - "Validate tests will pass when bugs are fixed"
    - "Ensure no existing functionality is broken by new tests"

VALIDATION_CHECKPOINT_E.2:
  - [ ] Failing tests created for all bug scenarios
  - [ ] Test failures match reported bug behavior exactly
  - [ ] Tests are specific and will validate bug fixes
  - [ ] No existing functionality broken by new tests
  - [ ] Ready to implement bug fixes to make tests pass
```

## 🔧 COMMON TDD IMPLEMENTATION PATTERNS

### Test-First Development Protocol
```yaml
RED_PHASE_REQUIREMENTS:
  - "Write failing tests BEFORE any implementation code"
  - "Tests must cover ALL requirements scenarios"
  - "Tests must fail for the RIGHT reasons"
  - "Test failure messages must be clear and actionable"
  
GREEN_PHASE_REQUIREMENTS:
  - "Write MINIMAL code to make tests pass"
  - "No over-engineering or premature optimization"
  - "Focus on making tests pass, not perfect architecture"
  - "ALL tests must pass before proceeding to refactor"
  
REFACTOR_PHASE_REQUIREMENTS:
  - "Improve code quality while maintaining passing tests"
  - "Run tests after each refactoring change"
  - "Focus on maintainability and performance"
  - "Never break tests during refactoring"
```

### Systematic Debugging Protocol
```yaml
WHEN_TESTS_FAIL:
  STEP_1: "Don't assume where the bug is"
  STEP_2: "Use systematic debugging from error_patterns_guide.md"
  STEP_3: "Apply 5-layer analysis: Code → Registration → Execution → Data → Framework"
  STEP_4: "Fix root cause, don't change requirements"
  STEP_5: "Validate fix with complete test suite"
  
NEVER_DO:
  - "Change requirements to fit broken implementation"
  - "Lower test expectations to make tests pass"
  - "Skip test phases or validation checkpoints"
  - "Assume bugs without systematic analysis"
```

## ⚡ WORKFLOW AUTOMATION TOOLS

### Automated TDD Workflow Scripts
```bash
#!/bin/bash
# scripts/tdd-workflow.sh - Complete TDD workflow automation

SCENARIO=$1  # new|enhance|repair|complete|fix
DATABASE=$2
OBJECT=$3
REQUIREMENTS_FILE="requirements/${DATABASE}_${OBJECT}_requirements.md"

echo "🎯 Starting TDD Workflow: $SCENARIO for $DATABASE $OBJECT"

# Validate requirements exist for new/enhance scenarios
if [[ "$SCENARIO" == "new" || "$SCENARIO" == "enhance" ]]; then
    if [ ! -f "$REQUIREMENTS_FILE" ]; then
        echo "❌ ERROR: Requirements file not found: $REQUIREMENTS_FILE"
        echo "Must complete Phase 1 & 2 before TDD implementation"
        exit 1
    fi
    
    # Check IMPLEMENTATION_READY status
    if ! grep -q "IMPLEMENTATION_READY" "$REQUIREMENTS_FILE"; then
        echo "❌ ERROR: Requirements not marked IMPLEMENTATION_READY"
        echo "Must complete Phase 2 validation before TDD implementation"
        exit 1
    fi
fi

# Route to appropriate workflow section
case $SCENARIO in
    "new")
        echo "📋 Following Section A: New Implementation TDD Workflow"
        echo "A.1 Pre-Implementation Validation - [ ]"
        echo "A.2 RED Phase - Write Failing Tests First - [ ]"
        echo "A.3 GREEN Phase - Minimal Implementation - [ ]"
        echo "A.4 REFACTOR Phase - Improve Code Quality - [ ]"
        echo "A.5 Integration Testing and Harness Validation - [ ]"
        ;;
    "enhance")
        echo "📋 Following Section B: Enhancement TDD Workflow"
        echo "B.1 Existing Implementation Analysis - [ ]"
        echo "B.2 RED Phase - Enhancement Tests First - [ ]"
        echo "B.3 GREEN Phase - Minimal Enhancement Implementation - [ ]"
        ;;
    "repair")
        echo "📋 Following Section C: Review and Repair TDD Workflow"
        echo "C.1 Implementation Assessment and Issue Identification - [ ]"
        echo "C.2 RED Phase - Write Tests for Missing/Broken Functionality - [ ]"
        ;;
    "fix")
        echo "📋 Following Section E: Bug Fix TDD Workflow"
        echo "E.1 Bug Analysis and Reproduction - [ ]"
        echo "E.2 RED Phase - Failing Tests for Bug Scenarios - [ ]"
        ;;
    *)
        echo "❌ ERROR: Unknown scenario: $SCENARIO"
        echo "Valid scenarios: new|enhance|repair|complete|fix"
        exit 1
        ;;
esac

echo "🔄 Follow the appropriate workflow section in ai_workflow_guide.md"
```

## 🏆 ENHANCED WORKFLOWS - Operation-Type-Driven Architecture

**For changetypes with 2+ mutually exclusive operation modes**

### 🔍 SECTION A-ENHANCED: NEW IMPLEMENTATION WITH OPERATIONTYPE TDD WORKFLOW

#### A-Enhanced.1: OperationType Architecture Planning
```yaml
BLOCKING_VALIDATION_A_ENHANCED.1:
  TYPE: "OPERATION_MODE_ASSESSMENT"
  REQUIREMENT: "Identify and document all mutually exclusive operation modes"
  
PLANNING_ACTIVITIES:
  OPERATION_MODE_ANALYSIS:
    - "List all operation modes from requirements (e.g., RENAME, SET, UNSET)"
    - "Confirm mutual exclusivity between modes"
    - "Define operation-specific attributes for each mode"
    - "Plan validation rules per operation type"
    - "Design SQL generation patterns per mode"
    
VALIDATION_CHECKPOINT_A_ENHANCED.1:
  SUCCESS_CRITERIA:
    - "OperationType enum clearly defined with all modes"
    - "Operation-mode-to-attributes mapping documented"
    - "Mutual exclusivity rules documented"
    - "Validation approach per operation type planned"
  FAILURE_ACTION: "Return to requirements analysis - modes not clearly defined"
```

#### A-Enhanced.2: RED Phase - OperationType Test Creation
```yaml
TDD_RED_PHASE_A_ENHANCED.2:
  FOCUS: "Create comprehensive tests for all operation types"
  
RED_ACTIVITIES:
  OPERATION_TYPE_TESTS:
    - "Create unit tests for each OperationType enum value"
    - "Test mutual exclusivity validation (SET + UNSET should fail)"
    - "Test operation-specific validation rules"
    - "Test SQL generation per operation type"
    - "Test backward compatibility inference logic"
    
  TEST_ORGANIZATION:
    - "Group tests by operation type for clarity"
    - "Include comprehensive SQL string comparison tests"
    - "Add integration tests per operation type"
    
VALIDATION_CHECKPOINT_A_ENHANCED.2:
  SUCCESS_CRITERIA:
    - "All tests fail initially (RED phase)"
    - "Tests cover all operation types comprehensively"
    - "Operation-specific validation tests created"
    - "SQL generation tests per operation mode"
  BLOCKING_REQUIREMENT: "Cannot proceed until comprehensive failing tests exist"
```

#### A-Enhanced.3: GREEN Phase - OperationType Implementation
```yaml
TDD_GREEN_PHASE_A_ENHANCED.3:
  FOCUS: "Implement OperationType-driven architecture to pass tests"
  
GREEN_ACTIVITIES:
  STATEMENT_ENHANCEMENT:
    - "Add OperationType enum to Statement class"
    - "Implement operation-specific validation methods"
    - "Add inference logic for backward compatibility"
    - "Create ValidationResult inner class"
    
  GENERATOR_ENHANCEMENT:
    - "Implement switch-based SQL generation"
    - "Create operation-specific SQL generation methods"
    - "Add enhanced validation delegation"
    - "Handle multiple SQL statements if needed"
    
  CHANGE_CLASS_UPDATES:
    - "Add operationType property support"
    - "Update generateStatements with OperationType handling"
    
VALIDATION_CHECKPOINT_A_ENHANCED.3:
  SUCCESS_CRITERIA:
    - "All tests pass (GREEN phase)"
    - "Each operation type works independently"
    - "Mutual exclusivity properly enforced"
    - "Backward compatibility maintained"
```

#### A-Enhanced.4: REFACTOR Phase - Architecture Optimization
```yaml
TDD_REFACTOR_PHASE_A_ENHANCED.4:
  FOCUS: "Optimize OperationType architecture while maintaining test pass rate"
  
REFACTOR_ACTIVITIES:
  CODE_OPTIMIZATION:
    - "Extract common validation logic where appropriate"
    - "Optimize SQL generation templates"
    - "Improve error message consistency across operations"
    - "Enhance performance of operation type inference"
    
  TESTING_REFINEMENT:
    - "Ensure test coverage for all operation combinations"
    - "Optimize test execution speed"
    - "Add edge case coverage per operation type"
    
VALIDATION_CHECKPOINT_A_ENHANCED.4:
  SUCCESS_CRITERIA:
    - "All tests still pass after refactoring"
    - "Code quality improved"
    - "Architecture is extensible for new operation types"
    - "Performance is acceptable"
```

### 🔧 SECTION B-ENHANCED: OPERATIONTYPE ENHANCEMENT TDD WORKFLOW

#### B-Enhanced.1: Existing Implementation Analysis
```yaml
ANALYSIS_ACTIVITIES:
  CURRENT_ARCHITECTURE_ASSESSMENT:
    - "Determine if existing implementation uses OperationType pattern"
    - "Identify current operation modes (explicit or implicit)"
    - "Assess mutual exclusivity handling in current code"
    - "Evaluate validation approach quality"
    
  ENHANCEMENT_PLANNING:
    - "Plan migration from property-based to OperationType architecture"
    - "Design backward compatibility preservation strategy"
    - "Identify new operation modes to add"
```

### 🔧 Key Success Factors for OperationType Implementations

```yaml
PROVEN_SUCCESS_PATTERNS:
  ENUM_DESIGN:
    - "Clear, descriptive enum values (RENAME, SET, UNSET)"
    - "Comprehensive documentation per enum value"
    - "Future-extensible design"
    
  VALIDATION_ARCHITECTURE:
    - "Operation-specific validation methods"
    - "Comprehensive mutual exclusivity checking"
    - "Clear error messages per operation type"
    
  SQL_GENERATION:
    - "Switch-based generation with operation-specific methods"
    - "Clean separation of SQL logic per operation"
    - "Proper parameter handling per operation type"
    
  BACKWARD_COMPATIBILITY:
    - "Inference logic that detects operation types from properties"
    - "Graceful handling of mixed property combinations"
    - "Clear error messages when inference fails"
    
REAL_WORLD_SUCCESS_METRICS:
  ALTER_WAREHOUSE_EXAMPLE:
    - "22/22 tests passing (12 integration + 10 unit)"
    - "7 operation types successfully implemented"
    - "Live database validation successful"
    - "71% quality improvement over property-based approach"
```

## 🔗 CROSS-REFERENCES AND NAVIGATION

### Related Documents
```yaml
WORKFLOW_SEQUENCE:
  PHASE_1: "ai_requirements_research.md - Requirements Research (COMPLETED)"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation (COMPLETED)" 
  PHASE_3: "THIS DOCUMENT - TDD Implementation"

IMPLEMENTATION_PATTERNS:
  NEW_CHANGETYPE: "changetype_patterns.md - New Changetype Pattern"
  EXTENSION: "changetype_patterns.md - Extension Pattern"
  SQL_OVERRIDE: "sql_generator_overrides.md - SQL Override Pattern"
  TESTING: "test_harness_guide.md - Integration Testing"

DEBUGGING_SUPPORT:
  ERROR_PATTERNS: "../snapshot_diff_implementation/error_patterns_guide.md"
  SYSTEMATIC_DEBUGGING: "../snapshot_diff_implementation/main_guide.md"
  OVERALL_NAVIGATION: "README.md"
```

### Decision Trees for Implementation Routing
```yaml
IMPLEMENTATION_PATTERN_ROUTING:
  IF_NEW_OBJECT_NOT_IN_LIQUIBASE:
    ROUTE_TO: "changetype_patterns.md → New Changetype Pattern"
    TDD_FOCUS: "Complete changetype lifecycle implementation"
    
  IF_ADD_DATABASE_ATTRIBUTES:
    ROUTE_TO: "changetype_patterns.md → Extension Pattern"
    TDD_FOCUS: "Database-specific attribute additions"
    
  IF_ONLY_SQL_SYNTAX_DIFFERENT:
    ROUTE_TO: "sql_generator_overrides.md → SQL Override Pattern"
    TDD_FOCUS: "Database-specific SQL generation"
```

## 🎯 TDD IMPLEMENTATION SUCCESS CRITERIA

### Successful TDD Implementation Indicators
```yaml
TEST_QUALITY:
  - "Tests written before implementation code in all phases"
  - "Complete test coverage for all requirements scenarios"
  - "Tests fail appropriately and pass when implementation is correct"
  
IMPLEMENTATION_QUALITY:
  - "Minimal implementation that makes all tests pass"
  - "Code refactored for quality while maintaining passing tests"
  - "Integration tests validate complete end-to-end functionality"
  
PROCESS_ADHERENCE:
  - "RED-GREEN-REFACTOR cycle followed strictly"
  - "No implementation code written before failing tests"
  - "All validation checkpoints passed"
```

## 💡 TDD WORKFLOW BEST PRACTICES

### Test-Driven Development Excellence
1. **Tests First Always**: Never write implementation code before failing tests
2. **Minimal Implementation**: Write only enough code to make tests pass
3. **Refactor Fearlessly**: Improve code quality while maintaining green tests
4. **Systematic Debugging**: Use error patterns and 5-layer analysis when issues arise
5. **Continuous Validation**: Run tests after every change

### Common TDD Pitfalls to Avoid
```yaml
IMPLEMENTATION_BEFORE_TESTS:
  PROBLEM: "Writing implementation code before failing tests"
  SOLUTION: "Always write failing tests first in RED phase"
  
OVER_ENGINEERING:
  PROBLEM: "Writing complex implementation beyond what tests require"
  SOLUTION: "Write minimal code to make tests pass, refactor later"
  
CHANGING_REQUIREMENTS:
  PROBLEM: "Modifying requirements when tests fail"
  SOLUTION: "Fix implementation or identify missing requirements, don't change existing ones"
  
SKIPPING_REFACTOR:
  PROBLEM: "Not improving code quality after making tests pass"
  SOLUTION: "Always refactor for quality while maintaining green tests"
```

Remember: **This intelligent workflow guide routes Claude through the optimal TDD path based on the specific implementation scenario**. The decision tree at the beginning determines the appropriate workflow section, and strict TDD discipline ensures high-quality, well-tested implementations. Every scenario follows the RED-GREEN-REFACTOR cycle with systematic validation checkpoints.