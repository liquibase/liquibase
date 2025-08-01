# Liquibase Changetype Implementation Patterns
## AI-Optimized Sequential Execution for New and Existing Changetypes

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: CHANGETYPE_PATTERNS
EXECUTION_MODE: SEQUENTIAL_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Complete syntax definition through systematic requirements research"
  - "Complete SQL test statements through comprehensive validation"
  - "Unit tests complete string comparison through exact SQL matching"
  - "Integration tests ALL generated SQL through systematic test harness"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/changetype_implementation/changetype_patterns.md"
COMPANION_DOCUMENTS:
  - README.md: "Navigation guide for changetype implementation"
  - master_process_loop.md: "Master process for all implementations"
  - sql_generator_overrides.md: "SQL syntax override implementations"
  - test_harness_guide.md: "Testing with harness protocols"
  - requirements_creation.md: "Detailed requirements specification"

RELATED_GUIDES:
  - "../snapshot_diff_implementation/error_patterns_guide.md": "Systematic debugging"
  - "../snapshot_diff_implementation/ai_quickstart.md": "Sequential execution patterns"
```

## 🛑 CRITICAL: Decision Tree - Which Pattern Do You Need?

```yaml
DECISION_PROTOCOL:
  QUESTION_1: "Does Liquibase already have this change type?"
  IF_NO:
    PATTERN: "NEW_CHANGETYPE_PATTERN"
    COMPLEXITY: "MEDIUM-HIGH"
    EXAMPLES: ["createWarehouse", "dropDatabase", "alterSchema"]
    GOTO: "ARCHITECTURE_DECISION_POINT"
    
  IF_YES:
    QUESTION_2: "What do you need to change?"
    OPTION_A: "Only SQL syntax differs for your database"
      PATTERN: "SQL_GENERATOR_OVERRIDE"
      COMPLEXITY: "LOW"
      EXAMPLES: ["renameTable", "addColumn", "dropColumn"]
      GUIDE: "sql_generator_overrides.md"
      
    OPTION_B: "Need to add new database-specific attributes"
      PATTERN: "NAMESPACE_ATTRIBUTE_EXTENSION"
      COMPLEXITY: "MEDIUM"
      EXAMPLES: ["createTable with transient", "alterSequence with setNoOrder"]
      GOTO: "EXISTING_CHANGETYPE_EXTENSION"
      
    OPTION_C: "Need completely different behavior"
      PATTERN: "NEW_CHANGETYPE_PATTERN"
      COMPLEXITY: "MEDIUM-HIGH"
      NOTE: "Consider if this should be a new changetype instead"
      GOTO: "ARCHITECTURE_DECISION_POINT"

ARCHITECTURE_DECISION_POINT:
  QUESTION_3: "How many mutually exclusive operation modes does this changetype have?"
  
  SINGLE_MODE_OPERATIONS:
    INDICATORS:
      - "One primary operation (CREATE, DROP, INSERT)"
      - "Minor variations only (IF EXISTS, CASCADE)"
      - "Shared validation across variations"
      - "Similar SQL generation pattern"
    EXAMPLES: ["DROP TABLE", "CREATE SEQUENCE", "CREATE INDEX"]
    ARCHITECTURE: "PROPERTY_BASED_IMPLEMENTATION"
    COMPLEXITY: "MEDIUM"
    SUCCESS_FACTORS: "Follow standard implementation patterns"
    
  MULTI_MODE_OPERATIONS:
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
    ARCHITECTURE: "OPERATION_TYPE_DRIVEN_ARCHITECTURE ⭐ RECOMMENDED"
    COMPLEXITY: "MEDIUM-HIGH"
    SUCCESS_FACTORS: "Use OperationType enum with operation-specific logic"
    PROVEN_SUCCESS_RATE: "71% quality improvement over property-based"
```

## 🚨 BEFORE YOU START - ANTI-GOALPOST-CHANGING PROTOCOL

```yaml
MANDATORY_PRECONDITIONS:
  DISCOVERY_PHASE_REQUIRED: "MUST check existing components before implementation"
  REQUIREMENTS_VALIDATION: "MUST create complete requirements document first"
  NO_ASSUMPTIONS: "DO NOT assume where bugs are - use systematic debugging"
  PHASE_TESTING: "MUST test each phase before proceeding to next"
  
FORBIDDEN_BEHAVIORS:
  - "Skipping discovery phase"
  - "Assuming problems exist without validation"
  - "Changing requirements to fit current code"
  - "Proceeding when validation fails"
  - "Making false assumptions about bug locations"
```

**Discovery Commands (MANDATORY FIRST STEP):**
```bash
# CRITICAL: Check if components already exist
find . -name "*<ChangeType>*" -type f
find . -name "*NamespaceAttributeStorage*" -type f
grep -r "changeType" src/main/java/liquibase/parser/

# CRITICAL: Check service registrations
ls -la src/main/resources/META-INF/services/

