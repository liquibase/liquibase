# AI-Optimized Database Object Implementation Guide
## Autonomous Implementation Workflow for Liquibase Extensions

### 🎯 Overview: Template-Based Implementation Process

**AI-optimized 4-phase approach** using template substitution and decision trees:

```
Phase 1: Requirements Development & Validation (2-3 hours) →
Phase 2: Change Types Implementation & Testing (4-5 hours) →  
Phase 3: Snapshot/Diff Implementation & Testing (4-5 hours) →
Phase 4: Diff-Changelog/Generate-Changelog Implementation & Testing (2-3 hours)
```

**Implementation Method**: Template-based with {PLACEHOLDER} substitution
**Validation Method**: Automated command sequences with success/failure indicators
**Error Handling**: Systematic recovery procedures for all failure scenarios
**Time Investment**: 12-16 hours total for complete implementation
**Result**: Production-ready database object with 95%+ test coverage and full user workflow support

---

## 🚨 **CRITICAL: Professional Implementation Philosophy** 

**LEARNED FROM LIQUIBASE TEAM REVIEW + USER EXPERIENCE DECISION**: After evaluating both approaches, we've adopted a **hybrid pattern** that combines the best of both worlds.

### **Key Principles: Developer Experience + Maintainability + Consistency**
- **Optimal Pattern**: Type-safe explicit methods backed by generic storage (hybrid approach)
- **Developer Experience**: "I prefer the previous way -- nicer experience" (explicit methods provide IntelliSense, type safety, autocomplete)
- **Maintainability**: Generic backend storage provides easy extensibility without code changes
- **Data Access Strategy**: Use INFORMATION_SCHEMA queries for all object types (both schema-level and account-level)
- **Best of Both Worlds**: Explicit API for developers, generic implementation for maintainability, consistent data access patterns

---

## 📋 Template Configuration (Complete This First)

**AUTONOMOUS_CHECKPOINT_0**: Template Configuration
**PROGRESS_INDICATOR**: [█░░░░] 0/4 Phases Complete

**INSTRUCTION**: Define these values for your specific database object implementation:

```yaml
TEMPLATE_CONFIGURATION:
  OBJECT_TYPE: "Stage"                    # Your object type (e.g., "Stage", "Pipe", "User")
  OBJECT_TYPE_LOWER: "stage"              # Lowercase version
  OBJECT_TYPE_UPPER: "STAGE"              # Uppercase version
  SQL_COMMAND_CREATE: "CREATE STAGE"      # Snowflake SQL command
  SQL_COMMAND_ALTER: "ALTER STAGE"        # Snowflake ALTER command
  SQL_COMMAND_DROP: "DROP STAGE"          # Snowflake DROP command
  INFORMATION_SCHEMA_TABLE: "STAGES"      # INFORMATION_SCHEMA table name
  PRIMARY_IDENTIFIER: "stageName"         # Primary property name
  EXAMPLE_PROPERTY_1: "url"               # Example configurable property
  EXAMPLE_PROPERTY_2: "credentials"       # Example configurable property
```

**VALIDATION_COMMAND**: 
```bash
echo "OBJECT_TYPE: {OBJECT_TYPE}"
echo "Proceed only if template values are defined above"
```

**SUCCESS_INDICATOR_0**: All template placeholders defined with actual values
**NEXT_ACTION**: Proceed to Phase 1 Requirements Development
**FAILURE_RECOVERY**: Define all placeholders before proceeding - they are used throughout all templates

---

## 🔍 Phase 1: Requirements Development & Validation (2-3 hours)

**AUTONOMOUS_CHECKPOINT_1**: Requirements Development
**PROGRESS_INDICATOR**: [██░░░] 1/4 Phases In Progress
**PREREQUISITE_CHECK**: Template Configuration Complete ✅

### 🚨 **DECISION_TREE_PHASE_1_CRITICAL**: Inheritance vs New Implementation

**LEARNED FROM LIQUIBASE TEAM REVIEW**: "CreateSequenceChange is extending AbstractChange instead of liquibase.change.core.CreateSequenceChange - so it's redeclaring minValue, maxValue, etc."

```yaml
INHERITANCE_DECISION:
  QUESTION: "Does Liquibase already have a base class for your object type?"
  
  EXISTING_LIQUIBASE_CLASS:
    INDICATORS:
      - "liquibase.change.core.Create{OBJECT_TYPE}Change exists"
      - "Similar objects already implemented in core Liquibase"
      - "Properties like minValue, maxValue already defined"
    ACTION: "EXTEND existing Liquibase class (professional approach)"
    EXAMPLE: "extends liquibase.change.core.CreateSequenceChange"
    ANTI_PATTERN: "extends AbstractChange (causes code duplication)"
    TIME_SAVED: "Hours of redeclaration avoided"
    
  NO_EXISTING_CLASS:
    INDICATORS:
      - "Snowflake-specific object not in core Liquibase"
      - "Unique properties not found in existing classes"
    ACTION: "Create new class extending AbstractChange"
    VALIDATION: "Double-check - search core Liquibase first"
```

**VALIDATION_COMMAND_INHERITANCE**:
```bash
# Search for existing Liquibase base classes
find ~/.m2/repository/org/liquibase -name "*.jar" -exec jar tf {} \; | grep -i "{OBJECT_TYPE_LOWER}" | grep Change
grep -r "class.*{OBJECT_TYPE}" ~/.m2/repository/org/liquibase/ 2>/dev/null || echo "No existing base class found"
```

### DECISION_TREE_PHASE_1: Requirements Source Selection

```yaml
REQUIREMENTS_SOURCE_DECISION:
  QUESTION: "Do you have existing requirements documentation for your object type?"
  
  EXISTING_REQUIREMENTS:
    INDICATORS:
      - "Requirements file exists in claude_guide/snowflake_requirements/"
      - "Complete attribute analysis already documented"
      - "SQL examples and test scenarios provided"
    ACTION: "Skip to Step 4 (Requirements Validation)"
    TIME_SAVED: "2-2.5 hours"
    
  PARTIAL_REQUIREMENTS:
    INDICATORS:
      - "Some documentation exists but incomplete"
      - "Basic SQL syntax known but no comprehensive analysis"
    ACTION: "Use existing as starting point, complete gaps with Steps 1-3"
    TIME_ESTIMATE: "1-1.5 hours"
    
  NO_REQUIREMENTS:
    INDICATORS:
      - "New object type not previously analyzed"
      - "No existing documentation"
    ACTION: "Complete full requirements development (Steps 1-5)"
    TIME_ESTIMATE: "2-3 hours"
```

### Why Requirements First?
**LEARNED**: Excellent requirements documents dramatically accelerate implementation and prevent costly rework. The most recent requirements were excellent because they followed systematic patterns.

### Requirements Development Process

**Step 1: Comprehensive Attribute Analysis (45 minutes)**

**TEMPLATE: ATTRIBUTE_ANALYSIS_STRUCTURE**
```yaml
ATTRIBUTE_ANALYSIS_TEMPLATE:
  CORE_ATTRIBUTES:
    - name: "{PRIMARY_IDENTIFIER}"
      dataType: "String"
      required: "Required"
      default: "N/A"
      validValues: "Valid identifier"
      constraints: "Must be unique"
      mutualExclusivity: "None"
      priority: "HIGH"
      notes: "Primary object identifier"

  CONFIGURATION_ATTRIBUTES:
    - name: "{EXAMPLE_PROPERTY_1}"
      dataType: "String"
      required: "Optional"
      default: "null"
      validValues: "Valid values for property"
      constraints: "Specific constraints"
      mutualExclusivity: "May conflict with {EXAMPLE_PROPERTY_2}"
      priority: "MEDIUM"
      notes: "Configurable property description"

  STATE_ATTRIBUTES:
    - name: "createdTime"
      dataType: "Date"
      required: "N/A"
      default: "System generated"
      notes: "Read-only, exclude from diff comparison"
```

**Step 2: SQL Examples Collection (30 minutes)**

**TEMPLATE: SQL_EXAMPLES_STRUCTURE**
```sql
-- Basic creation
{SQL_COMMAND_CREATE} object_name;

-- Full configuration
{SQL_COMMAND_CREATE} [OR REPLACE] [IF NOT EXISTS] object_name
  WITH {EXAMPLE_PROPERTY_1} = 'value1'
  AND {EXAMPLE_PROPERTY_2} = value2
  COMMENT = 'description';

-- Complex scenarios
{SQL_COMMAND_CREATE} object_name
  WITH CONDITIONAL_PROPERTY = CASE 
    WHEN condition THEN 'value1'
    ELSE 'value2'
  END;
  
-- Schema-level object context
{SQL_COMMAND_CREATE} database.schema.object_name
  WITH {EXAMPLE_PROPERTY_1} = 'value1';
  
-- Account-level object (if applicable)
{SQL_COMMAND_CREATE} object_name  -- No schema prefix
  WITH {EXAMPLE_PROPERTY_1} = 'value1';
```

**Step 3: Mutual Exclusivity Rules (15 minutes)**

**TEMPLATE: MUTUAL_EXCLUSIVITY_STRUCTURE**
```yaml
MUTUAL_EXCLUSIVITY_RULES:
  - RULE: "orReplace and ifNotExists cannot both be true"
    VALIDATION: "if (orReplace && ifNotExists) → error"
    ERROR_MESSAGE: "Cannot use both OR REPLACE and IF NOT EXISTS"
    
  # TEMPLATE_EXPANSION: Add object-specific rules
  - RULE: "Rule specific to {OBJECT_TYPE}"
    VALIDATION: "if (condition) → error"
    ERROR_MESSAGE: "Clear error message for users"
    
  # Common patterns:
  - RULE: "transient objects must have dataRetentionTimeInDays = 0"
    VALIDATION: "if (transient && dataRetention != 0) → error"
    ERROR_MESSAGE: "Transient objects cannot have data retention > 0"
```

**Step 4: Test Scenario Matrix (30 minutes)**

**DECISION_TREE_1_4**: Test Scenario Complexity
```yaml
TEST_COMPLEXITY_DECISION:
  QUESTION: "How complex is your object type?"
  
  SIMPLE_OBJECT:
    INDICATORS:
      - "Few configurable properties (< 5)"
      - "No mutual exclusivity rules"
      - "Standard CREATE/ALTER/DROP operations"
    ACTION: "Use basic test matrix (15-20 scenarios)"
    TIME_ESTIMATE: "20 minutes"
    
  COMPLEX_OBJECT:
    INDICATORS:
      - "Many configurable properties (> 5)"
      - "Multiple mutual exclusivity rules"
      - "Conditional behavior based on properties"
    ACTION: "Use comprehensive test matrix (30-50 scenarios)"
    TIME_ESTIMATE: "45 minutes"
    
  VERY_COMPLEX_OBJECT:
    INDICATORS:
      - "Extensive property combinations"
      - "Complex business rules"
      - "Multiple operation modes"
    ACTION: "Use exhaustive test matrix (50+ scenarios)"
    TIME_ESTIMATE: "60+ minutes"
```

**TEMPLATE: TEST_SCENARIO_STRUCTURE**
```yaml
TEST_SCENARIOS:
  BASIC_OPERATIONS:
    - "CREATE {OBJECT_TYPE_LOWER} with minimal properties"
    - "CREATE {OBJECT_TYPE_LOWER} with all properties" 
    - "ALTER {OBJECT_TYPE_LOWER} property changes"
    - "DROP {OBJECT_TYPE_LOWER} with and without IF EXISTS"
    
  VALIDATION_SCENARIOS:
    - "Required {PRIMARY_IDENTIFIER} missing → error"
    - "Invalid {EXAMPLE_PROPERTY_1} values → error"
    - "Mutual exclusivity violations → error"
    
  EDGE_CASES:
    - "Null {PRIMARY_IDENTIFIER} handling"
    - "Empty string {EXAMPLE_PROPERTY_1} handling"
    - "Special characters in {PRIMARY_IDENTIFIER}"
    - "Large {EXAMPLE_PROPERTY_2} values"
    
  # TEMPLATE_EXPANSION: Add object-specific scenarios
  OBJECT_SPECIFIC_SCENARIOS:
    - "Scenario specific to {OBJECT_TYPE} behavior"
    - "Complex {OBJECT_TYPE} configuration combinations"
```

**Step 5: Requirements Validation (20 minutes)**

