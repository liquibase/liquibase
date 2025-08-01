# Master Process Loop for Liquibase Extension Implementation
## AI-Optimized Sequential Execution Protocol

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: PROCESS_MASTER
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition through systematic requirements"
  - "Complete SQL test statements through validation phases"
  - "Unit tests complete string comparison through testing protocols"
  - "Integration tests ALL generated SQL through harness validation"
  - "Prevents Claude Code from skipping steps and changing goalposts"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/master_process_loop.md"
FOLDER_STRUCTURE:
  OVERVIEW: "This document - Master process for all changetype implementations"
  COMPANION_DOCUMENTS:
    - README.md: "Navigation guide for changetype implementation"
    - changetype_patterns.md: "Patterns for new and existing changetypes"
    - sql_generator_overrides.md: "SQL syntax override implementations"
    - test_harness_guide.md: "Testing with harness protocols"
    - requirements_creation.md: "Detailed requirements specification"
    - quick_reference.md: "Command reference and decision trees"

RELATED_SNAPSHOT_DIFF_GUIDES:
  BASE_PATH: "../snapshot_diff_implementation/"
  KEY_DOCUMENTS:
    - ai_quickstart.md: "Sequential execution patterns"
    - error_patterns_guide.md: "Systematic debugging framework"
```

## The Sequential Blocking Loop (Execute for EVERY Task)

```yaml
EXECUTION_RULES:
  - "Each step BLOCKS next step until validation complete"
  - "No goalpost changing - requirements fixed before implementation"
  - "All validations must pass before proceeding"
  - "Systematic debugging if any step fails"
