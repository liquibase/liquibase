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
  AUTOMATION_AVAILABLE:
    PROGRAM: "aipl_programs/systematic-implementation-debugging.yaml"
    TRIGGER: "When debugging incomplete implementations or test failures"
    BENEFIT: "Layer-by-layer automated diagnosis and fix generation"
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
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest=SnowflakeParameterValidationTest -q
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

## ROLLBACK_DESIGN_PRINCIPLES
```yaml
ROLLBACK_DECISION_MATRIX:
  CREATE_OPERATIONS:
    SUPPORTS_ROLLBACK: true
    ROLLBACK_STRATEGY: "Generate corresponding DROP operation"
    TEMPLATE: "DropChange with same object name"
    VALIDATION:
      TYPE: "CONTAINS_TEXT"
      TARGET: "${CREATE_CHANGE_FILE}"
      PATTERN: "return true"
  DROP_OPERATIONS:
    SUPPORTS_ROLLBACK: false
    REASON: "Data loss - cannot restore dropped objects"
    VALIDATION:
      TYPE: "CONTAINS_TEXT"
      TARGET: "${DROP_CHANGE_FILE}"
      PATTERN: "return false"
  ALTER_RENAME_OPERATIONS:
    SUPPORTS_ROLLBACK: true
    CONDITION: "newName property is not null/empty"
    ROLLBACK_STRATEGY: "Reverse RENAME operation"
    TEMPLATE: "AlterChange with swapped old/new names"
  ALTER_PROPERTY_OPERATIONS:
    SUPPORTS_ROLLBACK: false
    REASON: "Complex state - difficult to reverse SET/UNSET operations"
    EXCEPTIONS: "Document any special cases"

ROLLBACK_SEMANTIC_GUIDELINES:
  SAFETY_FIRST:
    RULE: "When in doubt, do not support rollback"
    REASON: "Better to be conservative than risk data corruption"
  SNOWFLAKE_CONSTRAINTS:
    RENAME_OPERATIONS: "Support rollback - safe and reversible"
    PROPERTY_CHANGES: "Usually no rollback - complex interdependencies"
    DDL_OPERATIONS: "Support rollback for CREATE, not for DROP"
  VALIDATION_REQUIREMENTS:
    ROLLBACK_TESTS: "All changetypes must have rollback decision tests"
    INTEGRATION_TESTS: "Rollback-supporting operations need database validation"
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

### STEP 3.1B: XSD_RESOLUTION_TROUBLESHOOTING

**For automated XSD troubleshooting**: Use `aipl_programs/xsd-resolution-troubleshooting.yaml`

```yaml
EXECUTION_STEP: "3.1B: XSD_RESOLUTION_TROUBLESHOOTING"
VALIDATION_MODE: STRICT
PURPOSE: "Diagnose and resolve XSD resolution failures that block XML validation"
SUCCESS_CRITERIA:
  - "XSD entity resolution working correctly"
  - "No 'Unable to resolve xml entity' errors"
  - "SnowflakeNamespaceDetails properly registered"
FAILURE_ACTION: "STOP - Fix XSD resolution before continuing"
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