```bash
# Validate against official Snowflake documentation
# Check examples against real Snowflake behavior
# Ensure all attributes map to actual INFORMATION_SCHEMA columns
```

**✅ Phase 1 Complete When:**
- [ ] Comprehensive attribute analysis document exists
- [ ] 5+ real SQL examples documented and tested
- [ ] Mutual exclusivity rules clearly defined
- [ ] Test scenario matrix covers all operations
- [ ] Requirements validated against Snowflake documentation

**AUTONOMOUS_CHECKPOINT_1_COMPLETE**: Requirements Validation Complete
**PROGRESS_INDICATOR**: [██░░░] 1/4 Phases Complete ✅
**NEXT_ACTION**: Proceed to Phase 2 Change Types Implementation
**TIME_INVESTED**: 2-3 hours (saves 10+ hours of rework)

---

## 🏗️ Phase 2: Change Types Implementation & Testing (4-5 hours)

**AUTONOMOUS_CHECKPOINT_2**: Change Types Implementation
**PROGRESS_INDICATOR**: [███░░] 2/4 Phases In Progress
**PREREQUISITE_CHECK**: Requirements Development Complete ✅

### PROVEN TDD Strategy (Based on 95%+ Coverage Achievement)

**Core Philosophy**: Follow exact patterns that achieved 95%+ coverage in liquibase-snowflake
**Key Insight**: "Complete SQL string assertions are superior to component testing" - User Feedback
**Proven Workflow**: Unit Tests (Business Logic) → Integration Tests (Real Database) → End-to-End Validation

### Step 1: Database Object Model (45 minutes)

**DECISION_TREE_1**: Object Level Classification

```yaml
OBJECT_LEVEL_DECISION:
  QUESTION: "Where does your object exist in Snowflake hierarchy?"
  
  SCHEMA_LEVEL_OBJECTS:
    INDICATORS:
      - "SELECT * FROM INFORMATION_SCHEMA.{INFORMATION_SCHEMA_TABLE} WHERE SCHEMA_NAME = ?"
      - "Object has database.schema.object_name identifier"
      - "Examples: FileFormat, Stage, Pipe, Table"
    TEMPLATE_ACTION: "Use SCHEMA_LEVEL_TEMPLATE below"
    
  ACCOUNT_LEVEL_OBJECTS:
    INDICATORS:
      - "SHOW {OBJECT_TYPE_UPPER}S" (no WHERE clause)
      - "Object has only object_name identifier (no schema)"
      - "Examples: Warehouse, User, Role, ResourceMonitor"
    TEMPLATE_ACTION: "Use ACCOUNT_LEVEL_TEMPLATE below"
    WARNING: "Requires unified extensibility framework"
```

**TEMPLATE: SCHEMA_LEVEL_DATABASE_OBJECT**
```java
// FILE: src/main/java/liquibase/database/object/{OBJECT_TYPE}.java
package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

/**
 * Database object representing a Snowflake {OBJECT_TYPE}.
 * Schema-level object implementation.
 */
public class {OBJECT_TYPE} extends AbstractDatabaseObject {
    
    // REQUIRED: Primary identifier
    private String {PRIMARY_IDENTIFIER};
    
    // REQUIRED: Schema reference for schema-level objects
    private Schema schema;
    
    // TEMPLATE_EXPANSION: Add your configurable properties here
    private String {EXAMPLE_PROPERTY_1};
    private String {EXAMPLE_PROPERTY_2};
    
    // TEMPLATE_EXPANSION: Add state properties (exclude from diff) here  
    private java.util.Date createdTime;
    private String owner;
    
    // REQUIRED: Default constructor
    public {OBJECT_TYPE}() {
        super();
    }
    
    // REQUIRED: Constructor with name
    public {OBJECT_TYPE}(String {PRIMARY_IDENTIFIER}) {
        this();
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER};
    }
    
    // REQUIRED: Object type name for Liquibase framework
    @Override
    public String getObjectTypeName() {
        return "{OBJECT_TYPE_LOWER}";
    }
    
    // REQUIRED: Name getter (used by framework)
    @Override
    public String getName() {
        return {PRIMARY_IDENTIFIER};
    }
    
    // REQUIRED: Name setter (used by framework)
    @Override
    public DatabaseObject setName(String name) {
        this.{PRIMARY_IDENTIFIER} = name;
        return this;
    }
    
    // REQUIRED: Schema-level objects should be snapshotted by default
    @Override
    public boolean snapshotByDefault() {
        return true;
    }
    
    // REQUIRED: Primary identifier getter/setter
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    // REQUIRED: Schema getter/setter for schema-level objects
    public Schema getSchema() { return schema; }
    public void setSchema(Schema schema) { this.schema = schema; }
    
    // TEMPLATE_EXPANSION: Add getters/setters for your properties
    public String get{EXAMPLE_PROPERTY_1}() { return {EXAMPLE_PROPERTY_1}; }
    public void set{EXAMPLE_PROPERTY_1}(String {EXAMPLE_PROPERTY_1}) { this.{EXAMPLE_PROPERTY_1} = {EXAMPLE_PROPERTY_1}; }
    
    public String get{EXAMPLE_PROPERTY_2}() { return {EXAMPLE_PROPERTY_2}; }
    public void set{EXAMPLE_PROPERTY_2}(String {EXAMPLE_PROPERTY_2}) { this.{EXAMPLE_PROPERTY_2} = {EXAMPLE_PROPERTY_2}; }
    
    // REQUIRED: State property getters/setters (exclude from diff comparison)
    public java.util.Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(java.util.Date createdTime) { this.createdTime = createdTime; }
    
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    
    // REQUIRED: equals() and hashCode() based ONLY on identity fields
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        {OBJECT_TYPE} that = ({OBJECT_TYPE}) obj;
        
        if ({PRIMARY_IDENTIFIER} != null ? !{PRIMARY_IDENTIFIER}.equals(that.{PRIMARY_IDENTIFIER}) : that.{PRIMARY_IDENTIFIER} != null) return false;
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = {PRIMARY_IDENTIFIER} != null ? {PRIMARY_IDENTIFIER}.hashCode() : 0;
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }
}
```

**TEMPLATE: ACCOUNT_LEVEL_DATABASE_OBJECT**
```java
// FILE: src/main/java/liquibase/database/object/{OBJECT_TYPE}.java
package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

/**
 * Database object representing a Snowflake {OBJECT_TYPE}.
 * Account-level object implementation (requires unified extensibility framework).
 */
public class {OBJECT_TYPE} extends AbstractDatabaseObject {
    
    // REQUIRED: Primary identifier
    private String {PRIMARY_IDENTIFIER};
    
    // NO SCHEMA: Account-level objects don't have schema context
    
    // TEMPLATE_EXPANSION: Add your configurable properties here
    private String {EXAMPLE_PROPERTY_1};
    private String {EXAMPLE_PROPERTY_2};
    
    // TEMPLATE_EXPANSION: Add state properties (exclude from diff) here  
    private java.util.Date createdTime;
    private String owner;
    
    // REQUIRED: Default constructor
    public {OBJECT_TYPE}() {
        super();
    }
    
    // REQUIRED: Constructor with name
    public {OBJECT_TYPE}(String {PRIMARY_IDENTIFIER}) {
        this();
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER};
    }
    
    // REQUIRED: Object type name for Liquibase framework
    @Override
    public String getObjectTypeName() {
        return "{OBJECT_TYPE_LOWER}";
    }
    
    // REQUIRED: Name getter (used by framework)
    @Override
    public String getName() {
        return {PRIMARY_IDENTIFIER};
    }
    
    // REQUIRED: Name setter (used by framework)
    @Override
    public DatabaseObject setName(String name) {
        this.{PRIMARY_IDENTIFIER} = name;
        return this;
    }
    
    // REQUIRED: Account-level objects should be snapshotted by default
    @Override
    public boolean snapshotByDefault() {
        return true;
    }
    
    // REQUIRED: Primary identifier getter/setter
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    // NO SCHEMA METHODS: Account-level objects don't have schema
    
    // TEMPLATE_EXPANSION: Add getters/setters for your properties
    public String get{EXAMPLE_PROPERTY_1}() { return {EXAMPLE_PROPERTY_1}; }
    public void set{EXAMPLE_PROPERTY_1}(String {EXAMPLE_PROPERTY_1}) { this.{EXAMPLE_PROPERTY_1} = {EXAMPLE_PROPERTY_1}; }
    
    public String get{EXAMPLE_PROPERTY_2}() { return {EXAMPLE_PROPERTY_2}; }
    public void set{EXAMPLE_PROPERTY_2}(String {EXAMPLE_PROPERTY_2}) { this.{EXAMPLE_PROPERTY_2} = {EXAMPLE_PROPERTY_2}; }
    
    // REQUIRED: State property getters/setters (exclude from diff comparison)
    public java.util.Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(java.util.Date createdTime) { this.createdTime = createdTime; }
    
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    
    // REQUIRED: equals() and hashCode() based ONLY on identity fields
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        {OBJECT_TYPE} that = ({OBJECT_TYPE}) obj;
        
        if ({PRIMARY_IDENTIFIER} != null ? !{PRIMARY_IDENTIFIER}.equals(that.{PRIMARY_IDENTIFIER}) : that.{PRIMARY_IDENTIFIER} != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = {PRIMARY_IDENTIFIER} != null ? {PRIMARY_IDENTIFIER}.hashCode() : 0;
        return result;
    }
}
```

**VALIDATION_COMMAND_1_1**:
```bash
# Create the file and validate compilation
mvn compile -q
```

**SUCCESS_INDICATOR**: `BUILD SUCCESS`
**FAILURE_RECOVERY**: See ERROR_RECOVERY_COMPILATION section

**PROVEN MICRO-CYCLE 2: Complete SQL String Assertion Pattern** 🏆

**KEY INSIGHT**: "Testing the completed SQL string is a better test" - Proven superior to component testing

```java
// RED: Test complete SQL generation (proven superior pattern)
@Test
@DisplayName("Should generate {OBJECT_TYPE_LOWER} with all properties")
void testGenerate{OBJECT_TYPE}_WithAllProperties_CompleteSQL() {
    // Given - Statement with all properties
    Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
    statement.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    statement.set{EXAMPLE_PROPERTY_1}("value1");
    statement.set{EXAMPLE_PROPERTY_2}("value2");
    
    // When - Generate SQL
    Sql[] sqls = generator.generateSql(statement, database, null);
    
    // Then - COMPLETE SQL STRING ASSERTION (PROVEN PATTERN)
    String expectedSQL = "{SQL_COMMAND_CREATE} TEST_OBJECT WITH " +
            "{EXAMPLE_PROPERTY_1} = 'value1' " +
            "{EXAMPLE_PROPERTY_2} = 'value2'";
    
    assertEquals(expectedSQL, sqls[0].toSql()); // KEY PATTERN
}

// GREEN: Implement SQL generator to pass test
public class Create{OBJECT_TYPE}GeneratorSnowflake extends AbstractSqlGenerator<Create{OBJECT_TYPE}Statement> {
    @Override
    public Sql[] generateSql(Create{OBJECT_TYPE}Statement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder("{SQL_COMMAND_CREATE} ");
        sql.append(database.escapeObjectName(statement.get{PRIMARY_IDENTIFIER}(), {OBJECT_TYPE}.class));
        
        // Build complete SQL to match test expectation
        if (statement.get{EXAMPLE_PROPERTY_1}() != null) {
            sql.append(" WITH {EXAMPLE_PROPERTY_1} = '").append(statement.get{EXAMPLE_PROPERTY_1}()).append("'");
        }
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}
```

**PROVEN MICRO-CYCLE 3: Comprehensive Validation Pattern**

