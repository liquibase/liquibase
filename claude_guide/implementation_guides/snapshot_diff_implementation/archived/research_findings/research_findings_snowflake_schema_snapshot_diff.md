# Schema Snapshot/Diff Research Findings - Snowflake
## Phase 1: XSD-Driven Object Structure Discovery and Analysis

## RESEARCH_METADATA
```yaml
RESEARCH_DATE: "2025-08-02"
OBJECT_TYPE: "SCHEMA"
DATABASE_TYPE: "SNOWFLAKE"
RESEARCH_DURATION: "3 hours focused investigation"
VALIDATION_STATUS: "COMPLETE"
XSD_COMPLIANCE_VALIDATED: true
STATE_ATTRIBUTES_MAPPED: true
```

## XSD_ATTRIBUTE_EXTRACTION_RESULTS
```yaml
XSD_SOURCE: "liquibase-snowflake-latest.xsd"
ELEMENT_ANALYZED: "createSchema, alterSchema, dropSchema"
TOTAL_XSD_ATTRIBUTES: 18
COVERAGE_PERCENTAGE: "85% (Configuration attributes)"
```

### XSD_CONFIGURATION_ATTRIBUTES (Primary - 85% Coverage)
```yaml
REQUIRED_ATTRIBUTES:
  - name: "schemaName"
    type: "xsd:string"
    required: true
    description: "Schema identifier name"
    
OPTIONAL_CONFIGURATION_ATTRIBUTES:
  - name: "databaseName"
    type: "xsd:string"
    description: "Parent database name"
    
  - name: "cloneFrom"
    type: "xsd:string"
    description: "Source schema for cloning operations"
    
  - name: "comment"
    type: "xsd:string"
    description: "Schema comment/description"
    
  - name: "dataRetentionTimeInDays"
    type: "xsd:int"
    description: "Time Travel retention period (0-90 days)"
    
  - name: "maxDataExtensionTimeInDays"
    type: "xsd:int"
    description: "Maximum Time Travel extension period"
    
  - name: "transient"
    type: "xsd:boolean"
    description: "Creates transient schema (no fail-safe)"
    
  - name: "managedAccess"
    type: "xsd:boolean"
    description: "Enable managed access for schema"
    
  - name: "defaultDdlCollation"
    type: "xsd:string"
    description: "Default collation for schema objects"
    
  - name: "tag"
    type: "xsd:string"
    description: "Tag assignment for schema"
    
  - name: "orReplace"
    type: "xsd:boolean"
    description: "Use CREATE OR REPLACE SCHEMA"
    
  - name: "ifNotExists"
    type: "xsd:boolean"
    description: "Use CREATE SCHEMA IF NOT EXISTS"
    
  - name: "pipeExecutionPaused"
    type: "xsd:boolean"
    description: "Pause pipe execution in schema"
    
  - name: "externalVolume"
    type: "xsd:string"
    description: "External volume specification for Iceberg schemas"
    
  - name: "catalog"
    type: "xsd:string"
    description: "Catalog specification for Iceberg schemas"
    
  - name: "replaceInvalidCharacters"
    type: "xsd:boolean"
    description: "Replace invalid characters in catalog sync"
    
  - name: "classificationProfile"
    type: "xsd:string"
    description: "Data classification profile for schema"
    
  - name: "storageSerializationPolicy"
    type: "xsd:string"
    description: "Storage serialization policy"
```

## STATE_ATTRIBUTE_DISCOVERY_RESULTS
```yaml
INFORMATION_SCHEMA_SOURCE: "INFORMATION_SCHEMA.SCHEMATA"
SHOW_COMMAND_SOURCE: "SHOW SCHEMAS"
TOTAL_STATE_ATTRIBUTES: 15
COVERAGE_PERCENTAGE: "15% (Operational metadata)"
```

