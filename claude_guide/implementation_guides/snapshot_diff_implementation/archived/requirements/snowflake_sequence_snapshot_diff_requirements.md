# Snowflake Sequence Snapshot/Diff Requirements - IMPLEMENTATION_READY
## Phase 2: XSD-Driven Requirements Documentation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "2_DOCUMENTATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_INPUT: "research_findings_snowflake_sequence_snapshot_diff.md"
IMPLEMENTATION_TYPE: "SNAPSHOT_DIFF"
NEXT_PHASE: "ai_workflow_guide.md"
OBJECT_TYPE: "SEQUENCE"
DATABASE_TYPE: "SNOWFLAKE"
```

## XSD_ATTRIBUTES_SPECIFICATION
```yaml
XSD_COMPLIANCE_COVERAGE: "100% - All 14+ configuration attributes implemented"
XSD_SOURCE_VALIDATION: "liquibase-snowflake-latest.xsd createSequence element verified"

REQUIRED_XSD_ATTRIBUTES:
  sequenceName:
    type: "xsd:string"
    required: true
    validation: "Non-null, non-empty string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_NAME"
    comparison: "ALWAYS_INCLUDE - Identity attribute"
    
OPTIONAL_XSD_ATTRIBUTES:
  catalogName:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_CATALOG"
    comparison: "ALWAYS_INCLUDE - Database relationship"
    
  schemaName:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_SCHEMA"
    comparison: "ALWAYS_INCLUDE - Schema relationship"
    
  startValue:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.START_VALUE"
    comparison: "ALWAYS_INCLUDE - Initial configuration"
    immutable: true
    note: "Cannot be changed after sequence creation"
    
  incrementBy:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.INCREMENT"
    comparison: "ALWAYS_INCLUDE - Step configuration"
    equivalent_show: "INTERVAL"
    
  minValue:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.MINIMUM_VALUE"
    comparison: "ALWAYS_INCLUDE - Range configuration"
    
  maxValue:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.MAXIMUM_VALUE"
    comparison: "ALWAYS_INCLUDE - Range configuration"
    
  cycle:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.CYCLE_OPTION (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Behavior configuration"
    conversion: "boolean → YES/NO"
    
  cacheSize:
    type: "xsd:string"
    mapping: "System cache configuration (not directly exposed in INFORMATION_SCHEMA)"
    comparison: "CONDITIONAL_INCLUDE - Often uses system defaults"
    
  dataType:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.DATA_TYPE"
    comparison: "ALWAYS_INCLUDE - Type specification"
    default: "NUMBER"
    immutable: true
    
  order:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.ORDERED (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Ordering configuration"
    conversion: "boolean → YES/NO"
    equivalent_show: "ORDERED (Y/N)"
    
  ordered:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.ORDERED (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Alternative attribute name for order"
    conversion: "boolean → YES/NO"
    note: "Alternative attribute name - same as 'order'"
    
  comment:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SEQUENCES.COMMENT"
    comparison: "ALWAYS_INCLUDE - User metadata"
    null_handling: "Treat NULL and empty string as equivalent"
    
OPERATIONAL_ATTRIBUTES:
  orReplace:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE OR REPLACE SEQUENCE syntax"
    
  ifNotExists:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE SEQUENCE IF NOT EXISTS syntax"

ALTER_SEQUENCE_SPECIFIC_ATTRIBUTES:
  setNoOrder:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - ALTER SEQUENCE operation"
    usage: "Set sequence to NOORDER (one-way operation)"
    note: "Cannot be undone - ORDER → NOORDER is permanent"
    
  setComment:
    type: "xsd:string"
    comparison: "EXCLUDE_FROM_DIFF - ALTER SEQUENCE operation"
    usage: "Set a comment on the sequence"
    
  unsetComment:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - ALTER
    usage: "Remove the comment from the sequence"
    mutually_exclusive: "setComment"
```

## STATE_ATTRIBUTES_SPECIFICATION
```yaml
STATE_COMPLIANCE_COVERAGE: "100% - All 18+ operational metadata attributes captured"
INFORMATION_SCHEMA_SOURCE: "INFORMATION_SCHEMA.SEQUENCES"
SHOW_COMMAND_SOURCE: "SHOW SEQUENCES"

IDENTITY_ATTRIBUTES:
  SEQUENCE_CATALOG:
    source: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_CATALOG"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Database relationship"
    validation: "Must match catalogName from XSD"
    
  SEQUENCE_SCHEMA:
    source: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_SCHEMA"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Schema relationship"
    validation: "Must match schemaName from XSD"
    
  SEQUENCE_NAME:
    source: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_NAME"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Sequence identity"
    validation: "Must match sequenceName from XSD"
    
  SEQUENCE_OWNER:
    source: "INFORMATION_SCHEMA.SEQUENCES.SEQUENCE_OWNER"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - May change due to ownership transfers"
    snapshot_usage: "Owner information for audit"

DATA_TYPE_ATTRIBUTES:
  DATA_TYPE:
    source: "INFORMATION_SCHEMA.SEQUENCES.DATA_TYPE"
    type: "STRING"
    comparison: "VALIDATION_COMPARE - Validates XSD dataType attribute"
    default: "NUMBER"
    
  NUMERIC_PRECISION:
    source: "INFORMATION_SCHEMA.SEQUENCES.NUMERIC_PRECISION"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - System-derived from data type"
    default: 38
    
  NUMERIC_PRECISION_RADIX:
    source: "INFORMATION_SCHEMA.SEQUENCES.NUMERIC_PRECISION_RADIX"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - System-derived"
    default: 10
    
  NUMERIC_SCALE:
    source: "INFORMATION_SCHEMA.SEQUENCES.NUMERIC_SCALE"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - System-derived"
    default: 0

SEQUENCE_CONFIGURATION_STATE:
  START_VALUE:
    source: "INFORMATION_SCHEMA.SEQUENCES.START_VALUE"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validates XSD startValue attribute"
    immutable: true
    
  MINIMUM_VALUE:
    source: "INFORMATION_SCHEMA.SEQUENCES.MINIMUM_VALUE"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validates XSD minValue attribute"
    system_default: -9223372036854775808
    
  MAXIMUM_VALUE:
    source: "INFORMATION_SCHEMA.SEQUENCES.MAXIMUM_VALUE"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validates XSD maxValue attribute"
    system_default: 9223372036854775807
    
  INCREMENT:
    source: "INFORMATION_SCHEMA.SEQUENCES.INCREMENT"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validates XSD incrementBy attribute"
    equivalent_show: "INTERVAL"
    
  CYCLE_OPTION:
    source: "INFORMATION_SCHEMA.SEQUENCES.CYCLE_OPTION"
    type: "STRING (YES/NO)"
    comparison: "VALIDATION_COMPARE - Validates XSD cycle attribute"
    
  ORDERED:
    source: "INFORMATION_SCHEMA.SEQUENCES.ORDERED"
    type: "STRING (YES/NO)"
    comparison: "VALIDATION_COMPARE - Validates XSD order/ordered attribute"
    equivalent_show: "ORDERED (Y/N)"

OPERATIONAL_STATE:
  NEXT_VALUE:
    source: "INFORMATION_SCHEMA.SEQUENCES.NEXT_VALUE"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - Changes with sequence usage"
    snapshot_usage: "Current sequence state for audit"
    
  COMMENT:
    source: "INFORMATION_SCHEMA.SEQUENCES.COMMENT"
    type: "STRING"
    comparison: "VALIDATION_COMPARE - Validates XSD comment attribute"

TIMESTAMPS:
  CREATED:
    source: "INFORMATION_SCHEMA.SEQUENCES.CREATED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Immutable timestamp"
    snapshot_usage: "Creation audit trail"
    
  LAST_ALTERED:
    source: "INFORMATION_SCHEMA.SEQUENCES.LAST_ALTERED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Changes with modifications"
    snapshot_usage: "Modification audit trail"

SHOW_SEQUENCES_EXCLUSIVE:
  OWNER_ROLE_TYPE:
    source: "SHOW SEQUENCES.OWNER_ROLE_TYPE"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Security state"
```

## XSD_COMPLIANCE_VALIDATION_REQUIREMENTS
```yaml
AUTOMATED_XSD_COMPLIANCE_FRAMEWORK:
  XSD_ATTRIBUTE_EXTRACTION:
    - "Parse liquibase-snowflake-latest.xsd createSequence element"
    - "Extract all 14+ configuration attributes with types and constraints"
    - "Generate attribute specification map for snapshot generator"
    - "Validate XSD completeness against Snowflake sequence documentation"
    
  XSD_SNAPSHOT_VALIDATION:
    - "Verify snapshot generator captures ALL XSD attributes"
    - "Validate XSD→INFORMATION_SCHEMA attribute mapping"
    - "Test XSD data type conversions (xsd:boolean → YES/NO, xsd:string → NUMBER)"
    - "Report missing or incorrectly implemented XSD attributes"
    
  XSD_COMPLIANCE_TESTING:
    - "Unit test: validateAllXSDAttributesImplemented(SEQUENCE)"
    - "Integration test: createSequenceWithAllAttributes → snapshot → validate"
    - "Regression test: ensureXSDEvolutionCompatibility"
    - "Performance test: completeAttributeCapturePerformance"
```

## OBJECT_MODEL_SPECIFICATION
```yaml
SEQUENCE_OBJECT_MODEL:
  LIQUIBASE_OBJECT_TYPE: "liquibase.structure.core.Sequence"
  SNOWFLAKE_EXTENSIONS: "Use setAttribute() for Snowflake-specific properties"
  
  CORE_PROPERTIES:
    name:
      source: "XSD sequenceName"
      type: "String"
      required: true
      validation: "Non-null, non-empty"
      
    schema:
      source: "XSD schemaName / INFORMATION_SCHEMA.SEQUENCE_SCHEMA"
      type: "Schema"
      required: true
      validation: "Parent schema relationship"
      
    startValue:
      source: "XSD startValue / INFORMATION_SCHEMA.START_VALUE"
      type: "BigInteger"
      immutable: true
      
    incrementBy:
      source: "XSD incrementBy / INFORMATION_SCHEMA.INCREMENT"
      type: "BigInteger"
      
    minValue:
      source: "XSD minValue / INFORMATION_SCHEMA.MINIMUM_VALUE"
      type: "BigInteger"
      
    maxValue:
      source: "XSD maxValue / INFORMATION_SCHEMA.MAXIMUM_VALUE"
      type: "BigInteger"
      
    willCycle:
      source: "XSD cycle / INFORMATION_SCHEMA.CYCLE_OPTION"
      type: "Boolean"
      conversion: "boolean ↔ YES/NO"
      
    ordered:
      source: "XSD order/ordered / INFORMATION_SCHEMA.ORDERED"
      type: "Boolean"
      conversion: "boolean ↔ YES/NO"
      
    dataType:
      source: "XSD dataType / INFORMATION_SCHEMA.DATA_TYPE"
      type: "String"
      default: "NUMBER"
      immutable: true
      
  SNOWFLAKE_SPECIFIC_ATTRIBUTES:
    comment:
      source: "XSD comment / INFORMATION_SCHEMA.COMMENT"
      type: "String"
      nullable: true
      
    cacheSize:
      source: "XSD cacheSize"
      type: "BigInteger"
      note: "System cache configuration, not directly exposed"
      
    numericPrecision:
      source: "INFORMATION_SCHEMA.NUMERIC_PRECISION"
      type: "Integer"
      readonly: true
      default: 38
      
    numericScale:
      source: "INFORMATION_SCHEMA.NUMERIC_SCALE"
      type: "Integer"
      readonly: true
      default: 0
      
    nextValue:
      source: "INFORMATION_SCHEMA.NEXT_VALUE"
      type: "BigInteger"
      readonly: true
      
    owner:
      source: "INFORMATION_SCHEMA.SEQUENCE_OWNER"
      type: "String"
      readonly: true
      
    created:
      source: "INFORMATION_SCHEMA.CREATED"
      type: "Timestamp"
      readonly: true
      
    lastAltered:
      source: "INFORMATION_SCHEMA.LAST_ALTERED"
      type: "Timestamp"
      readonly: true
```

## SNAPSHOT_SQL_REQUIREMENTS
```yaml
PRIMARY_SNAPSHOT_QUERY:
  QUERY_TYPE: "INFORMATION_SCHEMA_OPTIMIZED"
  SQL: |
    SELECT 
      SEQUENCE_CATALOG,
      SEQUENCE_SCHEMA,
      SEQUENCE_NAME,
      SEQUENCE_OWNER,
      DATA_TYPE,
      NUMERIC_PRECISION,
      NUMERIC_PRECISION_RADIX,
      NUMERIC_SCALE,
      START_VALUE,
      MINIMUM_VALUE,
      MAXIMUM_VALUE,
      NEXT_VALUE,
      INCREMENT,
      CYCLE_OPTION,
      CREATED,
      LAST_ALTERED,
      ORDERED,
      COMMENT
    FROM INFORMATION_SCHEMA.SEQUENCES
    WHERE SEQUENCE_CATALOG = ? AND SEQUENCE_SCHEMA = ? AND SEQUENCE_NAME = ?
    
  PARAMETERS: ["catalogName", "schemaName", "sequenceName"]
  PERFORMANCE: "Single sequence lookup - optimal"
  RESULT_PROCESSING: "Single row expected, null if not found"
  
SUPPLEMENTARY_SHOW_QUERY:
  QUERY_TYPE: "SHOW_COMMAND_METADATA"
  SQL: "SHOW SEQUENCES LIKE ? IN SCHEMA ?"
  PARAMETERS: ["sequenceName", "schemaName"]
  ADDITIONAL_ATTRIBUTES:
    - "OWNER_ROLE_TYPE"   # Security metadata
  USE_CASE: "Additional metadata not in INFORMATION_SCHEMA"
  
BULK_SNAPSHOT_QUERY:
  QUERY_TYPE: "SCHEMA_SEQUENCE_DISCOVERY"
  SQL: |
    SELECT 
      SEQUENCE_CATALOG,
      SEQUENCE_SCHEMA,
      SEQUENCE_NAME,
      SEQUENCE_OWNER,
      DATA_TYPE,
      START_VALUE,
      MINIMUM_VALUE,
      MAXIMUM_VALUE,
      INCREMENT,
      CYCLE_OPTION,
      ORDERED,
      CREATED,
      LAST_ALTERED,
      COMMENT
    FROM INFORMATION_SCHEMA.SEQUENCES
    WHERE SEQUENCE_CATALOG = ? AND SEQUENCE_SCHEMA = ?
    ORDER BY SEQUENCE_NAME
    
  PARAMETERS: ["catalogName", "schemaName"]
  PERFORMANCE: "All sequences in schema enumeration"
  RESULT_PROCESSING: "Multiple rows, process each as Sequence object"
  
SQL_QUERY_EXAMPLES:
  SINGLE_SEQUENCE_SNAPSHOT:
    description: "Snapshot specific sequence with all attributes"
    sql: "SELECT * FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_CATALOG = 'LTHDB' AND SEQUENCE_SCHEMA = 'TESTHARNESS' AND SEQUENCE_NAME = 'SNAPSHOT_TEST_SEQ'"
    expected_result: "Single sequence record with all XSD + state attributes"
    
  SEQUENCE_CONFIGURATION_VALIDATION:
    description: "Validate sequence configuration attributes"
    sql: "SELECT START_VALUE, INCREMENT, MINIMUM_VALUE, MAXIMUM_VALUE, CYCLE_OPTION, ORDERED FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?"
    validation: "Compare with XSD configuration attributes"
    
  SEQUENCE_TYPE_CONVERSION:
    description: "Handle sequence attribute type conversions"
    sql: "SELECT CYCLE_OPTION, ORDERED FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?"
    conversion: "YES/NO → boolean, Y/N → boolean"
    
  SEQUENCE_RANGE_HANDLING:
    description: "Handle sequence min/max value defaults"
    sql: "SELECT MINIMUM_VALUE, MAXIMUM_VALUE FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?"
    defaults: "MINIMUM_VALUE: -9223372036854775808, MAXIMUM_VALUE: 9223372036854775807"
    
  SEQUENCE_OWNER_TRACKING:
    description: "Capture ownership for audit (exclude from diff)"
    sql: "SELECT SEQUENCE_OWNER FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?"
    usage: "Snapshot-only for audit trail"
    
  SEQUENCE_OPERATIONAL_STATE:
    description: "Capture operational state (exclude from diff)"
    sql: "SELECT NEXT_VALUE, CREATED, LAST_ALTERED FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?"
    usage: "Snapshot-only for operational monitoring"
```

## COMPARISON_LOGIC_SPECIFICATION
```yaml
STRUCTURAL_COMPARISON_RULES:
  IDENTITY_COMPARISON:
    primary_key: ["catalogName", "schemaName", "sequenceName"]
    case_sensitivity: false
    rule: "Sequences with different names or locations are different objects"
    
  XSD_CONFIGURATION_COMPARISON:
    startValue:
      rule: "Numeric comparison with START_VALUE (immutable after creation)"
      type: "ImmutableNumericComparison"
      validation: "Cannot be changed after sequence creation"
      
    incrementBy:
      rule: "Numeric comparison with INCREMENT"
      type: "NumericComparison"
      
    minValue:
      rule: "Numeric comparison with MINIMUM_VALUE"
      type: "NumericComparison"
      default_handling: "System default vs configured value"
      
    maxValue:
      rule: "Numeric comparison with MAXIMUM_VALUE"
      type: "NumericComparison"
      default_handling: "System default vs configured value"
      
    cycle:
      rule: "Boolean comparison with CYCLE_OPTION conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    order_ordered:
      rule: "Boolean comparison with ORDERED conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      note: "Handle both 'order' and 'ordered' XSD attributes"
      
    comment:
      rule: "String comparison, NULL == empty string"
      type: "StringComparison"
      
    dataType:
      rule: "String comparison with DATA_TYPE (immutable after creation)"
      type: "ImmutableStringComparison"
      validation: "Cannot be changed after sequence creation"
      
EXCLUSION_RULES:
  ALWAYS_EXCLUDE:
    - "SEQUENCE_OWNER"       # Ownership management
    - "NEXT_VALUE"           # Operational state, changes with usage
    - "CREATED"              # Immutable timestamp
    - "LAST_ALTERED"         # Modification timestamp
    - "OWNER_ROLE_TYPE"      # Security state
    - "NUMERIC_PRECISION"    # System-derived from data type
    - "NUMERIC_PRECISION_RADIX"  # System-derived
    - "NUMERIC_SCALE"        # System-derived
    
  CREATION_ONLY_EXCLUDE:
    - "orReplace"            # CREATE OR REPLACE flag
    - "ifNotExists"          # CREATE IF NOT EXISTS flag
    
  ALTER_OPERATION_EXCLUDE:
    - "setNoOrder"           # ALTER SEQUENCE operation flag
    - "setComment"           # ALTER SEQUENCE operation
    - "unsetComment"         # ALTER SEQUENCE operation
    
DIFF_SCENARIO_MATRIX:
  MISSING_SEQUENCE:
    condition: "Sequence exists in reference but not in target"
    action: "Generate CREATE SEQUENCE change"
    attributes: "Include all configured XSD attributes"
    
  UNEXPECTED_SEQUENCE:
    condition: "Sequence exists in target but not in reference"
    action: "Generate DROP SEQUENCE change"
    
  CHANGED_SEQUENCE:
    condition: "Sequence exists in both but properties differ"
    action: "Generate ALTER SEQUENCE change"
    supported_changes:
      - "comment → ALTER SEQUENCE SET COMMENT"
      - "incrementBy → ALTER SEQUENCE SET INCREMENT"
      - "minValue → ALTER SEQUENCE SET MINVALUE"
      - "maxValue → ALTER SEQUENCE SET MAXVALUE"
      - "cycle → ALTER SEQUENCE SET CYCLE/NOCYCLE"
      - "order → ALTER SEQUENCE SET ORDER (one-way to NOORDER)"
    immutable_warnings:
      - "startValue → Cannot be changed (requires DROP/CREATE)"
      - "dataType → Cannot be changed (requires DROP/CREATE)"
      
EDGE_CASE_HANDLING:
  NULL_VS_EMPTY_COMMENTS:
    rule: "Treat NULL and empty string as equivalent"
    implementation: "StringUtils.isEmpty() for comparison"
    
  SYSTEM_DEFAULT_VALUES:
    rule: "Compare configured vs system default min/max values"
    system_defaults:
      MINIMUM_VALUE: -9223372036854775808
      MAXIMUM_VALUE: 9223372036854775807
    handling: "Treat explicit system defaults as equivalent to unspecified"
    
  ORDER_TO_NOORDER_TRANSITION:
    rule: "ORDER to NOORDER is a one-way operation"
    validation: "Cannot change from NOORDER back to ORDER"
    warning: "Generate warning for ORDER → NOORDER changes"
    
  NEXT_VALUE_CONSIDERATIONS:
    rule: "NEXT_VALUE changes during normal operation"
    handling: "Exclude from comparison but capture in snapshot for audit"
    
  SEQUENCE_USAGE_STATE:
    rule: "Sequences in active use may have different NEXT_VALUE"
    validation: "Do not flag NEXT_VALUE differences as structural changes"
```

## FRAMEWORK_INTEGRATION_REQUIREMENTS
```yaml
LIQUIBASE_SNAPSHOT_GENERATOR:
  CLASS_NAME: "SnowflakeSequenceSnapshotGenerator"
  BASE_CLASS: "liquibase.snapshot.jvm.JdbcSnapshotGenerator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  COORDINATION: "Must coordinate with existing SequenceSnapshotGenerator"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Sequence.class + SnowflakeDatabase"
      coordination: "Higher priority than standard SequenceSnapshotGenerator"
      
    addsTo:
      signature: "Class<? extends DatabaseObject>[] addsTo()"
      implementation: "Return new Class[] { Schema.class }"
      
    snapshotObject:
      signature: "DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot)"
      implementation: "Single sequence snapshot with XSD compliance"
      
    addTo:
      signature: "void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot)"
      implementation: "Bulk sequence discovery within schema"
      
SERVICE_REGISTRATION:
  FILE: "META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "liquibase.ext.snowflake.snapshot.SnowflakeSequenceSnapshotGenerator"
  
LIQUIBASE_COMPARATOR:
  CLASS_NAME: "SnowflakeSequenceComparator"
  BASE_CLASS: "liquibase.diff.compare.DatabaseObjectComparator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  COORDINATION: "Must coordinate with existing SequenceComparator"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Sequence.class + SnowflakeDatabase"
      coordination: "Higher priority than standard SequenceComparator"
      
    hash:
      signature: "String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Return [catalogName, schemaName, sequenceName] as hash key"
      
    isSameObject:
      signature: "boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Compare catalog, schema, and sequence names (case-insensitive)"
      
    findDifferences:
      signature: "ObjectDifferences findDifferences(...)"
      implementation: "XSD-driven property comparison with exclusion rules"
      coordination: "Chain to standard comparator for non-Snowflake properties"
      
SEQUENCE_OBJECT_INTEGRATION:
  OBJECT_TYPE: "liquibase.structure.core.Sequence"
  ATTRIBUTE_HANDLING: "Use setAttribute() for Snowflake-specific properties"
  RELATIONSHIP_MANAGEMENT: "Child of Schema"
  EXISTING_INTEGRATION: "Leverage existing Liquibase sequence infrastructure"
```

## TEST_SCENARIO_MATRIX
```yaml
XSD_COMPLIANCE_TESTS:
  TEST_ALL_XSD_ATTRIBUTES_CAPTURED:
    description: "Verify snapshot captures all 14+ XSD configuration attributes"
    setup: "Create sequence with all XSD attributes configured"
    execution: "Snapshot sequence and validate all attributes present"
    validation: "Assert all XSD attributes correctly mapped and converted"
    
  TEST_XSD_ATTRIBUTE_TYPE_CONVERSION:
    description: "Test XSD type conversions (boolean → YES/NO, string → NUMBER)"
    setup: "Create sequence with cycle=true, order=true, startValue=100"
    execution: "Snapshot and validate conversions"
    validation: "Assert CYCLE_OPTION='YES', ORDERED='YES', START_VALUE=100"
    
  TEST_XSD_COMPLIANCE_VALIDATION:
    description: "Automated XSD compliance validation framework"
    execution: "XSDComplianceTest.validateAllAttributesImplemented(SEQUENCE)"
    validation: "Assert no missing XSD attributes reported"
    
SNAPSHOT_FUNCTIONALITY_TESTS:
  TEST_SINGLE_SEQUENCE_SNAPSHOT:
    description: "Snapshot specific sequence with complete attributes"
    setup: "Known sequence with start value, increment, comment"
    execution: "Snapshot single sequence"
    validation: "All XSD + state attributes captured correctly"
    
  TEST_BULK_SEQUENCE_DISCOVERY:
    description: "Discover all sequences in schema"
    execution: "Bulk snapshot all sequences in TESTHARNESS schema"
    validation: "All sequences discovered with complete attributes"
    
  TEST_NONEXISTENT_SEQUENCE_HANDLING:
    description: "Handle snapshot of non-existent sequence"
    setup: "Request snapshot of 'NONEXISTENT_SEQUENCE'"
    execution: "Attempt snapshot"
    validation: "Return null gracefully, no exceptions"
    
COMPARISON_LOGIC_TESTS:
  TEST_SEQUENCE_IDENTITY_COMPARISON:
    description: "Test sequence identity comparison (case-insensitive names)"
    setup: "Two sequences: 'LTHDB.TESTHARNESS.TESTSEQ' and 'lthdb.testharness.testseq'"
    execution: "Compare sequence objects"
    validation: "Assert isSameObject returns true"
    
  TEST_XSD_ATTRIBUTE_DIFFERENCES:
    description: "Detect differences in XSD configuration attributes"
    setup: "Two sequences with different increments, cycle settings"
    execution: "Compare sequences"
    validation: "Assert differences detected for increment and cycle"
    
  TEST_STATE_ATTRIBUTE_EXCLUSIONS:
    description: "Verify state attributes excluded from structural comparison"
    setup: "Two sequences with different next values, owners, timestamps"
    execution: "Compare sequences"
    validation: "Assert no differences detected for excluded attributes"
    
  TEST_IMMUTABLE_ATTRIBUTE_HANDLING:
    description: "Handle immutable attributes (startValue, dataType)"
    setup: "Two sequences with different start values"
    execution: "Compare sequences"
    validation: "Assert difference detected with immutable warning"
    
DIFF_GENERATION_TESTS:
  TEST_MISSING_SEQUENCE_CHANGE:
    description: "Generate CREATE SEQUENCE change for missing sequence"
    setup: "Reference has sequence, target does not"
    execution: "Generate diff"
    validation: "CREATE SEQUENCE change with all XSD attributes"
    
  TEST_UNEXPECTED_SEQUENCE_CHANGE:
    description: "Generate DROP SEQUENCE change for unexpected sequence"
    setup: "Target has sequence, reference does not"
    execution: "Generate diff"
    validation: "DROP SEQUENCE change generated"
    
  TEST_CHANGED_SEQUENCE_PROPERTIES:
    description: "Generate ALTER SEQUENCE changes for property differences"
    setup: "Sequences with different comments, increments, cycle settings"
    execution: "Generate diff"
    validation: "ALTER SEQUENCE changes for modified properties"
    
EDGE_CASE_TESTS:
  TEST_NULL_VS_EMPTY_COMMENT_HANDLING:
    description: "Handle NULL vs empty string comment equivalence"
    setup: "One sequence with NULL comment, one with empty string"
    execution: "Compare sequences"
    validation: "Assert no difference detected"
    
  TEST_SYSTEM_DEFAULT_VALUE_HANDLING:
    description: "Handle system default min/max values"
    setup: "One sequence with explicit system defaults, one with unspecified"
    execution: "Compare sequences"
    validation: "Assert no difference detected for equivalent defaults"
    
  TEST_ORDER_TO_NOORDER_TRANSITION:
    description: "Handle ORDER to NOORDER one-way transition"
    setup: "Two sequences: one with ORDER=true, one with ORDER=false"
    execution: "Compare sequences"
    validation: "Assert difference detected with one-way operation warning"
    
  TEST_NEXT_VALUE_EXCLUSION:
    description: "Exclude NEXT_VALUE from structural comparison"
    setup: "Two sequences with different NEXT_VALUE due to usage"
    execution: "Compare sequences"
    validation: "Assert no difference detected for NEXT_VALUE"
    
  TEST_SEQUENCE_IN_USE_HANDLING:
    description: "Handle sequences that are actively being used"
    setup: "Sequence with NEXT_VALUE different from START_VALUE"
    execution: "Snapshot and compare"
    validation: "Validate operational state captured but excluded from diff"
    
INTEGRATION_TESTS:
  TEST_FRAMEWORK_REGISTRATION:
    description: "Verify snapshot generator properly registered"
    execution: "SnapshotGeneratorFactory.getInstance().getGenerators(Sequence.class, SnowflakeDatabase)"
    validation: "SnowflakeSequenceSnapshotGenerator included with correct priority"
    
  TEST_SCHEMA_RELATIONSHIP_MAINTENANCE:
    description: "Verify schema/sequence relationship properly maintained"
    execution: "Snapshot sequence and validate schema relationship"
    validation: "Sequence.getSchema() returns correct Schema object"
    
  TEST_EXISTING_SEQUENCE_INFRASTRUCTURE:
    description: "Verify integration with existing Liquibase sequence infrastructure"
    execution: "Test with standard Liquibase sequence operations"
    validation: "Snowflake-specific attributes properly handled alongside standard attributes"
    
  TEST_TEST_HARNESS_COMPATIBILITY:
    description: "Verify compatibility with Liquibase test harness"
    execution: "Run test harness sequence snapshot tests"
    validation: "All tests pass with XSD compliance"
    
  TEST_PERFORMANCE_WITH_MANY_SEQUENCES:
    description: "Performance test with realistic sequence counts"
    setup: "Schema with 100+ sequences"
    execution: "Bulk snapshot all sequences"
    validation: "Complete snapshot under performance thresholds"
```

## IMPLEMENTATION_GUIDANCE
```yaml
TDD_IMPLEMENTATION_PLAN:
  PHASE_1_SNAPSHOT_GENERATOR:
    1. "Create SnowflakeSequenceSnapshotGenerator unit tests"
    2. "Implement getPriority() method with SnowflakeDatabase priority"
    3. "Implement snapshotObject() with INFORMATION_SCHEMA.SEQUENCES query"
    4. "Add XSD attribute mapping with setAttribute() patterns"
    5. "Implement SHOW SEQUENCES supplementary query for additional attributes"
    6. "Handle schema/sequence relationship properly"
    7. "Coordinate with existing sequence snapshot infrastructure"
    
  PHASE_2_COMPARATOR:
    1. "Create SnowflakeSequenceComparator unit tests"
    2. "Implement identity comparison with case-insensitive names"
    3. "Implement XSD attribute comparison with exclusion rules"
    4. "Add immutable attribute handling (startValue, dataType)"
    5. "Add system default value comparison logic"
    6. "Add edge case handling (ORDER to NOORDER transition)"
    7. "Test all diff scenarios (missing, unexpected, changed)"
    
  PHASE_3_INTEGRATION:
    1. "Register snapshot generator in META-INF/services"
    2. "Register comparator in META-INF/services"
    3. "Integration test with real Snowflake database"
    4. "Test schema/sequence relationship maintenance"
    5. "Test integration with existing sequence infrastructure"
    6. "Test harness validation"
    7. "Performance optimization and validation"
    
VALIDATION_CHECKPOINTS:
  AFTER_SNAPSHOT_GENERATOR:
    - [ ] "All XSD attributes captured in snapshot"
    - [ ] "INFORMATION_SCHEMA query returns complete data"
    - [ ] "SHOW SEQUENCES integration working"
    - [ ] "Attribute type conversions correct"
    - [ ] "Schema/sequence relationship maintained"
    - [ ] "Integration with existing infrastructure working"
    
  AFTER_COMPARATOR:
    - [ ] "Sequence identity comparison working"
    - [ ] "XSD attribute differences detected"
    - [ ] "State attributes properly excluded"
    - [ ] "Immutable attribute handling correct"
    - [ ] "System default value handling correct"
    - [ ] "Edge cases handled correctly"
    
  AFTER_INTEGRATION:
    - [ ] "Service registration complete"
    - [ ] "Framework integration working"
    - [ ] "Schema relationship working"
    - [ ] "Existing infrastructure integration working"
    - [ ] "Test harness compatibility verified"
    - [ ] "Performance requirements met"
```

## QUALITY_VALIDATION
```yaml
REQUIREMENTS_COMPLETENESS:
  XSD_COMPLIANCE: "✅ All 14+ XSD configuration attributes specified with mappings"
  STATE_ATTRIBUTES: "✅ All 18+ operational metadata attributes documented"
  SQL_SPECIFICATIONS: "✅ Complete INFORMATION_SCHEMA and SHOW SEQUENCES queries"
  COMPARISON_LOGIC: "✅ All diff scenarios and edge cases covered"
  FRAMEWORK_INTEGRATION: "✅ All Liquibase interfaces and registration specified"
  
IMPLEMENTATION_READINESS:
  ACTIONABLE_REQUIREMENTS: "✅ All requirements immediately implementable"
  TEST_SCENARIOS: "✅ Comprehensive test coverage for all functionality"
  TDD_GUIDANCE: "✅ Step-by-step implementation plan provided"
  QUALITY_GATES: "✅ Validation checkpoints for each implementation phase"
  
HANDOFF_APPROVAL:
  PHASE_2_COMPLETE: "✅ Requirements documentation complete"
  XSD_COMPLIANCE_FRAMEWORK: "✅ Automated XSD validation specifications ready"
  IMPLEMENTATION_READY: "✅ Ready for Phase 3 TDD implementation"
  NEXT_PHASE: "ai_workflow_guide.md"
```

This requirements document provides complete, implementation-ready specifications for Snowflake Sequence snapshot/diff functionality with 100% XSD compliance, comprehensive state attribute coverage, and detailed framework integration guidance. Ready for TDD implementation in Phase 3.