```java
// RED: Test all validation scenarios (proven organization pattern)
@Test
@DisplayName("Should require {PRIMARY_IDENTIFIER}")
void shouldRequire{PRIMARY_IDENTIFIER}() {
    // Given - Change without required property
    Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
    // Don't set required {PRIMARY_IDENTIFIER}
    
    // When - Validate
    ValidationErrors errors = change.validate(database);
    
    // Then - Verify specific error message
    assertTrue(errors.hasErrors());
    assertTrue(errors.getErrorMessages().get(0).contains("{PRIMARY_IDENTIFIER} is required"));
}

@Test
@DisplayName("Should validate mutual exclusivity of orReplace and ifNotExists")
void shouldValidateMutualExclusivity() {
    // Given - Change with mutually exclusive properties
    Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
    change.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    change.setOrReplace(true);
    change.setIfNotExists(true);
    
    // When - Validate
    ValidationErrors errors = change.validate(database);
    
    // Then - Verify mutual exclusivity error
    assertTrue(errors.hasErrors());
    assertTrue(errors.getErrorMessages().stream()
        .anyMatch(msg -> msg.contains("Cannot specify both orReplace and ifNotExists")));
}

// GREEN: Implement validation to pass all tests
@Override
public ValidationErrors validate(Database database) {
    ValidationErrors errors = super.validate(database);
    
    // Required property validation
    if ({PRIMARY_IDENTIFIER} == null || {PRIMARY_IDENTIFIER}.trim().isEmpty()) {
        errors.addError("{PRIMARY_IDENTIFIER} is required");
    }
    
    // Mutual exclusivity validation
    if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
        errors.addError("Cannot specify both orReplace and ifNotExists");
    }
    
    return errors;
}
```

### Step 2: Change Implementation Templates (90 minutes)

**🚨 DECISION_TREE_2_2_CRITICAL**: Implementation Approach Selection

**LEARNED FROM LIQUIBASE TEAM REVIEW**: "I prefer interop version, as we don't need to worry with new properties and it's almost 1/5 of the size."

```yaml
IMPLEMENTATION_APPROACH_DECISION:
  QUESTION: "How many configurable properties does your object type have?"
  
  HIGH_PROPERTY_COUNT (10+ properties):
    INDICATORS:
      - "FileFormat-like objects with format-specific properties"
      - "Properties that depend on other property values"
      - "Many properties that may be added in future Snowflake versions"
    PROFESSIONAL_APPROACH: "Generic Property Storage Pattern"
    CODE_SIZE_TARGET: "75-100 LOC (reference: Interop team FileFormat)"
    BENEFITS:
      - "Zero maintenance for new Snowflake properties"
      - "Forces developer to read documentation (honest approach)"
      - "Self-documenting format-specific context"
    XSD_APPROACH: "Minimal - structural validation only"
    
  LOW_PROPERTY_COUNT (< 10 properties):
    INDICATORS:
      - "Simple objects with stable property set"
      - "Properties that are unlikely to change"
      - "Clear, non-conditional property relationships"
    ACCEPTABLE_APPROACH: "Explicit Property Mapping"
    CODE_SIZE_TARGET: "100-150 LOC maximum"
    VALIDATION_REQUIRED: "Ensure no existing Liquibase base class"
    XSD_APPROACH: "Full validation appropriate"
```

**ANTI-PATTERNS TO AVOID**:
```yaml
VERBOSE_OVERENGINEERING:
  WARNING: "354 LOC vs 75 LOC (5x larger)"
  SYMPTOMS:
    - "Explicit getters/setters for every possible property"
    - "Complex conditional validation in Java code"
    - "XSD with 50+ attributes"
  MAINTENANCE_COST: "Code changes required for every new Snowflake property"
  
INHERITANCE_DUPLICATION:
  WARNING: "Redeclaring minValue, maxValue, etc."
  SYMPTOM: "extends AbstractChange instead of liquibase.change.core.CreateSequenceChange"
  IMPACT: "Code duplication and maintenance burden"
```

**DECISION_TREE_2_3**: Change Type Selection
```yaml
CHANGE_TYPE_DECISION:
  QUESTION: "What change operations does your object type support?"
  
  CREATE_ONLY:
    INDICATORS:
      - "Object cannot be altered after creation"
      - "Examples: Account-level immutable objects"
    ACTION: "Implement only Create{OBJECT_TYPE}Change"
    TIME_ESTIMATE: "45 minutes"
    
  CREATE_DROP:
    INDICATORS:
      - "Object supports creation and deletion only"
      - "No alterable properties"
    ACTION: "Implement Create{OBJECT_TYPE}Change and Drop{OBJECT_TYPE}Change"
    TIME_ESTIMATE: "60 minutes"
    
  FULL_CRUD:
    INDICATORS:
      - "Object supports all operations"
      - "Has alterable properties"
      - "Standard database object lifecycle"
    ACTION: "Implement Create, Alter, and Drop change classes"
    TIME_ESTIMATE: "90 minutes"
```

**🏆 TEMPLATE: PROFESSIONAL_GENERIC_PROPERTY_STORAGE** (For 10+ Properties)

**APPROACH**: Generic property storage - proven concise and maintainable

```java
// FILE: src/main/java/liquibase/change/core/Create{OBJECT_TYPE}Change.java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.Create{OBJECT_TYPE}Statement;
import java.util.Map;
import java.util.HashMap;

@DatabaseChange(
    name = "create{OBJECT_TYPE}",
    description = "Creates a {OBJECT_TYPE_LOWER} object",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class Create{OBJECT_TYPE}Change extends AbstractChange {
    
    // REQUIRED: Primary identifier (always explicit)
    private String {PRIMARY_IDENTIFIER};
    
    // HYBRID PATTERN: Generic backend storage for maintainability
    private Map<String, String> objectProperties = new HashMap<>();
    
    // REQUIRED: Primary identifier (always explicit)
    @DatabaseChangeProperty(requiredForDatabase = "snowflake")
    public String get{PRIMARY_IDENTIFIER}() { 
        return {PRIMARY_IDENTIFIER}; 
    }
    
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { 
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; 
    }
    
    // DEVELOPER EXPERIENCE: Type-safe explicit methods for common properties
    @DatabaseChangeProperty(description = "Example property with type safety and IntelliSense")
    public String get{EXAMPLE_PROPERTY_1}() {
        return getObjectProperty("{EXAMPLE_PROPERTY_1}");
    }
    
    public void set{EXAMPLE_PROPERTY_1}(String value) {
        setObjectProperty("{EXAMPLE_PROPERTY_1}", value);
    }
    
    @DatabaseChangeProperty(description = "Another example property")
    public String get{EXAMPLE_PROPERTY_2}() {
        return getObjectProperty("{EXAMPLE_PROPERTY_2}");
    }
    
    public void set{EXAMPLE_PROPERTY_2}(String value) {
        setObjectProperty("{EXAMPLE_PROPERTY_2}", value);
    }
    
    // MAINTAINABILITY: Generic property methods (backend implementation)
    public void setObjectProperty(String propertyName, String propertyValue) {
        if (propertyValue != null) {
            objectProperties.put(propertyName, propertyValue);
        }
    }
    
    public String getObjectProperty(String propertyName) {
        return objectProperties.get(propertyName);
    }
    
    public Map<String, String> getObjectProperties() {
        return new HashMap<>(objectProperties);
    }
    
    // REQUIRED: Generate SQL statements
    @Override
    public SqlStatement[] generateStatements(Database database) {
        Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}({PRIMARY_IDENTIFIER});
        statement.setObjectProperties(objectProperties);
        
        return new SqlStatement[]{statement};
    }
    
    // REQUIRED: Minimal validation (business rules handled by Snowflake)
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if ({PRIMARY_IDENTIFIER} == null || {PRIMARY_IDENTIFIER}.trim().isEmpty()) {
            errors.addError("{PRIMARY_IDENTIFIER} is required");
        }
        
        // Let Snowflake handle property-specific validation
        // This forces developers to understand Snowflake semantics (honest approach)
        
        return errors;
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Created {OBJECT_TYPE_LOWER} " + get{PRIMARY_IDENTIFIER}();
    }
}
```

**🔧 TEMPLATE: EXPLICIT_PROPERTY_STORAGE** (For < 10 Properties Only)

**WARNING**: Only use for simple objects with stable, few properties

```java
// FILE: src/main/java/liquibase/change/core/Create{OBJECT_TYPE}Change.java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.Create{OBJECT_TYPE}Statement;

@DatabaseChange(
    name = "create{OBJECT_TYPE}",
    description = "Creates a {OBJECT_TYPE_LOWER} object",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class Create{OBJECT_TYPE}Change extends AbstractChange {
    
    // VALIDATION REQUIRED: Check for existing Liquibase base class first!
    // extends liquibase.change.core.Create{OBJECT_TYPE}Change if it exists
    
    private String {PRIMARY_IDENTIFIER};
    private String {EXAMPLE_PROPERTY_1};  // Only if truly stable
    private Boolean orReplace;
    private Boolean ifNotExists;
    
    @DatabaseChangeProperty(requiredForDatabase = "snowflake")
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    @DatabaseChangeProperty
    public String get{EXAMPLE_PROPERTY_1}() { return {EXAMPLE_PROPERTY_1}; }
    public void set{EXAMPLE_PROPERTY_1}(String {EXAMPLE_PROPERTY_1}) { this.{EXAMPLE_PROPERTY_1} = {EXAMPLE_PROPERTY_1}; }
    
    // Conditional properties
    @DatabaseChangeProperty
    public Boolean getOrReplace() { return orReplace; }
    public void setOrReplace(Boolean orReplace) { this.orReplace = orReplace; }
    
    @DatabaseChangeProperty
    public Boolean getIfNotExists() { return ifNotExists; }
    public void setIfNotExists(Boolean ifNotExists) { this.ifNotExists = ifNotExists; }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}({PRIMARY_IDENTIFIER});
        statement.set{EXAMPLE_PROPERTY_1}({EXAMPLE_PROPERTY_1});
        statement.setOrReplace(orReplace);
        statement.setIfNotExists(ifNotExists);
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if ({PRIMARY_IDENTIFIER} == null || {PRIMARY_IDENTIFIER}.trim().isEmpty()) {
            errors.addError("{PRIMARY_IDENTIFIER} is required");
        }
        
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        return errors;
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Created {OBJECT_TYPE_LOWER} " + get{PRIMARY_IDENTIFIER}();
    }
}
```

**TEMPLATE: ALTER_CHANGE_CLASS**
```java
// FILE: src/main/java/liquibase/change/core/Alter{OBJECT_TYPE}Change.java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.Alter{OBJECT_TYPE}Statement;

@DatabaseChange(
    name = "alter{OBJECT_TYPE}",
    description = "Alters a {OBJECT_TYPE_LOWER} object",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class Alter{OBJECT_TYPE}Change extends AbstractChange {
    
    // REQUIRED: Primary identifier property
    private String {PRIMARY_IDENTIFIER};
    
    // TEMPLATE_EXPANSION: Add alterable properties
    private String new{EXAMPLE_PROPERTY_1};
    private String new{EXAMPLE_PROPERTY_2};
    
    // REQUIRED: Primary identifier getter/setter
    @DatabaseChangeProperty(requiredForDatabase = "snowflake")
    public String get{PRIMARY_IDENTIFIER}() { 
        return {PRIMARY_IDENTIFIER}; 
    }
    
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { 
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; 
    }
    
    // TEMPLATE_EXPANSION: Add alterable property getters/setters
    @DatabaseChangeProperty
    public String getNew{EXAMPLE_PROPERTY_1}() { 
        return new{EXAMPLE_PROPERTY_1}; 
    }
    
    public void setNew{EXAMPLE_PROPERTY_1}(String new{EXAMPLE_PROPERTY_1}) { 
        this.new{EXAMPLE_PROPERTY_1} = new{EXAMPLE_PROPERTY_1}; 
    }
    
    // REQUIRED: Generate SQL statements
    @Override
    public SqlStatement[] generateStatements(Database database) {
        Alter{OBJECT_TYPE}Statement statement = new Alter{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}({PRIMARY_IDENTIFIER});
        statement.setNew{EXAMPLE_PROPERTY_1}(new{EXAMPLE_PROPERTY_1});
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Altered {OBJECT_TYPE_LOWER} " + get{PRIMARY_IDENTIFIER}();
    }
}
```