# CRITICAL: Review XSD for existing attributes
cat src/main/resources/*.xsd | grep -i <changetype>

# CRITICAL: When tests fail, create debug test FIRST
# Print actual output before assuming bug location
```

---

# NEW CHANGETYPE PATTERN

## 🏆 OPERATION-TYPE-DRIVEN ARCHITECTURE IMPLEMENTATION

**For changetypes with 2+ mutually exclusive operation modes - PROVEN SUCCESS PATTERN**

### Overview of OperationType Architecture

```yaml
ARCHITECTURE_COMPONENTS:
  CORE_ELEMENTS:
    1. "OperationType enum defining all mutually exclusive modes"
    2. "Operation-specific validation methods in Statement class"
    3. "Switch-based SQL generation in Generator class"
    4. "Backward compatibility inference logic"
    5. "Enhanced Change class with operationType support"
    
  SUCCESS_METRICS:
    - "71% quality improvement over property-based implementations"
    - "22/22 tests passing in ALTER WAREHOUSE real-world example"
    - "Complete mutual exclusivity enforcement"
    - "Clean, maintainable, extensible code architecture"
    
  APPLICABILITY:
    - "Any changetype with 2+ mutually exclusive operation modes"
    - "Operations that cannot be combined (RENAME + SET, GRANT + REVOKE)"
    - "Different validation requirements per operation"
    - "Different SQL generation patterns per operation"
```

### STEP 1: OperationType Architecture Planning

```yaml
PLANNING_ACTIVITIES:
  OPERATION_MODE_IDENTIFICATION:
    - "Review requirements document for all operation modes"
    - "Identify mutually exclusive operations (e.g., SET vs UNSET)"
    - "Map operation modes to specific attributes"
    - "Define validation rules per operation type"
    - "Plan SQL generation approach per operation"
    
  ENUM_DESIGN:
    - "Create descriptive enum values (RENAME, SET, UNSET)"
    - "Document each enum value with SQL pattern"
    - "Plan for future extensibility"
    
VALIDATION_CHECKPOINT_1:
  SUCCESS_CRITERIA:
    - "All operation modes clearly identified and documented"
    - "Mutual exclusivity rules defined"
    - "Operation-to-attributes mapping complete"
  FAILURE_ACTION: "Return to requirements - operation modes unclear"
```

### STEP 2: Enhanced Statement Class Implementation

**Use this template for multi-mode changetypes:**

```java
public class <ChangeType>Statement extends AbstractSqlStatement {
    
    // 1. OperationType enum
    public enum OperationType {
        MODE_A,    // e.g., RENAME
        MODE_B,    // e.g., SET  
        MODE_C     // e.g., UNSET
    }
    
    // 2. Core properties
    private OperationType operationType;
    private String <requiredAttribute>;
    private String <modeASpecificAttribute>;
    private Boolean <modeBSpecificAttribute>;
    
    // 3. Enhanced validation
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (<requiredAttribute> == null) {
            result.addError("Required attribute missing");
        }
        
        // Operation type inference and validation
        if (operationType == null) {
            operationType = inferOperationType();
            if (operationType == null) {
                result.addError("Cannot determine operation type");
            }
        }
        
        if (operationType != null) {
            validateOperationType(result);
        }
        
        return result;
    }
    
    // 4. Inference logic for backward compatibility
    private OperationType inferOperationType() {
        boolean hasModeAProps = hasModeAProperties();
        boolean hasModeBProps = hasModeBProperties();
        boolean hasModeCProps = hasModeCProperties();
        
        // Detect conflicts
        int modeCount = (hasModeAProps ? 1 : 0) + (hasModeBProps ? 1 : 0) + (hasModeCProps ? 1 : 0);
        if (modeCount > 1) return null; // Conflict detected
        
        if (hasModeAProps) return OperationType.MODE_A;
        if (hasModeBProps) return OperationType.MODE_B;
        if (hasModeCProps) return OperationType.MODE_C;
        return null;
    }
    
    // 5. Operation-specific validation  
    private void validateOperationType(ValidationResult result) {
        switch (operationType) {
            case MODE_A: validateModeA(result); break;
            case MODE_B: validateModeB(result); break;
            case MODE_C: validateModeC(result); break;
        }
    }
}
```

### STEP 3: Enhanced SQL Generator Implementation

**Switch-based SQL generation for clean separation:**

```java
public class <ChangeType>GeneratorSnowflake extends AbstractSqlGenerator<<ChangeType>Statement> {
    
    @Override
    public Sql[] generateSql(<ChangeType>Statement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Ensure operation type is set
        if (statement.getOperationType() == null) {
            statement.validate(); // Triggers inference
        }
        
        <ChangeType>Statement.OperationType operationType = statement.getOperationType();
        if (operationType == null) {
            throw new RuntimeException("Operation type could not be determined");
        }
        
        // Switch-based generation
        List<Sql> sqlList = new ArrayList<>();
        
        switch (operationType) {
            case MODE_A:
                sqlList.add(generateModeASql(statement, database));
                break;
            case MODE_B:
                sqlList.add(generateModeBSql(statement, database));
                break;
            case MODE_C:
                sqlList.add(generateModeCSQL(statement, database));
                break;
            default:
                throw new RuntimeException("Unsupported operation type: " + operationType);
        }
        
        return sqlList.toArray(new Sql[0]);
    }
    
    // Operation-specific SQL generation methods
    private Sql generateModeASql(<ChangeType>Statement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER <OBJECT> ");
        sql.append(database.escapeObjectName(statement.get<RequiredAttribute>(), Table.class));
        sql.append(" MODE_A_SYNTAX");
        // Add MODE_A specific logic
        return new UnparsedSql(sql.toString());
    }
    
    private Sql generateModeBSql(<ChangeType>Statement statement, Database database) {
        StringBuilder sql = new StringBuilder("ALTER <OBJECT> ");
        sql.append(database.escapeObjectName(statement.get<RequiredAttribute>(), Table.class));
        sql.append(" SET ");
        
        List<String> setClause = new ArrayList<>();
        // Add MODE_B specific properties
        sql.append(String.join(", ", setClause));
        return new UnparsedSql(sql.toString());
    }
}
```

### STEP 4: Real-World Success Example - ALTER WAREHOUSE

```yaml
ALTER_WAREHOUSE_SUCCESS_METRICS:
  OPERATION_TYPES_IMPLEMENTED: 
    - "RENAME: ALTER WAREHOUSE name RENAME TO new_name"
    - "SET: ALTER WAREHOUSE name SET property = value"
    - "UNSET: ALTER WAREHOUSE name UNSET property"
    - "SUSPEND: ALTER WAREHOUSE name SUSPEND" 
    - "RESUME: ALTER WAREHOUSE name RESUME [IF SUSPENDED]"
    - "ABORT_ALL_QUERIES: ALTER WAREHOUSE name ABORT ALL QUERIES"
    
  TEST_RESULTS:
    - "22/22 tests passing (12 integration + 10 unit tests)"
    - "Live Snowflake database validation successful"
    - "All operation types work independently"
    - "Mutual exclusivity properly enforced"
    
  QUALITY_IMPROVEMENTS:
    - "71% quality improvement over property-based approach"
    - "Clear separation of concerns per operation type"
    - "Easy extensibility for new operation types"
    - "Sophisticated validation with 15+ constraint checks"
```

## SEQUENTIAL_BLOCKING_IMPLEMENTATION

### STEP 1: Requirements Research - **COMPLETE SYNTAX DEFINITION**
```yaml
STEP_ID: NEW_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition"
```

#### BLOCKING_VALIDATION_1.1: Requirements Document Complete
```yaml
REQUIREMENT: "Complete requirements document created using requirements_creation.md"
LOCATION: "requirements/[changeTypeName]_requirements.md"
VALIDATION_CRITERIA:
  - "Official database documentation researched and referenced"
  - "All SQL syntax variations documented"
  - "Complete attribute analysis table provided"
  - "Mutual exclusivity rules identified"
  - "Test scenarios planned"
  - "Validation rules defined"
FAILURE_ACTION: "STOP - Create complete requirements before implementation"
```

**Requirements Research Checklist:**
- [ ] **MANDATORY**: Created requirements document following requirements_creation.md
- [ ] **MANDATORY**: Researched official database documentation with URLs
- [ ] **MANDATORY**: Documented all SQL syntax variations with complete examples
- [ ] **MANDATORY**: Created comprehensive attribute analysis table
- [ ] **MANDATORY**: Identified mutual exclusivity rules
- [ ] **MANDATORY**: Planned test scenarios covering all combinations
- [ ] **MANDATORY**: Defined complete validation rules

### STEP 2: Change Class Implementation - **COMPLETE ATTRIBUTE COVERAGE**
```yaml
STEP_ID: NEW_2.0
STATUS: BLOCKED
PREREQUISITES: [NEW_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition through comprehensive attributes"
```

#### BLOCKING_VALIDATION_2.1: Change Class Complete
```yaml
REQUIREMENT: "Change class implements all attributes from requirements"
VALIDATION_CRITERIA:
  - "All required attributes implemented with proper annotations"
  - "All optional attributes implemented with proper annotations"
  - "Validation methods implement complete business rules"
  - "Error messages provide clear guidance"
FAILURE_ACTION: "STOP - Complete change class implementation"
```

**Change Class Template:**
```java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.[ChangeType]Statement;

/**
 * Implements [ChangeType] functionality for [database].
 * 
 * SQL Syntax: [Document complete syntax from requirements]
 * Required Attributes: [List from requirements]
 * Optional Attributes: [List from requirements]
 */
