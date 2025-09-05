# Snowflake Database Snapshot/Diff Requirements - IMPLEMENTATION_READY
## Phase 2: XSD-Driven Requirements Documentation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "2_DOCUMENTATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_INPUT: "research_findings_snowflake_database_snapshot_diff.md"
IMPLEMENTATION_TYPE: "SNAPSHOT_DIFF"
NEXT_PHASE: "ai_workflow_guide.md"
OBJECT_TYPE: "DATABASE"
DATABASE_TYPE: "SNOWFLAKE"
```

## XSD_ATTRIBUTES_SPECIFICATION
```yaml
XSD_COMPLIANCE_COVERAGE: "100% - All 18 configuration attributes implemented"
XSD_SOURCE_VALIDATION: "liquibase-snowflake-latest.xsd createDatabase element verified"

REQUIRED_XSD_ATTRIBUTES:
  databaseName:
    type: "xsd:string"
    required: true
    validation: "Non-null, non-empty string"
    mapping: "INFORMATION_SCHEMA.DATABASES.DATABASE_NAME"
    comparison: "ALWAYS_INCLUDE - Identity attribute"
    
OPTIONAL_XSD_ATTRIBUTES:
  comment:
    type: "xsd:string"
    mapping: "INFORMATION_SCHEMA.DATABASES.COMMENT"
    comparison: "ALWAYS_INCLUDE - User metadata"
    null_handling: "Treat NULL and empty string as equivalent"
    
  dataRetentionTimeInDays:
    type: "xsd:int"
    mapping: "INFORMATION_SCHEMA.DATABASES.RETENTION_TIME"
    comparison: "ALWAYS_INCLUDE - Business configuration"
    validation: "0-90 for permanent databases"
    
  maxDataExtensionTimeInDays:
    type: "xsd:int"
    mapping: "SHOW DATABASES → MAX_DATA_EXTENSION_TIME_IN_DAYS"
    comparison: "CONDITIONAL_INCLUDE - Advanced Time Travel"
    
  transient:
    type: "xsd:boolean"
    mapping: "INFORMATION_SCHEMA.DATABASES.IS_TRANSIENT (YES/NO)"
    comparison: "ALWAYS_INCLUDE - Structural property"
    conversion: "boolean → YES/NO"
    
  defaultDdlCollation:
    type: "xsd:string"
    mapping: "SHOW DATABASES → DEFAULT_DDL_COLLATION"
    comparison: "ALWAYS_INCLUDE - Affects child objects"
    
  tag:
    type: "xsd:string"
    mapping: "SHOW DATABASES → TAG"
    comparison: "CONDITIONAL_INCLUDE - May be managed separately"
    
  orReplace:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE OR REPLACE DATABASE syntax"
    
  ifNotExists:
    type: "xsd:boolean"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only flag"
    usage: "CREATE DATABASE IF NOT EXISTS syntax"
    
  # Additional XSD attributes for Iceberg databases
  externalVolume:
    type: "xsd:string"
    mapping: "SHOW DATABASES → EXTERNAL_VOLUME"
    comparison: "CONDITIONAL_INCLUDE - Iceberg-specific"
    
  catalog:
    type: "xsd:string"
    mapping: "SHOW DATABASES → CATALOG"
    comparison: "CONDITIONAL_INCLUDE - Iceberg-specific"
    
  replaceInvalidCharacters:
    type: "xsd:boolean"
    mapping: "SHOW DATABASES → REPLACE_INVALID_CHARACTERS"
    comparison: "EXCLUDE_FROM_DIFF - Catalog sync specific"
    
  storageSerializationPolicy:
    type: "xsd:string"
    mapping: "SHOW DATABASES → STORAGE_SERIALIZATION_POLICY"
    comparison: "CONDITIONAL_INCLUDE - Storage configuration"
    
  catalogSync:
    type: "xsd:string"
    mapping: "SHOW DATABASES → CATALOG_SYNC"
    comparison: "EXCLUDE_FROM_DIFF - Synchronization setting"
    
  catalogSyncNamespaceMode:
    type: "xsd:string"
    mapping: "SHOW DATABASES → CATALOG_SYNC_NAMESPACE_MODE"
    comparison: "EXCLUDE_FROM_DIFF - Sync configuration"
    
  catalogSyncNamespaceFlattenDelimiter:
    type: "xsd:string"
    mapping: "SHOW DATABASES → CATALOG_SYNC_NAMESPACE_FLATTEN_DELIMITER"
    comparison: "EXCLUDE_FROM_DIFF - Sync configuration"
    
  cloneFrom:
    type: "xsd:string"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only operation"
    usage: "CLONE source database specification"
    
  fromDatabase:
    type: "xsd:string"
    comparison: "EXCLUDE_FROM_DIFF - Creation-only operation"
    usage: "Alternative clone source specification"