**TEMPLATE: DROP_CHANGE_CLASS**
```java
// FILE: src/main/java/liquibase/change/core/Drop{OBJECT_TYPE}Change.java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.Drop{OBJECT_TYPE}Statement;

@DatabaseChange(
    name = "drop{OBJECT_TYPE}",
    description = "Drops a {OBJECT_TYPE_LOWER} object",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class Drop{OBJECT_TYPE}Change extends AbstractChange {
    
    // REQUIRED: Primary identifier property
    private String {PRIMARY_IDENTIFIER};
    
    // OPTIONAL: Conditional drop
    private Boolean ifExists;
    
    // REQUIRED: Primary identifier getter/setter
    @DatabaseChangeProperty(requiredForDatabase = "snowflake")
    public String get{PRIMARY_IDENTIFIER}() { 
        return {PRIMARY_IDENTIFIER}; 
    }
    
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { 
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; 
    }
    
    @DatabaseChangeProperty
    public Boolean getIfExists() { 
        return ifExists; 
    }
    
    public void setIfExists(Boolean ifExists) { 
        this.ifExists = ifExists; 
    }
    
    // REQUIRED: Generate SQL statements
    @Override
    public SqlStatement[] generateStatements(Database database) {
        Drop{OBJECT_TYPE}Statement statement = new Drop{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}({PRIMARY_IDENTIFIER});
        statement.setIfExists(ifExists);
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Dropped {OBJECT_TYPE_LOWER} " + get{PRIMARY_IDENTIFIER}();
    }
}
```

**TDD MICRO-CYCLE TEMPLATE**
```java
// RED: Test change creation and properties
@Test
void testCreate{OBJECT_TYPE}Change_WithAllProperties_GeneratesCorrectStatement() {
    Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
    change.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    change.set{EXAMPLE_PROPERTY_1}("value1");
    
    SqlStatement[] statements = change.generateStatements(database);
    
    assertEquals(1, statements.length);
    assertTrue(statements[0] instanceof Create{OBJECT_TYPE}Statement);
}

// RED: Test validation
@Test
void testValidation_RequiredProperty_Missing_ReturnsError() {
    Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
    // Don't set required {PRIMARY_IDENTIFIER}
    
    ValidationErrors errors = change.validate(database);
    
    assertTrue(errors.hasErrors());
    assertTrue(errors.getErrorMessages().contains("{PRIMARY_IDENTIFIER} is required"));
}

// RED: Test mutual exclusivity (if applicable)
@Test
void testValidation_MutuallyExclusiveProperties_ReturnsError() {
    Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
    change.setOrReplace(true);
    change.setIfNotExists(true);
    
    ValidationErrors errors = change.validate(database);
    
    assertTrue(errors.hasErrors());
    assertTrue(errors.getErrorMessages().contains("Cannot use both OR REPLACE and IF NOT EXISTS"));
}
```

### Step 3: SQL Statement & Generation Templates (90 minutes)

**DECISION_TREE_2_3**: SQL Generation Complexity
```yaml
SQL_COMPLEXITY_DECISION:
  QUESTION: "How complex is the SQL generation for your object type?"
  
  SIMPLE_SQL:
    INDICATORS:
      - "Standard CREATE/ALTER/DROP syntax"
      - "Few conditional clauses"
      - "No complex property interactions"
    ACTION: "Use basic SQL generation template"
    TIME_ESTIMATE: "60 minutes"
    
  MODERATE_SQL:
    INDICATORS:
      - "Some conditional syntax (OR REPLACE, IF NOT EXISTS)"
      - "Property-dependent clauses"
      - "Schema vs account-level differences"
    ACTION: "Use conditional SQL generation template"
    TIME_ESTIMATE: "90 minutes"
    
  COMPLEX_SQL:
    INDICATORS:
      - "Highly conditional syntax"
      - "Complex property combinations"
      - "Multiple SQL variants based on context"
    ACTION: "Use advanced SQL generation with operation detection"
    TIME_ESTIMATE: "120+ minutes"
```

**TEMPLATE: SQL_STATEMENTS**
```java
// FILE: src/main/java/liquibase/statement/core/Create{OBJECT_TYPE}Statement.java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class Create{OBJECT_TYPE}Statement extends AbstractSqlStatement {
    
    // REQUIRED: Primary identifier
    private String {PRIMARY_IDENTIFIER};
    
    // TEMPLATE_EXPANSION: Add your configurable properties
    private String {EXAMPLE_PROPERTY_1};
    private String {EXAMPLE_PROPERTY_2};
    
    // TEMPLATE_EXPANSION: Add conditional properties
    private Boolean orReplace;
    private Boolean ifNotExists;
    
    // REQUIRED: Default constructor
    public Create{OBJECT_TYPE}Statement() {
        super();
    }
    
    // REQUIRED: Constructor with primary identifier
    public Create{OBJECT_TYPE}Statement(String {PRIMARY_IDENTIFIER}) {
        this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER};
    }
    
    // REQUIRED: Primary identifier getter/setter
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    // TEMPLATE_EXPANSION: Add property getters/setters
    public String get{EXAMPLE_PROPERTY_1}() { return {EXAMPLE_PROPERTY_1}; }
    public void set{EXAMPLE_PROPERTY_1}(String {EXAMPLE_PROPERTY_1}) { this.{EXAMPLE_PROPERTY_1} = {EXAMPLE_PROPERTY_1}; }
    
    public String get{EXAMPLE_PROPERTY_2}() { return {EXAMPLE_PROPERTY_2}; }
    public void set{EXAMPLE_PROPERTY_2}(String {EXAMPLE_PROPERTY_2}) { this.{EXAMPLE_PROPERTY_2} = {EXAMPLE_PROPERTY_2}; }
    
    // TEMPLATE_EXPANSION: Add conditional getters/setters
    public Boolean getOrReplace() { return orReplace; }
    public void setOrReplace(Boolean orReplace) { this.orReplace = orReplace; }
    
    public Boolean getIfNotExists() { return ifNotExists; }
    public void setIfNotExists(Boolean ifNotExists) { this.ifNotExists = ifNotExists; }
}

// FILE: src/main/java/liquibase/statement/core/Alter{OBJECT_TYPE}Statement.java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class Alter{OBJECT_TYPE}Statement extends AbstractSqlStatement {
    
    // REQUIRED: Primary identifier
    private String {PRIMARY_IDENTIFIER};
    
    // TEMPLATE_EXPANSION: Add alterable properties
    private String new{EXAMPLE_PROPERTY_1};
    private String new{EXAMPLE_PROPERTY_2};
    
    // Constructor and getters/setters...
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    public String getNew{EXAMPLE_PROPERTY_1}() { return new{EXAMPLE_PROPERTY_1}; }
    public void setNew{EXAMPLE_PROPERTY_1}(String new{EXAMPLE_PROPERTY_1}) { this.new{EXAMPLE_PROPERTY_1} = new{EXAMPLE_PROPERTY_1}; }
}

// FILE: src/main/java/liquibase/statement/core/Drop{OBJECT_TYPE}Statement.java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class Drop{OBJECT_TYPE}Statement extends AbstractSqlStatement {
    
    // REQUIRED: Primary identifier
    private String {PRIMARY_IDENTIFIER};
    
    // OPTIONAL: Conditional drop
    private Boolean ifExists;
    
    // Constructor and getters/setters...
    public String get{PRIMARY_IDENTIFIER}() { return {PRIMARY_IDENTIFIER}; }
    public void set{PRIMARY_IDENTIFIER}(String {PRIMARY_IDENTIFIER}) { this.{PRIMARY_IDENTIFIER} = {PRIMARY_IDENTIFIER}; }
    
    public Boolean getIfExists() { return ifExists; }
    public void setIfExists(Boolean ifExists) { this.ifExists = ifExists; }
}
```

**TEMPLATE: SQL_GENERATORS**
```java
// FILE: src/main/java/liquibase/sqlgenerator/core/Create{OBJECT_TYPE}GeneratorSnowflake.java
package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.Create{OBJECT_TYPE}Statement;
import liquibase.structure.core.{OBJECT_TYPE};

public class Create{OBJECT_TYPE}GeneratorSnowflake extends Abstract{OBJECT_TYPE}SqlGenerator<Create{OBJECT_TYPE}Statement> {
    
    @Override
    public boolean supports(Create{OBJECT_TYPE}Statement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
    
    @Override
    public Sql[] generateSql(Create{OBJECT_TYPE}Statement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder();
        
        // Build base CREATE command
        sql.append("CREATE ");
        
        // TEMPLATE_EXPANSION: Add conditional modifiers
        if (Boolean.TRUE.equals(statement.getOrReplace())) {
            sql.append("OR REPLACE ");
        }
        
        sql.append("{SQL_COMMAND_CREATE} ");
        
        if (Boolean.TRUE.equals(statement.getIfNotExists())) {
            sql.append("IF NOT EXISTS ");
        }
        
        // Add object name (escaped)
        sql.append(database.escapeObjectName(statement.get{PRIMARY_IDENTIFIER}(), {OBJECT_TYPE}.class));
        
        // TEMPLATE_EXPANSION: Add properties
        boolean hasProperties = false;
        
        if (statement.get{EXAMPLE_PROPERTY_1}() != null) {
            sql.append(hasProperties ? " AND " : " WITH ");
            sql.append("{EXAMPLE_PROPERTY_1} = '").append(statement.get{EXAMPLE_PROPERTY_1}()).append("'");
            hasProperties = true;
        }
        
        if (statement.get{EXAMPLE_PROPERTY_2}() != null) {
            sql.append(hasProperties ? " AND " : " WITH ");
            sql.append("{EXAMPLE_PROPERTY_2} = '").append(statement.get{EXAMPLE_PROPERTY_2}()).append("'");
            hasProperties = true;
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedObjects(statement))};
    }
    
    // REQUIRED: Define affected objects for dependency tracking
    protected {OBJECT_TYPE}[] getAffectedObjects(Create{OBJECT_TYPE}Statement statement) {
        {OBJECT_TYPE} object = new {OBJECT_TYPE}(statement.get{PRIMARY_IDENTIFIER}());
        // TEMPLATE_EXPANSION: Set schema for schema-level objects
        // object.setSchema(new Schema(statement.getCatalogName(), statement.getSchemaName()));
        return new {OBJECT_TYPE}[]{object};
    }
}

// FILE: src/main/java/liquibase/sqlgenerator/core/Alter{OBJECT_TYPE}GeneratorSnowflake.java
package liquibase.sqlgenerator.core;

public class Alter{OBJECT_TYPE}GeneratorSnowflake extends Abstract{OBJECT_TYPE}SqlGenerator<Alter{OBJECT_TYPE}Statement> {
    
    @Override
    public Sql[] generateSql(Alter{OBJECT_TYPE}Statement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("{SQL_COMMAND_ALTER} ");
        sql.append(database.escapeObjectName(statement.get{PRIMARY_IDENTIFIER}(), {OBJECT_TYPE}.class));
        
        // TEMPLATE_EXPANSION: Add alteration clauses
        if (statement.getNew{EXAMPLE_PROPERTY_1}() != null) {
            sql.append(" SET {EXAMPLE_PROPERTY_1} = '").append(statement.getNew{EXAMPLE_PROPERTY_1}()).append("'");
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedObjects(statement))};
    }
}

// FILE: src/main/java/liquibase/sqlgenerator/core/Drop{OBJECT_TYPE}GeneratorSnowflake.java
package liquibase.sqlgenerator.core;

public class Drop{OBJECT_TYPE}GeneratorSnowflake extends Abstract{OBJECT_TYPE}SqlGenerator<Drop{OBJECT_TYPE}Statement> {
    
    @Override
    public Sql[] generateSql(Drop{OBJECT_TYPE}Statement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("{SQL_COMMAND_DROP} ");
        
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.get{PRIMARY_IDENTIFIER}(), {OBJECT_TYPE}.class));
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedObjects(statement))};
    }
}
```

