# AI-Optimized Architectural Compliance Guide
## Automatic Detection and Prevention of Implementation Pattern Violations

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 1.0
DOCUMENT_TYPE: ARCHITECTURAL_COMPLIANCE
EXECUTION_MODE: VALIDATION_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "Architectural pattern violations during implementation"
  - "XSD schema completeness and compliance"
  - "Namespace attribute implementation requirements"
  - "AI consumption optimization for implementation decisions"
```

## CRITICAL: AUTOMATIC VIOLATION DETECTION

### NAMESPACE_ATTRIBUTE_EXTENSION vs DATABASE_SPECIFIC_CHANGE_CLASS

```yaml
VIOLATION_DETECTION_MATRIX:
  IF_EXISTS_LIQUIBASE_CHANGETYPE:
    RULE: "MUST use NAMESPACE_ATTRIBUTE_EXTENSION pattern"
    VIOLATION_INDICATORS:
      - "Creating class with name ending 'ChangeSnowflake'"
      - "Extending existing Liquibase change classes for database-specific behavior"
      - "Adding database-specific elements to XSD instead of namespace attributes"
    COMPLIANCE_INDICATORS:
      - "Defining namespace attributes in XSD schema"
      - "Utilizing existing SnowflakeNamespaceAwareXMLParser"
      - "Extending standard SQL generators, not creating new change classes"
      
  IF_NEW_CHANGETYPE_REQUIRED:
    RULE: "Use NEW_CHANGETYPE_PATTERN"
    COMPLIANCE_INDICATORS:
      - "No existing Liquibase changetype provides the functionality"
      - "Creating entirely new database objects or operations"
      - "Requires custom change class with database-specific business logic"
```

### AI-CONSUMABLE DECISION ALGORITHM

```yaml
STEP_1_CHANGETYPE_EXISTS_CHECK:
  QUESTION: "Does Liquibase core already have this changetype?"
  DETECTION_METHOD: "Search liquibase-core for existing Change class"
  IF_YES: "GOTO_NAMESPACE_ATTRIBUTE_EXTENSION"
  IF_NO: "GOTO_NEW_CHANGETYPE_PATTERN"

STEP_2_NAMESPACE_ATTRIBUTE_EXTENSION:
  REQUIRED_COMPONENTS:
    XSD_SCHEMA: "Define namespace attributes in liquibase-snowflake-latest.xsd"
    PARSER_SUPPORT: "Verify changetype in SnowflakeNamespaceAwareXMLParser.isTargetChangeType()"
    SQL_GENERATOR: "Extend standard generator, access namespace attributes"
    ATTRIBUTE_STORAGE: "Use SnowflakeNamespaceAttributeStorage for attribute access"
  FORBIDDEN_ACTIONS:
    - "Creating *ChangeSnowflake classes"
    - "Adding new elements to XSD for existing changetypes"
    - "Modifying Liquibase core change classes"

STEP_3_NEW_CHANGETYPE_PATTERN:
  REQUIRED_COMPONENTS:
    CHANGE_CLASS: "Create new *Change class for completely new functionality"
    STATEMENT_CLASS: "Create corresponding *Statement class"
    SQL_GENERATOR: "Create *Generator class for SQL generation"
    XSD_ELEMENT: "Define new element in XSD schema"
    SERVICE_REGISTRATION: "Register in META-INF/services files"
```

## XSD SCHEMA INTEGRATION REQUIREMENTS

### NAMESPACE ATTRIBUTE XSD PATTERNS

```yaml
NAMESPACE_ATTRIBUTE_DEFINITION:
  LOCATION: "src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
  PATTERN_TEMPLATE: |
    <!-- Snowflake namespace attributes (for use on standard changeTypes) -->
    <xsd:attribute name="attributeName" type="xsd:dataType">
        <xsd:annotation>
            <xsd:documentation>Clear description of attribute purpose and usage</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
  
  SUPPORTED_DATA_TYPES:
    - "xsd:boolean"
    - "xsd:string" 
    - "xsd:integer"
    - "xsd:decimal"
    
  VALIDATION_REQUIREMENTS:
    - "Attribute name must match Java property name"
    - "Data type must match expected Java type"
    - "Documentation must explain attribute purpose"
