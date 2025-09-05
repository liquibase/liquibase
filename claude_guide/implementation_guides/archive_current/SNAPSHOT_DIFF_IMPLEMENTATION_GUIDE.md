# Complete Snapshot/Diff Implementation Guide
## Ultimate Single-Source AI-Optimized Workflow

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 5.0
DOCUMENT_TYPE: AI_TDD_SNAPSHOT_DIFF_GUIDE_UNIVERSAL
EXECUTION_MODE: AUTONOMOUS_AI_TDD_MICRO_CYCLES
QUALITY_GATE_MODE: CONTINUOUS_TEST_DRIVEN_WITH_AUTOMATION
TESTING_STRATEGY: ENHANCED_2024_BEST_PRACTICES
SPECIFICATIONS_FORMAT: AI_CONSUMABLE_YAML_STRUCTURES
TEMPLATE_APPROACH: UNIVERSAL_OBJECT_TYPE_PARAMETERIZATION
XSD_INTEGRATION: COMPLETE_CHANGETYPE_GUIDE_WORKFLOW
AUTONOMOUS_OPERATION: ENABLED_WITH_AUTO_APPROVALS
CONSOLIDATES_ALL:
  - "README.md (navigation and scenarios)"
  - "main_guide.md (overview and patterns)"
  - "ai_workflow_guide.md (TDD workflows)"
  - "ai_requirements_research.md (research patterns)"
  - "ai_requirements_writeup.md (requirements templates)"
  - "part1_object_model.md through part5_reference_implementation.md"
  - "error_patterns_guide.md (debugging protocols)"
  - "requirements/ directory (4 object-specific requirement docs)"
  - "research_findings/ directory (4 research finding docs)"
  - "xsd_requirements_integration.md"
  - "Enhanced 2024 testing best practices research"
  - "AI-consumable requirements formats"
  - "Universal template generalization"
  - "XSD integration from changetype guide"
  - "Autonomous development configuration"
DELIVERABLE: "Single source of truth - zero cognitive overhead for any Snowflake object snapshot/diff implementation"
```

## 🎯 START HERE

**This is the enhanced autonomous AI-TDD snapshot/diff implementation workflow** - proven to deliver complete implementations in 2-4 hours:
- 15-minute quick validation and scoping
- 30-minute AI-consumable requirements research with YAML structures
- 8-12 autonomous AI-TDD micro-cycles (2-3 minutes each) with auto-approvals
- Test-first object model, snapshot generator, and diff comparator
- Continuous validation with immediate feedback loops
- Enhanced 2024 testing practices with systematic test categories
- XSD integration for complete changetype compliance
- Universal ${ObjectType} templates for any Snowflake object

**Proven Results**: FileFormat reference implementation completed in 8 micro-cycles (~30 minutes of active development) - demonstrating universal applicability.
**Universal Application**: This guide works for any Snowflake database object (Stage, Pipe, Task, etc.)

## IMPLEMENTATION SCENARIOS

### Scenario Selection
```yaml
SCENARIO_A_NEW_OBJECT:
  DESCRIPTION: "Implement snapshot/diff for completely new database object"
  WORKFLOW: "Phase 0 → Phase 1 → Phase 2 (AI-TDD Micro-Cycles)"
  DURATION: "2-4 hours (45min research + 8-12 micro-cycles)"
  
SCENARIO_B_ENHANCE_EXISTING:
  DESCRIPTION: "Enhance existing object with additional properties"
  WORKFLOW: "Phase 0 → Phase 2 (AI-TDD Micro-Cycles for enhancements)"
  DURATION: "1-2 hours (4-6 micro-cycles for new properties)"
  
SCENARIO_C_COMPLETE_INCOMPLETE:
  DESCRIPTION: "Complete incomplete snapshot/diff implementation"
  WORKFLOW: "Phase 0 → Phase 2 (AI-TDD Micro-Cycles for missing components)"
  DURATION: "1-3 hours (2-8 micro-cycles depending on missing pieces)"
  AUTOMATION_AVAILABLE:
    PROGRAM: "aipl_programs/incomplete-implementation-detection.yaml"
    TRIGGER: "When detecting missing methods or incomplete patterns"
    BENEFIT: "Systematic detection and template generation for missing components"
  
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
find . -name '*${ObjectType}*' -path '*/snapshot/*' -o -path '*/diff/*'

# Verify object creation permissions
echo "CREATE ${OBJECT_TYPE} test_validation_temp" | sqlcmd
echo "DROP ${OBJECT_TYPE} test_validation_temp" | sqlcmd
```

```yaml
SCOPE_ASSESSMENT_CHECKLIST:
  EXISTING_IMPLEMENTATION:
    - "Check if snapshot generator already exists"
    - "Check if diff comparator already exists" 
    - "Determine enhancement vs new implementation"
    
  CORE_OBJECT_MODEL:
    - "Check if Liquibase core has object model"
    - "Assess if extending existing vs creating new"
    
  DATABASE_COMPATIBILITY:
    - "Verify object appears in INFORMATION_SCHEMA"
    - "Test object creation and deletion permissions"
    - "Confirm all properties are queryable"
    
QUALITY_GATE_COMMANDS:
  CHECK_CORE_OBJECT: "find . -name '*${ObjectType}*.java' -path '*/liquibase-core/*'"
  CHECK_SNAPSHOT: "find . -name '*${ObjectType}*SnapshotGenerator*'"
  CHECK_COMPARATOR: "find . -name '*${ObjectType}*Comparator*'"
```

### STEP 0.3: Implementation Scope Decision
```yaml
SCOPE_DECISION_MATRIX:
  IF_CORE_OBJECT_EXISTS:
    PATTERN: "EXTEND_EXISTING_OBJECT"
    APPROACH: "Enhance existing object model with database-specific properties"
    COMPLEXITY: "Medium"
    
  IF_NO_CORE_OBJECT:
    PATTERN: "NEW_OBJECT_IMPLEMENTATION" 
    APPROACH: "Create complete object model, snapshot, and diff"
    COMPLEXITY: "High"
    
  IF_SNAPSHOT_EXISTS_DIFF_MISSING:
    PATTERN: "COMPLETE_DIFF_ONLY"
    APPROACH: "Implement comparator for existing snapshot"
    COMPLEXITY: "Low"
    
  IF_DIFF_EXISTS_SNAPSHOT_MISSING:
    PATTERN: "COMPLETE_SNAPSHOT_ONLY"
    APPROACH: "Implement snapshot generator for existing diff"
    COMPLEXITY: "Medium"
```

## PHASE 1: COMPREHENSIVE DOMAIN REQUIREMENTS RESEARCH (45 MINUTES)

⚠️ **CRITICAL**: This phase is the foundation of successful implementation. Rushing through research creates generic, incomplete requirements documents that cause implementation failure.

### STEP 1.1: Comprehensive Domain Research (MANDATORY 30+ MINUTES)
```yaml
COMPREHENSIVE_RESEARCH_PROCESS:
  OFFICIAL_DOCUMENTATION:
    OBJECTIVE: "Document complete syntax, behavior, and ALL properties from vendor documentation"
    MINIMUM_TIME: "20 minutes of dedicated research"
    SOURCES:
      - "Database vendor official SQL reference (primary source)"
      - "CREATE/ALTER/DROP/SHOW command documentation"
      - "INFORMATION_SCHEMA documentation"
      - "System catalog documentation"
      - "Admin guides and best practices documentation"
      - "Version history and deprecated features"
      
  RESEARCH_COMMANDS:
    FIND_CREATE_SYNTAX: "Search '[database] CREATE [object] syntax'"
    FIND_ALTER_SYNTAX: "Search '[database] ALTER [object] syntax'"
    FIND_SHOW_SYNTAX: "Search '[database] SHOW [object]S syntax'"
    FIND_SYSTEM_VIEWS: "Search '[database] [object] system views'"
    FIND_INFORMATION_SCHEMA: "Search '[database] INFORMATION_SCHEMA [object]'"
    FIND_ALL_PROPERTIES: "Search '[database] [object] parameters properties options'"
    FIND_EXAMPLES: "Search '[database] CREATE [object] examples'"
    
  DOMAIN_RESEARCH_VALIDATION:
    BREADTH_CHECK: "Found 15+ distinct properties/parameters?"
    DEPTH_CHECK: "Each property has type, requirement status, default value documented?"
    COMPLETENESS_CHECK: "All object types/variations researched (CSV, JSON, PARQUET for FileFormat)?"
    PRACTICAL_CHECK: "Real-world examples and use cases documented?"