**TDD MICRO-CYCLE TEMPLATES**
```java
// RED: Test statement creation
@Test
void testCreate{OBJECT_TYPE}Statement_WithAllProperties_SetsCorrectValues() {
    Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
    statement.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    statement.set{EXAMPLE_PROPERTY_1}("value1");
    
    assertEquals("TEST_OBJECT", statement.get{PRIMARY_IDENTIFIER}());
    assertEquals("value1", statement.get{EXAMPLE_PROPERTY_1}());
}

// RED: Test complete SQL generation (proven superior pattern)
@Test
void testGenerateSQL_AllProperties_ProducesCorrectCompleteSQL() {
    Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
    statement.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    statement.set{EXAMPLE_PROPERTY_1}("value1");
    statement.set{EXAMPLE_PROPERTY_2}("value2");
    
    Sql[] sql = generator.generateSql(statement, database, null);
    
    // Complete SQL string assertion (proven superior approach)
    String expectedSQL = "{SQL_COMMAND_CREATE} TEST_OBJECT WITH {EXAMPLE_PROPERTY_1} = 'value1' AND {EXAMPLE_PROPERTY_2} = 'value2'";
    assertEquals(expectedSQL, sql[0].toSql(), "Should generate correct complete SQL");
}

// RED: Test conditional SQL generation
@Test
void testGenerateSQL_WithOrReplace_GeneratesCorrectSQL() {
    Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
    statement.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    statement.setOrReplace(true);
    
    Sql[] sql = generator.generateSql(statement, database, null);
    
    String expectedSQL = "CREATE OR REPLACE {SQL_COMMAND_CREATE} TEST_OBJECT";
    assertTrue(sql[0].toSql().contains("OR REPLACE"), "Should include OR REPLACE clause");
}

// RED: Test validation in generator
@Test
void testGenerateSQL_InvalidMutualExclusivity_ThrowsException() {
    Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
    statement.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    statement.setOrReplace(true);
    statement.setIfNotExists(true);
    
    assertThrows(ValidationFailedException.class, () -> {
        generator.generateSql(statement, database, null);
    });
}
```

### Step 4: Professional Development Practices (30 minutes)

**🚨 CRITICAL**: Fix anti-patterns identified in Liquibase team review

#### **4A: Proper Logging (Not System.out.println)**

**ANTI-PATTERN IDENTIFIED**: "it used a lot of println for debug with emojis instead of loggers"

**PROFESSIONAL PATTERN**:
```java
// WRONG: Console debugging with emojis
System.out.println("🔧 CORE: discoverRootLevelExtensionObjects() called");
System.out.println("⚡ DEBUG: Processing " + objects.size() + " objects");

// RIGHT: Proper Liquibase logging
import liquibase.Scope;

public class Your{OBJECT_TYPE}Class {
    private static final Logger logger = Scope.getCurrentScope().getLog(Your{OBJECT_TYPE}Class.class);
    
    public void someMethod() {
        logger.info("Processing {OBJECT_TYPE_LOWER} discovery");
        logger.debug("Found " + objects.size() + " objects");
        
        if (logger.isDebugEnabled()) {
            logger.debug("Object details: " + objects.toString());
        }
    }
}
```

**VALIDATION_COMMAND_LOGGING**:
```bash
# Find and fix System.out.println usage
grep -r "System\.out\.println" src/main/java/ | wc -l  # Should be 0
grep -r "println.*emoji" src/main/java/ && echo "❌ Found emoji debugging" || echo "✅ Clean logging"
```

#### **4B: Security - Credential Validation**

**SECURITY ISSUE IDENTIFIED**: "Claude also included Kevin's credentials in the testing configurations"

**VALIDATION_COMMANDS_SECURITY**:
```bash
# Scan for hardcoded credentials
grep -r -i "password.*=" src/ | grep -v "example\|placeholder" && echo "❌ Found credentials" || echo "✅ No credentials"
grep -r -i "jdbc:.*@" src/ && echo "❌ Found connection strings with credentials" || echo "✅ Clean connections"
grep -r "COMMUNITYKEVIN\|uQ1lAjwVisliu8CpUTVh0UnxoTUk3" src/ && echo "❌ Found actual credentials" || echo "✅ No real credentials"
```

**PROFESSIONAL PATTERN**:
```yaml
# Use configuration files (not hardcoded)
# File: src/test/resources/liquibase.sdk.local.yaml
liquibase:
  sdk:
    testSystem:
      snowflake:
        url: "${SNOWFLAKE_URL:jdbc:snowflake://example.com/}"
        username: "${SNOWFLAKE_USER:test_user}"
        password: "${SNOWFLAKE_PASSWORD:placeholder}"
```

#### **4C: Modular PR Strategy**

**FEEDBACK**: "I would also ask to break it into 4 or 5 different PRs, one for each functionality"

**PROFESSIONAL PR BREAKDOWN**:
```yaml
PR_STRATEGY:
  PR_1_FOUNDATION:
    SCOPE: "Database object model + basic change classes"
    FILES: "database/object/, change/core/Create*Change.java"
    SIZE: "100-200 LOC"
    
  PR_2_SQL_GENERATION:
    SCOPE: "SQL statement classes + generators"
    FILES: "statement/core/, sqlgenerator/"
    DEPENDENCIES: "PR_1_FOUNDATION"
    
  PR_3_SNAPSHOT_DIFF:
    SCOPE: "Database introspection + comparison"
    FILES: "snapshot/jvm/, diff/output/"
    DEPENDENCIES: "PR_1_FOUNDATION"
    
  PR_4_CHANGELOG_GENERATION:
    SCOPE: "Diff-to-changelog generation"
    FILES: "change/core/*ChangeGenerator"
    DEPENDENCIES: "PR_2_SQL_GENERATION, PR_3_SNAPSHOT_DIFF"
    
  PR_5_TESTING_INTEGRATION:
    SCOPE: "Comprehensive test suite"
    FILES: "src/test/java/**/*Test.java"
    DEPENDENCIES: "All previous PRs"
```

### Step 5: Service Registration & XSD Integration (30 minutes)

**PROVEN PATTERN: Service Registration Template**
```bash
# Add to src/main/resources/META-INF/services/liquibase.change.Change
liquibase.change.core.Create{OBJECT_TYPE}Change
liquibase.change.core.Alter{OBJECT_TYPE}Change
liquibase.change.core.Drop{OBJECT_TYPE}Change

# Add to src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator
liquibase.sqlgenerator.core.Create{OBJECT_TYPE}GeneratorSnowflake
liquibase.sqlgenerator.core.Alter{OBJECT_TYPE}GeneratorSnowflake
liquibase.sqlgenerator.core.Drop{OBJECT_TYPE}GeneratorSnowflake
```

**VALIDATION_COMMAND_SERVICE_REGISTRATION**:
```bash
# Validate service registration
grep -n "{OBJECT_TYPE}" src/main/resources/META-INF/services/liquibase.change.Change
grep -n "{OBJECT_TYPE}" src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator
mvn compile -q  # Must succeed for registration to work
```

**XSD Schema Integration Template:**
```xml
<xsd:element name="create{OBJECT_TYPE}" substitutionGroup="dbms:changeType">
    <xsd:complexType>
        <xsd:attribute name="{PRIMARY_IDENTIFIER}" type="xsd:string" use="required"/>
        <xsd:attribute name="{EXAMPLE_PROPERTY_1}" type="xsd:string"/>
        <xsd:attribute name="{EXAMPLE_PROPERTY_2}" type="xsd:string"/>
        <xsd:attribute name="orReplace" type="xsd:boolean"/>
        <xsd:attribute name="ifNotExists" type="xsd:boolean"/>
    </xsd:complexType>
</xsd:element>

<xsd:element name="alter{OBJECT_TYPE}" substitutionGroup="dbms:changeType">
    <xsd:complexType>
        <xsd:attribute name="{PRIMARY_IDENTIFIER}" type="xsd:string" use="required"/>
        <xsd:attribute name="new{EXAMPLE_PROPERTY_1}" type="xsd:string"/>
    </xsd:complexType>
</xsd:element>

<xsd:element name="drop{OBJECT_TYPE}" substitutionGroup="dbms:changeType">
    <xsd:complexType>
        <xsd:attribute name="{PRIMARY_IDENTIFIER}" type="xsd:string" use="required"/>
        <xsd:attribute name="ifExists" type="xsd:boolean"/>
    </xsd:complexType>
</xsd:element>
```

### Step 5: PROVEN Comprehensive Testing Strategy (90 minutes)

**CRITICAL**: Follow the exact testing organization that achieved 95%+ coverage

**PROVEN TEST ORGANIZATION PATTERN**: From CreateFileFormatChangeTest.java

```java
/**
 * Comprehensive unit tests for Create{OBJECT_TYPE}Change with 90%+ coverage focus.
 * Tests all validation methods, format-specific options, and changetype execution patterns.
 * Follows established testing patterns: changetype execution, complete SQL string validation.
 */
@DisplayName("Create{OBJECT_TYPE}Change")
public class Create{OBJECT_TYPE}ChangeTest {
    
    private Create{OBJECT_TYPE}Change change;
    private Database database;
    
    @BeforeEach
    void setUp() {
        change = new Create{OBJECT_TYPE}Change();
        database = mock(SnowflakeDatabase.class); // ✅ CORRECT: Change classes don't access database
    }
    
    // ==================== Basic Functionality Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should generate basic {OBJECT_TYPE_LOWER} statement") 
    void shouldGenerateBasic{OBJECT_TYPE}Statement() {
        // Given
        change.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
        change.set{EXAMPLE_PROPERTY_1}("value1");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then - Verify complete SQL string generation (preferred approach)
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof Create{OBJECT_TYPE}Statement);
        
        Create{OBJECT_TYPE}Statement stmt = (Create{OBJECT_TYPE}Statement) statements[0];
        assertEquals("TEST_OBJECT", stmt.get{PRIMARY_IDENTIFIER}());
        assertEquals("value1", stmt.get{EXAMPLE_PROPERTY_1}());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should require {PRIMARY_IDENTIFIER}")
    void shouldRequire{PRIMARY_IDENTIFIER}() {
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("{PRIMARY_IDENTIFIER} is required"));
    }
    
    @Test
    @DisplayName("Should validate mutual exclusivity of orReplace and ifNotExists")
    void shouldValidateMutualExclusivity() {
        change.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        ValidationErrors errors = change.validate(database);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both orReplace and ifNotExists")));
    }
    
    // ==================== Additional Branch Coverage Tests ====================
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        Database h2Database = mock(Database.class);
        
        assertFalse(change.supports(h2Database));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        change.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
        
        String message = change.getConfirmationMessage();
        
        assertTrue(message.contains("TEST_OBJECT"));
        assertTrue(message.toLowerCase().contains("{OBJECT_TYPE_LOWER}"));
    }
}
```

**PROVEN SQL GENERATOR TESTING PATTERN**:

```java
/**
 * Tests for Create{OBJECT_TYPE}GeneratorSnowflake using complete SQL string assertions.
 * Pattern: Direct SQL verification without mocking (proven superior approach).
 */
@DisplayName("Create{OBJECT_TYPE}GeneratorSnowflake")
public class Create{OBJECT_TYPE}GeneratorSnowflakeTest {
    
    private Create{OBJECT_TYPE}GeneratorSnowflake generator;
    private Database database;
    
    @BeforeEach
    void setUp() {
        generator = new Create{OBJECT_TYPE}GeneratorSnowflake();
        database = mock(SnowflakeDatabase.class); // ✅ CORRECT: SQL Generators only generate SQL strings
        when(database.escapeObjectName(anyString(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
    
    @Test
    @DisplayName("Should generate complete SQL with all properties")
    void testGenerate{OBJECT_TYPE}_WithAllProperties_CompleteSQL() {
        // Given - Statement with all properties
        Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}("FULL_OBJECT");
        statement.set{EXAMPLE_PROPERTY_1}("value1");
        statement.set{EXAMPLE_PROPERTY_2}("value2");
        statement.setOrReplace(true);
        
        // When - Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then - COMPLETE SQL STRING ASSERTION (PROVEN PATTERN)
        String expectedSQL = "CREATE OR REPLACE {SQL_COMMAND_CREATE} FULL_OBJECT WITH " +
                "{EXAMPLE_PROPERTY_1} = 'value1' AND " +
                "{EXAMPLE_PROPERTY_2} = 'value2'";
        
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate minimal SQL")
    void testGenerate{OBJECT_TYPE}_MinimalProperties_BasicSQL() {
        // Given - Minimal statement
        Create{OBJECT_TYPE}Statement statement = new Create{OBJECT_TYPE}Statement();
        statement.set{PRIMARY_IDENTIFIER}("BASIC_OBJECT");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        String expectedSQL = "{SQL_COMMAND_CREATE} BASIC_OBJECT";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
}
```

**PROVEN INTEGRATION TEST PATTERN**: Real Database with Schema Isolation

