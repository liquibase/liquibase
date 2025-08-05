# Complete Diff/Generate Changelog Implementation Guide
## Practical Workflow for Schema Comparison and Changelog Generation

## START HERE

**This guide covers implementing diff and generate-changelog functionality** for Liquibase extensions:
- Schema comparison between databases
- Changelog generation from schema differences  
- Integration testing with real database connections
- Validation of changetype and snapshot/diff components

**Prerequisites**: Changetype and snapshot/diff implementations must be complete before using this guide.

## IMPLEMENTATION SCENARIOS

### Scenario Selection
```yaml
SCENARIO_A_NEW_DIFF_SUPPORT:
  DESCRIPTION: "Add diff/changelog support for new database object type"
  PREREQUISITES: "Changetype + Snapshot/Diff implementations complete"
  WORKFLOW: "Phase 0 → Phase 1 → Phase 2 → Phase 3"
  DURATION: "4-6 hours"
  
SCENARIO_B_ENHANCE_EXISTING:
  DESCRIPTION: "Improve existing diff/changelog functionality"
  PREREQUISITES: "Basic diff support exists"
  WORKFLOW: "Phase 0 → Phase 2 → Phase 3"
  DURATION: "2-4 hours"
  
SCENARIO_C_FIX_CHANGELOG_BUGS:
  DESCRIPTION: "Fix bugs in changelog generation"
  PREREQUISITES: "Existing implementation with issues"
  WORKFLOW: "Phase 0 → Phase 3"
  DURATION: "1-3 hours"
```

## PHASE 0: PREREQUISITES VALIDATION

### STEP 0.1: Validate Changetype Implementation
```bash
# Test that all changetypes work correctly
mvn test -Dtest="*Change*Test" -q

# Test integration with real database
mvn test -Dtest="*Change*IntegrationTest" -q
```

**Success Criteria:**
- All changetype tests pass
- All integration tests pass
- SQL generation works for all scenarios

### STEP 0.2: Validate Snapshot/Diff Implementation
```bash
# Test snapshot generators
mvn test -Dtest="*Snapshot*Test" -q

# Test diff comparators  
mvn test -Dtest="*Comparator*Test" -q

# Test integration
mvn test -Dtest="*ObjectIntegrationTest" -q
```

**Success Criteria:**
- Snapshot generators correctly capture database state
- Comparators correctly identify differences
- Integration tests pass with real database

### STEP 0.3: Database Connectivity Validation
```bash
# Test basic connectivity
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" \
SNOWFLAKE_USER="COMMUNITYKEVIN" \
SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" \
mvn test -Dtest="DatabaseObjectIntegrationTest" -q
```

## PHASE 1: SCHEMA ISOLATION SETUP

### STEP 1.1: Create Test Schema Pattern
```java
// Use established naming pattern for schema isolation
private String getReferenceSchema(String testMethod) {
    return "DIFF_TEST_REF_" + testMethod;
}

private String getComparisonSchema(String testMethod) {
    return "DIFF_TEST_CMP_" + testMethod;
}
```

### STEP 1.2: Database Connection Setup
```java
// Reference database (source of truth)
String referenceUrl = "jdbc:snowflake://server/?db=DATABASE&warehouse=WH&schema=" + getReferenceSchema(testMethod) + "&role=ROLE";

// Comparison database (target for comparison)
String comparisonUrl = "jdbc:snowflake://server/?db=DATABASE&warehouse=WH&schema=" + getComparisonSchema(testMethod) + "&role=ROLE";
```

### STEP 1.3: Test Object Creation
```java
// Create test objects in reference schema
String createObjects = "USE SCHEMA " + referenceSchema + "; " +
    "CREATE TABLE TEST_CUSTOMERS_" + testMethod + " (ID NUMBER(10,0) NOT NULL, NAME VARCHAR(100)); " +
    "CREATE SEQUENCE TEST_SEQ_" + testMethod + " START WITH 1 INCREMENT BY 1;";

// Leave comparison schema empty or with different objects
```

