# AI Workflow Guide - Snapshot/Diff Implementation
## Phase 3: Test-Driven Development (TDD) Implementation with Decision Tree Navigation

## EXECUTION_PROTOCOL
```yaml
PHASE: 3_TDD_IMPLEMENTATION
PROTOCOL_VERSION: 3.0
EXECUTION_MODE: SEQUENTIAL_BLOCKING_TDD
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
INPUT_REQUIRED: "[object]_snapshot_diff_requirements.md (IMPLEMENTATION_READY from Phase 2)"
DELIVERABLE: "Complete tested snapshot/diff implementation with comprehensive coverage"
APPROACH: "RED_GREEN_REFACTOR"
```

## WORKFLOW_OVERVIEW
```yaml
PURPOSE: "Intelligent workflow guide that leads Claude through decision points to implement, enhance, complete, or fix snapshot/diff implementations using strict TDD"
INPUT: "Complete snapshot/diff requirements document from Phase 2 with IMPLEMENTATION_READY status"
OUTPUT: "Fully tested, working snapshot/diff implementation with comprehensive test coverage"
DURATION: "6-10 hours depending on complexity and scenario"
CRITICAL_SUCCESS_FACTOR: "Tests drive implementation - object models and snapshot/diff logic exist only to make tests pass"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/snapshot_diff_implementation/ai_workflow_guide.md"
WORKFLOW_SEQUENCE:
  PHASE_1: "ai_requirements_research.md - Requirements Research (COMPLETED)"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation (COMPLETED)"
  PHASE_3: "THIS DOCUMENT - TDD Implementation"
  
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for snapshot/diff implementation"
  - main_guide.md: "Overview and systematic debugging framework"
  - error_patterns_guide.md: "Comprehensive error debugging patterns"
  - ../changetype_implementation/ai_workflow_guide.md: "Parallel TDD workflow for changetypes"
```

## 🎯 INTELLIGENT DECISION TREE - WHAT ARE YOU TRYING TO ACCOMPLISH?

```yaml
DECISION_POINT_1: "What is your snapshot/diff implementation scenario?"

SCENARIO_A_NEW_OBJECT:
  DESCRIPTION: "Implement snapshot/diff for a completely new database object"
  REQUIREMENTS: "Must have [object]_snapshot_diff_requirements.md (IMPLEMENTATION_READY)"
  WORKFLOW: "Go to Section A: New Object Snapshot/Diff TDD Workflow"
  DURATION: "8-10 hours"
  
SCENARIO_B_ENHANCE_EXISTING:
  DESCRIPTION: "Enhance existing object snapshot/diff with additional properties or capabilities"
  REQUIREMENTS: "Must have [object]_snapshot_diff_requirements.md (IMPLEMENTATION_READY)"
  WORKFLOW: "Go to Section B: Enhancement TDD Workflow"
  DURATION: "4-6 hours"
  
SCENARIO_C_COMPLETE_INCOMPLETE:
  DESCRIPTION: "Complete an incomplete snapshot/diff implementation"
  REQUIREMENTS: "Access to existing partial implementation + requirements"
  WORKFLOW: "Go to Section C: Completion TDD Workflow"
  DURATION: "5-7 hours"
  
SCENARIO_D_FIX_BUGS:
  DESCRIPTION: "Fix bugs in existing snapshot/diff implementation"
  REQUIREMENTS: "Access to existing implementation + bug reports/test failures"
  WORKFLOW: "Go to Section D: Bug Fix TDD Workflow"
  DURATION: "2-4 hours"
  
SCENARIO_E_PERFORMANCE_OPTIMIZATION:
  DESCRIPTION: "Optimize performance of existing snapshot/diff implementation"
  REQUIREMENTS: "Access to existing implementation + performance requirements"
  WORKFLOW: "Go to Section E: Performance Optimization TDD Workflow"
  DURATION: "3-5 hours"
  
SCENARIO_F_FRAMEWORK_COMPATIBILITY:
  DESCRIPTION: "Fix framework integration or test harness compatibility issues"
  REQUIREMENTS: "Access to existing implementation + framework compatibility requirements"
  WORKFLOW: "Go to Section F: Framework Compatibility TDD Workflow"
  DURATION: "2-4 hours"
```

