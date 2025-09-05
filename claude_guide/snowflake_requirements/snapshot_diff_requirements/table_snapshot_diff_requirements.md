# Snowflake Table Snapshot/Diff Requirements - IMPLEMENTATION_READY
## Phase 2: XSD-Driven Requirements Documentation

## REQUIREMENTS_METADATA
```yaml
REQUIREMENTS_VERSION: "3.0"
PHASE: "2_DOCUMENTATION_COMPLETE"
STATUS: "IMPLEMENTATION_READY"
RESEARCH_INPUT: "research_findings_snowflake_table_snapshot_diff.md"
IMPLEMENTATION_TYPE: "SNAPSHOT_DIFF"
NEXT_PHASE: "ai_workflow_guide.md"
OBJECT_TYPE: "TABLE"
DATABASE_TYPE: "SNOWFLAKE"
```

## XSD_ATTRIBUTES_SPECIFICATION
```yaml
XSD_COMPLIANCE_COVERAGE: "100% - All 22+ namespace configuration attributes implemented"
XSD_SOURCE_VALIDATION: "liquibase-snowflake-latest.xsd table namespace attributes verified"
NOTE: "Table attributes defined as namespace-prefixed attributes on standard createTable elements"

STANDARD_CREATETABLE_ATTRIBUTES:
  tableName:
    type: "standard createTable"
    required: true
    validation: "Non-null, non-empty string"
    mapping: "INFORMATION_SCHEMA.TABLES.TABLE_NAME"
    comparison: "ALWAYS_INCLUDE - Identity attribute"
    
  schemaName:
    type: "standard createTable"
    mapping: "INFORMATION_SCHEMA.TABLES.TABLE_SCHEMA"
    comparison: "ALWAYS_INCLUDE - Parent relationship"
    
  catalogName:
    type: "standard createTable"
    mapping: "INFORMATION_SCHEMA.TABLES.TABLE_CATALOG"
    comparison: "ALWAYS_INCLUDE - Database relationship"
    
  remarks:
    type: "standard createTable"
    mapping: "INFORMATION_SCHEMA.TABLES.COMMENT"
    comparison: "ALWAYS_INCLUDE - User metadata"
    equivalent_xsd: "comment attribute"

SNOWFLAKE_NAMESPACE_ATTRIBUTES:
  TABLE_TYPE_ATTRIBUTES:
    transient:
      type: "xsd:boolean"
      mapping: "INFORMATION_SCHEMA.TABLES.IS_TRANSIENT (YES/NO)"
      comparison: "ALWAYS_INCLUDE - Structural table type"
      conversion: "boolean → YES/NO"
      
    volatile:
      type: "xsd:boolean"
      mapping: "System-specific table type detection"
      comparison: "CONDITIONAL_INCLUDE - Rarely used table type"
      
    temporary:
      type: "xsd:boolean"
      mapping: "INFORMATION_SCHEMA.TABLES.IS_TEMPORARY (YES/NO)"
      comparison: "ALWAYS_INCLUDE - Structural table type"
      conversion: "boolean → YES/NO"
      
    localTemporary:
      type: "xsd:boolean"
      mapping: "INFORMATION_SCHEMA.TABLES.IS_TEMPORARY + scope detection"
      comparison: "ALWAYS_INCLUDE - Structural table type"
      
    globalTemporary:
      type: "xsd:boolean"
      mapping: "INFORMATION_SCHEMA.TABLES.IS_TEMPORARY + scope detection"
      comparison: "ALWAYS_INCLUDE - Structural table type"
      
  CLUSTERING_ATTRIBUTES:
    clusterBy:
      type: "xsd:string"
      mapping: "INFORMATION_SCHEMA.TABLES.CLUSTERING_KEY"
      comparison: "ALWAYS_INCLUDE - Performance configuration"
      format_normalization: "LINEAR(column) format"
      
    dropClusteringKey:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation flag"
      usage: "ALTER TABLE DROP CLUSTERING KEY"
      
    suspendRecluster:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation flag"
      usage: "ALTER TABLE SUSPEND RECLUSTER"
      
    resumeRecluster:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation flag"
      usage: "ALTER TABLE RESUME RECLUSTER"
      
  TIME_TRAVEL_ATTRIBUTES:
    dataRetentionTimeInDays:
      type: "xsd:int"
      mapping: "INFORMATION_SCHEMA.TABLES.RETENTION_TIME"
      comparison: "ALWAYS_INCLUDE - Business configuration"
      validation: "0-90 for permanent tables"
      
    maxDataExtensionTimeInDays:
      type: "xsd:int"
      mapping: "SHOW TABLES → MAX_DATA_EXTENSION_TIME_IN_DAYS"
      comparison: "CONDITIONAL_INCLUDE - Advanced Time Travel"
      
    setDataRetentionTimeInDays:
      type: "xsd:int"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation"
      usage: "ALTER TABLE SET DATA_RETENTION_TIME_IN_DAYS"
      
  FEATURE_ATTRIBUTES:
    changeTracking:
      type: "xsd:boolean"
      mapping: "SHOW TABLES.CHANGE_TRACKING (ON/OFF)"
      comparison: "ALWAYS_INCLUDE - Feature configuration"
      conversion: "boolean → ON/OFF"
      
    setChangeTracking:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation"
      usage: "ALTER TABLE SET CHANGE_TRACKING"
      
    enableSchemaEvolution:
      type: "xsd:boolean"
      mapping: "SHOW TABLES.ENABLE_SCHEMA_EVOLUTION (Y/N)"
      comparison: "ALWAYS_INCLUDE - Feature configuration"
      conversion: "boolean → Y/N"
      
    setEnableSchemaEvolution:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - ALTER TABLE operation"
      usage: "ALTER TABLE SET ENABLE_SCHEMA_EVOLUTION"
      
    copyGrants:
      type: "xsd:boolean"
      comparison: "EXCLUDE_FROM_DIFF - CREATE OR REPLACE specific"
      usage: "Only valid with OR REPLACE operations"
      
  COLLATION_ATTRIBUTES:
    defaultDdlCollation:
      type: "xsd:string"
      mapping: "SHOW TABLES → DEFAULT_DDL_COLLATION"
      comparison: "CONDITIONAL_INCLUDE - Character set configuration"
```

