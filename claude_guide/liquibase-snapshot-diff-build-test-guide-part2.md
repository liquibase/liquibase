# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Part 2: Snapshot Implementation

This document covers implementing the SnapshotGenerator to capture database objects from the database.

### Table of Contents
1. [Understanding SnapshotGenerator](#understanding-snapshotgenerator)
2. [Implementation Steps](#implementation-steps)
3. [SQL Query Patterns](#sql-query-patterns)
4. [Testing Strategies](#testing-strategies)
5. [Performance Considerations](#performance-considerations)

---

## Understanding SnapshotGenerator

### SnapshotGenerator Lifecycle

1. **Discovery Phase**: Liquibase calls `getPriority()` to find appropriate generators
2. **Object Snapshot**: `snapshotObject()` captures a specific object
3. **Bulk Snapshot**: `addTo()` captures all objects of a type

### Key Methods in Liquibase 4.33.0

```java
public abstract class SnapshotGenerator {
    // Priority constants
    public static final int PRIORITY_NONE = -1;
    public static final int PRIORITY_DEFAULT = 1;
    public static final int PRIORITY_DATABASE = 5;
    public static final int PRIORITY_ADDITIONAL = 50;

    // Determine if this generator handles the object type
    public abstract int getPriority(Class<? extends DatabaseObject> objectType, Database database);

    // Snapshot a specific object
    protected abstract DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
        throws DatabaseException;

    // Add all objects of this type to the snapshot
    protected abstract void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
        throws DatabaseException;

    // Define what types this generator adds objects to
    public abstract Class<? extends DatabaseObject>[] addsTo();

    // Define what generators this replaces (usually null)
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }
}
```

---

## Implementation Steps

### Step 2.1: Create the Unit Test

Create `src/test/groovy/.../snapshot/[ObjectType]SnapshotGeneratorTest.groovy`:

```groovy
package liquibase.ext.mydb.snapshot

import liquibase.database.Database
import liquibase.database.core.MySQLDatabase
import liquibase.ext.mydb.database.MyDatabase
import liquibase.ext.mydb.database.object.MyObject
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.SnapshotGenerator
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Schema
import spock.lang.Specification

import java.sql.ResultSet
import java.sql.SQLException

class MyObjectSnapshotGeneratorTest extends Specification {

    def generator = new MyObjectSnapshotGenerator()
    def database = Mock(MyDatabase)
    def snapshot = Mock(DatabaseSnapshot)

    def "test getPriority returns correct values"() {
        expect:
        generator.getPriority(objectType, db) == expectedPriority
        
        where:
        objectType      | db                   | expectedPriority
        MyObject.class  | new MyDatabase()     | SnapshotGenerator.PRIORITY_DATABASE
        MyObject.class  | new MySQLDatabase()  | SnapshotGenerator.PRIORITY_NONE
        Schema.class    | new MyDatabase()     | SnapshotGenerator.PRIORITY_NONE
    }

    def "test addsTo returns Schema class"() {
        expect:
        generator.addsTo() == [Schema.class] as Class[]
    }

    def "test snapshotObject with existing object"() {
        given:
        def resultSet = Mock(ResultSet)
        def example = new MyObject()
        example.setName("TEST_OBJECT")
        example.setSchema(new Schema("CATALOG", "SCHEMA"))
        
        when:
        // Mock the database query
        database.query(_ as String, _ as String) >> resultSet
        
        // Mock ResultSet behavior
        resultSet.next() >>> [true, false]  // First call returns true, second false
        resultSet.getString("name") >> "TEST_OBJECT"
        resultSet.getString("property1") >> "VALUE1"
        resultSet.getInt("property2") >> 42
        resultSet.getString("property3") >> "true"
        resultSet.getString("comment") >> "Test comment"
        resultSet.getString("state") >> "ACTIVE"
        resultSet.getString("owner") >> "ADMIN"
        resultSet.getTimestamp("created_on") >> new java.sql.Timestamp(System.currentTimeMillis())
        
        def result = generator.snapshotObject(example, snapshot)
        
        then:
        result != null
        result instanceof MyObject
        result.getName() == "TEST_OBJECT"
        result.getProperty1() == "VALUE1"
        result.getProperty2() == 42
        result.getProperty3() == true
        result.getComment() == "Test comment"
        result.getState() == "ACTIVE"
        result.getOwner() == "ADMIN"
        result.getCreatedOn() != null
    }

    def "test snapshotObject with non-existent object"() {
        given:
        def resultSet = Mock(ResultSet)
        def example = new MyObject()
        example.setName("NON_EXISTENT")
        
        when:
        database.query(_ as String, _ as String) >> resultSet
        resultSet.next() >> false  // No results
        
        def result = generator.snapshotObject(example, snapshot)
        
        then:
        result == null
    }

    def "test addTo for bulk snapshot"() {
        given:
        def resultSet = Mock(ResultSet)
        def schema = new Schema("CATALOG", "SCHEMA")
        
        when:
        database.query(_ as String) >> resultSet
        
        // Mock multiple objects
        resultSet.next() >>> [true, true, true, false]
        resultSet.getString("name") >>> ["OBJ1", "OBJ2", "OBJ3"]
        resultSet.getString("schema_name") >>> ["SCHEMA", "SCHEMA", "SCHEMA"]
        resultSet.getString("property1") >>> ["VALUE1", "VALUE2", "VALUE1"]
        resultSet.getInt("property2") >>> [10, 20, 30]
        // ... mock other properties
        
        snapshot.getDatabase() >> database
        
        generator.addTo(schema, snapshot)
        
        then:
        3 * snapshot.add(_ as MyObject)
    }

    def "test SQL injection protection"() {
        given:
        def example = new MyObject()
        example.setName("TEST'; DROP TABLE objects; --")
        
        when:
        generator.snapshotObject(example, snapshot)
        
        then:
        1 * database.query(
            { String sql -> sql.contains("?") || sql.contains("'TEST''; DROP TABLE") },
            _ as String
        )
    }

    def "test error handling"() {
        given:
        def example = new MyObject()
        example.setName("TEST")
        
        when:
        database.query(_ as String, _ as String) >> { 
            throw new SQLException("Connection lost") 
        }
        generator.snapshotObject(example, snapshot)
        
        then:
        thrown(DatabaseException)
    }
}
```

### Step 2.2: Implement the SnapshotGenerator

Create `src/main/java/.../snapshot/[ObjectType]SnapshotGenerator.java`:

```java
package liquibase.ext.mydb.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.DatabaseException;
import liquibase.ext.mydb.database.MyDatabase;
import liquibase.ext.mydb.database.object.MyObject;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyObjectSnapshotGenerator extends SnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof MyDatabase && MyObject.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] addsTo() {
        return new Class[] { Schema.class };
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        Database database = snapshot.getDatabase();
        MyObject myObject = (MyObject) example;
        
        // Build query to find specific object
        String query = buildObjectQuery(myObject, database);
        
        List<MyObject> objects = executeQuery(database, query);
        
        if (objects.isEmpty()) {
            return null;
        }
        
        // Return the first (should be only) match
        MyObject result = objects.get(0);
        result.setSchema(myObject.getSchema());
        return result;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        if (!(foundObject instanceof Schema)) {
            return;
        }
        
        Schema schema = (Schema) foundObject;
        Database database = snapshot.getDatabase();
        
        // Build query to find all objects in schema
        String query = buildSchemaQuery(schema, database);
        
        List<MyObject> objects = executeQuery(database, query);
        
        for (MyObject object : objects) {
            object.setSchema(schema);
            snapshot.add(object);
        }
    }

    private String buildObjectQuery(MyObject object, Database database) {
        // Database-specific query building
        if (database instanceof SnowflakeDatabase) {
            return String.format("SHOW OBJECTS LIKE '%s' IN SCHEMA %s.%s",
                escapeSql(object.getName()),
                escapeSql(object.getSchema().getCatalogName()),
                escapeSql(object.getSchema().getName())
            );
        } else {
            // Standard SQL approach
            return "SELECT * FROM INFORMATION_SCHEMA.OBJECTS WHERE OBJECT_NAME = ? AND SCHEMA_NAME = ?";
        }
    }

    private String buildSchemaQuery(Schema schema, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return String.format("SHOW OBJECTS IN SCHEMA %s.%s",
                escapeSql(schema.getCatalogName()),
                escapeSql(schema.getName())
            );
        } else {
            return "SELECT * FROM INFORMATION_SCHEMA.OBJECTS WHERE SCHEMA_NAME = ?";
        }
    }

    private List<MyObject> executeQuery(Database database, String query) throws DatabaseException {
        List<MyObject> objects = new ArrayList<>();
        
        try (ResultSet rs = database.query(new RawSqlStatement(query), new ArrayList<>())) {
            while (rs.next()) {
                MyObject object = parseResultSet(rs, database);
                if (object != null) {
                    objects.add(object);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error querying objects: " + e.getMessage(), e);
        }
        
        return objects;
    }

    private MyObject parseResultSet(ResultSet rs, Database database) throws SQLException {
        MyObject object = new MyObject();
        
        // Parse required properties
        object.setName(rs.getString("name"));
        
        // Parse optional properties with null handling
        String property1 = rs.getString("property1");
        if (property1 != null && !"null".equalsIgnoreCase(property1)) {
            object.setProperty1(property1);
        }
        
        // Handle numeric properties
        int property2 = rs.getInt("property2");
        if (!rs.wasNull()) {
            object.setProperty2(property2);
        }
        
        // Handle boolean properties (database-specific)
        object.setProperty3(parseBoolean(rs.getString("property3")));
        
        // Handle comments
        String comment = rs.getString("comment");
        if (!StringUtil.isEmpty(comment) && !"null".equalsIgnoreCase(comment)) {
            object.setComment(comment);
        }
        
        // Parse state properties
        object.setState(rs.getString("state"));
        object.setOwner(rs.getString("owner"));
        object.setCreatedOn(rs.getTimestamp("created_on"));
        
        return object;
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        // Handle various boolean representations
        return "true".equalsIgnoreCase(value) || 
               "yes".equalsIgnoreCase(value) || 
               "y".equalsIgnoreCase(value) ||
               "1".equals(value);
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "NULL";
        }
        // Basic SQL injection protection
        return value.replace("'", "''");
    }
}
```

---

## SQL Query Patterns

### Pattern 1: SHOW Commands (Snowflake, MySQL)

```java
// For specific object
"SHOW OBJECTS LIKE 'OBJECT_NAME' IN SCHEMA schema_name"

// For all objects in schema
"SHOW OBJECTS IN SCHEMA schema_name"

// Parse results carefully - column names may vary
rs.getString("name")      // or "Name" or "OBJECT_NAME"
rs.getString("created_on") // or "created" or "CREATED_ON"
```

### Pattern 2: Information Schema (PostgreSQL, SQL Server)

```java
// For specific object
"SELECT * FROM information_schema.objects 
 WHERE object_name = ? AND schema_name = ?"

// For all objects in schema
"SELECT * FROM information_schema.objects 
 WHERE schema_name = ? 
 ORDER BY object_name"
```

### Pattern 3: System Tables (Oracle)

```java
// For specific object
"SELECT * FROM ALL_OBJECTS 
 WHERE OBJECT_NAME = ? AND OWNER = ?"

// For all objects
"SELECT * FROM ALL_OBJECTS 
 WHERE OWNER = ? AND OBJECT_TYPE = 'MY_TYPE'"
```

### Handling Database-Specific Differences

```java
private String getObjectsQuery(Database database) {
    if (database instanceof SnowflakeDatabase) {
        return "SHOW WAREHOUSES";
    } else if (database instanceof PostgresDatabase) {
        return "SELECT * FROM pg_warehouses";
    } else if (database instanceof OracleDatabase) {
        return "SELECT * FROM DBA_WAREHOUSES";
    } else {
        throw new DatabaseException("Unsupported database type");
    }
}
```

---

## Testing Strategies

### Integration Test

Create `src/test/groovy/.../snapshot/[ObjectType]SnapshotGeneratorIntegrationTest.groovy`:

```groovy
package liquibase.ext.mydb.snapshot

import liquibase.Scope
import liquibase.database.DatabaseFactory
import liquibase.ext.mydb.database.MyDatabase
import liquibase.ext.mydb.database.object.MyObject
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.statement.core.RawSqlStatement
import liquibase.structure.core.Schema
import spock.lang.Requires
import spock.lang.Specification

@Requires({ System.getenv("TEST_DB_URL") })
class MyObjectSnapshotGeneratorIntegrationTest extends Specification {

    def database
    
    def setup() {
        database = DatabaseFactory.getInstance().openDatabase(
            System.getenv("TEST_DB_URL"),
            System.getenv("TEST_DB_USERNAME"),
            System.getenv("TEST_DB_PASSWORD"),
            null,
            null
        )
    }
    
    def cleanup() {
        if (database?.getConnection()?.isClosed() == false) {
            database.close()
        }
    }
    
    def "test snapshot existing object"() {
        given:
        // Create test object
        database.execute([new RawSqlStatement("""
            CREATE OBJECT IF NOT EXISTS TEST_SNAPSHOT_OBJ
            WITH PROPERTY1 = 'VALUE1'
                 PROPERTY2 = 42
                 COMMENT = 'Integration test object'
        """)])
        
        when:
        def example = new MyObject()
        example.setName("TEST_SNAPSHOT_OBJ")
        example.setSchema(new Schema(database.getDefaultCatalogName(), database.getDefaultSchemaName()))
        
        def snapshotControl = new SnapshotControl(database, MyObject.class)
        def snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            example, database, snapshotControl
        )
        
        def object = snapshot.get(example)
        
        then:
        object != null
        object.getName() == "TEST_SNAPSHOT_OBJ"
        object.getProperty1() == "VALUE1"
        object.getProperty2() == 42
        object.getComment() == "Integration test object"
        object.getState() != null
        object.getOwner() != null
        object.getCreatedOn() != null
        
        cleanup:
        database.execute([new RawSqlStatement("DROP OBJECT IF EXISTS TEST_SNAPSHOT_OBJ")])
    }
    
    def "test snapshot all objects in schema"() {
        given:
        // Create multiple test objects
        (1..3).each { i ->
            database.execute([new RawSqlStatement("""
                CREATE OBJECT IF NOT EXISTS TEST_BULK_OBJ_${i}
                WITH PROPERTY1 = 'VALUE${i}'
            """)])
        }
        
        when:
        def schema = new Schema(database.getDefaultCatalogName(), database.getDefaultSchemaName())
        def snapshotControl = new SnapshotControl(database, MyObject.class)
        def snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            schema, database, snapshotControl
        )
        
        def objects = snapshot.get(MyObject.class)
        def testObjects = objects.findAll { it.getName().startsWith("TEST_BULK_OBJ_") }
        
        then:
        testObjects.size() == 3
        testObjects.any { it.getName() == "TEST_BULK_OBJ_1" && it.getProperty1() == "VALUE1" }
        testObjects.any { it.getName() == "TEST_BULK_OBJ_2" && it.getProperty1() == "VALUE2" }
        testObjects.any { it.getName() == "TEST_BULK_OBJ_3" && it.getProperty1() == "VALUE3" }
        
        cleanup:
        (1..3).each { i ->
            database.execute([new RawSqlStatement("DROP OBJECT IF EXISTS TEST_BULK_OBJ_${i}")])
        }
    }
    
    def "test snapshot non-existent object returns null"() {
        when:
        def example = new MyObject()
        example.setName("NON_EXISTENT_OBJECT_XYZ123")
        example.setSchema(new Schema(database.getDefaultCatalogName(), database.getDefaultSchemaName()))
        
        def snapshotControl = new SnapshotControl(database, MyObject.class)
        def snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
            example, database, snapshotControl
        )
        
        def object = snapshot.get(example)
        
        then:
        object == null
    }
}
```

---

## Performance Considerations

### 1. Query Optimization

```java
// BAD: N+1 queries
for (String objectName : objectNames) {
    MyObject obj = queryObject(objectName);
    snapshot.add(obj);
}

// GOOD: Batch query
List<MyObject> objects = queryAllObjects();
for (MyObject obj : objects) {
    snapshot.add(obj);
}
```

### 2. Result Set Handling

```java
// Use try-with-resources for automatic cleanup
try (ResultSet rs = database.query(statement, params)) {
    while (rs.next()) {
        // Process row
    }
} catch (SQLException e) {
    throw new DatabaseException(e);
}
```

### 3. Memory Management

```java
// For large result sets, consider streaming
public void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) {
    // Process in batches to avoid memory issues
    int batchSize = 1000;
    int offset = 0;
    
    while (true) {
        List<MyObject> batch = queryBatch(offset, batchSize);
        if (batch.isEmpty()) {
            break;
        }
        
        for (MyObject obj : batch) {
            snapshot.add(obj);
        }
        
        offset += batchSize;
    }
}
```

### 4. Caching Considerations

```java
// SnapshotGeneratorFactory handles caching
// Don't cache in your generator - let the framework handle it
```

---

## Service Registration

Don't forget to register your generator in `META-INF/services/liquibase.snapshot.SnapshotGenerator`:

```
liquibase.ext.mydb.snapshot.MyObjectSnapshotGenerator
```

---

## Common Issues and Solutions

### Issue 1: Column Name Case Sensitivity

```java
// Some databases return uppercase column names
String name = rs.getString("NAME"); // Not "name"

// Solution: Use column index or handle both cases
String name = getStringCaseInsensitive(rs, "name");

private String getStringCaseInsensitive(ResultSet rs, String columnName) {
    try {
        return rs.getString(columnName);
    } catch (SQLException e) {
        try {
            return rs.getString(columnName.toUpperCase());
        } catch (SQLException e2) {
            return rs.getString(columnName.toLowerCase());
        }
    }
}
```

### Issue 2: Null vs Empty vs "null" String

```java
// Databases may return the string "null" instead of SQL NULL
String value = rs.getString("column");
if (value != null && !"null".equalsIgnoreCase(value) && !value.isEmpty()) {
    object.setValue(value);
}
```

### Issue 3: Database-Specific Boolean Values

```java
private Boolean parseBoolean(String value, Database database) {
    if (value == null) return null;
    
    if (database instanceof OracleDatabase) {
        return "1".equals(value) || "Y".equalsIgnoreCase(value);
    } else if (database instanceof PostgresDatabase) {
        return "t".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    } else {
        return Boolean.parseBoolean(value);
    }
}
```

---

## Testing Checklist

- [ ] Unit tests for all public methods
- [ ] Mock ResultSet behavior correctly
- [ ] Test null/empty value handling
- [ ] Test SQL injection protection
- [ ] Integration test with real database
- [ ] Test object exists scenario
- [ ] Test object doesn't exist scenario
- [ ] Test bulk snapshot (addTo method)
- [ ] Performance test with many objects
- [ ] Test database-specific SQL variations

---

## Next Steps

With your SnapshotGenerator complete:
1. Run all unit tests
2. Run integration tests against real database
3. Verify objects are captured with all properties
4. Check performance with realistic data volumes
5. Proceed to **Part 3: Diff Implementation**
