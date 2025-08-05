# Enforced TDD Snapshot/Diff Implementation Guide
## External Enforcement + Legacy Guide Integration

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 6.0
DOCUMENT_TYPE: EXTERNALLY_ENFORCED_AI_TDD_GUIDE  
EXECUTION_MODE: STATE_DRIVEN_BLOCKING_ENFORCEMENT
QUALITY_GATE_MODE: HARD_VALIDATION_WITH_CHECKPOINTS
TESTING_STRATEGY: 4_CATEGORY_SYSTEMATIC_COVERAGE
ENFORCEMENT_TYPE: EXTERNAL_BEHAVIORAL_BLOCKING
LEGACY_INTEGRATION: COMPLETE_8_MICROCYCLE_PATTERN
AUTONOMOUS_OPERATION: ENABLED_WITH_ENFORCEMENT_VALIDATION
INNOVATION: COMBINES_SELF_DISCIPLINE_SOLUTION_WITH_EXTERNAL_BLOCKING
```

## 🎯 THE BREAKTHROUGH: External Enforcement Solution

**Problem Identified**: Past approaches failed due to reliance on self-discipline under pressure.

**Solution**: **External enforcement system that blocks bad behavior** rather than relying on memory/discipline.

### Core Innovation: Blocking vs Reminding
```bash
# OLD APPROACH (Failed)
echo "Remember to follow TDD!" 
# → Gets ignored under pressure

# NEW APPROACH (Enforced)  
test -f ".checkpoints/requirements_complete" || {
    echo "BLOCKED: Requirements phase not complete"
    return 1  # HARD FAILURE - CANNOT PROCEED
}
```

## 🚀 QUICK START: Complete FileFormat Implementation

### Pre-Flight Validation (Run First)
```bash
# Verify enforcement system operational
.scripts/tdd_workflow.sh status

# Verify all prerequisites  
test -f .scripts/tdd_workflow.sh && echo "✅ TDD workflow available"
test -f .templates/object_model_template.java && echo "✅ Templates available"
mvn compile -q && echo "✅ Project compiles"
```

### 3-Command Implementation
```bash
# 1. Initialize with enforcement
.scripts/tdd_workflow.sh init FileFormat NEW_OBJECT

# 2. Follow guided phases (enforcement prevents skipping)
.scripts/tdd_workflow.sh next

# 3. Execute micro-cycles with validation
.scripts/tdd_workflow.sh cycle <name> <test_class> <test_method>
```

## IMPLEMENTATION SCENARIOS

### Scenario Selection with Enforcement
```yaml
SCENARIO_A_NEW_OBJECT:
  DESCRIPTION: "Implement snapshot/diff for completely new database object"
  WORKFLOW: "Phase 0 → Phase 1 → Phase 2 (AI-TDD Micro-Cycles) → Phase 3 (XSD Integration)"
  DURATION: "2-4 hours (45min research + 8-12 micro-cycles + XSD validation)"
  ENFORCEMENT: "External blocking prevents phase skipping"
  
SCENARIO_B_ENHANCE_EXISTING:
  DESCRIPTION: "Enhance existing object with additional properties"
  WORKFLOW: "Phase 0 → Phase 2 (AI-TDD Micro-Cycles for enhancements)"
  DURATION: "1-2 hours (4-6 micro-cycles for new properties)"
  AUTOMATION_AVAILABLE:
    PROGRAM: "aipl_programs/incomplete-implementation-detection.yaml"
    TRIGGER: "When detecting missing methods or incomplete patterns"
    BENEFIT: "Systematic detection and template generation for missing components"
  
SCENARIO_C_COMPLETE_INCOMPLETE:
  DESCRIPTION: "Complete incomplete snapshot/diff implementation"
  WORKFLOW: "Phase 0 → Phase 2 (AI-TDD Micro-Cycles for missing components)"
  DURATION: "1-3 hours (2-8 micro-cycles depending on missing pieces)"
  ENFORCEMENT: "Validation prevents completion until all components implemented"
  
SCENARIO_D_FIX_BUGS:
  DESCRIPTION: "Fix bugs in existing implementation"
  WORKFLOW: "Phase 0 → AI-TDD Micro-Cycles (test-driven bug fixes)"
  DURATION: "30min-2 hours (1-4 micro-cycles per bug)"
  AUTOMATION_AVAILABLE:
    PROGRAM: "aipl_programs/systematic-implementation-debugging.yaml"
    TRIGGER: "When troubleshooting compilation or runtime errors"
    BENEFIT: "Layer-by-layer automated diagnosis and isolation"
  