## PHASE 2: DIFF COMMAND IMPLEMENTATION

### STEP 2.1: Basic Diff Command
```bash
# Set up environment variables
export LIQUIBASE_CLASSPATH="/path/to/liquibase-snowflake.jar"
export USER="your_user"
export PASSWORD="your_password"

# Run diff command
liquibase \
  --url="$REFERENCE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  --reference-url="$COMPARISON_URL" \
  --reference-username="$USER" \
  --reference-password="$PASSWORD" \
  diff
```

### STEP 2.2: Diff with Output Formatting
```bash
# JSON format diff
liquibase \
  --url="$REFERENCE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  --reference-url="$COMPARISON_URL" \
  --reference-username="$USER" \
  --reference-password="$PASSWORD" \
  diff --format=json

# Text format with specific schemas
liquibase diff \
  --includeCatalog=false \
  --includeSchema=false \
  --includeTablespace=false \
  --outputSchemas=TARGET_SCHEMA
```

## PHASE 3: CHANGELOG GENERATION

### STEP 3.1: Generate Changelog Command
```bash
# Generate changelog from differences
liquibase \
  --url="$REFERENCE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  --reference-url="$COMPARISON_URL" \
  --reference-username="$USER" \
  --reference-password="$PASSWORD" \
  diff-changelog --changelog-file=generated-changelog.xml
```

### STEP 3.2: Generate Changelog from Database
```bash
# Generate changelog from existing database
liquibase \
  --url="$DATABASE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  generate-changelog --changelog-file=database-changelog.xml
```

### STEP 3.3: Validate Generated Changelog
```bash
# Test that generated changelog is valid
liquibase \
  --url="$CLEAN_DATABASE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  --changelog-file=generated-changelog.xml \
  update

# Verify the update worked correctly
liquibase \
  --url="$CLEAN_DATABASE_URL" \
  --username="$USER" \
  --password="$PASSWORD" \
  status
```

## INTEGRATION TESTING PATTERNS

### Test Method Template
```java
@Test
public void testObjectTypeDiffChangelog() throws Exception {
    String testMethod = "testObjectTypeDiffChangelog";
    String referenceSchema = "DIFF_TEST_REF_" + testMethod;
    String comparisonSchema = "DIFF_TEST_CMP_" + testMethod;
    
    try {
        // Setup: Create schemas and objects
        createTestSchemas(referenceSchema, comparisonSchema);
        createTestObjects(referenceSchema, testMethod);
        
        // Execute: Run diff command
        Database refDb = getDatabase(getReferenceUrl(referenceSchema));
        Database compDb = getDatabase(getComparisonUrl(comparisonSchema));
        
        DiffResult diffResult = DiffGeneratorFactory.getInstance()
            .compare(refDb, compDb, new CompareControl());
            
        // Validate: Check diff results
        assertNotNull(diffResult);
        assertFalse(diffResult.getMissingObjects().isEmpty());
        
        // Generate changelog
        DiffToChangeLog changelogGenerator = new DiffToChangeLog(diffResult, new DiffOutputControl());
        String changelog = changelogGenerator.generateChangelog();
        
        // Validate changelog is not empty and contains expected changes
        assertNotNull(changelog);
        assertTrue(changelog.contains("createTable"));
        
    } finally {
        // Cleanup: Drop test schemas
        cleanupTestSchemas(referenceSchema, comparisonSchema);
    }
}
```