### STEP 4.1A: SQL_GENERATOR_CHAIN_MANAGEMENT
```yaml
EXECUTION_STEP: "4.1A: SQL_GENERATOR_CHAIN_MANAGEMENT"
PURPOSE: "Understand and control SQL generator selection and priority"
SUCCESS_CRITERIA:
  - "Generator priority logic understood"
  - "Override vs extend patterns clear"
  - "Chain debugging commands available"
FAILURE_ACTION: "Review generator selection conflicts"

**For automated incomplete implementation detection**: Use `aipl_programs/incomplete-implementation-detection.yaml`

GENERATOR_PRIORITY_SELECTION:
  HOW_LIQUIBASE_CHOOSES_GENERATORS:
    RULE_1: "Database-specific generators override generic ones"
    RULE_2: "Higher priority (lower number) generators selected first"
    RULE_3: "Last registered generator of same priority wins"
    RULE_4: "supports() method determines eligibility"
    
  PRIORITY_LEVELS:
    PRIORITY_5: "Generic Liquibase generators (lowest priority)"
    PRIORITY_10: "Database-specific generators (medium priority)"
    PRIORITY_1: "Override generators (highest priority)"
    
  SELECTION_ALGORITHM:
    STEP_1: "Filter by supports(statement, database) returning true"
    STEP_2: "Sort by priority (ascending - lower numbers first)"
    STEP_3: "Select first generator from sorted list"
    STEP_4: "Call generateSql() on selected generator"

GENERATOR_IMPLEMENTATION_PATTERNS:
  EXTEND_PATTERN:
    DESCRIPTION: "Enhance parent generator functionality"
    PRIORITY: "PRIORITY_10"
    TEMPLATE: |
      public class CreateTableGeneratorSnowflake extends CreateTableGenerator {
          @Override
          public int getPriority() { return PRIORITY_10; }
          
          @Override
          public boolean supports(CreateTableStatement statement, Database database) {
              return database instanceof SnowflakeDatabase;
          }
          
          @Override
          public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain chain) {
              Sql[] baseSql = super.generateSql(statement, database, chain);
              return enhanceWithSnowflakeFeatures(baseSql, statement);
          }
      }
    USE_WHEN: "Adding Snowflake-specific features to existing changetype"
    
  OVERRIDE_PATTERN:
    DESCRIPTION: "Completely replace parent generator"
    PRIORITY: "PRIORITY_1"
    TEMPLATE: |
      public class CreateWarehouseGeneratorSnowflake extends AbstractSqlGenerator<CreateWarehouseStatement> {
          @Override
          public int getPriority() { return PRIORITY_1; }
          
          @Override
          public boolean supports(CreateWarehouseStatement statement, Database database) {
              return database instanceof SnowflakeDatabase;
          }
          
          @Override
          public Sql[] generateSql(CreateWarehouseStatement statement, Database database, SqlGeneratorChain chain) {
              return new Sql[]{ new UnparsedSql(buildCreateWarehouseSql(statement)) };
          }
      }
    USE_WHEN: "Implementing completely new changetype"

GENERATOR_CHAIN_DEBUGGING:
  PRIORITY_CONFLICTS:
    SYMPTOM: "Wrong generator being selected"
    DEBUG_COMMAND: "Add logging to supports() and getPriority() methods"
    CHECK_PRIORITY: "Verify getPriority() returns expected value"
    CHECK_SUPPORTS: "Verify supports() method logic for your database/statement"
    
  GENERATOR_NOT_FOUND:
    SYMPTOM: "No generator found for statement"
    DEBUG_STEPS:
      - "Verify service registration in META-INF/services/liquibase.sqlgenerator.SqlGenerator"
      - "Check class implements SqlGenerator interface"
      - "Verify supports() method returns true for target database"
      - "Confirm generator class exists on classpath"
    
  MULTIPLE_GENERATORS:
    SYMPTOM: "Unexpected generator selected"
    SOLUTION: "Use priority levels to control selection order"
    VALIDATION: "Create test that verifies correct generator selected for Snowflake database"

GENERATOR_TESTING_PROTOCOLS:
  UNIT_TEST_TEMPLATE:
    PURPOSE: "Verify correct generator selection and SQL generation"
    TEMPLATE: |
      @Test
      public void testSnowflakeGeneratorSelection() {
          Database snowflakeDb = new SnowflakeDatabase();
          CreateTableStatement statement = new CreateTableStatement();
          
          SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();
          SqlGenerator generator = factory.getGenerator(statement, snowflakeDb);
          
          assertTrue("Should select Snowflake generator", 
                    generator instanceof CreateTableGeneratorSnowflake);
          
          Sql[] sql = generator.generateSql(statement, snowflakeDb, null);
          assertNotNull("Should generate SQL", sql);
          assertTrue("Should contain Snowflake syntax", 
                    sql[0].toSql().contains("SNOWFLAKE_SPECIFIC_SYNTAX"));
      }
```

### STEP 4.1B: SERVICE_REGISTRATION_COMPLETE_REFERENCE

**For automated service registration validation**: Use `aipl_programs/service-registration-validation.yaml`

```yaml
EXECUTION_STEP: "4.1B: SERVICE_REGISTRATION_COMPLETE_REFERENCE"
VALIDATION_MODE: STRICT
PURPOSE: "Complete service discovery registration for all Liquibase components"
SUCCESS_CRITERIA:
  - "All 6 service files properly configured"
  - "No component discovery failures"
  - "All custom objects included in snapshot scope"
FAILURE_ACTION: "STOP - Fix service registration issues"
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

### STEP 4.3: AUTO_ROLLBACK_IMPLEMENTATION
```yaml
CREATE_ROLLBACK_METHODS:
  CREATE_CHANGETYPE_SUPPORTS_ROLLBACK:
    MODIFY_FILE:
      TARGET: "src/main/java/liquibase/change/core/Create${ObjectType}Change.java"
      TEMPLATE: |
        @Override
        public boolean supportsRollback(Database database) {
            return database instanceof SnowflakeDatabase;
        }
        
        @Override
        public Change[] createInverses() {
            Drop${ObjectType}Change inverse = new Drop${ObjectType}Change();
            inverse.set${ObjectType}Name(get${ObjectType}Name());
            inverse.setCatalogName(getCatalogName());
            inverse.setSchemaName(getSchemaName());
            inverse.setIfExists(true);
            return new Change[]{inverse};
        }
        
  DROP_CHANGETYPE_NO_ROLLBACK:
    MODIFY_FILE:
      TARGET: "src/main/java/liquibase/change/core/Drop${ObjectType}Change.java"
      TEMPLATE: |
        @Override
        public boolean supportsRollback(Database database) {
            return false;
        }
        
  ALTER_CHANGETYPE_CONDITIONAL_ROLLBACK:
    CONDITIONAL:
      CONDITION:
        TYPE: "CONTAINS_TEXT"
        TARGET: "src/main/java/liquibase/change/core/Alter${ObjectType}Change.java"
        PATTERN: "newName"
      THEN:
        MODIFY_FILE:
          TARGET: "src/main/java/liquibase/change/core/Alter${ObjectType}Change.java"
          TEMPLATE: |
            @Override
            public boolean supportsRollback(Database database) {
                return getNew${ObjectType}Name() != null;
            }
            
            @Override
            public Change[] createInverses() {
                Alter${ObjectType}Change inverse = new Alter${ObjectType}Change();
                inverse.set${ObjectType}Name(getNew${ObjectType}Name());
                inverse.setNew${ObjectType}Name(get${ObjectType}Name());
                return new Change[]{inverse};
            }
      ELSE:
        MODIFY_FILE:
          TARGET: "src/main/java/liquibase/change/core/Alter${ObjectType}Change.java"
          TEMPLATE: |
            @Override
            public boolean supportsRollback(Database database) {
                return false;
            }