SCENARIO_E_OPTIMIZE_PERFORMANCE:
  DESCRIPTION: "Performance optimization of existing implementation"  
  WORKFLOW: "Phase 0 → AI-TDD Micro-Cycles (test-driven optimization)"
  DURATION: "1-3 hours (2-6 micro-cycles with performance tests)"
```

## PHASE 0: QUICK VALIDATION AND ASSESSMENT (OPTIONAL)

### When to Use Quick Validation
- Checking if snapshot/diff already exists for object type
- Quick database connectivity and scope validation  
- 15-minute assessment before full implementation
- Validating realistic success criteria for test harness

### STEP 0.1: Quick Database Object Validation
```bash
# Test database connectivity and object queryability
echo "SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S LIMIT 1" | sqlcmd

# Check if snapshot/diff already implemented
find . -name "*${ObjectType}*.java" -path "*/liquibase-core/*"
find . -name "*${ObjectType}*SnapshotGenerator*"
find . -name "*${ObjectType}*Comparator*"
```

### STEP 0.2: Implementation Scope Decision
```yaml
SCOPE_DECISION_MATRIX:
  IF_CORE_OBJECT_EXISTS:
    PATTERN: "EXTEND_EXISTING_OBJECT"
    APPROACH: "Enhance existing object model with database-specific properties"
    COMPLEXITY: "Medium"
    ENFORCEMENT: "Enhanced validation prevents generic implementations"
    
  IF_NO_CORE_OBJECT:
    PATTERN: "NEW_OBJECT_IMPLEMENTATION" 
    APPROACH: "Create complete object model, snapshot, and diff"
    COMPLEXITY: "High"
    ENFORCEMENT: "Complete 8 micro-cycle pattern required"
    
  IF_SNAPSHOT_EXISTS_DIFF_MISSING:
    PATTERN: "COMPLETE_DIFF_ONLY"
    APPROACH: "Implement comparator for existing snapshot"
    COMPLEXITY: "Low"
    ENFORCEMENT: "Comparator-specific validation"
    
  IF_DIFF_EXISTS_SNAPSHOT_MISSING:
    PATTERN: "COMPLETE_SNAPSHOT_ONLY"
    APPROACH: "Implement snapshot generator for existing diff"
    COMPLEXITY: "Medium"
    ENFORCEMENT: "Snapshot-specific micro-cycles required"
```

## PHASE 1: ENFORCED REQUIREMENTS RESEARCH

### External Validation Checkpoints
```yaml
REQUIREMENTS_ENFORCEMENT:
  PROPERTY_COUNT_MINIMUM: "25+ properties (20+ beyond identity)"
  SNOWFLAKE_SPECIFICITY: "15+ domain-specific values (CSV,JSON,GZIP,etc.)"
  ANTI_GENERIC_VALIDATION: "0 generic properties allowed"
  SQL_COMPLETENESS: "Working INFORMATION_SCHEMA queries required"
  
BLOCKING_MECHANISM:
  COMMAND: "validate_enhanced_property_requirements()"
  FAILURE_ACTION: "CANNOT_PROCEED_TO_NEXT_PHASE"
  CHECKPOINT_FILE: ".checkpoints/requirements_complete"
```

### Step 1.1: Research Documentation (Enforced)
```bash
# System tracks these WebFetch calls and validates completeness
WebFetch: CREATE ${ObjectType} documentation  
WebFetch: SHOW ${ObjectType}S documentation
WebFetch: DESCRIBE ${ObjectType} documentation

# Validation runs automatically
.scripts/enhanced_validation.sh validate_enhanced_property_requirements requirements.md ${ObjectType}
```

## PHASE 2: ENFORCED 8 MICRO-CYCLE TDD IMPLEMENTATION

### Micro-Cycle Structure (Legacy Guide Proven Pattern)
```yaml
PROVEN_8_MICROCYCLE_PATTERN:
  SOURCE: "Legacy guide: FileFormat completed in 8 micro-cycles, 30 minutes"
  DURATION: "2-3 minutes per cycle"
  TOTAL_TIME: "24-30 minutes active development"
  SUCCESS_RATE: "100% when enforcement prevents shortcuts"
  
