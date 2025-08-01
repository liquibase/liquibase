# AI Requirements Research Workflow
## Phase 1: Active Investigation and Discovery of Database Object Capabilities

## EXECUTION_PROTOCOL
```yaml
PHASE: 1_REQUIREMENTS_RESEARCH
PROTOCOL_VERSION: 3.0
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
DELIVERABLE: "research_findings_[object].md"
NEXT_PHASE: "ai_requirements_writeup.md"
```

## WORKFLOW_OVERVIEW
```yaml
PURPOSE: "Active investigation and discovery of database object capabilities"
INPUT: "Database object name and basic understanding of what needs implementation"
OUTPUT: "Complete research findings document with all raw data for requirements writeup"
DURATION: "2-4 hours of focused research"
CRITICAL_SUCCESS_FACTOR: "Leave no stone unturned - complete investigation before documentation"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/ai_requirements_research.md"
WORKFLOW_SEQUENCE:
  PHASE_1: "THIS DOCUMENT - Requirements Research"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation"
  PHASE_3: "ai_workflow_guide.md - TDD Implementation"
  
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - master_process_loop.md: "Overall development process context"
  - ../snapshot_diff_implementation/ai_requirements_research.md: "Parallel research workflow for snapshot/diff"
```

## 🔍 RESEARCH INVESTIGATION PROTOCOL

### Step 1: Official Documentation Deep-Dive Analysis
```yaml
BLOCKING_VALIDATION_1.1:
  TYPE: "OFFICIAL_DOCUMENTATION_COMPLETE"
  REQUIREMENT: "All official database documentation sources identified and thoroughly analyzed"
  DELIVERABLE: "Complete documentation source list with URLs, versions, and analysis notes"
  FAILURE_ACTION: "STOP - Must have complete official documentation before proceeding"

RESEARCH_ACTIVITIES:
  DOCUMENTATION_DISCOVERY:
    - "Identify primary official documentation (vendor website, SQL reference manuals)"
    - "Find version-specific documentation matching target database version"
    - "Locate SQL syntax references, command references, data type guides"
    - "Discover vendor-specific extensions and proprietary features"
    - "Find official examples, tutorials, and best practices documentation"
    
  DOCUMENTATION_ANALYSIS:
    - "Read ALL sections related to target database object thoroughly"
    - "Document all syntax variations and optional clauses"
    - "Note version differences and compatibility requirements"
    - "Identify deprecated features and recommended alternatives"
    - "Extract ALL examples and syntax patterns"

VALIDATION_CHECKPOINT_1.1:
  - [ ] Primary official documentation sources identified (minimum 3 sources)
  - [ ] Version-specific documentation located and analyzed
  - [ ] Complete SQL syntax documentation found and reviewed
  - [ ] All official examples extracted and documented
  - [ ] Documentation analysis notes complete and thorough
```

### Step 2: SQL Syntax Discovery and Validation
```yaml
BLOCKING_VALIDATION_1.2:
  TYPE: "SQL_SYNTAX_COMPLETE"
  REQUIREMENT: "All SQL syntax variations discovered, tested, and validated with real database"
  DELIVERABLE: "Complete SQL syntax matrix with all tested variations"
  FAILURE_ACTION: "STOP - Must have complete tested SQL syntax before proceeding"

RESEARCH_ACTIVITIES:
  SYNTAX_DISCOVERY:
    - "Extract ALL SQL syntax patterns from official documentation"
    - "Identify ALL optional clauses, parameters, and modifiers"
    - "Document ALL valid keyword combinations and ordering rules"
    - "Find ALL syntax variations (short form, long form, aliases)"
    - "Discover database-specific syntax extensions and proprietary features"
    
  SYNTAX_VALIDATION:
    - "Test EVERY syntax variation against real database instance"
    - "Validate ALL parameter combinations work as documented"
    - "Test edge cases and boundary conditions"
    - "Verify error conditions and error messages"
    - "Document actual SQL output for each syntax pattern"

VALIDATION_CHECKPOINT_1.2:
  - [ ] All SQL syntax variations extracted from documentation
  - [ ] Every syntax pattern tested against real database
  - [ ] All parameter combinations validated
  - [ ] Edge cases and error conditions tested
  - [ ] Complete SQL syntax matrix documented with test results
```

