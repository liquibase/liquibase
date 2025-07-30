# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Part 4: Testing Guide

This document provides comprehensive testing strategies for your Liquibase extension implementation.

### Table of Contents
1. [Testing Overview](#testing-overview)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [Test Harness](#test-harness)
5. [Performance Testing](#performance-testing)
6. [Edge Cases and Negative Testing](#edge-cases-and-negative-testing)

---

## Testing Overview

### Testing Pyramid for Liquibase Extensions

```
                 /\
                /  \  End-to-End Tests (Test Harness)
               /    \
              /------\ Integration Tests
             /        \
            /----------\ Unit Tests
```

### Test Coverage Goals

- **Unit Tests**: 90%+ coverage of business logic
- **Integration Tests**: All database interactions
- **Test Harness**: Key user workflows
- **Performance Tests**: Realistic data volumes

### Testing Infrastructure

```groovy
// build.gradle
dependencies {
    testImplementation 'org.spockframework:spock-core:2.3-groovy-3.0'
    testImplementation 'org.liquibase:liquibase-test-harness:4.33.0'
    testImplementation 'com.h2database:h2:2.1.214'
    testImplementation 'org.mockito:mockito-core:4.11.0'
}

test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

---

## Unit Testing

### Mocking Strategies

#### Database Mocking
```groovy
class DatabaseMockingExamples extends Specification {
    
    def "test with mocked database"() {
        given:
        def database = Mock(MyDatabase)
        def resultSet = Mock(ResultSet)
        
        when:
        database.getDatabaseProductName() >> "MyDatabase"
        database.getDefaultCatalogName() >> "DEFAULT_CATALOG"
        database.isCaseSensitive() >> false
        database.query(_ as SqlStatement) >> resultSet
        
        // Configure ResultSet
        resultSet.next() >>> [true, true, false]  // Two rows
        resultSet.getString("name") >>> ["OBJ1", "OBJ2"]
        
        then:
        // Test your component
        def result = componentUnderTest.process(database)
        result.size() == 2
    }
    
    def "test exception handling"() {
        given:
        def database = Mock(Database)
        
        when:
        database.query(_ as SqlStatement) >> { 
            throw new SQLException("Connection lost") 
        }
        componentUnderTest.process(database)
        
        then:
        thrown(DatabaseException)
    }
}
```

#### Snapshot Mocking
```groovy
def "test with mocked snapshot"() {
    given:
    def snapshot = Mock(DatabaseSnapshot)
    def database = Mock(Database)
    def snapshotControl = Mock(SnapshotControl)
    
    when:
    snapshot.getDatabase() >> database
    snapshot.getSnapshotControl() >> snapshotControl
    snapshot.get(_ as DatabaseObject) >> null  // Not found
    snapshot.get(MyObject.class) >> [obj1, obj2, obj3]
    
    then:
    // Test snapshot interactions
}
```

### Data-Driven Testing with Spock

```groovy
@Unroll
def "test property validation: #scenario"() {
    given:
    def object = new MyObject()
    
    when:
    if (propertyName) {
        object."set${propertyName.capitalize()}"(value)
    }
    
    then:
    if (shouldThrow) {
        thrown(expectedError)
    } else {
        noExceptionThrown()
        object."get${propertyName.capitalize()}"() == expectedValue
    }
    
    where:
    scenario               | propertyName | value      | shouldThrow | expectedError            | expectedValue
    "valid string"         | "property1"  | "VALID"    | false       | null                    | "VALID"
    "null string"         | "property1"  | null       | false       | null                    | null
    "empty string"        | "property1"  | ""         | true        | IllegalArgumentException | null
    "invalid enum"        | "property1"  | "INVALID"  | true        | IllegalArgumentException | null
    "negative number"     | "property2"  | -1         | true        | IllegalArgumentException | null
    "valid number"        | "property2"  | 42         | false       | null                    | 42
    "boolean true"        | "property3"  | true       | false       | null                    | true
    "boolean false"       | "property3"  | false      | false       | null                    | false
}
```

### Testing Database-Specific Behavior

```groovy
def "test case sensitivity handling"() {
    given:
    def comparator = new MyObjectComparator()
    def obj1 = createObject("test_object")
    def obj2 = createObject("TEST_OBJECT")
    
    when:
    def caseSensitiveDb = Mock(Database) { isCaseSensitive() >> true }
    def caseInsensitiveDb = Mock(Database) { isCaseSensitive() >> false }
    
    then:
    !comparator.isSameObject(obj1, obj2, caseSensitiveDb, Mock(DatabaseObjectComparatorChain))
    comparator.isSameObject(obj1, obj2, caseInsensitiveDb, Mock(DatabaseObjectComparatorChain))
}
```

---

## Integration Testing

### Database Connection Setup

```groovy
abstract class DatabaseIntegrationTest extends Specification {
    
    @Shared
    def database
    
    def setupSpec() {
        def url = System.getenv("TEST_DB_URL") ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        def username = System.getenv("TEST_DB_USERNAME") ?: "sa"
        def password = System.getenv("TEST_DB_PASSWORD") ?: ""
        
        database = DatabaseFactory.getInstance().openDatabase(url, username, password, null, null)
    }
    
    def cleanupSpec() {
        database?.close()
    }
    
    def cleanup() {
        // Clean up after each test
        rollbackDatabase()
    }
    
    protected void rollbackDatabase() {
        try {
            database.rollback()
        } catch (Exception e) {
            // Log but don't fail test
        }
    }
}
```

### Testing Snapshot Generation

```groovy
class MyObjectSnapshotIntegrationTest extends DatabaseIntegrationTest {
    
    def "test snapshot captures all properties"() {
        given:
        createTestObject("TEST_OBJ", [
            property1: "VALUE1",
            property2: 42,
            property3: true,
            comment: "Integration test"
        ])
        
        when:
        def example = new MyObject()
        example.setName("TEST_OBJ")
        example.setSchema(database.getDefaultSchema())
        
        def snapshot = createSnapshot(example)
        def capturedObject = snapshot.get(example)
        
        then:
        capturedObject != null
        capturedObject.getProperty1() == "VALUE1"
        capturedObject.getProperty2() == 42
        capturedObject.getProperty3() == true
        capturedObject.getComment() == "Integration test"
        capturedObject.getState() != null  // State populated
        capturedObject.getOwner() != null  // Owner populated
        
        cleanup:
        dropTestObject("TEST_OBJ")
    }
    
    def "test bulk snapshot performance"() {
        given:
        def objectCount = 100
        def startTime = System.currentTimeMillis()
        
        // Create many objects
        (1..objectCount).each { i ->
            createTestObject("PERF_TEST_${i}", [property1: "VALUE${i}"])
        }
        def setupTime = System.currentTimeMillis() - startTime
        
        when:
        startTime = System.currentTimeMillis()
        def snapshot = createSnapshot(database.getDefaultSchema())
        def snapshotTime = System.currentTimeMillis() - startTime
        
        def objects = snapshot.get(MyObject.class)
        def testObjects = objects.findAll { it.name.startsWith("PERF_TEST_") }
        
        then:
        testObjects.size() == objectCount
        snapshotTime < 5000  // Should complete within 5 seconds
        
        println "Setup time: ${setupTime}ms, Snapshot time: ${snapshotTime}ms"
        println "Performance: ${objectCount / (snapshotTime / 1000.0)} objects/second"
        
        cleanup:
        (1..objectCount).each { i ->
            dropTestObject("PERF_TEST_${i}")
        }
    }
    
    // Helper methods
    protected void createTestObject(String name, Map properties) {
        def sql = buildCreateSql(name, properties)
        database.execute([new RawSqlStatement(sql)])
    }
    
    protected void dropTestObject(String name) {
        database.execute([new RawSqlStatement("DROP OBJECT IF EXISTS ${name}")])
    }
    
    protected DatabaseSnapshot createSnapshot(DatabaseObject example) {
        def control = new SnapshotControl(database, example.getClass())
        return SnapshotGeneratorFactory.getInstance().createSnapshot(example, database, control)
    }
}
```

### Testing Diff Generation

```groovy
class MyObjectDiffIntegrationTest extends Specification {
    
    def referenceDb
    def targetDb
    
    def setup() {
        referenceDb = createDatabase("reference")
        targetDb = createDatabase("target")
    }
    
    def cleanup() {
        referenceDb?.close()
        targetDb?.close()
    }
    
    def "test diff identifies all change types"() {
        given:
        // Reference database state
        referenceDb.execute([
            createObject("UNCHANGED", [property1: "VALUE1"]),
            createObject("MODIFIED", [property1: "OLD", property2: 10]),
            createObject("DELETED", [property1: "VALUE1"])
        ])
        
        // Target database state
        targetDb.execute([
            createObject("UNCHANGED", [property1: "VALUE1"]),
            createObject("MODIFIED", [property1: "NEW", property2: 20]),
            createObject("ADDED", [property1: "VALUE1"])
        ])
        
        when:
        def compareControl = new CompareControl(MyObject.class)
        def diffResult = DiffGeneratorFactory.getInstance().compare(
            referenceDb, targetDb, compareControl
        )
        
        then:
        // Verify missing (to be created)
        def missing = diffResult.getMissingObjects(MyObject.class)
        missing.size() == 1
        missing[0].name == "DELETED"
        
        // Verify unexpected (to be dropped)
        def unexpected = diffResult.getUnexpectedObjects(MyObject.class)
        unexpected.size() == 1
        unexpected[0].name == "ADDED"
        
        // Verify changed
        def changed = diffResult.getChangedObjects(MyObject.class)
        changed.size() == 1
        def modifiedDiff = changed.entrySet()[0]
        modifiedDiff.key.name == "MODIFIED"
        modifiedDiff.value.hasDifference("property1")
        modifiedDiff.value.hasDifference("property2")
    }
    
    def "test diff to changelog conversion"() {
        given:
        referenceDb.execute([createObject("TEST_OBJ", [property1: "VALUE1"])])
        // targetDb is empty
        
        when:
        def diffResult = DiffGeneratorFactory.getInstance().compare(
            referenceDb, targetDb, new CompareControl(MyObject.class)
        )
        
        def changelog = new DatabaseChangeLog()
        new DiffToChangeLog(diffResult, new DiffOutputControl()).print(changelog)
        
        then:
        changelog.changeSets.size() == 1
        
        def changes = changelog.changeSets[0].changes
        changes.size() == 1
        changes[0] instanceof CreateMyObjectChange
        changes[0].objectName == "TEST_OBJ"
        changes[0].property1 == "VALUE1"
    }
}
```

---

## Test Harness

### Extending Liquibase Test Harness

```groovy
package liquibase.ext.mydb.harness

import liquibase.harness.SnapshotTest
import liquibase.harness.util.TestUtils
import org.junit.Test

class MyObjectSnapshotHarnessTest extends SnapshotTest {
    
    @Test
    void testMyObjectSnapshot() {
        // Use harness utilities
        TestUtils.executeUpdate(getDatabase(), """
            CREATE OBJECT HARNESS_TEST_OBJ
            WITH PROPERTY1 = 'HARNESS_VALUE'
                 PROPERTY2 = 99
                 COMMENT = 'Test harness object'
        """)
        
        try {
            // Take snapshot
            def snapshot = takeSnapshot(MyObject.class)
            
            // Verify
            assertNotNull("Snapshot should not be null", snapshot)
            
            def objects = snapshot.get(MyObject.class)
            assertFalse("Should find objects", objects.isEmpty())
            
            def testObj = objects.find { it.name == "HARNESS_TEST_OBJ" }
            assertNotNull("Should find test object", testObj)
            assertEquals("HARNESS_VALUE", testObj.property1)
            assertEquals(99, testObj.property2)
            assertEquals("Test harness object", testObj.comment)
            
        } finally {
            TestUtils.executeUpdate(getDatabase(), 
                "DROP OBJECT IF EXISTS HARNESS_TEST_OBJ")
        }
    }
    
    @Test
    void testSnapshotPerformance() {
        def objectCount = 50
        def objects = []
        
        // Create test objects
        def setupStart = System.currentTimeMillis()
        (1..objectCount).each { i ->
            TestUtils.executeUpdate(getDatabase(), 
                "CREATE OBJECT PERF_OBJ_${i} WITH PROPERTY1 = 'VALUE${i}'")
            objects << "PERF_OBJ_${i}"
        }
        def setupTime = System.currentTimeMillis() - setupStart
        
        try {
            // Measure snapshot time
            def snapshotStart = System.currentTimeMillis()
            def snapshot = takeSnapshot(MyObject.class)
            def snapshotTime = System.currentTimeMillis() - snapshotStart
            
            // Verify
            def capturedObjects = snapshot.get(MyObject.class)
                .findAll { it.name.startsWith("PERF_OBJ_") }
            
            assertEquals(objectCount, capturedObjects.size())
            
            // Performance assertion
            assertTrue("Snapshot should complete within 3 seconds", 
                      snapshotTime < 3000)
            
            println "Setup: ${setupTime}ms, Snapshot: ${snapshotTime}ms"
            println "Rate: ${objectCount / (snapshotTime / 1000.0)} objects/sec"
            
        } finally {
            objects.each { name ->
                TestUtils.executeUpdate(getDatabase(), 
                    "DROP OBJECT IF EXISTS ${name}")
            }
        }
    }
}
```

### Diff Test Harness

```groovy
class MyObjectDiffHarnessTest extends DiffTest {
    
    @Test
    void testCompleteDiffWorkflow() {
        // Setup reference database
        setupReferenceDatabase { db ->
            TestUtils.executeUpdate(db, """
                CREATE OBJECT DIFF_OBJ_1 
                WITH PROPERTY1 = 'REF_VALUE'
                     PROPERTY2 = 10
            """)
            TestUtils.executeUpdate(db, """
                CREATE OBJECT DIFF_OBJ_2
                WITH PROPERTY1 = 'TO_DELETE'
            """)
        }
        
        // Setup target database
        setupTargetDatabase { db ->
            TestUtils.executeUpdate(db, """
                CREATE OBJECT DIFF_OBJ_1
                WITH PROPERTY1 = 'TARGET_VALUE'
                     PROPERTY2 = 20
            """)
            TestUtils.executeUpdate(db, """
                CREATE OBJECT DIFF_OBJ_3
                WITH PROPERTY1 = 'TO_CREATE'
            """)
        }
        
        try {
            // Run diff
            def diffResult = runDiff(MyObject.class)
            
            // Verify missing (in reference, not in target)
            def missing = diffResult.getMissingObjects(MyObject.class)
            assertEquals(1, missing.size())
            assertEquals("DIFF_OBJ_2", missing[0].name)
            
            // Verify unexpected (in target, not in reference)
            def unexpected = diffResult.getUnexpectedObjects(MyObject.class)
            assertEquals(1, unexpected.size())
            assertEquals("DIFF_OBJ_3", unexpected[0].name)
            
            // Verify changed
            def changed = diffResult.getChangedObjects(MyObject.class)
            assertEquals(1, changed.size())
            
            def changedObj = changed.keySet().iterator().next()
            assertEquals("DIFF_OBJ_1", changedObj.name)
            
            def differences = changed.get(changedObj)
            assertTrue(differences.hasDifference("property1"))
            assertTrue(differences.hasDifference("property2"))
            
        } finally {
            // Cleanup
            ["DIFF_OBJ_1", "DIFF_OBJ_2", "DIFF_OBJ_3"].each { name ->
                cleanupObject(getReferenceDatabase(), name)
                cleanupObject(getTargetDatabase(), name)
            }
        }
    }
}
```

---

## Performance Testing

### Performance Test Suite

```groovy
@Category(PerformanceTest.class)
class MyObjectPerformanceTest extends Specification {
    
    @Shared
    def performanceDb
    
    def setupSpec() {
        performanceDb = createPerformanceDatabase()
    }
    
    def cleanupSpec() {
        performanceDb?.close()
    }
    
    def "test snapshot scaling"() {
        given:
        def testCases = [10, 50, 100, 500, 1000]
        def results = []
        
        when:
        testCases.each { count ->
            def result = measureSnapshotPerformance(count)
            results << result
            
            // Clean up before next iteration
            cleanupTestObjects(count)
        }
        
        then:
        // Verify linear or better scaling
        results.each { result ->
            def objectsPerSecond = result.count / (result.time / 1000.0)
            println "${result.count} objects: ${result.time}ms (${objectsPerSecond} obj/s)"
            
            // Performance should not degrade significantly
            assert objectsPerSecond > 50  // Minimum 50 objects/second
        }
        
        // Check scaling factor
        def scalingFactor = calculateScalingFactor(results)
        assert scalingFactor < 1.5  // Should scale better than O(n^1.5)
    }
    
    def "test diff performance with large datasets"() {
        given:
        def objectCount = 1000
        def changePercent = 10  // 10% of objects changed
        
        // Create reference state
        createBulkObjects(performanceDb, objectCount)
        def referenceSnapshot = takeSnapshot(performanceDb)
        
        // Modify some objects
        modifyRandomObjects(performanceDb, objectCount * changePercent / 100)
        
        when:
        def startTime = System.currentTimeMillis()
        def diffResult = DiffGeneratorFactory.getInstance().compare(
            referenceSnapshot, performanceDb, new CompareControl(MyObject.class)
        )
        def diffTime = System.currentTimeMillis() - startTime
        
        then:
        def changedCount = diffResult.getChangedObjects(MyObject.class).size()
        changedCount == objectCount * changePercent / 100
        
        diffTime < 10000  // Should complete within 10 seconds
        
        println "Diff of ${objectCount} objects with ${changedCount} changes: ${diffTime}ms"
    }
    
    def "test memory usage"() {
        given:
        def runtime = Runtime.getRuntime()
        def objectCount = 10000
        
        // Create many objects
        createBulkObjects(performanceDb, objectCount)
        
        when:
        // Force GC and measure baseline
        runtime.gc()
        Thread.sleep(100)
        def baselineMemory = runtime.totalMemory() - runtime.freeMemory()
        
        // Take snapshot
        def snapshot = takeSnapshot(performanceDb)
        
        // Measure memory after snapshot
        def afterMemory = runtime.totalMemory() - runtime.freeMemory()
        def memoryUsed = (afterMemory - baselineMemory) / 1024 / 1024  // MB
        
        then:
        snapshot.get(MyObject.class).size() >= objectCount
        
        // Memory usage should be reasonable
        def memoryPerObject = memoryUsed * 1024 / objectCount  // KB per object
        println "Memory usage: ${memoryUsed}MB for ${objectCount} objects"
        println "Memory per object: ${memoryPerObject}KB"
        
        assert memoryPerObject < 10  // Less than 10KB per object
    }
}
```

---

## Edge Cases and Negative Testing

### Edge Case Testing

```groovy
class MyObjectEdgeCaseTest extends Specification {
    
    def "test boundary values"() {
        when:
        def object = new MyObject()
        object."set${property}"(value)
        
        then:
        if (shouldSucceed) {
            object."get${property}"() == expectedValue
        } else {
            thrown(expectedException)
        }
        
        where:
        property    | value           | shouldSucceed | expectedValue    | expectedException
        "Property2" | 0               | true          | 0               | null
        "Property2" | Integer.MAX_VALUE | true        | Integer.MAX_VALUE | null
        "Property2" | -1              | false         | null            | IllegalArgumentException
        "Comment"   | "A" * 255       | true          | "A" * 255       | null
        "Comment"   | "A" * 256       | false         | null            | IllegalArgumentException
        "Comment"   | ""              | true          | ""              | null
        "Comment"   | null            | true          | null            | null
    }
    
    def "test special characters in strings"() {
        given:
        def specialStrings = [
            "Simple text",
            "Text with 'quotes'",
            'Text with "double quotes"',
            "Text with\nnewlines",
            "Text with\ttabs",
            "Text with unicode: 你好",
            "Text with emoji: 😀",
            "Text with SQL: '; DROP TABLE x; --",
            "Text with null char: \0",
            "Text with backslash: \\",
        ]
        
        when:
        def results = specialStrings.collect { str ->
            try {
                def obj = new MyObject()
                obj.setName("TEST")
                obj.setComment(str)
                return [string: str, success: true, stored: obj.getComment()]
            } catch (Exception e) {
                return [string: str, success: false, error: e.class.simpleName]
            }
        }
        
        then:
        results.each { result ->
            println "String: '${result.string}' - Success: ${result.success}"
            if (result.success) {
                assert result.stored == result.string
            }
        }
    }
    
    def "test concurrent modifications"() {
        given:
        def database = Mock(Database)
        def object = new MyObject()
        object.setName("CONCURRENT_TEST")
        
        when:
        // Simulate concurrent modification
        database.execute(_ as SqlStatement[]) >> {
            throw new DatabaseException("Object was modified by another session")
        }
        
        def change = new AlterMyObjectChange()
        change.setObjectName("CONCURRENT_TEST")
        change.execute(database)
        
        then:
        thrown(DatabaseException)
    }
}
```

### Negative Testing

```groovy
class MyObjectNegativeTest extends Specification {
    
    def "test invalid database type"() {
        given:
        def unsupportedDb = Mock(Database)
        unsupportedDb.getClass() >> com.example.UnsupportedDatabase
        
        when:
        def generator = new MyObjectSnapshotGenerator()
        def priority = generator.getPriority(MyObject.class, unsupportedDb)
        
        then:
        priority == SnapshotGenerator.PRIORITY_NONE
    }
    
    def "test malformed SQL injection attempts"() {
        given:
        def maliciousNames = [
            "'; DROP TABLE users; --",
            "\" OR 1=1 --",
            "'; INSERT INTO admin VALUES ('hacker'); --",
            "\0; DELETE FROM objects; --"
        ]
        
        when:
        def results = maliciousNames.collect { name ->
            try {
                def obj = new MyObject()
                obj.setName(name)
                
                // Attempt to use in SQL
                def sql = "SELECT * FROM objects WHERE name = '" + obj.getName() + "'"
                return [name: name, sql: sql, safe: true]
            } catch (Exception e) {
                return [name: name, error: e.message, safe: false]
            }
        }
        
        then:
        // Verify all malicious attempts are handled safely
        results.each { result ->
            println "Injection attempt: ${result.name}"
            // Should either reject the name or escape it properly
            assert result.safe || result.error != null
        }
    }
    
    def "test resource exhaustion"() {
        given:
        def database = Mock(Database)
        def resultSet = Mock(ResultSet)
        
        when:
        // Simulate infinite result set
        resultSet.next() >> true  // Always returns true
        resultSet.getString("name") >> { "OBJECT_${System.nanoTime()}" }
        
        database.query(_ as SqlStatement) >> resultSet
        
        def generator = new MyObjectSnapshotGenerator()
        def snapshot = Mock(DatabaseSnapshot)
        snapshot.getDatabase() >> database
        
        // This should have protection against infinite loops
        generator.addTo(new Schema(), snapshot)
        
        then:
        // Should eventually throw or have built-in limits
        thrown(Exception)
    }
}
```

---

## Test Execution and Reporting

### Gradle Configuration

```groovy
// build.gradle
test {
    useJUnitPlatform()
    
    // Test categories
    if (project.hasProperty('testCategory')) {
        useJUnitPlatform {
            includeTags project.testCategory
        }
    }
    
    // Test reporting
    reports {
        junitXml.enabled = true
        html.enabled = true
    }
    
    // JaCoCo coverage
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

// Custom test tasks
task unitTest(type: Test) {
    useJUnitPlatform {
        excludeTags 'integration', 'performance'
    }
}

task integrationTest(type: Test) {
    useJUnitPlatform {
        includeTags 'integration'
    }
}

task performanceTest(type: Test) {
    useJUnitPlatform {
        includeTags 'performance'
    }
}
```

### Running Tests

```bash
# All tests
./gradlew test

# Only unit tests
./gradlew unitTest

# Only integration tests
./gradlew integrationTest -PtestDbUrl=jdbc:mydb://localhost:5432/test

# Performance tests
./gradlew performanceTest -DrunPerformanceTests=true

# Specific test class
./gradlew test --tests MyObjectSnapshotGeneratorTest

# With coverage
./gradlew test jacocoTestReport

# Test report location
open build/reports/tests/test/index.html
open build/reports/jacoco/test/html/index.html
```

---

## Testing Checklist

### Unit Testing
- [ ] All public methods tested
- [ ] Edge cases covered
- [ ] Error conditions tested
- [ ] Mocks used appropriately
- [ ] Data-driven tests for variations
- [ ] 90%+ code coverage

### Integration Testing
- [ ] Real database connections
- [ ] Transaction handling
- [ ] Large dataset handling
- [ ] Database-specific features
- [ ] Error recovery
- [ ] Performance baselines

### Test Harness
- [ ] Snapshot accuracy
- [ ] Diff correctness
- [ ] Change generation
- [ ] End-to-end workflows
- [ ] Cross-database compatibility

### Performance Testing
- [ ] Scaling behavior
- [ ] Memory usage
- [ ] Query optimization
- [ ] Concurrent access
- [ ] Resource cleanup

### Negative Testing
- [ ] Invalid inputs
- [ ] Security (SQL injection)
- [ ] Resource exhaustion
- [ ] Concurrent modifications
- [ ] Network failures

---

## Next Steps

With comprehensive testing in place:
1. Run full test suite
2. Review coverage reports
3. Address any gaps
4. Document known limitations
5. Proceed to **Part 5: Reference Implementation**