VALIDATE_ROLLBACK_IMPLEMENTATION:
  CHECK_METHODS_EXIST:
    TYPE: "CONTAINS_TEXT"
    TARGET: "src/main/java/liquibase/change/core/Create${ObjectType}Change.java"
    PATTERN: "supportsRollback"
    FAILURE_ACTION: "STOP"
    
  CHECK_INVERSE_METHODS:
    TYPE: "CONTAINS_TEXT"
    TARGET: "src/main/java/liquibase/change/core/Create${ObjectType}Change.java"
    PATTERN: "createInverses"
    FAILURE_ACTION: "STOP"
    
  TEST_ROLLBACK_FUNCTIONALITY:
    COMMAND:
      EXECUTE: "mvn test -Dtest='*${ObjectType}*Test' -DtestMethod='*rollback*'"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
```

### STEP 4.4: ROLLBACK_TESTING_REQUIREMENTS
```yaml
CONDITIONAL_ROLLBACK_PATTERNS:
  RENAME_OPERATION_ROLLBACK:
    SUPPORTS_ROLLBACK_TEMPLATE: |
      @Override
      public boolean supportsRollback(Database database) {
          return database instanceof SnowflakeDatabase && 
                 getNew${OBJECT_TYPE}Name() != null && !getNew${OBJECT_TYPE}Name().trim().isEmpty();
      }
    CREATE_INVERSES_TEMPLATE: |
      @Override
      public Change[] createInverses() {
          if (getNew${OBJECT_TYPE}Name() == null || getNew${OBJECT_TYPE}Name().trim().isEmpty()) {
              return new Change[0];
          }
          ${ALTER_CHANGE_TYPE} inverse = new ${ALTER_CHANGE_TYPE}();
          inverse.set${OBJECT_TYPE}Name(getNew${OBJECT_TYPE}Name());
          inverse.setNew${OBJECT_TYPE}Name(get${OBJECT_TYPE}Name());
          inverse.setIfExists(true);
          return new Change[]{inverse};
      }
    UNIT_TEST_TEMPLATE: |
      @Test
      void shouldSupportRollbackForRename() {
          change.set${OBJECT_TYPE}Name("OLD_${OBJECT_TYPE}");
          change.setNew${OBJECT_TYPE}Name("NEW_${OBJECT_TYPE}");
          assertTrue(change.supportsRollback(database));
      }
      
      @Test
      void shouldNotSupportRollbackWhenNewNameEmpty() {
          change.set${OBJECT_TYPE}Name("TEST_${OBJECT_TYPE}");
          change.setNew${OBJECT_TYPE}Name("");
          assertFalse(change.supportsRollback(database));
      }
      
      @Test
      void shouldCreateInverseForRename() {
          change.set${OBJECT_TYPE}Name("OLD_${OBJECT_TYPE}");
          change.setNew${OBJECT_TYPE}Name("NEW_${OBJECT_TYPE}");
          
          Change[] inverses = change.createInverses();
          assertEquals(1, inverses.length);
          
          ${ALTER_CHANGE_TYPE} inverse = (${ALTER_CHANGE_TYPE}) inverses[0];
          assertEquals("NEW_${OBJECT_TYPE}", inverse.get${OBJECT_TYPE}Name());
          assertEquals("OLD_${OBJECT_TYPE}", inverse.getNew${OBJECT_TYPE}Name());
          assertTrue(inverse.getIfExists());
      }