### Step 3: Parameter/Attribute Enumeration and Analysis
```yaml
BLOCKING_VALIDATION_1.3:
  TYPE: "PARAMETER_ANALYSIS_COMPLETE"
  REQUIREMENT: "All parameters/attributes enumerated with complete analysis of types, constraints, and behaviors"
  DELIVERABLE: "Complete parameter analysis table with all attributes categorized"
  FAILURE_ACTION: "STOP - Must have complete parameter analysis before proceeding"

RESEARCH_ACTIVITIES:
  PARAMETER_DISCOVERY:
    - "List ALL parameters/attributes mentioned in official documentation"
    - "Identify EVERY data type, constraint, and validation rule"
    - "Find ALL default values, ranges, and acceptable value sets"
    - "Discover parameter dependencies and mutual exclusivity rules"
    - "Locate vendor-specific parameters and extensions"
    
  PARAMETER_ANALYSIS:
    - "Test EVERY parameter with valid values to confirm behavior"
    - "Test EVERY parameter with edge cases and boundary values"
    - "Test EVERY parameter with invalid values to map error conditions"
    - "Validate parameter interactions and dependency rules"
    - "Document actual database behavior vs documented behavior"

VALIDATION_CHECKPOINT_1.3:
  - [ ] All parameters/attributes enumerated from documentation
  - [ ] Every parameter tested with valid, edge case, and invalid values
  - [ ] All data types, constraints, and validation rules documented
  - [ ] Parameter dependencies and mutual exclusivity rules identified
  - [ ] Complete parameter analysis table created with test results
```

### Step 4: Edge Case Identification and Boundary Testing
```yaml
BLOCKING_VALIDATION_1.4:
  TYPE: "EDGE_CASE_ANALYSIS_COMPLETE"
  REQUIREMENT: "All edge cases, boundary conditions, and special scenarios identified and tested"
  DELIVERABLE: "Complete edge case analysis with test results and behavior documentation"
  FAILURE_ACTION: "STOP - Must have complete edge case analysis before proceeding"

RESEARCH_ACTIVITIES:
  EDGE_CASE_DISCOVERY:
    - "Identify ALL boundary conditions mentioned in documentation"
    - "Find ALL special cases, exceptions, and unusual scenarios"
    - "Discover system limits, constraints, and capacity boundaries"
    - "Locate compatibility issues and version dependencies"
    - "Find ALL warning conditions and advisory scenarios"
    
  BOUNDARY_TESTING:
    - "Test ALL identified boundary conditions with real database"
    - "Validate system limits and constraint enforcement"
    - "Test compatibility scenarios and version-specific behaviors"
    - "Verify warning conditions and error message accuracy"
    - "Document actual behavior at boundaries vs expected behavior"

VALIDATION_CHECKPOINT_1.4:
  - [ ] All edge cases and boundary conditions identified from documentation
  - [ ] Every boundary condition tested against real database
  - [ ] System limits and constraints validated
  - [ ] Compatibility issues and version dependencies documented
  - [ ] Complete edge case analysis with test results documented
```