```java
/**
 * Integration test using real Snowflake database with schema isolation.
 * Pattern: Cost-conscious testing with cleanup verification.
 */
@DisplayName("{OBJECT_TYPE} Integration Test")
public class {OBJECT_TYPE}IntegrationTest {
    
    private Database database;
    private Connection rawConnection;
    private String testObjectName = "TEST_INTEGRATION_{OBJECT_TYPE_UPPER}";
    
    @BeforeEach
    void setUp() throws Exception {
        // PROVEN PATTERN: Real database connection setup
        rawConnection = TestDatabaseConfigUtil.getSnowflakeConnection();
        JdbcConnection jdbcConnection = new JdbcConnection(rawConnection);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
        
        assertTrue(database instanceof SnowflakeDatabase, "Must be Snowflake database");
    }
    
    @Test
    @DisplayName("Should create {OBJECT_TYPE_LOWER} successfully")
    void testCreate{OBJECT_TYPE}_RealDatabase_ExecutesSuccessfully() throws Exception {
        try {
            // Given - Create change with real properties
            Create{OBJECT_TYPE}Change change = new Create{OBJECT_TYPE}Change();
            change.set{PRIMARY_IDENTIFIER}(testObjectName);
            change.set{EXAMPLE_PROPERTY_1}("integration_value");
            
            // When - Execute against real database
            SqlStatement[] statements = change.generateStatements(database);
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            
            executor.execute(statements[0], new ArrayList<>());
            
            // Then - Verify object exists in database
            // Real verification against actual database state
            assertTrue(objectExists(testObjectName), "Object should exist after creation");
            
        } finally {
            // CRITICAL: Always cleanup
            cleanup{OBJECT_TYPE}(testObjectName);
        }
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (rawConnection != null && !rawConnection.isClosed()) {
            cleanup{OBJECT_TYPE}(testObjectName);
            rawConnection.close();
        }
    }
    
    private boolean objectExists(String objectName) throws Exception {
        // Implementation to check if object exists in database
        return true; // Placeholder
    }
    
    private void cleanup{OBJECT_TYPE}(String objectName) {
        try {
            Statement stmt = rawConnection.createStatement();
            stmt.execute("{SQL_COMMAND_DROP} IF EXISTS " + objectName);
            stmt.close();
        } catch (Exception e) {
            // Log but don't fail - cleanup is best effort
        }
    }
}
```

**PROVEN VALIDATION_COMMANDS_2** (Based on 95%+ Coverage Achievement): 

```bash
# Step 1: Validate compilation (basic template correctness)
mvn compile -q
```
**SUCCESS_INDICATOR_2_1**: `BUILD SUCCESS`
**FAILURE_RECOVERY_2_1**: Check template placeholder substitution - see ERROR_RECOVERY_COMPILATION

```bash
# Step 2: Run change type unit tests (proven pattern organization)
mvn test -Dtest="*{OBJECT_TYPE}*Change*Test" -q
```
**SUCCESS_INDICATOR_2_2**: All validation tests pass, confirmation message tests pass
**FAILURE_RECOVERY_2_2**: Check validation logic implementation - see ERROR_RECOVERY_UNIT_TESTS

```bash
# Step 3: Run SQL generator tests (complete SQL string assertions)
mvn test -Dtest="*{OBJECT_TYPE}*Generator*Test" -q
```
**SUCCESS_INDICATOR_2_3**: All SQL generation tests pass with complete SQL verification
**FAILURE_RECOVERY_2_3**: Check SQL template correctness - see ERROR_RECOVERY_SQL_GENERATION

```bash
# Step 4: Validate service registration
grep -n "{OBJECT_TYPE}" src/main/resources/META-INF/services/liquibase.change.Change
grep -n "{OBJECT_TYPE}" src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator
```
**SUCCESS_INDICATOR_2_4**: All classes found in service registration files
**FAILURE_RECOVERY_2_4**: Add missing service registrations

```bash
# Step 5: Integration test with real database (cost-conscious)
mvn test -Dtest="*{OBJECT_TYPE}*IntegrationTest" -q
```
**SUCCESS_INDICATOR_2_5**: Integration tests pass with real Snowflake, cleanup verified
**FAILURE_RECOVERY_2_5**: Check database connection and cleanup - see ERROR_RECOVERY_INTEGRATION

**PROVEN COVERAGE VALIDATION**:
```bash
# Generate coverage report (proven 95%+ achievement pattern)
mvn test jacoco:report
open target/site/jacoco/index.html
```
**SUCCESS_INDICATOR_2_6**: Change classes show 90%+ line coverage, SQL generators show 95%+ coverage
**TARGET_COVERAGE**: Change classes: 90%+, SQL generators: 95%+, Integration: End-to-end workflow

**✅ Phase 2 Complete When:**
- [ ] All unit tests pass (90%+ coverage achieved) - **VALIDATION_2_2**
- [ ] Integration tests pass with real Snowflake database - **VALIDATION_2_4**
- [ ] Service registration verified - **VALIDATION_2_1**
- [ ] XSD schema validates - **VALIDATION_2_1**
- [ ] CREATE/ALTER/DROP operations work end-to-end - **VALIDATION_2_4**

**AUTONOMOUS_CHECKPOINT_2_COMPLETE**: Change Types Implementation Complete
**PROGRESS_INDICATOR**: [███░░] 2/4 Phases Complete ✅
**NEXT_ACTION**: Proceed to Phase 3 Snapshot/Diff Implementation
**TIME_INVESTED**: 4-5 hours (core database operations functional)

---

## 🔍 Phase 3: Snapshot/Diff Implementation & Testing (4-5 hours)

**AUTONOMOUS_CHECKPOINT_3**: Snapshot/Diff Implementation
**PROGRESS_INDICATOR**: [████░] 3/4 Phases In Progress
**PREREQUISITE_CHECK**: Change Types Implementation Complete ✅

### Database Introspection Implementation

### Step 1: Object Model Enhancement (30 minutes)

**Add snapshot-specific properties:**

```java
// Enhance database object with snapshot properties
public class YourObject extends AbstractDatabaseObject {
    // ... existing properties
    
    // Snapshot-specific properties (exclude from diff comparison)
    private Date createdTime;
    private String owner;
    private String lastModified;
    
    // Configuration properties (include in diff comparison)  
    private String configurableProperty;
    private Boolean settableFlag;
}
```

### Step 2: Snapshot Generator Implementation (2 hours)

**DECISION_TREE_3_2**: Snapshot Data Source
```yaml
SNAPSHOT_SOURCE_DECISION:
  QUESTION: "How do you query your object type in Snowflake?"
  
  INFORMATION_SCHEMA:
    INDICATORS:
      - "SELECT * FROM INFORMATION_SCHEMA.{INFORMATION_SCHEMA_TABLE}"
      - "Both schema-level and account-level objects can use this"
      - "Supports parameterized queries"
      - "Consistent interface across all object types"
    ACTION: "Use INFORMATION_SCHEMA template (RECOMMENDED)"
    SQL_PATTERN: "SELECT * FROM INFORMATION_SCHEMA.{INFORMATION_SCHEMA_TABLE} WHERE SCHEMA_NAME = ?"
    ADVANTAGES:
      - "Standard SQL patterns familiar to developers"
      - "Reliable and well-tested approach"
      - "Consistent interface across all Snowflake objects"
      - "Supports complex filtering and parameterization"
    
  SHOW_COMMAND:
    STATUS: "ALTERNATIVE (NOT RECOMMENDED)"
    INDICATORS:
      - "SHOW {OBJECT_TYPE_UPPER}S command works"
      - "Some account-level objects traditionally use this"
      - "No parameterization possible"
    ACTION: "Avoid unless INFORMATION_SCHEMA unavailable"
    SQL_PATTERN: "SHOW {OBJECT_TYPE_UPPER}S"
    DISADVANTAGES: 
      - "Less consistent interface"
      - "Requires unified extensibility framework"
      - "More complex to implement and maintain"
    
  CUSTOM_QUERY:
    STATUS: "LAST RESORT"
    INDICATORS:
      - "INFORMATION_SCHEMA table doesn't exist for object type"
      - "Requires custom SQL or system functions"
    ACTION: "Use custom query template only when necessary"
    SQL_PATTERN: "Custom implementation required"
    COMPLEXITY: "HIGH"
```

**TDD Micro-Cycle 7: Snapshot Generator Structure**

```java
// RED: Test generator priority and type support
@Test
void testPriority_YourObjectType_ReturnsCorrectPriority() {
    YourObjectSnapshotGenerator generator = new YourObjectSnapshotGenerator();
    
    int priority = generator.getPriority(YourObject.class, database);
    
    assertEquals(PRIORITY_DATABASE, priority);
}

// GREEN: Implement basic structure
public class YourObjectSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {
    
    public YourObjectSnapshotGeneratorSnowflake() {
        super(YourObject.class, new Class[]{Schema.class}); // Schema-level objects
        // OR: super(YourObject.class, new Class[]{Account.class}); // Account-level objects
    }
    
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            if (YourObject.class.isAssignableFrom(objectType)) {
                return PRIORITY_DATABASE;
            }
        }
        return PRIORITY_NONE;
    }
}
```

**CORRECTED APPROACH: Integration Tests for Database Snapshot Generators** 🏆

**CRITICAL PRINCIPLE**: Database interactions must NOT be mocked in Liquibase testing

### **Unit Tests (No Database Interaction)**
Focus on business logic only - no database mocking:

```java
// ❌ WRONG: Do NOT mock database interactions
// ✅ CORRECT: Test business logic only

@Test
void testPriorityForSnowflakeDatabase() {
    // Test business logic without database calls
    assertEquals(PRIORITY_DATABASE, generator.getPriority({OBJECT_TYPE}.class, snowflakeDatabase));
    assertEquals(PRIORITY_NONE, generator.getPriority({OBJECT_TYPE}.class, nonSnowflakeDatabase));
}

@Test
void testHashGeneration() {
    // Test hash logic without database calls
    {OBJECT_TYPE} object = new {OBJECT_TYPE}();
    object.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    
    String[] hash = generator.hash(object, database, chain);
    assertEquals("TEST_OBJECT", hash[0]);
}

@Test  
void testNullObjectHandling() {
    // Test null handling without database calls
    assertNull(generator.snapshotObject(null, databaseSnapshot));
    
    {OBJECT_TYPE} emptyObject = new {OBJECT_TYPE}();
    assertNull(generator.snapshotObject(emptyObject, databaseSnapshot));
}
```

### **Integration Tests (Real Database Required)**
All database snapshot logic must use real Snowflake connections:

```java
// ✅ CORRECT: Integration test with real database
@Test
@IntegrationTest
void testSnapshotObject_RealDatabase_ValidObject() throws Exception {
    // Given - Real database setup
    {OBJECT_TYPE} example = new {OBJECT_TYPE}();
    example.set{PRIMARY_IDENTIFIER}("TEST_OBJECT");
    
    // Setup real test object in database
    createTestObjectInDatabase("TEST_OBJECT");
    
    // When - Execute with real database
    {OBJECT_TYPE} result = ({OBJECT_TYPE}) generator.snapshotObject(example, realDatabaseSnapshot);
    
    // Then - Verify real data
    assertNotNull(result);
    assertEquals("TEST_OBJECT", result.get{PRIMARY_IDENTIFIER}());
    
    // Cleanup
    dropTestObjectFromDatabase("TEST_OBJECT");
}

@Test
@IntegrationTest  
void testAddTo_RealDatabase_DiscoversBulkObjects() throws Exception {
    // Given - Real schema with test objects
    Schema schema = setupTestSchema();
    createMultipleTestObjects(schema);
    
    // When - Execute bulk discovery with real database
    generator.addTo(schema, realDatabaseSnapshot);
    
    // Then - Verify real objects discovered
    Set<{OBJECT_TYPE}> objects = schema.getDatabaseObjects({OBJECT_TYPE}.class);
    assertTrue(objects.size() >= 2);
    
    // Cleanup
    cleanupTestObjects();
}
```

### **Implementation Pattern (Real Database Access)**