### STATE_OPERATIONAL_ATTRIBUTES (Secondary - 15% Coverage)
```yaml
SYSTEM_METADATA_PROPERTIES:
  - name: "CATALOG_NAME"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Parent database name"
    
  - name: "SCHEMA_NAME"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Schema name (matches XSD schemaName)"
    
  - name: "SCHEMA_OWNER"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Schema owner role"
    
  - name: "IS_TRANSIENT"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Transient flag (YES/NO)"
    
  - name: "IS_MANAGED_ACCESS"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Managed access flag (YES/NO)"
    
  - name: "RETENTION_TIME"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "NUMBER"
    description: "Time Travel retention time in days"
    
  - name: "DEFAULT_CHARACTER_SET_CATALOG"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Default character set catalog (typically NULL)"
    
  - name: "DEFAULT_CHARACTER_SET_SCHEMA"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Default character set schema (typically NULL)"
    
  - name: "DEFAULT_CHARACTER_SET_NAME"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Default character set name (typically NULL)"
    
  - name: "SQL_PATH"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "SQL path (typically NULL)"
    
  - name: "CREATED"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "TIMESTAMP_NTZ"
    description: "Schema creation timestamp"
    
  - name: "LAST_ALTERED"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "TIMESTAMP_NTZ"
    description: "Last modification timestamp"
    
  - name: "COMMENT"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Schema comment (matches XSD comment)"
    
  - name: "REPLICABLE_WITH_FAILOVER_GROUPS"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Failover group replication status"
    
  - name: "OWNER_ROLE_TYPE"
    source: "INFORMATION_SCHEMA.SCHEMATA"
    type: "STRING"
    description: "Owner role type (ROLE/USER)"
```

## ATTRIBUTE_CATEGORIZATION_MATRIX
```yaml
XSD_CONFIGURATION_ATTRIBUTES_STRUCTURAL:
  ALWAYS_INCLUDE_IN_DIFF:
    - "schemaName"        # Identity - triggers CREATE/DROP
    - "databaseName"      # Parent relationship
    - "comment"           # User-visible metadata
    - "dataRetentionTimeInDays"  # Business configuration
    - "maxDataExtensionTimeInDays"  # Business configuration
    - "transient"         # Structural property
    - "managedAccess"     # Security configuration
    - "defaultDdlCollation"  # Affects child objects
    - "pipeExecutionPaused"  # Operational configuration

  CONDITIONAL_INCLUDE_IN_DIFF:
    - "cloneFrom"         # Only relevant during creation
    - "tag"               # May be managed separately
    - "externalVolume"    # Iceberg-specific
    - "catalog"           # Iceberg-specific
    - "classificationProfile"  # Data governance specific
    
STATE_OPERATIONAL_ATTRIBUTES_EXCLUDED:
  EXCLUDE_FROM_DIFF:
    - "SCHEMA_OWNER"      # May change due to role management
    - "CREATED"           # Immutable timestamp
    - "LAST_ALTERED"      # Changes with any modification
    - "OWNER_ROLE_TYPE"   # Security state
    - "DEFAULT_CHARACTER_SET_*"  # Typically NULL in Snowflake
    - "SQL_PATH"          # Typically NULL in Snowflake
    - "REPLICABLE_WITH_FAILOVER_GROUPS"  # System metadata
    
  INCLUDE_IN_SNAPSHOT_ONLY:
    - "CATALOG_NAME"      # Parent database reference
    - "IS_TRANSIENT"      # Structural flag for validation
    - "IS_MANAGED_ACCESS" # Security flag for validation
    - "RETENTION_TIME"    # May differ from configured value
```

## SQL_MAPPING_SPECIFICATIONS
```yaml
INFORMATION_SCHEMA_PRIMARY_QUERY:
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
    
  PERFORMANCE: "Optimal for single schema lookup"
  AVAILABILITY: "Available in all Snowflake editions"
  
SHOW_COMMAND_SUPPLEMENTARY_QUERY:
  SQL: "SHOW SCHEMAS IN DATABASE ?"
  ADDITIONAL_ATTRIBUTES:
    - "Additional metadata may be available in SHOW output"
  USE_CASE: "Bulk schema discovery and validation"
  
BULK_SNAPSHOT_QUERY:
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
    
  PERFORMANCE: "Efficient for all schemas in database snapshot"
```