## STATE_ATTRIBUTES_SPECIFICATION
```yaml
STATE_COMPLIANCE_COVERAGE: "100% - All 30+ operational metadata attributes captured"
INFORMATION_SCHEMA_SOURCE: "INFORMATION_SCHEMA.TABLES"
SHOW_COMMAND_SOURCE: "SHOW TABLES"

IDENTITY_ATTRIBUTES:
  TABLE_CATALOG:
    source: "INFORMATION_SCHEMA.TABLES.TABLE_CATALOG"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Database relationship"
    validation: "Must match catalogName from createTable"
    
  TABLE_SCHEMA:
    source: "INFORMATION_SCHEMA.TABLES.TABLE_SCHEMA"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Schema relationship"
    validation: "Must match schemaName from createTable"
    
  TABLE_NAME:
    source: "INFORMATION_SCHEMA.TABLES.TABLE_NAME"
    type: "STRING"
    comparison: "IDENTITY_INCLUDE - Table identity"
    validation: "Must match tableName from createTable"
    
  TABLE_OWNER:
    source: "INFORMATION_SCHEMA.TABLES.TABLE_OWNER"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - May change due to ownership transfers"
    snapshot_usage: "Owner information for audit"
    
TABLE_TYPE_STATE:
  TABLE_TYPE:
    source: "INFORMATION_SCHEMA.TABLES.TABLE_TYPE"
    type: "STRING"
    comparison: "SNAPSHOT_ONLY - Object classification"
    values: ["BASE TABLE", "VIEW", "EXTERNAL TABLE"]
    
  IS_TRANSIENT:
    source: "INFORMATION_SCHEMA.TABLES.IS_TRANSIENT"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Validates XSD transient attribute"
    
  IS_TEMPORARY:
    source: "INFORMATION_SCHEMA.TABLES.IS_TEMPORARY"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Validates XSD temporary attributes"
    
  IS_ICEBERG:
    source: "INFORMATION_SCHEMA.TABLES.IS_ICEBERG"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Iceberg table type validation"
    
  IS_DYNAMIC:
    source: "INFORMATION_SCHEMA.TABLES.IS_DYNAMIC"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Dynamic table type validation"
    
  IS_HYBRID:
    source: "INFORMATION_SCHEMA.TABLES.IS_HYBRID"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Hybrid table type validation"
    
  IS_IMMUTABLE:
    source: "INFORMATION_SCHEMA.TABLES.IS_IMMUTABLE"
    type: "STRING (YES/NO)"
    comparison: "SNAPSHOT_ONLY - Immutable table type validation"

CLUSTERING_STATE:
  CLUSTERING_KEY:
    source: "INFORMATION_SCHEMA.TABLES.CLUSTERING_KEY"
    type: "STRING"
    comparison: "VALIDATION_COMPARE - Validates XSD clusterBy attribute"
    format: "LINEAR(column_list) or expression format"
    
  AUTO_CLUSTERING_ON:
    source: "INFORMATION_SCHEMA.TABLES.AUTO_CLUSTERING_ON"
    type: "STRING (YES/NO)"
    comparison: "EXCLUDE_FROM_DIFF - System-managed state"
    equivalent_show: "AUTOMATIC_CLUSTERING (ON/OFF)"

DATA_METRICS:
  ROW_COUNT:
    source: "INFORMATION_SCHEMA.TABLES.ROW_COUNT"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - Data-dependent, changes frequently"
    snapshot_usage: "Data statistics for audit"
    
  BYTES:
    source: "INFORMATION_SCHEMA.TABLES.BYTES"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - Data-dependent, changes frequently"
    snapshot_usage: "Storage metrics for audit"

TIME_TRAVEL_STATE:
  RETENTION_TIME:
    source: "INFORMATION_SCHEMA.TABLES.RETENTION_TIME"
    type: "NUMBER"
    comparison: "VALIDATION_COMPARE - Validate against XSD dataRetentionTimeInDays"

TIMESTAMPS:
  CREATED:
    source: "INFORMATION_SCHEMA.TABLES.CREATED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Immutable timestamp"
    snapshot_usage: "Creation audit trail"
    
  LAST_ALTERED:
    source: "INFORMATION_SCHEMA.TABLES.LAST_ALTERED"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - Changes with modifications"
    snapshot_usage: "Modification audit trail"
    
  LAST_DDL:
    source: "INFORMATION_SCHEMA.TABLES.LAST_DDL"
    type: "TIMESTAMP_NTZ"
    comparison: "EXCLUDE_FROM_DIFF - DDL operation timestamp"
    
  LAST_DDL_BY:
    source: "INFORMATION_SCHEMA.TABLES.LAST_DDL_BY"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - User tracking"

METADATA_ATTRIBUTES:
  COMMENT:
    source: "INFORMATION_SCHEMA.TABLES.COMMENT"
    type: "STRING"
    comparison: "VALIDATION_COMPARE - Matches standard createTable remarks"
    
  IS_INSERTABLE_INTO:
    source: "INFORMATION_SCHEMA.TABLES.IS_INSERTABLE_INTO"
    type: "STRING (YES/NO)"
    comparison: "EXCLUDE_FROM_DIFF - Permission-dependent"
    
  IS_TYPED:
    source: "INFORMATION_SCHEMA.TABLES.IS_TYPED"
    type: "STRING (YES/NO)"
    comparison: "EXCLUDE_FROM_DIFF - System property"

SHOW_TABLES_EXCLUSIVE_ATTRIBUTES:
  CHANGE_TRACKING:
    source: "SHOW TABLES.CHANGE_TRACKING"
    type: "STRING (ON/OFF)"
    comparison: "VALIDATION_COMPARE - Validates XSD changeTracking attribute"
    
  SEARCH_OPTIMIZATION:
    source: "SHOW TABLES.SEARCH_OPTIMIZATION"
    type: "STRING (ON/OFF)"
    comparison: "SNAPSHOT_ONLY - Feature state"
    
  SEARCH_OPTIMIZATION_PROGRESS:
    source: "SHOW TABLES.SEARCH_OPTIMIZATION_PROGRESS"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - System process state"
    
  SEARCH_OPTIMIZATION_BYTES:
    source: "SHOW TABLES.SEARCH_OPTIMIZATION_BYTES"
    type: "NUMBER"
    comparison: "EXCLUDE_FROM_DIFF - System metrics"
    
  IS_EXTERNAL:
    source: "SHOW TABLES.IS_EXTERNAL"
    type: "STRING (Y/N)"
    comparison: "SNAPSHOT_ONLY - External table type validation"
    
  ENABLE_SCHEMA_EVOLUTION:
    source: "SHOW TABLES.ENABLE_SCHEMA_EVOLUTION"
    type: "STRING (Y/N)"
    comparison: "VALIDATION_COMPARE - Validates XSD enableSchemaEvolution attribute"
    
  OWNER_ROLE_TYPE:
    source: "SHOW TABLES.OWNER_ROLE_TYPE"
    type: "STRING"
    comparison: "EXCLUDE_FROM_DIFF - Security state"
    
  IS_EVENT:
    source: "SHOW TABLES.IS_EVENT"
    type: "STRING (Y/N)"
    comparison: "SNAPSHOT_ONLY - Event table type validation"
```