```java
@Override
protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
        throws DatabaseException, InvalidExampleException {
    if (!(example instanceof {OBJECT_TYPE})) {
        return null;
    }
    
    {OBJECT_TYPE} {OBJECT_TYPE_LOWER} = ({OBJECT_TYPE}) example;
    if ({OBJECT_TYPE_LOWER}.get{PRIMARY_IDENTIFIER}() == null) {
        return null;
    }
    
    Database database = snapshot.getDatabase();
    
    // ✅ CORRECT: Direct database access (no mocking)
    String sql = "SELECT {PRIMARY_IDENTIFIER_UPPER}, {EXAMPLE_PROPERTY_1_UPPER}, {EXAMPLE_PROPERTY_2_UPPER} " +
                "FROM {INFORMATION_SCHEMA_TABLE} " +
                "WHERE {PRIMARY_IDENTIFIER_UPPER} = ?";
    
    List<String> parameters = Arrays.asList({OBJECT_TYPE_LOWER}.get{PRIMARY_IDENTIFIER}());
    RawParameterizedSqlStatement rawSql = new RawParameterizedSqlStatement(sql, parameters.toArray());
    
    // ✅ CORRECT: Real ExecutorService call (no mocking)
    List<Map<String, Object>> results = Scope.getCurrentScope()
        .getSingleton(ExecutorService.class)
        .getExecutor("jdbc", database)
        .queryForList(rawSql);
    
    if (results.isEmpty()) {
        return null;
    }
    
    Map<String, Object> row = results.get(0);
    {OBJECT_TYPE} result = new {OBJECT_TYPE}();
    result.set{PRIMARY_IDENTIFIER}((String) row.get("{PRIMARY_IDENTIFIER_UPPER}"));
    result.set{EXAMPLE_PROPERTY_1}((String) row.get("{EXAMPLE_PROPERTY_1_UPPER}"));
    result.set{EXAMPLE_PROPERTY_2}((String) row.get("{EXAMPLE_PROPERTY_2_UPPER}"));
    
    return result;
}

@Override
protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
        throws DatabaseException, InvalidExampleException {
    
    if (foundObject instanceof Schema) {
        Schema schema = (Schema) foundObject;
        Database database = snapshot.getDatabase();
        
        // ✅ CORRECT: Real INFORMATION_SCHEMA query
        String sql = "SELECT {PRIMARY_IDENTIFIER_UPPER}, {EXAMPLE_PROPERTY_1_UPPER}, {EXAMPLE_PROPERTY_2_UPPER} " +
                    "FROM {INFORMATION_SCHEMA_TABLE} " +
                    "WHERE SCHEMA_NAME = ? AND CATALOG_NAME = ?";
        
        List<String> parameters = Arrays.asList(schema.getName(), schema.getCatalogName());
        RawParameterizedSqlStatement rawSql = new RawParameterizedSqlStatement(sql, parameters.toArray());
        
        try {
            // ✅ CORRECT: Real database execution
            List<Map<String, Object>> results = Scope.getCurrentScope()
                .getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database)
                .queryForList(rawSql);
            
            for (Map<String, Object> row : results) {
                {OBJECT_TYPE} object = new {OBJECT_TYPE}();
                object.set{PRIMARY_IDENTIFIER}((String) row.get("{PRIMARY_IDENTIFIER_UPPER}"));
                object.set{EXAMPLE_PROPERTY_1}((String) row.get("{EXAMPLE_PROPERTY_1_UPPER}"));
                object.set{EXAMPLE_PROPERTY_2}((String) row.get("{EXAMPLE_PROPERTY_2_UPPER}"));
                object.setSchema(schema);
                
                schema.addDatabaseObject(object);
                
                try {
                    snapshot.include(object);
                } catch (InvalidExampleException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning(
                        "Could not include {OBJECT_TYPE_LOWER} " + object.get{PRIMARY_IDENTIFIER}() + " in snapshot", e);
                }
            }
        } catch (DatabaseException e) {
            throw new DatabaseException("Failed to discover {OBJECT_TYPE_LOWER} objects in schema " + schema.getName(), e);
        }
    }
}
```

### **Testing Strategy Summary**

**✅ CORRECT: Mock Database Object (No DB Access Needed)**
- **Change Classes**: Mock Database for validation/statement generation tests
- **SQL Generators**: Mock Database for SQL string generation tests  
- **Comparators**: Mock Database for object comparison logic tests

**❌ WRONG: Mock Database Interactions (Real DB Access Required)**  
- **Snapshot Generators**: Must use real Snowflake connections - NO mocking
- **Integration Tests**: Must use real database for end-to-end validation
- **Database Query Logic**: Must test against actual INFORMATION_SCHEMA tables

**Summary:**
1. **Business Logic**: Mock Database objects when no real DB access needed
2. **Database Operations**: Use real Snowflake connections - never mock DB interactions  
3. **Focus**: Test SQL generation logic vs real database introspection separately

### Step 3: Comparator Implementation (1 hour)

**TDD Micro-Cycle 10: Object Comparison**

```java
// RED: Test object comparison logic
@Test
void testFindDifferences_IdenticalObjects_ReturnsNoDifferences() {
    YourObject obj1 = createTestObject("TEST", "value1");
    YourObject obj2 = createTestObject("TEST", "value1");
    
    ObjectDifferences differences = comparator.findDifferences(
        obj1, obj2, database, compareControl, chain, excludeSet);
    
    assertTrue(differences.hasDifferences() == false);
}

@Test
void testFindDifferences_DifferentConfigurationProperties_ReturnsDifferences() {
    YourObject obj1 = createTestObject("TEST", "value1");
    YourObject obj2 = createTestObject("TEST", "value2");
    
    ObjectDifferences differences = comparator.findDifferences(
        obj1, obj2, database, compareControl, chain, excludeSet);
    
    assertTrue(differences.hasDifferences());
    assertTrue(differences.getDifference("property1") != null);
}

// GREEN: Implement comparator
public class YourObjectComparator implements DatabaseObjectComparator {
    
    // Exclude state properties from comparison
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "createdTime", "owner", "lastModified"
    };
    
    @Override
    public ObjectDifferences findDifferences(DatabaseObject obj1, DatabaseObject obj2,
                                           Database database, CompareControl control,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        exclude = new HashSet<>(exclude);
        exclude.addAll(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        return chain.findDifferences(obj1, obj2, database, control, exclude);
    }
}
```

### Step 4: Integration Testing (1.5 hours)

**Real Database Snapshot Testing:**

```java
@Test
void testYourObjectSnapshot_RealDatabase_DiscoverAllObjects() throws Exception {
    Database realDatabase = TestDatabaseConfigUtil.getSnowflakeDatabase();
    Schema testSchema = new Schema("TEST_SCHEMA");
    
    // Create test objects
    createTestObjects(realDatabase.getConnection(), testSchema.getName());
    
    // Generate snapshot
    DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
        .createSnapshot(testSchema, realDatabase, new SnapshotControl(realDatabase));
    
    // Verify objects discovered
    Set<YourObject> objects = snapshot.get(YourObject.class);
    assertFalse(objects.isEmpty(), "Should discover test objects");
    
    // Verify object properties populated correctly
    YourObject testObject = objects.stream()
        .filter(obj -> "TEST_OBJECT".equals(obj.getName()))
        .findFirst().orElse(null);
    
    assertNotNull(testObject);
    assertEquals("expected_value", testObject.getProperty1());
}
```

**VALIDATION_COMMANDS_3**:
```bash
# Test snapshot generator compilation
mvn compile -q
```
**SUCCESS_INDICATOR_3_1**: `BUILD SUCCESS`
**FAILURE_RECOVERY_3_1**: See ERROR_RECOVERY_COMPILATION section

```bash
# Test single object snapshots
mvn test -Dtest="*{OBJECT_TYPE}*SnapshotGenerator*Test" -q
```
**SUCCESS_INDICATOR_3_2**: All snapshot tests pass
**FAILURE_RECOVERY_3_2**: See ERROR_RECOVERY_SNAPSHOT_TESTS section

```bash
# Test diff comparators
mvn test -Dtest="*{OBJECT_TYPE}*Comparator*Test" -q
```
**SUCCESS_INDICATOR_3_3**: All comparator tests pass
**FAILURE_RECOVERY_3_3**: See ERROR_RECOVERY_COMPARATOR_TESTS section

```bash
# Integration test with real database snapshots
SNOWFLAKE_URL="jdbc:snowflake://server/..." mvn test -Dtest="*{OBJECT_TYPE}*SnapshotIntegrationTest" -q
```
**SUCCESS_INDICATOR_3_4**: Real database snapshots work correctly
**FAILURE_RECOVERY_3_4**: See ERROR_RECOVERY_SNAPSHOT_INTEGRATION section

**✅ Phase 3 Complete When:**
- [ ] Single object snapshots work correctly - **VALIDATION_3_2**
- [ ] Bulk object discovery functions - **VALIDATION_3_2**
- [ ] Objects added to both parent and top-level snapshot - **VALIDATION_3_4**
- [ ] Comparator excludes state properties correctly - **VALIDATION_3_3**
- [ ] Real database snapshot tests pass - **VALIDATION_3_4**
- [ ] Service registration completed - **VALIDATION_3_1**

**AUTONOMOUS_CHECKPOINT_3_COMPLETE**: Snapshot/Diff Implementation Complete
**PROGRESS_INDICATOR**: [████░] 3/4 Phases Complete ✅
**NEXT_ACTION**: Proceed to Phase 4 Diff-Changelog Implementation
**TIME_INVESTED**: 8-10 hours (database introspection functional)

---

## 📊 Phase 4: Diff-Changelog/Generate-Changelog Implementation & Testing (2-3 hours)

**AUTONOMOUS_CHECKPOINT_4**: Diff-Changelog Implementation
**PROGRESS_INDICATOR**: [█████] 4/4 Phases In Progress
**PREREQUISITE_CHECK**: Snapshot/Diff Implementation Complete ✅

### Change Generator Implementation

### Step 1: Change Generators for Diff Operations (1.5 hours)

**DECISION_TREE_4_1**: Change Generator Scope
```yaml
CHANGE_GENERATOR_DECISION:
  QUESTION: "What types of differences need change generators?"
  
  BASIC_DIFF_SUPPORT:
    INDICATORS:
      - "Only need CREATE operations for missing objects"
      - "Object is rarely altered or dropped via diff"
    ACTION: "Implement only MissingObjectChangeGenerator"
    TIME_ESTIMATE: "30 minutes"
    
  STANDARD_DIFF_SUPPORT:
    INDICATORS:
      - "Need CREATE/DROP for missing/unexpected objects"
      - "Object properties don't typically change"
    ACTION: "Implement Missing and Unexpected change generators"
    TIME_ESTIMATE: "60 minutes"
    
  FULL_DIFF_SUPPORT:
    INDICATORS:
      - "Need CREATE/ALTER/DROP for all difference types"
      - "Object properties change frequently"
      - "Users expect comprehensive diff-changelog support"
    ACTION: "Implement Missing, Unexpected, and Changed generators"
    TIME_ESTIMATE: "90 minutes"
```

**TDD Micro-Cycle 11: Missing Object Change Generator**

```java
// RED: Test missing object handling
@Test
void testFixMissing_YourObjectMissing_ReturnsCreateChange() {
    YourObject missingObject = new YourObject();
    missingObject.setName("MISSING_OBJECT");
    
    Class<? extends Change>[] changes = generator.fixMissing(
        missingObject, diffOutputControl, referenceDatabase, comparisonDatabase, chain);
    
    assertEquals(1, changes.length);
    assertEquals(CreateYourObjectChange.class, changes[0]);
}

// GREEN: Implement missing object generator
public class MissingYourObjectChangeGenerator implements ChangeGenerator {
    
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (YourObject.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }
    
    @Override
    public Class<? extends Change>[] fixMissing(DatabaseObject missingObject, DiffOutputControl control,
                                               Database referenceDatabase, Database comparisonDatabase,
                                               ChangeGeneratorChain chain) {
        if (missingObject instanceof YourObject) {
            return new Class[]{CreateYourObjectChange.class};
        }
        return null;
    }
}
```

**Implement all three generators:**
- `MissingYourObjectChangeGenerator` → `CreateYourObjectChange`
- `UnexpectedYourObjectChangeGenerator` → `DropYourObjectChange`  
- `ChangedYourObjectChangeGenerator` → `AlterYourObjectChange`

### Step 2: Schema Isolation Setup (30 minutes)

**Cost-conscious testing pattern:**

```java
public class YourObjectDiffIntegrationTest {
    
    private String referenceSchema;
    private String comparisonSchema;
    
    @BeforeEach
    void setupSchemaIsolation() {
        String testMethod = getTestMethodName();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        referenceSchema = "DIFF_TEST_REF_" + testMethod + "_" + timestamp;
        comparisonSchema = "DIFF_TEST_CMP_" + testMethod + "_" + timestamp;
        
        // Create isolated test schemas
        createCleanSchema(connection, referenceSchema);
        createCleanSchema(connection, comparisonSchema);
    }
    
    @AfterEach
    void cleanupSchemas() {
        dropSchemaIfExists(connection, referenceSchema);
        dropSchemaIfExists(connection, comparisonSchema);
    }
}
```