## 🔍 SECTION A: NEW OBJECT SNAPSHOT/DIFF TDD WORKFLOW

### A.1: Pre-Implementation Validation and Framework Scope Assessment
```yaml
BLOCKING_VALIDATION_A.1:
  TYPE: "REQUIREMENTS_AND_SCOPE_READY"
  REQUIREMENT: "Requirements document ready and framework scope validated for realistic success criteria"
  FAILURE_ACTION: "STOP - Return to Phase 2 or validate framework scope before implementation"

VALIDATION_ACTIVITIES:
  REQUIREMENTS_CHECK:
    - "Verify [object]_snapshot_diff_requirements.md exists and is marked IMPLEMENTATION_READY"
    - "Confirm all sections complete (object model, snapshot SQL, comparison logic)"
    - "Validate framework integration requirements are actionable"
    - "Check that all quality gates from Phase 2 are passed"
    - "Ensure test scenarios are comprehensive and realistic"
    
  FRAMEWORK_SCOPE_VALIDATION:
    - "Test database object creation and existence validation"
    - "Validate test harness scope includes this object type (if applicable)"
    - "Set realistic success criteria based on framework capabilities"
    - "Document scope limitations if object type not in default test harness scope"
    - "Establish appropriate testing approach based on scope validation"

VALIDATION_CHECKPOINT_A.1:
  - [ ] Requirements document exists and marked IMPLEMENTATION_READY
  - [ ] All sections complete and comprehensive
  - [ ] Framework scope validated and realistic success criteria established
  - [ ] Testing approach confirmed based on scope validation
  - [ ] Ready to begin snapshot/diff TDD implementation
```

### A.2: RED PHASE - Write Failing Tests for Object Model First
```yaml
BLOCKING_VALIDATION_A.2:
  TYPE: "OBJECT_MODEL_TESTS_FAILING"
  REQUIREMENT: "Complete failing test suite for object model written before any implementation code"
  FAILURE_ACTION: "STOP - Must write all failing object model tests before implementation"

TDD_RED_ACTIVITIES:
  OBJECT_MODEL_TEST_CREATION:
    - "Create failing unit tests for all object properties and categorization"
    - "Create failing tests for object construction and property access"
    - "Create failing tests for property comparison and equals/hashCode logic"
    - "Create failing tests for object serialization and string representation"
    - "Ensure ALL object model tests fail initially (no implementation exists yet)"
    
  TEST_VALIDATION:
    - "Run object model test suite and confirm ALL tests fail appropriately"
    - "Verify test failure messages are clear and informative"
    - "Confirm tests cover all properties from requirements"
    - "Validate test structure follows TDD best practices"
    - "Ensure tests are independent and can run in any order"

VALIDATION_CHECKPOINT_A.2:
  - [ ] Complete failing test suite created for object model
  - [ ] All tests fail appropriately with clear failure messages
  - [ ] Tests cover all properties and behaviors from requirements
  - [ ] Test suite runs successfully (all failing as expected)
  - [ ] Ready to implement object model to make tests pass
```