## XSD_COMPLIANCE_VALIDATION_REQUIREMENTS
```yaml
AUTOMATED_XSD_COMPLIANCE_FRAMEWORK:
  XSD_NAMESPACE_ATTRIBUTE_EXTRACTION:
    - "Parse liquibase-snowflake-latest.xsd table namespace attributes"
    - "Extract all 22+ configuration attributes with types and constraints"
    - "Generate attribute specification map for snapshot generator"
    - "Validate XSD completeness against Snowflake table documentation"
    
  STANDARD_CREATETABLE_INTEGRATION:
    - "Verify integration with standard Liquibase createTable attributes"
    - "Validate namespace attribute precedence and conflicts"
    - "Test standard + namespace attribute combinations"
    - "Report conflicts between standard and namespace attributes"
    
  XSD_SNAPSHOT_VALIDATION:
    - "Verify snapshot generator captures ALL XSD namespace attributes"
    - "Validate XSD→INFORMATION_SCHEMA attribute mapping"
    - "Test XSD data type conversions (boolean → ON/OFF/YES/NO/Y/N)"
    - "Report missing or incorrectly implemented XSD attributes"
    
  XSD_COMPLIANCE_TESTING:
    - "Unit test: validateAllXSDAttributesImplemented(TABLE)"
    - "Integration test: createTableWithAllAttributes → snapshot → validate"
    - "Regression test: ensureXSDEvolutionCompatibility"
    - "Performance test: completeAttributeCapturePerformance"
```