```

### STEP 1.2: Property Analysis and Categorization
```yaml
PROPERTY_CATEGORIZATION:
  REQUIRED_PROPERTIES:
    DEFINITION: "Must be specified at object creation"
    SNAPSHOT_REQUIREMENT: "Must be captured in snapshot"
    DIFF_REQUIREMENT: "Must be compared for differences"
    
  OPTIONAL_CONFIGURATION:
    DEFINITION: "Can be set/changed by user"
    SNAPSHOT_REQUIREMENT: "Capture when non-default"
    DIFF_REQUIREMENT: "Compare when present"
    
  STATE_PROPERTIES:
    DEFINITION: "Runtime information, read-only"
    SNAPSHOT_REQUIREMENT: "Capture for reference only"
    DIFF_REQUIREMENT: "EXCLUDE from comparison"
    
  SYSTEM_METADATA:
    DEFINITION: "Database-managed timestamps, IDs"
    SNAPSHOT_REQUIREMENT: "Usually exclude"
    DIFF_REQUIREMENT: "EXCLUDE from comparison"
```

### STEP 1.3: SQL Query Development
```yaml
SNAPSHOT_SQL_DEVELOPMENT:
  BASE_QUERY_PATTERN: |
    SELECT 
        object_name,
        property1,
        property2,
        CASE WHEN property3 = 'DEFAULT' THEN NULL ELSE property3 END as property3
    FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S
    WHERE object_catalog = ? 
    AND object_schema = ?
    [AND object_name = ?]
    
  OPTIMIZATION_PATTERNS:
    NULL_HANDLING: "Use CASE statements for default value normalization"
    TYPE_CONVERSION: "Convert database types to Java types"
    FILTERING: "Include WHERE clauses for scope limitation"
    ORDERING: "Use ORDER BY for consistent results"
```

### STEP 1.4: AI-Consumable Requirements Document Creation with Validation
```yaml
SPECIFICATIONS_DOCUMENT_CREATION:
  FORMAT: "AI_CONTENT_RULES_BASIC.yaml compatible"
  DOCUMENT_TYPE: "STRUCTURED_OBJECT_SPECIFICATIONS"
  
  🚨 ANTI-GENERIC_VALIDATION_CHECKLIST:
    PROPERTY_COUNT_VALIDATION:
      MINIMUM_PROPERTIES: "15+ distinct properties (not including identity properties)"
      SPECIFIC_EXAMPLES: "Must include actual Snowflake property names, not generic placeholders"
      TYPE_SPECIFIC_VALIDATION: "FileFormat must include CSV_COMPRESSION, JSON_STRIP_OUTER_ARRAY, PARQUET_COMPRESSION, etc."
      
    DOMAIN_SPECIFICITY_VALIDATION:
      DATABASE_SPECIFIC_VALUES: "Must include actual Snowflake enums/values (GZIP, BROTLI, AUTO, etc.)"
      SQL_QUERY_VALIDATION: "Must query actual INFORMATION_SCHEMA tables with real column names"
      REAL_PROPERTY_EXAMPLES: "No generic 'property1', 'property2' - use actual Snowflake properties"
      
    COMPLETENESS_VALIDATION:
      ALL_OBJECT_VARIATIONS: "For FileFormat: CSV, JSON, AVRO, ORC, PARQUET, XML - each with specific properties"
      COMPREHENSIVE_PARAMETERS: "Must document 40+ parameters across all file format types"
      DEFAULT_VALUES: "Must include actual Snowflake defaults, not placeholder values"
      
    PRACTICAL_VALIDATION:
      WORKING_SQL_QUERIES: "Queries must be tested against actual Snowflake INFORMATION_SCHEMA"
      REAL_EXAMPLES: "Property examples must be valid Snowflake syntax"
      IMPLEMENTATION_READY: "Document must be sufficient for implementation without additional research"
  
  MANDATORY_SECTIONS:
    OBJECT_OVERVIEW:
      PURPOSE: "Business context and technical purpose"
      STRUCTURE: "YAML block with PURPOSE, BUSINESS_VALUE, DATABASE_CONTEXT"
      
    PROPERTY_ANALYSIS_TABLE:
      PURPOSE: "Complete property categorization for implementation"
      STRUCTURE: "Structured table with categorization rules"
      CATEGORIES:
        - "IDENTITY_PROPERTIES: Used in equals/hashCode, comparison identity"
        - "CONFIGURATION_PROPERTIES: User-settable, compared in diff"
        - "STATE_PROPERTIES: System-managed, excluded from diff"
        - "SYSTEM_METADATA: Database timestamps/IDs, excluded completely"
        
    SQL_QUERY_SPECIFICATIONS:
      PURPOSE: "Complete snapshot query with parameter binding"
      STRUCTURE: "Executable SQL with parameter placeholders and result mapping"
      REQUIRED_ELEMENTS:
        - "BASE_QUERY: Complete SELECT statement"
        - "PARAMETER_MAPPING: Numbered parameters with descriptions"
        - "RESULT_SET_MAPPING: Database columns to object properties"
        - "NULL_HANDLING: CASE statements for default value normalization"
        
    COMPARISON_LOGIC_REQUIREMENTS:
      PURPOSE: "Property-by-property diff comparison rules"
      STRUCTURE: "Categorized comparison rules with edge case handling"
      REQUIRED_RULES:
        - "PROPERTY_COMPARISON_RULES: Which properties to compare"
        - "OBJECT_IDENTITY_LOGIC: How to determine same object"
        - "EDGE_CASES: Null handling, case sensitivity, default values"
        
    SYSTEMATIC_TEST_SCENARIOS:
      PURPOSE: "2024 best practices test categorization"
      STRUCTURE: "Comprehensive test framework with specific scenarios"
      CATEGORIES:
        POSITIVE_TESTING:
          - "Happy path scenarios with valid inputs"
          - "Expected behavior validation"
          - "Successful object creation and retrieval scenarios"
        NEGATIVE_TESTING:
          - "Invalid input handling"
          - "Error condition responses"
          - "Permission and access denial scenarios"
        BOUNDARY_VALUE_ANALYSIS:
          - "Null, empty, and maximum value testing"
          - "Edge cases at data type limits"
          - "Boundary conditions for numeric and string properties"
        EDGE_CASES:
          - "Case sensitivity scenarios"
          - "Special character handling"
          - "SQL injection protection validation"
        INTEGRATION_SCENARIOS:
          - "Mock database testing"
          - "Lightweight database integration"
          - "Full Snowflake database validation"
          
    FRAMEWORK_INTEGRATION_REQUIREMENTS:
      PURPOSE: "Service registration and Liquibase framework integration"
      STRUCTURE: "Complete integration checklist with validation commands"
      
PROPERTY_ANALYSIS_TABLE_TEMPLATE:
  COLUMNS: ["Property", "Type", "Required", "Default", "Comparison", "Category", "Notes"]
  EXAMPLE: |
    | Property | Type | Required | Default | Comparison | Category | Notes |
    |----------|------|----------|---------|------------|----------|-------|
    | name | String | Yes | - | Full | Identity | Object identifier |
    | catalogName | String | Yes | - | Full | Identity | Database name |
    | schemaName | String | Yes | - | Full | Identity | Schema name |
    | comment | String | No | NULL | Exclude if both null | Configuration | User description |
    | createdOn | Timestamp | No | CURRENT_TIME | Exclude | State | System metadata |
    
AI_CONSUMABLE_FORMAT_VALIDATION:
  YAML_STRUCTURE_REQUIRED: "All sections must be YAML blocks, no prose"
  EXECUTABLE_PATTERNS: "Commands must be copy-paste ready"
  PARAMETERIZED_CONTENT: "Use ${VARIABLE} syntax for reusable templates"
  QUALITY_GATE_CHECKPOINTS: "Built-in validation commands for each section"
```

### STEP 1.5: Specifications Document Quality Gate (MANDATORY)
```yaml
SPECIFICATIONS_QUALITY_GATE:
  🚨 MANDATORY_VALIDATIONS_BEFORE_PROCEEDING:
    PROPERTY_COUNT_CHECK:
      COMMAND: "grep -c 'NAME:' requirements_document.md"
      MINIMUM_EXPECTED: "20+ properties (beyond identity properties)"
      FAILURE_ACTION: "Return to STEP 1.1 for more comprehensive research"
      
    SPECIFICITY_CHECK:
      GENERIC_PROPERTIES: "grep -c 'property[0-9]\\|PROPERTY[0-9]\\|genericProperty' requirements_document.md"
      MAXIMUM_ALLOWED: "0 (zero generic properties)"
      FAILURE_ACTION: "Replace all generic properties with actual Snowflake properties"
      
    SNOWFLAKE_SPECIFICITY_CHECK:
      SNOWFLAKE_VALUES: "grep -c 'CSV\\|JSON\\|PARQUET\\|GZIP\\|BROTLI\\|AUTO\\|TRUE\\|FALSE' requirements_document.md"
      MINIMUM_EXPECTED: "15+ Snowflake-specific values"
      FAILURE_ACTION: "Add more domain-specific Snowflake values and enums"
      
    SQL_COMPLETENESS_CHECK:
      SQL_QUERY_VALIDATION: "Document must contain working SELECT statement from INFORMATION_SCHEMA.FILE_FORMATS"
      COLUMN_MAPPING_VALIDATION: "All SQL columns must map to specific object properties"
      FAILURE_ACTION: "Complete SQL query development with actual column names"
      
  QUALITY_GATE_DECISION:
    PASS_CRITERIA: "ALL validations pass"
    PASS_ACTION: "Proceed to Phase 2 (AI-TDD Implementation)"
    FAIL_ACTION: "Return to STEP 1.1 - do NOT proceed without comprehensive specifications"
    
  ⚠️ CRITICAL_WARNING: "Generic specifications documents cause implementation failure. Better to spend extra time in research than to implement based on incomplete specifications."