### A.3: GREEN PHASE - Minimal Object Model Implementation
```yaml
BLOCKING_VALIDATION_A.3:
  TYPE: "OBJECT_MODEL_TESTS_PASSING"
  REQUIREMENT: "Minimal object model implementation makes all tests pass"
  FAILURE_ACTION: "STOP - Fix object model implementation until all tests pass"

TDD_GREEN_ACTIVITIES:
  MINIMAL_OBJECT_MODEL:
    - "Implement database object class with minimal code to pass property tests"
    - "Add property categorization (Required/Optional/State/Structural/Metadata)"
    - "Implement comparison logic excluding state properties"
    - "Add service registration for DatabaseObject interface"
    - "Focus on making tests pass, not on perfect architecture"
    
  TEST_VALIDATION:
    - "Run object model test suite and confirm ALL tests now pass"
    - "Verify no tests were modified to make them pass"
    - "Confirm object model handles all properties from requirements"
    - "Validate property categorization is correct for comparison logic"
    - "Ensure service registration enables framework recognition"

VALIDATION_CHECKPOINT_A.3:
  - [ ] Minimal object model implementation complete
  - [ ] ALL object model tests now pass without modification
  - [ ] Property categorization correct for comparison logic
  - [ ] Service registration complete for framework recognition
  - [ ] Ready to begin snapshot implementation TDD phase
```

### A.4: RED PHASE - Write Failing Tests for Snapshot Generator
```yaml
BLOCKING_VALIDATION_A.4:
  TYPE: "SNAPSHOT_TESTS_FAILING"
  REQUIREMENT: "Complete failing test suite for snapshot generator written before implementation"
  FAILURE_ACTION: "STOP - Must write all failing snapshot tests before implementation"

TDD_RED_ACTIVITIES:
  SNAPSHOT_TEST_CREATION:
    - "Create failing tests for all snapshot SQL query patterns from requirements"
    - "Create failing tests for ResultSet parsing and object creation"
    - "Create failing tests for batch and single object retrieval"
    - "Create failing tests for snapshot accuracy and completeness"
    - "Create failing tests for error handling and edge cases"
    
  TEST_VALIDATION:
    - "Run snapshot test suite and confirm ALL tests fail appropriately"
    - "Verify test failures demonstrate missing snapshot functionality"
    - "Confirm tests cover all SQL patterns from requirements"
    - "Validate tests check for accurate object property population"
    - "Ensure tests cover error conditions and edge cases"

VALIDATION_CHECKPOINT_A.4:
  - [ ] Complete failing test suite created for snapshot generator
  - [ ] All tests fail appropriately demonstrating missing functionality
  - [ ] Tests cover all SQL patterns and scenarios from requirements
  - [ ] Tests validate accurate object property population
  - [ ] Ready to implement snapshot generator to make tests pass
```

### A.5: GREEN PHASE - Minimal Snapshot Generator Implementation
```yaml
BLOCKING_VALIDATION_A.5:
  TYPE: "SNAPSHOT_TESTS_PASSING"
  REQUIREMENT: "Minimal snapshot generator implementation makes all tests pass"
  FAILURE_ACTION: "STOP - Fix snapshot generator until all tests pass"

TDD_GREEN_ACTIVITIES:
  MINIMAL_SNAPSHOT_GENERATOR:
    - "Implement SnapshotGenerator with minimal code to pass SQL query tests"
    - "Add ResultSet parsing logic to create database objects correctly"
    - "Implement batch and single object retrieval as required by tests"
    - "Add error handling for database connectivity and query failures"
    - "Focus on making tests pass with correct object population"
    
  TEST_VALIDATION:
    - "Run snapshot test suite and confirm ALL tests now pass"
    - "Verify SQL queries execute correctly and return expected results"
    - "Confirm ResultSet parsing creates objects with correct properties"
    - "Validate batch retrieval works efficiently and accurately"
    - "Ensure error handling works as specified in tests"

VALIDATION_CHECKPOINT_A.5:
  - [ ] Minimal snapshot generator implementation complete
  - [ ] ALL snapshot tests now pass without modification
  - [ ] SQL queries execute correctly with accurate results
  - [ ] ResultSet parsing creates correctly populated objects
  - [ ] Ready to begin diff implementation TDD phase
```