### Step 5: Mutual Exclusivity Rule Discovery
```yaml
BLOCKING_VALIDATION_1.5:
  TYPE: "MUTUAL_EXCLUSIVITY_COMPLETE"
  REQUIREMENT: "All mutual exclusivity rules, conflicts, and incompatible combinations identified and validated"
  DELIVERABLE: "Complete mutual exclusivity matrix with all conflict scenarios documented"
  FAILURE_ACTION: "STOP - Must have complete mutual exclusivity analysis before proceeding"

RESEARCH_ACTIVITIES:
  EXCLUSIVITY_DISCOVERY:
    - "Identify ALL mutually exclusive parameters from documentation"
    - "Find ALL conflicting options and incompatible combinations"
    - "Discover ALL 'either/or' scenarios and choice requirements"
    - "Locate ALL conditional parameters and dependency conflicts"
    - "Find ALL version-specific exclusivity rules"
    
  EXCLUSIVITY_VALIDATION:
    - "Test EVERY identified mutual exclusivity rule with real database"
    - "Validate ALL conflict scenarios produce expected errors"
    - "Test ALL 'either/or' scenarios to confirm exclusive behavior"
    - "Verify conditional parameter conflicts behave as documented"
    - "Document actual error messages and behavior for all conflicts"

VALIDATION_CHECKPOINT_1.5:
  - [ ] All mutual exclusivity rules identified from documentation
  - [ ] Every exclusivity rule tested against real database
  - [ ] All conflict scenarios validated and error messages documented
  - [ ] Either/or scenarios and conditional conflicts tested
  - [ ] Complete mutual exclusivity matrix created with test results
```

### Step 6: Error Condition Mapping
```yaml
BLOCKING_VALIDATION_1.6:
  TYPE: "ERROR_MAPPING_COMPLETE"
  REQUIREMENT: "All error conditions, validation failures, and exception scenarios mapped and tested"
  DELIVERABLE: "Complete error condition catalog with exact error messages and trigger conditions"
  FAILURE_ACTION: "STOP - Must have complete error mapping before proceeding"

RESEARCH_ACTIVITIES:
  ERROR_DISCOVERY:
    - "Identify ALL error conditions mentioned in documentation"
    - "Find ALL validation failures and constraint violations"
    - "Discover ALL exception scenarios and failure modes"
    - "Locate ALL warning conditions and advisory messages"
    - "Find ALL system-level errors and resource constraints"
    
  ERROR_VALIDATION:
    - "Trigger EVERY identified error condition with real database"
    - "Capture exact error messages and error codes"
    - "Test error condition variations and message consistency"
    - "Validate error recovery scenarios and cleanup requirements"
    - "Document error condition hierarchy and severity levels"

VALIDATION_CHECKPOINT_1.6:
  - [ ] All error conditions identified from documentation
  - [ ] Every error condition triggered and tested
  - [ ] Exact error messages and codes captured
  - [ ] Error recovery scenarios and cleanup requirements documented
  - [ ] Complete error condition catalog created with test results
```

## 📊 RESEARCH DELIVERABLE TEMPLATE

### Research Findings Document Structure
```yaml
FILENAME: "research_findings_[database]_[object].md"
TEMPLATE_SECTIONS:

RESEARCH_METADATA:
  - "Research date and duration"
  - "Database version and environment details"
  - "Researcher identification and validation"
  - "Documentation sources and versions analyzed"

OFFICIAL_DOCUMENTATION_ANALYSIS:
  - "Complete source list with URLs and versions"
  - "Documentation analysis notes and findings"
  - "Version differences and compatibility notes"
  - "Official examples and syntax patterns extracted"

SQL_SYNTAX_MATRIX:
  - "All syntax variations with test results"
  - "Parameter combinations and ordering rules"
  - "Optional clauses and modifier behaviors"
  - "Database-specific extensions and features"

PARAMETER_ANALYSIS_TABLE:
  - "All parameters with types, constraints, defaults"
  - "Valid value ranges and acceptable value sets"
  - "Parameter dependencies and interaction rules"
  - "Test results for all parameter combinations"

EDGE_CASE_ANALYSIS:
  - "Boundary conditions and system limits"
  - "Special scenarios and exception cases"
  - "Compatibility issues and version dependencies"
  - "Test results for all edge case scenarios"

MUTUAL_EXCLUSIVITY_MATRIX:
  - "All mutually exclusive parameter combinations"
  - "Either/or scenarios and choice requirements"
  - "Conditional parameter conflicts"
  - "Error messages for all conflict scenarios"

ERROR_CONDITION_CATALOG:
  - "All error conditions with exact messages"
  - "Error codes and severity classifications"
  - "Error trigger conditions and test scenarios"
  - "Error recovery and cleanup requirements"

RESEARCH_VALIDATION:
  - "All validation checkpoints completed"
  - "Research completeness assessment"
  - "Identified gaps and areas needing further investigation"
  - "Readiness assessment for Phase 2 documentation"
```

