# Complete Changetype Implementation Guide
## Ultimate Single-Source AI-Optimized Workflow

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: ULTIMATE_IMPLEMENTATION_GUIDE
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
CONSOLIDATES_ALL:
  - "README.md (navigation)"
  - "SIMPLE_PARAMETER_VALIDATION.md (quick validation)"
  - "ai_architectural_compliance.md (archived)"
  - "xsd_requirements_integration.md (archived)" 
  - "requirements_creation.md (archived)"
  - "changetype_patterns.md (archived)"
  - "All implementation guides"
RESULT: "Single source of truth - zero cognitive overhead"
```

## START HERE

**This is the complete changetype implementation workflow** - everything you need in one place:
- Quick validation (15-minute health check)
- Full implementation workflow (requirements → testing)
- All commands, templates, and validation protocols

**No other files needed.** All functionality consolidated into this single guide.

## IMPLEMENTATION SCENARIOS

### Scenario Selection
```yaml
SCENARIO_A_NEW_CHANGETYPE:
  DESCRIPTION: "Implement completely new changetype for database-specific operation"
  WORKFLOW: "Phase 0 → Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5"
  DURATION: "6-10 hours"
  
SCENARIO_B_EXTEND_EXISTING:
  DESCRIPTION: "Add database-specific attributes to existing Liquibase changetype"
  WORKFLOW: "Phase 0 → Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5"
  DURATION: "4-6 hours"
  
SCENARIO_C_ENHANCE_GENERATOR:
  DESCRIPTION: "Enhance existing SQL generator with additional functionality"
  WORKFLOW: "Phase 0 → Phase 3 → Phase 4 → Phase 5"
  DURATION: "3-5 hours"
  
SCENARIO_D_FIX_VALIDATION:
  DESCRIPTION: "Fix bugs in existing changetype or validation"
  WORKFLOW: "Phase 0 → Phase 4 → Phase 5"
  DURATION: "2-4 hours"
```

## PHASE 0: QUICK VALIDATION (OPTIONAL)

### When to Use Quick Validation
- Checking XSD completeness against Snowflake
- Validating new changetype parameters before full implementation  
- Quick health check of existing implementation coverage
- 15-minute verification vs days of full implementation

### 3-Step Quick Validation Process

#### STEP 0.1: Run INFORMATION_SCHEMA Query
```bash
# Use existing SnowflakeParameterValidationTest
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LTHDB&warehouse=LTHDB_TEST_WH&schema=TESTHARNESS&role=LIQUIBASE_TEST_HARNESS_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest=SnowflakeParameterValidationTest -q
```

#### STEP 0.2: Compare Against XSD Schema
```bash
# Check XSD file for missing attributes
grep -n "createDatabase\|createWarehouse\|createSequence" /src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd

# Look for missing attributes in relevant changetype elements
# INFORMATION_SCHEMA shows metadata fields - filter out obvious non-DDL parameters
```

#### STEP 0.3: Manual Documentation Review
- Cross-check Snowflake official docs for parameters INFORMATION_SCHEMA doesn't cover
- Focus on CREATE/ALTER/DROP syntax sections
- Example: https://docs.snowflake.com/en/sql-reference/sql/create-database

### Quick Validation Results (As of 2025-08-01)
| Object Type | Total Parameters | XSD Coverage | Status |
|-------------|------------------|--------------|---------|
| CREATE DATABASE | 14 | 14 | 100% Complete |
| CREATE WAREHOUSE | 17 | 17 | 100% Complete |
| CREATE SEQUENCE | 4 | 4 | 100% Complete |

**Key Finding**: All core XSD schemas are complete.

### Quick Validation Success Criteria
- INFORMATION_SCHEMA query runs successfully
- Parameters compared against XSD schema
- Manual documentation review confirms no gaps
- Any legitimate missing parameters added to XSD

**Continue to Phase 1 for full changetype implementation →**

## PHASE 1: ARCHITECTURAL PATTERN DECISION

### STEP 1.1: CHANGETYPE_EXISTS_CHECK
```yaml
DECISION_ALGORITHM:
  QUESTION: "Does Liquibase core already have this changetype?"
  DETECTION_METHOD: "Search liquibase-core for existing Change class"
  COMMAND: "find . -name '*Change*.java' -path '*/liquibase-core/*' | grep -i [changeTypeName]"
  
  IF_EXISTS:
    PATTERN: "NAMESPACE_ATTRIBUTE_EXTENSION"
    GOTO: "STEP_1.2_NAMESPACE_VALIDATION"
    
  IF_NOT_EXISTS:
    PATTERN: "NEW_CHANGETYPE_CREATION"
    GOTO: "STEP_1.3_NEW_CHANGETYPE_VALIDATION"