## OBJECT_MODEL_SPECIFICATION
```yaml
TABLE_OBJECT_MODEL:
  LIQUIBASE_OBJECT_TYPE: "liquibase.structure.core.Table"
  SNOWFLAKE_EXTENSIONS: "Use setAttribute() for Snowflake-specific properties"
  
  CORE_PROPERTIES:
    name:
      source: "Standard createTable tableName"
      type: "String"
      required: true
      validation: "Non-null, non-empty"
      
    schema:
      source: "Standard createTable schemaName / INFORMATION_SCHEMA.TABLE_SCHEMA"
      type: "Schema"
      required: true
      validation: "Parent schema relationship"
      
    remarks:
      source: "Standard createTable remarks / INFORMATION_SCHEMA.COMMENT"
      type: "String"
      nullable: true
      
  SNOWFLAKE_SPECIFIC_ATTRIBUTES:
    isTransient:
      source: "XSD transient / INFORMATION_SCHEMA.IS_TRANSIENT"
      type: "Boolean"
      default: false
      
    isTemporary:
      source: "XSD temporary / INFORMATION_SCHEMA.IS_TEMPORARY"
      type: "Boolean"
      default: false
      
    isLocalTemporary:
      source: "XSD localTemporary"
      type: "Boolean"
      default: false
      
    isGlobalTemporary:
      source: "XSD globalTemporary"
      type: "Boolean"
      default: false
      
    isVolatile:
      source: "XSD volatile"
      type: "Boolean"
      default: false
      
    clusterBy:
      source: "XSD clusterBy / INFORMATION_SCHEMA.CLUSTERING_KEY"
      type: "String"
      format: "Comma-separated column list"
      
    dataRetentionTimeInDays:
      source: "XSD dataRetentionTimeInDays / INFORMATION_SCHEMA.RETENTION_TIME"
      type: "Integer"
      range: "0-90"
      
    maxDataExtensionTimeInDays:
      source: "XSD maxDataExtensionTimeInDays"
      type: "Integer"
      
    changeTracking:
      source: "XSD changeTracking / SHOW TABLES.CHANGE_TRACKING"
      type: "Boolean"
      conversion: "boolean ↔ ON/OFF"
      
    enableSchemaEvolution:
      source: "XSD enableSchemaEvolution / SHOW TABLES.ENABLE_SCHEMA_EVOLUTION"
      type: "Boolean"
      conversion: "boolean ↔ Y/N"
      
    defaultDdlCollation:
      source: "XSD defaultDdlCollation"
      type: "String"
      
    copyGrants:
      source: "XSD copyGrants"
      type: "Boolean"
      usage: "CREATE OR REPLACE operations only"
      
    tableType:
      source: "INFORMATION_SCHEMA.TABLE_TYPE"
      type: "String"
      readonly: true
      values: ["BASE TABLE", "VIEW", "EXTERNAL TABLE"]
      
    isIceberg:
      source: "INFORMATION_SCHEMA.IS_ICEBERG"
      type: "Boolean"
      readonly: true
      
    isDynamic:
      source: "INFORMATION_SCHEMA.IS_DYNAMIC"
      type: "Boolean"
      readonly: true
      
    isHybrid:
      source: "INFORMATION_SCHEMA.IS_HYBRID"
      type: "Boolean"
      readonly: true
      
    isImmutable:
      source: "INFORMATION_SCHEMA.IS_IMMUTABLE"
      type: "Boolean"
      readonly: true
      
    owner:
      source: "INFORMATION_SCHEMA.TABLE_OWNER"
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
      
    rowCount:
      source: "INFORMATION_SCHEMA.ROW_COUNT"
      type: "Long"
      readonly: true
      
    bytes:
      source: "INFORMATION_SCHEMA.BYTES"
      type: "Long"
      readonly: true
```

## SNAPSHOT_SQL_REQUIREMENTS
```yaml
PRIMARY_SNAPSHOT_QUERY:
  QUERY_TYPE: "INFORMATION_SCHEMA_OPTIMIZED"
  SQL: |
    SELECT 
      TABLE_CATALOG,
      TABLE_SCHEMA,
      TABLE_NAME,
      TABLE_OWNER,
      TABLE_TYPE,
      IS_TRANSIENT,
      CLUSTERING_KEY,
      ROW_COUNT,
      BYTES,
      RETENTION_TIME,
      CREATED,
      LAST_ALTERED,
      LAST_DDL,
      LAST_DDL_BY,
      AUTO_CLUSTERING_ON,
      COMMENT,
      IS_INSERTABLE_INTO,
      IS_TEMPORARY,
      IS_ICEBERG,
      IS_DYNAMIC,
      IS_IMMUTABLE,
      IS_HYBRID,
      IS_TYPED
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_CATALOG = ? AND TABLE_SCHEMA = ? AND TABLE_NAME = ?
    
  PARAMETERS: ["catalogName", "schemaName", "tableName"]
  PERFORMANCE: "Single table lookup - optimal"
  RESULT_PROCESSING: "Single row expected, null if not found"
  
SUPPLEMENTARY_SHOW_QUERY:
  QUERY_TYPE: "SHOW_COMMAND_METADATA"
  SQL: "SHOW TABLES LIKE ? IN SCHEMA ?"
  PARAMETERS: ["tableName", "schemaName"]
  ADDITIONAL_ATTRIBUTES:
    - "CHANGE_TRACKING"
    - "SEARCH_OPTIMIZATION"
    - "SEARCH_OPTIMIZATION_PROGRESS"
    - "SEARCH_OPTIMIZATION_BYTES"
    - "IS_EXTERNAL"
    - "ENABLE_SCHEMA_EVOLUTION"
    - "OWNER_ROLE_TYPE"
    - "IS_EVENT"
    - "AUTOMATIC_CLUSTERING"
  USE_CASE: "Feature-specific attributes not in INFORMATION_SCHEMA"
  
BULK_SNAPSHOT_QUERY:
  QUERY_TYPE: "SCHEMA_TABLE_DISCOVERY"
  SQL: |
    SELECT 
      TABLE_CATALOG,
      TABLE_SCHEMA,
      TABLE_NAME,
      TABLE_OWNER,
      TABLE_TYPE,
      IS_TRANSIENT,
      CLUSTERING_KEY,
      RETENTION_TIME,
      CREATED,
      LAST_ALTERED,
      COMMENT,
      IS_TEMPORARY,
      IS_ICEBERG,
      IS_DYNAMIC,
      IS_IMMUTABLE,
      IS_HYBRID
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_CATALOG = ? AND TABLE_SCHEMA = ?
    ORDER BY TABLE_NAME
    
  PARAMETERS: ["catalogName", "schemaName"]
  PERFORMANCE: "All tables in schema enumeration"
  RESULT_PROCESSING: "Multiple rows, process each as Table object"
  
SQL_QUERY_EXAMPLES:
  SINGLE_TABLE_SNAPSHOT:
    description: "Snapshot specific table with all attributes"
    sql: "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG = 'LTHDB' AND TABLE_SCHEMA = 'TESTHARNESS' AND TABLE_NAME = 'SNAPSHOT_TEST_TABLE'"
    expected_result: "Single table record with all XSD + state attributes"
    
  TRANSIENT_TABLE_HANDLING:
    description: "Handle transient table attribute conversion"
    sql: "SELECT IS_TRANSIENT, RETENTION_TIME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
    conversion: "YES → true, NO → false"
    validation: "Transient tables have different retention behavior"
    
  CLUSTERING_KEY_NORMALIZATION:
    description: "Handle clustering key format normalization"
    sql: "SELECT CLUSTERING_KEY FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
    format: "LINEAR(ID) or expression format → column list"
    
  CHANGE_TRACKING_FEATURE:
    description: "Handle change tracking feature via SHOW"
    sql: "SHOW TABLES LIKE ? IN SCHEMA ?"
    attributes: ["CHANGE_TRACKING"]
    conversion: "ON → true, OFF → false"
    
  TEMPORARY_TABLE_SCOPE:
    description: "Handle temporary table scope detection"
    sql: "SELECT IS_TEMPORARY FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
    scope_detection: "Additional logic needed for LOCAL vs GLOBAL"
    
  ICEBERG_TABLE_ATTRIBUTES:
    description: "Handle Iceberg table type validation"
    sql: "SELECT IS_ICEBERG, IS_EXTERNAL FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?"
    validation: "Iceberg tables have specific attribute patterns"
```