## 🚨 CRITICAL RESEARCH PROTOCOLS

### Research Quality Standards
```yaml
DOCUMENTATION_THOROUGHNESS:
  MINIMUM_SOURCES: "3 official documentation sources minimum"
  VERSION_COVERAGE: "Target database version + 1 previous version"
  EXAMPLE_EXTRACTION: "ALL official examples extracted and tested"
  SYNTAX_COVERAGE: "100% of documented syntax variations tested"

TESTING_COMPLETENESS:
  DATABASE_VALIDATION: "ALL findings tested against real database instance"
  PARAMETER_COVERAGE: "Every parameter tested with valid/invalid/edge values"
  ERROR_VERIFICATION: "Every documented error condition triggered and validated"
  BOUNDARY_TESTING: "All system limits and boundaries tested"

ANALYSIS_DEPTH:
  MUTUAL_EXCLUSIVITY: "All conflict scenarios identified and tested"
  DEPENDENCY_MAPPING: "All parameter dependencies documented and validated"
  EDGE_CASE_COVERAGE: "All boundary conditions and special scenarios tested"
  VERSION_DIFFERENCES: "All version-specific behaviors documented"
```

### Research Validation Gates
```yaml
PHASE_1_COMPLETION_GATE:
  DOCUMENTATION_COMPLETE:
    - [ ] All official sources identified and thoroughly analyzed
    - [ ] Version-specific documentation reviewed
    - [ ] All syntax patterns extracted and documented
    
  TESTING_COMPLETE:
    - [ ] Every syntax variation tested against real database
    - [ ] All parameters tested with valid/edge/invalid values
    - [ ] All error conditions triggered and validated
    
  ANALYSIS_COMPLETE:
    - [ ] Parameter analysis table complete with all attributes
    - [ ] Mutual exclusivity matrix complete with all conflicts
    - [ ] Edge case analysis complete with all boundaries
    - [ ] Error condition catalog complete with all scenarios
    
  DELIVERABLE_READY:
    - [ ] Research findings document complete and comprehensive
    - [ ] All validation checkpoints passed
    - [ ] Research reviewed for completeness and accuracy
    - [ ] Ready for handoff to Phase 2 documentation
```

## ⚡ RESEARCH AUTOMATION TOOLS

### Automated Research Scripts
```bash
#!/bin/bash
# scripts/requirements-research-workflow.sh
# Automated research workflow with validation checkpoints

DATABASE=$1
OBJECT=$2
RESEARCH_DIR="research_findings"

echo "🔍 Starting Phase 1: Requirements Research for $DATABASE $OBJECT"

# Create research workspace
mkdir -p "$RESEARCH_DIR"
RESEARCH_FILE="$RESEARCH_DIR/research_findings_${DATABASE}_${OBJECT}.md"

# Initialize research document from template
cp templates/research_findings_template.md "$RESEARCH_FILE"

# Research validation checkpoints
echo "📋 Research Validation Checkpoints:"
echo "1.1 Official Documentation Analysis - [ ]"
echo "1.2 SQL Syntax Discovery and Validation - [ ]" 
echo "1.3 Parameter/Attribute Enumeration - [ ]"
echo "1.4 Edge Case Identification - [ ]"
echo "1.5 Mutual Exclusivity Rule Discovery - [ ]"
echo "1.6 Error Condition Mapping - [ ]"

echo "🎯 Research deliverable: $RESEARCH_FILE"
echo "🔄 Next Phase: ai_requirements_writeup.md"
```

## 🔗 CROSS-REFERENCES AND NAVIGATION