MICRO_CYCLE_CATEGORIES:
  OBJECT_MODEL_CYCLES: "Cycles 1-3 (Identity, Properties, Framework Integration)"
  SNAPSHOT_CYCLES: "Cycles 4-5 (Basic Support, Database Querying)"  
  COMPARATOR_CYCLES: "Cycles 6-7 (Identity Comparison, Property Comparison)"
  INTEGRATION_CYCLE: "Cycle 8 (Service Registration, Final Integration)"
```

### Enhanced Micro-Cycle Execution (With Blocking)
```yaml
ENFORCED_MICROCYCLE_TEMPLATE:
  RED_PHASE_ENFORCEMENT:
    VALIDATION: "Test MUST fail first"
    COMMAND: "validate_red_phase(test_class, test_method)"
    BLOCKING: "Cannot proceed to GREEN until test fails"
    
  GREEN_PHASE_ENFORCEMENT:
    VALIDATION: "Test MUST pass after minimal implementation"
    COMMAND: "validate_green_phase(test_class, test_method)"
    BLOCKING: "Cannot proceed to REFACTOR until test passes"
    
  REFACTOR_PHASE_ENFORCEMENT:
    VALIDATION: "All tests MUST still pass"
    COMMAND: "validate_refactor_phase(test_class)"
    BLOCKING: "Cannot complete cycle until all tests pass"
```

### 4-Category Test Structure (Legacy Guide Integration)
```yaml
SYSTEMATIC_TEST_CATEGORIES:
  POSITIVE_TESTING:
    PURPOSE: "Happy path scenarios with valid inputs"
    EXAMPLES:
      - "testConstructorWithName()"
      - "testBasicGettersAndSetters()"
      - "testValidPropertyValues()"
    SECTION_MARKER: "// === POSITIVE TESTS ==="
    TEMPLATE: |
      @Test
      void should${ExpectedBehavior}When${ValidInput}() {
          // Arrange: Setup valid test data
          ${ObjectType} ${objectType} = new ${ObjectType}();
          ${objectType}.setName("VALID_NAME");
          
          // Act: Execute the operation
          ${ResultType} result = ${methodUnderTest}(${objectType});
          
          // Assert: Verify expected behavior
          assertNotNull(result);
          assertEquals("VALID_NAME", result.getName());
      }
    
  NEGATIVE_TESTING:
    PURPOSE: "Invalid input handling and error conditions"
    EXAMPLES:
      - "testNullNameHandling()"
      - "testInvalidPropertyValues()"
      - "testUnsupportedOperations()"
    SECTION_MARKER: "// === NEGATIVE TESTS ==="
    TEMPLATE: |
      @Test
      void should${ErrorBehavior}When${InvalidInput}() {
          // Arrange: Setup invalid test data
          ${ObjectType} ${objectType} = new ${ObjectType}();
          ${objectType}.setName(null); // Invalid input
          
          // Act & Assert: Verify error handling
          assertThrows(${ExpectedException}.class, () -> {
              ${methodUnderTest}(${objectType});
          });
      }
    
  BOUNDARY_TESTING:
    PURPOSE: "Edge conditions at data type limits"
    EXAMPLES:
      - "testEmptyNameHandling()"
      - "testMaximumLengthValues()"
      - "testMinimumValidValues()"
    SECTION_MARKER: "// === BOUNDARY TESTS ==="
    TEMPLATE: |
      @Test
      void should${BehaviorAtBoundary}When${BoundaryCondition}() {
          // Arrange: Setup boundary condition
          ${ObjectType} ${objectType} = new ${ObjectType}();
          ${objectType}.setName(""); // Empty string boundary
          
          // Act: Execute the operation
          ${ResultType} result = ${methodUnderTest}(${objectType});
          
          // Assert: Verify boundary handling
          ${boundaryAssertion}(result);
      }
    
  EDGE_CASE_TESTING:
    PURPOSE: "Complex scenarios and contract validation"
    EXAMPLES:
      - "testEqualsContract()"
      - "testHashCodeContract()"
      - "testToStringFormat()"
    SECTION_MARKER: "// === EDGE CASE TESTS ==="
    TEMPLATE: |
      @Test
      void should${EdgeCaseBehavior}When${SpecialScenario}() {
          // Arrange: Setup edge case scenario
          ${ObjectType} obj1 = new ${ObjectType}();
          obj1.setName("Test_Object");
          ${ObjectType} obj2 = new ${ObjectType}();
          obj2.setName("TEST_OBJECT"); // Case sensitivity edge case
          
          // Act: Execute comparison operation
          boolean result = ${comparisonMethod}(obj1, obj2);
          
          // Assert: Verify edge case handling
          ${edgeCaseAssertion}(result);
      }