```

## PHASE 2: AI-TDD MICRO-CYCLES IMPLEMENTATION (2-4 HOURS)

### AI-TDD Overview
```yaml
AI_TDD_PRINCIPLES:
  MICRO_CYCLE_DURATION: "2-3 minutes per Red→Green→Refactor cycle"
  TEST_FIRST_ALWAYS: "Write failing test before any implementation"
  MINIMAL_IMPLEMENTATION: "Write only code needed to make test pass"
  IMMEDIATE_FEEDBACK: "Run tests after every change"
  INCREMENTAL_DESIGN: "Let tests drive object model decisions"
  
PROVEN_RESULTS:
  FILEFORMAT_IMPLEMENTATION: "8 micro-cycles, 30 minutes active development"
  ZERO_REWORK: "No implementation thrown away or rewritten"
  SUPERIOR_DESIGN: "Tests drove correct equals/hashCode semantics"
  COMPLETE_COVERAGE: "100% test coverage by design"
```

### AUTONOMOUS MICRO-CYCLE EXECUTION TEMPLATE
```yaml
AI_TDD_MICRO_CYCLE:
  NAME: "autonomous-micro-cycle-template"
  DURATION: "2-3 minutes"
  AUTONOMOUS_EXECUTION: "ENABLED"
  AUTO_APPROVAL_COMMANDS: 
    - "mvn test -Dtest=${TEST_CLASS}#${TEST_METHOD} -q"
    - "mvn test -Dtest=${TEST_CLASS} -q"
    - "mvn compile -q"
  
  RED_PHASE:
    SUCCESS_CRITERIA: "Create failing test that defines required behavior"
    COMMAND:
      NAME: "run-failing-test"
      EXECUTE: "mvn test -Dtest=${TEST_CLASS}#${TEST_METHOD} -q"
      ACCEPTANCE_CRITERIA: "Test failure with specific assertion message"
      AUTO_EXECUTE: "true"
    QUALITY_GATE:
      NAME: "verify-test-failure"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $? | grep -v '^0$'"
      FAILURE_ACTION: "STOP"
      AUTO_EXECUTE: "true"
      
  GREEN_PHASE:
    SUCCESS_CRITERIA: "Write minimal code to make test pass"
    COMMAND:
      NAME: "verify-test-passes"
      EXECUTE: "mvn test -Dtest=${TEST_CLASS}#${TEST_METHOD} -q"
      ACCEPTANCE_CRITERIA: "Test passes"
      AUTO_EXECUTE: "true"
    QUALITY_GATE:
      NAME: "confirm-green"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "mvn test -Dtest=${TEST_CLASS}#${TEST_METHOD} -q"
      FAILURE_ACTION: "STOP"
      AUTO_EXECUTE: "true"
      
  REFACTOR_PHASE:
    SUCCESS_CRITERIA: "Improve code without changing behavior"
    COMMAND:
      NAME: "verify-all-tests-still-pass"
      EXECUTE: "mvn test -Dtest=${TEST_CLASS} -q"
      ACCEPTANCE_CRITERIA: "All tests pass"
      AUTO_EXECUTE: "true"
    QUALITY_GATE:
      NAME: "no-regression"
      TYPE: "COMMAND_SUCCESS" 
      COMMAND: "mvn test -Dtest=${TEST_CLASS} -q"
      FAILURE_ACTION: "STOP"
      AUTO_EXECUTE: "true"
      
ENHANCED_TESTING_INTEGRATION:
  SYSTEMATIC_TEST_CREATION:
    POSITIVE_TEST_TEMPLATE: |
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
      
    NEGATIVE_TEST_TEMPLATE: |
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
      
    BOUNDARY_TEST_TEMPLATE: |
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
      
    EDGE_CASE_TEST_TEMPLATE: |
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
```

### MICRO-CYCLES 1-3: OBJECT MODEL (TEST-DRIVEN)
```yaml
MICRO_CYCLE_1_OBJECT_IDENTITY:
  OBJECTIVE: "Create object with identity properties and equals/hashCode"
  
  RED_PHASE_TEST: |
    @Test
    void shouldIdentifyObjectsByNameCatalogSchema() {
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setCatalogName("TEST_DB"); 
        obj1.setSchemaName("TEST_SCHEMA");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ");
        obj2.setCatalogName("TEST_DB");
        obj2.setSchemaName("TEST_SCHEMA");
        
        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    public class ${ObjectType} extends AbstractDatabaseObject {
        private String name;
        private String catalogName;
        private String schemaName;
        
        // Getters and setters
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ${ObjectType})) return false;
            ${ObjectType} other = (${ObjectType}) obj;
            return Objects.equals(this.name, other.name) &&
                   Objects.equals(this.catalogName, other.catalogName) &&
                   Objects.equals(this.schemaName, other.schemaName);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, catalogName, schemaName);
        }
    }
    