### A.6: RED PHASE - Write Failing Tests for Diff and Comparison Logic
```yaml
BLOCKING_VALIDATION_A.6:
  TYPE: "DIFF_TESTS_FAILING"
  REQUIREMENT: "Complete failing test suite for diff and comparison logic written before implementation"
  FAILURE_ACTION: "STOP - Must write all failing diff tests before implementation"

TDD_RED_ACTIVITIES:
  DIFF_TEST_CREATION:
    - "Create failing tests for all property comparison scenarios from requirements"
    - "Create failing tests for diff detection with all change combinations"
    - "Create failing tests for state property exclusion from comparisons"
    - "Create failing tests for complex comparison scenarios and edge cases"
    - "Create failing tests for diff result format and change categorization"
    
  TEST_VALIDATION:
    - "Run diff test suite and confirm ALL tests fail appropriately"
    - "Verify test failures demonstrate missing comparison functionality"
    - "Confirm tests cover all comparison scenarios from requirements"
    - "Validate tests check for proper state property exclusion"
    - "Ensure tests validate diff result format and categorization"

VALIDATION_CHECKPOINT_A.6:
  - [ ] Complete failing test suite created for diff and comparison logic
  - [ ] All tests fail appropriately demonstrating missing functionality
  - [ ] Tests cover all comparison scenarios from requirements
  - [ ] Tests validate state property exclusion and diff formatting
  - [ ] Ready to implement diff logic to make tests pass
```

### A.7: GREEN PHASE - Minimal Diff and Comparison Implementation
```yaml
BLOCKING_VALIDATION_A.7:
  TYPE: "DIFF_TESTS_PASSING"
  REQUIREMENT: "Minimal diff and comparison implementation makes all tests pass"
  FAILURE_ACTION: "STOP - Fix diff implementation until all tests pass"

TDD_GREEN_ACTIVITIES:
  MINIMAL_DIFF_IMPLEMENTATION:
    - "Implement DatabaseObjectComparator with minimal code to pass comparison tests"
    - "Add property comparison logic excluding state properties as specified"
    - "Implement diff result creation with proper change categorization"
    - "Add complex comparison handling for collections and nested objects"
    - "Focus on making tests pass with accurate diff detection"
    
  TEST_VALIDATION:
    - "Run diff test suite and confirm ALL tests now pass"
    - "Verify property comparisons work correctly with state exclusion"
    - "Confirm diff results are formatted correctly with proper categorization"
    - "Validate complex comparison scenarios work as expected"
    - "Ensure diff detection is accurate for all change combinations"

VALIDATION_CHECKPOINT_A.7:
  - [ ] Minimal diff and comparison implementation complete
  - [ ] ALL diff tests now pass without modification
  - [ ] Property comparisons work correctly with state exclusion
  - [ ] Diff results formatted correctly with proper categorization
  - [ ] Ready to begin refactoring and integration testing
```

### A.8: REFACTOR PHASE - Improve Code Quality While Maintaining Green Tests
```yaml
BLOCKING_VALIDATION_A.8:
  TYPE: "REFACTORED_WITH_PASSING_TESTS"
  REQUIREMENT: "Code quality improved through refactoring while maintaining all passing tests"
  FAILURE_ACTION: "STOP - Refactoring broke tests, must fix before proceeding"

TDD_REFACTOR_ACTIVITIES:
  CODE_QUALITY_IMPROVEMENT:
    - "Refactor object model for better structure and maintainability"
    - "Refactor snapshot generator for better performance and readability"
    - "Refactor comparison logic for clarity and efficiency"
    - "Extract common patterns and eliminate code duplication"
    - "Add comprehensive documentation and comments"
    
  CONTINUOUS_VALIDATION:
    - "Run complete test suite after each refactoring change"
    - "Ensure ALL tests remain passing throughout refactoring"
    - "Verify refactoring improves code quality without changing behavior"
    - "Confirm performance improvements where applicable"
    - "Validate code follows project conventions and patterns"

VALIDATION_CHECKPOINT_A.8:
  - [ ] Code refactored for improved quality and maintainability
  - [ ] ALL tests remain passing after refactoring
  - [ ] Code duplication eliminated and patterns extracted
  - [ ] Performance optimized where applicable
  - [ ] Ready for integration testing phase
```

