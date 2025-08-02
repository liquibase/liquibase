# XSD Schema Requirements Integration Guide
## AI-Optimized XSD Schema Creation During Requirements Phase

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 1.0
DOCUMENT_TYPE: XSD_REQUIREMENTS_INTEGRATION
EXECUTION_MODE: REQUIREMENTS_PHASE_BLOCKING
VALIDATION_MODE: STRICT
FAILURE_BEHAVIOR: STOP_AND_REPORT
ADDRESSES_CORE_ISSUES:
  - "XSD schema creation during requirements phase, not post-implementation"
  - "Namespace attribute definition before coding begins"
  - "Architectural compliance validation in requirements"
  - "AI-consumable XSD patterns and templates"
```

## CRITICAL: XSD SCHEMA CREATION IN REQUIREMENTS PHASE

### MANDATORY REQUIREMENTS PHASE INTEGRATION

```yaml
XSD_REQUIREMENTS_INTEGRATION:
  TIMING: "MUST occur during requirements creation, before any coding"
  LOCATION: "Step 8 of requirements_creation.md process"
  BLOCKING_REQUIREMENT: "Cannot proceed to implementation without XSD schema definition"
  
  REQUIREMENTS_TEMPLATE_ENHANCEMENT:
    SECTION_8: "XSD Schema Integration and Architectural Compliance"
    MANDATORY_CONTENT:
      - "Implementation pattern identification"
      - "XSD attribute definitions for namespace attributes"
      - "Parser integration requirements"
      - "Architectural compliance validation"
```

### AI-CONSUMABLE XSD DECISION MATRIX

```yaml
XSD_SCHEMA_DECISION_ALGORITHM:
  INPUT_PARAMETERS:
    changetype_name: "String - name of the changetype being implemented"
    implementation_pattern: "NAMESPACE_EXTENSION | NEW_CHANGETYPE"
    required_attributes: "List<AttributeRequirement>"
    
  DECISION_FLOW:
    STEP_1_PATTERN_DETECTION:
      IF_NAMESPACE_EXTENSION:
        XSD_ACTION: "Define namespace attributes, NOT new elements"
        LOCATION: "Add to existing standard elements with namespace prefix"
        VALIDATION: "Ensure no new element creation for existing changetypes"
        
      IF_NEW_CHANGETYPE:
        XSD_ACTION: "Define new element with all attributes"
        LOCATION: "Create new element definition in XSD"
        VALIDATION: "Ensure complete element definition with all attributes"
    
    STEP_2_ATTRIBUTE_DEFINITION:
      FOR_EACH_ATTRIBUTE:
        TEMPLATE: "Generate XSD attribute definition"
        TYPE_MAPPING: "Map Java types to XSD types"
        DOCUMENTATION: "Add clear attribute documentation"
        VALIDATION: "Verify attribute name matches Java property"
```

## XSD NAMESPACE ATTRIBUTE TEMPLATES

### NAMESPACE ATTRIBUTE XSD GENERATION

```yaml
NAMESPACE_ATTRIBUTE_TEMPLATE:
  BASE_STRUCTURE: |
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
    Java_Enum: "xsd:string"  # with restrictions if needed
    
  DOCUMENTATION_REQUIREMENTS:
    FORMAT: "Clear, concise description of attribute purpose"
    INCLUDE: "Valid values, defaults, constraints, usage examples"
    EXCLUDE: "Implementation details, technical jargon"
```

### ATTRIBUTE GENERATION ALGORITHM

```java
// AI-consumable XSD attribute generation
public String generateXSDAttribute(AttributeRequirement attr) {
    String template = """
        <!-- Snowflake namespace attributes (for use on standard %s) -->
        <xsd:attribute name="%s" type="%s">
            <xsd:annotation>
                <xsd:documentation>%s</xsd:documentation>
            </xsd:annotation>
        </xsd:attribute>
        """;
    
    return String.format(template, 
        attr.getChangeType(),
        attr.getName(),
        mapJavaTypeToXSD(attr.getJavaType()),
        attr.getDocumentation()
    );
}
```

## XSD VALIDATION INTEGRATION

### REQUIREMENTS PHASE XSD VALIDATION

```yaml
XSD_VALIDATION_REQUIREMENTS:
  VALIDATION_TIMING: "During requirements creation, Step 8"
  VALIDATION_SCOPE: "All defined attributes must have XSD definitions"
  
  VALIDATION_CHECKLIST:
    ATTRIBUTE_COMPLETENESS:
      CHECK: "Every required attribute has XSD definition"
      VALIDATION: "Parse requirements attribute table, verify XSD coverage"
      FAILURE_ACTION: "STOP - Add missing XSD attribute definitions"
      
    TYPE_CONSISTENCY:
      CHECK: "XSD types match planned Java types"
      VALIDATION: "Verify type mappings are correct"
      FAILURE_ACTION: "STOP - Fix type mapping inconsistencies"
      
    NAMESPACE_COMPLIANCE:
      CHECK: "Namespace attributes use correct namespace"
      VALIDATION: "Verify http://www.liquibase.org/xml/ns/snowflake namespace"
      FAILURE_ACTION: "STOP - Correct namespace attribute definitions"
      
    DOCUMENTATION_COMPLETENESS:
      CHECK: "All attributes have clear documentation"
      VALIDATION: "Verify xsd:documentation elements are present and clear"
      FAILURE_ACTION: "STOP - Add comprehensive attribute documentation"
