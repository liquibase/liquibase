# AI Requirements Research Workflow - Snapshot/Diff Implementation
## Phase 1: Active Investigation and Discovery of Database Object Structure and Behavior

## EXECUTION_PROTOCOL
```yaml
PHASE: 1_REQUIREMENTS_RESEARCH
PROTOCOL_VERSION: 3.0
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
DELIVERABLE: "research_findings_[object]_snapshot_diff.md"
NEXT_PHASE: "ai_requirements_writeup.md"
```

## WORKFLOW_OVERVIEW
```yaml
PURPOSE: "Active investigation and discovery of database object structure, properties, and snapshot/diff capabilities"
INPUT: "Database object name and understanding that snapshot/diff capabilities need implementation"
OUTPUT: "Complete research findings document with all object structure data for requirements writeup"
DURATION: "3-5 hours of focused research"
CRITICAL_SUCCESS_FACTOR: "Complete understanding of object structure and state properties before documentation"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/snapshot_diff_implementation/ai_requirements_research.md"
WORKFLOW_SEQUENCE:
  PHASE_1: "THIS DOCUMENT - Requirements Research"
  PHASE_2: "ai_requirements_writeup.md - Requirements Documentation"
  PHASE_3: "ai_workflow_guide.md - TDD Implementation"
  
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for snapshot/diff implementation"
  - main_guide.md: "Overview and systematic debugging framework"
  - ../changetype_implementation/ai_requirements_research.md: "Parallel research workflow for changetypes"
```

## 🔍 SNAPSHOT/DIFF RESEARCH INVESTIGATION PROTOCOL

### Step 1: Database Object Structure Discovery
```yaml
BLOCKING_VALIDATION_1.1:
  TYPE: "OBJECT_STRUCTURE_COMPLETE"
  REQUIREMENT: "Complete database object structure discovered through SHOW/DESCRIBE commands and official documentation"
  DELIVERABLE: "Complete object structure map with all properties, types, and metadata"
  FAILURE_ACTION: "STOP - Must have complete object structure before proceeding"

RESEARCH_ACTIVITIES:
  STRUCTURE_DISCOVERY:
    - "Identify ALL SHOW commands that return object information"
    - "Find ALL DESCRIBE commands that reveal object properties"
    - "Discover information_schema tables containing object metadata"
    - "Locate system views and catalog tables with object details"
    - "Find vendor-specific metadata commands and functions"
    
  STRUCTURE_ANALYSIS:
    - "Execute EVERY SHOW/DESCRIBE command against real database objects"
    - "Analyze ALL columns returned by each metadata command"
    - "Document ALL property names, data types, and possible values"
    - "Identify system-generated vs user-configurable properties"
    - "Map relationships between different metadata sources"

VALIDATION_CHECKPOINT_1.1:
  - [ ] All SHOW/DESCRIBE commands identified and executed
  - [ ] All information_schema and system view queries tested
  - [ ] Complete property inventory documented with types and values
  - [ ] System vs user properties clearly categorized
  - [ ] Object structure map complete and validated
```

### Step 2: Property Categorization and Behavior Analysis
```yaml
BLOCKING_VALIDATION_1.2:
  TYPE: "PROPERTY_CATEGORIZATION_COMPLETE"  
  REQUIREMENT: "All object properties categorized by type, behavior, and snapshot relevance"
  DELIVERABLE: "Complete property categorization matrix with behavioral analysis"
  FAILURE_ACTION: "STOP - Must have complete property categorization before proceeding"

RESEARCH_ACTIVITIES:
  PROPERTY_CATEGORIZATION:
    - "Identify REQUIRED properties (always present, never null)"
    - "Identify OPTIONAL properties (may be present, can be null)"
    - "Identify STATE properties (change frequently, not structural)"
    - "Identify STRUCTURAL properties (define object structure)"
    - "Identify METADATA properties (system-generated, read-only)"
    
  BEHAVIOR_ANALYSIS:
    - "Test property stability across object lifecycle operations"
    - "Validate which properties change during ALTER operations"
    - "Identify properties that should/shouldn't trigger diff detection"
    - "Test property behavior during object creation/modification"
    - "Document property dependencies and interaction rules"

VALIDATION_CHECKPOINT_1.2:
  - [ ] All properties categorized (Required/Optional/State/Structural/Metadata)
  - [ ] Property stability tested across object lifecycle
  - [ ] Properties that should trigger diffs clearly identified
  - [ ] State properties excluded from structural comparisons identified
  - [ ] Complete property categorization matrix created with behavioral notes
```