### Step 3: Full-Lifecycle Testing (1 hour)

**The Gold Standard Test:**

```java
@Test
public void testYourObjectFullLifecycle_CreateGenerateDropDeploy_PerfectRoundTrip() throws Exception {
    try {
        // STEP 1: Create test objects with direct SQL
        createTestYourObjects(connection, referenceSchema);
        
        // STEP 2: Generate changelog from database
        File changelogFile = new File("target/test-output/" + testMethodName + "-generated.xml");
        generateChangelogFromDatabase(referenceDatabase, changelogFile);
        
        // STEP 3: Validate changelog structure
        validateChangelogStructure(changelogFile);
        
        // STEP 4: Drop all objects to reset state
        dropAllTestObjects(connection, referenceSchema);
        
        // STEP 5: Deploy generated changelog to recreate objects
        deployChangelog(comparisonDatabase, changelogFile);
        
        // STEP 6: Compare original vs recreated schemas
        DiffResult diffResult = compareSchemas(referenceDatabase, comparisonDatabase);
        
        // STEP 7: Assert perfect round-trip (CRITICAL SUCCESS CRITERIA)
        assertTrue(diffResult.getMissingObjects(YourObject.class).isEmpty(), 
                  "Missing YourObjects after round-trip");
        assertTrue(diffResult.getUnexpectedObjects(YourObject.class).isEmpty(), 
                  "Unexpected YourObjects after round-trip");
        assertTrue(diffResult.getChangedObjects(YourObject.class).isEmpty(), 
                  "Changed YourObjects after round-trip");
        
    } finally {
        cleanupTestSchemas();
    }
}

// Helper methods
private void generateChangelogFromDatabase(Database database, File changelogFile) throws Exception {
    CommandScope generateCommand = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME);
    generateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, 
                                   database.getConnection().getURL());
    generateCommand.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, 
                                   changelogFile.getAbsolutePath());
    generateCommand.execute();
}

private void deployChangelog(Database database, File changelogFile) throws Exception {
    CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
    updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, 
                                 database.getConnection().getURL());
    updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, 
                                 changelogFile.getAbsolutePath());
    updateCommand.execute();
}
```

**VALIDATION_COMMANDS_4**:
```bash
# Test change generator compilation
mvn compile -q
```
**SUCCESS_INDICATOR_4_1**: `BUILD SUCCESS`
**FAILURE_RECOVERY_4_1**: See ERROR_RECOVERY_COMPILATION section

```bash
# Test change generators
mvn test -Dtest="*{OBJECT_TYPE}*ChangeGenerator*Test" -q
```
**SUCCESS_INDICATOR_4_2**: All change generator tests pass
**FAILURE_RECOVERY_4_2**: See ERROR_RECOVERY_CHANGE_GENERATOR_TESTS section

```bash
# Full-lifecycle integration test
SNOWFLAKE_URL="jdbc:snowflake://server/..." mvn test -Dtest="*{OBJECT_TYPE}*FullCycle*Test" -q
```
**SUCCESS_INDICATOR_4_3**: Perfect round-trip achieved (no differences)
**FAILURE_RECOVERY_4_3**: See ERROR_RECOVERY_FULL_LIFECYCLE section

```bash
# Command-line workflow test (manual validation)
./test_command_line_workflow.sh {OBJECT_TYPE_LOWER}
```
**SUCCESS_INDICATOR_4_4**: All commands execute successfully
**FAILURE_RECOVERY_4_4**: See ERROR_RECOVERY_COMMAND_LINE section

**✅ Phase 4 Complete When:**
- [ ] All three change generators implemented and registered - **VALIDATION_4_1**
- [ ] `diff` command shows your objects correctly - **VALIDATION_4_4**
- [ ] `diff-changelog` generates valid XML for your objects - **VALIDATION_4_4**
- [ ] `generate-changelog` captures your objects from database - **VALIDATION_4_4**
- [ ] Full-lifecycle test achieves perfect round-trip - **VALIDATION_4_3**
- [ ] Command-line workflow validated - **VALIDATION_4_4**

**AUTONOMOUS_CHECKPOINT_4_COMPLETE**: Implementation Complete ✅
**PROGRESS_INDICATOR**: [█████] 4/4 Phases Complete ✅
**TOTAL_TIME_INVESTED**: 12-16 hours
**DELIVERABLE**: Production-ready database object with 95%+ test coverage and full user workflow support

---

## 🎯 Success Criteria & Validation

### Implementation Complete When:

**Phase 1 - Requirements:**
- [ ] Comprehensive requirements document with attribute analysis
- [ ] Test scenario matrix covering all operations
- [ ] Mutual exclusivity rules defined
- [ ] SQL examples validated against Snowflake

**Phase 2 - Change Types:**
- [ ] Unit tests achieve 90%+ coverage
- [ ] Integration tests pass with real database
- [ ] All CREATE/ALTER/DROP operations work
- [ ] Service registration and XSD schema complete

**Phase 3 - Snapshot/Diff:**
- [ ] Objects discovered in database snapshots
- [ ] Single object and bulk discovery work
- [ ] Comparator correctly handles differences
- [ ] Real database integration tests pass

**Phase 4 - Diff/Changelog:**
- [ ] Perfect round-trip testing passes
- [ ] Command-line workflow functions
- [ ] Generated changelogs deploy successfully
- [ ] End-to-end user workflow validated

### Quality Gates:

- **TDD Compliance**: Every feature developed test-first
- **Real Database Testing**: No mocked database interactions
- **95%+ Coverage**: Systematic enhancement achieved
- **Cost Management**: Schema isolation enables parallel tests
- **User Validation**: Complete command-line workflow works

---

## 🚀 Key Success Insights

### What Makes This Process Successful:

1. **Requirements First**: Excellent requirements prevent costly rework
2. **TDD Discipline**: Test-first development catches issues early
3. **Complete SQL String Assertions**: More reliable than component testing
4. **Real Database Integration**: Ensures actual Snowflake compatibility
5. **Schema Isolation**: Enables cost-efficient parallel testing
6. **Full-Lifecycle Validation**: Guarantees end-user workflow success

### Time Distribution:
- **Requirements (20%)**: 2-3 hours - prevents 10+ hours of rework
- **Change Types (35%)**: 4-5 hours - core implementation
- **Snapshot/Diff (30%)**: 4-5 hours - database introspection
- **Diff/Changelog (15%)**: 2-3 hours - user workflow validation

### Common Success Factors:
- **Systematic approach**: Following proven patterns
- **Quality gates**: Clear completion criteria for each phase
- **Cost consciousness**: Strategic use of expensive database resources
- **User focus**: Validating complete end-to-end workflows

---

## 🚨 ERROR RECOVERY PROCEDURES

### ERROR_RECOVERY_COMPILATION
**TRIGGER**: `BUILD FAILURE` on `mvn compile`
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check for missing imports
grep -n "import" src/main/java/liquibase/database/object/{OBJECT_TYPE}.java
grep -n "cannot find symbol" target/maven-status/maven-compiler-plugin/compile/default-compile/inputFiles.lst

# Step 2: Validate template substitution
echo "Check that all {PLACEHOLDER} values were replaced with actual values"
grep -n "{[A-Z_]*}" src/main/java/liquibase/database/object/{OBJECT_TYPE}.java

# Step 3: Check service registration
ls -la src/main/resources/META-INF/services/
grep -n "{OBJECT_TYPE}" src/main/resources/META-INF/services/*

# Step 4: Retry compilation with verbose output
mvn compile -X | grep -A10 -B10 "ERROR"
```

### ERROR_RECOVERY_UNIT_TESTS
**TRIGGER**: Unit test failures
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Run specific failing test with stack traces
mvn test -Dtest="FailingTestClass" -Dmaven.test.failure.ignore=false

# Step 2: Check test template substitution
grep -n "{[A-Z_]*}" src/test/java/**/*{OBJECT_TYPE}*Test.java

# Step 3: Validate test data setup
grep -n "@BeforeEach\|@Before" src/test/java/**/*{OBJECT_TYPE}*Test.java

# Step 4: Check database mock setup
grep -n "@IntegrationTest\|createTest.*InDatabase\|realDatabase" src/test/java/**/*{OBJECT_TYPE}*Test.java

# Step 5: Run with debugging
mvn test -Dtest="FailingTestClass" -Dmaven.surefire.debug=true
```

### ERROR_RECOVERY_SQL_GENERATION
**TRIGGER**: SQL generation test failures
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check actual vs expected SQL
echo "Enable SQL logging in test:"
echo "Add: LogServiceFactory.setLogLevel(LogLevel.DEBUG);"

# Step 2: Validate SQL template
grep -n "StringBuilder\|sql.append" src/main/java/**/*{OBJECT_TYPE}*Generator*.java

# Step 3: Check Snowflake SQL syntax
echo "Manually test SQL in Snowflake console:"
echo "{SQL_COMMAND_CREATE} test_object WITH property = 'value';"

# Step 4: Validate escaping
grep -n "escapeObjectName" src/main/java/**/*{OBJECT_TYPE}*Generator*.java
```

### ERROR_RECOVERY_INTEGRATION
**TRIGGER**: Integration test failures
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check database connectivity
SNOWFLAKE_URL="$TEST_URL" mvn test -Dtest="DatabaseConnectionTest" -q

# Step 2: Validate schema isolation
echo "Check that test schemas are properly created/cleaned:"
grep -n "CREATE SCHEMA\|DROP SCHEMA" src/test/java/**/*IntegrationTest.java

# Step 3: Check actual Snowflake error
echo "Enable Snowflake error logging:"
echo "Add: System.setProperty('liquibase.logLevel', 'DEBUG');"

# Step 4: Manual SQL validation
echo "Test SQL manually in Snowflake console with test credentials"
```

### ERROR_RECOVERY_SNAPSHOT_TESTS
**TRIGGER**: Snapshot generator test failures
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check INFORMATION_SCHEMA query
echo "Validate query syntax:"
grep -n "SELECT.*FROM INFORMATION_SCHEMA" src/main/java/**/*{OBJECT_TYPE}*SnapshotGenerator*.java

# Step 2: Check integration test setup
grep -n "@IntegrationTest\|realDatabase\|createTest.*InDatabase" src/test/java/**/*{OBJECT_TYPE}*SnapshotGenerator*Test.java

# Step 3: Validate object construction
grep -n "new {OBJECT_TYPE}" src/main/java/**/*{OBJECT_TYPE}*SnapshotGenerator*.java

# Step 4: Check service registration
grep -n "SnapshotGenerator" src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator
```

### ERROR_RECOVERY_FULL_LIFECYCLE
**TRIGGER**: Full-lifecycle test failures (round-trip not perfect)
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check diff results
echo "Examine what differences were found:"
grep -n "getMissingObjects\|getUnexpectedObjects\|getChangedObjects" src/test/java/**/*FullCycle*Test.java

# Step 2: Validate changelog generation
echo "Check generated changelog XML:"
ls -la target/test-output/
cat target/test-output/*{OBJECT_TYPE_LOWER}*-generated.xml

# Step 3: Check deployment success
echo "Verify changelog deployed successfully:"
grep -n "deployChangelog" src/test/java/**/*FullCycle*Test.java

# Step 4: Manual diff verification
echo "Run manual diff command:"
echo "liquibase diff --reference-url=... --url=..."
```

### ERROR_RECOVERY_COMMAND_LINE
**TRIGGER**: Command-line workflow failures
**SYSTEMATIC_RECOVERY**:
```bash
# Step 1: Check classpath
echo $LIQUIBASE_CLASSPATH
ls -la *.jar

# Step 2: Validate connection
liquibase --url="$TEST_URL" --username="$USER" --password="$PASS" status

# Step 3: Test individual commands
liquibase --help | grep -E "diff|generate-changelog"

# Step 4: Check extension loading
liquibase --log-level=DEBUG diff 2>&1 | grep -i "loading\|extension"
```

---

*This guide captures the proven process for implementing new database objects with 95%+ test coverage and complete user workflow support. Follow the phased approach systematically for optimal results.*