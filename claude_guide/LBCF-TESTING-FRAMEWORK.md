# LBCF Testing Framework Guide

## Testing Philosophy

### Core Principles
1. **Test First**: Write tests before implementation
2. **Real Database Testing**: Always test against actual database
3. **Comprehensive Coverage**: Test success, failure, and edge cases
4. **Fast Feedback**: Run tests frequently during development
5. **Clean Environment**: Each test is independent

## Test Architecture

### Three-Layer Testing Strategy

```
┌─────────────────────┐
│  Unit Tests         │ Fast, isolated component tests
├─────────────────────┤
│  Integration Tests  │ Real database, full lifecycle
├─────────────────────┤
│  Test Harness       │ Complete changelog execution
└─────────────────────┘
```

## Test Development Workflow

### 1. Start with Unit Tests

```groovy
// Test the change class first
class Create${Object}ChangeTest extends Specification {
    def "validate required fields"() {
        given: "a change without required fields"
        def change = new Create${Object}Change()
        
        when: "validation is performed"
        def errors = change.validate(new ${Database}Database())
        
        then: "errors are reported"
        errors.hasErrors()
        errors.errorMessages.contains("${object}Name is required")
    }
}
```

### 2. Test SQL Generation

```groovy
class Create${Object}Generator${Database}Test extends Specification {
    def "generate correct SQL"() {
        given: "a statement with all attributes"
        def statement = new Create${Object}Statement(name, attr1, attr2)
        
        when: "SQL is generated"
        def sql = generator.generateSql(statement, database, null)
        
        then: "SQL contains expected elements"
        sql[0].toSql() == expectedSql
        
        where:
        name    | attr1   | attr2 | expectedSql
        "TEST"  | null    | null  | "CREATE ${OBJECT} TEST"
        "TEST"  | "val1"  | 10    | "CREATE ${OBJECT} TEST WITH ATTR1='val1' ATTR2=10"
    }
}
```

### 3. Integration Test Against Database

```groovy
@LiquibaseIntegrationTest
class ${Object}${Database}IntegrationTest extends Specification {
    def "execute change against real database"() {
        given: "a create change"
        def change = new Create${Object}Change()
        change.${object}Name = uniqueName()
        
        when: "change is executed"
        executeChange(change)
        
        then: "object exists in database"
        objectExists(change.${object}Name)
        
        cleanup: "remove test object"
        dropObject(change.${object}Name)
    }
}
```

## Testing Patterns

### 1. Test Data Management

```groovy
trait TestDataHelper {
    // Generate unique names to avoid conflicts
    String uniqueName(String prefix = "LB_TEST") {
        "${prefix}_${System.currentTimeMillis()}_${UUID.randomUUID().toString()[0..7]}"
    }
    
    // Clean up test objects
    void cleanupTestObjects(Database database, String pattern = "LB_TEST%") {
        database.execute([
            new RawSqlStatement("DROP ${OBJECT} IF EXISTS LIKE '${pattern}'")
        ])
    }
}
```

### 2. Database Connection Management

```groovy
abstract class DatabaseIntegrationTest extends Specification {
    @Shared Database database
    @Shared DatabaseTestSystem testSystem
    
    def setupSpec() {
        testSystem = getTestSystem("${database}")
        if (testSystem?.shouldTest()) {
            database = createDatabase(testSystem.connection)
        }
    }
    
    def cleanupSpec() {
        database?.close()
    }
    
    boolean shouldRun() {
        testSystem?.shouldTest() ?: false
    }
}
```

### 3. Change Execution Helper

```groovy
trait ChangeExecutor {
    void executeChange(Change change, Database database) {
        def changeSet = new ChangeSet(
            "test-${UUID.randomUUID()}", 
            "test", 
            false, 
            false, 
            null, 
            null, 
            null, 
            null
        )
        changeSet.addChange(change)
        
        def changelog = new DatabaseChangeLog()
        changelog.addChangeSet(changeSet)
        
        changelog.execute(database, new Contexts())
    }
    
    void executeChangelog(String changelogPath, Database database) {
        def liquibase = new Liquibase(
            changelogPath,
            new ClassLoaderResourceAccessor(),
            database
        )
        liquibase.update(new Contexts())
    }
}
```

### 4. Assertion Helpers