### Step 3: Snapshot SQL Command Discovery and Validation
```yaml
BLOCKING_VALIDATION_1.3:
  TYPE: "SNAPSHOT_SQL_COMPLETE"
  REQUIREMENT: "All SQL commands for object snapshots discovered, tested, and optimized"
  DELIVERABLE: "Complete snapshot SQL command catalog with performance analysis"
  FAILURE_ACTION: "STOP - Must have complete snapshot SQL before proceeding"

RESEARCH_ACTIVITIES:
  SQL_DISCOVERY:
    - "Identify optimal SHOW commands for object snapshots"
    - "Find most efficient DESCRIBE commands for property retrieval"
    - "Discover information_schema queries for batch object retrieval"
    - "Locate system view queries for comprehensive object metadata"
    - "Find vendor-specific efficient snapshot query patterns"
    
  SQL_VALIDATION:
    - "Test EVERY snapshot SQL command against real database"
    - "Validate SQL results contain ALL required properties"
    - "Test SQL performance with varying object counts"
    - "Verify SQL compatibility across database versions"
    - "Document exact SQL syntax and expected result formats"

VALIDATION_CHECKPOINT_1.3:
  - [ ] All snapshot SQL commands identified and tested
  - [ ] Optimal SQL patterns selected for performance
  - [ ] SQL results validated to contain all required properties
  - [ ] Performance characteristics documented for each SQL pattern
  - [ ] Complete snapshot SQL catalog created with examples
```

### Step 4: Object State and Lifecycle Analysis
```yaml
BLOCKING_VALIDATION_1.4:
  TYPE: "LIFECYCLE_ANALYSIS_COMPLETE"
  REQUIREMENT: "Complete object lifecycle and state transitions analyzed and documented"
  DELIVERABLE: "Object lifecycle analysis with all state transitions and property changes mapped"
  FAILURE_ACTION: "STOP - Must have complete lifecycle analysis before proceeding"

RESEARCH_ACTIVITIES:
  LIFECYCLE_DISCOVERY:
    - "Map ALL possible object states (creating, active, disabled, dropped, etc.)"
    - "Identify ALL operations that change object properties"
    - "Document ALL property changes during ALTER operations"
    - "Find ALL temporary or transitional states"
    - "Discover ALL system-managed state properties"
    
  STATE_ANALYSIS:
    - "Test object creation and document initial state"
    - "Execute ALL ALTER operations and document property changes"
    - "Test object in all possible states and document differences"
    - "Validate state transitions and property consistency"
    - "Document which properties indicate current object state"

VALIDATION_CHECKPOINT_1.4:
  - [ ] All possible object states identified and tested
  - [ ] All ALTER operations tested and property changes documented
  - [ ] State transition rules documented and validated
  - [ ] System-managed vs user-controlled states clearly separated
  - [ ] Complete object lifecycle analysis created with state property mapping
```