VALIDATION_ENFORCEMENT:
  COMMAND: "validate_comprehensive_test_coverage(test_class)"
  REQUIREMENTS: "All 4 categories must be present with section markers"
  MINIMUM_TESTS: "15+ total tests across all categories"
  TEMPLATE_COMPLIANCE: "Tests must follow Arrange-Act-Assert pattern"
```

## PHASE 3: XSD SCHEMA INTEGRATION (COMPLETE CHANGETYPE COMPLIANCE)

### XSD Integration Overview
```yaml
XSD_INTEGRATION_PURPOSE:
  OBJECTIVE: "Ensure snapshot/diff objects are properly integrated with Liquibase XSD schema"
  COMPLIANCE: "Complete changetype workflow compatibility"
  ENFORCEMENT: "External validation prevents incomplete integration"
  INTEGRATION_POINTS:
    - "DatabaseObject XSD definitions"
    - "SnapshotGenerator service registration"
    - "DatabaseObjectComparator service registration"
    - "Schema validation for XML changelogs"
    
CRITICAL_WORKFLOW_DEPENDENCY:
  SNAPSHOT_TO_CHANGETYPE: "Objects discovered by snapshot must be supported by changetype generation"
  ENFORCEMENT_CHECKPOINT: "XSD validation required before integration completion"
```

### STEP 3.1: XSD Schema Validation (Enforced)
```yaml
XSD_VALIDATION_REQUIREMENTS:
  SCHEMA_FILES:
    PRIMARY: "src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
    BACKUP: "Previous version XSD files for compatibility"
    
  VALIDATION_COMMANDS:
    - "xmllint --schema liquibase-snowflake-latest.xsd sample_changelog.xml"
    - "mvn compile -q # Ensures XSD compilation"
    - ".scripts/enhanced_validation.sh validate_xsd_integration_readiness ${ObjectType}"
    
  ENFORCEMENT_BLOCKING:
    FAILURE_ACTION: "Cannot complete integration until XSD validates"
    CHECKPOINT_REQUIRED: ".checkpoints/xsd_integration_complete"
```

### STEP 3.2: Changetype Integration Validation
```yaml
CHANGETYPE_INTEGRATION_CHECK:
  REQUIRED_ELEMENTS:
    - "DatabaseObject properly extends AbstractDatabaseObject"
    - "setAttribute/getAttribute methods for changetype properties"
    - "XSD element definition for object type"
    - "Changetype implementation (if applicable)"
    
  VALIDATION_PROCESS:
    COMMAND: "validate_xsd_integration_readiness()"
    BLOCKING: "Hard stop if changetype integration incomplete"
    SUCCESS_CRITERIA: "Object appears in snapshot AND can be used in changesets"
```

## PHASE 4: ENFORCED INTEGRATION AND VALIDATION

### Service Registration Enforcement
```yaml
SERVICE_REGISTRATION_VALIDATION:
  SNAPSHOT_GENERATOR: "Must be registered in META-INF/services/liquibase.snapshot.SnapshotGenerator"
  DIFF_COMPARATOR: "Must be registered in META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
  VALIDATION_COMMAND: "validate_service_registration(object_type)"
  BLOCKING: "Cannot complete until services registered"
```

### Final Integration Tests (Enforced)
```bash
# These commands run with autonomous approval - but must pass
mvn test -Dtest="*${ObjectType}*Test*" -q
mvn compile -q
SNOWFLAKE_URL="..." mvn test -Dtest="*${ObjectType}*IntegrationTest" -q
```

## ENFORCEMENT COMMANDS REFERENCE

### Workflow Control
```bash
# Initialize enforcement system
.scripts/tdd_workflow.sh init <ObjectType> [scenario]

# Check current state and requirements
.scripts/tdd_workflow.sh status

# Execute next phase (with validation)
.scripts/tdd_workflow.sh next

# Execute single micro-cycle (with TDD enforcement)
.scripts/tdd_workflow.sh cycle <name> <test_class> <test_method>

# Complete current phase (with comprehensive validation)
.scripts/tdd_workflow.sh complete