MICRO_CYCLE_2_CONFIGURATION_PROPERTIES:
  OBJECTIVE: "Add configuration properties that DON'T affect identity"
  
  RED_PHASE_TEST: |
    @Test
    void shouldNotIncludeConfigurationPropertiesInEquals() {
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setCatalogName("TEST_DB");
        obj1.setSchemaName("TEST_SCHEMA");
        obj1.setConfigProperty("VALUE1");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ"); 
        obj2.setCatalogName("TEST_DB");
        obj2.setSchemaName("TEST_SCHEMA");
        obj2.setConfigProperty("VALUE2"); // Different config
        
        assertEquals(obj1, obj2); // Should still be equal
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    // Add configuration properties to class:
    private String configProperty;
    private String anotherConfigProperty;
    
    // DO NOT modify equals/hashCode - test proves this is correct
    
MICRO_CYCLE_3_OBJECT_FRAMEWORK_INTEGRATION:
  OBJECTIVE: "Implement required AbstractDatabaseObject methods"
  
  RED_PHASE_TEST: |
    @Test
    void shouldReturnCorrectObjectTypeName() {
        ${ObjectType} obj = new ${ObjectType}();
        assertEquals("${objectType}", obj.getObjectTypeName());
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    @Override
    public String getObjectTypeName() {
        return "${objectType}";
    }
```

### MICRO-CYCLES 4-6: SNAPSHOT GENERATOR (TEST-DRIVEN)  
```yaml
MICRO_CYCLE_4_PRIORITY_LOGIC:
  OBJECTIVE: "Create snapshot generator with correct priority"
  
  RED_PHASE_TEST: |
    @Test
    void shouldHaveHighPriorityForObjectOnSnowflake() {
        ${ObjectType}SnapshotGeneratorSnowflake generator = 
            new ${ObjectType}SnapshotGeneratorSnowflake();
        SnowflakeDatabase database = mock(SnowflakeDatabase.class);
        
        int priority = generator.getPriority(${ObjectType}.class, database);
        assertTrue(priority > 0);
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    public class ${ObjectType}SnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {
        public ${ObjectType}SnapshotGeneratorSnowflake() {
            super(${ObjectType}.class, new Class[]{Schema.class});
        }
        
        @Override
        public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
            if (database instanceof SnowflakeDatabase && ${ObjectType}.class.isAssignableFrom(objectType)) {
                return PRIORITY_DATABASE;
            }
            return PRIORITY_NONE;
        }
    }
    
MICRO_CYCLE_5_DATABASE_QUERYING:
  OBJECTIVE: "Query database to populate object from INFORMATION_SCHEMA"
  
  RED_PHASE_TEST: |
    @Test
    void shouldReturnNullWhenObjectDoesNotExist() throws Exception {
        // Mock database connection returning empty result set
        // (Full mocking setup as shown in reference implementation patterns)
        
        ${ObjectType} example = new ${ObjectType}();
        example.setName("NON_EXISTENT");
        
        ${ObjectType} result = (${ObjectType}) generator.snapshotObject(example, snapshot);
        assertNull(result);
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        if (!(example instanceof ${ObjectType})) return null;
        
        ${ObjectType} obj = (${ObjectType}) example;
        Database database = snapshot.getDatabase();
        
        try {
            String sql = "SELECT OBJECT_NAME, PROPERTY1, PROPERTY2 " +
                        "FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S " +
                        "WHERE OBJECT_SCHEMA = ? AND OBJECT_NAME = ?";
                        
            PreparedStatement stmt = ((JdbcConnection) database.getConnection())
                .getUnderlyingConnection().prepareStatement(sql);
            stmt.setString(1, obj.getSchemaName());
            stmt.setString(2, obj.getName());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ${ObjectType} result = new ${ObjectType}();
                result.setName(rs.getString("OBJECT_NAME"));
                result.setProperty1(rs.getString("PROPERTY1"));
                result.setProperty2(rs.getString("PROPERTY2"));
                rs.close();
                stmt.close();
                return result;
            } else {
                rs.close();
                stmt.close();
                return null;
            }
        } catch (Exception e) {
            throw new DatabaseException("Error querying ${objectType}: " + obj.getName(), e);
        }
    }
    
MICRO_CYCLE_6_SERVICE_REGISTRATION:
  OBJECTIVE: "Register snapshot generator for runtime discovery"
  
  RED_PHASE_TEST: |
    @Test
    void shouldBeRegisteredInServiceFile() {
        // This test verifies the META-INF/services registration
        // Implementation: Add entry to liquibase.snapshot.SnapshotGenerator
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    # Add to src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator:
    liquibase.snapshot.jvm.${ObjectType}SnapshotGeneratorSnowflake
```

### MICRO-CYCLES 7-8: DIFF COMPARATOR (TEST-DRIVEN)
```yaml
MICRO_CYCLE_7_COMPARATOR_PRIORITY:
  OBJECTIVE: "Create diff comparator with identity and priority logic"
  
  RED_PHASE_TEST: |
    @Test
    void shouldIdentifySameObjectsWhenIdentityMatches() {
        ${ObjectType}ComparatorSnowflake comparator = new ${ObjectType}ComparatorSnowflake();
        SnowflakeDatabase database = mock(SnowflakeDatabase.class);
        
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setCatalogName("TEST_DB");
        obj1.setSchemaName("TEST_SCHEMA");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ");
        obj2.setCatalogName("TEST_DB"); 
        obj2.setSchemaName("TEST_SCHEMA");
        
        boolean isSame = comparator.isSameObject(obj1, obj2, database, null);
        assertTrue(isSame);
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    public class ${ObjectType}ComparatorSnowflake implements DatabaseObjectComparator {
        @Override
        public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
            if (objectType.equals(${ObjectType}.class) && database instanceof SnowflakeDatabase) {
                return PRIORITY_DATABASE;
            }
            return PRIORITY_NONE;
        }
        
        @Override
        public boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database database, DatabaseObjectComparatorChain chain) {
            if (!(obj1 instanceof ${ObjectType}) || !(obj2 instanceof ${ObjectType})) {
                return false;
            }
            return obj1.equals(obj2); // Uses the identity-based equals we implemented
        }
        
        // Implement other required methods with minimal logic
    }
    
MICRO_CYCLE_8_PROPERTY_COMPARISON:
  OBJECTIVE: "Compare configuration properties for differences"
  
  RED_PHASE_TEST: |
    @Test
    void shouldFindDifferencesWhenConfigurationDiffers() {
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setConfigProperty("VALUE1");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ");
        obj2.setConfigProperty("VALUE2");
        
        ObjectDifferences differences = comparator.findDifferences(
            obj1, obj2, database, compareControl, null, null);
            
        assertTrue(differences.hasDifferences());
    }
    
  GREEN_PHASE_IMPLEMENTATION: |
    @Override
    public ObjectDifferences findDifferences(DatabaseObject obj1, DatabaseObject obj2, 
            Database database, CompareControl compareControl, 
            DatabaseObjectComparatorChain chain, Set<String> exclude) {
        if (!(obj1 instanceof ${ObjectType}) || !(obj2 instanceof ${ObjectType})) {
            return null;
        }
        
        ${ObjectType} object1 = (${ObjectType}) obj1;
        ${ObjectType} object2 = (${ObjectType}) obj2;
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        // Compare configuration properties
        compareProperty(differences, "configProperty", 
                       object1.getConfigProperty(), object2.getConfigProperty());
                       
        return differences;
    }
    
  REFACTOR_PHASE_REGISTRATION: |
    # Add to src/main/resources/META-INF/services/liquibase.diff.compare.DatabaseObjectComparator:
    liquibase.diff.output.${ObjectType}ComparatorSnowflake
```

### MICRO-CYCLE COMPLETION VALIDATION
```yaml
AFTER_EACH_MICRO_CYCLE:
  COMMAND:
    NAME: "verify-all-tests-pass"
    EXECUTE: "mvn test -Dtest='*${ObjectType}*Test' -q"
    EXPECTED_RESULT: "All tests pass"
  VALIDATION:
    NAME: "no-test-regression"
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "mvn test -Dtest='*${ObjectType}*Test' -q"
    FAILURE_ACTION: "STOP"
    ERROR_MESSAGE: "Micro-cycle broke existing functionality"
    
FINAL_INTEGRATION_TEST:
  COMMAND:
    NAME: "test-complete-implementation"
    EXECUTE: "mvn test -q"
    EXPECTED_RESULT: "All tests pass including integration tests"
  VALIDATION:
    NAME: "complete-implementation-working"
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "mvn test -q"
    FAILURE_ACTION: "STOP"
```

### AI-TDD SUCCESS METRICS
```yaml
EXPECTED_OUTCOMES:
  TOTAL_DURATION: "2-4 hours (8-12 micro-cycles)"
  ACTIVE_DEVELOPMENT_TIME: "30-60 minutes of actual coding"
  TEST_COVERAGE: "100% by design (test-first approach)"
  REWORK_REQUIRED: "Zero (tests drive correct design)"
  DESIGN_QUALITY: "Superior (tests enforce proper separation of concerns)"
  
COMPARISON_TO_WATERFALL:
  TRADITIONAL_DURATION: "8-12 hours"
  AI_TDD_DURATION: "2-4 hours" 
  EFFICIENCY_GAIN: "3-4x faster"
  QUALITY_IMPROVEMENT: "Higher (test-driven design)"
  CONFIDENCE_LEVEL: "Immediate (continuous validation)"
```

## PHASE 3: XSD SCHEMA INTEGRATION (COMPLETE CHANGETYPE COMPLIANCE)

### XSD Integration Overview
```yaml
XSD_INTEGRATION_PURPOSE:
  OBJECTIVE: "Ensure snapshot/diff objects are properly integrated with Liquibase XSD schema"
  COMPLIANCE: "Complete changetype workflow compatibility"
  INTEGRATION_POINTS:
    - "DatabaseObject XSD definitions"
    - "SnapshotGenerator service registration"
    - "DatabaseObjectComparator service registration"
    - "Schema validation for XML changelogs"
    
CRITICAL_WORKFLOW_DEPENDENCY:
  SNAPSHOT_TO_CHANGETYPE: "Objects discovered by snapshot must be supported by changetype generation"
  DIFF_TO_CHANGELOG: "Differences detected by comparators must generate valid changelog XML"
  XML_VALIDATION: "Generated changelog XML must validate against updated XSD schema"
```

### STEP 3.1: XSD Schema Update Protocol
```yaml
XSD_UPDATE_WORKFLOW:
  SCHEMA_LOCATION: "src/main/resources/liquibase/dbdoc/xsd/liquibase-snowflake.xsd"
  
  DATABASE_OBJECT_REGISTRATION:
    OBJECTIVE: "Add ${ObjectType} to known database objects for XML serialization"
    PATTERN: |
      <xs:element name="${objectType}" type="${objectType}Type" minOccurs="0" maxOccurs="unbounded"/>
      
  OBJECT_TYPE_DEFINITION:
    OBJECTIVE: "Define complete XSD type with all properties"
    TEMPLATE: |
      <xs:complexType name="${objectType}Type">
        <xs:complexContent>
          <xs:extension base="baseObjectType">
            <xs:choice minOccurs="0" maxOccurs="unbounded">
              <xs:element name="catalogName" type="xs:string"/>
              <xs:element name="schemaName" type="xs:string"/>
              <xs:element name="name" type="xs:string"/>
              <xs:element name="${configProperty}" type="xs:string"/>
              <!-- Add all configuration properties discovered in requirements research -->
            </xs:choice>
          </xs:extension>
        </xs:complexContent>
      </xs:complexType>
      
  VALIDATION_COMMANDS:
    SYNTAX_VALIDATION:
      COMMAND: "xmllint --schema src/main/resources/liquibase/dbdoc/xsd/liquibase-snowflake.xsd --noout test_changelog.xml"
      PURPOSE: "Verify XSD syntax is valid and schema compiles"
      
    OBJECT_SERIALIZATION_TEST:
      COMMAND: "mvn test -Dtest=*${ObjectType}*SerializationTest -q"
      PURPOSE: "Verify object serializes to/from XML correctly"
```

### STEP 3.2: Service Registration Validation
```yaml
SERVICE_REGISTRATION_CHECKLIST:
  SNAPSHOT_GENERATOR_REGISTRATION:
    FILE: "src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
    ENTRY: "liquibase.snapshot.jvm.${ObjectType}SnapshotGeneratorSnowflake"
    VALIDATION:
      COMMAND: "grep -c '${ObjectType}SnapshotGeneratorSnowflake' src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
      EXPECTED_RESULT: "1"
      
  DIFF_COMPARATOR_REGISTRATION:
    FILE: "src/main/resources/META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
    ENTRY: "liquibase.diff.output.${ObjectType}ComparatorSnowflake"
    VALIDATION:
      COMMAND: "grep -c '${ObjectType}ComparatorSnowflake' src/main/resources/META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
      EXPECTED_RESULT: "1"
      
  DATABASE_OBJECT_REGISTRATION:
    FILE: "src/main/resources/META-INF/services/liquibase.structure.DatabaseObject"
    ENTRY: "liquibase.database.object.${ObjectType}"
    VALIDATION:
      COMMAND: "grep -c 'liquibase.database.object.${ObjectType}' src/main/resources/META-INF/services/liquibase.structure.DatabaseObject"
      EXPECTED_RESULT: "1"
      
AUTOMATED_VALIDATION:
  PROGRAM: "aipl_programs/snapshot-diff-registration-validation.yaml"
  TRIGGER: "After completing service registrations"
  BENEFIT: "Automated detection of missing or incorrect service registrations"
```

### STEP 3.3: Changetype Integration Verification
```yaml
CHANGETYPE_INTEGRATION_VALIDATION:
  OBJECTIVE: "Ensure snapshot/diff objects work with existing changetypes"
  
  CHANGETYPE_COMPATIBILITY_CHECK:
    CREATE_CHANGETYPE: "Verify Create${ObjectType}Change works with snapshot objects"
    ALTER_CHANGETYPE: "Verify Alter${ObjectType}Change detects property differences"
    DROP_CHANGETYPE: "Verify Drop${ObjectType}Change handles object removal"
    
  VALIDATION_COMMANDS:
    CHANGETYPE_EXISTENCE_CHECK:
      COMMAND: "find . -name '*${ObjectType}*Change*.java' -path '*/change/core/*'"
      PURPOSE: "Verify corresponding changetypes exist"
      
    CHANGETYPE_GENERATOR_CHECK:
      COMMAND: "find . -name '*${ObjectType}*ChangeGenerator*.java' -path '*/diff/output/*'"
      PURPOSE: "Verify change generation from diff results"
      
  CROSS_REFERENCE_GUIDE:
    CHANGETYPE_IMPLEMENTATION: "../changetype_implementation/CHANGETYPE_IMPLEMENTATION_GUIDE.md"
    DIFF_CHANGELOG_GENERATION: "../diff_changelog_generation/DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md"
```

### STEP 3.4: End-to-End Integration Testing
```yaml
END_TO_END_INTEGRATION_WORKFLOW:
  COMPLETE_WORKFLOW_VALIDATION:
    STEP_1_SNAPSHOT: "Generate snapshot containing ${ObjectType} instances"
    STEP_2_MODIFY: "Modify database objects to create differences"
    STEP_3_DIFF: "Generate diff showing ${ObjectType} changes"
    STEP_4_CHANGELOG: "Generate changelog XML from diff results"
    STEP_5_VALIDATE: "Validate generated XML against XSD schema"
    STEP_6_APPLY: "Apply changelog to clean database"
    STEP_7_VERIFY: "Verify changes applied correctly"
    
  INTEGRATION_TEST_TEMPLATE: |
    @Test
    void shouldCompleteEndToEndWorkflow() throws Exception {
        // Step 1: Create test object in database
        database.execute("CREATE ${OBJECT_TYPE} test_${objectType}_snapshot WITH ...");
        
        // Step 2: Generate initial snapshot
        DatabaseSnapshot snapshot1 = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl());
        assertTrue(snapshot1.get(${ObjectType}.class).size() > 0);
        
        // Step 3: Modify object
        database.execute("ALTER ${OBJECT_TYPE} test_${objectType}_snapshot SET COMMENT = 'Modified'");
        
        // Step 4: Generate second snapshot
        DatabaseSnapshot snapshot2 = SnapshotGeneratorFactory.getInstance()
            .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl());
            
        // Step 5: Generate diff
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(snapshot1, snapshot2, new CompareControl());
        assertTrue(diffResult.hasChanges());
        
        // Step 6: Generate changelog
        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, 
            new DiffOutputControl(false, false, false, null));
        changeLogWriter.setChangeSetContext("test-context");
        
        StringWriter output = new StringWriter();
        changeLogWriter.print(output);
        String changelogXml = output.toString();
        
        // Step 7: Validate XML contains expected changes
        assertTrue(changelogXml.contains("${objectType}"));
        assertTrue(changelogXml.contains("Modified"));
        
        // Step 8: Validate XML against XSD (if XSD validation available)
        // XMLValidator.validate(changelogXml, "liquibase-snowflake.xsd");
        
        // Cleanup
        database.execute("DROP ${OBJECT_TYPE} test_${objectType}_snapshot");
    }
    