### Step 5: Comparison Logic and Diff Scenarios
```yaml
BLOCKING_VALIDATION_1.5:
  TYPE: "COMPARISON_LOGIC_COMPLETE"
  REQUIREMENT: "All comparison scenarios and diff detection logic requirements identified and validated"
  DELIVERABLE: "Complete comparison logic specification with all diff scenarios documented"
  FAILURE_ACTION: "STOP - Must have complete comparison logic before proceeding"

RESEARCH_ACTIVITIES:
  COMPARISON_DISCOVERY:
    - "Identify ALL properties that should trigger diff detection"
    - "Find ALL properties that should be ignored in comparisons"
    - "Discover ALL complex comparison scenarios (collections, nested objects)"
    - "Locate ALL edge cases in property comparison logic"
    - "Find ALL vendor-specific comparison considerations"
    
  DIFF_SCENARIO_ANALYSIS:
    - "Test ALL single property change scenarios"
    - "Test ALL multiple property change combinations"
    - "Test ALL edge cases (null values, empty collections, etc.)"
    - "Validate comparison behavior with different data types"
    - "Document expected diff output for all scenarios"

VALIDATION_CHECKPOINT_1.5:
  - [ ] All properties categorized for diff inclusion/exclusion
  - [ ] All comparison scenarios tested and documented
  - [ ] Complex comparison logic requirements identified
  - [ ] Edge cases and special comparison scenarios documented
  - [ ] Complete comparison logic specification created with test scenarios
```

### Step 6: Integration Points and Framework Compatibility
```yaml
BLOCKING_VALIDATION_1.6:
  TYPE: "INTEGRATION_ANALYSIS_COMPLETE"
  REQUIREMENT: "All Liquibase framework integration points and compatibility requirements analyzed"
  DELIVERABLE: "Complete integration analysis with framework compatibility assessment"
  FAILURE_ACTION: "STOP - Must have complete integration analysis before proceeding"

RESEARCH_ACTIVITIES:
  INTEGRATION_DISCOVERY:
    - "Identify ALL Liquibase interfaces for snapshot/diff implementation"
    - "Find ALL service registration requirements and patterns"
    - "Discover ALL framework extension points and integration patterns"
    - "Locate ALL existing implementations for reference and compatibility"
    - "Find ALL framework limitations and constraints"
    
  COMPATIBILITY_ANALYSIS:
    - "Test framework integration patterns with test implementations"
    - "Validate service registration and discovery mechanisms"
    - "Test framework compatibility with different object types"
    - "Analyze framework limitations and realistic success criteria"
    - "Document integration requirements and implementation patterns"

VALIDATION_CHECKPOINT_1.6:
  - [ ] All Liquibase integration interfaces identified
  - [ ] Service registration requirements documented
  - [ ] Framework limitations and constraints analyzed
  - [ ] Realistic success criteria established based on framework capabilities
  - [ ] Complete integration analysis created with implementation guidance
```

## 📊 RESEARCH DELIVERABLE TEMPLATE

### Research Findings Document Structure
```yaml
FILENAME: "research_findings_[database]_[object]_snapshot_diff.md"
TEMPLATE_SECTIONS:

RESEARCH_METADATA:
  - "Research date and duration"
  - "Database version and environment details"
  - "Researcher identification and validation"
  - "Research scope and object type analyzed"

OBJECT_STRUCTURE_ANALYSIS:
  - "Complete property inventory with types and constraints"
  - "SHOW/DESCRIBE command catalog with result examples"
  - "Information schema and system view query patterns"
  - "Object structure map with all metadata sources"

PROPERTY_CATEGORIZATION_MATRIX:
  - "Required properties (always present)"
  - "Optional properties (may be null/absent)"
  - "State properties (excluded from structural comparison)"
  - "Structural properties (trigger diff detection)"
  - "Metadata properties (system-generated, read-only)"

SNAPSHOT_SQL_CATALOG:
  - "Optimal snapshot SQL commands with performance analysis"
  - "Complete SQL syntax with parameter handling"
  - "Expected result formats and data type mappings"
  - "Batch query patterns for multiple object retrieval"

OBJECT_LIFECYCLE_ANALYSIS:
  - "All possible object states and transitions"
  - "Property changes during ALTER operations"
  - "System-managed vs user-controlled state properties"
  - "State transition rules and consistency requirements"

COMPARISON_LOGIC_SPECIFICATION:
  - "Properties included/excluded from diff detection"
  - "Complex comparison scenarios and edge cases"
  - "Data type-specific comparison requirements"
  - "Expected diff output formats for all scenarios"

INTEGRATION_ANALYSIS:
  - "Liquibase framework integration requirements"
  - "Service registration patterns and requirements"
  - "Framework limitations and realistic success criteria"
  - "Implementation patterns and integration guidance"

RESEARCH_VALIDATION:
  - "All validation checkpoints completed"
  - "Research completeness assessment"
  - "Identified gaps and areas needing further investigation"
  - "Readiness assessment for Phase 2 documentation"
```