```

## STATE_ATTRIBUTES_SPECIFICATION
```yaml
STATE_COMPLIANCE_COVERAGE: "100% - All 10 operational metadata attributes captured"
INFORMATION_SCHEMA_SOURCE: "INFORMATION_SCHEMA.DATABASES"
SHOW_COMMAND_SOURCE: "SHOW DATABASES"

OPERATIONAL_METADATA_ATTRIBUTES:
  DATABASE_OWNER:
    source: "INFORMATION_SCHEMA.DATABASES.DATABASE_OWNER"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - May change due to role management"
    snapshot_usage: "Owner information for audit"
    
  IS_TRANSIENT:
    source: "INFORMATION_SCHEMA.DATABASES.IS_TRANSIENT"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Validates XSD transient attribute"
    
  CREATED:
    source: "INFORMATION_SCHEMA.DATABASES.CREATED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Immutable timestamp"
    snapshot_usage: "Creation audit trail"
    
  LAST_ALTERED:
    source: "INFORMATION_SCHEMA.DATABASES.LAST_ALTERED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Changes with modifications"
    snapshot_usage: "Modification audit trail"
    
  RETENTION_TIME:
    source: "INFORMATION_SCHEMA.DATABASES.RETENTION_TIME"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validate against XSD dataRetentionTimeInDays"
    
  TYPE:
    source: "INFORMATION_SCHEMA.DATABASES.TYPE"
    type: "STRING"
    comparison: "SNAPSHOT_ONLY - Database type classification"
    values: ["STANDARD", "SHARED", "IMPORTED"]
    
  REPLICABLE_WITH_FAILOVER_GROUPS:
    source: "INFORMATION_SCHEMA.DATABASES.REPLICABLE_WITH_FAILOVER_GROUPS"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - System metadata"
    
  OWNER_ROLE_TYPE:
    source: "INFORMATION_SCHEMA.DATABASES.OWNER_ROLE_TYPE"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Security state"
    
SHOW_DATABASES_EXCLUSIVE_ATTRIBUTES:
  IS_DEFAULT:
    source: "SHOW DATABASES.IS_DEFAULT"
    type: "STRING (Y/N)"
    comparison: "EXCLUDE_FROM_DIFF - Session-specific state"
    
  IS_CURRENT:
    source: "SHOW DATABASES.IS_CURRENT"
    type: "STRING (Y/N)"
    comparison: "EXCLUDE_FROM_DIFF - Session-specific state"
    
  ORIGIN:
    source: "SHOW DATABASES.ORIGIN"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - System metadata"
    
  OPTIONS:
    source: "SHOW DATABASES.OPTIONS"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - System metadata"
```

## XSD_COMPLIANCE_VALIDATION_REQUIREMENTS
```yaml
AUTOMATED_XSD_COMPLIANCE_FRAMEWORK:
  XSD_ATTRIBUTE_EXTRACTION:
    - "Parse liquibase-snowflake-latest.xsd createDatabase element"
    - "Extract all 18 configuration attributes with types and constraints"
    - "Generate attribute specification map for snapshot generator"
    - "Validate XSD completeness against Snowflake documentation"
    
  XSD_SNAPSHOT_VALIDATION:
    - "Verify snapshot generator captures ALL XSD attributes"
    - "Validate XSD→INFORMATION_SCHEMA attribute mapping"
    - "Test XSD data type conversions (xsd:int → NUMBER, xsd:boolean → YES/NO)"
    - "Report missing or incorrectly implemented XSD attributes"
    
  XSD_COMPLIANCE_TESTING:
    - "Unit test: validateAllXSDAttributesImplemented(DATABASE)"
    - "Integration test: createDatabaseWithAllAttributes → snapshot → validate"
    - "Regression test: ensureXSDEvolutionCompatibility"
    - "Performance test: completeAttributeCapturePerformance"
