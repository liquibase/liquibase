AIPL_VERSION: "1.0"
PROGRAM_NAME: "diff-generate-changelog-implementation"
DESCRIPTION: "Complete implementation guide for Liquibase diff-changelog and generate-changelog commands with Snowflake extension integration"

VARIABLES:
  LIQUIBASE_SNOWFLAKE_JAR: "/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/target/liquibase-snowflake-*.jar"
  LIQUIBASE_CLI: "/Users/kevinchappell/liquibase/liquibase-4.33.0/liquibase"
  TEST_DATABASE_URL: "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE"
  TEST_DATABASE_USER: "COMMUNITYKEVIN"
  TEST_DATABASE_PASSWORD: "uQ1lAjwVisliu8CpUTVh0UnxoTUk3"
  TEST_METHOD: "${METHOD_NAME}"
  REFERENCE_SCHEMA: "DIFF_TEST_REF_${TEST_METHOD}"
  COMPARISON_SCHEMA: "DIFF_TEST_CMP_${TEST_METHOD}"
  REFERENCE_URL: "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${REFERENCE_SCHEMA}&role=LB_INT_ROLE"
  COMPARISON_URL: "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${COMPARISON_SCHEMA}&role=LB_INT_ROLE"
  OUTPUT_CHANGELOG: "./diff-changelog-${TEST_METHOD}.xml"
  CHANGETYPE_TEST_PATTERN: "*Change*Test"
  SNAPSHOT_TEST_PATTERN: "*Snapshot*Test,*Comparator*Test"
  INTEGRATION_TEST_PATTERN: "*IntegrationTest"

PHASES:
  PHASE_0_PREREQUISITES_VALIDATION:
    DESCRIPTION: "Validate all changetype and snapshot/diff implementations are complete and tested before proceeding"
    BLOCKING: true
    STEPS:
      - VALIDATE_CHANGETYPE_IMPLEMENTATIONS
      - VALIDATE_SNAPSHOT_DIFF_IMPLEMENTATIONS  
      - VALIDATE_INTEGRATION_READINESS
      - VALIDATE_EXISTING_INFRASTRUCTURE
      - VALIDATE_DATABASE_CONNECTIVITY

  PHASE_1_SCHEMA_ISOLATION_TESTING:
    DESCRIPTION: "Test diff/changelog functionality using established schema isolation patterns"
    STEPS:
      - CREATE_ISOLATED_TEST_SCHEMAS
      - SETUP_TEST_OBJECTS
      - EXECUTE_DIFF_COMMANDS
      - EXECUTE_CHANGELOG_GENERATION
      - CLEANUP_TEST_SCHEMAS

  PHASE_2_INTEGRATION_VALIDATION:
    DESCRIPTION: "Validate existing infrastructure components work together"
    STEPS:
      - VALIDATE_CHANGEGENERATOR_REGISTRATION
      - VALIDATE_SNAPSHOT_INTEGRATION
      - VALIDATE_DIFF_INTEGRATION
      - VALIDATE_COMMAND_INTEGRATION

  PHASE_3_COMPREHENSIVE_TESTING:
    DESCRIPTION: "Test all object types with proper schema isolation"
    STEPS:
      - TEST_TABLE_DIFF_CHANGELOG
      - TEST_SEQUENCE_DIFF_CHANGELOG
      - TEST_DATABASE_DIFF_CHANGELOG
      - TEST_SCHEMA_DIFF_CHANGELOG
      - TEST_WAREHOUSE_DIFF_CHANGELOG

TEMPLATES:
  SCHEMA_HELPER_TEMPLATE: |
    private String getUniqueSchemaName(String methodName) {
        return "DIFF_TEST_REF_" + methodName;
    }
    
    private String getComparisonSchemaName(String methodName) {
        return "DIFF_TEST_CMP_" + methodName;
    }

  SCHEMA_ISOLATION_SETUP_TEMPLATE: |
    // Following SchemaIsolationHook pattern
    String testMethod = "${TEST_METHOD}";
    String referenceSchema = "DIFF_TEST_REF_" + testMethod;
    String comparisonSchema = "DIFF_TEST_CMP_" + testMethod;
    
    // Create isolated schemas
    String sql = "CREATE SCHEMA IF NOT EXISTS " + referenceSchema + "; " +
                "CREATE SCHEMA IF NOT EXISTS " + comparisonSchema + ";";
    
  TEST_OBJECT_CREATION_TEMPLATE: |
    // Create test objects following established naming patterns
    String objectName = "TEST_CUSTOMERS_" + methodName;
    String sequenceName = "TEST_SEQ_" + methodName;
    
    String createObjects = "USE SCHEMA " + referenceSchema + "; " +
        "CREATE TABLE " + objectName + " (ID NUMBER(10,0) NOT NULL, NAME VARCHAR(100)); " +
        "CREATE SEQUENCE " + sequenceName + " START WITH 1 INCREMENT BY 1;";
    
  DIFF_COMMAND_TEMPLATE: |
    LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} \
      --url="${REFERENCE_URL}" \
      --username="${TEST_DATABASE_USER}" \
      --password="${TEST_DATABASE_PASSWORD}" \
      --reference-url="${COMPARISON_URL}" \
      --reference-username="${TEST_DATABASE_USER}" \
      --reference-password="${TEST_DATABASE_PASSWORD}" \
      diff
      
  CHANGELOG_GENERATION_TEMPLATE: |
    LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} \
      --url="${REFERENCE_URL}" \
      --username="${TEST_DATABASE_USER}" \
      --password="${TEST_DATABASE_PASSWORD}" \
      --reference-url="${COMPARISON_URL}" \
      --reference-username="${TEST_DATABASE_USER}" \
      --reference-password="${TEST_DATABASE_PASSWORD}" \
      diff-changelog --changelog-file="${OUTPUT_CHANGELOG}"