```groovy
trait DatabaseAssertions {
    boolean tableExists(Database database, String tableName) {
        database.getConnection().getMetaData()
            .getTables(null, null, tableName.toUpperCase(), ["TABLE"] as String[])
            .next()
    }
    
    boolean columnExists(Database database, String tableName, String columnName) {
        database.getConnection().getMetaData()
            .getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())
            .next()
    }
    
    int rowCount(Database database, String tableName, String whereClause = "1=1") {
        def result = database.getConnection()
            .createStatement()
            .executeQuery("SELECT COUNT(*) FROM ${tableName} WHERE ${whereClause}")
        result.next()
        result.getInt(1)
    }
}
```

## Test Categories

### 1. Validation Tests

```groovy
def "validate attribute constraints"() {
    given:
    def change = new Create${Object}Change()
    change.${object}Name = "TEST"
    change.${attribute} = invalidValue
    
    when:
    def errors = change.validate(database)
    
    then:
    errors.hasErrors()
    errors.errorMessages.any { it.contains(expectedError) }
    
    where:
    invalidValue | expectedError
    -1          | "must be positive"
    "invalid"   | "invalid format"
    null        | "is required"
}
```

### 2. SQL Generation Tests

```groovy
@Unroll
def "generate SQL for #scenario"() {
    given:
    def statement = createStatement(params)
    
    when:
    def sql = generator.generateSql(statement, database, null)
    
    then:
    sql.length == expectedCount
    sql[0].toSql() == expectedSql
    
    where:
    scenario          | params                    | expectedCount | expectedSql
    "basic create"    | [name: "TEST"]           | 1            | "CREATE ${OBJECT} TEST"
    "with options"    | [name: "TEST", opt: "X"] | 1            | "CREATE ${OBJECT} TEST WITH OPTION X"
    "multiple steps"  | [name: "TEST", tag: "T"] | 2            | "CREATE ${OBJECT} TEST"
}
```

### 3. Rollback Tests

```groovy
def "test rollback functionality"() {
    given: "a change with rollback"
    def change = new Create${Object}Change()
    change.${object}Name = uniqueName()
    
    when: "change is executed"
    executeChange(change)
    
    then: "object exists"
    objectExists(change.${object}Name)
    
    when: "rollback is performed"
    def rollbackStatements = change.generateRollbackStatements(database)
    database.execute(rollbackStatements)
    
    then: "object no longer exists"
    !objectExists(change.${object}Name)
}
```

### 4. Error Handling Tests

```groovy
def "handle database errors gracefully"() {
    given: "a change that will fail"
    def change = new Create${Object}Change()
    change.${object}Name = "INVALID/NAME"
    
    when: "change is executed"
    executeChange(change)
    
    then: "appropriate exception is thrown"
    def e = thrown(DatabaseException)
    e.message.contains("Invalid object name")
}
```

## Integration Test Structure

### Complete Integration Test Template

```groovy
@LiquibaseIntegrationTest
@Stepwise
class ${Object}${Database}IntegrationTest extends Specification 
    implements TestDataHelper, ChangeExecutor, DatabaseAssertions {
    
    @Shared Database database
    @Shared DatabaseTestSystem testSystem
    
    def setupSpec() {
        testSystem = Scope.currentScope.getSingleton(TestSystemFactory)
            .getTestSystem("${database}")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(
                    new JdbcConnection(testSystem.connection)
                )
        }
    }
    
    def cleanupSpec() {
        cleanupTestObjects(database)
        database?.close()
    }
    
    def "create simple ${object}"() {
        given:
        def objectName = uniqueName("${OBJECT}")
        
        when:
        executeChange(new Create${Object}Change().tap {
            ${object}Name = objectName
        })
        
        then:
        objectExists(objectName)
        
        cleanup:
        dropObject(objectName)
    }
    
    def "create ${object} with all options"() {
        given:
        def objectName = uniqueName("${OBJECT}_FULL")
        
        when:
        executeChange(new Create${Object}Change().tap {
            ${object}Name = objectName
            attribute1 = "value1"
            attribute2 = 100
            attribute3 = true
        })
        
        then:
        def metadata = getObjectMetadata(objectName)
        metadata.attribute1 == "value1"
        metadata.attribute2 == 100
        metadata.attribute3 == true
        
        cleanup:
        dropObject(objectName)
    }
    
    def "alter existing ${object}"() {
        given: "an existing object"
        def objectName = uniqueName("${OBJECT}_ALTER")
        createObject(objectName)
        
        when: "alter is performed"
        executeChange(new Alter${Object}Change().tap {
            ${object}Name = objectName
            newAttribute1 = "updated"
        })
        
        then: "changes are applied"
        getObjectMetadata(objectName).attribute1 == "updated"
        
        cleanup:
        dropObject(objectName)
    }
    
    def "drop ${object} with cascade"() {
        given: "object with dependencies"
        def objectName = uniqueName("${OBJECT}_CASCADE")
        createObjectWithDependencies(objectName)
        
        when:
        executeChange(new Drop${Object}Change().tap {
            ${object}Name = objectName
            cascade = true
        })
        
        then:
        !objectExists(objectName)
        !dependenciesExist(objectName)
    }
    
    // Helper methods specific to object type
    private boolean objectExists(String name) {
        // Database-specific query
    }
    
    private Map getObjectMetadata(String name) {
        // Database-specific query
    }
    
    private void createObject(String name) {
        // Direct SQL creation for test setup
    }
    
    private void dropObject(String name) {
        try {
            database.execute([
                new RawSqlStatement("DROP ${OBJECT} IF EXISTS ${name}")
            ])
        } catch (Exception ignored) {
            // Ignore cleanup errors
        }
    }
}
```