## COMPARISON_LOGIC_SPECIFICATION
```yaml
STRUCTURAL_COMPARISON_RULES:
  IDENTITY_COMPARISON:
    primary_key: ["catalogName", "schemaName", "tableName"]
    case_sensitivity: false
    rule: "Tables with different names or locations are different objects"
    
  XSD_CONFIGURATION_COMPARISON:
    remarks_comment:
      rule: "String comparison, NULL == empty string"
      type: "StringComparison"
      sources: ["createTable remarks", "XSD comment", "INFORMATION_SCHEMA.COMMENT"]
      
    transient:
      rule: "Boolean comparison with IS_TRANSIENT conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    temporary:
      rule: "Boolean comparison with IS_TEMPORARY conversion"
      type: "BooleanComparison"
      conversion: "boolean → YES/NO → boolean"
      
    localTemporary:
      rule: "Boolean comparison with scope detection"
      type: "ComplexBooleanComparison"
      scope_detection: "Requires additional logic for LOCAL vs GLOBAL"
      
    globalTemporary:
      rule: "Boolean comparison with scope detection"
      type: "ComplexBooleanComparison"
      scope_detection: "Requires additional logic for LOCAL vs GLOBAL"
      
    clusterBy:
      rule: "String comparison with format normalization"
      type: "NormalizedStringComparison"
      normalization: "LINEAR(column_list) → column_list format"
      
    dataRetentionTimeInDays:
      rule: "Numeric comparison with RETENTION_TIME"
      type: "NumericComparison"
      validation: "Range 0-90 for permanent tables"
      
    changeTracking:
      rule: "Boolean comparison with ON/OFF conversion"
      type: "BooleanComparison"
      conversion: "boolean → ON/OFF → boolean"
      
    enableSchemaEvolution:
      rule: "Boolean comparison with Y/N conversion"
      type: "BooleanComparison"
      conversion: "boolean → Y/N → boolean"
      
    defaultDdlCollation:
      rule: "String comparison, case-sensitive"
      type: "StringComparison"
      
EXCLUSION_RULES:
  ALWAYS_EXCLUDE:
    - "TABLE_OWNER"          # Ownership management
    - "ROW_COUNT"            # Data-dependent metrics
    - "BYTES"                # Data-dependent metrics
    - "CREATED"              # Immutable timestamp
    - "LAST_ALTERED"         # Modification timestamp
    - "LAST_DDL"             # DDL timestamp
    - "LAST_DDL_BY"          # User tracking
    - "IS_INSERTABLE_INTO"   # Permission-dependent
    - "IS_TYPED"             # System property
    - "AUTO_CLUSTERING_ON"   # System-managed state
    - "SEARCH_OPTIMIZATION_PROGRESS"  # System process state
    - "SEARCH_OPTIMIZATION_BYTES"     # System metrics
    - "OWNER_ROLE_TYPE"      # Security state
    
  OPERATION_ONLY_EXCLUDE:
    - "dropClusteringKey"    # ALTER TABLE operation flag
    - "suspendRecluster"     # ALTER TABLE operation flag
    - "resumeRecluster"      # ALTER TABLE operation flag
    - "setDataRetentionTimeInDays"    # ALTER TABLE operation
    - "setChangeTracking"    # ALTER TABLE operation
    - "setEnableSchemaEvolution"      # ALTER TABLE operation
    - "copyGrants"           # CREATE OR REPLACE specific
    
DIFF_SCENARIO_MATRIX:
  MISSING_TABLE:
    condition: "Table exists in reference but not in target"
    action: "Generate CREATE TABLE change"
    attributes: "Include all configured XSD namespace attributes"
    columns: "Include column definitions from snapshot"
    
  UNEXPECTED_TABLE:
    condition: "Table exists in target but not in reference"
    action: "Generate DROP TABLE change"
    safety: "Include CASCADE/RESTRICT handling"
    
  CHANGED_TABLE:
    condition: "Table exists in both but properties differ"
    action: "Generate ALTER TABLE change"
    supported_changes:
      - "remarks/comment → COMMENT ON TABLE"
      - "clusterBy → ALTER TABLE CLUSTER BY"
      - "dataRetentionTimeInDays → ALTER TABLE SET DATA_RETENTION_TIME_IN_DAYS"
      - "changeTracking → ALTER TABLE SET CHANGE_TRACKING"
      - "enableSchemaEvolution → ALTER TABLE SET ENABLE_SCHEMA_EVOLUTION"
      - "Column-level changes handled by separate generators"
      
EDGE_CASE_HANDLING:
  NULL_VS_EMPTY_COMMENTS:
    rule: "Treat NULL and empty string as equivalent"
    implementation: "StringUtils.isEmpty() for comparison"
    
  CLUSTERING_KEY_FORMAT_VARIATIONS:
    rule: "Normalize clustering key format for comparison"
    formats: ["LINEAR(column_list)", "expression format", "column_list"]
    normalization: "Convert to canonical column list format"
    
  TEMPORARY_TABLE_SCOPE_DETECTION:
    rule: "Distinguish between LOCAL and GLOBAL temporary tables"
    detection: "Analyze table metadata and session scope"
    
  INHERITED_VS_EXPLICIT_RETENTION:
    rule: "Compare configured value with actual RETENTION_TIME"
    validation: "Account for schema/database inheritance"
    
  TABLE_TYPE_COMBINATIONS:
    rule: "Handle mutually exclusive table type flags"
    validation: "Only one of transient/temporary/volatile should be true"
    
  ICEBERG_TABLE_CONSTRAINTS:
    rule: "Iceberg tables have specific attribute constraints"
    validation: "Validate Iceberg-specific attribute combinations"
```