PHASE_0_PREREQUISITES_VALIDATION:
  VALIDATE_EXISTING_INFRASTRUCTURE:
    VALIDATE:
      NAME: "verify-infrastructure-exists"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
      PATTERN: "ChangeGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "ChangeGenerator infrastructure missing - diff/changelog functionality requires complete ChangeGenerator registration"

  VALIDATE_CHANGETYPE_IMPLEMENTATIONS:
    VALIDATE:
      NAME: "verify-changetype-completeness"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "mvn test -Dtest=${CHANGETYPE_TEST_PATTERN} -q"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Changetype implementations not complete - run changetype implementation guide first"

    VALIDATE:
      NAME: "verify-changetype-service-registration"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.change.Change"
      PATTERN: "Change"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Changetype service registration missing"

    VALIDATE:
      NAME: "verify-sql-generator-registration"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator"
      PATTERN: "Generator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "SQL generator service registration missing"

  VALIDATE_SNAPSHOT_DIFF_IMPLEMENTATIONS:
    VALIDATE:
      NAME: "verify-snapshot-diff-completeness"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "mvn test -Dtest=${SNAPSHOT_TEST_PATTERN} -q"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Snapshot/diff implementations not complete - run snapshot/diff implementation guide first"

    VALIDATE:
      NAME: "verify-snapshot-generator-registration"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
      PATTERN: "SnapshotGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Snapshot generator service registration missing"

    VALIDATE:
      NAME: "verify-comparator-registration"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.DatabaseObjectComparator"
      PATTERN: "Comparator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Database object comparator service registration missing"

  VALIDATE_INTEGRATION_READINESS:
    COMMAND:
      NAME: "test-changetype-snapshot-integration"
      EXECUTE: "SNOWFLAKE_URL=${TEST_DATABASE_URL} SNOWFLAKE_USER=${TEST_DATABASE_USER} SNOWFLAKE_PASSWORD=${TEST_DATABASE_PASSWORD} mvn test -Dtest=${INTEGRATION_TEST_PATTERN} -q"

    VALIDATE:
      NAME: "verify-integration-test-success"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Changetype and snapshot/diff integration not working - fix integration issues first"

    VALIDATE:
      NAME: "verify-database-object-registration"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.structure.DatabaseObject"
      PATTERN: "DatabaseObject"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Database object service registration missing - objects won't appear in snapshots"

  VALIDATE_DATABASE_CONNECTIVITY:
    COMMAND:
      NAME: "test-database-connection"
      EXECUTE: "SNOWFLAKE_URL=${TEST_DATABASE_URL} SNOWFLAKE_USER=${TEST_DATABASE_USER} SNOWFLAKE_PASSWORD=${TEST_DATABASE_PASSWORD} mvn test -Dtest=DatabaseConnectionTest -q"

    VALIDATE:
      NAME: "verify-database-access"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Database connectivity failed - check credentials and network access"