```

## OBJECT_MODEL_SPECIFICATION
```yaml
DATABASE_OBJECT_MODEL:
  LIQUIBASE_OBJECT_TYPE: "liquibase.structure.core.Database"
  SNOWFLAKE_EXTENSIONS: "Use setAttribute() for Snowflake-specific properties"
  
  CORE_PROPERTIES:
    name:
      source: "XSD databaseName"
      type: "String"
      required: true
      validation: "Non-null, non-empty"
      
    comment:
      source: "XSD comment / INFORMATION_SCHEMA.COMMENT"
      type: "String"
      nullable: true
      
  SNOWFLAKE_SPECIFIC_ATTRIBUTES:
    isTransient:
      source: "XSD transient / INFORMATION_SCHEMA.IS_TRANSIENT"
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
      
    tag:
      source: "XSD tag"
      type: "String"
      
    databaseType:
      source: "INFORMATION_SCHEMA.TYPE"
      type: "String"
      readonly: true
      
    owner:
      source: "INFORMATION_SCHEMA.DATABASE_OWNER"
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
      DATABASE_NAME,
      DATABASE_OWNER,
      IS_TRANSIENT,
      COMMENT,
      CREATED,
      LAST_ALTERED,
      RETENTION_TIME,
      TYPE,
      REPLICABLE_WITH_FAILOVER_GROUPS,
      OWNER_ROLE_TYPE
    FROM INFORMATION_SCHEMA.DATABASES
    WHERE DATABASE_NAME = ?
    
  PARAMETERS: ["databaseName"]
  PERFORMANCE: "Single database lookup - optimal"
  RESULT_PROCESSING: "Single row expected, null if not found"
  
SUPPLEMENTARY_SHOW_QUERY:
  QUERY_TYPE: "SHOW_COMMAND_METADATA"
  SQL: "SHOW DATABASES LIKE ?"
  PARAMETERS: ["databaseName"]
  ADDITIONAL_ATTRIBUTES:
    - "IS_DEFAULT"
    - "IS_CURRENT"
    - "ORIGIN"
    - "OPTIONS"
    - "DEFAULT_DDL_COLLATION"
    - "EXTERNAL_VOLUME"
    - "CATALOG"
  USE_CASE: "Attributes not available in INFORMATION_SCHEMA"
  
BULK_SNAPSHOT_QUERY:
  QUERY_TYPE: "SCHEMA_DISCOVERY"
  SQL: |
    SELECT 
      DATABASE_NAME,
      DATABASE_OWNER,
      IS_TRANSIENT,
      COMMENT,
      CREATED,
      LAST_ALTERED,
      RETENTION_TIME,
      TYPE,
      OWNER_ROLE_TYPE
    FROM INFORMATION_SCHEMA.DATABASES
    ORDER BY DATABASE_NAME
    
  PERFORMANCE: "All databases enumeration"
  RESULT_PROCESSING: "Multiple rows, process each as Database object"
  
SQL_QUERY_EXAMPLES:
  SINGLE_DATABASE_SNAPSHOT:
    description: "Snapshot specific database with all attributes"
    sql: "SELECT * FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = 'LTHDB'"
    expected_result: "Single database record with all XSD + state attributes"
    
  TRANSIENT_DATABASE_HANDLING:
    description: "Handle transient database attribute conversion"
    sql: "SELECT IS_TRANSIENT FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = ?"
    conversion: "YES → true, NO → false"
    
  RETENTION_TIME_VALIDATION:
    description: "Validate configured vs actual retention time"
    sql: "SELECT RETENTION_TIME FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = ?"
    validation: "Compare with XSD dataRetentionTimeInDays"
    
  ICEBERG_DATABASE_ATTRIBUTES:
    description: "Handle Iceberg-specific attributes via SHOW"
    sql: "SHOW DATABASES LIKE ?"
    attributes: ["EXTERNAL_VOLUME", "CATALOG", "STORAGE_SERIALIZATION_POLICY"]
    
  DATABASE_OWNER_TRACKING:
    description: "Capture ownership for audit (exclude from diff)"
    sql: "SELECT DATABASE_OWNER, OWNER_ROLE_TYPE FROM INFORMATION_SCHEMA.DATABASES WHERE DATABASE_NAME = ?"
    usage: "Snapshot-only for audit trail"