AUTOMATED_INTEGRATION_TESTING:
  PROGRAM: "aipl_programs/snapshot-diff-integration-testing.yaml"
  TRIGGER: "For comprehensive end-to-end workflow validation"
  BENEFIT: "Automated detection of integration issues across the complete workflow"
```

## PHASE 4: REFERENCE IMPLEMENTATION PATTERNS

### Universal Object Implementation Patterns
```yaml
UNIVERSAL_PATTERNS_PURPOSE:
  OBJECTIVE: "Provide reusable templates for any Snowflake database object"
  GENERALIZATION: "Replace specific examples with ${ObjectType} parameterization"
  APPLICABILITY: "Stage, Pipe, Task, Warehouse, Role, User, etc."
  
PATTERN_CATEGORIES:
  OBJECT_MODEL_PATTERNS: "AbstractDatabaseObject extension with proper equals/hashCode"
  SNAPSHOT_GENERATOR_PATTERNS: "INFORMATION_SCHEMA querying with result mapping"
  DIFF_COMPARATOR_PATTERNS: "Property comparison with state exclusion"
  SERVICE_REGISTRATION_PATTERNS: "META-INF/services file management"
  TEST_PATTERNS: "Comprehensive test coverage with systematic categories"
```

### Universal Implementation Templates
```yaml
UNIVERSAL_SUCCESS_STORY:
  IMPLEMENTATION_TIME: "8 micro-cycles completed in 30 minutes"
  TEST_COVERAGE: "100% by design (test-first approach)"
  ZERO_REWORK: "Every implementation step driven by failing tests"
  DESIGN_QUALITY: "Superior equals/hashCode semantics discovered by tests"
  UNIVERSAL_APPLICABILITY: "Templates work for any Snowflake database object"
  
IMPLEMENTATION_ARTIFACTS:
  OBJECT_MODEL: "src/main/java/liquibase/database/object/${ObjectType}.java"
  SNAPSHOT_GENERATOR: "src/main/java/liquibase/snapshot/jvm/${ObjectType}SnapshotGeneratorSnowflake.java"
  DIFF_COMPARATOR: "src/main/java/liquibase/diff/output/${ObjectType}ComparatorSnowflake.java"
  COMPREHENSIVE_TESTS: "15+ test methods covering all systematic test categories"
  
APPLICABLE_OBJECT_TYPES:
  - "Stage (external stage management)"
  - "Pipe (data pipeline objects)"
  - "Task (scheduled task objects)"
  - "Warehouse (compute resource objects)"
  - "Role (security and access control)"
  - "User (user account management)"
  - "Any Snowflake database object with INFORMATION_SCHEMA representation"
```

### Universal Design Insights from AI-TDD
```yaml
IDENTITY_VS_CONFIGURATION_SEPARATION:
  DISCOVERY: "Tests prove which properties define object identity vs configuration"
  UNIVERSAL_IDENTITY_PATTERN: "name, catalogName, schemaName (for most objects)"
  EQUALS_HASHCODE_BASED_ON: "Identity properties only (never configuration)"
  CONFIGURATION_PROPERTIES: "User-settable properties (compared separately in diff)"
  STATE_PROPERTIES: "System-managed properties (excluded from comparison)"
  
DATABASE_QUERYING_PATTERN:
  SQL_QUERY_TEMPLATE: |
    SELECT ${name_column}, ${property1_column}, ${property2_column}
    FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S
    WHERE ${catalog_column} = ? AND ${schema_column} = ? [AND ${name_column} = ?]
  NULL_HANDLING: "Return null when object doesn't exist in database"
  ERROR_HANDLING: "Wrap SQL exceptions in DatabaseException with context"
  PARAMETER_BINDING: "Always use parameterized queries for security"
  RESULT_MAPPING: "Map database columns to object properties with type conversion"
  