```

### PARSER INTEGRATION VALIDATION

```yaml
PARSER_COMPLIANCE_CHECKLIST:
  CHANGETYPE_SUPPORT:
    FILE: "src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java"
    METHOD: "isTargetChangeType(String localName)"
    REQUIREMENT: "Target changetype MUST be listed in method"
    VALIDATION: "Search for changetype name in isTargetChangeType method"
    
  OBJECT_NAME_EXTRACTION:
    METHOD: "getObjectName(String changeType, Attributes attributes)"
    REQUIREMENT: "MUST handle object name extraction for changetype"
    VALIDATION: "Verify case statement includes changetype"
    
  ATTRIBUTE_STORAGE:
    CLASS: "SnowflakeNamespaceAttributeStorage"
    REQUIREMENT: "Attributes MUST be stored using object name as key"
    VALIDATION: "Verify SQL generator can retrieve stored attributes"
```

## SQL GENERATOR INTEGRATION PATTERNS

### NAMESPACE ATTRIBUTE ACCESS PATTERN

```java
// CORRECT: Accessing namespace attributes in SQL generator
public class ChangeTypeGeneratorSnowflake extends ChangeTypeGenerator {
    
    @Override
    public Sql[] generateSql(ChangeTypeStatement statement, Database database, SqlGeneratorChain chain) {
        // Get base SQL from parent
        Sql[] baseSql = super.generateSql(statement, database, chain);
        
        // Access namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getObjectName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Enhance SQL with namespace attributes
            return enhanceWithNamespaceAttributes(baseSql, attributes);
        }
        
        return baseSql;
    }
}
```

### ARCHITECTURAL VIOLATION EXAMPLES

```java
// VIOLATION: Creating database-specific change class for existing changetype
@DatabaseChange(name = "createSequence", description = "Creates sequence")
public class CreateSequenceChangeSnowflake extends CreateSequenceChange {
    // THIS IS WRONG - violates namespace attribute pattern
}

// CORRECT: Using namespace attributes with standard changetype
// No custom change class needed - use standard createSequence with namespace attributes
```

## VALIDATION AUTOMATION

### AI-CONSUMABLE COMPLIANCE CHECKS

```yaml
AUTOMATED_VALIDATION_COMMANDS:
  CHECK_ARCHITECTURAL_VIOLATIONS:
    COMMAND: "find . -name '*ChangeSnowflake.java' -path '*/change/core/*'"
    EXPECTED_RESULT: "Empty output (no files found)"
    VIOLATION_ACTION: "Delete violating files, implement namespace pattern"
    
  VERIFY_NAMESPACE_PARSER_SUPPORT:
    COMMAND: "grep -n 'createChangeType' src/main/java/liquibase/parser/SnowflakeNamespaceAwareXMLParser.java"
    REQUIREMENT: "Target changetype must be found in isTargetChangeType method"
    
  VALIDATE_XSD_ATTRIBUTES:
    COMMAND: "grep -A 5 -B 1 'xsd:attribute.*name=\"newAttribute\"' src/main/resources/*.xsd"
    REQUIREMENT: "All namespace attributes must be defined in XSD"
    
  CHECK_SQL_GENERATOR_PATTERN:
    COMMAND: "grep -l 'SnowflakeNamespaceAttributeStorage' src/main/java/liquibase/sqlgenerator/core/*.java"
    REQUIREMENT: "SQL generators must access namespace attributes correctly"