```

## COMPARISON_LOGIC_SPECIFICATION
```yaml
STRUCTURAL_COMPARISON_RULES:
  IDENTITY_COMPARISON:
    primary_key: "databaseName"
    case_sensitivity: false
    rule: "Databases with different names are different objects"
    
  XSD_CONFIGURATION_COMPARISON:
    comment:
      rule: "String comparison, NULL == empty string"
      type: "StringComparison"
      
    dataRetentionTimeInDays:
      rule: "Numeric comparison with RETENTION_TIME"
      type: "NumericComparison"
      validation: "Range 0-90 for permanent databases"
      
    transient:
      rule: "Boolean comparison with IS_TRANSIENT conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    defaultDdlCollation:
      rule: "String comparison, case-sensitive"
      type: "StringComparison"
      
    maxDataExtensionTimeInDays:
      rule: "Numeric comparison if configured"
      type: "ConditionalNumericComparison"
      
EXCLUSION_RULES:
  ALWAYS_EXCLUDE:
    - "DATABASE_OWNER"         # Role management
    - "CREATED"                # Immutable timestamp
    - "LAST_ALTERED"           # Modification timestamp
    - "IS_DEFAULT"             # Session state
    - "IS_CURRENT"             # Session state
    - "OWNER_ROLE_TYPE"        # Security state
    - "ORIGIN"                 # System metadata
    - "OPTIONS"                # System metadata
    - "REPLICABLE_WITH_FAILOVER_GROUPS"  # System metadata
    
  CREATION_ONLY_EXCLUDE:
    - "orReplace"              # CREATE OR REPLACE flag
    - "ifNotExists"            # CREATE IF NOT EXISTS flag
    - "cloneFrom"              # Clone operation
    - "fromDatabase"           # Clone source
    
DIFF_SCENARIO_MATRIX:
  MISSING_DATABASE:
    condition: "Database exists in reference but not in target"
    action: "Generate CREATE DATABASE change"
    attributes: "Include all configured XSD attributes"
    
  UNEXPECTED_DATABASE:
    condition: "Database exists in target but not in reference"
    action: "Generate DROP DATABASE change"
    safety: "Include CASCADE/RESTRICT handling"
    
  CHANGED_DATABASE:
    condition: "Database exists in both but properties differ"
    action: "Generate ALTER DATABASE change"
    supported_changes:
      - "comment → ALTER DATABASE SET COMMENT"
      - "dataRetentionTimeInDays → ALTER DATABASE SET DATA_RETENTION_TIME_IN_DAYS"
      - "maxDataExtensionTimeInDays → ALTER DATABASE SET MAX_DATA_EXTENSION_TIME_IN_DAYS"
      - "defaultDdlCollation → ALTER DATABASE SET DEFAULT_DDL_COLLATION"
      
EDGE_CASE_HANDLING:
  NULL_VS_EMPTY_COMMENTS:
    rule: "Treat NULL and empty string as equivalent"
    implementation: "StringUtils.isEmpty() for comparison"
    
  INHERITED_VS_EXPLICIT_RETENTION:
    rule: "Compare configured value with actual RETENTION_TIME"
    validation: "Account for schema/database inheritance"
    
  TRANSIENT_DATABASE_CONSTRAINTS:
    rule: "Transient databases have different retention behavior"
    validation: "Validate retention time constraints for transient databases"
    
  ICEBERG_DATABASE_ATTRIBUTES:
    rule: "Iceberg-specific attributes only compared for Iceberg databases"
    detection: "Check database type or presence of EXTERNAL_VOLUME"
```

## FRAMEWORK_INTEGRATION_REQUIREMENTS
```yaml
LIQUIBASE_SNAPSHOT_GENERATOR:
  CLASS_NAME: "SnowflakeDatabaseSnapshotGenerator"
  BASE_CLASS: "liquibase.snapshot.jvm.JdbcSnapshotGenerator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Database.class + SnowflakeDatabase"
      
    addsTo:
      signature: "Class<? extends DatabaseObject>[] addsTo()"
      implementation: "Return new Class[] { CatalogAndSchema.class }"
      
    snapshotObject:
      signature: "DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot)"
      implementation: "Single database snapshot with XSD compliance"
      
    addTo:
      signature: "void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot)"
      implementation: "Bulk database discovery (typically not used for databases)"
      
SERVICE_REGISTRATION:
  FILE: "META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "liquibase.ext.snowflake.snapshot.SnowflakeDatabaseSnapshotGenerator"
  