### A.9: Integration Testing with Realistic Success Criteria
```yaml
BLOCKING_VALIDATION_A.9:
  TYPE: "INTEGRATION_TESTS_COMPLETE"
  REQUIREMENT: "Integration tests complete with realistic success criteria based on framework scope"
  FAILURE_ACTION: "STOP - Fix integration issues using systematic debugging"

INTEGRATION_ACTIVITIES:
  FRAMEWORK_INTEGRATION_TESTING:
    - "Test object creation and database state validation"
    - "Test snapshot functionality against real database"
    - "Test diff functionality with actual object changes"
    - "Validate service registration and framework recognition"
    - "Apply realistic success criteria based on framework scope validation"
    
  SUCCESS_CRITERIA_APPLICATION:
    - "If in test harness scope: Full test harness pass with snapshot validation"
    - "If not in scope: Changesets execute + objects created + manual verification"
    - "Document any scope limitations and alternative validation approaches"
    - "Use systematic debugging if integration tests fail"
    - "Apply error patterns from error_patterns_guide.md"

VALIDATION_CHECKPOINT_A.9:
  - [ ] Integration tests complete with appropriate success criteria
  - [ ] Framework integration working correctly
  - [ ] Snapshot and diff functionality validated against real database
  - [ ] Any scope limitations documented with alternative validation
  - [ ] Implementation complete and fully validated
```

## 🔧 SECTION B: ENHANCEMENT TDD WORKFLOW

### B.1: Existing Implementation Analysis and Enhancement Planning
```yaml
BLOCKING_VALIDATION_B.1:
  TYPE: "EXISTING_ANALYSIS_COMPLETE"
  REQUIREMENT: "Existing snapshot/diff implementation analyzed and enhancement requirements validated"
  FAILURE_ACTION: "STOP - Must understand existing implementation before enhancement"

ANALYSIS_ACTIVITIES:
  EXISTING_CODE_REVIEW:
    - "Analyze existing object model structure and property handling"
    - "Review existing snapshot SQL and ResultSet parsing logic"
    - "Understand current comparison logic and diff generation"
    - "Identify integration points and service registration patterns"
    - "Map current functionality against enhancement requirements"
    
  ENHANCEMENT_VALIDATION:
    - "Validate enhancement requirements are complete and actionable"
    - "Confirm new properties/features don't conflict with existing functionality"
    - "Identify impact on comparison logic and diff detection"
    - "Plan backward compatibility and migration strategy"
    - "Assess impact on existing tests and functionality"

VALIDATION_CHECKPOINT_B.1:
  - [ ] Existing implementation thoroughly analyzed and understood
  - [ ] Enhancement requirements validated against existing functionality
  - [ ] Impact on comparison logic and diff detection assessed
  - [ ] Backward compatibility strategy planned
  - [ ] Ready to begin enhancement TDD implementation
```

## 🐛 SECTION D: BUG FIX TDD WORKFLOW

### D.1: Bug Analysis and Reproduction for Snapshot/Diff Issues
```yaml
BLOCKING_VALIDATION_D.1:
  TYPE: "BUGS_REPRODUCED"
  REQUIREMENT: "All reported snapshot/diff bugs systematically analyzed and reproduced with failing tests"
  FAILURE_ACTION: "STOP - Must reproduce bugs with tests before fixing"

BUG_ANALYSIS_ACTIVITIES:
  SNAPSHOT_DIFF_BUG_REPRODUCTION:
    - "Create failing tests that reproduce each reported bug"
    - "Analyze bug reports and map to specific functionality (object model/snapshot/diff)"
    - "Test against real database to validate bug behavior"
    - "Document bug scenarios with expected vs actual behavior"
    - "Categorize bugs by component (object model/snapshot generator/comparator)"
    
  ROOT_CAUSE_ANALYSIS:
    - "Use systematic debugging from error_patterns_guide.md"
    - "Identify whether bugs are in SQL queries, object creation, or comparison logic"
    - "Trace bug causes through snapshot → object → comparison execution path"
    - "Check service registration and framework integration issues"
    - "Document fix approach for each bug category"

VALIDATION_CHECKPOINT_D.1:
  - [ ] All bugs reproduced with failing tests
  - [ ] Root causes identified through systematic debugging
  - [ ] Bug scenarios documented with expected behavior
  - [ ] Bugs categorized by component (object model/snapshot/diff)
  - [ ] Ready to implement bug fixes using TDD
```