```

### AUTOMATED XSD VALIDATION COMMANDS

```yaml
XSD_VALIDATION_AUTOMATION:
  VALIDATE_ATTRIBUTE_DEFINITIONS:
    COMMAND: "xmllint --schema xsd-validation.xsd --noout liquibase-snowflake-latest.xsd"
    PURPOSE: "Validate XSD syntax and structure"
    
  CHECK_NAMESPACE_ATTRIBUTES:
    COMMAND: "grep -c 'name=\"${attributeName}\"' src/main/resources/*.xsd"
    PURPOSE: "Verify all planned attributes are defined"
    
  VALIDATE_TYPE_MAPPINGS:
    COMMAND: "grep -A 2 'name=\"${attributeName}\"' src/main/resources/*.xsd | grep 'type='"
    PURPOSE: "Verify attribute types match requirements"
    
  DOCUMENTATION_COMPLETENESS:
    COMMAND: "grep -A 5 'name=\"${attributeName}\"' src/main/resources/*.xsd | grep -c 'documentation'"
    PURPOSE: "Ensure all attributes have documentation"
```

## PARSER INTEGRATION REQUIREMENTS

### NAMESPACE PARSER XSD INTEGRATION

```yaml
PARSER_INTEGRATION_REQUIREMENTS:
  PARSER_CLASS: "SnowflakeNamespaceAwareXMLParser"
  INTEGRATION_POINTS:
    CHANGETYPE_SUPPORT:
      METHOD: "isTargetChangeType(String localName)"
      REQUIREMENT: "Must support changetype for namespace attribute processing"
      VALIDATION: "Verify changetype listed in method"
      
    OBJECT_NAME_EXTRACTION:
      METHOD: "getObjectName(String changeType, Attributes attributes)"
      REQUIREMENT: "Must extract object name for attribute storage"
      VALIDATION: "Verify case statement handles changetype"
      
    ATTRIBUTE_PROCESSING:
      METHOD: "processNamespaceAttributes(Change change, Element changeSetElement)"
      REQUIREMENT: "Must process all defined namespace attributes"
      VALIDATION: "Verify all XSD attributes are processed"
```

### PARSER MODIFICATION TEMPLATES

```java
// AI-consumable parser modification template
private boolean isTargetChangeType(String localName) {
    return "createTable".equals(localName) ||
           "alterTable".equals(localName) ||
           "dropTable".equals(localName) ||
           "renameTable".equals(localName) ||
           "createSequence".equals(localName) ||
           "${NEW_CHANGETYPE}".equals(localName) ||  // ADD NEW CHANGETYPE HERE
           "alterSequence".equals(localName) ||
           "dropSequence".equals(localName);
}

private String getObjectName(String changeType, Attributes attributes) {
    switch (changeType) {
        case "createTable":
        case "alterTable":
        case "dropTable":
            return attributes.getValue("tableName");
        case "renameTable":
            return attributes.getValue("oldTableName");
        case "createSequence":
        case "alterSequence":
        case "dropSequence":
            return attributes.getValue("sequenceName");
        case "${NEW_CHANGETYPE}":  // ADD NEW CHANGETYPE HERE
            return attributes.getValue("${OBJECT_NAME_ATTRIBUTE}");
        default:
            return null;
    }
}
```

## REQUIREMENTS DOCUMENT TEMPLATE ENHANCEMENT

### XSD SCHEMA SECTION TEMPLATE

```markdown
## 8. XSD Schema Integration and Architectural Compliance

### Implementation Pattern Decision
- **Pattern**: NAMESPACE_ATTRIBUTE_EXTENSION
- **Justification**: Extending existing Liquibase createSequence changetype with Snowflake-specific ORDER/NOORDER attributes
- **Architectural Compliance**: ✅ Confirmed - using namespace attributes, not creating new change class