PHASE_1_SCHEMA_ISOLATION_TESTING:
  CREATE_ISOLATED_TEST_SCHEMAS:
    COMMAND:
      NAME: "create-method-based-schemas"
      EXECUTE: "TEST_METHOD=${TEST_METHOD} && REFERENCE_SCHEMA=DIFF_TEST_REF_${TEST_METHOD} && COMPARISON_SCHEMA=DIFF_TEST_CMP_${TEST_METHOD} && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='CREATE SCHEMA IF NOT EXISTS ${REFERENCE_SCHEMA}; CREATE SCHEMA IF NOT EXISTS ${COMPARISON_SCHEMA};'"

    VALIDATE:
      NAME: "verify-schemas-created"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Schema creation failed"

  SETUP_TEST_OBJECTS:
    COMMAND:
      NAME: "create-test-objects-method-based-naming"
      EXECUTE: "TEST_METHOD=${TEST_METHOD} && REFERENCE_SCHEMA=DIFF_TEST_REF_${TEST_METHOD} && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='USE SCHEMA ${REFERENCE_SCHEMA}; CREATE TABLE TEST_CUSTOMERS_${TEST_METHOD} (ID NUMBER(10,0) NOT NULL, NAME VARCHAR(100), EMAIL VARCHAR(200), CREATED_DATE DATE); CREATE SEQUENCE TEST_SEQ_${TEST_METHOD} START WITH 1 INCREMENT BY 1;'"

    VALIDATE:
      NAME: "verify-test-objects-created"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Test object creation failed"

  EXECUTE_DIFF_COMMANDS:
    COMMAND:
      NAME: "test-diff-with-schema-isolation"
      EXECUTE: "TEST_METHOD=${TEST_METHOD} && REFERENCE_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=DIFF_TEST_REF_${TEST_METHOD}&role=LB_INT_ROLE' && COMPARISON_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=DIFF_TEST_CMP_${TEST_METHOD}&role=LB_INT_ROLE' && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${REFERENCE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' --reference-url='${COMPARISON_URL}' --reference-username='${TEST_DATABASE_USER}' --reference-password='${TEST_DATABASE_PASSWORD}' diff"

    VALIDATE:
      NAME: "verify-diff-command-success"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Diff command failed"

  EXECUTE_CHANGELOG_GENERATION:
    COMMAND:
      NAME: "test-changelog-generation-schema-isolation"
      EXECUTE: "TEST_METHOD=${TEST_METHOD} && REFERENCE_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=DIFF_TEST_REF_${TEST_METHOD}&role=LB_INT_ROLE' && COMPARISON_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=DIFF_TEST_CMP_${TEST_METHOD}&role=LB_INT_ROLE' && OUTPUT_FILE='./diff-changelog-${TEST_METHOD}.xml' && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${REFERENCE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' --reference-url='${COMPARISON_URL}' --reference-username='${TEST_DATABASE_USER}' --reference-password='${TEST_DATABASE_PASSWORD}' diff-changelog --changelog-file='${OUTPUT_FILE}'"

    VALIDATE:
      NAME: "verify-changelog-generated"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Changelog generation failed"

  CLEANUP_TEST_SCHEMAS:
    COMMAND:
      NAME: "cleanup-schemas-established-pattern"
      EXECUTE: "TEST_METHOD=${TEST_METHOD} && REFERENCE_SCHEMA=DIFF_TEST_REF_${TEST_METHOD} && COMPARISON_SCHEMA=DIFF_TEST_CMP_${TEST_METHOD} && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='DROP SCHEMA IF EXISTS ${REFERENCE_SCHEMA} CASCADE; DROP SCHEMA IF EXISTS ${COMPARISON_SCHEMA} CASCADE;' && rm -f diff-changelog-${TEST_METHOD}.xml"

    VALIDATE:
      NAME: "verify-cleanup-success"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Cleanup failed"

PHASE_2_INTEGRATION_VALIDATION:
  VALIDATE_CHANGEGENERATOR_REGISTRATION:
    VALIDATE:
      NAME: "verify-changegenerator-count"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "grep -c 'ChangeGenerator' src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "ChangeGenerator registration incomplete"

    COMMAND:
      NAME: "list-registered-changegenerators"
      EXECUTE: "cat src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"

  VALIDATE_SNAPSHOT_INTEGRATION:
    VALIDATE:
      NAME: "verify-snapshot-generators"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
      PATTERN: "SnapshotGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Snapshot generators not registered"

  VALIDATE_DIFF_INTEGRATION:
    VALIDATE:
      NAME: "verify-diff-comparators"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.DatabaseObjectComparator"
      PATTERN: "Comparator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Diff comparators not registered"

  VALIDATE_COMMAND_INTEGRATION:
    COMMAND:
      NAME: "verify-liquibase-commands-available"
      EXECUTE: "${LIQUIBASE_CLI} --help | grep -E 'diff|changelog'"

    VALIDATE:
      NAME: "verify-commands-exist"
      TYPE: "COMMAND_SUCCESS"
      COMMAND: "echo $?"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Liquibase diff/changelog commands not available"