## FRAMEWORK_INTEGRATION_REQUIREMENTS
```yaml
LIQUIBASE_SNAPSHOT_GENERATOR:
  CLASS_NAME: "SnowflakeTableSnapshotGenerator"
  BASE_CLASS: "liquibase.snapshot.jvm.JdbcSnapshotGenerator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  COORDINATION: "Must coordinate with existing TableSnapshotGenerator"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Table.class + SnowflakeDatabase"
      coordination: "Higher priority than standard TableSnapshotGenerator"
      
    addsTo:
      signature: "Class<? extends DatabaseObject>[] addsTo()"
      implementation: "Return new Class[] { Schema.class }"
      
    snapshotObject:
      signature: "DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot)"
      implementation: "Single table snapshot with XSD compliance"
      integration: "Coordinate with column and constraint snapshots"
      
    addTo:
      signature: "void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot)"
      implementation: "Bulk table discovery within schema"
      performance: "Efficient batch processing for many tables"
      
SERVICE_REGISTRATION:
  FILE: "META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "liquibase.ext.snowflake.snapshot.SnowflakeTableSnapshotGenerator"
  
LIQUIBASE_COMPARATOR:
  CLASS_NAME: "SnowflakeTableComparator"
  BASE_CLASS: "liquibase.diff.compare.DatabaseObjectComparator"
  PRIORITY: "PRIORITY_DATABASE for SnowflakeDatabase instances"
  COORDINATION: "Must coordinate with existing TableComparator"
  
  REQUIRED_METHODS:
    getPriority:
      signature: "int getPriority(Class<? extends DatabaseObject> objectType, Database database)"
      implementation: "Return PRIORITY_DATABASE for Table.class + SnowflakeDatabase"
      coordination: "Higher priority than standard TableComparator"
      
    hash:
      signature: "String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Return [catalogName, schemaName, tableName] as hash key"
      
    isSameObject:
      signature: "boolean isSameObject(DatabaseObject obj1, DatabaseObject obj2, Database accordingTo, DatabaseObjectComparatorChain chain)"
      implementation: "Compare catalog, schema, and table names (case-insensitive)"
      
    findDifferences:
      signature: "ObjectDifferences findDifferences(...)"
      implementation: "XSD-driven property comparison with exclusion rules"
      coordination: "Chain to standard comparator for non-Snowflake properties"
      
TABLE_OBJECT_INTEGRATION:
  OBJECT_TYPE: "liquibase.structure.core.Table"
  ATTRIBUTE_HANDLING: "Use setAttribute() for Snowflake-specific properties"
  RELATIONSHIP_MANAGEMENT: "Child of Schema, parent of Columns/Constraints/Indexes"
  COLUMN_INTEGRATION: "Coordinate with ColumnSnapshotGenerator for complete table definition"
  CONSTRAINT_INTEGRATION: "Coordinate with constraint snapshot generators"
```