```

### STEP 1.2: NAMESPACE_ATTRIBUTE_EXTENSION_VALIDATION
```yaml
ARCHITECTURAL_COMPLIANCE_CHECKLIST:
  FORBIDDEN_ACTIONS:
    - "Creating *ChangeSnowflake classes for existing changetypes"
    - "Adding new elements to XSD for existing changetypes"
    - "Modifying Liquibase core change classes"
    
  REQUIRED_COMPONENTS:
    XSD_NAMESPACE_ATTRIBUTES: "Define in liquibase-snowflake-latest.xsd"
    PARSER_SUPPORT: "Verify in SnowflakeNamespaceAwareXMLParser.isTargetChangeType()"
    SQL_GENERATOR: "Extend standard generator, access SnowflakeNamespaceAttributeStorage"
    
  VALIDATION_COMMANDS:
    CHECK_VIOLATIONS: "find . -name '*ChangeSnowflake.java' -path '*/change/core/*'"
    EXPECTED_RESULT: "Empty output (no files found)"
    FAILURE_ACTION: "Delete violating files, implement namespace pattern"
    
  GOTO: "PHASE_2_REQUIREMENTS"
```

### STEP 1.3: NEW_CHANGETYPE_CREATION_VALIDATION
```yaml
NEW_CHANGETYPE_REQUIREMENTS:
  REQUIRED_COMPONENTS:
    CHANGE_CLASS: "Create *Change class for new functionality"
    STATEMENT_CLASS: "Create *Statement class"
    SQL_GENERATOR: "Create *Generator class"
    XSD_ELEMENT: "Define new element in XSD"
    SERVICE_REGISTRATION: "Register in META-INF/services files"
    
  GOTO: "PHASE_2_REQUIREMENTS"
```

## PHASE 2: REQUIREMENTS RESEARCH

### STEP 2.1: OFFICIAL_DOCUMENTATION_RESEARCH
```yaml
BLOCKING_VALIDATION:
  REQUIREMENT: "Official database vendor documentation researched"
  VALIDATION_CRITERIA:
    - "Official documentation URL included"
    - "Database version specified"
    - "Complete syntax documented from official source"
    - "All optional clauses identified"
  FAILURE_ACTION: "STOP - Complete official documentation research"
  
RESEARCH_COMMANDS:
  FIND_OFFICIAL_DOCS: "Search '[database] CREATE [object] syntax'"
  COPY_SYNTAX: "Copy full BNF/syntax diagram with all optional clauses"
  NOTE_VERSION: "Document URL and database version"
```

### STEP 2.2: COMPREHENSIVE_ATTRIBUTE_ANALYSIS
```yaml
ATTRIBUTE_TABLE_REQUIREMENTS:
  COLUMNS: ["Attribute", "Description", "Data Type", "Default", "Valid Values", "Required", "Constraints"]
  PROCESS:
    FOR_EACH_PARAMETER:
      EXTRACT: "Parameter name as it appears in Liquibase"
      DESCRIBE: "What the parameter does"
      TYPE: "String, Boolean, Integer, etc."
      DEFAULT: "Database default value if not specified"
      VALUES: "Acceptable values or ranges"
      REQUIRED: "Yes/No"
      CONSTRAINTS: "Limitations or business rules"
      
PROPERTY_CATEGORIZATION:
  REQUIRED_PROPERTIES:
    DEFINITION: "Must be specified at object creation"
    SQL_REQUIREMENT: "MUST be included in all SQL generation"
    TEST_REQUIREMENT: "MUST be tested with complete string comparison"
    
  OPTIONAL_CONFIGURATION:
    DEFINITION: "Can be set/changed by user"
    SQL_REQUIREMENT: "MUST be included in SQL when specified"
    TEST_REQUIREMENT: "MUST be tested in all combinations"
    
  STATE_PROPERTIES:
    DEFINITION: "Runtime information, read-only"
    SQL_REQUIREMENT: "EXCLUDE FROM SQL GENERATION"
    TEST_REQUIREMENT: "EXCLUDE FROM UNIT TEST COMPARISONS"