UNIT_TEST_ROLLBACK_COVERAGE:
  CREATE_CHANGETYPE_TESTS:
    FILE: "src/test/java/liquibase/change/core/Create${ObjectType}ChangeTest.java"
    REQUIRED_TESTS:
      SUPPORTS_ROLLBACK:
        TEMPLATE: |
          @Test
          @DisplayName("Should support rollback for Snowflake database")
          void shouldSupportRollback() {
              assertTrue(change.supportsRollback(database));
          }
          
      DOES_NOT_SUPPORT_NON_SNOWFLAKE:
        TEMPLATE: |
          @Test
          @DisplayName("Should not support rollback for non-Snowflake database")
          void shouldNotSupportRollbackForNonSnowflake() {
              assertFalse(change.supportsRollback(null));
          }
          
      CREATE_INVERSE_VALIDATION:
        TEMPLATE: |
          @Test
          @DisplayName("Should create inverse Drop${ObjectType}Change")
          void shouldCreateInverseDrop${ObjectType}() {
              change.set${ObjectType}Name("TEST_${OBJECT_UPPER}");
              change.setCatalogName("TEST_CATALOG");
              change.setSchemaName("TEST_SCHEMA");
              
              Change[] inverses = change.createInverses();
              
              assertNotNull(inverses);
              assertEquals(1, inverses.length);
              assertTrue(inverses[0] instanceof Drop${ObjectType}Change);
              
              Drop${ObjectType}Change dropChange = (Drop${ObjectType}Change) inverses[0];
              assertEquals("TEST_${OBJECT_UPPER}", dropChange.get${ObjectType}Name());
              assertEquals("TEST_CATALOG", dropChange.getCatalogName());
              assertEquals("TEST_SCHEMA", dropChange.getSchemaName());
              assertTrue(dropChange.getIfExists());
          }
          
  DROP_CHANGETYPE_TESTS:
    FILE: "src/test/java/liquibase/change/core/Drop${ObjectType}ChangeTest.java"
    REQUIRED_TESTS:
      NO_ROLLBACK_SUPPORT:
        TEMPLATE: |
          @Test
          @DisplayName("Should not support rollback for DROP operations")
          void shouldNotSupportRollback() {
              assertFalse(change.supportsRollback(database));
              assertFalse(change.supportsRollback(null));
          }
          
  ALTER_CHANGETYPE_TESTS:
    FILE: "src/test/java/liquibase/change/core/Alter${ObjectType}ChangeTest.java"
    CONDITIONAL:
      CONDITION:
        TYPE: "CONTAINS_TEXT"
        TARGET: "src/main/java/liquibase/change/core/Alter${ObjectType}Change.java"
        PATTERN: "newName"
      THEN:
        REQUIRED_TESTS:
          RENAME_ROLLBACK_SUPPORT:
            TEMPLATE: |
              @Test
              @DisplayName("Should support rollback for RENAME operations")
              void shouldSupportRollbackForRename() {
                  change.set${ObjectType}Name("OLD_NAME");
                  change.setNew${ObjectType}Name("NEW_NAME");
                  
                  assertTrue(change.supportsRollback(database));
              }
              
          NO_RENAME_NO_ROLLBACK:
            TEMPLATE: |
              @Test
              @DisplayName("Should not support rollback when newName is null")
              void shouldNotSupportRollbackWhenNewNameNull() {
                  change.set${ObjectType}Name("TEST_NAME");
                  change.setNew${ObjectType}Name(null);
                  
                  assertFalse(change.supportsRollback(database));
              }
              
          CREATE_INVERSE_RENAME:
            TEMPLATE: |
              @Test
              @DisplayName("Should create inverse RENAME operation")
              void shouldCreateInverseRenameOperation() {
                  change.set${ObjectType}Name("OLD_NAME");
                  change.setNew${ObjectType}Name("NEW_NAME");
                  change.setCatalogName("TEST_CATALOG");
                  change.setSchemaName("TEST_SCHEMA");
                  
                  Change[] inverses = change.createInverses();
                  
                  assertNotNull(inverses);
                  assertEquals(1, inverses.length);
                  assertTrue(inverses[0] instanceof Alter${ObjectType}Change);
                  
                  Alter${ObjectType}Change inverseChange = (Alter${ObjectType}Change) inverses[0];
                  assertEquals("NEW_NAME", inverseChange.get${ObjectType}Name());
                  assertEquals("OLD_NAME", inverseChange.getNew${ObjectType}Name());
                  assertEquals("TEST_CATALOG", inverseChange.getCatalogName());
                  assertEquals("TEST_SCHEMA", inverseChange.getSchemaName());
              }
      ELSE:
        REQUIRED_TESTS:
          NO_ROLLBACK_SUPPORT:
            TEMPLATE: |
              @Test
              @DisplayName("Should not support rollback for ALTER operations")
              void shouldNotSupportRollback() {
                  assertFalse(change.supportsRollback(database));
              }

INTEGRATION_TEST_ROLLBACK_COVERAGE:
  ROLLBACK_EXECUTION_TESTS:
    PURPOSE: "Verify rollback operations execute successfully in database"
    TEMPLATE: |
      @Test
      @DisplayName("Should execute rollback successfully")
      void shouldExecuteRollbackSuccessfully() {
          // Execute forward change
          Sql[] forwardSql = change.generateStatements(database);
          executeSql(forwardSql);
          
          // Execute rollback
          Change[] inverses = change.createInverses();
          Sql[] rollbackSql = inverses[0].generateStatements(database);
          executeSql(rollbackSql);
          
          // Verify original state restored
          assertObjectDoesNotExist("${OBJECT_NAME}");
      }
      
ROLLBACK_TEST_VALIDATION:
  VERIFY_UNIT_TESTS:
    COMMAND:
      EXECUTE: "mvn test -Dtest='*${ObjectType}*ChangeTest' -Dtest.method='*rollback*'"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
      
  VERIFY_INTEGRATION_TESTS:
    COMMAND:
      EXECUTE: "mvn test -Dtest='*${ObjectType}*IntegrationTest' -Dtest.method='*rollback*'"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
      
  VERIFY_ALL_ROLLBACK_TESTS:
    COMMAND:
      EXECUTE: "mvn test -Dtest='*Test' -Dtest.method='*rollback*'"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