# Reset workflow (emergency use only)
.scripts/tdd_workflow.sh reset
```

### Template Generation
```bash
# Generate all template files for object type
.scripts/template_substitution.sh generate <ObjectType>

# Single template substitution
.scripts/template_substitution.sh substitute <template> <output> <ObjectType>
```

### Enhanced Validation
```bash
# Comprehensive test coverage validation
.scripts/enhanced_validation.sh validate_comprehensive_test_coverage <TestClass>

# 8 micro-cycle completion validation  
.scripts/enhanced_validation.sh validate_8_microcycle_completion <ObjectType>

# Enhanced requirements validation (legacy guide standards)
.scripts/enhanced_validation.sh validate_enhanced_property_requirements <req_file> <ObjectType>

# XSD integration readiness
.scripts/enhanced_validation.sh validate_xsd_integration_readiness <ObjectType>
```

## SUCCESS CRITERIA (Enforced Validation)

### Completion Validation Checklist
```yaml
PHASE_COMPLETION_GATES:
  REQUIREMENTS_PHASE:
    - "25+ properties documented with Snowflake-specific values"
    - "0 generic properties (anti-generic validation)"
    - "Working SQL queries from INFORMATION_SCHEMA"
    - "Checkpoint: .checkpoints/requirements_complete"
    
  OBJECT_MODEL_PHASE:
    - "4-category test structure with 15+ tests"
    - "All tests passing (comprehensive validation)"
    - "Object compiles and extends AbstractDatabaseObject"
    - "Checkpoint: .checkpoints/object_model_complete"
    
  SNAPSHOT_GENERATOR_PHASE:
    - "Database querying implementation"
    - "Test coverage across all 4 categories"
    - "Integration with Liquibase framework"
    - "Checkpoint: .checkpoints/snapshot_generator_complete"
    
  DIFF_COMPARATOR_PHASE:
    - "Property-by-property comparison logic"
    - "Identity comparison implementation"
    - "ObjectDifferences handling"
    - "Checkpoint: .checkpoints/diff_comparator_complete"
    
  INTEGRATION_PHASE:
    - "Services registered in META-INF"
    - "Integration tests passing"
    - "8+ micro-cycles completed"
    - "Checkpoint: .checkpoints/integration_complete"
```

### Final Validation Commands
```bash
# Verify 8 micro-cycle pattern completion
.scripts/enhanced_validation.sh validate_8_microcycle_completion FileFormat

# Comprehensive test coverage check
.scripts/enhanced_validation.sh validate_comprehensive_test_coverage FileFormat

# XSD integration readiness
.scripts/enhanced_validation.sh validate_xsd_integration_readiness FileFormat