LIQUIBASE_COMPARATOR:
  CLASS_NAME: "SnowflakeDatabaseComparator"
  BASE_CLASS: "liquibase.diff.compare.DatabaseObjectComparator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Database.class + SnowflakeDatabase"
      
    hash:
      signature: "String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Return database name as hash key"
      
    isSameObject:
      signature: "boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Compare database names (case-insensitive)"
      
    findDifferences:
      signature: "ObjectDifferences findDifferences(...)"
      implementation: "XSD-driven property comparison with exclusion rules"
      
DATABASE_OBJECT_INTEGRATION:
  OBJECT_TYPE: "liquibase.structure.core.Database"
  ATTRIBUTE_HANDLING: "Use setAttribute() for Snowflake-specific properties"
  RELATIONSHIP_MANAGEMENT: "Parent for Schema objects"
```

## TEST_SCENARIO_MATRIX
```yaml
XSD_COMPLIANCE_TESTS:
  TEST_ALL_XSD_ATTRIBUTES_CAPTURED:
    description: "Verify snapshot captures all 18 XSD configuration attributes"
    setup: "Create database with all XSD attributes configured"
    execution: "Snapshot database and validate all attributes present"
    validation: "Assert all XSD attributes correctly mapped and converted"
    
  TEST_XSD_ATTRIBUTE_TYPE_CONVERSION:
    description: "Test XSD type conversion (boolean → YES/NO, int → NUMBER)"
    setup: "Create database with transient=true, dataRetentionTimeInDays=7"
    execution: "Snapshot and validate conversions"
    validation: "Assert IS_TRANSIENT='YES', RETENTION_TIME=7"
    
  TEST_XSD_COMPLIANCE_VALIDATION:
    description: "Automated XSD compliance validation framework"
    execution: "XSDComplianceTest.validateAllAttributesImplemented(DATABASE)"
    validation: "Assert no missing XSD attributes reported"
    
SNAPSHOT_FUNCTIONALITY_TESTS:
  TEST_SINGLE_DATABASE_SNAPSHOT:
    description: "Snapshot specific database with complete attributes"
    setup: "Known database with comment, retention time, transient flag"
    execution: "Snapshot single database"
    validation: "All XSD + state attributes captured correctly"
    
  TEST_BULK_DATABASE_DISCOVERY:
    description: "Discover all databases in Snowflake instance"
    execution: "Bulk snapshot all databases"
    validation: "All databases discovered with complete attributes"
    
  TEST_NONEXISTENT_DATABASE_HANDLING:
    description: "Handle snapshot of non-existent database"
    setup: "Request snapshot of 'NONEXISTENT_DB'"
    execution: "Attempt snapshot"
    validation: "Return null gracefully, no exceptions"
    
COMPARISON_LOGIC_TESTS:
  TEST_DATABASE_IDENTITY_COMPARISON:
    description: "Test database identity comparison (case-insensitive names)"
    setup: "Two databases: 'TESTDB' and 'testdb'"
    execution: "Compare database objects"
    validation: "Assert isSameObject returns true"
    
  TEST_XSD_ATTRIBUTE_DIFFERENCES:
    description: "Detect differences in XSD configuration attributes"
    setup: "Two databases with different comments, retention times"
    execution: "Compare databases"
    validation: "Assert differences detected for comment and retention time"
    
  TEST_STATE_ATTRIBUTE_EXCLUSIONS:
    description: "Verify state attributes excluded from structural comparison"
    setup: "Two databases with different owners, creation times"
    execution: "Compare databases"
    validation: "Assert no differences detected for excluded attributes"
    
DIFF_GENERATION_TESTS:
  TEST_MISSING_DATABASE_CHANGE:
    description: "Generate CREATE DATABASE change for missing database"
    setup: "Reference has database, target does not"
    execution: "Generate diff"
    validation: "CREATE DATABASE change with all XSD attributes"
    
  TEST_UNEXPECTED_DATABASE_CHANGE:
    description: "Generate DROP DATABASE change for unexpected database"
    setup: "Target has database, reference does not"
    execution: "Generate diff"
    validation: "DROP DATABASE change generated"
    
  TEST_CHANGED_DATABASE_PROPERTIES:
    description: "Generate ALTER DATABASE changes for property differences"
    setup: "Databases with different comments, retention times"
    execution: "Generate diff"
    validation: "ALTER DATABASE changes for modified properties"
    