```

### REQUIREMENTS PHASE VALIDATION

```yaml
REQUIREMENTS_ARCHITECTURAL_CHECKLIST:
  PATTERN_IDENTIFICATION:
    - "[ ] Determined if extending existing changetype or creating new one"
    - "[ ] Verified existing changetype exists in Liquibase core"
    - "[ ] Confirmed namespace attribute extension is appropriate pattern"
    
  XSD_SCHEMA_PLANNING:
    - "[ ] Planned namespace attribute definitions"
    - "[ ] Specified attribute names, types, and documentation"
    - "[ ] Verified parser integration requirements"
    
  IMPLEMENTATION_REQUIREMENTS:
    - "[ ] Documented SQL generator enhancement approach"
    - "[ ] Confirmed namespace attribute storage utilization"
    - "[ ] Planned testing strategy for namespace attributes"
    
  VIOLATION_PREVENTION:
    - "[ ] Confirmed NOT creating database-specific change classes"
    - "[ ] Verified XSD defines attributes, not new elements"
    - "[ ] Validated parser integration approach"
```

## AI CONSUMPTION OPTIMIZATION

### STRUCTURED DECISION PROTOCOLS

```yaml
AI_DECISION_FRAMEWORK:
  INPUT_PARAMETERS:
    - "changetype_name: String"
    - "required_attributes: List<String>"
    - "database_name: String"
    
  DECISION_ALGORITHM:
    STEP_1: "CHECK_EXISTING_CHANGETYPE(changetype_name)"
    STEP_2: "IF exists THEN namespace_extension ELSE new_changetype"
    STEP_3: "VALIDATE_XSD_REQUIREMENTS(required_attributes)"
    STEP_4: "VERIFY_PARSER_SUPPORT(changetype_name)"
    STEP_5: "PLAN_SQL_GENERATOR_ENHANCEMENT()"
    
  OUTPUT_SPECIFICATIONS:
    - "implementation_pattern: NAMESPACE_EXTENSION | NEW_CHANGETYPE"
    - "xsd_attributes: List<AttributeDefinition>"
    - "parser_modifications: List<RequiredChange>"
    - "sql_generator_approach: String"
```

### BLOCKING VALIDATION EXECUTION

```yaml
EXECUTION_CHECKPOINTS:
  CHECKPOINT_1_PATTERN_VALIDATION:
    CONDITION: "Implementation pattern identified and validated"
    FAILURE_ACTION: "STOP - Review architectural requirements"
    
  CHECKPOINT_2_XSD_COMPLIANCE:
    CONDITION: "XSD schema requirements documented"
    FAILURE_ACTION: "STOP - Define namespace attributes in XSD"
    
  CHECKPOINT_3_PARSER_INTEGRATION:
    CONDITION: "Parser integration requirements specified"
    FAILURE_ACTION: "STOP - Plan namespace parser modifications"
    
  CHECKPOINT_4_ARCHITECTURAL_COMPLIANCE:
    CONDITION: "No database-specific change classes planned"
    FAILURE_ACTION: "STOP - Switch to namespace attribute pattern"
```

## IMPLEMENTATION QUALITY GATES

### MANDATORY PRE-IMPLEMENTATION VALIDATION

```yaml
QUALITY_GATE_REQUIREMENTS:
  ARCHITECTURE_COMPLIANCE:
    VALIDATION: "No *ChangeSnowflake classes for existing changetypes"
    ENFORCEMENT: "BLOCKING - Must use namespace attributes"
    
  XSD_COMPLETENESS:
    VALIDATION: "All namespace attributes defined in XSD"
    ENFORCEMENT: "BLOCKING - Must define before implementation"
    
  PARSER_READINESS:
    VALIDATION: "Target changetype supported by namespace parser"
    ENFORCEMENT: "BLOCKING - Must add parser support"
    
  SQL_GENERATOR_APPROACH:
    VALIDATION: "Generator enhancement approach documented"
    ENFORCEMENT: "BLOCKING - Must plan namespace attribute access"
```

This guide ensures AI implementations automatically detect and prevent architectural violations while optimizing for AI consumption through structured decision protocols and blocking validation checkpoints.