```

```
START
├── 1. CAPTURE TASK - **RETROSPECTIVE FIRST PATTERN**
│   ├── BLOCKING_VALIDATION_1.1: "Task added to todo list with ALL sub-tasks"
│   ├── Add to todo list in this exact order:
│   │   1. Create retrospective document (PENDING - reminds me it's required)
│   │   2. Main implementation task (PENDING)
│   │   3. Update project plan with results (PENDING)
│   │   4. Update guides with learnings (PENDING)
│   ├── BLOCKING_VALIDATION_1.2: "Project plan status updated to IN PROGRESS"
│   └── ADDRESSES_CORE_ISSUE: "Prevents step skipping through mandatory todo tracking"
│
├── 2. VERIFY REQUIREMENTS - **COMPLETE SYNTAX DEFINITION**
│   ├── BLOCKING_VALIDATION_2.1: "Requirements document exists and complete"
│   ├── Check requirements doc exists:
│   │   └── Location: requirements/[changeType]_requirements.md
│   ├── If not, MUST create using:
│   │   └── Guide: requirements_creation.md
│   ├── BLOCKING_VALIDATION_2.2: "Requirements include complete SQL syntax examples"
│   ├── BLOCKING_VALIDATION_2.3: "All properties documented with constraints"
│   ├── BLOCKING_VALIDATION_2.4: "Complete SQL test statements provided"
│   ├── Update project plan with requirements validation
│   └── ADDRESSES_CORE_ISSUE: "Complete syntax definition requires comprehensive requirements"
│
├── 3. ASSESS CURRENT STATE ⚡ CRITICAL STEP - **PREVENT GOALPOST CHANGING**
│   ├── BLOCKING_VALIDATION_3.1: "Discovery phase completed before implementation"
│   ├── DISCOVERY PHASE (Do this FIRST!):
│   │   ├── Search for existing components:
│   │   │   find . -name "*<ChangeType>*" -type f
│   │   │   find . -name "*NamespaceAttributeStorage*" -type f
│   │   │   grep -r "changeType" src/main/java/liquibase/parser/
│   │   ├── Check service registrations:
│   │   │   └── File: src/main/resources/META-INF/services/
│   │   ├── Review XSD for existing attributes:
│   │   │   └── File: src/main/resources/*.xsd
│   │   └── CRITICAL: Question any documented limitations
│   ├── BLOCKING_VALIDATION_3.2: "Existing code completeness assessed"
│   ├── BLOCKING_VALIDATION_3.3: "Phase tests executed to verify functionality"
│   ├── BLOCKING_VALIDATION_3.4: "Comparison against requirements completed"
│   ├── Update project plan with assessment findings
│   └── ADDRESSES_CORE_ISSUE: "Prevents goalpost changing by establishing baseline"
│   
│   💡 FIELD-TESTED LEARNINGS: 
│   - Always check what exists before implementing!
│   - dropDatabase was 95% complete, saved 15+ minutes
│   - alterSequence ALL components existed, saved 1+ hour
│   - When tests fail, create debug test FIRST to see actual output
│
├── 4. IMPLEMENT - **SYSTEMATIC VALIDATION AT EACH PHASE**
│   ├── BLOCKING_VALIDATION_4.1: "Implementation guide selected based on decision tree"
│   ├── Choose implementation guide based on decision tree:
│   │   ├── NEW change type (doesn't exist in Liquibase):
│   │   │   └── Guide: changetype_patterns.md (New Changetype Pattern)
│   │   ├── EXISTING change type (add namespace attributes):
│   │   │   └── Guide: changetype_patterns.md (Extension Pattern)
│   │   └── SQL SYNTAX OVERRIDE (change SQL generation):
│   │       └── Guide: sql_generator_overrides.md
│   ├── BLOCKING_VALIDATION_4.2: "Each phase validated before proceeding"
│   ├── BLOCKING_VALIDATION_4.3: "Unit tests include complete SQL string comparison"
│   ├── Follow guide phases systematically with validation checkpoints
│   ├── Test each phase before moving to next (NO EXCEPTIONS)
│   ├── Update project plan after each major milestone
│   └── ADDRESSES_CORE_ISSUE: "Unit tests complete string comparison enforced"
│
├── 5. VALIDATE - **COMPREHENSIVE SQL TESTING**
│   ├── BLOCKING_VALIDATION_5.1: "All unit tests passing with complete SQL comparison"
│   ├── BLOCKING_VALIDATION_5.2: "Test harness files created with ALL SQL scenarios"
│   ├── Run all unit tests with STRICT validation:
│   │   ├── REQUIREMENT: Complete SQL string comparison in ALL unit tests
│   │   ├── REQUIREMENT: All property combinations tested
│   │   └── ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
│   ├── Create test harness XML file:
│   │   └── Guide: test_harness_guide.md
│   ├── 🚨 TEST HARNESS EXECUTION (MANDATORY):
│   │   ├── BLOCKING_VALIDATION_5.3: "JAR built and installed correctly"
│   │   ├── Build & Install JAR: cd extension-project && mvn clean install -DskipTests
│   │   │   ⚠️ CRITICAL: Use mvn install not mvn package! Test harness loads via Maven dependencies
│   │   ├── Change to harness: cd ../liquibase-test-harness
│   │   ├── Verify location: pwd (MUST show liquibase-test-harness)
│   │   ├── BLOCKING_VALIDATION_5.4: "Integration tests execute ALL generated SQL"
│   │   ├── Run test: mvn test -Dtest=ChangeObjectTests -DchangeObjects=X -DdbName=[database]
│   │   ├── BLOCKING_VALIDATION_5.5: "All changesets executed and verified"
│   │   └── ONLY mark complete if test PASSES
│   │   └── ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
│   ├── BLOCKING_VALIDATION_5.6: "Requirements coverage verified"
│   └── Update project plan → "COMPLETE" only after ALL tests pass
│
├── 6. RETROSPECTIVE (🚨 MANDATORY - LEARNING INTEGRATION)
│   ├── BLOCKING_VALIDATION_6.1: "Retrospective file created with complete analysis"
│   ├── Create retrospective file:
│   │   └── Location: retrospectives/<TASK>_RETRO.md
│   ├── BLOCKING_VALIDATION_6.2: "All three categories documented with evidence"
│   ├── Document THREE categories (MANDATORY):
│   │   ├── 📈 WHAT'S WORKING (Keep doing):
│   │   │   ├── List successful patterns/approaches
│   │   │   ├── Update confidence metrics in docs
│   │   │   └── Reinforce in relevant guides
│   │   ├── 🛑 WHAT'S NOT WORKING (Stop doing):
│   │   │   ├── Identify failed approaches
│   │   │   ├── REMOVE from documentation
│   │   │   └── Add warnings if tempting but wrong
│   │   └── 🔧 WHAT NEEDS IMPROVEMENT (Fix/enhance):
│   │       ├── Identify gaps or inefficiencies
│   │       ├── UPDATE docs with improvements
│   │       └── Add specific examples/warnings
│   ├── BLOCKING_VALIDATION_6.3: "Key learnings printed and explained"
│   ├── 🔔 PRINT KEY LEARNINGS TO CHAT for user visibility
│   ├── BLOCKING_VALIDATION_6.4: "Learnings immediately applied to guides"
│   └── IMMEDIATELY apply learnings to guides
│
├── 7. INTEGRATE LEARNINGS (🚨 PREVENTS REPEATED MISTAKES)
│   ├── BLOCKING_VALIDATION_7.1: "All guides updated with learnings"
│   ├── Update guides with learnings:
│   │   ├── Add to troubleshooting sections
│   │   ├── Update file references if needed
│   │   ├── Add warnings for common pitfalls
│   │   └── Enhance error pattern libraries
│   ├── BLOCKING_VALIDATION_7.2: "Project plan marked complete with evidence"
│   ├── Update project plan → "COMPLETE"
│   ├── BLOCKING_VALIDATION_7.3: "All todos marked complete"
│   └── Mark all todos complete
│
└── CONTINUE → Next task (no pause unless validation fails)
    └── ⚠️ ONLY STOP IF:
        - BLOCKING_VALIDATION fails at any step
        - Encountering blockers/issues
        - Need clarification on requirements
        - Finding unexpected complexity
        - Tests failing after systematic debugging
```

## Sequential Blocking Validation Checkpoints

### Phase 1 Validations - Task Capture
```yaml
VALIDATION_1.1:
  TYPE: "TODO_LIST_COMPLETE"
  REQUIREMENT: "All sub-tasks added including retrospective"
  FAILURE_ACTION: "STOP - Add missing todos before proceeding"
  
VALIDATION_1.2:
  TYPE: "PROJECT_PLAN_UPDATED"
  REQUIREMENT: "Status changed to IN PROGRESS with timestamp"
  FAILURE_ACTION: "STOP - Update project plan before proceeding"
```

### Phase 2 Validations - Requirements
```yaml
VALIDATION_2.1:
  TYPE: "REQUIREMENTS_DOCUMENT_EXISTS"
  PATH: "requirements/[changeType]_requirements.md"
  FAILURE_ACTION: "STOP - Create requirements using requirements_creation.md"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
VALIDATION_2.2:
  TYPE: "COMPLETE_SQL_SYNTAX_DOCUMENTED"
  REQUIREMENT: "All SQL operations with complete syntax examples"
  FAILURE_ACTION: "STOP - Add complete SQL examples to requirements"
  ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
  
VALIDATION_2.3:
  TYPE: "ALL_PROPERTIES_DOCUMENTED"
  REQUIREMENT: "Complete property table with constraints"
  FAILURE_ACTION: "STOP - Complete property documentation"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
VALIDATION_2.4:
  TYPE: "COMPLETE_SQL_TEST_STATEMENTS"
  REQUIREMENT: "Executable SQL examples for all operations"
  FAILURE_ACTION: "STOP - Provide complete SQL test statements"
  ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

### Phase 3 Validations - Assessment
```yaml
VALIDATION_3.1:
  TYPE: "DISCOVERY_PHASE_COMPLETE"
  REQUIREMENT: "Existing components searched and documented"
  FAILURE_ACTION: "STOP - Complete discovery before implementation"
  PREVENTS: "Goalpost changing by establishing baseline"
  
VALIDATION_3.2:
  TYPE: "EXISTING_CODE_ASSESSED"
  REQUIREMENT: "Completeness evaluation documented"
  FAILURE_ACTION: "STOP - Assess existing code completeness"
  
VALIDATION_3.3:
  TYPE: "PHASE_TESTS_EXECUTED"
  REQUIREMENT: "Current functionality verified through tests"
  FAILURE_ACTION: "STOP - Execute phase tests to establish baseline"
  
VALIDATION_3.4:
  TYPE: "REQUIREMENTS_COMPARISON_COMPLETE"
  REQUIREMENT: "Gap analysis between current state and requirements"
  FAILURE_ACTION: "STOP - Complete requirements comparison"
```

### Phase 4 Validations - Implementation
```yaml
VALIDATION_4.1:
  TYPE: "IMPLEMENTATION_GUIDE_SELECTED"
  REQUIREMENT: "Correct guide chosen based on decision tree"
  FAILURE_ACTION: "STOP - Select appropriate implementation guide"
  
VALIDATION_4.2:
  TYPE: "PHASE_BY_PHASE_VALIDATION"
  REQUIREMENT: "Each implementation phase validated before next"
  FAILURE_ACTION: "STOP - Validate current phase before proceeding"
  
VALIDATION_4.3:
  TYPE: "UNIT_TESTS_COMPLETE_SQL_COMPARISON"
  REQUIREMENT: "All unit tests compare complete SQL strings"
  FAILURE_ACTION: "STOP - Add complete SQL string comparison to unit tests"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

### Phase 5 Validations - Testing
```yaml
VALIDATION_5.1:
  TYPE: "UNIT_TESTS_PASSING_STRICT"
  REQUIREMENT: "All unit tests pass with complete SQL validation"
  FAILURE_ACTION: "STOP - Fix unit tests with complete SQL comparison"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
  
VALIDATION_5.2:
  TYPE: "TEST_HARNESS_FILES_COMPLETE"
  REQUIREMENT: "Test harness files cover ALL SQL scenarios"
  FAILURE_ACTION: "STOP - Create comprehensive test harness files"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
  
VALIDATION_5.3:
  TYPE: "JAR_BUILD_SUCCESS"
  REQUIREMENT: "Extension JAR built and installed successfully"
  FAILURE_ACTION: "STOP - Fix build issues before harness testing"
  
VALIDATION_5.4:
  TYPE: "INTEGRATION_TESTS_ALL_SQL"
  REQUIREMENT: "All generated SQL executed and verified in integration tests"
  FAILURE_ACTION: "STOP - Ensure all SQL tested in integration"
  ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
  
VALIDATION_5.5:
  TYPE: "ALL_CHANGESETS_EXECUTED"
  REQUIREMENT: "Test harness executed all expected changesets"
  FAILURE_ACTION: "STOP - Verify all changesets in test harness"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
  
VALIDATION_5.6:
  TYPE: "REQUIREMENTS_COVERAGE_VERIFIED"
  REQUIREMENT: "All requirements validated through tests"
  FAILURE_ACTION: "STOP - Ensure complete requirements coverage"
```

### Phase 6 Validations - Retrospective
```yaml
VALIDATION_6.1:
  TYPE: "RETROSPECTIVE_FILE_EXISTS"
  PATH: "retrospectives/[TASK]_RETRO.md"
  FAILURE_ACTION: "STOP - Create retrospective file before completion"
  
VALIDATION_6.2:
  TYPE: "THREE_CATEGORIES_DOCUMENTED"
  REQUIREMENT: "Working, Not Working, Needs Improvement all documented"
  FAILURE_ACTION: "STOP - Complete all retrospective categories"
  
VALIDATION_6.3:
  TYPE: "KEY_LEARNINGS_COMMUNICATED"
  REQUIREMENT: "Learnings printed to chat for user visibility"
  FAILURE_ACTION: "STOP - Communicate learnings before proceeding"
  
VALIDATION_6.4:
  TYPE: "LEARNINGS_APPLIED_TO_GUIDES"
  REQUIREMENT: "Documentation updated with new learnings"
  FAILURE_ACTION: "STOP - Apply learnings to guides immediately"
```

### Phase 7 Validations - Integration
```yaml
VALIDATION_7.1:
  TYPE: "GUIDES_UPDATED_WITH_LEARNINGS"
  REQUIREMENT: "All relevant guides enhanced with new learnings"
  FAILURE_ACTION: "STOP - Update guides with learnings"
  
VALIDATION_7.2:
  TYPE: "PROJECT_PLAN_MARKED_COMPLETE"
  REQUIREMENT: "Project plan shows COMPLETE with evidence"
  FAILURE_ACTION: "STOP - Update project plan completion status"
  
VALIDATION_7.3:
  TYPE: "ALL_TODOS_COMPLETE"
  REQUIREMENT: "All todo items marked complete"
  FAILURE_ACTION: "STOP - Mark all todos complete"
```

## Error Recovery Protocol
```yaml
ON_VALIDATION_FAILURE:
  - STOP_EXECUTION: true
  - IDENTIFY_FAILED_VALIDATION: "[VALIDATION_ID]"
  - CONSULT_ERROR_PATTERNS: "../snapshot_diff_implementation/error_patterns_guide.md"
  - APPLY_SYSTEMATIC_DEBUGGING: "5-layer analysis framework"
  - FIX_ISSUE_COMPLETELY: "Address root cause, not symptoms"
  - RETRY_FROM_FAILED_VALIDATION: true
  - DO_NOT_PROCEED: "Until validation passes"
  - NO_GOALPOST_CHANGING: "Do not redefine requirements to fit code"
```

## Quick Checklist Version - AI Optimized

### Before starting ANY task:
```yaml
MANDATORY_PRECONDITIONS:
  - [ ] Task added to todo with ALL sub-tasks (including retrospective)
  - [ ] Project plan updated to "IN PROGRESS"
  - [ ] Requirements document exists and complete
  - [ ] Complete SQL syntax examples documented
  VALIDATION: "BLOCKING - Cannot proceed without all preconditions"
```

### During task - Phase Validation:
```yaml
EACH_PHASE_REQUIRES:
  - [ ] Current phase validation complete
  - [ ] Project plan updated after phase
  - [ ] Unit tests passing with complete SQL comparison
  - [ ] Integration tests covering ALL generated SQL
  VALIDATION: "BLOCKING - Each phase gates next phase"
```

### After task completion - **HARD STOP CHECKPOINTS**:
```yaml
COMPLETION_GATES:
  - [ ] All unit tests passing with complete SQL string comparison
  - [ ] Test harness XML created covering ALL SQL scenarios
  - [ ] JAR built with mvn clean install
  - [ ] Test harness executed successfully
  - [ ] ALL generated SQL verified in integration tests
  VALIDATION: "BLOCKING - Cannot claim completion without all gates"
```

**🛑 CHECKPOINT 1: BEFORE CLAIMING COMPLETION**
```yaml
MANDATORY_EVIDENCE:
  - [ ] SHOW RETROSPECTIVE FILE: Use Read tool to display full content
  - [ ] CONFIRM LEARNINGS CAPTURED: State what learned and what will change
  - [ ] VERIFY GUIDE UPDATES: Show which guides updated and specific changes
  VALIDATION: "BLOCKING - Must provide evidence before completion"
```

**🛑 CHECKPOINT 2: BEFORE UPDATING PROJECT PLAN**  
```yaml
USER_CONFIRMATION_REQUIRED:
  - [ ] ASK USER: "Retrospective at [path]. Proceed with completion?"
  - [ ] WAIT FOR CONFIRMATION: Do not continue until user acknowledges
  VALIDATION: "BLOCKING - Requires user confirmation"
```

**🛑 CHECKPOINT 3: BEFORE STARTING NEXT TASK**
```yaml
REFLECTION_REQUIRED:
  - [ ] MANDATORY PAUSE: "Previous retrospective complete. Ready for next?"
  - [ ] FORCE REFLECTION: "What did retrospective teach about process?"
  VALIDATION: "BLOCKING - Must reflect before continuing"
```

### Only after all checkpoints:
```yaml
FINAL_COMPLETION:
  - [ ] Project plan shows "COMPLETE" with date  
  - [ ] All todos marked complete
  - [ ] All four core issues addressed
  VALIDATION: "COMPLETE - All validations passed"
```

## Critical Process Failures to Prevent

**❌ FORBIDDEN BEHAVIORS:**
```yaml
NEVER_ALLOWED:
  - "Marking retrospectives Done without creating them"
  - "Updating project plan as afterthought"
  - "Treating documentation as separate from work"
  - "Assuming problems exist without discovery validation"
  - "Skipping validation checkpoints"
  - "Changing requirements to fit current code"
  - "Proceeding when validations fail"
```

**✅ REQUIRED BEHAVIORS:**
```yaml
ALWAYS_REQUIRED:
  - "Sequential blocking execution with validation gates"
  - "Complete SQL string comparison in ALL unit tests"
  - "Integration tests covering ALL generated SQL"
  - "Systematic debugging when issues arise"
  - "Retrospectives with evidence-based learnings"
  - "Immediate application of learnings to guides"
```

## 🚨 SELF-ACCOUNTABILITY SYSTEM 

### Before Every "Complete" Claim:
```yaml
EVIDENCE_REQUIREMENTS:
  1. READ_RETROSPECTIVE_ALOUD: "Display full retrospective content using Read tool"
  2. ASK_USER_PERMISSION: "Retrospective at [path], may I proceed?"
  3. SHOW_GUIDE_UPDATES: "Evidence of specific guide changes made"
  VALIDATION: "BLOCKING - Cannot claim complete without evidence"
```

### Forbidden Phrases Until All Validations Pass:
```yaml
FORBIDDEN_UNTIL_COMPLETE:
  - ❌ "Task complete"
  - ❌ "Implementation finished" 
  - ❌ "Ready for next task"
  - ❌ "Moving on"
```

### Required Phrases During Process:
```yaml
REQUIRED_COMMUNICATION:
  - ✅ "Technical work done, now creating retrospective"
  - ✅ "Retrospective complete at [path], requesting completion permission"
  - ✅ "Learnings: [specific], Changes: [specific]"
  - ✅ "Validation [X] failed, applying systematic debugging"
```

### Evidence Required for Completion:
```yaml
COMPLETION_EVIDENCE:
  - RETROSPECTIVE_FILE: "Must display full content using Read tool"
  - GUIDE_UPDATES: "Must show specific changes made to process documents"
  - LEARNING_INTEGRATION: "Must state how this changes future work"
  - VALIDATION_PROOF: "Must show all validations passed"
```

## Automated Workflow Scripts

### Development Workflow Script
```bash
#!/bin/bash
# scripts/changetype-workflow.sh - Enhanced with validation
set -e

echo "=== Liquibase Changetype Development Workflow ==="

# Validate inputs
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 [database] [changetype]"
    echo "Example: $0 snowflake createWarehouse"
    exit 1
fi

DATABASE=$1
CHANGETYPE=$2

echo "1. Validation Phase..."
echo "   - Checking requirements document..."
if [ ! -f "requirements/${CHANGETYPE}_requirements.md" ]; then
    echo "   ERROR: Requirements document missing!"
    echo "   Create: requirements/${CHANGETYPE}_requirements.md"
    exit 1
fi

echo "2. Building extension for $DATABASE..."
cd liquibase-$DATABASE
mvn clean install -DskipTests

echo "3. Installing to local repository..."
echo "   (Required for test harness dependency resolution)"

echo "4. Changing to test harness..."
cd ../liquibase-test-harness

echo "5. Running changetype test harness for $CHANGETYPE..."
mvn test -Dtest=ChangeObjectTests -DdbName=$DATABASE -DchangeObjects="$CHANGETYPE"

echo "6. Validation complete!"
echo "=== Workflow Complete ==="
```

## Continuous Workflow Mode

**DEFAULT**: Continue to next change type after completing current one
```yaml
WORKFLOW_RULES:
  - "No approval needed between tasks if all validations pass"
  - "Keep momentum through systematic implementation"
  - "Maintain documentation and learning integration"
```

**STOP AND ASK FOR HELP WHEN**:
```yaml
BLOCKING_CONDITIONS:
  - 🚫 "Tests failing after systematic debugging attempts"
  - 🤔 "Requirements unclear or conflicting"
  - 🐛 "Encountering unexpected bugs or blockers"
  - 📚 "Architectural decisions need discussion"
  - ⚡ "Implementation taking significantly longer than expected"
  - 🔧 "Test environment not available or misconfigured"
  - ⚠️ "Cannot validate implementation (e.g., no test harness)"
  - 🛑 "Any validation checkpoint fails repeatedly"
```

**DO NOT**:
```yaml
FORBIDDEN_ACTIONS:
  - "Mark tasks complete without full validation"
  - "Skip retrospectives because it was simple"
  - "Continue without asking when blocked"
  - "Change requirements to fit current implementation"
  - "Skip validation checkpoints for any reason"
```

## Emergency Recovery Protocol

If validation failure detected mid-process:
```yaml
RECOVERY_STEPS:
  1. STOP_IMMEDIATELY: true
  2. IDENTIFY_FAILED_VALIDATION: "[VALIDATION_ID] at step [STEP]"
  3. ADD_MISSING_TODOS: "Fill any gaps in todo tracking"
  4. UPDATE_PROJECT_PLAN: "Reflect current state accurately"
  5. CONSULT_ERROR_PATTERNS: "../snapshot_diff_implementation/error_patterns_guide.md"
  6. APPLY_SYSTEMATIC_DEBUGGING: "5-layer analysis framework"
  7. RESUME_FROM_FAILED_VALIDATION: "After fixing root cause"
```

## Implementation Decision Tree - Enhanced

**Does Liquibase already have this change type?**
```yaml
DECISION_FLOW:
  LIQUIBASE_HAS_CHANGETYPE:
    ANSWER: "YES"
    GUIDE: "changetype_patterns.md (Extension Pattern)"
    EXAMPLES: 
      - "createTable (add snowflake:transient)"
      - "alterSequence (add snowflake:setNoOrder)"
      - "renameTable (SQL generator override)"
    VALIDATION: "Check existing implementation completeness first"
    
  LIQUIBASE_MISSING_CHANGETYPE:
    ANSWER: "NO"
    GUIDE: "changetype_patterns.md (New Changetype Pattern)"
    EXAMPLES:
      - "createWarehouse"
      - "dropDatabase" 
      - "alterSchema"
    VALIDATION: "Complete requirements document required"
    
  SQL_SYNTAX_DIFFERENT:
    ANSWER: "EXISTS but SQL syntax wrong for database"
    GUIDE: "sql_generator_overrides.md"
    EXAMPLES:
      - "addColumn (Snowflake needs COLUMN keyword)"
      - "dropColumn (case sensitivity differences)"
    VALIDATION: "SQL syntax research required first"
```

## Key Files Reference - Updated Paths

### Project Files
```yaml
CORE_DOCUMENTS:
  - PROJECT_PLAN: "project-specific/PROJECT_PLAN.md"
  - REQUIREMENTS: "requirements/[changeType]_requirements.md"
  - TEST_RESULTS: "test-results/TEST_RESULTS.md"
```

### Implementation Guides  
```yaml
CHANGETYPE_GUIDES:
  - PROCESS_MASTER: "changetype_implementation/master_process_loop.md"
  - CHANGETYPE_PATTERNS: "changetype_implementation/changetype_patterns.md"
  - SQL_OVERRIDES: "changetype_implementation/sql_generator_overrides.md"
  - TEST_HARNESS: "changetype_implementation/test_harness_guide.md"
  - REQUIREMENTS_CREATION: "changetype_implementation/requirements_creation.md"

SNAPSHOT_DIFF_GUIDES:
  - AI_QUICKSTART: "snapshot_diff_implementation/ai_quickstart.md"
  - ERROR_PATTERNS: "snapshot_diff_implementation/error_patterns_guide.md"
  - MAIN_GUIDE: "snapshot_diff_implementation/main_guide.md"
```

### Source Code Locations
```yaml
IMPLEMENTATION_PATHS:
  - CHANGE_CLASSES: "src/main/java/liquibase/change/[database]/"
  - GENERATORS: "src/main/java/liquibase/sqlgenerator/core/[database]/"
  - PARSER: "src/main/java/liquibase/parser/[database]/"
  - STORAGE: "src/main/java/liquibase/parser/[database]/NamespaceAttributeStorage.java"
  - SERVICE_REGISTRATIONS: "src/main/resources/META-INF/services/"
  - XSD_SCHEMA: "src/main/resources/*.xsd"
  - TEST_HARNESS_FILES: "liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/[database]/"
```

## Cross-Reference Links
```yaml
RELATED_DOCUMENTS:
  SNAPSHOT_DIFF_GUIDES: "../snapshot_diff_implementation/"
  ERROR_DEBUGGING: "../snapshot_diff_implementation/error_patterns_guide.md"
  NAVIGATION: "README.md - Complete navigation guide"
  
IMPLEMENTATION_GUIDES:
  CHANGETYPE_PATTERNS: "changetype_patterns.md"
  SQL_OVERRIDES: "sql_generator_overrides.md"
  TEST_HARNESS: "test_harness_guide.md"
  REQUIREMENTS: "requirements_creation.md"
```