## TEST_SCENARIO_MATRIX
```yaml
XSD_COMPLIANCE_TESTS:
  TEST_ALL_XSD_NAMESPACE_ATTRIBUTES_CAPTURED:
    description: "Verify snapshot captures all 22+ XSD namespace attributes"
    setup: "Create table with all XSD namespace attributes configured"
    execution: "Snapshot table and validate all attributes present"
    validation: "Assert all XSD namespace attributes correctly mapped and converted"
    
  TEST_STANDARD_CREATETABLE_INTEGRATION:
    description: "Test integration with standard createTable attributes"
    setup: "Create table with standard + namespace attributes"
    execution: "Snapshot table and validate attribute precedence"
    validation: "Assert no conflicts between standard and namespace attributes"
    
  TEST_XSD_ATTRIBUTE_TYPE_CONVERSION:
    description: "Test XSD type conversions (boolean → ON/OFF/YES/NO/Y/N)"
    setup: "Create table with transient=true, changeTracking=true, enableSchemaEvolution=true"
    execution: "Snapshot and validate conversions"
    validation: "Assert IS_TRANSIENT='YES', CHANGE_TRACKING='ON', ENABLE_SCHEMA_EVOLUTION='Y'"
    
  TEST_XSD_COMPLIANCE_VALIDATION:
    description: "Automated XSD compliance validation framework"
    execution: "XSDComplianceTest.validateAllAttributesImplemented(TABLE)"
    validation: "Assert no missing XSD attributes reported"
    
SNAPSHOT_FUNCTIONALITY_TESTS:
  TEST_SINGLE_TABLE_SNAPSHOT:
    description: "Snapshot specific table with complete attributes"
    setup: "Known table with clustering, retention time, change tracking"
    execution: "Snapshot single table"
    validation: "All XSD + state attributes captured correctly"
    
  TEST_BULK_TABLE_DISCOVERY:
    description: "Discover all tables in schema"
    execution: "Bulk snapshot all tables in TESTHARNESS schema"
    validation: "All tables discovered with complete attributes"
    
  TEST_TABLE_WITH_COLUMNS_INTEGRATION:
    description: "Snapshot table with column definitions"
    setup: "Table with various column types and constraints"
    execution: "Snapshot table"
    validation: "Table attributes + column definitions captured"
    
  TEST_NONEXISTENT_TABLE_HANDLING:
    description: "Handle snapshot of non-existent table"
    setup: "Request snapshot of 'NONEXISTENT_TABLE'"
    execution: "Attempt snapshot"
    validation: "Return null gracefully, no exceptions"
    
COMPARISON_LOGIC_TESTS:
  TEST_TABLE_IDENTITY_COMPARISON:
    description: "Test table identity comparison (case-insensitive names)"
    setup: "Two tables: 'LTHDB.TESTHARNESS.TESTTABLE' and 'lthdb.testharness.testtable'"
    execution: "Compare table objects"
    validation: "Assert isSameObject returns true"
    
  TEST_XSD_ATTRIBUTE_DIFFERENCES:
    description: "Detect differences in XSD configuration attributes"
    setup: "Two tables with different clustering, change tracking settings"
    execution: "Compare tables"
    validation: "Assert differences detected for clustering and change tracking"
    
  TEST_STATE_ATTRIBUTE_EXCLUSIONS:
    description: "Verify state attributes excluded from structural comparison"
    setup: "Two tables with different row counts, owners, timestamps"
    execution: "Compare tables"
    validation: "Assert no differences detected for excluded attributes"
    
  TEST_CLUSTERING_KEY_NORMALIZATION:
    description: "Test clustering key format normalization in comparison"
    setup: "Two tables with 'LINEAR(ID)' vs 'ID' clustering"
    execution: "Compare tables"
    validation: "Assert no difference detected after normalization"
    
DIFF_GENERATION_TESTS:
  TEST_MISSING_TABLE_CHANGE:
    description: "Generate CREATE TABLE change for missing table"
    setup: "Reference has table, target does not"
    execution: "Generate diff"
    validation: "CREATE TABLE change with all XSD attributes and columns"
    
  TEST_UNEXPECTED_TABLE_CHANGE:
    description: "Generate DROP TABLE change for unexpected table"
    setup: "Target has table, reference does not"
    execution: "Generate diff"
    validation: "DROP TABLE change generated"
    
  TEST_CHANGED_TABLE_PROPERTIES:
    description: "Generate ALTER TABLE changes for property differences"
    setup: "Tables with different comments, clustering, change tracking"
    execution: "Generate diff"
    validation: "ALTER TABLE changes for modified properties"
    
EDGE_CASE_TESTS:
  TEST_NULL_VS_EMPTY_COMMENT_HANDLING:
    description: "Handle NULL vs empty string comment equivalence"
    setup: "One table with NULL comment, one with empty string"
    execution: "Compare tables"
    validation: "Assert no difference detected"
    
  TEST_TEMPORARY_TABLE_SCOPE_DETECTION:
    description: "Handle temporary table scope detection"
    setup: "Local and global temporary tables"
    execution: "Snapshot and compare"
    validation: "Validate temporary table scope handling"
    
  TEST_TRANSIENT_TABLE_CONSTRAINTS:
    description: "Handle transient table specific behavior"
    setup: "Transient table with different retention characteristics"
    execution: "Snapshot and compare"
    validation: "Validate transient-specific attribute handling"
    
  TEST_ICEBERG_TABLE_ATTRIBUTES:
    description: "Handle Iceberg table type validation"
    setup: "Iceberg table with specific attribute patterns"
    execution: "Snapshot table"
    validation: "Iceberg-specific attributes captured and validated"
    
  TEST_TABLE_TYPE_MUTUAL_EXCLUSION:
    description: "Handle mutually exclusive table type flags"
    setup: "Tables with different type flags (transient, temporary, etc.)"
    execution: "Snapshot and validate"
    validation: "Only one table type flag should be true"
    
INTEGRATION_TESTS:
  TEST_FRAMEWORK_REGISTRATION:
    description: "Verify snapshot generator properly registered"
    execution: "SnapshotGeneratorFactory.getInstance().getGenerators(Table.class, SnowflakeDatabase)"
    validation: "SnowflakeTableSnapshotGenerator included with correct priority"
    
  TEST_SCHEMA_RELATIONSHIP_MAINTENANCE:
    description: "Verify schema/table relationship properly maintained"
    execution: "Snapshot table and validate schema relationship"
    validation: "Table.getSchema() returns correct Schema object"
    
  TEST_COLUMN_INTEGRATION:
    description: "Verify integration with column snapshot generators"
    execution: "Snapshot table with columns"
    validation: "Table attributes + column definitions both captured"
    
  TEST_CONSTRAINT_INTEGRATION:
    description: "Verify integration with constraint snapshot generators"
    execution: "Snapshot table with constraints"
    validation: "Table attributes + constraints both captured"
    
  TEST_TEST_HARNESS_COMPATIBILITY:
    description: "Verify compatibility with Liquibase test harness"
    execution: "Run test harness table snapshot tests"
    validation: "All tests pass with XSD compliance"
    
  TEST_PERFORMANCE_WITH_MANY_TABLES:
    description: "Performance test with realistic table counts"
    setup: "Schema with 500+ tables"
    execution: "Bulk snapshot all tables"
    validation: "Complete snapshot under performance thresholds"
```