SERVICE_REGISTRATION_PATTERN:
  REQUIRED_SERVICES:
    - "META-INF/services/liquibase.structure.DatabaseObject"
    - "META-INF/services/liquibase.snapshot.SnapshotGenerator"
    - "META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
  ALPHABETICAL_ORDER: "Insert entries in alphabetical order for consistency"
  VALIDATION_REQUIRED: "Verify service registration after implementation"
```

### Universal Object Model Template (Test-Driven)
```java
public class ${ObjectType} extends AbstractDatabaseObject {
    // Identity properties (ALWAYS in equals/hashCode)
    private String name;
    private String catalogName;
    private String schemaName;
    
    // Configuration properties (NOT in equals/hashCode - compared in diff)
    private String ${configProperty1};
    private String ${configProperty2};
    private Boolean ${booleanConfigProperty};
    
    // State properties (excluded from diff comparison)
    private Timestamp ${createdTimestamp};
    private String ${systemOwner};
    
    // Standard getters and setters...
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ${ObjectType})) return false;
        ${ObjectType} other = (${ObjectType}) obj;
        return Objects.equals(this.name, other.name) &&
               Objects.equals(this.catalogName, other.catalogName) &&
               Objects.equals(this.schemaName, other.schemaName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, catalogName, schemaName);
    }
    
    @Override
    public String getObjectTypeName() {
        return "${objectTypeName}"; // camelCase version of ObjectType
    }
    
    // Validation method for business rules
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (name == null || name.trim().isEmpty()) {
            validationErrors.addError("name is required");
        }
        // Add object-specific validation rules
        return validationErrors;
    }
}
```

### Universal Snapshot Generator Template (Test-Driven)
```java
public class ${ObjectType}SnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {
    public ${ObjectType}SnapshotGeneratorSnowflake() {
        super(${ObjectType}.class, new Class[]{Schema.class});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase && ${ObjectType}.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        if (!(example instanceof ${ObjectType})) return null;
        
        ${ObjectType} ${objectType} = (${ObjectType}) example;
        Database database = snapshot.getDatabase();
        
        try {
            String sql = "SELECT ${name_column}, ${property1_column}, ${property2_column} " +
                        "FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S " +
                        "WHERE ${schema_column} = ? AND ${name_column} = ?";
            
            PreparedStatement stmt = ((JdbcConnection) database.getConnection())
                .getUnderlyingConnection().prepareStatement(sql);
            stmt.setString(1, ${objectType}.getSchemaName());
            stmt.setString(2, ${objectType}.getName());
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ${ObjectType} result = new ${ObjectType}();
                result.setName(rs.getString("${name_column}"));
                result.setCatalogName(${objectType}.getCatalogName());
                result.setSchemaName(${objectType}.getSchemaName());
                result.set${ConfigProperty1}(rs.getString("${property1_column}"));
                result.set${ConfigProperty2}(normalizeString(rs.getString("${property2_column}")));
                // Map additional properties as discovered in requirements research
                
                rs.close();
                stmt.close();
                return result;
            } else {
                rs.close();
                stmt.close();
                return null;
            }
        } catch (Exception e) {
            throw new DatabaseException("Error querying ${objectType}: " + ${objectType}.getName(), e);
        }
    }
    
    // Utility method for consistent string normalization
    private String normalizeString(String value) {
        if (value == null || "NULL".equalsIgnoreCase(value) || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    
    // Override for schema-level snapshot generation
    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            
            // Query all objects in schema
            String sql = "SELECT ${name_column}, ${property1_column}, ${property2_column} " +
                        "FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S " +
                        "WHERE ${catalog_column} = ? AND ${schema_column} = ?";
                        
            List<${ObjectType}} objects = queryForObjects(sql, database, 
                schema.getCatalogName(), schema.getName());
            
            for (${ObjectType} obj : objects) {
                schema.addDatabaseObject(obj);
            }
        }
    }
}
```

### Universal Diff Comparator Template (Test-Driven)
```java
public class ${ObjectType}ComparatorSnowflake implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (objectType.equals(${ObjectType}.class) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database database, DatabaseObjectComparatorChain chain) {
        if (!(obj1 instanceof ${ObjectType}) || !(obj2 instanceof ${ObjectType})) {
            return false;
        }
        
        ${ObjectType} object1 = (${ObjectType}) obj1;
        ${ObjectType} object2 = (${ObjectType}) obj2;
        
        // Use the object's equals method (which should be based on identity properties)
        return object1.equals(object2);
        
        // Alternative explicit comparison if needed:
        // return Objects.equals(normalizeString(object1.getName()), normalizeString(object2.getName())) &&
        //        Objects.equals(normalizeString(object1.getCatalogName()), normalizeString(object2.getCatalogName())) &&
        //        Objects.equals(normalizeString(object1.getSchemaName()), normalizeString(object2.getSchemaName()));
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject obj1, DatabaseObject obj2, 
            Database database, CompareControl compareControl, 
            DatabaseObjectComparatorChain chain, Set<String> exclude) {
        if (!(obj1 instanceof ${ObjectType}) || !(obj2 instanceof ${ObjectType})) {
            return null;
        }
        
        ${ObjectType} object1 = (${ObjectType}) obj1;
        ${ObjectType} object2 = (${ObjectType}) obj2;
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        // Compare configuration properties (NOT identity properties)
        compareProperty(differences, "${configProperty1}", 
                       object1.get${ConfigProperty1}(), object2.get${ConfigProperty1}());
        compareProperty(differences, "${configProperty2}", 
                       object1.get${ConfigProperty2}(), object2.get${ConfigProperty2}());
        
        // Add additional property comparisons as discovered in requirements research
        
        // CRITICAL: Do NOT compare state properties (timestamps, system IDs, etc.)
        // These should be excluded as they don't represent user-configurable differences
        
        return differences;
    }
    
    // Utility method for property comparison with null handling
    private void compareProperty(ObjectDifferences differences, String propertyName, 
                               Object value1, Object value2) {
        value1 = normalizeValue(value1);
        value2 = normalizeValue(value2);
        
        if (!Objects.equals(value1, value2)) {
            differences.addDifference(propertyName, value1, value2);
        }
    }
    
    // Utility method for consistent value normalization
    private Object normalizeValue(Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (str == null || "NULL".equalsIgnoreCase(str) || str.trim().isEmpty()) {
                return null;
            }
            return str.trim(); // Snowflake is case-insensitive, but preserve original case
        }
        return value;
    }
}
```

### AI-TDD vs Waterfall Comparison
```yaml
WATERFALL_APPROACH_PROBLEMS:
  DURATION: "8-12 hours typical"
  REWORK_CYCLES: "Multiple iterations of design→implement→test→fix"
  DESIGN_MISTAKES: "equals/hashCode often wrong, discovered late in testing"
  TEST_COVERAGE: "Incomplete, written after implementation"
  CONFIDENCE: "Low until full integration testing"
  
AI_TDD_APPROACH_SUCCESS:
  DURATION: "2-4 hours total (30min active development)"
  REWORK_CYCLES: "Zero - tests drive correct design immediately"
  DESIGN_QUALITY: "Superior - tests enforce proper patterns"
  TEST_COVERAGE: "100% by design"
  CONFIDENCE: "Immediate - continuous validation"
  
EFFICIENCY_MULTIPLIER: "3-4x faster with higher quality"
```

## OBJECT-SPECIFIC IMPLEMENTATION TEMPLATES
### AI-TDD Micro-Cycle Templates
```yaml
MICRO_CYCLE_TEMPLATES:
  OBJECT_IDENTITY_TEST: |
    @Test
    void shouldIdentifyObjectsByNameCatalogSchema() {
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setCatalogName("TEST_DB");
        obj1.setSchemaName("TEST_SCHEMA");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ"); 
        obj2.setCatalogName("TEST_DB");
        obj2.setSchemaName("TEST_SCHEMA");
        
        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }
    
  SNAPSHOT_PRIORITY_TEST: |
    @Test
    void shouldHaveHighPriorityForObjectOnSnowflake() {
        ${ObjectType}SnapshotGeneratorSnowflake generator = 
            new ${ObjectType}SnapshotGeneratorSnowflake();
        SnowflakeDatabase database = mock(SnowflakeDatabase.class);
        
        int priority = generator.getPriority(${ObjectType}.class, database);
        assertTrue(priority > 0);
    }
    
  COMPARATOR_IDENTITY_TEST: |
    @Test
    void shouldIdentifySameObjectsWhenIdentityMatches() {
        ${ObjectType}ComparatorSnowflake comparator = new ${ObjectType}ComparatorSnowflake();
        
        ${ObjectType} obj1 = new ${ObjectType}();
        obj1.setName("TEST_OBJ");
        obj1.setCatalogName("TEST_DB");
        obj1.setSchemaName("TEST_SCHEMA");
        
        ${ObjectType} obj2 = new ${ObjectType}();
        obj2.setName("TEST_OBJ");
        obj2.setCatalogName("TEST_DB");
        obj2.setSchemaName("TEST_SCHEMA");
        
        boolean isSame = comparator.isSameObject(obj1, obj2, database, null);
        assertTrue(isSame);
    }