# Complete workflow state check
.scripts/tdd_workflow.sh status
```

## BEHAVIORAL CHANGE MECHANISM

### Why This Works vs Previous Approaches

**Self-Discipline Approach (Failed)**:
```
Me: "I'll remember to follow TDD"  
Pressure: "Just implement it quickly"
Result: Skipped tests, broken implementation
```

**External Enforcement Approach (New)**:
```bash
# Literally cannot proceed without validation
transition_to_phase() {
    validate_phase_transition "$current" "$new" || {
        echo "BLOCKED: Complete current phase first"
        return 1  # HARD STOP
    }
}
```

### Enforcement Points
1. **Phase Transitions**: Cannot skip phases without completing validation
2. **TDD Red Phase**: Test must fail first before implementation  
3. **TDD Green Phase**: Test must pass before refactoring
4. **Requirements Quality**: Cannot proceed with generic/incomplete requirements
5. **Test Coverage**: Must have 4-category systematic test structure
6. **Service Registration**: Cannot complete without proper META-INF registration

## EXPECTED OUTCOME

**Timeline**: Complete FileFormat implementation in ~1 hour (30min setup + 30min execution)
**Quality**: 100% test coverage by design, proven 8 micro-cycle pattern
**Reliability**: External enforcement prevents shortcuts and ensures quality gates
**Reusability**: Works for any Snowflake object type with universal templates

This guide combines the **proven 8 micro-cycle pattern** from the legacy guide with **external enforcement mechanisms** to achieve our ultimate goal: autonomous, high-quality snapshot/diff implementations without relying on self-discipline.

## PHASE 5: COMPREHENSIVE ERROR PATTERNS AND DEBUGGING

### Common Error Patterns with Enforcement Solutions
```yaml
COMMON_ERROR_PATTERNS:
  SERVICE_REGISTRATION_MISSING:
    SYMPTOM: "Objects not found in snapshot/diff operations"
    CAUSE: "Missing or incorrect META-INF/services registration"
    SOLUTION: "Use automated validation: aipl_programs/snapshot-diff-registration-validation.yaml"
    ENFORCEMENT: "validate_service_registration() blocks completion until fixed"
    COMMAND: ".scripts/enhanced_validation.sh validate_service_registration ${ObjectType}"
    
  SQL_QUERY_ERRORS:
    SYMPTOM: "DatabaseException during snapshot generation"
    CAUSE: "Invalid SQL syntax or missing columns in INFORMATION_SCHEMA queries"
    SOLUTION: "Test SQL queries directly in database console"
    ENFORCEMENT: "Snapshot generator validation requires working queries"
    DEBUG_COMMAND: "Execute snapshot SQL manually to verify syntax"
    
  COMPILATION_ISSUES:
    SYMPTOM: "Java compilation errors in object model or generators"
    CAUSE: "Incorrect imports, missing methods, or framework API changes"
    SOLUTION: "Use systematic debugging: aipl_programs/systematic-implementation-debugging.yaml"
    ENFORCEMENT: "mvn compile -q must pass before proceeding to next phase"
    
  TEST_COVERAGE_INSUFFICIENT:
    SYMPTOM: "validate_comprehensive_test_coverage() fails"
    CAUSE: "Missing test categories or insufficient test count"
    SOLUTION: "Add tests following 4-category structure with templates"
    ENFORCEMENT: "Cannot complete object model phase without 15+ tests in 4 categories"
    
  XSD_VALIDATION_FAILURES:
    SYMPTOM: "XML changelog validation errors"
    CAUSE: "Missing XSD definitions or incorrect schema elements"
    SOLUTION: "Update XSD schema file and validate with xmllint"
    ENFORCEMENT: "XSD integration phase blocks until schema validates"
    
  EQUALS_HASHCODE_CONTRACT_VIOLATIONS:
    SYMPTOM: "Object comparison failures in diff operations"
    CAUSE: "Incorrect equals/hashCode implementation"
    SOLUTION: "Follow TDD micro-cycle for identity methods with edge case tests"
    ENFORCEMENT: "Edge case test validation ensures contract compliance"
```

### Systematic Debugging Protocol
```yaml
DEBUGGING_WORKFLOW:
  STEP_1_COMPILATION:
    COMMAND: "mvn compile -q"
    SUCCESS_ACTION: "Proceed to Step 2"
    FAILURE_ACTION: "Fix compilation errors before proceeding"
    ENFORCEMENT: "Hard block until compilation passes"
    
  STEP_2_UNIT_TESTS:
    COMMAND: "mvn test -Dtest='*${ObjectType}*Test*' -q"
    SUCCESS_ACTION: "Proceed to Step 3"
    FAILURE_ACTION: "Fix failing unit tests"
    ENFORCEMENT: "Cannot proceed without passing unit tests"
    
  STEP_3_SERVICE_REGISTRATION:
    COMMAND: ".scripts/enhanced_validation.sh validate_service_registration ${ObjectType}"
    SUCCESS_ACTION: "Proceed to Step 4"
    FAILURE_ACTION: "Fix service registration issues"
    ENFORCEMENT: "Hard validation checkpoint"
    
  STEP_4_INTEGRATION_TESTS:
    COMMAND: "SNOWFLAKE_URL='...' mvn test -Dtest='*${ObjectType}*IntegrationTest' -q"
    SUCCESS_ACTION: "Implementation complete"
    FAILURE_ACTION: "Debug integration issues"  
    ENFORCEMENT: "Final validation gate"
```

### Troubleshooting Quick Reference
```bash
# Compilation Issues
mvn compile -q                    # Must pass for phase progression
mvn dependency:tree               # Check for version conflicts
.scripts/enhanced_validation.sh validate_xsd_integration_readiness ${ObjectType}

# Service Registration Validation
find . -name "META-INF" -type d   # Locate service files
ls -la src/main/resources/META-INF/services/
grep -r "${ObjectType}" src/main/resources/META-INF/services/

# Database Connection Testing
SNOWFLAKE_URL="..." mvn test -Dtest=SnowflakeParameterValidationTest -q
echo "SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S LIMIT 1" | database_console