## 🔧 SECTION F: FRAMEWORK COMPATIBILITY TDD WORKFLOW

### F.1: Framework Integration Analysis and Compatibility Requirements
```yaml
BLOCKING_VALIDATION_F.1:
  TYPE: "FRAMEWORK_ISSUES_IDENTIFIED"
  REQUIREMENT: "All framework compatibility issues identified and realistic solutions planned"
  FAILURE_ACTION: "STOP - Must understand framework limitations before attempting fixes"

COMPATIBILITY_ANALYSIS_ACTIVITIES:
  FRAMEWORK_INTEGRATION_REVIEW:
    - "Analyze service registration and META-INF files"
    - "Test object recognition by Liquibase framework"
    - "Validate test harness scope inclusion/exclusion"
    - "Check interface implementation completeness"
    - "Document framework limitations and constraints"
    
  REALISTIC_SOLUTION_PLANNING:
    - "Identify which issues can be resolved vs framework limitations"
    - "Plan service registration fixes and interface implementations"
    - "Establish realistic success criteria based on framework capabilities"
    - "Document alternative validation approaches for scope limitations"
    - "Plan systematic approach to framework compatibility improvements"

VALIDATION_CHECKPOINT_F.1:
  - [ ] All framework compatibility issues identified
  - [ ] Realistic solutions planned vs framework limitations documented
  - [ ] Service registration and interface issues mapped
  - [ ] Alternative validation approaches planned for limitations
  - [ ] Ready to implement framework compatibility improvements
```

## 🚨 CRITICAL SNAPSHOT/DIFF TDD PROTOCOLS

### Test-First Development for Snapshot/Diff
```yaml
RED_PHASE_REQUIREMENTS:
  - "Write failing tests for object model BEFORE implementing classes"
  - "Write failing tests for snapshot SQL BEFORE implementing generators"
  - "Write failing tests for comparison logic BEFORE implementing comparators"
  - "Tests must cover ALL property categories and comparison scenarios"
  
GREEN_PHASE_REQUIREMENTS:
  - "Write MINIMAL code to make object model tests pass"
  - "Write MINIMAL code to make snapshot tests pass with correct SQL"
  - "Write MINIMAL code to make comparison tests pass with accurate diffs"
  - "Focus on making tests pass, not perfect architecture"
  
REFACTOR_PHASE_REQUIREMENTS:
  - "Improve object model structure while maintaining passing tests"
  - "Optimize snapshot SQL performance while maintaining accuracy"
  - "Refactor comparison logic for clarity while maintaining correctness"
  - "Never break tests during refactoring"
```

### Framework Scope Validation Protocol
```yaml
CRITICAL_EARLY_VALIDATION:
  STEP_1: "Create simple test object in database"
  STEP_2: "Test if object appears in default snapshot scope"
  STEP_3: "Set realistic success criteria based on scope results"
  STEP_4: "Document scope limitations if object not included"
  STEP_5: "Plan alternative validation approaches for out-of-scope objects"
  
SUCCESS_CRITERIA_BY_SCOPE:
  IF_IN_SCOPE:
    SUCCESS: "Full test harness pass with snapshot validation"
    VALIDATION: "Objects appear in snapshots and diffs work correctly"
    
  IF_NOT_IN_SCOPE:
    SUCCESS: "Changesets execute + objects created + manual verification"
    VALIDATION: "Direct database queries confirm object creation and changes"
    DOCUMENTATION: "Create scope limitation documentation"
```