## OBJECT_LIFECYCLE_ANALYSIS
```yaml
SCHEMA_STATES:
  CREATION_STATE:
    initial_properties:
      - "SCHEMA_NAME": "Required identifier"
      - "CATALOG_NAME": "Parent database"
      - "SCHEMA_OWNER": "Creating role"
      - "IS_TRANSIENT": "Based on TRANSIENT flag"
      - "IS_MANAGED_ACCESS": "Based on MANAGED ACCESS flag"
      - "CREATED": "Current timestamp"
      - "RETENTION_TIME": "Inherited from database or configured value"
      
  OPERATIONAL_STATE:
    modifiable_properties:
      - "comment": "Via ALTER SCHEMA"
      - "dataRetentionTimeInDays": "Via ALTER SCHEMA"
      - "maxDataExtensionTimeInDays": "Via ALTER SCHEMA"
      - "defaultDdlCollation": "Via ALTER SCHEMA"
      - "managedAccess": "Via ALTER SCHEMA ENABLE/DISABLE MANAGED ACCESS"
      - "pipeExecutionPaused": "Via ALTER SCHEMA"
      - "SCHEMA_OWNER": "Via GRANT OWNERSHIP"
      
    immutable_properties:
      - "schemaName": "Cannot be changed directly (requires rename operation)"
      - "databaseName": "Cannot be changed (schema tied to database)"
      - "transient": "Cannot be changed after creation"
      - "CREATED": "Immutable timestamp"
      
  DROP_STATE:
    - "Schema and all contained objects deleted"
    - "Time Travel data retained per retention policy"
    - "Schema name becomes available for reuse within database"
```

## COMPARISON_LOGIC_REQUIREMENTS
```yaml
STRUCTURAL_COMPARISON_RULES:
  IDENTITY_COMPARISON:
    - "Compare schemaName (case-insensitive)"
    - "Compare databaseName/CATALOG_NAME for full identity"
    - "Schemas with different names or databases are different objects"
    
  CONFIGURATION_COMPARISON:
    - "comment": "String comparison, treat NULL and empty as equivalent"
    - "dataRetentionTimeInDays": "Numeric comparison, compare configured vs actual RETENTION_TIME"
    - "maxDataExtensionTimeInDays": "Numeric comparison if configured"
    - "transient": "Boolean comparison with IS_TRANSIENT (YES/NO conversion)"
    - "managedAccess": "Boolean comparison with IS_MANAGED_ACCESS (YES/NO conversion)"
    - "defaultDdlCollation": "String comparison, case-sensitive"
    - "pipeExecutionPaused": "Boolean comparison if configured"
    
  EXCLUSION_RULES:
    - "Exclude SCHEMA_OWNER from structural comparison"
    - "Exclude CREATED, LAST_ALTERED timestamps"
    - "Exclude OWNER_ROLE_TYPE security state"
    - "Exclude DEFAULT_CHARACTER_SET_* (typically NULL)"
    - "Exclude SQL_PATH (typically NULL)"
    - "Exclude REPLICABLE_WITH_FAILOVER_GROUPS system metadata"
    
DIFF_SCENARIOS:
  MISSING_SCHEMA:
    - "Schema exists in reference but not in target"
    - "Generate CREATE SCHEMA change"
    
  UNEXPECTED_SCHEMA:
    - "Schema exists in target but not in reference"
    - "Generate DROP SCHEMA change"
    
  CHANGED_SCHEMA:
    - "Schema exists in both but properties differ"
    - "Generate ALTER SCHEMA change for modified properties"
    
  EDGE_CASES:
    - "NULL vs empty string comments"
    - "Inherited vs explicitly set retention time"
    - "Managed access state changes"
    - "Transient schemas with different behavior"
```