# Test Coverage Validation
.scripts/enhanced_validation.sh validate_comprehensive_test_coverage ${ObjectType}
grep -c "@Test" src/test/java/**/*${ObjectType}*Test.java
grep -c "=== .* TESTS ===" src/test/java/**/*${ObjectType}*Test.java

# XSD Schema Validation
xmllint --schema src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd sample_changelog.xml
mvn clean compile -q             # Verify XSD compiles with project

# Complete Workflow Status Check
.scripts/tdd_workflow.sh status
ls -la .checkpoints/              # See completed phases
ls -la .process_state/            # See current workflow state
```

## UNIVERSAL IMPLEMENTATION TEMPLATES

### Universal Object Model Template (Test-Driven)
```java
package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import java.util.Objects;

/**
 * Represents a Snowflake ${ObjectType} database object.
 * Generated via enforced TDD micro-cycles.
 */
public class ${ObjectType} extends AbstractDatabaseObject {
    
    private String name;
    private Schema schema;
    // Additional properties added via TDD micro-cycles
    
    public ${ObjectType}() {
        super();
    }
    
    public ${ObjectType}(String name) {
        this();
        setName(name);
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public ${ObjectType} setName(String name) {
        this.name = name;
        return this;
    }
    
    @Override
    public Schema getSchema() {
        return schema;
    }
    
    @Override
    public ${ObjectType} setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }
    
    // Property methods added via TDD micro-cycles
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ${ObjectType} that = (${ObjectType}) obj;
        return Objects.equals(name, that.name) &&
               Objects.equals(schema, that.schema);
               // Additional property comparisons added via TDD
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, schema);
        // Additional properties added via TDD micro-cycles
    }
    
    @Override
    public String toString() {
        return "${ObjectType}{" +
               "name='" + name + '\'' +
               ", schema=" + schema +
               // Additional properties added via TDD micro-cycles
               '}';
    }
}
```

### Service Registration Pattern (Enforced)
```yaml
SERVICE_REGISTRATION_REQUIREMENTS:
  REQUIRED_FILES:
    - "META-INF/services/liquibase.structure.DatabaseObject"
    - "META-INF/services/liquibase.snapshot.SnapshotGenerator"  
    - "META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
    
  ALPHABETICAL_ORDERING: "Insert entries in alphabetical order for consistency"
  VALIDATION_COMMAND: "validate_service_registration(${ObjectType})"
  ENFORCEMENT: "Hard block until all services properly registered"
  
EXAMPLE_ENTRIES:
  DatabaseObject: "liquibase.database.object.${ObjectType}"
  SnapshotGenerator: "liquibase.ext.snowflake.snapshot.${ObjectType}SnapshotGenerator"
  Comparator: "liquibase.ext.snowflake.diff.compare.${ObjectType}Comparator"
```

## AUTOMATION PROGRAM REFERENCES

### Available AIPL Programs for Enhanced Automation
```yaml
AIPL_PROGRAMS:
  INCOMPLETE_IMPLEMENTATION_DETECTION:
    FILE: "aipl_programs/incomplete-implementation-detection.yaml"
    PURPOSE: "Systematic detection and template generation for missing components"
    TRIGGER: "When detecting partial implementations"
    INTEGRATION: "Called automatically by enforcement system"
    
  SYSTEMATIC_IMPLEMENTATION_DEBUGGING:
    FILE: "aipl_programs/systematic-implementation-debugging.yaml"
    PURPOSE: "Layer-by-layer automated diagnosis and isolation"
    TRIGGER: "When troubleshooting compilation or runtime errors"
    INTEGRATION: "Referenced in error pattern solutions"
    
  SNAPSHOT_DIFF_REGISTRATION_VALIDATION:
    FILE: "aipl_programs/snapshot-diff-registration-validation.yaml"
    PURPOSE: "Automated service registration validation and correction"
    TRIGGER: "Service registration validation failures"
    INTEGRATION: "Integrated with validate_service_registration()"
    
  SNAPSHOT_DIFF_INTEGRATION_TESTING:
    FILE: "aipl_programs/snapshot-diff-integration-testing.yaml"
    PURPOSE: "Comprehensive integration test automation"
    TRIGGER: "Integration phase validation"
    INTEGRATION: "Final validation workflow"
```

**ULTIMATE SUCCESS**: This guide represents the breakthrough combination of **proven technical patterns** with **external behavioral enforcement**, eliminating reliance on self-discipline while maintaining the comprehensive coverage of the original legacy guide.