```

## PHASE 5: TESTING AND VALIDATION

### STEP 5.1: COMPREHENSIVE_TEST_SCENARIOS
```yaml
MULTI_FORMAT_COVERAGE_STRATEGY:
  PRIORITY_CHANGETYPES:
    HIGH_PRIORITY: ["CREATE_DATABASE", "CREATE_WAREHOUSE", "CREATE_FILE_FORMAT"]
    MEDIUM_PRIORITY: ["ALTER_DATABASE", "ALTER_WAREHOUSE", "ALTER_FILE_FORMAT"]
    LOW_PRIORITY: ["DROP_*", "UTILITY_OPERATIONS"]
  FILE_NAMING_CONVENTIONS:
    YAML_FORMAT: "${CHANGETYPE_NAME}.yaml"
    JSON_FORMAT: "${CHANGETYPE_NAME}.json"
    CONDITIONAL_FEATURES: "${CHANGETYPE_NAME}${FEATURE_NAME}.yaml"
  COVERAGE_TARGETS:
    MINIMUM_YAML_COVERAGE: "30%"
    MINIMUM_JSON_COVERAGE: "20%"
    CREATE_OPERATIONS_COVERAGE: "80%"
  VALIDATION:
    NAME: "verify-multi-format-coverage"
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "find /liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake -name '*.yaml' -o -name '*.json' | wc -l"
    MINIMUM_COUNT: "10"

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

**For automated architectural compliance validation**: Use `aipl_programs/architectural-compliance-validation.yaml`

```yaml
EXECUTION_STEP: "5.2: AUTOMATED_VALIDATION_COMMANDS"
VALIDATION_MODE: STRICT
PURPOSE: "Architectural compliance and build validation"
SUCCESS_CRITERIA:
  - "No architectural pattern violations"
  - "All tests pass"
  - "Build completes successfully"
FAILURE_ACTION: "STOP - Fix compliance issues"
```

### STEP 5.3: TEST_DEVELOPMENT_GUIDELINES

**For automated test development guidance**: Use `aipl_programs/test-development-guidelines-snowflake.yaml`

**For automated systematic debugging**: Use `aipl_programs/systematic-implementation-debugging.yaml` when tests fail

**For automated test file management**: Use `aipl_programs/test-harness-orphaned-files.yaml` when resolving missing test files

```yaml
EXECUTION_STEP: "5.3: TEST_DEVELOPMENT_GUIDELINES"
VALIDATION_MODE: STRICT
PURPOSE: "Snowflake-specific test development patterns and troubleshooting"
SUCCESS_CRITERIA:
  - "All tests pass with correct SQL format expectations"
  - "Test harness integration working correctly"
  - "Rollback strategies properly configured"
FAILURE_ACTION: "STOP - Fix test development issues"
```
    
  ENUM_VALUES:
    GENERATED: "XSMALL"
    NOT_GENERATED: "'XSMALL'"
    RULE: "Enum values without quotes"
    
  STRING_LITERALS:
    GENERATED: "'actual string'"
    NOT_GENERATED: "actual string"
    RULE: "String literals with single quotes"
    
  NULL_VALUES:
    GENERATED: "NULL"
    NOT_GENERATED: "null"
    RULE: "NULL uppercase, not lowercase"

TEST_HARNESS_INTEGRATION:
  DEPENDENCY_CONFIGURATION:
    REQUIRED_DEPENDENCY: |
      <dependency>
          <groupId>org.liquibase</groupId>
          <artifactId>liquibase-snowflake</artifactId>
          <version>0-SNAPSHOT</version>
          <scope>test</scope>
      </dependency>
    CRITICAL: "Must be on test classpath for XSD resolution"
    
  DATABASE_CLEANUP_PATTERN:
    STRATEGY: "Aurora pattern with runAlways:true"
    EXAMPLE: |
      <changeSet id="cleanup-warehouses" author="test-harness" runAlways="true">
          <sql>DROP WAREHOUSE IF EXISTS TEST_WAREHOUSE;</sql>
      </changeSet>
    PURPOSE: "Ensure consistent test execution environment"

TEST_PHASE_WORKFLOW_UNDERSTANDING:
  PHASE_1_UPDATESQL:
    PURPOSE: "Generate SQL without executing"
    BEHAVIOR: "SQL generation and validation only"
    CONFLICTS: "Object name conflicts can occur"
    
  PHASE_2_UPDATE:
    PURPOSE: "Execute generated SQL against database" 
    BEHAVIOR: "Actual database state changes"
    DEPENDENCY: "Requires working database connection"
    
  OBJECT_CONFLICT_RESOLUTION:
    ISSUE: "Tests fail due to object naming conflicts between phases"
    SOLUTION: "Use unique object names or proper cleanup between phases"
    EXAMPLE: "TEST_WAREHOUSE_${TIMESTAMP} vs TEST_WAREHOUSE"

ROLLBACK_STRATEGY_SELECTION:
  ROLLBACK_BY_TAG:
    USAGE: "mvn test -DrollbackStrategy=rollbackByTag -DdbName=snowflake"
    RECOMMENDED_FOR: "Cloud databases like Snowflake"
    BEHAVIOR: "Tags database state, rolls back to tag"
    ADVANTAGE: "More reliable for cloud environments"
    
  ROLLBACK_TO_DATE:
    USAGE: "mvn test -DrollbackStrategy=rollbackToDate -DdbName=snowflake"
    DEFAULT: "true"
    BEHAVIOR: "Records timestamp, rolls back changes after time"
    LIMITATION: "Can be less reliable in cloud environments"