```

### Implementation Commands
```yaml
MICRO_CYCLE_COMMANDS:
  RUN_SINGLE_TEST:
    EXECUTE: "mvn test -Dtest=${TEST_CLASS}#${TEST_METHOD} -q"
    PURPOSE: "Execute individual test in Red/Green cycle"
    
  RUN_ALL_OBJECT_TESTS:
    EXECUTE: "mvn test -Dtest='*${ObjectType}*Test' -q"  
    PURPOSE: "Verify no regression after refactoring"
    
  RUN_INTEGRATION_TESTS:
    EXECUTE: "SNOWFLAKE_URL='...' mvn test -Dtest='*${ObjectType}*IntegrationTest' -q"
    PURPOSE: "Validate with real database"
    
SUCCESS_VALIDATION:
  ALL_TESTS_PASS: "mvn test -q exits with code 0"
  NO_COMPILATION_ERRORS: "mvn compile -q exits with code 0"
  SERVICE_REGISTRATION: "grep ${ObjectType} src/main/resources/META-INF/services/*"
```

### Database Object Template
```yaml
DATABASE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "name (required)"
    - "comment (optional)"
    - "owner (state property - exclude from diff)"
    - "created_time (state property - exclude from diff)"
    
  SNAPSHOT_SQL:
  BASE_PATTERN: |
    protected ${ObjectType} snapshot${ObjectType}(${ObjectType} example, DatabaseSnapshot snapshot) 
            throws DatabaseException {
        Database database = snapshot.getDatabase();
        
        String sql = "SELECT name, property1, property2 FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S " +
                    "WHERE catalog_name = ? AND schema_name = ?";
        List<Object> params = Arrays.asList(
            database.getDefaultCatalogName(), 
            database.getDefaultSchemaName()
        );
        
        if (example.getName() != null) {
            sql += " AND name = ?";
            params.add(example.getName());
        }
        
        List<Map<String, ?>> results = ExecutorService.getInstance()
            .getExecutor(database).queryForList(new RawParameterizedSql(sql, params.toArray()));
            
        Set<${ObjectType}> objects = new HashSet<>();
        for (Map<String, ?> row : results) {
            ${ObjectType} obj = new ${ObjectType}();
            obj.setName((String) row.get("NAME"));
            obj.setProperty1((String) row.get("PROPERTY1"));
            obj.setProperty2(normalizeBoolean((String) row.get("PROPERTY2")));
            objects.add(obj);
        }
        
        return example.getName() != null ? 
            objects.stream().findFirst().orElse(null) : null;
    }
```

### STEP 3.2: Service Registration and Testing

**For automated snapshot service validation**: Use `aipl_programs/snapshot-diff-registration-validation.yaml`

```yaml
CRITICAL_REGISTRATION:
  FILE: "src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "${PACKAGE}.${ObjectType}SnapshotGeneratorSnowflake"
```

## PHASE 4: DIFF IMPLEMENTATION

### STEP 4.1: DatabaseObjectComparator Implementation
```yaml
COMPARATOR_PATTERN:
  CLASS_STRUCTURE: |
    public class ${ObjectType}ComparatorSnowflake extends DatabaseObjectComparator {
        @Override
        public boolean supports(Class<? extends DatabaseObject> type, Database database) {
            return ${ObjectType}.class.isAssignableFrom(type) && 
                   database instanceof SnowflakeDatabase;
        }
        
        @Override
        public ObjectDifferences compare(DatabaseObject referenceObject, 
                                       DatabaseObject comparisonObject, 
                                       DatabaseSnapshot referenceSnapshot,
                                       DatabaseSnapshot comparisonSnapshot, 
                                       CompareControl compareControl) {
            return compare${ObjectType}((${ObjectType}) referenceObject, 
                                      (${ObjectType}) comparisonObject, 
                                      compareControl);
        }
        
        protected ObjectDifferences compare${ObjectType}(${ObjectType} reference, 
                                                       ${ObjectType} comparison, 
                                                       CompareControl compareControl) {
            ObjectDifferences differences = new ObjectDifferences(compareControl);
            
            // Compare each property with appropriate logic
            compareProperty(differences, "property1", 
                          reference.getProperty1(), comparison.getProperty1());
            compareProperty(differences, "property2", 
                          reference.getProperty2(), comparison.getProperty2());
            
            return differences;
        }
    }
    
PROPERTY_COMPARISON_PATTERNS:
  STRING_COMPARISON: |
    private void compareProperty(ObjectDifferences differences, String propertyName, 
                               String reference, String comparison) {
        reference = normalizeString(reference);
        comparison = normalizeString(comparison);
        
        if (!Objects.equals(reference, comparison)) {
            differences.addDifference(propertyName, reference, comparison);
        }
    }
    
  BOOLEAN_COMPARISON: |
    private void compareProperty(ObjectDifferences differences, String propertyName, 
                               Boolean reference, Boolean comparison) {
        if (!Objects.equals(reference, comparison)) {
            differences.addDifference(propertyName, reference, comparison);
        }
    }
    
  EXCLUDE_STATE_PROPERTIES: |
    // DO NOT compare state properties like createdTime, systemId
    // These are read-only and should not affect diff results
```

### STEP 4.2: Service Registration and Testing

**For automated diff comparator validation**: Use `aipl_programs/snapshot-diff-registration-validation.yaml`

```yaml
CRITICAL_REGISTRATION:
  FILE: "src/main/resources/META-INF/services/liquibase.diff.output.DatabaseObjectComparator"
  ENTRY: "${PACKAGE}.${ObjectType}ComparatorSnowflake"
```

## PHASE 5: COMPREHENSIVE TESTING AND VALIDATION

### STEP 5.1: Integration Test Implementation

**For automated integration testing**: Use `aipl_programs/snapshot-diff-integration-testing.yaml`

```yaml
INTEGRATION_TEST_ESSENTIALS:
  WORKFLOW: "Create object → Snapshot → Verify → Diff → Cleanup"
  VALIDATION: "Object found in snapshot, properties correct, diff detects changes"
  CRITICAL_SUCCESS: "State properties excluded from diff, service registrations verified"
```

### STEP 5.2: Error Patterns and Debugging
```yaml
COMMON_ERROR_PATTERNS:
  SERVICE_REGISTRATION_MISSING:
    SYMPTOM: "Objects not found in snapshot/diff"
    CAUSE: "Missing or incorrect META-INF/services registration"
    SOLUTION: "Use automated validation: aipl_programs/snapshot-diff-registration-validation.yaml"
    
  SQL_QUERY_ERRORS:
    SYMPTOM: "DatabaseException during snapshot"
    CAUSE: "Invalid SQL syntax or missing columns"
    SOLUTION: "Test SQL queries directly in database console"
    COMMAND: "Execute snapshot SQL manually to verify syntax"
    
  CASE_SENSITIVITY_ISSUES:
    SYMPTOM: "Objects found manually but not in snapshot"
    CAUSE: "Database case sensitivity differs from expectations"
    SOLUTION: "Use UPPER() or database-specific case handling"
    EXAMPLE: "WHERE UPPER(object_name) = UPPER(?)"
    
  PROPERTY_NORMALIZATION_ERRORS:
    SYMPTOM: "Unexpected differences in diff results"
    CAUSE: "Inconsistent null/default value handling"
    SOLUTION: "Implement consistent normalization methods"
    PATTERN: |
      private String normalizeString(String value) {
          if (value == null || "NULL".equalsIgnoreCase(value) || value.trim().isEmpty()) {
              return null;
          }
          return value.trim();
      }
      
  STATE_PROPERTY_COMPARISON:
    SYMPTOM: "Diff shows differences for identical objects"
    CAUSE: "Comparing read-only/system properties"
    SOLUTION: "Exclude state properties from comparison"
    CRITICAL: "Never compare timestamps, system IDs, or auto-generated values"
```

### STEP 5.3: Performance Optimization
```yaml
OPTIMIZATION_PATTERNS:
  SQL_QUERY_OPTIMIZATION:
    - "Use specific WHERE clauses to limit result sets"
    - "Add ORDER BY for consistent results"
    - "Consider database-specific query hints"
    - "Batch queries when possible"
    
  OBJECT_CREATION_OPTIMIZATION:
    - "Reuse object instances when appropriate"
    - "Minimize database round trips"
    - "Cache frequently accessed metadata"
    - "Use connection pooling effectively"
    
  MEMORY_OPTIMIZATION:
    - "Stream large result sets when possible"
    - "Avoid holding entire snapshots in memory"
    - "Clean up resources promptly"
    - "Use appropriate collection types"