```

### STEP 2.3: SQL_EXAMPLES_AND_MUTUAL_EXCLUSIVITY
```yaml
SQL_EXAMPLE_REQUIREMENTS:
  MINIMUM_COUNT: 5
  COVERAGE:
    - "Minimal valid SQL (required properties only)"
    - "Optional properties combinations"
    - "Edge cases and maximum complexity"
    - "Mutually exclusive variations (separate examples)"
    
MUTUAL_EXCLUSIVITY_ANALYSIS:
  DETECTION: "Look for 'cannot be used with' in documentation"
  VALIDATION: "Test combinations in database to verify"
  DOCUMENTATION: "Document which attributes conflict"
  TEST_STRATEGY: "Plan separate test files for incompatible features"
```

## PHASE 3: XSD SCHEMA INTEGRATION

### STEP 3.1: XSD_PATTERN_IMPLEMENTATION
```yaml
NAMESPACE_ATTRIBUTE_XSD_TEMPLATE:
  LOCATION: "src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
  TEMPLATE: |
    <!-- Snowflake namespace attributes (for use on standard ${changeType}) -->
    <xsd:attribute name="${attributeName}" type="${xsdType}">
        <xsd:annotation>
            <xsd:documentation>${documentation}</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
TYPE_MAPPINGS:
  Java_String: "xsd:string"
  Java_Boolean: "xsd:boolean"
  Java_Integer: "xsd:integer"
  Java_BigInteger: "xsd:decimal"
  Java_Enum: "xsd:string"
  
VALIDATION_COMMANDS:
  SYNTAX_CHECK: "xmllint --schema xsd-validation.xsd --noout liquibase-snowflake-latest.xsd"
  ATTRIBUTE_COUNT: "grep -c 'name=\"${attributeName}\"' src/main/resources/*.xsd"
  TYPE_VALIDATION: "grep -A 2 'name=\"${attributeName}\"' src/main/resources/*.xsd | grep 'type='"
```

### STEP 3.2: PARSER_INTEGRATION_VALIDATION
```yaml
PARSER_COMPLIANCE_REQUIREMENTS:
  FILE: "src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java"
  
  CHANGETYPE_SUPPORT:
    METHOD: "isTargetChangeType(String localName)"
    REQUIREMENT: "Target changetype MUST be listed"
    VALIDATION: "grep -n '${changeTypeName}' SnowflakeNamespaceAwareXMLParser.java"
    
  OBJECT_NAME_EXTRACTION:
    METHOD: "getObjectName(String changeType, Attributes attributes)" 
    REQUIREMENT: "MUST handle object name extraction"
    TEMPLATE: |
      case "${changeTypeName}":
          return attributes.getValue("${objectNameAttribute}");
          
  ATTRIBUTE_STORAGE:
    CLASS: "SnowflakeNamespaceAttributeStorage"
    REQUIREMENT: "Attributes stored using object name as key"
    ACCESS_PATTERN: "SnowflakeNamespaceAttributeStorage.getAttributes(objectName)"
```

## PHASE 4: IMPLEMENTATION

### STEP 4.1: SQL_GENERATOR_IMPLEMENTATION
```yaml
NAMESPACE_ATTRIBUTE_ACCESS_PATTERN:
  TEMPLATE: |
    public class ${ChangeType}GeneratorSnowflake extends ${ChangeType}Generator {
        @Override
        public Sql[] generateSql(${ChangeType}Statement statement, Database database, SqlGeneratorChain chain) {
            // Get base SQL from parent
            Sql[] baseSql = super.generateSql(statement, database, chain);
            
            // Access namespace attributes
            Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getObjectName());
            
            if (attributes != null && !attributes.isEmpty()) {
                return enhanceWithNamespaceAttributes(baseSql, attributes);
            }
            
            return baseSql;
        }
        
        private Sql[] enhanceWithNamespaceAttributes(Sql[] baseSql, Map<String, String> attributes) {
            // Enhancement logic based on namespace attributes
            StringBuilder enhancedSql = new StringBuilder(baseSql[0].toSql());
            
            // Add Snowflake-specific syntax based on attributes
            ${ATTRIBUTE_ENHANCEMENT_LOGIC}
            
            return new Sql[]{new UnparsedSql(enhancedSql.toString(), getAffectedObjects(statement))};
        }
    }
    