ROLLBACK_TESTING_PATTERNS:
  AUTO_ROLLBACK_UNIT_TESTS:
    PURPOSE: "Verify rollback methods implemented correctly"
    PATTERNS:
      SUPPORTS_ROLLBACK_CHECK:
        TEMPLATE: |
          @Test
          void shouldSupportRollback() {
              assertTrue(change.supportsRollback(database));
          }
          
      INVERSE_CREATION_VALIDATION:
        TEMPLATE: |
          @Test
          void shouldCreateCorrectInverse() {
              // Setup change with test data
              change.setObjectName("TEST_OBJECT");
              
              // Create inverse
              Change[] inverses = change.createInverses();
              
              // Validate inverse properties
              assertNotNull(inverses);
              assertEquals(1, inverses.length);
              assertTrue(inverses[0] instanceof ExpectedInverseType);
          }
          
  CONDITIONAL_ROLLBACK_TESTS:
    PURPOSE: "Test rollback support based on operation type"
    EXAMPLE_ALTER_FILEFORMAT:
      RENAME_SUPPORTS_ROLLBACK:
        TEMPLATE: |
          @Test
          void shouldSupportRollbackForRename() {
              change.setOperationType("RENAME");
              change.setNewFileFormatName("NEW_NAME");
              
              assertTrue(change.supportsRollback(database));
          }
          
      SET_DOES_NOT_SUPPORT_ROLLBACK:
        TEMPLATE: |
          @Test
          void shouldNotSupportRollbackForSet() {
              change.setOperationType("SET");
              
              assertFalse(change.supportsRollback(database));
          }
          
  ROLLBACK_INTEGRATION_TESTS:
    PURPOSE: "Verify rollback executes successfully in database"
    DATABASE_LEVEL_VALIDATION:
      TEMPLATE: |
        def "${CHANGETYPE} rollback should execute successfully"() {
            given: "A ${OBJECT_TYPE} to modify"
            testContext.executeSql("CREATE ${OBJECT_TYPE} ${TEST_OBJECT_NAME}")
            
            and: "A ${CHANGETYPE} for operation"
            ${CHANGETYPE} change = new ${CHANGETYPE}()
            change.set${OBJECT_TYPE}Name("${TEST_OBJECT_NAME}")
            change.setNew${OBJECT_TYPE}Name("${NEW_OBJECT_NAME}")
            
            when: "Execute forward and rollback"
            testContext.executeSql(change.generateStatements(database))
            testContext.executeSql(change.createInverses()[0].generateStatements(database))
            
            then: "Original state restored"
            testContext.${OBJECT_TYPE}Exists("${TEST_OBJECT_NAME}")
            !testContext.${OBJECT_TYPE}Exists("${NEW_OBJECT_NAME}")
        }
    TEST_CONTEXT_EXPANSION:
      EXISTENCE_CHECKS:
        - "warehouseExists(String name)"
        - "databaseExists(String name)"
        - "fileFormatExists(String name)"
      HELPER_TEMPLATE: |
        boolean ${OBJECT_TYPE}Exists(String ${OBJECT_TYPE}Name) {
            try {
                String sql = "SHOW ${OBJECT_TYPE}S LIKE '${${OBJECT_TYPE}Name}'"
                def result = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement(sql))
                return !result.isEmpty()
            } catch (Exception e) {
                return false
            }
        }
    CLEANUP_STRATEGY:
      TEMPLATE: |
        cleanup:
        testContext.executeSql("DROP ${OBJECT_TYPE} IF EXISTS ${TEST_OBJECT_NAME}")
        testContext.executeSql("DROP ${OBJECT_TYPE} IF EXISTS ${NEW_OBJECT_NAME}")
    DATABASE_STATE_VALIDATION:
      TEMPLATE: |
        @Test
        void shouldExecuteRollbackSuccessfully() {
            // Create object
            executeSql(change.generateStatements(database));
            verifyObjectExists("TEST_OBJECT");
            
            // Execute rollback
            Change[] inverses = change.createInverses();
            executeSql(inverses[0].generateStatements(database));
            
            // Verify rollback succeeded
            verifyObjectDoesNotExist("TEST_OBJECT");
        }

ROLLBACK_IMPLEMENTATION_PITFALLS:
  NULL_DATABASE_ISSUE:
    PROBLEM: "Calling supportsRollback(null) in createInverses()"
    SOLUTION: "Use direct condition check instead"
    VALIDATION:
      TYPE: "NOT_CONTAINS_TEXT"
      TARGET: "${CHANGE_CLASS_FILE}"
      PATTERN: "supportsRollback(null)"
  INCONSISTENT_LOGIC:
    PROBLEM: "Different logic in supportsRollback() vs createInverses()"
    SOLUTION: "Extract condition to private method"
    TEMPLATE: |
      private boolean isRenameOperation() {
          return getNew${OBJECT_TYPE}Name() != null && !getNew${OBJECT_TYPE}Name().trim().isEmpty();
      }
  MISSING_IMPORTS:
    PROBLEM: "Change class not imported for createInverses() return type"
    VALIDATION:
      TYPE: "CONTAINS_TEXT"
      TARGET: "${CHANGE_CLASS_FILE}"
      PATTERN: "import liquibase.change.Change;"
  INCORRECT_TEST_ASSERTIONS:
    PROBLEM: "Existing tests assume no rollback support"
    SOLUTION: "Update test to assert rollback functionality"
    TEMPLATE: |
      @Test
      void shouldSupportRollbackByDefault() {
          change.set${OBJECT_TYPE}Name("TEST_${OBJECT_TYPE}");
          change.setNew${OBJECT_TYPE}Name("NEW_${OBJECT_TYPE}");
          assertTrue(change.supportsRollback(database));
      }