## ⚡ SNAPSHOT/DIFF WORKFLOW AUTOMATION TOOLS

### Automated Snapshot/Diff TDD Scripts
```bash
#!/bin/bash
# scripts/snapshot-diff-tdd-workflow.sh - Complete snapshot/diff TDD automation

SCENARIO=$1  # new|enhance|complete|fix|performance|framework
DATABASE=$2
OBJECT=$3
REQUIREMENTS_FILE="requirements/${DATABASE}_${OBJECT}_snapshot_diff_requirements.md"

echo "🎯 Starting Snapshot/Diff TDD Workflow: $SCENARIO for $DATABASE $OBJECT"

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
        echo "📋 Following Section A: New Object Snapshot/Diff TDD Workflow"
        echo "A.1 Pre-Implementation Validation and Framework Scope Assessment - [ ]"
        echo "A.2 RED Phase - Write Failing Tests for Object Model First - [ ]"
        echo "A.3 GREEN Phase - Minimal Object Model Implementation - [ ]"
        echo "A.4 RED Phase - Write Failing Tests for Snapshot Generator - [ ]"
        echo "A.5 GREEN Phase - Minimal Snapshot Generator Implementation - [ ]"
        echo "A.6 RED Phase - Write Failing Tests for Diff and Comparison Logic - [ ]"
        echo "A.7 GREEN Phase - Minimal Diff and Comparison Implementation - [ ]"
        echo "A.8 REFACTOR Phase - Improve Code Quality - [ ]"
        echo "A.9 Integration Testing with Realistic Success Criteria - [ ]"
        ;;
    "enhance")
        echo "📋 Following Section B: Enhancement TDD Workflow"
        echo "B.1 Existing Implementation Analysis and Enhancement Planning - [ ]"
        ;;
    "fix")
        echo "📋 Following Section D: Bug Fix TDD Workflow"
        echo "D.1 Bug Analysis and Reproduction for Snapshot/Diff Issues - [ ]"
        ;;
    "framework")
        echo "📋 Following Section F: Framework Compatibility TDD Workflow"
        echo "F.1 Framework Integration Analysis and Compatibility Requirements - [ ]"
        ;;
    *)
        echo "❌ ERROR: Unknown scenario: $SCENARIO"
        echo "Valid scenarios: new|enhance|complete|fix|performance|framework"
        exit 1
        ;;
esac

echo "🔄 Follow the appropriate workflow section in ai_workflow_guide.md"
echo "🎯 Remember to validate framework scope early for realistic success criteria"
```

## 🔗 CROSS-REFERENCES AND NAVIGATION

### Related Documents
```yaml
WORKFLOW_SEQUENCE:
  PHASE_1: "ai_requirements_research.md - Requirements Research (COMPLETED)"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation (COMPLETED)"
  PHASE_3: "THIS DOCUMENT - TDD Implementation"

IMPLEMENTATION_SUPPORT:
  OBJECT_MODEL: "part1_object_model.md - Database object model patterns"
  SNAPSHOT_LOGIC: "part2_snapshot_implementation.md - SnapshotGenerator patterns"
  DIFF_LOGIC: "part3_diff_implementation.md - Comparator and diff patterns"
  TESTING_GUIDE: "part4_testing_guide.md - Testing with framework limitations"

DEBUGGING_SUPPORT:
  ERROR_PATTERNS: "error_patterns_guide.md - Comprehensive error debugging"
  SYSTEMATIC_DEBUGGING: "main_guide.md - 5-layer debugging framework"
  OVERALL_NAVIGATION: "README.md"
```