### Related Documents
```yaml
WORKFLOW_SEQUENCE:
  CURRENT: "Phase 1 - Requirements Research (THIS DOCUMENT)"
  NEXT: "Phase 2 - Requirements Documentation (ai_requirements_writeup.md)"
  FINAL: "Phase 3 - TDD Implementation (ai_workflow_guide.md)"

COMPANION_WORKFLOWS:
  SNAPSHOT_DIFF_RESEARCH: "../snapshot_diff_implementation/ai_requirements_research.md"
  MASTER_PROCESS: "master_process_loop.md"
  OVERALL_NAVIGATION: "README.md"

DELIVERABLE_CONSUMERS:
  PHASE_2_INPUT: "ai_requirements_writeup.md requires research_findings_[object].md"
  REQUIREMENTS_VALIDATION: "Quality gates validate research completeness"
```

### Research Decision Trees
```yaml
RESEARCH_SCOPE_DECISIONS:
  NEW_DATABASE_OBJECT:
    RESEARCH_FOCUS: "Complete object lifecycle (create/alter/drop operations)"
    PRIORITY_AREAS: "All parameters, all syntax variations, all error conditions"
    
  EXISTING_OBJECT_ENHANCEMENT:
    RESEARCH_FOCUS: "Database-specific attributes and syntax differences"
    PRIORITY_AREAS: "New parameters, syntax variations, compatibility impacts"
    
  SQL_SYNTAX_OVERRIDE:
    RESEARCH_FOCUS: "Database-specific SQL generation requirements"
    PRIORITY_AREAS: "Syntax differences, parameter handling, error conditions"
```

## 🎯 RESEARCH SUCCESS CRITERIA

### Complete Research Indicators
```yaml
DOCUMENTATION_MASTERY:
  - "All official documentation sources thoroughly analyzed"
  - "Version-specific differences clearly understood"
  - "All syntax patterns extracted and validated"
  
PRACTICAL_VALIDATION:
  - "Every documented feature tested against real database"
  - "All edge cases and error conditions verified"
  - "Complete understanding of actual vs documented behavior"
  
COMPREHENSIVE_ANALYSIS:
  - "All parameters categorized and analyzed"
  - "All mutual exclusivity rules identified and tested"
  - "Complete error condition catalog with exact messages"
  
IMPLEMENTATION_READINESS:
  - "Research findings document complete and comprehensive"
  - "All validation checkpoints passed"
  - "Ready for systematic requirements documentation in Phase 2"
```

## 💡 RESEARCH BEST PRACTICES

### Systematic Investigation Approach
1. **Start Broad, Go Deep**: Begin with overview documentation, then dive deep into specifics
2. **Test Everything**: Never assume documentation is accurate - validate with real database
3. **Document as You Go**: Capture findings immediately to avoid losing insights
4. **Question Assumptions**: Challenge documented behavior with edge case testing
5. **Think Implementation**: Consider how findings will translate to code requirements

### Common Research Pitfalls to Avoid
```yaml
INCOMPLETE_DOCUMENTATION_ANALYSIS:
  PROBLEM: "Stopping at first documentation source found"
  SOLUTION: "Systematically find and analyze ALL official sources"
  
UNTESTED_ASSUMPTIONS:
  PROBLEM: "Accepting documentation at face value without validation"
  SOLUTION: "Test every documented feature against real database"
  
SHALLOW_PARAMETER_ANALYSIS:
  PROBLEM: "Only testing happy path parameter combinations"
  SOLUTION: "Test all parameters with valid/edge/invalid values systematically"
  
MISSING_ERROR_CONDITIONS:
  PROBLEM: "Not investigating error scenarios thoroughly"
  SOLUTION: "Deliberately trigger every error condition and document exact messages"
```

Remember: **Impeccable requirements start with impeccable research**. This phase is the foundation for everything that follows. Leave no stone unturned, test everything, and document comprehensively. The quality of Phase 2 requirements documentation and Phase 3 implementation success depends entirely on the thoroughness of this research phase.