## Test Harness Tests

### Changelog-Based Testing

`src/test/resources/changelogs/${database}/${object}/complete-test.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:${database}="http://www.liquibase.org/xml/ns/${database}"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/${database}
        http://www.liquibase.org/xml/ns/${database}/liquibase-${database}-latest.xsd">

    <!-- Preconditions -->
    <changeSet id="preconditions" author="test">
        <preConditions onFail="MARK_RAN">
            <dbms type="${database}"/>
        </preConditions>
    </changeSet>

    <!-- Test: Basic creation -->
    <changeSet id="create-basic" author="test">
        <${database}:create${Object} ${object}Name="TEST_BASIC"/>
        <rollback>
            <${database}:drop${Object} ${object}Name="TEST_BASIC"/>
        </rollback>
    </changeSet>

    <!-- Test: Creation with all attributes -->
    <changeSet id="create-full" author="test">
        <${database}:create${Object} 
            ${object}Name="TEST_FULL"
            attribute1="value1"
            attribute2="100"
            attribute3="true"/>
    </changeSet>

    <!-- Test: Conditional creation -->
    <changeSet id="create-conditional" author="test">
        <preConditions onFail="SKIP">
            <not>
                <objectExists objectName="TEST_CONDITIONAL" objectType="${object}"/>
            </not>
        </preConditions>
        <${database}:create${Object} ${object}Name="TEST_CONDITIONAL"/>
    </changeSet>

    <!-- Test: Alteration -->
    <changeSet id="alter-object" author="test">
        <${database}:alter${Object} 
            ${object}Name="TEST_FULL"
            newAttribute1="updated_value"/>
    </changeSet>

    <!-- Test: Complex scenario -->
    <changeSet id="complex-scenario" author="test">
        <${database}:create${Object} ${object}Name="TEST_PARENT"/>
        <${database}:create${Object} 
            ${object}Name="TEST_CHILD"
            parentObject="TEST_PARENT"/>
        <rollback>
            <${database}:drop${Object} ${object}Name="TEST_CHILD"/>
            <${database}:drop${Object} ${object}Name="TEST_PARENT"/>
        </rollback>
    </changeSet>

    <!-- Cleanup -->
    <changeSet id="cleanup" author="test" runAlways="true">
        <sql>
            DELETE FROM DATABASECHANGELOG 
            WHERE ID LIKE 'test-%' 
            AND FILENAME LIKE '%complete-test.xml'
        </sql>
    </changeSet>
</databaseChangeLog>
```

### Test Harness Test Class

```groovy
@LiquibaseIntegrationTest
class ${Object}ChangelogTest extends Specification {
    
    @Shared Liquibase liquibase
    @Shared Database database
    
    def setupSpec() {
        def testSystem = getTestSystem("${database}")
        if (testSystem?.shouldTest()) {
            database = createDatabase(testSystem.connection)
            liquibase = new Liquibase(
                "changelogs/${database}/${object}/complete-test.xml",
                new ClassLoaderResourceAccessor(),
                database
            )
        }
    }
    
    def "execute complete changelog"() {
        when: "changelog is executed"
        liquibase.update(new Contexts())
        
        then: "all objects are created"
        objectExists("TEST_BASIC")
        objectExists("TEST_FULL")
        objectExists("TEST_CONDITIONAL")
        
        when: "rollback two changes"
        liquibase.rollback(2, new Contexts())
        
        then: "recent changes are rolled back"
        !objectExists("TEST_CHILD")
        !objectExists("TEST_PARENT")
    }
}
```

## Performance Testing

### Load Test Template