COMMON_TEST_FAILURE_PATTERNS:
  XSD_RESOLUTION_FAILURE:
    ERROR: "Unable to resolve xml entity"
    CAUSE: "liquibase-snowflake JAR not on test classpath"
    SOLUTION: "Verify dependency configuration and mvn clean install"
    
  SQL_FORMAT_MISMATCH:
    ERROR: "Expected SQL string doesn't match generated"
    CAUSE: "Incorrect expectations for Snowflake SQL format"
    SOLUTION: "Use format guidelines above for test expectations"
    
  WAREHOUSE_STATE_ISSUES:
    ERROR: "Warehouse suspended or unavailable"
    CAUSE: "Warehouses auto-suspend and need resuming"
    SOLUTION: "Add warehouse resume commands to test setup"
    
  TEST_ISOLATION_FAILURES:
    ERROR: "Tests pass individually but fail in suite"
    CAUSE: "Shared state between tests"
    SOLUTION: "Implement proper cleanup with unique object names"

DEBUGGING_COMMANDS:
  CHECK_DEPENDENCY: "mvn dependency:tree | grep liquibase-snowflake"
  VERIFY_CLASSPATH: "mvn test -X | grep 'liquibase-snowflake.*jar'"
  TEST_DATABASE_CONNECTION: "mvn test -Dtest=DatabaseConnectionTest -DdbName=snowflake"
  ISOLATED_TEST_RUN: "mvn test -Dtest=${SpecificTest} -DforkCount=1"
```

### STEP 5.4: MULTI_FORMAT_TESTING
```yaml
CREATE_YAML_TEST_FILES:
  YAML_CHANGELOG_TEMPLATE:
    CREATE_FILE:
      TARGET: "src/main/resources/liquibase/harness/change/changelogs/snowflake/${changetype}.yaml"
      TEMPLATE: |
        databaseChangeLog:
          - changeSet:
              id: "${changesetId}"
              author: "test-harness"
              changes:
                - ${changetype}:
                    ${attribute1}: "${value1}"
                    ${attribute2}: ${value2}
                    snowflake_${attribute}: "${snowflakeValue}"
                    
CREATE_JSON_TEST_FILES:
  JSON_CHANGELOG_TEMPLATE:
    CREATE_FILE:
      TARGET: "src/main/resources/liquibase/harness/change/changelogs/snowflake/${changetype}.json"
      TEMPLATE: |
        {
          "databaseChangeLog": [
            {
              "changeSet": {
                "id": "${changesetId}",
                "author": "test-harness",
                "changes": [
                  {
                    "${changetype}": {
                      "${attribute1}": "${value1}",
                      "${attribute2}": ${value2},
                      "snowflake_${attribute}": "${snowflakeValue}"
                    }
                  }
                ]
              }
            }
          ]
        }

VALIDATE_XML_NAMESPACE_SYNTAX:
  XML_NAMESPACE_TEMPLATE:
    CREATE_FILE:
      TARGET: "src/main/resources/liquibase/harness/change/changelogs/snowflake/${changetype}_xml.xml"
      TEMPLATE: |
        <?xml version="1.0" encoding="UTF-8"?>
        <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                          xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake">
          <changeSet id="${changesetId}" author="test-harness">
            <snowflake:${changetype} 
                ${attribute1}="${value1}"
                ${attribute2}="${value2}"
                ${attribute}="${snowflakeValue}"/>
          </changeSet>
        </databaseChangeLog>

EXECUTE_MULTI_FORMAT_TESTS:
  TEST_XML_FORMAT:
    COMMAND:
      EXECUTE: "mvn test -Dtest=ChangeObjectTests -DchangeObjects=${changetype} -DdbName=snowflake"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
      
  TEST_YAML_FORMAT:
    COMMAND:
      EXECUTE: "mvn test -Dtest=ChangeObjectTests -DchangeObjects=${changetype}_yaml -DdbName=snowflake"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"
      
  TEST_JSON_FORMAT:
    COMMAND:
      EXECUTE: "mvn test -Dtest=ChangeObjectTests -DchangeObjects=${changetype}_json -DdbName=snowflake"
      EXPECTED_RESULT: "BUILD SUCCESS"
      FAILURE_ACTION: "STOP"