### Decision Trees for Implementation Routing
```yaml
OBJECT_COMPLEXITY_ROUTING:
  IF_SIMPLE_OBJECT:
    FOCUS: "Basic property handling and straightforward comparison logic"
    TDD_EMPHASIS: "Clear object model and efficient snapshot SQL"
    
  IF_COMPLEX_OBJECT:
    FOCUS: "Advanced property relationships and complex comparison scenarios"
    TDD_EMPHASIS: "Robust comparison logic and performance optimization"
    
  IF_FRAMEWORK_INTEGRATION_ISSUES:
    FOCUS: "Service registration and realistic success criteria"
    TDD_EMPHASIS: "Framework compatibility and alternative validation approaches"
```

## 🎯 SNAPSHOT/DIFF TDD IMPLEMENTATION SUCCESS CRITERIA

### Successful TDD Implementation Indicators
```yaml
OBJECT_MODEL_QUALITY:
  - "Object model tests written before implementation classes"
  - "All properties correctly categorized for comparison purposes"
  - "Property access and comparison logic thoroughly tested"
  
SNAPSHOT_FUNCTIONALITY:
  - "Snapshot tests written before generator implementation"
  - "SQL queries accurate and performant for all scenarios"
  - "ResultSet parsing creates correctly populated objects"
  
DIFF_FUNCTIONALITY:
  - "Comparison tests written before comparator implementation"
  - "State properties correctly excluded from comparisons"
  - "Diff results accurate for all change combinations"
  
FRAMEWORK_INTEGRATION:
  - "Realistic success criteria established based on scope validation"
  - "Service registration complete and framework recognition working"
  - "Integration tests validate end-to-end functionality appropriately"
```

## 💡 SNAPSHOT/DIFF TDD WORKFLOW BEST PRACTICES

### Test-Driven Development Excellence for Snapshot/Diff
1. **Object Model First**: Always start with object model tests before snapshot/diff logic
2. **Property Categorization**: Test property categorization thoroughly for comparison accuracy
3. **SQL Validation**: Validate snapshot SQL against real database in tests
4. **Framework Scope**: Validate framework scope early and set realistic success criteria
5. **Systematic Debugging**: Use error patterns and 5-layer analysis for integration issues

### Common Snapshot/Diff TDD Pitfalls to Avoid
```yaml
UNREALISTIC_EXPECTATIONS:
  PROBLEM: "Expecting test harness to include custom objects without validation"
  SOLUTION: "Validate framework scope early and set realistic success criteria"
  
MISSING_PROPERTY_CATEGORIZATION:
  PROBLEM: "Not testing property categorization for comparison logic"
  SOLUTION: "Write tests that verify state properties are excluded from comparisons"
  
UNTESTED_SQL_QUERIES:
  PROBLEM: "Not testing snapshot SQL against real database"
  SOLUTION: "Test SQL queries in realistic database environments during TDD"
  
FRAMEWORK_ASSUMPTIONS:
  PROBLEM: "Assuming framework integration will work without testing"
  SOLUTION: "Test service registration and framework recognition systematically"
```

### Snapshot/Diff TDD Excellence
```yaml
OBJECT_MODEL_PRINCIPLES:
  - "Every property tested for correct categorization and comparison behavior"
  - "Object lifecycle thoroughly understood through comprehensive testing"
  - "Property relationships and dependencies validated with tests"
  
SNAPSHOT_DIFF_PRINCIPLES:
  - "Every snapshot SQL pattern tested against real database"
  - "Every comparison scenario tested with expected diff results"
  - "Framework integration tested with realistic success criteria"
  
QUALITY_PRINCIPLES:
  - "Tests drive all implementation decisions"
  - "Realistic success criteria based on framework capabilities"
  - "Systematic debugging applied to all integration issues"
```

Remember: **Successful snapshot/diff implementations depend on understanding object structure, property behavior, and framework limitations through comprehensive TDD**. This intelligent workflow guide routes Claude through the optimal TDD path based on the specific implementation scenario, with special emphasis on framework scope validation and realistic success criteria for snapshot/diff functionality.