EDGE_CASE_TESTS:
  TEST_NULL_VS_EMPTY_COMMENT_HANDLING:
    description: "Handle NULL vs empty string comment equivalence"
    setup: "One database with NULL comment, one with empty string"
    execution: "Compare databases"
    validation: "Assert no difference detected"
    
  TEST_TRANSIENT_DATABASE_CONSTRAINTS:
    description: "Handle transient database specific behavior"
    setup: "Transient database with different retention characteristics"
    execution: "Snapshot and compare"
    validation: "Validate transient-specific attribute handling"
    
  TEST_ICEBERG_DATABASE_ATTRIBUTES:
    description: "Handle Iceberg-specific database attributes"
    setup: "Iceberg database with external volume, catalog"
    execution: "Snapshot database"
    validation: "Iceberg-specific attributes captured via SHOW DATABASES"
    
INTEGRATION_TESTS:
  TEST_FRAMEWORK_REGISTRATION:
    description: "Verify snapshot generator properly registered"
    execution: "SnapshotGeneratorFactory.getInstance().getGenerators(Database.class, SnowflakeDatabase)"
    validation: "SnowflakeDatabaseSnapshotGenerator included with correct priority"
    
  TEST_TEST_HARNESS_COMPATIBILITY:
    description: "Verify compatibility with Liquibase test harness"
    execution: "Run test harness database snapshot tests"
    validation: "All tests pass with XSD compliance"
    
  TEST_PERFORMANCE_WITH_MANY_DATABASES:
    description: "Performance test with realistic database counts"
    setup: "Snowflake instance with 50+ databases"
    execution: "Bulk snapshot all databases"
    validation: "Complete snapshot under performance thresholds"
```

## IMPLEMENTATION_GUIDANCE
```yaml
TDD_IMPLEMENTATION_PLAN:
  PHASE_1_SNAPSHOT_GENERATOR:
    1. "Create SnowflakeDatabaseSnapshotGenerator unit tests"
    2. "Implement getPriority() method with SnowflakeDatabase priority"
    3. "Implement snapshotObject() with INFORMATION_SCHEMA.DATABASES query"
    4. "Add XSD attribute mapping with setAttribute() patterns"
    5. "Implement SHOW DATABASES supplementary query for additional attributes"
    
  PHASE_2_COMPARATOR:
    1. "Create SnowflakeDatabaseComparator unit tests"
    2. "Implement identity comparison with case-insensitive names"
    3. "Implement XSD attribute comparison with exclusion rules"
    4. "Add edge case handling (NULL vs empty strings)"
    5. "Test all diff scenarios (missing, unexpected, changed)"
    
  PHASE_3_INTEGRATION:
    1. "Register snapshot generator in META-INF/services"
    2. "Register comparator in META-INF/services"
    3. "Integration test with real Snowflake database"
    4. "Test harness validation"
    5. "Performance optimization and validation"
    
VALIDATION_CHECKPOINTS:
  AFTER_SNAPSHOT_GENERATOR:
    - [ ] "All XSD attributes captured in snapshot"
    - [ ] "INFORMATION_SCHEMA query returns complete data"
    - [ ] "SHOW DATABASES integration working"
    - [ ] "Attribute type conversions correct"
    
  AFTER_COMPARATOR:
    - [ ] "Database identity comparison working"
    - [ ] "XSD attribute differences detected"
    - [ ] "State attributes properly excluded"
    - [ ] "Edge cases handled correctly"
    
  AFTER_INTEGRATION:
    - [ ] "Service registration complete"
    - [ ] "Framework integration working"
    - [ ] "Test harness compatibility verified"
    - [ ] "Performance requirements met"
```

## QUALITY_VALIDATION
```yaml
REQUIREMENTS_COMPLETENESS:
  XSD_COMPLIANCE: "✅ All 18 XSD configuration attributes specified with mappings"
  STATE_ATTRIBUTES: "✅ All 10 operational metadata attributes documented"
  SQL_SPECIFICATIONS: "✅ Complete INFORMATION_SCHEMA and SHOW DATABASES queries"
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

This requirements document provides complete, implementation-ready specifications for Snowflake Database snapshot/diff functionality with 100% XSD compliance, comprehensive state attribute coverage, and detailed framework integration guidance. Ready for TDD implementation in Phase 3.