### XSD Schema Requirements

#### Namespace Attribute Definitions
```xml
<!-- Snowflake namespace attributes (for use on standard createSequence) -->
<xsd:attribute name="order" type="xsd:boolean">
    <xsd:annotation>
        <xsd:documentation>Whether to maintain order in sequence values (ORDER) or not (NOORDER). Default is NOORDER.</xsd:documentation>
    </xsd:annotation>
</xsd:attribute>

<xsd:attribute name="comment" type="xsd:string">
    <xsd:annotation>
        <xsd:documentation>Comment to add to the sequence</xsd:documentation>
    </xsd:annotation>
</xsd:attribute>
```

#### XSD Validation Requirements
- [x] All namespace attributes defined in XSD
- [x] Attribute types match Java property types (Boolean -> xsd:boolean, String -> xsd:string)
- [x] Documentation provided for all attributes
- [x] Namespace URI consistent: http://www.liquibase.org/xml/ns/snowflake

### Namespace Parser Integration
- **Target Changetype**: ✅ createSequence already supported in SnowflakeNamespaceAwareXMLParser.isTargetChangeType()
- **Object Name Extraction**: ✅ sequenceName handled in getObjectName() method
- **Attribute Storage**: ✅ Will use SnowflakeNamespaceAttributeStorage with sequence name as key

### SQL Generator Integration Plan
- **Generator Class**: CreateSequenceGeneratorSnowflake (extends standard CreateSequenceGenerator)
- **Attribute Access**: Retrieve attributes using SnowflakeNamespaceAttributeStorage.getAttributes(sequenceName)
- **SQL Enhancement**: Add ORDER/NOORDER keywords based on namespace attributes
- **Cleanup**: Remove attributes from storage after SQL generation

### Architectural Validation Rules
1. ✅ **For Existing Changetypes**: NOT creating database-specific change classes
2. ✅ **Namespace Attributes**: Defined in XSD schema  
3. ✅ **Parser Support**: createSequence supported by namespace parser
4. ✅ **SQL Generator**: Extending standard generator, not creating new change class
```

## AI CONSUMPTION OPTIMIZATION

### STRUCTURED XSD REQUIREMENTS

```yaml
AI_XSD_WORKFLOW:
  INPUT_PROCESSING:
    REQUIREMENTS_ANALYSIS: "Parse attribute table from requirements document"
    PATTERN_DETECTION: "Identify namespace extension vs new changetype"
    ATTRIBUTE_EXTRACTION: "Extract attribute names, types, descriptions"
    
  XSD_GENERATION:
    TEMPLATE_SELECTION: "Choose namespace attribute vs new element template"
    TYPE_MAPPING: "Convert Java types to XSD types"
    DOCUMENTATION_GENERATION: "Create clear attribute documentation"
    
  VALIDATION_AUTOMATION:
    XSD_SYNTAX_CHECK: "Validate generated XSD syntax"
    COMPLETENESS_CHECK: "Verify all attributes have XSD definitions"
    COMPLIANCE_CHECK: "Validate architectural pattern compliance"
    
  OUTPUT_GENERATION:
    XSD_DEFINITIONS: "Complete XSD attribute definitions"
    PARSER_MODIFICATIONS: "Required parser integration changes"
    GENERATOR_REQUIREMENTS: "SQL generator integration approach"
```

### BLOCKING EXECUTION CHECKPOINTS

```yaml
XSD_REQUIREMENTS_CHECKPOINTS:
  CHECKPOINT_1_PATTERN_VALIDATION:
    CONDITION: "Implementation pattern identified and documented"
    VALIDATION: "Pattern matches architectural requirements"
    FAILURE_ACTION: "STOP - Review architectural compliance requirements"
    
  CHECKPOINT_2_XSD_COMPLETENESS:
    CONDITION: "All required attributes have XSD definitions"
    VALIDATION: "Every attribute in requirements has corresponding XSD"
    FAILURE_ACTION: "STOP - Add missing XSD attribute definitions"
    
  CHECKPOINT_3_PARSER_READINESS:
    CONDITION: "Parser integration requirements documented"
    VALIDATION: "All integration points identified and planned"
    FAILURE_ACTION: "STOP - Plan namespace parser integration"
    
  CHECKPOINT_4_GENERATOR_APPROACH:
    CONDITION: "SQL generator enhancement approach documented"
    VALIDATION: "Namespace attribute access approach planned"
    FAILURE_ACTION: "STOP - Plan SQL generator namespace integration"
```

This guide ensures XSD schema requirements are properly integrated during the requirements phase, preventing post-implementation schema issues and ensuring architectural compliance from the start.