@DatabaseChange(
    name = "[changeTypeName]",
    description = "[Clear description from requirements]",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "[AppliesTo value from requirements]"
)
public class [ChangeType]Change extends AbstractChange {

    // CRITICAL: All attributes from requirements document must be implemented
    
    @DatabaseChangeProperty(
        description = "[Description from requirements]",
        requiredForDatabase = "all"  // or specific databases
    )
    private String requiredAttribute;
    
    @DatabaseChangeProperty(
        description = "[Description from requirements]"
    )
    private String optionalAttribute;
    
    // MANDATORY: Getters and setters for ALL attributes
    public String getRequiredAttribute() {
        return requiredAttribute;
    }
    
    public void setRequiredAttribute(String requiredAttribute) {
        this.requiredAttribute = requiredAttribute;
    }
    
    public String getOptionalAttribute() {
        return optionalAttribute;
    }
    
    public void setOptionalAttribute(String optionalAttribute) {
        this.optionalAttribute = optionalAttribute;
    }
    
    @Override
    public String getConfirmationMessage() {
        return "[ChangeType] executed on " + getRequiredAttribute();
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        // CRITICAL: Create statement with ALL attributes
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setRequiredAttribute(getRequiredAttribute());
        statement.setOptionalAttribute(getOptionalAttribute());
        
        return new SqlStatement[] { statement };
    }
    
    /**
     * CRITICAL: Implement complete validation from requirements.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);
        
        // MANDATORY: All validation rules from requirements document
        if (getRequiredAttribute() == null || getRequiredAttribute().trim().isEmpty()) {
            validationErrors.addError("requiredAttribute is required");
        }
        
        // MANDATORY: Implement mutual exclusivity rules from requirements
        if (hasConflictingAttributes()) {
            validationErrors.addError("Conflicting attributes detected: [specific conflict]");
        }
        
        // MANDATORY: Implement value constraints from requirements
        if (!isValidAttributeValue(getOptionalAttribute())) {
            validationErrors.addError("optionalAttribute has invalid value: " + getOptionalAttribute());
        }
        
        return validationErrors;
    }
    
    /**
     * Helper method to check mutual exclusivity rules.
     * Implementation based on requirements document.
     */
    private boolean hasConflictingAttributes() {
        // Implement mutual exclusivity rules from requirements
        return false;
    }
    