## IMPLEMENTATION_GUIDANCE
```yaml
TDD_IMPLEMENTATION_PLAN:
  PHASE_1_SNAPSHOT_GENERATOR:
    1. "Create SnowflakeTableSnapshotGenerator unit tests"
    2. "Implement getPriority() method with SnowflakeDatabase priority"
    3. "Implement snapshotObject() with INFORMATION_SCHEMA.TABLES query"
    4. "Add XSD namespace attribute mapping with setAttribute() patterns"
    5. "Implement SHOW TABLES supplementary query for additional attributes"
    6. "Handle schema/table relationship properly"
    7. "Coordinate with column and constraint snapshot generators"
    
  PHASE_2_COMPARATOR:
    1. "Create SnowflakeTableComparator unit tests"
    2. "Implement identity comparison with case-insensitive names"
    3. "Implement XSD attribute comparison with exclusion rules"
    4. "Add complex attribute handling (clustering key normalization)"
    5. "Add temporary table scope detection logic"
    6. "Add edge case handling (NULL vs empty strings, format normalization)"
    7. "Test all diff scenarios (missing, unexpected, changed)"
    
  PHASE_3_INTEGRATION:
    1. "Register snapshot generator in META-INF/services"
    2. "Register comparator in META-INF/services"
    3. "Integration test with real Snowflake database"
    4. "Test schema/table relationship maintenance"
    5. "Test column and constraint integration"
    6. "Test harness validation"
    7. "Performance optimization and validation"
    
VALIDATION_CHECKPOINTS:
  AFTER_SNAPSHOT_GENERATOR:
    - [ ] "All XSD namespace attributes captured in snapshot"
    - [ ] "INFORMATION_SCHEMA query returns complete data"
    - [ ] "SHOW TABLES integration working"
    - [ ] "Attribute type conversions correct"
    - [ ] "Schema/table relationship maintained"
    - [ ] "Column integration working"
    
  AFTER_COMPARATOR:
    - [ ] "Table identity comparison working"
    - [ ] "XSD attribute differences detected"
    - [ ] "State attributes properly excluded"
    - [ ] "Clustering key normalization correct"
    - [ ] "Temporary table handling correct"
    - [ ] "Edge cases handled correctly"
    
  AFTER_INTEGRATION:
    - [ ] "Service registration complete"
    - [ ] "Framework integration working"
    - [ ] "Schema relationship working"
    - [ ] "Column integration working"
    - [ ] "Constraint integration working"
    - [ ] "Test harness compatibility verified"
    - [ ] "Performance requirements met"
```

## QUALITY_VALIDATION
```yaml
REQUIREMENTS_COMPLETENESS:
  XSD_COMPLIANCE: "✅ All 22+ XSD namespace configuration attributes specified with mappings"
  STATE_ATTRIBUTES: "✅ All 30+ operational metadata attributes documented"
  SQL_SPECIFICATIONS: "✅ Complete INFORMATION_SCHEMA and SHOW TABLES queries"
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

This requirements document provides complete, implementation-ready specifications for Snowflake Table snapshot/diff functionality with 100% XSD compliance, comprehensive state attribute coverage, and detailed framework integration guidance. Ready for TDD implementation in Phase 3.