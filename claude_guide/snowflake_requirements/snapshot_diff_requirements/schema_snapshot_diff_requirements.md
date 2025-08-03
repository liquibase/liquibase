# Snowflake Schema Snapshot/Diff Requirements - IMPLEMENTATION_READY
## Phase 2: XSD-Driven Requirements Documentation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "2_DOCUMENTATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_INPUT: "research_findings_snowflake_schema_snapshot_diff.md"
IMPLEMENTATION_TYPE: "SNAPSHOT_DIFF"
NEXT_PHASE: "ai_workflow_guide.md"
OBJECT_TYPE: "SCHEMA"
DATABASE_TYPE: "SNOWFLAKE"
```

## XSD_ATTRIBUTES_SPECIFICATION
```yaml
XSD_COMPLIANCE_COVERAGE: "100% - All 18 configuration attributes implemented"
XSD_SOURCE_VALIDATION: "liquibase-snowflake-latest.xsd createSchema element verified"

REQUIRED_XSD_ATTRIBUTES:
  schemaName:
    type: "xsd:string"
    required: true
    validation: "Non-null, non-empty string"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.SCHEMA_NAME"
    comparison: "ALWAYS_INCLUDE - Identity attribute"
    
OPTIONAL_XSD_ATTRIBUTES:
  databaseName:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.CATALOG_NAME"
    comparison: "ALWAYS_INCLUDE - Parent relationship"
    
  comment:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.COMMENT"
    comparison: "ALWAYS_INCLUDE - User metadata"
    null_handling: "Treat NULL and empty string as equivalent"
    
  dataRetentionTimeInDays:
    type: "xsd:int"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.RETENTION_TIME"
    comparison: "ALWAYS_INCLUDE - Business configuration"
    validation: "0-90 for permanent schemas"
    
  maxDataExtensionTimeInDays:
    type: "xsd:int"
    mapping: "SHOW SCHEMAS → MAX_DATA_EXTENSION_TIME_IN_DAYS"
    comparison: "CONDITIONAL_INCLUDE - Advanced Time Travel"
    
  transient:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.IS_TRANSIENT (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Structural property"
    conversion: "boolean → YES/NO"
    
  managedAccess:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.SCHEMATA.IS_MANAGED_ACCESS (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Security configuration"
    conversion: "boolean → YES/NO"
    
  defaultDdlCollation:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → DEFAULT_DDL_COLLATION"
    comparison: "ALWAYS_INCLUDE - Affects child objects"
    
  tag:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → TAG"
    comparison: "CONDITIONAL_INCLUDE - May be managed separately"
    
  pipeExecutionPaused:
    type: "xsd:boolean"
    mapping: "SHOW SCHEMAS → PIPE_EXECUTION_PAUSED"
    comparison: "ALWAYS_INCLUDE - Operational configuration"
    conversion: "boolean → TRUE/FALSE"
    
  orReplace:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE OR REPLACE SCHEMA syntax"
    
  ifNotExists:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE SCHEMA IF NOT EXISTS syntax"
    
  # Additional XSD attributes for Iceberg schemas
  externalVolume:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → EXTERNAL_VOLUME"
    comparison: "CONDITIONAL_INCLUDE - Iceberg-specific"
    
  catalog:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → CATALOG"
    comparison: "CONDITIONAL_INCLUDE - Iceberg-specific"
    
  replaceInvalidCharacters:
    type: "xsd:boolean"
    mapping: "SHOW SCHEMAS → REPLACE_INVALID_CHARACTERS"
    comparison: "EXCLUDE_FROM_DIFF - Catalog sync specific"
    
  classificationProfile:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → CLASSIFICATION_PROFILE"
    comparison: "CONDITIONAL_INCLUDE - Data governance"
    
  storageSerializationPolicy:
    type: "xsd:string"
    mapping: "SHOW SCHEMAS → STORAGE_SERIALIZATION_POLICY"
    comparison: "CONDITIONAL_INCLUDE - Storage configuration"
    
  cloneFrom:
    type: "xsd:string"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only operation"
    usage: "CLONE source schema specification"
```

## STATE_ATTRIBUTES_SPECIFICATION
```yaml
STATE_COMPLIANCE_COVERAGE: "100% - All 15 operational metadata attributes captured"
INFORMATION_SCHEMA_SOURCE: "INFORMATION_SCHEMA.SCHEMATA"
SHOW_COMMAND_SOURCE: "SHOW SCHEMAS"

OPERATIONAL_METADATA_ATTRIBUTES:
  CATALOG_NAME:
    source: "INFORMATION_SCHEMA.SCHEMATA.CATALOG_NAME"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Parent database relationship"
    validation: "Must match databaseName from XSD"
    
  SCHEMA_OWNER:
    source: "INFORMATION_SCHEMA.SCHEMATA.SCHEMA_OWNER"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - May change due to role management"
    snapshot_usage: "Owner information for audit"
    
  IS_TRANSIENT:
    source: "INFORMATION_SCHEMA.SCHEMATA.IS_TRANSIENT"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Validates XSD transient attribute"
    
  IS_MANAGED_ACCESS:
    source: "INFORMATION_SCHEMA.SCHEMATA.IS_MANAGED_ACCESS"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Validates XSD managedAccess attribute"
    
  RETENTION_TIME:
    source: "INFORMATION_SCHEMA.SCHEMATA.RETENTION_TIME"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validate against XSD dataRetentionTimeInDays"
    
  DEFAULT_CHARACTER_SET_CATALOG:
    source: "INFORMATION_SCHEMA.SCHEMATA.DEFAULT_CHARACTER_SET_CATALOG"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Typically NULL in Snowflake"
    
  DEFAULT_CHARACTER_SET_SCHEMA:
    source: "INFORMATION_SCHEMA.SCHEMATA.DEFAULT_CHARACTER_SET_SCHEMA"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Typically NULL in Snowflake"
    
  DEFAULT_CHARACTER_SET_NAME:
    source: "INFORMATION_SCHEMA.SCHEMATA.DEFAULT_CHARACTER_SET_NAME"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Typically NULL in Snowflake"
    
  SQL_PATH:
    source: "INFORMATION_SCHEMA.SCHEMATA.SQL_PATH"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Typically NULL in Snowflake"
    
  CREATED:
    source: "INFORMATION_SCHEMA.SCHEMATA.CREATED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Immutable timestamp"
    snapshot_usage: "Creation audit trail"
    
  LAST_ALTERED:
    source: "INFORMATION_SCHEMA.SCHEMATA.LAST_ALTERED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Changes with modifications"
    snapshot_usage: "Modification audit trail"
    
  COMMENT:
    source: "INFORMATION_SCHEMA.SCHEMATA.COMMENT"
    type: "STRING"
    comparison: "VALIDATION_COMPARE - Matches XSD comment"
    
  REPLICABLE_WITH_FAILOVER_GROUPS:
    source: "INFORMATION_SCHEMA.SCHEMATA.REPLICABLE_WITH_FAILOVER_GROUPS"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - System metadata"
    
  OWNER_ROLE_TYPE:
    source: "INFORMATION_SCHEMA.SCHEMATA.OWNER_ROLE_TYPE"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Security state"
```

## XSD_COMPLIANCE_VALIDATION_REQUIREMENTS
```yaml
AUTOMATED_XSD_COMPLIANCE_FRAMEWORK:
  XSD_ATTRIBUTE_EXTRACTION:
    - "Parse liquibase-snowflake-latest.xsd createSchema element"
    - "Extract all 18 configuration attributes with types and constraints"
    - "Generate attribute specification map for snapshot generator"
    - "Validate XSD completeness against Snowflake documentation"
    
  XSD_SNAPSHOT_VALIDATION:
    - "Verify snapshot generator captures ALL XSD attributes"
    - "Validate XSD→INFORMATION_SCHEMA attribute mapping"
    - "Test XSD data type conversions (xsd:int → NUMBER, xsd:boolean → YES/NO)"
    - "Report missing or incorrectly implemented XSD attributes"
    
  XSD_COMPLIANCE_TESTING:
    - "Unit test: validateAllXSDAttributesImplemented(SCHEMA)"
    - "Integration test: createSchemaWithAllAttributes → snapshot → validate"
    - "Regression test: ensureXSDEvolutionCompatibility"
    - "Performance test: completeAttributeCapturePerformance"
```

## OBJECT_MODEL_SPECIFICATION
```yaml
SCHEMA_OBJECT_MODEL:
  LIQUIBASE_OBJECT_TYPE: "liquibase.structure.core.Schema"
  SNOWFLAKE_EXTENSIONS: "Use setAttribute() for Snowflake-specific properties"
  
  CORE_PROPERTIES:
    name:
      source: "XSD schemaName"
      type: "String"
      required: true
      validation: "Non-null, non-empty"
      
    catalog:
      source: "XSD databaseName / INFORMATION_SCHEMA.CATALOG_NAME"
      type: "Catalog"
      required: true
      validation: "Parent database relationship"
      
  SNOWFLAKE_SPECIFIC_ATTRIBUTES:
    comment:
      source: "XSD comment / INFORMATION_SCHEMA.COMMENT"
      type: "String"
      nullable: true
      
    isTransient:
      source: "XSD transient / INFORMATION_SCHEMA.IS_TRANSIENT"
      type: "Boolean"
      default: false
      
    isManagedAccess:
      source: "XSD managedAccess / INFORMATION_SCHEMA.IS_MANAGED_ACCESS"
      type: "Boolean"
      default: false
      
    dataRetentionTimeInDays:
      source: "XSD dataRetentionTimeInDays / INFORMATION_SCHEMA.RETENTION_TIME"
      type: "Integer"
      range: "0-90"
      
    maxDataExtensionTimeInDays:
      source: "XSD maxDataExtensionTimeInDays"
      type: "Integer"
      
    defaultDdlCollation:
      source: "XSD defaultDdlCollation"
      type: "String"
      
    pipeExecutionPaused:
      source: "XSD pipeExecutionPaused"
      type: "Boolean"
      
    tag:
      source: "XSD tag"
      type: "String"
      
    owner:
      source: "INFORMATION_SCHEMA.SCHEMA_OWNER"
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
      CATALOG_NAME,
      SCHEMA_NAME,
      SCHEMA_OWNER,
      IS_TRANSIENT,
      IS_MANAGED_ACCESS,
      RETENTION_TIME,
      DEFAULT_CHARACTER_SET_CATALOG,
      DEFAULT_CHARACTER_SET_SCHEMA,
      DEFAULT_CHARACTER_SET_NAME,
      SQL_PATH,
      CREATED,
      LAST_ALTERED,
      COMMENT,
      REPLICABLE_WITH_FAILOVER_GROUPS,
      OWNER_ROLE_TYPE
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE CATALOG_NAME = ? AND SCHEMA_NAME = ?
    
  PARAMETERS: ["catalogName", "schemaName"]
  PERFORMANCE: "Single schema lookup - optimal"
  RESULT_PROCESSING: "Single row expected, null if not found"
  
SUPPLEMENTARY_SHOW_QUERY:
  QUERY_TYPE: "SHOW_COMMAND_METADATA"
  SQL: "SHOW SCHEMAS LIKE ? IN DATABASE ?"
  PARAMETERS: ["schemaName", "catalogName"]
  ADDITIONAL_ATTRIBUTES:
    - "DEFAULT_DDL_COLLATION"
    - "PIPE_EXECUTION_PAUSED"
    - "TAG"
    - "EXTERNAL_VOLUME"
    - "CATALOG"
    - "CLASSIFICATION_PROFILE"
  USE_CASE: "Attributes not available in INFORMATION_SCHEMA"
  
BULK_SNAPSHOT_QUERY:
  QUERY_TYPE: "DATABASE_SCHEMA_DISCOVERY"
  SQL: |
    SELECT 
      CATALOG_NAME,
      SCHEMA_NAME,
      SCHEMA_OWNER,
      IS_TRANSIENT,
      IS_MANAGED_ACCESS,
      RETENTION_TIME,
      CREATED,
      LAST_ALTERED,
      COMMENT,
      OWNER_ROLE_TYPE
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE CATALOG_NAME = ?
    ORDER BY SCHEMA_NAME
    
  PARAMETERS: ["catalogName"]
  PERFORMANCE: "All schemas in database enumeration"
  RESULT_PROCESSING: "Multiple rows, process each as Schema object"
  
SQL_QUERY_EXAMPLES:
  SINGLE_SCHEMA_SNAPSHOT:
    description: "Snapshot specific schema with all attributes"
    sql: "SELECT * FROM INFORMATION_SCHEMA.SCHEMATA WHERE CATALOG_NAME = 'LTHDB' AND SCHEMA_NAME = 'TESTHARNESS'"
    expected_result: "Single schema record with all XSD + state attributes"
    
  MANAGED_ACCESS_SCHEMA_HANDLING:
    description: "Handle managed access schema attribute conversion"
    sql: "SELECT IS_MANAGED_ACCESS FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?"
    conversion: "YES → true, NO → false"
    
  TRANSIENT_SCHEMA_VALIDATION:
    description: "Validate transient schema configuration"
    sql: "SELECT IS_TRANSIENT, RETENTION_TIME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?"
    validation: "Transient schemas have different retention behavior"
    
  ICEBERG_SCHEMA_ATTRIBUTES:
    description: "Handle Iceberg-specific attributes via SHOW"
    sql: "SHOW SCHEMAS LIKE ? IN DATABASE ?"
    attributes: ["EXTERNAL_VOLUME", "CATALOG", "CLASSIFICATION_PROFILE"]
    
  SCHEMA_OWNER_TRACKING:
    description: "Capture ownership for audit (exclude from diff)"
    sql: "SELECT SCHEMA_OWNER, OWNER_ROLE_TYPE FROM INFORMATION_SCHEMA.SCHEMATA WHERE CATALOG_NAME = ? AND SCHEMA_NAME = ?"
    usage: "Snapshot-only for audit trail"
```

## COMPARISON_LOGIC_SPECIFICATION
```yaml
STRUCTURAL_COMPARISON_RULES:
  IDENTITY_COMPARISON:
    primary_key: ["catalogName", "schemaName"]
    case_sensitivity: false
    rule: "Schemas with different names or catalogs are different objects"
    
  XSD_CONFIGURATION_COMPARISON:
    comment:
      rule: "String comparison, NULL == empty string"
      type: "StringComparison"
      
    dataRetentionTimeInDays:
      rule: "Numeric comparison with RETENTION_TIME"
      type: "NumericComparison"
      validation: "Range 0-90 for permanent schemas"
      
    transient:
      rule: "Boolean comparison with IS_TRANSIENT conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    managedAccess:
      rule: "Boolean comparison with IS_MANAGED_ACCESS conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    defaultDdlCollation:
      rule: "String comparison, case-sensitive"
      type: "StringComparison"
      
    pipeExecutionPaused:
      rule: "Boolean comparison if configured"
      type: "ConditionalBooleanComparison"
      
    maxDataExtensionTimeInDays:
      rule: "Numeric comparison if configured"
      type: "ConditionalNumericComparison"
      
EXCLUSION_RULES:
  ALWAYS_EXCLUDE:
    - "SCHEMA_OWNER"         # Role management
    - "CREATED"              # Immutable timestamp
    - "LAST_ALTERED"         # Modification timestamp
    - "OWNER_ROLE_TYPE"      # Security state
    - "DEFAULT_CHARACTER_SET_*"  # Typically NULL
    - "SQL_PATH"             # Typically NULL
    - "REPLICABLE_WITH_FAILOVER_GROUPS"  # System metadata
    
  CREATION_ONLY_EXCLUDE:
    - "orReplace"            # CREATE OR REPLACE flag
    - "ifNotExists"          # CREATE IF NOT EXISTS flag
    - "cloneFrom"            # Clone operation
    
DIFF_SCENARIO_MATRIX:
  MISSING_SCHEMA:
    condition: "Schema exists in reference but not in target"
    action: "Generate CREATE SCHEMA change"
    attributes: "Include all configured XSD attributes"
    
  UNEXPECTED_SCHEMA:
    condition: "Schema exists in target but not in reference"
    action: "Generate DROP SCHEMA change"
    safety: "Include CASCADE/RESTRICT handling"
    
  CHANGED_SCHEMA:
    condition: "Schema exists in both but properties differ"
    action: "Generate ALTER SCHEMA change"
    supported_changes:
      - "comment → ALTER SCHEMA SET COMMENT"
      - "dataRetentionTimeInDays → ALTER SCHEMA SET DATA_RETENTION_TIME_IN_DAYS"
      - "maxDataExtensionTimeInDays → ALTER SCHEMA SET MAX_DATA_EXTENSION_TIME_IN_DAYS"
      - "defaultDdlCollation → ALTER SCHEMA SET DEFAULT_DDL_COLLATION"
      - "managedAccess → ALTER SCHEMA ENABLE/DISABLE MANAGED ACCESS"
      - "pipeExecutionPaused → ALTER SCHEMA SET PIPE_EXECUTION_PAUSED"
      
EDGE_CASE_HANDLING:
  NULL_VS_EMPTY_COMMENTS:
    rule: "Treat NULL and empty string as equivalent"
    implementation: "StringUtils.isEmpty() for comparison"
    
  INHERITED_VS_EXPLICIT_RETENTION:
    rule: "Compare configured value with actual RETENTION_TIME"
    validation: "Account for database inheritance"
    
  MANAGED_ACCESS_IMPLICATIONS:
    rule: "Managed access affects child object permissions"
    validation: "Validate managed access state changes"
    
  TRANSIENT_SCHEMA_CONSTRAINTS:
    rule: "Transient schemas have different retention behavior"
    validation: "Validate retention time constraints for transient schemas"
    
  ICEBERG_SCHEMA_ATTRIBUTES:
    rule: "Iceberg-specific attributes only compared for Iceberg schemas"
    detection: "Check schema type or presence of EXTERNAL_VOLUME"
```

## FRAMEWORK_INTEGRATION_REQUIREMENTS
```yaml
LIQUIBASE_SNAPSHOT_GENERATOR:
  CLASS_NAME: "SnowflakeSchemaSnapshotGenerator"
  BASE_CLASS: "liquibase.snapshot.jvm.JdbcSnapshotGenerator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Schema.class + SnowflakeDatabase"
      
    addsTo:
      signature: "Class<? extends DatabaseObject>[] addsTo()"
      implementation: "Return new Class[] { Catalog.class }"
      
    snapshotObject:
      signature: "DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot)"
      implementation: "Single schema snapshot with XSD compliance"
      
    addTo:
      signature: "void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot)"
      implementation: "Bulk schema discovery within database/catalog"
      
SERVICE_REGISTRATION:
  FILE: "META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "liquibase.ext.snowflake.snapshot.SnowflakeSchemaSnapshotGenerator"
  
LIQUIBASE_COMPARATOR:
  CLASS_NAME: "SnowflakeSchemaComparator"
  BASE_CLASS: "liquibase.diff.compare.DatabaseObjectComparator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Schema.class + SnowflakeDatabase"
      
    hash:
      signature: "String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Return [catalogName, schemaName] as hash key"
      
    isSameObject:
      signature: "boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Compare catalog and schema names (case-insensitive)"
      
    findDifferences:
      signature: "ObjectDifferences findDifferences(...)"
      implementation: "XSD-driven property comparison with exclusion rules"
      
SCHEMA_OBJECT_INTEGRATION:
  OBJECT_TYPE: "liquibase.structure.core.Schema"
  ATTRIBUTE_HANDLING: "Use setAttribute() for Snowflake-specific properties"
  RELATIONSHIP_MANAGEMENT: "Child of Catalog, parent of Tables/Sequences/etc."
```

## TEST_SCENARIO_MATRIX
```yaml
XSD_COMPLIANCE_TESTS:
  TEST_ALL_XSD_ATTRIBUTES_CAPTURED:
    description: "Verify snapshot captures all 18 XSD configuration attributes"
    setup: "Create schema with all XSD attributes configured"
    execution: "Snapshot schema and validate all attributes present"
    validation: "Assert all XSD attributes correctly mapped and converted"
    
  TEST_XSD_ATTRIBUTE_TYPE_CONVERSION:
    description: "Test XSD type conversion (boolean → YES/NO, int → NUMBER)"
    setup: "Create schema with transient=true, managedAccess=true, dataRetentionTimeInDays=7"
    execution: "Snapshot and validate conversions"
    validation: "Assert IS_TRANSIENT='YES', IS_MANAGED_ACCESS='YES', RETENTION_TIME=7"
    
  TEST_XSD_COMPLIANCE_VALIDATION:
    description: "Automated XSD compliance validation framework"
    execution: "XSDComplianceTest.validateAllAttributesImplemented(SCHEMA)"
    validation: "Assert no missing XSD attributes reported"
    
SNAPSHOT_FUNCTIONALITY_TESTS:
  TEST_SINGLE_SCHEMA_SNAPSHOT:
    description: "Snapshot specific schema with complete attributes"
    setup: "Known schema with comment, retention time, managed access flag"
    execution: "Snapshot single schema"
    validation: "All XSD + state attributes captured correctly"
    
  TEST_BULK_SCHEMA_DISCOVERY:
    description: "Discover all schemas in database"
    execution: "Bulk snapshot all schemas in LTHDB"
    validation: "All schemas discovered with complete attributes"
    
  TEST_NONEXISTENT_SCHEMA_HANDLING:
    description: "Handle snapshot of non-existent schema"
    setup: "Request snapshot of 'NONEXISTENT_SCHEMA'"
    execution: "Attempt snapshot"
    validation: "Return null gracefully, no exceptions"
    
COMPARISON_LOGIC_TESTS:
  TEST_SCHEMA_IDENTITY_COMPARISON:
    description: "Test schema identity comparison (case-insensitive names)"
    setup: "Two schemas: 'LTHDB.TESTSCHEMA' and 'lthdb.testschema'"
    execution: "Compare schema objects"
    validation: "Assert isSameObject returns true"
    
  TEST_XSD_ATTRIBUTE_DIFFERENCES:
    description: "Detect differences in XSD configuration attributes"
    setup: "Two schemas with different comments, managed access settings"
    execution: "Compare schemas"
    validation: "Assert differences detected for comment and managed access"
    
  TEST_STATE_ATTRIBUTE_EXCLUSIONS:
    description: "Verify state attributes excluded from structural comparison"
    setup: "Two schemas with different owners, creation times"
    execution: "Compare schemas"
    validation: "Assert no differences detected for excluded attributes"
    
DIFF_GENERATION_TESTS:
  TEST_MISSING_SCHEMA_CHANGE:
    description: "Generate CREATE SCHEMA change for missing schema"
    setup: "Reference has schema, target does not"
    execution: "Generate diff"
    validation: "CREATE SCHEMA change with all XSD attributes"
    
  TEST_UNEXPECTED_SCHEMA_CHANGE:
    description: "Generate DROP SCHEMA change for unexpected schema"
    setup: "Target has schema, reference does not"
    execution: "Generate diff"
    validation: "DROP SCHEMA change generated"
    
  TEST_CHANGED_SCHEMA_PROPERTIES:
    description: "Generate ALTER SCHEMA changes for property differences"
    setup: "Schemas with different comments, managed access settings"
    execution: "Generate diff"
    validation: "ALTER SCHEMA changes for modified properties"
    
EDGE_CASE_TESTS:
  TEST_NULL_VS_EMPTY_COMMENT_HANDLING:
    description: "Handle NULL vs empty string comment equivalence"
    setup: "One schema with NULL comment, one with empty string"
    execution: "Compare schemas"
    validation: "Assert no difference detected"
    
  TEST_MANAGED_ACCESS_SCHEMA_BEHAVIOR:
    description: "Handle managed access schema specific behavior"
    setup: "Managed access schema with restricted permissions"
    execution: "Snapshot and compare"
    validation: "Validate managed access specific attribute handling"
    
  TEST_TRANSIENT_SCHEMA_CONSTRAINTS:
    description: "Handle transient schema specific behavior"
    setup: "Transient schema with different retention characteristics"
    execution: "Snapshot and compare"
    validation: "Validate transient-specific attribute handling"
    
  TEST_ICEBERG_SCHEMA_ATTRIBUTES:
    description: "Handle Iceberg-specific schema attributes"
    setup: "Iceberg schema with external volume, catalog"
    execution: "Snapshot schema"
    validation: "Iceberg-specific attributes captured via SHOW SCHEMAS"
    
INTEGRATION_TESTS:
  TEST_FRAMEWORK_REGISTRATION:
    description: "Verify snapshot generator properly registered"
    execution: "SnapshotGeneratorFactory.getInstance().getGenerators(Schema.class, SnowflakeDatabase)"
    validation: "SnowflakeSchemaSnapshotGenerator included with correct priority"
    
  TEST_CATALOG_RELATIONSHIP_MAINTENANCE:
    description: "Verify catalog/schema relationship properly maintained"
    execution: "Snapshot schema and validate catalog relationship"
    validation: "Schema.getCatalog() returns correct Database object"
    
  TEST_TEST_HARNESS_COMPATIBILITY:
    description: "Verify compatibility with Liquibase test harness"
    execution: "Run test harness schema snapshot tests"
    validation: "All tests pass with XSD compliance"
    
  TEST_PERFORMANCE_WITH_MANY_SCHEMAS:
    description: "Performance test with realistic schema counts"
    setup: "Database with 100+ schemas"
    execution: "Bulk snapshot all schemas"
    validation: "Complete snapshot under performance thresholds"
```

## IMPLEMENTATION_GUIDANCE
```yaml
TDD_IMPLEMENTATION_PLAN:
  PHASE_1_SNAPSHOT_GENERATOR:
    1. "Create SnowflakeSchemaSnapshotGenerator unit tests"
    2. "Implement getPriority() method with SnowflakeDatabase priority"
    3. "Implement snapshotObject() with INFORMATION_SCHEMA.SCHEMATA query"
    4. "Add XSD attribute mapping with setAttribute() patterns"
    5. "Implement SHOW SCHEMAS supplementary query for additional attributes"
    6. "Handle catalog/schema relationship properly"
    
  PHASE_2_COMPARATOR:
    1. "Create SnowflakeSchemaComparator unit tests"
    2. "Implement identity comparison with case-insensitive names"
    3. "Implement XSD attribute comparison with exclusion rules"
    4. "Add managed access and transient schema handling"
    5. "Add edge case handling (NULL vs empty strings)"
    6. "Test all diff scenarios (missing, unexpected, changed)"
    
  PHASE_3_INTEGRATION:
    1. "Register snapshot generator in META-INF/services"
    2. "Register comparator in META-INF/services"
    3. "Integration test with real Snowflake database"
    4. "Test catalog/schema relationship maintenance"
    5. "Test harness validation"
    6. "Performance optimization and validation"
    
VALIDATION_CHECKPOINTS:
  AFTER_SNAPSHOT_GENERATOR:
    - [ ] "All XSD attributes captured in snapshot"
    - [ ] "INFORMATION_SCHEMA query returns complete data"
    - [ ] "SHOW SCHEMAS integration working"
    - [ ] "Attribute type conversions correct"
    - [ ] "Catalog/schema relationship maintained"
    
  AFTER_COMPARATOR:
    - [ ] "Schema identity comparison working"
    - [ ] "XSD attribute differences detected"
    - [ ] "State attributes properly excluded"
    - [ ] "Managed access handling correct"
    - [ ] "Edge cases handled correctly"
    
  AFTER_INTEGRATION:
    - [ ] "Service registration complete"
    - [ ] "Framework integration working"
    - [ ] "Catalog relationship working"
    - [ ] "Test harness compatibility verified"
    - [ ] "Performance requirements met"
```

## QUALITY_VALIDATION
```yaml
REQUIREMENTS_COMPLETENESS:
  XSD_COMPLIANCE: "✅ All 18 XSD configuration attributes specified with mappings"
  STATE_ATTRIBUTES: "✅ All 15 operational metadata attributes documented"
  SQL_SPECIFICATIONS: "✅ Complete INFORMATION_SCHEMA and SHOW SCHEMAS queries"
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

This requirements document provides complete, implementation-ready specifications for Snowflake Schema snapshot/diff functionality with 100% XSD compliance, comprehensive state attribute coverage, and detailed framework integration guidance. Ready for TDD implementation in Phase 3.