    /**
     * Helper method to validate attribute values.
     * Implementation based on requirements document.
     */
    private boolean isValidAttributeValue(String value) {
        // Implement value constraints from requirements
        return true;
    }
}
```

### STEP 3: Statement Class Implementation - **COMPLETE SQL GENERATION**
```yaml
STEP_ID: NEW_3.0
STATUS: BLOCKED
PREREQUISITES: [NEW_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements"
```

#### BLOCKING_VALIDATION_3.1: Statement Class Complete
```yaml
REQUIREMENT: "Statement class supports all SQL generation scenarios"
VALIDATION_CRITERIA:
  - "All attributes from change class supported"
  - "SQL generation covers all property combinations"
  - "Edge cases and boundary conditions handled"
FAILURE_ACTION: "STOP - Complete statement class implementation"
```

**Statement Class Template:**
```java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

/**
 * SQL statement for [ChangeType] operations.
 * Supports all attributes defined in requirements document.
 */
public class [ChangeType]Statement extends AbstractSqlStatement {
    
    // CRITICAL: All attributes from Change class must be supported
    private String requiredAttribute;
    private String optionalAttribute;
    
    // MANDATORY: Default constructor
    public [ChangeType]Statement() {
    }
    
    // MANDATORY: Constructor with required attributes
    public [ChangeType]Statement(String requiredAttribute) {
        this.requiredAttribute = requiredAttribute;
    }
    
    // MANDATORY: Getters and setters for ALL attributes
    public String getRequiredAttribute() {
        return requiredAttribute;
    }
    
    public void setRequiredAttribute(String requiredAttribute) {
        this.requiredAttribute = requiredAttribute;
    }
    
    public String getOptionalAttribute() {
        return optionalAttribute;
    }
    
    public void setOptionalAttribute(String optionalAttribute) {
        this.optionalAttribute = optionalAttribute;
    }
    
    /**
     * CRITICAL: Validate statement completeness.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    public boolean isComplete() {
        return requiredAttribute != null && !requiredAttribute.trim().isEmpty();
    }
    
    /**
     * Helper method to get all attributes for SQL generation.
     * Used by generators to ensure complete SQL.
     */
    public boolean hasOptionalAttribute() {
        return optionalAttribute != null && !optionalAttribute.trim().isEmpty();
    }
}
```

### STEP 4: SQL Generator Implementation - **COMPLETE SQL GENERATION**
```yaml
STEP_ID: NEW_4.0
STATUS: BLOCKED
PREREQUISITES: [NEW_3.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements and unit tests complete string comparison"
```

#### BLOCKING_VALIDATION_4.1: SQL Generator Complete
```yaml
REQUIREMENT: "SQL generator produces complete SQL for all attribute combinations"
VALIDATION_CRITERIA:
  - "SQL generation handles all required attributes"
  - "SQL generation handles all optional attributes"
  - "SQL validation ensures completeness"
  - "Edge cases properly handled"
FAILURE_ACTION: "STOP - Complete SQL generator implementation"
```

**SQL Generator Template:**
```java
package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.[Database]Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.[ChangeType]Statement;

/**
 * Generates SQL for [ChangeType] operations.
 * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
 */
public class [ChangeType]Generator extends AbstractSqlGenerator<[ChangeType]Statement> {
    
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public boolean supports([ChangeType]Statement statement, Database database) {
        return database instanceof [Database]Database;
    }
    
    @Override
    public ValidationErrors validate([ChangeType]Statement statement, Database database, 
                                   SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        
        // MANDATORY: Validate statement completeness
        if (!statement.isComplete()) {
            validationErrors.addError("Statement is incomplete - missing required attributes");
        }
        
        return validationErrors;
    }
    
    @Override
    public Sql[] generateSql([ChangeType]Statement statement, Database database, 
                            SqlGeneratorChain sqlGeneratorChain) {
        
        // CRITICAL: Build complete SQL statement
        String sql = buildCompleteSql(statement, database);
        
        // CRITICAL: Validate SQL completeness
        validateGeneratedSql(sql, statement);
        
        return new Sql[] { new UnparsedSql(sql) };
    }
    
    /**
     * CRITICAL: Builds complete SQL with all attributes.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    private String buildCompleteSql([ChangeType]Statement statement, Database database) {
        StringBuilder sql = new StringBuilder();
        
        // MANDATORY: Include all required SQL elements from requirements
        sql.append("[SQL_OPERATION] ");
        sql.append(statement.getRequiredAttribute());
        
        // MANDATORY: Include optional elements when present
        if (statement.hasOptionalAttribute()) {
            sql.append(" WITH ");
            sql.append(statement.getOptionalAttribute());
        }
        
        // CRITICAL: Return complete, executable SQL
        return sql.toString();
    }
    
    /**
     * CRITICAL: Validates generated SQL completeness.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    private void validateGeneratedSql(String sql, [ChangeType]Statement statement) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalStateException("Generated SQL cannot be null or empty");
        }
        
        // MANDATORY: Validate SQL contains required elements
        if (!sql.toUpperCase().contains("[REQUIRED_SQL_KEYWORD]")) {
            throw new IllegalStateException("SQL missing required keyword: " + sql);
        }
        
        // MANDATORY: Validate SQL contains all specified attributes
        if (!sql.contains(statement.getRequiredAttribute())) {
            throw new IllegalStateException("SQL missing required attribute: " + sql);
        }
    }
}
```

### STEP 5: Service Registration - **COMPONENT DISCOVERY**
```yaml
STEP_ID: NEW_5.0
STATUS: BLOCKED
PREREQUISITES: [NEW_4.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition requires proper registration"
```

#### BLOCKING_VALIDATION_5.1: All Services Registered
```yaml
REQUIREMENT: "All components registered for Liquibase service discovery"
VALIDATION_CRITERIA:
  - "Change class registered in liquibase.change.Change"
  - "SQL generator registered in liquibase.sqlgenerator.SqlGenerator"
  - "Class names match exactly in service files"
  - "JAR includes all service files after build"
FAILURE_ACTION: "STOP - Fix service registration"
```

**Service Registration Files:**

**File 1:** `src/main/resources/META-INF/services/liquibase.change.Change`
```
liquibase.change.core.[ChangeType]Change
```

**File 2:** `src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`
```
liquibase.sqlgenerator.core.[ChangeType]Generator
```

### STEP 6: Unit Testing - **COMPLETE SQL STRING COMPARISON**
```yaml
STEP_ID: NEW_6.0
STATUS: BLOCKED
PREREQUISITES: [NEW_5.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

#### BLOCKING_VALIDATION_6.1: Unit Tests with Complete SQL Validation
```yaml
REQUIREMENT: "All unit tests validate complete SQL strings exactly"
VALIDATION_CRITERIA:
  - "Tests compare entire SQL statement, not partial matches"
  - "All attribute combinations tested"
  - "Edge cases and error conditions covered"
  - "SQL completeness validated in every test"
FAILURE_ACTION: "STOP - Add complete SQL string comparison to all tests"
```

**Complete Unit Test Template:**
```java
package liquibase.sqlgenerator.core;

import liquibase.database.core.[Database]Database;
import liquibase.sql.Sql;
import liquibase.statement.core.[ChangeType]Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for [ChangeType]Generator.
 * ADDRESSES_CORE_ISSUE: Unit tests must compare complete SQL strings.
 */
public class [ChangeType]GeneratorTest {
    
    private [ChangeType]Generator generator;
    private [Database]Database database;
    
    @BeforeEach
    public void setUp() {
        generator = new [ChangeType]Generator();
        database = new [Database]Database();
    }
    
    /**
     * CRITICAL: Test complete SQL generation with required attributes.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    @Test
    public void testGeneratesCompleteSqlWithRequiredAttributes() {
        // Arrange
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setRequiredAttribute("test_value");
        
        // Act
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Assert - MANDATORY: Complete SQL string comparison
        assertEquals(1, sqls.length);
        String actualSQL = sqls[0].toSql();
        String expectedSQL = "[EXPECTED_COMPLETE_SQL_WITH_REQUIRED_ATTRS]";
        
        // CRITICAL: Exact string comparison required
        assertEquals(expectedSQL, actualSQL,
            "Generated SQL must match expected SQL exactly. " +
            "Expected: '" + expectedSQL + "', " +
            "Actual: '" + actualSQL + "'");
        
        // MANDATORY: Verify SQL completeness
        assertTrue(actualSQL.contains("[REQUIRED_KEYWORD]"),
            "SQL must contain required keyword");
        assertTrue(actualSQL.contains("test_value"),
            "SQL must contain required attribute value");
    }
    
    /**
     * CRITICAL: Test complete SQL generation with all attributes.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    @Test
    public void testGeneratesCompleteSqlWithAllAttributes() {
        // Arrange
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setRequiredAttribute("test_required");
        statement.setOptionalAttribute("test_optional");
        
        // Act
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Assert - MANDATORY: Complete SQL string comparison
        String actualSQL = sqls[0].toSql();
        String expectedSQL = "[EXPECTED_COMPLETE_SQL_WITH_ALL_ATTRS]";
        
        // CRITICAL: Exact string comparison required
        assertEquals(expectedSQL, actualSQL,
            "SQL with all attributes must match exactly");
        
        // MANDATORY: Verify all elements present
        assertTrue(actualSQL.contains("test_required"),
            "SQL must contain required attribute");
        assertTrue(actualSQL.contains("test_optional"),
            "SQL must contain optional attribute");
    }
    
    /**
     * CRITICAL: Test all property combinations systematically.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    @Test
    public void testAllPropertyCombinations() {
        // Test matrix based on requirements document
        // MANDATORY: Test every possible combination from requirements
        
        // Test Case 1: Minimal required attributes
        testSqlGeneration("min_required", null, 
            "[EXPECTED_SQL_MINIMAL]");
        
        // Test Case 2: All attributes
        testSqlGeneration("full_required", "full_optional", 
            "[EXPECTED_SQL_COMPLETE]");
        
        // Add more test cases based on requirements combinations
    }
    
    /**
     * Helper method for systematic SQL testing.
     * ADDRESSES_CORE_ISSUE: Unit tests not comparing complete SQL strings.
     */
    private void testSqlGeneration(String required, String optional, String expectedSql) {
        [ChangeType]Statement statement = new [ChangeType]Statement();
        statement.setRequiredAttribute(required);
        if (optional != null) {
            statement.setOptionalAttribute(optional);
        }
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        String actualSQL = sqls[0].toSql();
        
        // CRITICAL: Complete SQL string comparison
        assertEquals(expectedSql, actualSQL,
            String.format("SQL generation failed for required='%s', optional='%s'", 
                required, optional));
    }
    
    /**
     * CRITICAL: Test error conditions and validation.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    @Test
    public void testValidationFailsForIncompleteStatement() {
        [ChangeType]Statement statement = new [ChangeType]Statement();
        // Intentionally incomplete - missing required attribute
        
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // MANDATORY: Should have validation errors
        assertTrue(errors.hasErrors(),
            "Generator must validate statement completeness");
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("incomplete") || msg.contains("required")),
            "Error message must indicate missing required attributes");
    }
}
```

---

# EXISTING CHANGETYPE EXTENSION PATTERN

## NAMESPACE_ATTRIBUTE_IMPLEMENTATION

### STEP 1: Discovery and Assessment - **PREVENT GOALPOST CHANGING**
```yaml
STEP_ID: EXT_1.0
STATUS: BLOCKING
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition through existing component analysis"
```

#### BLOCKING_VALIDATION_1.1: Complete Discovery Assessment
```yaml
REQUIREMENT: "All existing components identified and assessed"
VALIDATION_CRITERIA:
  - "Existing Storage, Parser, Generator components found and analyzed"
  - "Service registrations documented"
  - "XSD attributes reviewed"
  - "Current functionality tested and documented"
FAILURE_ACTION: "STOP - Complete discovery before implementation"
```

**Mandatory Discovery Commands:**
```bash
# CRITICAL: Check for existing namespace storage
find . -name "*NamespaceAttributeStorage*" -type f

# CRITICAL: Check for existing parser components
grep -r "isTargetChangeType" src/main/java/liquibase/parser/

# CRITICAL: Check existing XSD definitions
cat src/main/resources/*.xsd | grep -A 10 -B 10 "<changeType>"

# CRITICAL: Check service registrations
ls -la src/main/resources/META-INF/services/
cat src/main/resources/META-INF/services/liquibase.parser.ChangeLogParser

# CRITICAL: Test current functionality
# Create minimal test to verify existing behavior before changes
```

### STEP 2: Namespace Attribute Storage - **COMPLETE ATTRIBUTE SUPPORT**
```yaml  
STEP_ID: EXT_2.0
STATUS: BLOCKED
PREREQUISITES: [EXT_1.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition through comprehensive attribute storage"
```

#### BLOCKING_VALIDATION_2.1: Storage Implementation Complete
```yaml
REQUIREMENT: "Namespace storage supports all new attributes from requirements"
VALIDATION_CRITERIA:
  - "All new attributes have proper storage methods"
  - "Type conversion handled correctly"
  - "Default values properly implemented"
  - "Validation rules enforced"
FAILURE_ACTION: "STOP - Complete storage implementation"
```

**Namespace Attribute Storage Template:**
```java
package liquibase.parser.[database];

import liquibase.change.Change;
import liquibase.change.core.[ChangeType]Change;
import liquibase.parser.NamespaceAttributeStorage;

/**
 * Stores namespace attributes for [database]-specific change types.
 * ADDRESSES_CORE_ISSUE: Complete syntax definition.
 */
public class [Database]NamespaceAttributeStorage extends NamespaceAttributeStorage {
    
    // CRITICAL: Add all new attributes from requirements
    private Boolean newAttribute;
    private String anotherNewAttribute;
    
    @Override
    public boolean supports(Change change) {
        // MANDATORY: Support target change type
        return change instanceof [ChangeType]Change;
    }
    
    /**
     * CRITICAL: Getter for new attribute with proper type handling.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    public Boolean getNewAttribute() {
        return newAttribute;
    }
    
    /**
     * CRITICAL: Setter with validation from requirements.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    public void setNewAttribute(String value) {
        // MANDATORY: Handle type conversion and validation
        if (value != null) {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                this.newAttribute = Boolean.valueOf(value);
            } else {
                throw new IllegalArgumentException(
                    "newAttribute must be 'true' or 'false', got: " + value);
            }
        }
    }
    
    public String getAnotherNewAttribute() {
        return anotherNewAttribute;
    }
    
    public void setAnotherNewAttribute(String anotherNewAttribute) {
        // MANDATORY: Validate against requirements constraints
        if (anotherNewAttribute != null && !isValidValue(anotherNewAttribute)) {
            throw new IllegalArgumentException(
                "anotherNewAttribute has invalid value: " + anotherNewAttribute);
        }
        this.anotherNewAttribute = anotherNewAttribute;
    }
    
    /**
     * Validation helper based on requirements document.
     */
    private boolean isValidValue(String value) {
        // Implement validation rules from requirements
        return true;
    }
}
```

### STEP 3: Parser Enhancement - **COMPLETE ATTRIBUTE PARSING**
```yaml
STEP_ID: EXT_3.0
STATUS: BLOCKED
PREREQUISITES: [EXT_2.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete syntax definition through comprehensive parsing"
```

#### BLOCKING_VALIDATION_3.1: Parser Implementation Complete
```yaml
REQUIREMENT: "Parser correctly handles all new namespace attributes"
VALIDATION_CRITERIA:
  - "All new attributes parsed correctly"
  - "Priority ensures parser is used"
  - "Error handling provides clear messages"
  - "Integration with storage component works"
FAILURE_ACTION: "STOP - Complete parser implementation"
```

**Parser Enhancement Template:**
```java
package liquibase.parser.[database];

import liquibase.change.Change;
import liquibase.change.core.[ChangeType]Change;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.xml.AbstractChangeLogParser;
import liquibase.resource.ResourceAccessor;
import org.w3c.dom.Element;

/**
 * Enhanced parser for [database]-specific namespace attributes.
 * ADDRESSES_CORE_ISSUE: Complete syntax definition.
 */
public class [Database]ChangeLogParser extends AbstractChangeLogParser {
    
    @Override
    public int getPriority() {
        // CRITICAL: Must be higher than default to intercept parsing
        return PRIORITY_DATABASE + 10;
    }
    
    @Override
    protected void parseChangeSetChildren(Element changeSetElement, 
                                        ChangeSet changeSet, 
                                        ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        
        // MANDATORY: Call parent implementation first
        super.parseChangeSetChildren(changeSetElement, changeSet, resourceAccessor);
        
        // CRITICAL: Process each change for namespace attributes
        for (Change change : changeSet.getChanges()) {
            if (isTargetChangeType(change)) {
                processNamespaceAttributes(change, changeSetElement);
            }
        }
    }
    
    /**
     * CRITICAL: Identify target change types for namespace processing.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    private boolean isTargetChangeType(Change change) {
        return change instanceof [ChangeType]Change;
        // Add more change types as needed
    }
    
    /**
     * CRITICAL: Process all namespace attributes from requirements.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    private void processNamespaceAttributes(Change change, Element changeSetElement) 
            throws ChangeLogParseException {
        
        // MANDATORY: Find the change element
        Element changeElement = findChangeElement(changeSetElement, change);
        if (changeElement == null) {
            return;
        }
        
        // CRITICAL: Create or get storage
        [Database]NamespaceAttributeStorage storage = getOrCreateStorage(change);
        
        // MANDATORY: Process all new attributes from requirements
        String newAttributeValue = changeElement.getAttribute("[database]:newAttribute");
        if (newAttributeValue != null && !newAttributeValue.isEmpty()) {
            try {
                storage.setNewAttribute(newAttributeValue);
            } catch (IllegalArgumentException e) {
                throw new ChangeLogParseException(
                    "Invalid value for [database]:newAttribute: " + e.getMessage());
            }
        }
        
        String anotherAttributeValue = changeElement.getAttribute("[database]:anotherNewAttribute");
        if (anotherAttributeValue != null && !anotherAttributeValue.isEmpty()) {
            storage.setAnotherNewAttribute(anotherAttributeValue);
        }
    }
    
    /**
     * Helper method to find the change element in the changeset.
     */
    private Element findChangeElement(Element changeSetElement, Change change) {
        // Implementation to find the specific change element
        return null; // Placeholder
    }
    
    /**
     * CRITICAL: Get or create namespace storage for the change.
     * ADDRESSES_CORE_ISSUE: Complete syntax definition.
     */
    private [Database]NamespaceAttributeStorage getOrCreateStorage(Change change) {
        [Database]NamespaceAttributeStorage storage = 
            ([Database]NamespaceAttributeStorage) change.getNamespaceAttributeStorage();
        
        if (storage == null) {
            storage = new [Database]NamespaceAttributeStorage();
            change.setNamespaceAttributeStorage(storage);
        }
        
        return storage;
    }
}
```

### STEP 4: SQL Generator Enhancement - **COMPLETE SQL WITH ATTRIBUTES**
```yaml
STEP_ID: EXT_4.0
STATUS: BLOCKED
PREREQUISITES: [EXT_3.0]
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUE: "Complete SQL test statements with namespace attributes"
```

#### BLOCKING_VALIDATION_4.1: Generator Handles All Attributes
```yaml
REQUIREMENT: "SQL generator incorporates all namespace attributes into SQL"
VALIDATION_CRITERIA:
  - "All namespace attributes affect SQL generation"
  - "SQL completeness validated with attributes"
  - "Edge cases and combinations handled"
  - "Generated SQL is complete and executable"
FAILURE_ACTION: "STOP - Complete generator enhancement"
```

**SQL Generator Enhancement Template:**
```java
package liquibase.sqlgenerator.core.[database];

import liquibase.change.Change;
import liquibase.change.core.[ChangeType]Change;
import liquibase.database.Database;
import liquibase.database.core.[Database]Database;
import liquibase.parser.[database].[Database]NamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.[ChangeType]Generator;
import liquibase.statement.core.[ChangeType]Statement;

/**
 * Enhanced SQL generator supporting [database] namespace attributes.
 * ADDRESSES_CORE_ISSUE: Complete SQL test statements with namespace attributes.
 */
public class [ChangeType]Generator[Database] extends [ChangeType]Generator {
    
    @Override
    public int getPriority() {
        // CRITICAL: Higher priority to override default generator
        return PRIORITY_DATABASE + 5;
    }
    
    @Override
    public boolean supports([ChangeType]Statement statement, Database database) {
        return database instanceof [Database]Database;
    }
    
    @Override
    public Sql[] generateSql([ChangeType]Statement statement, Database database, 
                            SqlGeneratorChain sqlGeneratorChain) {
        
        // CRITICAL: Get namespace attributes from the change
        [Database]NamespaceAttributeStorage storage = getNamespaceStorage(statement);
        
        // CRITICAL: Build complete SQL including namespace attributes
        String sql = buildCompleteSqlWithAttributes(statement, database, storage);
        
        // CRITICAL: Validate SQL completeness
        validateGeneratedSqlWithAttributes(sql, statement, storage);
        
        return new Sql[] { new UnparsedSql(sql) };
    }
    
    /**
     * CRITICAL: Builds complete SQL incorporating namespace attributes.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements with namespace attributes.
     */
    private String buildCompleteSqlWithAttributes([ChangeType]Statement statement, 
                                                 Database database,
                                                 [Database]NamespaceAttributeStorage storage) {
        StringBuilder sql = new StringBuilder();
        
        // MANDATORY: Start with base SQL
        sql.append("[BASE_SQL_OPERATION] ");
        sql.append(statement.getRequiredAttribute());
        
        // CRITICAL: Add namespace attributes to SQL
        if (storage != null) {
            if (storage.getNewAttribute() != null && storage.getNewAttribute()) {
                sql.append(" [ATTRIBUTE_SQL_CLAUSE]");
            }
            
            if (storage.getAnotherNewAttribute() != null) {
                sql.append(" [ANOTHER_ATTRIBUTE_CLAUSE] ");
                sql.append(storage.getAnotherNewAttribute());
            }
        }
        
        return sql.toString();
    }
    
    /**
     * CRITICAL: Validates complete SQL with all attributes.
     * ADDRESSES_CORE_ISSUE: Complete SQL test statements.
     */
    private void validateGeneratedSqlWithAttributes(String sql, 
                                                   [ChangeType]Statement statement,
                                                   [Database]NamespaceAttributeStorage storage) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalStateException("Generated SQL cannot be null or empty");
        }
        
        // MANDATORY: Validate base SQL elements
        if (!sql.toUpperCase().contains("[REQUIRED_KEYWORD]")) {
            throw new IllegalStateException("SQL missing required keyword: " + sql);
        }
        
        // CRITICAL: Validate namespace attributes are reflected in SQL
        if (storage != null) {
            if (storage.getNewAttribute() != null && storage.getNewAttribute()) {
                if (!sql.contains("[ATTRIBUTE_SQL_CLAUSE]")) {
                    throw new IllegalStateException(
                        "SQL missing namespace attribute clause: " + sql);
                }
            }
        }
    }
    
    /**
     * Helper method to get namespace storage from statement.
     */
    private [Database]NamespaceAttributeStorage getNamespaceStorage([ChangeType]Statement statement) {
        // Implementation to retrieve namespace storage
        // This may require extending the statement or using change context
        return null; // Placeholder
    }
}
```

## Cross-Reference Links and Automated Workflows

### Automated Workflow Scripts

**New Changetype Workflow:**
```bash
#!/bin/bash
# scripts/new-changetype-workflow.sh
set -e

CHANGETYPE=$1
DATABASE=$2

echo "=== New Changetype Development Workflow ==="
echo "Creating $CHANGETYPE for $DATABASE database"

echo "1. Validating requirements document..."
if [ ! -f "requirements/${CHANGETYPE}_requirements.md" ]; then
    echo "ERROR: Requirements document missing!"
    echo "Create: requirements/${CHANGETYPE}_requirements.md"
    exit 1
fi

echo "2. Building extension..."
cd liquibase-$DATABASE
mvn clean install -DskipTests=false

echo "3. Running unit tests..."
mvn test -Dtest="*${CHANGETYPE}*Test"

echo "4. Installing to local repository..."
echo "5. Testing with test harness..."
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DdbName=$DATABASE -DchangeObjects="$CHANGETYPE"

echo "=== New Changetype Workflow Complete ==="
```

**Existing Changetype Extension Workflow:**
```bash
#!/bin/bash
# scripts/extend-changetype-workflow.sh
set -e

CHANGETYPE=$1
DATABASE=$2
ATTRIBUTES=$3

echo "=== Existing Changetype Extension Workflow ==="
echo "Extending $CHANGETYPE for $DATABASE with attributes: $ATTRIBUTES"

echo "1. Discovery phase..."
find . -name "*${CHANGETYPE}*" -type f
find . -name "*NamespaceAttributeStorage*" -type f

echo "2. Building extension..."
cd liquibase-$DATABASE
mvn clean install -DskipTests=false

echo "3. Testing enhanced functionality..."
mvn test -Dtest="*${CHANGETYPE}*Test"

echo "=== Extension Workflow Complete ==="
```

## 🚀 INTEGRATION TEST PERFORMANCE OPTIMIZATION

### AI-Optimized Parallel Execution Strategy for Changetype Implementation
```yaml
INTEGRATION_TEST_PERFORMANCE:
  EXECUTION_MODE: PARALLEL_OPTIMIZED_CHANGETYPE_TESTING  
  ADDRESSES_CORE_ISSUES:
    - "Integration test performance optimization for time savings"
    - "ALL integration tests for all changetypes parallel execution capability"
    - "Account-level object naming conflicts preventing parallel execution"
  SUCCESS_METRICS:
    - "60% performance improvement demonstrated (warehouse example)"
    - "Scalable to ALL changetype integration tests"
    - "Zero naming conflicts with systematic strategy"
```

### When to Implement Parallel Execution Strategy
```yaml
PARALLEL_EXECUTION_DECISION_TREE:
  NEW_CHANGETYPE_PATTERN:
    ACCOUNT_LEVEL_OBJECTS: "MANDATORY - Implement naming strategy from start"
    SCHEMA_LEVEL_OBJECTS: "AUTOMATIC - Test harness handles isolation"
    IMPLEMENTATION_POINT: "During STEP 6 (Unit Testing) phase"
    
  EXISTING_CHANGETYPE_EXTENSION:
    ACCOUNT_LEVEL_OBJECTS: "RECOMMENDED - Migrate to naming strategy"
    SCHEMA_LEVEL_OBJECTS: "ALREADY_READY - No changes needed"
    IMPLEMENTATION_POINT: "After SQL generator enhancement complete"
    
  SQL_GENERATOR_OVERRIDE:
    PARALLEL_READINESS: "DEPENDS - Check object types created in integration tests"
    ASSESSMENT_REQUIRED: "Review existing integration tests for object types"
```

### Integration Test Naming Strategy Integration
```yaml
CHANGETYPE_IMPLEMENTATION_ENHANCEMENT:
  ADDRESSES_CORE_ISSUE: "Account-level object naming conflicts preventing parallel execution"
  
  STANDARD_IMPLEMENTATION_ADDITIONS:
    HELPER_METHOD: "Add getUniqueObjectName() methods to integration test classes"
    NAMING_PATTERN: "TEST_{CLASS_PREFIX}_{OBJECT_TYPE}_{METHOD_NAME}"
    CLEANUP_STRATEGY: "Use same unique names in cleanup logic"
    
  INTEGRATION_WITH_EXISTING_STEPS:
    STEP_6_UNIT_TESTING: "Add parallel execution validation"
    STEP_7_TEST_HARNESS: "Verify schema isolation + unique naming compatibility"
    STEP_8_INTEGRATION: "Default to parallel execution for performance"
```

### Performance Impact by Changetype Category
```yaml
PERFORMANCE_EXPECTATIONS:
  ACCOUNT_LEVEL_CHANGETYPES:
    EXAMPLES: ["createWarehouse", "alterDatabase", "createRole", "grantPrivilege"]
    PARALLEL_BENEFIT: "HIGH - 40-60% improvement expected"
    NAMING_STRATEGY: "MANDATORY"
    CONFLICT_RISK: "VERY_HIGH without naming strategy"
    
  SCHEMA_LEVEL_CHANGETYPES:
    EXAMPLES: ["createTable", "addColumn", "createView", "createSequence"]
    PARALLEL_BENEFIT: "MODERATE - 20-30% improvement expected"
    NAMING_STRATEGY: "NOT_REQUIRED - Schema isolation sufficient"
    CONFLICT_RISK: "NONE - Automatically isolated"
    
  MIXED_CHANGETYPES:
    EXAMPLES: ["createDatabase with tables", "createSchema with objects"]
    PARALLEL_BENEFIT: "VARIABLE - Depends on account vs schema object ratio"
    NAMING_STRATEGY: "REQUIRED for account-level components only"
    CONFLICT_RISK: "MEDIUM - Account-level conflicts only"
```

### Implementation Template Enhancement
```java
/**
 * ENHANCED: Integration test template with parallel execution support.
 * ADDRESSES_CORE_ISSUE: ALL integration tests for all changetypes parallel execution capability.
 */
public class [ChangeType]GeneratorSnowflakeIntegrationTest {
    
    /**
     * CRITICAL: Parallel execution naming strategy.
     * ADDRESSES_CORE_ISSUE: Account-level object naming conflicts.
     */
    private String getUniqueObjectName(String methodName) {
        return "TEST_[CHANGETYPE_PREFIX]_" + methodName;
    }
    
    /**
     * EXAMPLE: Parallel-execution-ready integration test method.
     * ADDRESSES_CORE_ISSUE: Integration test performance optimization.
     */
    @Test
    public void testBasicRequiredOnly() throws Exception {
        // CRITICAL: Use unique naming for account-level objects
        String objectName = getUniqueObjectName("testBasicRequiredOnly");
        
        try {
            // Test implementation with unique object name
            executeTest(objectName);
            
        } finally {
            // MANDATORY: Cleanup using same unique name
            cleanupObject(objectName);
        }
    }
    
    /**
     * CRITICAL: Validation method for parallel execution readiness.
     * Run this test to verify naming strategy prevents conflicts.
     */
    @Test
    public void validateParallelExecutionReadiness() {
        // Ensure all test methods use unique naming
        assertThat("Integration test implements parallel execution naming strategy", 
                   hasUniqueNamingStrategy(), is(true));
    }
}
```

### Changetype Implementation Workflow Enhancement
```yaml
ENHANCED_WORKFLOW_STEPS:
  EXISTING_STEP_6_UNIT_TESTING:
    ADDITIONS:
      - "Implement getUniqueObjectName() helper method"
      - "Update all test methods to use unique naming"
      - "Validate parallel execution compatibility"
    NEW_VALIDATION: "mvn test -Dtest='[ChangeType]*Test' -DforkCount=2 -DreuseForks=true"
    
  EXISTING_STEP_7_TEST_HARNESS:
    ADDITIONS:
      - "Verify schema isolation + unique naming work together"
      - "Test both sequential and parallel execution"
      - "Document performance improvement"
    NEW_VALIDATION: "Both sequential and parallel test harness execution successful"
    
  NEW_STEP_8_PERFORMANCE_VALIDATION:
    PURPOSE: "Validate integration test performance optimization"
    ACTIVITIES:
      - "Measure sequential vs parallel execution times"
      - "Document performance improvement percentage"
      - "Verify zero naming conflicts in parallel execution"
    SUCCESS_CRITERIA: "40-60% performance improvement for account-level objects"
```

## Cross-Reference Links
```yaml
RELATED_DOCUMENTS:
  MASTER_PROCESS: "master_process_loop.md - Overall development process"
  SQL_OVERRIDES: "sql_generator_overrides.md - SQL syntax modifications"
  TEST_HARNESS: "test_harness_guide.md - Testing protocols + parallel execution"
  PARALLEL_EXECUTION: "integration_test_parallel_execution_best_practices.md - Comprehensive parallel testing guide"
  REQUIREMENTS: "requirements_creation.md - Requirements specification"
  ERROR_DEBUGGING: "../snapshot_diff_implementation/error_patterns_guide.md"
  
NAVIGATION: "README.md - Complete navigation guide"
```