## 🚨 CRITICAL RESEARCH PROTOCOLS

### Snapshot/Diff Specific Research Quality Standards
```yaml
OBJECT_STRUCTURE_THOROUGHNESS:
  METADATA_COMPLETENESS: "ALL SHOW/DESCRIBE commands tested and documented"
  PROPERTY_COVERAGE: "100% of object properties identified and categorized"
  STATE_ANALYSIS: "All object states and transitions mapped"
  INTEGRATION_VALIDATION: "Framework compatibility thoroughly assessed"

SNAPSHOT_SQL_COMPLETENESS:
  COMMAND_TESTING: "Every snapshot SQL command tested against real database"
  PERFORMANCE_ANALYSIS: "SQL performance characteristics documented"
  RESULT_VALIDATION: "SQL results validated to contain all required properties"
  VERSION_COMPATIBILITY: "SQL compatibility across database versions verified"

COMPARISON_LOGIC_DEPTH:
  PROPERTY_CATEGORIZATION: "All properties categorized for comparison inclusion/exclusion"
  SCENARIO_COVERAGE: "All diff scenarios tested and documented"
  EDGE_CASE_ANALYSIS: "All edge cases and special comparison scenarios identified"
  FRAMEWORK_ALIGNMENT: "Comparison logic aligned with Liquibase patterns"
```

### Research Validation Gates
```yaml
PHASE_1_COMPLETION_GATE:
  STRUCTURE_COMPLETE:
    - [ ] Complete object structure discovered and mapped
    - [ ] All properties categorized by type and behavior
    - [ ] Property stability across lifecycle operations validated
    
  SNAPSHOT_COMPLETE:
    - [ ] All snapshot SQL commands identified and tested
    - [ ] Optimal SQL patterns selected for performance
    - [ ] SQL results validated for completeness
    
  ANALYSIS_COMPLETE:
    - [ ] Object lifecycle and state transitions mapped
    - [ ] Comparison logic requirements specified
    - [ ] Framework integration points analyzed
    
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
# scripts/snapshot-diff-research-workflow.sh
# Automated snapshot/diff research workflow with validation checkpoints

DATABASE=$1
OBJECT=$2
RESEARCH_DIR="research_findings"

echo "🔍 Starting Phase 1: Snapshot/Diff Requirements Research for $DATABASE $OBJECT"

# Create research workspace
mkdir -p "$RESEARCH_DIR"
RESEARCH_FILE="$RESEARCH_DIR/research_findings_${DATABASE}_${OBJECT}_snapshot_diff.md"

# Initialize research document from template
cp templates/snapshot_diff_research_template.md "$RESEARCH_FILE"

# Research validation checkpoints
echo "📋 Research Validation Checkpoints:"
echo "1.1 Database Object Structure Discovery - [ ]"
echo "1.2 Property Categorization and Behavior Analysis - [ ]"
echo "1.3 Snapshot SQL Command Discovery and Validation - [ ]"
echo "1.4 Object State and Lifecycle Analysis - [ ]"
echo "1.5 Comparison Logic and Diff Scenarios - [ ]"
echo "1.6 Integration Points and Framework Compatibility - [ ]"

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
  CHANGETYPE_RESEARCH: "../changetype_implementation/ai_requirements_research.md"
  MAIN_GUIDE: "main_guide.md"
  OVERALL_NAVIGATION: "README.md"

DELIVERABLE_CONSUMERS:
  PHASE_2_INPUT: "ai_requirements_writeup.md requires research_findings_[object]_snapshot_diff.md"
  REQUIREMENTS_VALIDATION: "Quality gates validate research completeness"
```