### Command Line Integration Test
```bash
#!/bin/bash
# Integration test script for diff/changelog functionality

TEST_METHOD="commandLineIntegrationTest"
REFERENCE_SCHEMA="DIFF_TEST_REF_$TEST_METHOD"
COMPARISON_SCHEMA="DIFF_TEST_CMP_$TEST_METHOD"

# Setup test schemas
sqlcmd -Q "CREATE SCHEMA IF NOT EXISTS $REFERENCE_SCHEMA; CREATE SCHEMA IF NOT EXISTS $COMPARISON_SCHEMA;"

# Create test objects in reference schema only
sqlcmd -Q "USE SCHEMA $REFERENCE_SCHEMA; CREATE TABLE TEST_TABLE (ID INT, NAME VARCHAR(50));"

# Run diff and capture output
DIFF_OUTPUT=$(liquibase diff \
  --url="$REFERENCE_URL" \
  --reference-url="$COMPARISON_URL" \
  --format=text)

echo "Diff Output: $DIFF_OUTPUT"

# Generate changelog
liquibase diff-changelog \
  --url="$REFERENCE_URL" \
  --reference-url="$COMPARISON_URL" \
  --changelog-file="test-changelog-$TEST_METHOD.xml"

# Validate changelog was created
if [ -f "test-changelog-$TEST_METHOD.xml" ]; then
    echo "SUCCESS: Changelog generated successfully"
    cat "test-changelog-$TEST_METHOD.xml"
else
    echo "FAILURE: Changelog not generated"
    exit 1
fi

# Cleanup
sqlcmd -Q "DROP SCHEMA IF EXISTS $REFERENCE_SCHEMA CASCADE; DROP SCHEMA IF EXISTS $COMPARISON_SCHEMA CASCADE;"
rm -f "test-changelog-$TEST_METHOD.xml"
```

## VALIDATION CHECKLIST

### Diff Functionality
- [ ] Diff command executes without errors
- [ ] Differences are correctly identified
- [ ] Output formats (text, JSON) work correctly
- [ ] Schema filtering works as expected

### Changelog Generation  
- [ ] Changelog files are generated successfully
- [ ] Generated changelogs are valid XML
- [ ] Changelogs contain expected change operations
- [ ] Generated changelogs can be successfully applied

### Integration Requirements
- [ ] All prerequisite tests pass (changetype + snapshot/diff)
- [ ] Database connectivity works for both reference and comparison
- [ ] Schema isolation prevents test interference
- [ ] Cleanup procedures work correctly

## TROUBLESHOOTING

### Common Issues

**Issue**: "No differences found" when differences should exist
- **Cause**: Snapshot generators not capturing all properties
- **Solution**: Review and enhance snapshot generator implementation

**Issue**: "Generated changelog fails validation"  
- **Cause**: Change generators producing invalid XML or SQL
- **Solution**: Review change generator implementations and XSD schema

**Issue**: "Database connection failures"
- **Cause**: Incorrect connection URLs or credentials
- **Solution**: Validate database connectivity in Phase 0

**Issue**: "Schema isolation failures"
- **Cause**: Test schemas not properly cleaned up
- **Solution**: Implement robust cleanup in finally blocks

### Debug Commands
```bash
# Enable debug logging
export LIQUIBASE_LOG_LEVEL=DEBUG

# Test individual components
mvn test -Dtest="*Snapshot*Test" -X
mvn test -Dtest="*Comparator*Test" -X
mvn test -Dtest="*ChangeGenerator*Test" -X

# Validate service registration
find . -name "*.jar" -exec jar tf {} \; | grep META-INF/services
```

## SUCCESS CRITERIA

### Phase Completion Criteria

**Phase 0 Complete:**
- All prerequisite tests pass
- Database connectivity validated
- No blocking issues identified

**Phase 1 Complete:**
- Schema isolation pattern implemented
- Test object creation works
- Database connections established

**Phase 2 Complete:**
- Diff commands execute successfully
- Differences correctly identified
- Output formatting works

**Phase 3 Complete:**
- Changelogs generated successfully
- Generated changelogs are valid
- Integration tests pass

### Overall Success
- End-to-end diff/changelog workflow functions correctly
- All test scenarios pass
- Real database integration works
- Documentation is complete and accurate