SERVICE_REGISTRATION:
  FILE: "src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator"
  ENTRY: "${PACKAGE}.${ChangeType}GeneratorSnowflake"
```

### STEP 4.2: VALIDATION_RULES_IMPLEMENTATION
```yaml
VALIDATION_PATTERNS:
  REQUIRED_FIELD_VALIDATION:
    PATTERN: "if (${field} == null || ${field}.isEmpty()) throw new ValidationErrors('${field} is required')"
    
  MUTUAL_EXCLUSIVITY_VALIDATION:
    PATTERN: |
      if (${field1} != null && ${field2} != null) {
          throw new ValidationErrors("Cannot use both ${field1} and ${field2}");
      }
      
  VALUE_RANGE_VALIDATION:
    PATTERN: |
      if (${field} != null && (${field} < ${min} || ${field} > ${max})) {
          throw new ValidationErrors("${field} must be between ${min} and ${max}");
      }
```

## PHASE 5: TESTING AND VALIDATION

### STEP 5.1: COMPREHENSIVE_TEST_SCENARIOS
```yaml
TEST_FILE_ORGANIZATION:
  COMPATIBLE_FEATURES:
    FILE: "${changeType}.xml"
    SCENARIOS:
      - "Required properties only"
      - "Required + optional properties"
      - "Maximum compatible complexity"
      
  MUTUALLY_EXCLUSIVE_FEATURES:
    FILE_PER_EXCLUSION: "${changeType}${ExclusiveFeature}.xml"
    EXAMPLE: "createSequenceOrReplace.xml, createSequenceIfNotExists.xml"
    
UNIT_TEST_REQUIREMENTS:
  STRING_COMPARISON: "Complete SQL strings must match exactly"
  PROPERTY_COVERAGE: "All required and optional properties tested"
  STATE_EXCLUSION: "State properties excluded from comparisons"
  
INTEGRATION_TEST_REQUIREMENTS:
  SQL_EXECUTION: "All generated SQL must execute successfully"
  DATABASE_STATE: "Final state must match expected properties"
  COMPLETE_COVERAGE: "Every SQL generation path tested"
```

### STEP 5.2: AUTOMATED_VALIDATION_COMMANDS
```yaml
ARCHITECTURAL_COMPLIANCE_VALIDATION:
  CHECK_VIOLATIONS: "find . -name '*ChangeSnowflake.java' -path '*/change/core/*'"
  VERIFY_PARSER_SUPPORT: "grep -n '${changeTypeName}' src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java"
  VALIDATE_XSD_ATTRIBUTES: "grep -A 5 -B 1 'xsd:attribute.*name=\"${newAttribute}\"' src/main/resources/*.xsd"
  CHECK_GENERATOR_PATTERN: "grep -l 'SnowflakeNamespaceAttributeStorage' src/main/java/liquibase/sqlgenerator/core/*.java"
  
TEST_EXECUTION:
  UNIT_TESTS: "mvn test -Dtest='*${ChangeType}*Test'"
  INTEGRATION_TESTS: "mvn test -Dtest='*${ChangeType}*IntegrationTest'"
  FULL_SUITE: "mvn test"
  
BUILD_VALIDATION:
  COMPILE: "mvn compile"
  PACKAGE: "mvn package -DskipTests"
  LINT_CHECK: "mvn checkstyle:check"
```

## BLOCKING_EXECUTION_CHECKPOINTS

### CHECKPOINT_1: ARCHITECTURAL_PATTERN_VALIDATION
```yaml
CONDITION: "Implementation pattern identified and compliant"
VALIDATION: 
  - "Pattern decision documented (namespace vs new changetype)"
  - "No architectural violations detected"
  - "XSD approach planned (attributes vs elements)"
FAILURE_ACTION: "STOP - Review architectural compliance requirements"
```

### CHECKPOINT_2: REQUIREMENTS_COMPLETENESS
```yaml
CONDITION: "Complete requirements documented with examples"
VALIDATION:
  - "Attribute table complete with all 7 columns"
  - "Property categorization complete"
  - "Mutual exclusivity rules identified"
  - "Minimum 5 SQL examples provided"