### Research Decision Trees
```yaml
RESEARCH_SCOPE_DECISIONS:
  NEW_DATABASE_OBJECT:
    RESEARCH_FOCUS: "Complete object structure, lifecycle, and comparison requirements"
    PRIORITY_AREAS: "All properties, all states, all comparison scenarios"
    
  EXISTING_OBJECT_ENHANCEMENT:
    RESEARCH_FOCUS: "Property differences and enhanced comparison requirements"
    PRIORITY_AREAS: "New properties, state changes, comparison logic updates"
    
  FRAMEWORK_COMPATIBILITY:
    RESEARCH_FOCUS: "Integration points and realistic success criteria"
    PRIORITY_AREAS: "Service registration, framework limitations, test harness scope"
```

## 🎯 RESEARCH SUCCESS CRITERIA

### Complete Research Indicators
```yaml
OBJECT_STRUCTURE_MASTERY:
  - "All object properties discovered and categorized"
  - "Complete understanding of object lifecycle and states"
  - "All snapshot SQL commands identified and optimized"
  
COMPARISON_LOGIC_CLARITY:
  - "All diff scenarios analyzed and documented"
  - "Property comparison rules clearly defined"
  - "Edge cases and special scenarios thoroughly tested"
  
FRAMEWORK_INTEGRATION_UNDERSTANDING:
  - "Liquibase integration points clearly mapped"
  - "Framework limitations understood and documented"
  - "Realistic success criteria established"
  
IMPLEMENTATION_READINESS:
  - "Research findings document complete and comprehensive"
  - "All validation checkpoints passed"
  - "Ready for systematic requirements documentation in Phase 2"
```

## 💡 SNAPSHOT/DIFF RESEARCH BEST PRACTICES

### Systematic Investigation Approach
1. **Structure First**: Understand complete object structure before analyzing behavior
2. **Test All Commands**: Validate every SHOW/DESCRIBE command against real objects
3. **Categorize Properties**: Clearly separate structural from state properties
4. **Map Lifecycles**: Understand how properties change throughout object lifecycle
5. **Consider Framework**: Always research with Liquibase integration in mind

### Common Research Pitfalls to Avoid
```yaml
INCOMPLETE_PROPERTY_ANALYSIS:
  PROBLEM: "Not categorizing properties by behavior and comparison relevance"
  SOLUTION: "Systematically categorize every property for snapshot/diff purposes"
  
MISSING_STATE_PROPERTIES:
  PROBLEM: "Including frequently-changing state properties in structural comparisons"
  SOLUTION: "Clearly identify and exclude state properties from diff detection"
  
UNTESTED_SQL_COMMANDS:
  PROBLEM: "Assuming SHOW/DESCRIBE commands work without validation"
  SOLUTION: "Test every snapshot SQL command against real database objects"
  
FRAMEWORK_ASSUMPTIONS:
  PROBLEM: "Not understanding Liquibase framework limitations and integration requirements"
  SOLUTION: "Thoroughly research framework integration points and establish realistic success criteria"
```

### Snapshot/Diff Specific Considerations
```yaml
PROPERTY_STABILITY:
  QUESTION: "Does this property change frequently or represent structural information?"
  DECISION: "Include only stable structural properties in diff comparisons"
  
COMPARISON_SENSITIVITY:
  QUESTION: "Should changes to this property trigger changelog generation?"
  DECISION: "Include only properties where changes require database migration"
  
FRAMEWORK_SCOPE:
  QUESTION: "Will the test harness include this object type in snapshot scope?"
  DECISION: "Validate scope early and set realistic success criteria"
```

Remember: **Successful snapshot/diff implementations depend on complete understanding of object structure and behavior**. This research phase must thoroughly analyze every aspect of the database object that will be snapshotted and compared. The quality of object modeling, snapshot SQL, and comparison logic in later phases depends entirely on the thoroughness of this research phase.