VALIDATE_SQL_CONSISTENCY:
  COMPARE_XML_YAML_SQL:
    COMMAND:
      EXECUTE: "diff expected_sql_xml.sql expected_sql_yaml.sql"
      EXPECTED_RESULT: ""
      FAILURE_ACTION: "STOP"
      
  COMPARE_XML_JSON_SQL:
    COMMAND:
      EXECUTE: "diff expected_sql_xml.sql expected_sql_json.sql"
      EXPECTED_RESULT: ""
      FAILURE_ACTION: "STOP"

VALIDATE_PARSER_SUPPORT:
  CHECK_YAML_PARSER:
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "mvn test -Dtest=*YamlParserTest* -DtestFormats=yaml"
    FAILURE_ACTION: "STOP"
    
  CHECK_JSON_PARSER:
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "mvn test -Dtest=*JsonParserTest* -DtestFormats=json"
    FAILURE_ACTION: "STOP"
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
  - "Auto-rollback methods implemented where applicable"
FAILURE_ACTION: "STOP - Complete implementation with namespace pattern"
```

### CHECKPOINT_5: TEST_COVERAGE_VALIDATION
```yaml
CONDITION: "Complete test coverage implemented"
VALIDATION:
  - "Unit tests with complete string comparison"
  - "Integration tests for all SQL generation paths"
  - "Separate test files for mutually exclusive features"
  - "Rollback unit tests for all applicable changetypes"
  - "Multi-format testing (XML, YAML, JSON) implemented"
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

# Run rollback tests only
mvn test -Dtest="*Test" -Dtest.method="*rollback*"

# Run specific changetype rollback tests
mvn test -Dtest="*${ChangeType}*Test" -Dtest.method="*rollback*"

# Run full test suite
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Multi-format testing
mvn test -Dtest=ChangeObjectTests -DchangeObjects=${changetype},${changetype}_yaml,${changetype}_json -DdbName=snowflake
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
-  Rollback unit tests for all applicable changetypes
-  Multi-format testing (XML, YAML, JSON) validation
-  100% passing test suite

### ENHANCED_SUCCESS_CRITERIA

**For automated implementation completeness validation**: Use `aipl_programs/implementation-completeness-validation.yaml`

```yaml
ROLLBACK_COVERAGE:
  VALIDATE:
    NAME: "verify-rollback-decisions"
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "grep -c 'supportsRollback' src/main/java/liquibase/change/core/*Change.java"
    MINIMUM_COUNT: "${TOTAL_CHANGETYPES}"
  CONDITIONAL_ROLLBACK_TESTS:
    VALIDATE:
      NAME: "verify-conditional-rollback-tests"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/test/java/liquibase/change/core/*ChangeTest.java"
      PATTERN: "shouldSupportRollbackFor|shouldNotSupportRollbackWhen"
MULTI_FORMAT_COVERAGE:
  VALIDATE:
    NAME: "verify-multi-format-files"
    TYPE: "COMMAND_SUCCESS"
    COMMAND: "find /liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake -name '*.yaml' -o -name '*.json' | wc -l"
    MINIMUM_COUNT: "10"
  PRIORITY_COVERAGE:
    HIGH_PRIORITY_YAML: "80% of CREATE operations have YAML format"
    MEDIUM_PRIORITY_JSON: "50% of ALTER operations have JSON format"
INTEGRATION_TEST_COVERAGE:
  VALIDATE:
    NAME: "verify-rollback-integration-tests"
    TYPE: "CONTAINS_TEXT"
    TARGET: "src/test/groovy/liquibase/change/core/RollbackIntegrationTest.groovy"
    PATTERN: "rollback should execute successfully"
  DATABASE_VALIDATION:
    ROLLBACK_OPERATIONS: "All rollback-supporting changes tested with database"
    STATE_VERIFICATION: "Object existence checks implemented for all object types"
```

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

## DIFF_CHANGELOG_INTEGRATION_REFERENCE

### CHANGEGENERATOR_CRITICAL_INTEGRATION
```yaml
INTEGRATION_POINT: "ChangeGenerator classes convert diff results to changelog entries"
REGISTRATION_REQUIREMENT:
  FILE: "src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
  PATTERN: "Missing/Unexpected/Changed + ChangeGenerator for each object type"
  VALIDATION: "grep -c 'ChangeGenerator' src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
  
WORKFLOW_INTEGRATION:
  STEP_1: "Snapshot generators capture database state"
  STEP_2: "Diff comparators detect differences" 
  STEP_3: "ChangeGenerators convert differences to changetype entries"
  STEP_4: "Liquibase core commands output formatted changelog"
  
CROSS_REFERENCE_GUIDE: "DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md"
```

### SERVICE_REGISTRATION_COMPLETENESS
```yaml
REQUIRED_SERVICES_FOR_DIFF_INTEGRATION:
  CHANGETYPE_SERVICE: "liquibase.change.Change"
  SQL_GENERATOR_SERVICE: "liquibase.sqlgenerator.SqlGenerator"
  CHANGEGENERATOR_SERVICE: "liquibase.diff.output.changelog.ChangeGenerator"
  
VALIDATION_COMMANDS:
  CHECK_CHANGETYPE_REGISTRATION: "ls src/main/resources/META-INF/services/liquibase.change.Change"
  CHECK_CHANGEGENERATOR_REGISTRATION: "ls src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
  VERIFY_REGISTRATION_COUNT: "wc -l src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
```