FAILURE_ACTION: "STOP - Complete requirements documentation"
```

### CHECKPOINT_3: XSD_SCHEMA_READINESS
```yaml
CONDITION: "XSD schema requirements specified"
VALIDATION:
  - "All namespace attributes defined in XSD"
  - "Parser integration requirements documented"
  - "Type mappings validated"
FAILURE_ACTION: "STOP - Define XSD schema requirements"
```

### CHECKPOINT_4: IMPLEMENTATION_COMPLETENESS
```yaml
CONDITION: "SQL generator implementation complete"
VALIDATION:
  - "Generator extends standard class (not new change class)"
  - "Namespace attribute access implemented"
  - "Service registration completed"
FAILURE_ACTION: "STOP - Complete implementation with namespace pattern"
```

### CHECKPOINT_5: TEST_COVERAGE_VALIDATION
```yaml
CONDITION: "Complete test coverage implemented"
VALIDATION:
  - "Unit tests with complete string comparison"
  - "Integration tests for all SQL generation paths"
  - "Separate test files for mutually exclusive features"
FAILURE_ACTION: "STOP - Implement comprehensive test coverage"
```

## QUICK_REFERENCE_COMMANDS

### ARCHITECTURAL_DECISION
```bash
# Check if changetype exists in Liquibase core
find . -name '*Change*.java' -path '*/liquibase-core/*' | grep -i ${changeTypeName}

# Validate no architectural violations
find . -name '*ChangeSnowflake.java' -path '*/change/core/*'
```

### XSD_VALIDATION
```bash
# Check XSD attribute definitions
grep -A 5 'name="${attributeName}"' src/main/resources/*.xsd

# Validate XSD syntax
xmllint --schema xsd-validation.xsd --noout liquibase-snowflake-latest.xsd
```

### PARSER_VALIDATION
```bash
# Verify changetype support
grep -n "${changeTypeName}" src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java

# Check object name extraction
grep -A 10 "getObjectName" src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java
```

### TEST_EXECUTION
```bash
# Run specific changetype tests
mvn test -Dtest="*${ChangeType}*Test"

# Run full test suite
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"
```

### BUILD_VALIDATION
```bash
# Compile and validate
mvn compile

# Package with tests
mvn package

# Lint check
mvn checkstyle:check
```

## SUCCESS_CRITERIA

### ARCHITECTURAL_COMPLIANCE
- No database-specific change classes for existing changetypes
- Namespace attributes properly defined in XSD
- Parser integration properly implemented
- SQL generator extends standard generator

### IMPLEMENTATION_COMPLETENESS
-  All Snowflake-specific attributes implemented
-  Complete SQL generation for all property combinations
-  Proper validation rules for mutual exclusivity
-  Service registration completed

### TEST_COVERAGE
-  Unit tests with complete string comparison
-  Integration tests for all SQL paths
-  Separate test files for mutually exclusive features
-  100% passing test suite

### DOCUMENTATION_QUALITY
-  Requirements document with comprehensive attribute table
-  XSD schema properly documented
-  Implementation approach clearly specified
-  Validation rules comprehensively documented

## COMMON_VIOLATIONS_AND_SOLUTIONS

### ARCHITECTURAL_VIOLATIONS
```yaml
VIOLATION: "Creating CreateSequenceChangeSnowflake for existing changetype"
SOLUTION: "Delete violating class, use namespace attributes with standard createSequence"

VIOLATION: "Adding new XSD elements for existing changetypes"
SOLUTION: "Use namespace attributes, not new elements"

VIOLATION: "Modifying Liquibase core classes"
SOLUTION: "Extend standard generators, access namespace storage"
```

### IMPLEMENTATION_ISSUES
```yaml
ISSUE: "SQL generator not accessing namespace attributes"
SOLUTION: "Use SnowflakeNamespaceAttributeStorage.getAttributes(objectName)"

ISSUE: "Parser not processing changetype"
SOLUTION: "Add changetype to isTargetChangeType() method"

ISSUE: "Service not registered"
SOLUTION: "Add entry to META-INF/services/liquibase.sqlgenerator.SqlGenerator"
```

This consolidated guide eliminates document sprawl while preserving all critical functionality for AI-driven changetype implementation.