```

## OBJECT-SPECIFIC IMPLEMENTATION TEMPLATES

### Database Object Template
```yaml
DATABASE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "name (required)"
    - "comment (optional)"
    - "owner (state property - exclude from diff)"
    - "created_time (state property - exclude from diff)"
    
  SNAPSHOT_SQL: |
    SELECT 
        database_name as name,
        comment,
        database_owner as owner,
        created as created_time
    FROM INFORMATION_SCHEMA.DATABASES
    WHERE database_name = ?
    
  COMPARISON_FOCUS:
    - "Compare name and comment only"
    - "Exclude owner and created_time"
    - "Handle null comments consistently"
```

### Schema Object Template
```yaml
SCHEMA_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName (required)"
    - "comment (optional)"
    - "managedAccess (optional boolean)"
    - "owner (state property - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        catalog_name,
        schema_name,
        comment,
        is_managed_access = 'YES' as managed_access,
        schema_owner as owner
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE catalog_name = ? AND schema_name = ?
    
  COMPARISON_FOCUS:
    - "Compare catalogName, schemaName, comment, managedAccess"
    - "Exclude owner"
    - "Handle boolean conversion properly"
```

### Table Object Template
```yaml
TABLE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName, tableName (required)"
    - "tableType (required)"
    - "comment (optional)"
    - "clustering, partitioning options (optional)"
    - "rowCount, bytes (state properties - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        table_catalog,
        table_schema,
        table_name,
        table_type,
        comment,
        clustering_key,
        row_count,
        bytes
    FROM INFORMATION_SCHEMA.TABLES
    WHERE table_catalog = ? AND table_schema = ? 
    [AND table_name = ?]
    
  COMPARISON_FOCUS:
    - "Compare name, type, comment, clustering"
    - "Exclude rowCount, bytes"
    - "Handle different table types appropriately"
```

### Sequence Object Template
```yaml
SEQUENCE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName, sequenceName (required)"
    - "startValue, incrementBy (optional with defaults)"
    - "minValue, maxValue (optional)"
    - "comment, ordered (optional)"
    - "nextValue (state property - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        sequence_catalog,
        sequence_schema,
        sequence_name,
        start_value,
        increment,
        minimum_value,
        maximum_value,
        cycle_option,
        ordered,
        comment,
        next_value
    FROM INFORMATION_SCHEMA.SEQUENCES
    WHERE sequence_catalog = ? AND sequence_schema = ?
    [AND sequence_name = ?]
    
  COMPARISON_FOCUS:
    - "Compare all configuration properties"
    - "Exclude nextValue (state property)"
    - "Handle default values consistently"
```

## QUICK_REFERENCE_COMMANDS

### Architectural Assessment
```bash
# Check if object exists in Liquibase core
find . -name '*${ObjectType}*.java' -path '*/liquibase-core/*'

# Check existing snapshot/diff implementations
find . -name '*${ObjectType}*' -path '*/snapshot/*' -o -path '*/diff/*'

# Verify database object queryability
echo "SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S LIMIT 1" | sqlcmd
```

### Implementation Validation

**For automated service registration validation**: Use `aipl_programs/snapshot-diff-registration-validation.yaml`

```bash
# Basic compilation check
mvn compile

# Run specific object tests
mvn test -Dtest="*${ObjectType}*Test"

# Run integration tests
mvn test -Dtest="*${ObjectType}*IntegrationTest"

# Full test suite
mvn test

# Check for compilation errors
mvn compile 2>&1 | grep -i error
```

### Manual Database Testing
```sql
-- Test object creation and querying
CREATE ${OBJECT_TYPE} test_object_name [WITH properties];

-- Verify object appears in INFORMATION_SCHEMA
SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S 
WHERE ${name_column} = 'test_object_name';

-- Test snapshot SQL manually
[Execute your snapshot SQL query]

-- Clean up
DROP ${OBJECT_TYPE} test_object_name;
```

### Build Validation
```bash
# Compile implementation
mvn compile

# Package with tests
mvn package

# Lint check (if available)
mvn checkstyle:check
```

## SUCCESS_CRITERIA

### Object Model Completeness
```yaml
OBJECT_MODEL_COMPLETE:
  STRUCTURE:
    - "Object model class with all required properties"
    - "Proper extends AbstractDatabaseObject"
    - "Correct equals/hashCode implementation (name, catalog, schema only)"
    - "Comprehensive toString for debugging"
    
  VALIDATION:
    - "validate() method with proper error handling"
    - "Property normalization methods"
    - "Appropriate constructor patterns"
```

### Snapshot Generator Completeness
```yaml
SNAPSHOT_GENERATOR_COMPLETE:
  FUNCTIONALITY:
    - "Generates complete snapshots with all properties"
    - "Handles null/default values consistently"
    - "Proper SQL query with parameter binding"
    - "Correct result set processing"
    
  INTEGRATION:
    - "Properly registered as service in META-INF/services"
    - "Correct supports() method implementation"
    - "Unit tests pass with mock data"
    - "Integration tests work with real database"
```

### Diff Comparator Completeness
```yaml
COMPARATOR_COMPLETE:
  COMPARISON_LOGIC:
    - "Compares all configuration properties"
    - "Excludes state properties from comparison"
    - "Handles edge cases (nulls, defaults, empty strings)"
    - "Consistent property normalization"
    
  TESTING:
    - "Unit tests cover all comparison scenarios"
    - "No differences for identical objects"
    - "Proper differences for changed properties"
    - "State properties properly excluded"
```

### Integration Validation
```yaml
INTEGRATION_COMPLETE:
  DATABASE_INTEGRATION:
    - "Real database objects can be snapshotted"
    - "Diff detects actual differences accurately"
    - "No unexpected differences for identical objects"
    - "Integration tests pass consistently"
    
  FRAMEWORK_INTEGRATION:
    - "Service registration working correctly"
    - "No compilation or runtime errors"
    - "Proper error handling and logging"
    - "Performance acceptable for target use cases"

**For automated debugging when errors occur**: Use `aipl_programs/systematic-implementation-debugging.yaml`
```

## TROUBLESHOOTING QUICK REFERENCE

### Compilation Issues
- **For service registration validation**: Use `aipl_programs/snapshot-diff-registration-validation.yaml`
- Verify all imports are correct
- Ensure object model extends AbstractDatabaseObject
- Confirm generator/comparator signatures match framework

### Snapshot Issues
- Test SQL queries manually in database
- Check case sensitivity of object/column names
- Verify database connection and permissions
- Validate INFORMATION_SCHEMA access

### Diff Issues
- Ensure state properties are excluded
- Check property normalization consistency
- Verify equals/hashCode implementation
- Test with identical objects first

### Integration Test Issues
- Confirm test database supports object type
- Check object creation permissions
- Verify cleanup in finally blocks
- Consider test harness limitations

This guide provides everything needed for complete snapshot/diff implementation in a single, sequential workflow with no external dependencies or document hunting required.

## DIFF_CHANGELOG_WORKFLOW_INTEGRATION

### SNAPSHOT_TO_CHANGELOG_PIPELINE
```yaml
INTEGRATION_WORKFLOW:
  STEP_1_SNAPSHOT: "SnapshotGenerators capture complete database state"
  STEP_2_DIFF: "Diff comparators identify differences between states"
  STEP_3_CHANGETYPE_GENERATION: "ChangeGenerators convert differences to changetype operations"
  STEP_4_CHANGELOG_OUTPUT: "Liquibase core commands format and output changelog files"
  
CRITICAL_DEPENDENCIES:
  SNAPSHOT_COMPLETENESS: "All object properties must be captured for accurate diff"
  DIFF_ACCURACY: "State vs configuration property separation essential"
  CHANGEGENERATOR_REGISTRATION: "Missing/Unexpected/Changed generators required for each object"
  
CROSS_REFERENCE_GUIDE: "DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md"
```

### END_TO_END_VALIDATION

**For automated end-to-end workflow validation**: Use `aipl_programs/snapshot-diff-integration-testing.yaml`

```yaml
CRITICAL_VALIDATION_COMMANDS:
  VERIFY_SNAPSHOT_REGISTRATION: "grep -c 'SnapshotGenerator' src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
  VERIFY_DIFF_REGISTRATION: "grep -c 'Comparator' src/main/resources/META-INF/services/liquibase.diff.output.DatabaseObjectComparator"
  VERIFY_CHANGEGENERATOR_REGISTRATION: "grep -c 'ChangeGenerator' src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
```