```groovy
def "performance test for bulk operations"() {
    given: "multiple objects to create"
    def objectCount = 100
    def changes = (1..objectCount).collect { i ->
        new Create${Object}Change().tap {
            ${object}Name = "PERF_TEST_${i}"
        }
    }
    
    when: "all changes are executed"
    def startTime = System.currentTimeMillis()
    changes.each { executeChange(it) }
    def duration = System.currentTimeMillis() - startTime
    
    then: "execution completes within timeout"
    duration < 30000 // 30 seconds for 100 objects
    
    and: "all objects exist"
    (1..objectCount).every { i ->
        objectExists("PERF_TEST_${i}")
    }
    
    cleanup:
    database.execute([
        new RawSqlStatement("DROP ${OBJECT} IF EXISTS LIKE 'PERF_TEST_%'")
    ])
}
```

## Test Execution

### Running Tests

```bash
# Unit tests only
./mvnw test -pl liquibase-${database}

# Integration tests
./mvnw test -pl liquibase-integration-tests \
  -Dtest="*${Database}*" \
  -Dliquibase.sdk.testSystem.test=${database}

# Specific test class
./mvnw test -pl liquibase-integration-tests \
  -Dtest="${Object}${Database}IntegrationTest" \
  -Dliquibase.sdk.testSystem.test=${database}

# With debug output
./mvnw test -pl liquibase-integration-tests \
  -Dtest="*${Database}*" \
  -Dliquibase.sdk.testSystem.test=${database} \
  -Dliquibase.sql.logLevel=FINE
```

### Test Configuration

`liquibase-extension-testing/src/main/resources/liquibase.sdk.local.yaml`

```yaml
liquibase:
  sdk:
    testSystem:
      test: ${database}
      ${database}:
        url: jdbc:${database}://localhost:${port}/${database}
        username: test_user
        password: test_password
        # Database-specific configuration
        options:
          trustServerCertificate: true
          encrypt: false
```

## Test Quality Checklist

### Coverage Requirements

- [ ] Every public method has at least one test
- [ ] All validation rules are tested
- [ ] Success paths tested
- [ ] Failure paths tested
- [ ] Edge cases covered
- [ ] Null/empty handling tested
- [ ] SQL injection prevention tested

### Test Characteristics

- [ ] Tests are independent
- [ ] Tests are repeatable
- [ ] Tests clean up after themselves
- [ ] Test names clearly describe behavior
- [ ] Assertions are specific and meaningful
- [ ] No hard-coded values that might break

### Common Testing Mistakes to Avoid

1. **Testing Implementation, Not Behavior**
   ```groovy
   // Bad: Tests internal implementation
   assert change.@internalField == "value"
   
   // Good: Tests observable behavior
   assert change.generateStatements(db)[0].toSql().contains("value")
   ```

2. **Inadequate Cleanup**
   ```groovy
   // Bad: No cleanup
   def "test creation"() {
       when: executeChange(change)
       then: objectExists()
   }
   
   // Good: Always cleanup
   def "test creation"() {
       when: executeChange(change)
       then: objectExists()
       cleanup: dropObject()
   }
   ```

3. **Non-Unique Test Data**
   ```groovy
   // Bad: Fixed names cause conflicts
   change.objectName = "TEST_OBJECT"
   
   // Good: Unique names prevent conflicts
   change.objectName = uniqueName("TEST_OBJECT")
   ```

## Debugging Test Failures

### Enable Detailed Logging

```groovy
def setup() {
    // Enable SQL logging
    LogFactory.getLogger().setLevel(LogLevel.DEBUG)
    
    // Log generated SQL
    database.setOutputDefaultSchema(true)
    database.setOutputDefaultCatalog(true)
}
```

### Capture SQL for Analysis

```groovy
def "debug SQL generation"() {
    given:
    def recorder = new SqlRecorder()
    database = new RecordingDatabaseWrapper(database, recorder)
    
    when:
    executeChange(change)
    
    then:
    println "Executed SQL:"
    recorder.statements.each { println "  ${it}" }
    
    // Continue with assertions...
}
```

### Test Database State

```groovy
def "verify database state"() {
    when: "debugging test failure"
    
    then: "print current state"
    println "Objects in database:"
    def rs = database.connection.metaData.getTables(null, null, "TEST_%", null)
    while (rs.next()) {
        println "  Table: ${rs.getString('TABLE_NAME')}"
    }
}
```

## Summary

Effective testing is critical for rapid, high-quality development. By following these patterns:

1. Start with unit tests for fast feedback
2. Use integration tests to verify database behavior
3. Create comprehensive test scenarios
4. Maintain clean, independent tests
5. Debug systematically when tests fail

Remember: **Good tests enable confident refactoring and rapid development**. Invest in test quality to maximize development velocity.