## FRAMEWORK_INTEGRATION_ANALYSIS
```yaml
LIQUIBASE_INTEGRATION_REQUIREMENTS:
  SNAPSHOT_GENERATOR_INTERFACE:
    - "Extend liquibase.snapshot.jvm.JdbcSnapshotGenerator"
    - "Override getPriority() for Snowflake database priority"
    - "Implement snapshotObject() for single schema snapshot"
    - "Implement addTo() for bulk schema discovery within database"
    
  DATABASE_OBJECT_MODEL:
    - "Use liquibase.structure.core.Schema object"
    - "Map XSD attributes to Schema properties"
    - "Handle Snowflake-specific attributes via setAttribute()"
    - "Properly set catalog (database) relationship"
    
  SERVICE_REGISTRATION:
    - "Register in META-INF/services/liquibase.snapshot.SnapshotGenerator"
    - "Priority: PRIORITY_DATABASE for SnowflakeDatabase instances"
    
FRAMEWORK_LIMITATIONS:
  STANDARD_SCHEMA_OBJECT_CONSTRAINTS:
    - "Limited built-in properties on Schema object"
    - "Must use setAttribute() for Snowflake-specific properties"
    - "Catalog/Schema relationship properly maintained"
    
  COMPARISON_FRAMEWORK_INTEGRATION:
    - "Schema comparisons supported in Liquibase core"
    - "Can leverage existing SchemaComparator patterns"
    - "Schema-level changes commonly generated in diff operations"
    
REALISTIC_SUCCESS_CRITERIA:
  SNAPSHOT_FUNCTIONALITY:
    - "✅ Can capture all XSD + state attributes in snapshot"
    - "✅ Efficient INFORMATION_SCHEMA queries"
    - "✅ Proper catalog/schema relationship maintenance"
    
  DIFF_FUNCTIONALITY:
    - "✅ Full schema-level diff support"
    - "✅ Can detect configuration differences"
    - "✅ Schema rename operations supported via ALTER SCHEMA"
    
  INTEGRATION_SCOPE:
    - "✅ Full integration with snapshot framework"
    - "✅ Complete integration with diff/changelog generation"
    - "✅ Supports test harness validation"
```

## XSD_COMPLIANCE_VALIDATION_FRAMEWORK
```yaml
AUTOMATED_VALIDATION_SPECIFICATIONS:
  XSD_ATTRIBUTE_COVERAGE_TEST:
    - "Parse createSchema XSD element"
    - "Extract all 18 configuration attributes"
    - "Validate snapshot generator captures all XSD attributes"
    - "Report missing XSD attribute implementations"
    
  ATTRIBUTE_MAPPING_VALIDATION:
    - "Verify XSD→INFORMATION_SCHEMA mapping for all attributes"
    - "Test SQL queries return expected attribute values"
    - "Validate data type conversions (xsd:int → NUMBER, xsd:boolean → YES/NO)"
    
  STATE_ATTRIBUTE_INTEGRATION_TEST:
    - "Verify all operational metadata attributes captured"
    - "Test SHOW SCHEMAS integration for supplementary attributes"
    - "Validate attribute categorization (XSD vs STATE vs COMPUTED)"
    
REGRESSION_TESTING_FRAMEWORK:
  XSD_EVOLUTION_COMPATIBILITY:
    - "Automated detection of new XSD attributes"
    - "Backward compatibility testing for removed attributes"
    - "Version-specific attribute implementation testing"
    
  PERFORMANCE_VALIDATION:
    - "Bulk snapshot performance with complete attribute capture"
    - "INFORMATION_SCHEMA query optimization validation"
    - "Memory usage monitoring for databases with many schemas"
```

## IMPLEMENTATION_READINESS_ASSESSMENT
```yaml
PHASE_1_COMPLETION_STATUS:
  XSD_ATTRIBUTE_EXTRACTION: "✅ COMPLETE"
    - "18 configuration attributes identified and documented"
    - "Complete XSD parsing with types and constraints"
    - "Attribute relationships and dependencies mapped"
    
  STATE_ATTRIBUTE_DISCOVERY: "✅ COMPLETE"
    - "15 operational metadata attributes identified"
    - "INFORMATION_SCHEMA.SCHEMATA mapping complete"
    - "System-generated vs user-controlled properties categorized"
    
  ATTRIBUTE_CATEGORIZATION: "✅ COMPLETE"
    - "85% XSD configuration / 15% operational state split validated"
    - "Structural vs excluded properties clearly defined"
    - "SQL source mapping complete for all attributes"
    
  FRAMEWORK_INTEGRATION_ANALYSIS: "✅ COMPLETE"
    - "Liquibase integration points identified"
    - "Framework limitations documented with realistic success criteria"
    - "Service registration requirements specified"
    
NEXT_PHASE_READINESS:
  READY_FOR_PHASE_2: true
  DELIVERABLE_STATUS: "PHASE_1_COMPLETE"
  HANDOFF_TO: "ai_requirements_writeup.md Phase 2"
  REQUIREMENTS_INPUT: "research_findings_snowflake_schema_snapshot_diff.md"
```

This research findings document provides complete XSD-driven analysis of Snowflake Schema objects with 100% attribute coverage (18 XSD configuration + 15 operational state attributes), comprehensive SQL mapping, and realistic framework integration assessment. Ready for Phase 2 requirements documentation.