# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Part 4: Testing Guide - AI Implementation Instructions

This document provides step-by-step testing strategies for Liquibase snapshot/diff extension implementation. Follow these instructions exactly for reliable implementation.

### Table of Contents
1. [Test Environment Setup](#test-environment-setup)
2. [Testing Strategy Overview](#testing-strategy-overview)
3. [Unit Testing Implementation](#unit-testing-implementation)
4. [Integration Testing Implementation](#integration-testing-implementation)
5. [Test Harness Implementation](#test-harness-implementation)
6. [Common Error Patterns and Solutions](#common-error-patterns-and-solutions)

---

## Test Environment Setup

### Test Database Configuration
**MANDATORY:** Use dedicated test environment for integration tests

**Test Environment:**
- **Database:** `LB_INT_SNAPSHOT_DB`
- **Schema:** `BASE_SCHEMA`
- **Role:** `LB_INT_ROLE`
- **Connection:** Same Snowflake credentials as changetype integration tests

### Connection Validation (Start Every Session)

**Step 1: Validate Test Environment**
```bash
# Test connection to integration test environment
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_INT_SNAPSHOT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest=ConnectionValidationTest -q
```

**Expected Output:** Connection successful, can query BASE_SCHEMA
**If Failed:** Verify environment exists, credentials correct

---

## Testing Strategy Overview

### Testing Pyramid for Snapshot/Diff Extensions

```
                 /\
                /  \  Test Harness (Full Integration)
               /    \  ✅ Database/Schema/Table/Sequence supported
              /------\ Integration Tests (Real Database)
             /        \  Bridge unit tests → test harness
            /----------\ Unit Tests (Mocked, Stable Foundation)
```

### Test Coverage Goals and Success Criteria

- **Unit Tests:** 90%+ coverage, no database connections, stable foundation
- **Integration Tests:** Real database validation, "almost guaranteed test harness success"
- **Test Harness:** Full user workflow validation, all object types supported

### Test Harness Scope (IMPORTANT)

**✅ SUPPORTED OBJECTS (Will work in test harness):**
- `liquibase.database.object.Database` ✅
- `liquibase.structure.core.Schema` ✅  
- `liquibase.structure.core.Table` ✅
- `liquibase.structure.core.Sequence` ✅

**❌ UNSUPPORTED OBJECTS (Test harness limitations):**
- Warehouse objects (account-level, framework unsupported)

**Result:** Our Database/Schema/Table/Sequence implementation will work fully in test harness.

---

## Unit Testing Implementation

### Unit Test Strategy
**Purpose:** Stable foundation without database connections
**Approach:** Comprehensive TDD with extensive mocking
**Success:** 90%+ business logic coverage

### Unit Test Structure (Per Object Type)

#### 1. Object Model Tests
```java
// Example: DatabaseTest.java (23 tests)
@Test
void testDatabaseCreationWithMinimalProperties() {
    database.setName("TEST_DB");
    assertEquals("TEST_DB", database.getName());
    assertEquals("TEST_DB", database.getSnapshotId());
}

@Test
void testDataRetentionTimeInDays() {
    database.setDataRetentionTimeInDays(7);
    assertEquals(7, database.getDataRetentionTimeInDays());
    assertThrows(IllegalArgumentException.class, () -> database.setDataRetentionTimeInDays(-1));
}
```

**Test Categories:**
- ✅ Identity and core properties (name, snapshotId)
- ✅ All XSD configuration attributes (18 properties)
- ✅ Operational state attributes (10 properties)
- ✅ Validation logic (constraints, valid values)
- ✅ Property categorization (config vs state)  
- ✅ Object relationships (containing objects, schema)
- ✅ equals/hashCode implementation

#### 2. Snapshot Generator Tests
```java
// Example: DatabaseSnapshotGeneratorTest.java (16 tests)
@Test
void testSnapshotObjectWithExistingDatabase() throws Exception {
    // Setup mocked database connection
    when(database.getConnection()).thenReturn(jdbcConnection);
    when(jdbcConnection.createStatement()).thenReturn(statement);
    when(statement.executeQuery(any())).thenReturn(resultSet);
    
    // Mock result set data
    when(resultSet.next()).thenReturn(true, false);
    when(resultSet.getString("DATABASE_NAME")).thenReturn("TEST_DB");
    
    // Execute and verify
    DatabaseObject result = generator.snapshotObject(example, snapshot);
    assertEquals("TEST_DB", result.getName());
}
```

**Test Categories:**
- ✅ Framework integration (priority, addsTo, replaces)
- ✅ Database query execution and result mapping
- ✅ Type conversions (YES/NO → boolean, null handling)
- ✅ Error handling (SQLException, null inputs)
- ✅ Multiple query patterns (INFORMATION_SCHEMA + SHOW commands)

#### 3. Comparator Tests
```java
// Example: DatabaseComparatorTest.java (21 tests)
@Test
void testFindDifferencesWithDifferentComments() {
    Database db1 = createTestDatabase();
    db1.setComment("Original comment");
    
    Database db2 = createTestDatabase();
    db2.setComment("Modified comment");
    
    ObjectDifferences differences = comparator.findDifferences(db1, db2, database, compareControl, chain, new HashSet<>());
    
    assertTrue(differences.hasDifferences());
}
```

**Test Categories:**
- ✅ Framework integration (priority, hash generation)
- ✅ Object identity comparison (case-insensitive names)
- ✅ Configuration property diff detection
- ✅ State property exclusion from comparison
- ✅ Edge cases (null values, empty strings)

### Unit Test Success Criteria
**All Must Pass:**
- ✅ 90%+ code coverage achieved
- ✅ All business logic paths tested
- ✅ No database connections used
- ✅ All edge cases and error conditions covered
- ✅ Framework integration points validated

---

## Integration Testing Implementation

### Integration Test Strategy
**Purpose:** Bridge unit tests → test harness with real database validation
**Goal:** "Almost guaranteed test harness success"
**Approach:** Three-phase implementation

### Phase 1: Direct Component Testing (START HERE)

#### Connection Setup Pattern
```java
@BeforeEach
public void setUp() throws Exception {
    String url = System.getenv("SNOWFLAKE_URL");
    String user = System.getenv("SNOWFLAKE_USER");
    String password = System.getenv("SNOWFLAKE_PASSWORD");
    
    connection = DriverManager.getConnection(url, user, password);
    database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
}
```

#### 1A. SnapshotGenerator Integration Tests

**Test Pattern:**
```java
@Test
public void testDatabaseSnapshotGeneratorIntegration() throws Exception {
    String uniqueName = "INT_TEST_DB_" + System.currentTimeMillis();
    
    try {
        // Create test object in database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + uniqueName);
        createStmt.execute();
        createStmt.close();
        
        // Use SnapshotGenerator to capture it
        DatabaseSnapshotGenerator generator = new DatabaseSnapshotGenerator();
        Database example = new Database();
        example.setName(uniqueName);
        
        DatabaseSnapshot snapshot = new DatabaseSnapshot(database, new SnapshotControl(database));
        DatabaseObject result = generator.snapshotObject(example, snapshot);
        
        // Verify properties match
        assertNotNull(result);
        assertEquals(uniqueName, result.getName());
        
    } finally {
        // Cleanup
        PreparedStatement dropStmt = connection.prepareStatement("DROP DATABASE IF EXISTS " + uniqueName);
        dropStmt.execute();
        dropStmt.close();
    }
}
```

**Success Criteria:**
- ✅ Database queries execute successfully
- ✅ Objects created and retrieved correctly
- ✅ Object properties match database state
- ✅ No exceptions in snapshot workflow

#### 1B. Comparator Integration Tests (3 Scenarios)

**Scenario 1: Same Objects**
```java
@Test
public void testComparatorSameObjects() throws Exception {
    Database db1 = createTestDatabase("SAME_DB");
    Database db2 = createTestDatabase("SAME_DB");
    
    DatabaseComparator comparator = new DatabaseComparator();
    ObjectDifferences differences = comparator.findDifferences(db1, db2, database, null, null, null);
    
    assertFalse(differences.hasDifferences()); // Should be identical
}
```

**Scenario 2: New Source Objects**
```java
@Test
public void testComparatorNewSourceObjects() throws Exception {
    Database source = createTestDatabase("SOURCE_DB");
    source.setComment("Source comment");
    
    Database target = createTestDatabase("SOURCE_DB");
    target.setComment(null); // Missing comment
    
    ObjectDifferences differences = comparator.findDifferences(source, target, database, null, null, null);
    
    assertTrue(differences.hasDifferences()); // Should detect missing comment
}
```

**Scenario 3: New Target Objects**
```java
@Test
public void testComparatorNewTargetObjects() throws Exception {
    Database source = createTestDatabase("TARGET_DB");
    source.setDataRetentionTimeInDays(null);
    
    Database target = createTestDatabase("TARGET_DB");
    target.setDataRetentionTimeInDays(7); // Additional property
    
    ObjectDifferences differences = comparator.findDifferences(source, target, database, null, null, null);
    
    assertTrue(differences.hasDifferences()); // Should detect additional property
}
```

#### 1C. Create → Snapshot → Compare Workflow
```java
@Test
public void testCompleteWorkflow() throws Exception {
    String uniqueName = "WORKFLOW_DB_" + System.currentTimeMillis();
    
    try {
        // CREATE: Set up test objects in database
        PreparedStatement createStmt = connection.prepareStatement(
            "CREATE DATABASE " + uniqueName + " COMMENT = 'Integration test'");
        createStmt.execute();
        createStmt.close();
        
        // SNAPSHOT: Capture current state
        DatabaseSnapshotGenerator generator = new DatabaseSnapshotGenerator();
        Database example = new Database();
        example.setName(uniqueName);
        
        DatabaseSnapshot snapshot = new DatabaseSnapshot(database, new SnapshotControl(database));
        Database snapshotResult = (Database) generator.snapshotObject(example, snapshot);
        
        // COMPARE: Create different version and compare
        Database modifiedVersion = new Database();
        modifiedVersion.setName(uniqueName);
        modifiedVersion.setComment("Modified comment");
        
        DatabaseComparator comparator = new DatabaseComparator();
        ObjectDifferences differences = comparator.findDifferences(
            snapshotResult, modifiedVersion, database, null, null, null);
        
        // VALIDATE: Workflow executed without exceptions
        assertNotNull(snapshotResult);
        assertNotNull(differences);
        assertTrue(differences.hasDifferences()); // Should detect comment difference
        
    } finally {
        PreparedStatement dropStmt = connection.prepareStatement("DROP DATABASE IF EXISTS " + uniqueName);
        dropStmt.execute();
        dropStmt.close();
    }
}
```

### Phase 2: Framework API Integration Tests

#### 2D. Service Registration Tests
```java
@Test
public void testSnapshotGeneratorRegistration() {
    // Test direct service loading
    SnapshotGenerator generator = new DatabaseSnapshotGenerator();
    
    // Verify framework integration
    assertEquals(SnapshotGenerator.PRIORITY_DATABASE, 
                generator.getPriority(Database.class, database));
    assertEquals(SnapshotGenerator.PRIORITY_NONE, 
                generator.getPriority(Schema.class, database));
    
    // Verify configuration
    assertNotNull(generator.addsTo());
    assertEquals(0, generator.addsTo().length);
    assertNull(generator.replaces());
}

@Test
public void testComparatorRegistration() {
    DatabaseComparator comparator = new DatabaseComparator();
    
    // Verify framework integration
    assertEquals(DatabaseObjectComparator.PRIORITY_DATABASE,
                comparator.getPriority(Database.class, database));
    assertEquals(DatabaseObjectComparator.PRIORITY_NONE,
                comparator.getPriority(Schema.class, database));
}
```

### Phase 3: Pattern Replication

**After Database object integration tests work:**
1. **Apply** successful patterns to Schema object
2. **Apply** successful patterns to Table object  
3. **Apply** successful patterns to Sequence object
4. **Validate** all object types work with real database

### Integration Test Success Criteria
**All Must Pass Before Test Harness:**
- ✅ Database queries execute successfully
- ✅ Objects created and retrieved correctly
- ✅ Comparisons detect differences accurately
- ✅ No exceptions in snapshot/diff workflow
- ✅ Framework service registration works
- ✅ All test scenarios pass (same/new source/new target)

---

## Test Harness Implementation

### Test Harness Confidence Level
**Expected Result:** Full test harness success for Database/Schema/Table/Sequence objects
**Reason:** Integration tests validate real database functionality

### Test Harness Execution Pattern
```bash
# Build and install extension
cd liquibase-snowflake
mvn clean install -DskipTests

# Switch to test harness
cd ../liquibase-test-harness

# Run snapshot tests
mvn test -Dtest=SnapshotObjectTests -DdbName=snowflake -DsnapshotObjects=createDatabase
mvn test -Dtest=SnapshotObjectTests -DdbName=snowflake -DsnapshotObjects=createSchema
mvn test -Dtest=SnapshotObjectTests -DdbName=snowflake -DsnapshotObjects=createTable
mvn test -Dtest=SnapshotObjectTests -DdbName=snowflake -DsnapshotObjects=createSequence
```

### Test Harness Success Criteria
- ✅ All changesets execute correctly
- ✅ Objects created in database
- ✅ Snapshots contain expected objects
- ✅ JSON comparisons work
- ✅ No test harness errors

---

## Common Error Patterns and Solutions

### Pattern 1: Service Registration Issues

**Symptom:** "No snapshot generator found for type"

**Debug Steps:**
```bash
# 1. Verify service file exists
ls -la src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator

# 2. Check JAR contents
jar -tf target/*.jar | grep META-INF/services

# 3. Verify class name matches exactly
grep -r "class.*SnapshotGenerator" src/
```

**Solution:** Ensure exact class name match in service file

### Pattern 2: Database Connection Issues

**Symptom:** Connection failures in integration tests

**Debug Steps:**
```bash
# Validate connection manually
SNOWFLAKE_URL="jdbc:snowflake://..." mvn test -Dtest=ConnectionValidationTest
```

**Solution:** Verify credentials, database, schema, and role exist

### Pattern 3: Null Handling Issues

**Symptom:** NullPointerException in parseResultSet

**Solution:** Defensive null checking
```java
// Good pattern
String value = rs.getString("property");
if (value != null && !"null".equalsIgnoreCase(value)) {
    object.setProperty(value);
}
```

### Pattern 4: Case Sensitivity Issues

**Symptom:** Column not found errors

**Solution:** Handle case variations
```java
private String getColumnValue(ResultSet rs, String... possibleNames) {
    for (String name : possibleNames) {
        try {
            return rs.getString(name);
        } catch (SQLException e) {
            // Try next variation
        }
    }
    throw new DatabaseException("Column not found: " + Arrays.toString(possibleNames));
}
```

---

## Testing Implementation Checklist

### Unit Testing
- [ ] Object model tests implemented (20+ tests per object)
- [ ] Snapshot generator tests implemented (15+ tests per object)
- [ ] Comparator tests implemented (20+ tests per object)
- [ ] 90%+ code coverage achieved
- [ ] All tests use mocks, no database connections
- [ ] All edge cases and error conditions tested

### Integration Testing - Phase 1
- [ ] Test environment connection validated
- [ ] SnapshotGenerator integration tests implemented
- [ ] Real database create → snapshot → verify workflow working
- [ ] Comparator integration tests implemented (3 scenarios)
- [ ] Create → Snapshot → Compare workflow tests implemented
- [ ] All tests clean up properly

### Integration Testing - Phase 2
- [ ] Service registration tests implemented
- [ ] Framework API integration validated
- [ ] Component discovery working correctly

### Integration Testing - Phase 3
- [ ] Pattern applied to all object types
- [ ] All object types working with real database
- [ ] Comprehensive integration coverage achieved

### Test Harness
- [ ] Extension JAR built and installed
- [ ] All object types tested in harness
- [ ] Full snapshot/diff functionality validated
- [ ] Test harness success achieved

---

## Next Steps

After completing all testing phases:
1. **Unit Tests** provide stable foundation
2. **Integration Tests** ensure real database functionality  
3. **Test Harness** validates full user workflows
4. **Ready for production** snapshot/diff implementation

**Success Criteria Met:** "Almost guaranteed test harness success" achieved through comprehensive integration testing with real database validation.