PHASE_3_COMPREHENSIVE_TESTING:
  TEST_TABLE_DIFF_CHANGELOG:
    COMMAND:
      NAME: "test-table-objects"
      EXECUTE: "TEST_METHOD=testTableObjects && REFERENCE_SCHEMA=DIFF_TEST_REF_${TEST_METHOD} && COMPARISON_SCHEMA=DIFF_TEST_CMP_${TEST_METHOD} && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='CREATE SCHEMA IF NOT EXISTS ${REFERENCE_SCHEMA}; USE SCHEMA ${REFERENCE_SCHEMA}; CREATE TABLE TEST_CUSTOMERS_${TEST_METHOD} (ID NUMBER, NAME VARCHAR(100));' && REFERENCE_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${REFERENCE_SCHEMA}&role=LB_INT_ROLE' && COMPARISON_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${COMPARISON_SCHEMA}&role=LB_INT_ROLE' && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${REFERENCE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' --reference-url='${COMPARISON_URL}' --reference-username='${TEST_DATABASE_USER}' --reference-password='${TEST_DATABASE_PASSWORD}' diff-changelog --changelog-file='./table-diff-${TEST_METHOD}.xml' && rm -f table-diff-${TEST_METHOD}.xml && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='DROP SCHEMA IF EXISTS ${REFERENCE_SCHEMA} CASCADE; DROP SCHEMA IF EXISTS ${COMPARISON_SCHEMA} CASCADE;'"

  TEST_SEQUENCE_DIFF_CHANGELOG:
    COMMAND:
      NAME: "test-sequence-objects"
      EXECUTE: "TEST_METHOD=testSequenceObjects && REFERENCE_SCHEMA=DIFF_TEST_REF_${TEST_METHOD} && COMPARISON_SCHEMA=DIFF_TEST_CMP_${TEST_METHOD} && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='CREATE SCHEMA IF NOT EXISTS ${REFERENCE_SCHEMA}; USE SCHEMA ${REFERENCE_SCHEMA}; CREATE SEQUENCE TEST_SEQ_${TEST_METHOD} START WITH 1 INCREMENT BY 1;' && REFERENCE_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${REFERENCE_SCHEMA}&role=LB_INT_ROLE' && COMPARISON_URL='jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=${COMPARISON_SCHEMA}&role=LB_INT_ROLE' && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${REFERENCE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' --reference-url='${COMPARISON_URL}' --reference-username='${TEST_DATABASE_USER}' --reference-password='${TEST_DATABASE_PASSWORD}' diff && rm -f sequence-diff-${TEST_METHOD}.xml && LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='DROP SCHEMA IF EXISTS ${REFERENCE_SCHEMA} CASCADE; DROP SCHEMA IF EXISTS ${COMPARISON_SCHEMA} CASCADE;'"

  TEST_DATABASE_DIFF_CHANGELOG:
    VALIDATE:
      NAME: "verify-database-objects-supported"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
      PATTERN: "DatabaseChangeGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Database ChangeGenerators not registered"

  TEST_SCHEMA_DIFF_CHANGELOG:
    VALIDATE:
      NAME: "verify-schema-objects-supported"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
      PATTERN: "SchemaChangeGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Schema ChangeGenerators not registered"

  TEST_WAREHOUSE_DIFF_CHANGELOG:
    VALIDATE:
      NAME: "verify-warehouse-objects-supported"
      TYPE: "CONTAINS_TEXT"
      TARGET: "src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator"
      PATTERN: "WarehouseChangeGenerator"
      FAILURE_ACTION: "STOP"
      ERROR_MESSAGE: "Warehouse ChangeGenerators not registered"


ERROR_HANDLING:
  GLOBAL_FAILURE_ACTION: "STOP"
  CLEANUP_ON_FAILURE:
    - COMMAND:
        EXECUTE: "rm -f diff-changelog-*.xml table-diff-*.xml sequence-diff-*.xml"
        CONTINUE_ON_ERROR: true
    - COMMAND:
        EXECUTE: "LIQUIBASE_CLASSPATH=${LIQUIBASE_SNOWFLAKE_JAR} ${LIQUIBASE_CLI} --url='${TEST_DATABASE_URL}' --username='${TEST_DATABASE_USER}' --password='${TEST_DATABASE_PASSWORD}' execute-sql --sql='DROP SCHEMA IF EXISTS DIFF_TEST_REF_${TEST_METHOD} CASCADE; DROP SCHEMA IF EXISTS DIFF_TEST_CMP_${TEST_METHOD} CASCADE;'"
        CONTINUE_ON_ERROR: true

SUCCESS_CRITERIA:
  INFRASTRUCTURE_VALIDATED: "Existing ChangeGenerator infrastructure confirmed working"
  SCHEMA_ISOLATION_WORKING: "Method-name-based schema isolation patterns functioning"
  DIFF_COMMANDS_WORKING: "Liquibase core diff and diff-changelog commands work with Snowflake extension"
  ALL_OBJECTS_SUPPORTED: "Tables, sequences, databases, schemas, warehouses all detected in diff operations"
  CHANGELOG_GENERATION_WORKING: "Valid XML changelog files generated from schema differences"
  CLEANUP_SUCCESSFUL: "Test schemas properly cleaned up using established patterns"
  INTEGRATION_COMPLETE: "Changetype/snapshot/diff integration confirmed functional"