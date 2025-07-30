# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Part 3: Diff Implementation

This document covers implementing the Comparator and DiffGenerator for detecting and generating changes between database objects.

### Table of Contents
1. [Understanding the Diff Process](#understanding-the-diff-process)
2. [Implementing the Comparator](#implementing-the-comparator)
3. [Implementing the DiffGenerator](#implementing-the-diffgenerator)
4. [Change Type Integration](#change-type-integration)
5. [Testing Diff Components](#testing-diff-components)

---

## Understanding the Diff Process

### Diff Workflow in Liquibase 4.33.0

1. **Snapshot Phase**: Both databases are snapshotted
2. **Comparison Phase**: Objects are compared using Comparators
3. **Difference Detection**: Changes are categorized as:
   - Missing (exists in reference, not in target)
   - Unexpected (exists in target, not in reference)
   - Changed (exists in both but different)
4. **Change Generation**: DiffGenerator creates Change objects

### Key Interfaces

```java
// Comparator - Identifies differences
public interface DatabaseObjectComparator {
    int getPriority(Class<? extends DatabaseObject> objectType, Database database);
    String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain);
    boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                        Database accordingTo, DatabaseObjectComparatorChain chain);
    ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                     Database accordingTo, CompareControl compareControl,
                                     DatabaseObjectComparatorChain chain, Set<String> exclude);
}

// DiffGenerator - Creates changes
public interface MissingObjectChangeGenerator {
    Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, 
                       DiffResult diffResult, ChangeGeneratorChain chain);
}

public interface UnexpectedObjectChangeGenerator {
    Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, 
                          DiffResult diffResult, ChangeGeneratorChain chain);
}

public interface ChangedObjectChangeGenerator {
    Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, 
                       DiffOutputControl control, DiffResult diffResult, 
                       ChangeGeneratorChain chain);
}
```

---

## Implementing the Comparator

### Step 3.1: Create Comparator Unit Test

Create `src/test/groovy/.../diff/output/[ObjectType]ComparatorTest.groovy`:

```groovy
package liquibase.ext.mydb.diff.output

import liquibase.database.Database
import liquibase.database.core.PostgresDatabase
import liquibase.diff.compare.CompareControl
import liquibase.diff.compare.DatabaseObjectComparator
import liquibase.diff.compare.DatabaseObjectComparatorChain
import liquibase.ext.mydb.database.MyDatabase
import liquibase.ext.mydb.database.object.MyObject
import liquibase.structure.core.Schema
import spock.lang.Specification
import spock.lang.Unroll

class MyObjectComparatorTest extends Specification {

    def comparator = new MyObjectComparator()
    def database = Mock(MyDatabase)
    def chain = Mock(DatabaseObjectComparatorChain)
    def compareControl = new CompareControl()
    
    def "test getPriority returns correct values"() {
        expect:
        comparator.getPriority(objectType, db) == expectedPriority
        
        where:
        objectType      | db                   | expectedPriority
        MyObject.class  | new MyDatabase()     | DatabaseObjectComparator.PRIORITY_DATABASE
        MyObject.class  | new PostgresDatabase() | DatabaseObjectComparator.PRIORITY_NONE
        Schema.class    | new MyDatabase()     | DatabaseObjectComparator.PRIORITY_NONE
    }
    
    def "test hash returns object identifier"() {
        given:
        def obj = new MyObject()
        obj.setName("TEST_OBJ")
        obj.setSchema(new Schema("CATALOG", "SCHEMA"))
        
        when:
        def hash = comparator.hash(obj, database, chain)
        
        then:
        hash == ["TEST_OBJ"] as String[]
    }
    
    def "test isSameObject with matching objects"() {
        given:
        def obj1 = new MyObject()
        obj1.setName("TEST_OBJ")
        obj1.setSchema(new Schema("CAT", "SCHEMA"))
        
        def obj2 = new MyObject()
        obj2.setName("TEST_OBJ")
        obj2.setSchema(new Schema("CAT", "SCHEMA"))
        
        expect:
        comparator.isSameObject(obj1, obj2, database, chain)
    }
    
    def "test isSameObject with different names"() {
        given:
        def obj1 = new MyObject()
        obj1.setName("OBJ1")
        
        def obj2 = new MyObject()
        obj2.setName("OBJ2")
        
        expect:
        !comparator.isSameObject(obj1, obj2, database, chain)
    }
    
    def "test isSameObject with case differences"() {
        given:
        def obj1 = new MyObject()
        obj1.setName("test_obj")
        
        def obj2 = new MyObject()
        obj2.setName("TEST_OBJ")
        
        when:
        database.isCaseSensitive() >> false
        
        then:
        comparator.isSameObject(obj1, obj2, database, chain)
    }
    
    @Unroll
    def "test findDifferences for #property change"() {
        given:
        def reference = new MyObject()
        reference.setName("TEST_OBJ")
        reference."set${property}"(referenceValue)
        
        def comparison = new MyObject()
        comparison.setName("TEST_OBJ")
        comparison."set${property}"(comparisonValue)
        
        when:
        def differences = comparator.findDifferences(
            reference, comparison, database, compareControl, chain, new HashSet()
        )
        
        then:
        if (shouldDetectDifference) {
            differences.hasDifferences()
            def diff = differences.getDifference(property.uncapitalize())
            diff != null
            diff.referenceValue == referenceValue
            diff.comparedValue == comparisonValue
        } else {
            !differences.hasDifferences()
        }
        
        where:
        property     | referenceValue | comparisonValue | shouldDetectDifference
        "Property1"  | "VALUE1"       | "VALUE2"        | true
        "Property1"  | "VALUE1"       | "VALUE1"        | false
        "Property1"  | null           | "VALUE1"        | true
        "Property1"  | "VALUE1"       | null            | true
        "Property2"  | 10             | 20              | true
        "Property2"  | 10             | 10              | false
        "Property3"  | true           | false           | true
        "Property3"  | true           | true            | false
        "Comment"    | "Old"          | "New"           | true
        "Comment"    | null           | null            | false
    }
    
    def "test excluded fields are not compared"() {
        given:
        def reference = new MyObject()
        reference.setName("TEST_OBJ")
        reference.setState("ACTIVE")
        reference.setOwner("USER1")
        reference.setCreatedOn(new Date())
        
        def comparison = new MyObject()
        comparison.setName("TEST_OBJ")
        comparison.setState("INACTIVE")  // Different
        comparison.setOwner("USER2")     // Different
        comparison.setCreatedOn(new Date() + 1)  // Different
        
        when:
        def differences = comparator.findDifferences(
            reference, comparison, database, compareControl, chain, new HashSet()
        )
        
        then:
        !differences.hasDifferences()  // State fields should be excluded
    }
    
    def "test custom exclusions"() {
        given:
        def reference = new MyObject()
        reference.setName("TEST_OBJ")
        reference.setProperty1("VALUE1")
        reference.setProperty2(10)
        
        def comparison = new MyObject()
        comparison.setName("TEST_OBJ")
        comparison.setProperty1("VALUE2")  // Different
        comparison.setProperty2(20)        // Different
        
        when:
        def exclusions = new HashSet(["property1"])
        def differences = comparator.findDifferences(
            reference, comparison, database, compareControl, chain, exclusions
        )
        
        then:
        differences.hasDifferences()
        !differences.hasDifference("property1")  // Excluded
        differences.hasDifference("property2")   // Not excluded
    }
    
    def "test null object handling"() {
        when:
        comparator.isSameObject(null, new MyObject(), database, chain)
        
        then:
        thrown(NullPointerException)
    }
}
```

### Step 3.2: Implement the Comparator

Create `src/main/java/.../diff/output/[ObjectType]Comparator.java`:

```java
package liquibase.ext.mydb.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.ext.mydb.database.MyDatabase;
import liquibase.ext.mydb.database.object.MyObject;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MyObjectComparator implements DatabaseObjectComparator {
    
    // Fields that should not trigger differences (runtime state)
    private static final String[] EXCLUDED_FIELDS = {
        "state", "createdOn", "updatedOn", "owner", 
        "lastAccessedOn", "currentUsers", "isActive"
    };

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (MyObject.class.isAssignableFrom(objectType) && database instanceof MyDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        MyObject myObject = (MyObject) databaseObject;
        // Return the unique identifier(s)
        return new String[] { myObject.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof MyObject && databaseObject2 instanceof MyObject)) {
            return false;
        }
        
        MyObject obj1 = (MyObject) databaseObject1;
        MyObject obj2 = (MyObject) databaseObject2;
        
        // Compare identifiers
        String name1 = obj1.getName();
        String name2 = obj2.getName();
        
        if (name1 == null || name2 == null) {
            return false;
        }
        
        // Handle case sensitivity
        if (accordingTo != null && !accordingTo.isCaseSensitive()) {
            return name1.equalsIgnoreCase(name2);
        } else {
            return name1.equals(name2);
        }
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                           Database accordingTo, CompareControl compareControl,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        // Add our excluded fields to the exclusion set
        exclude = new HashSet<>(exclude);
        exclude.addAll(Arrays.asList(EXCLUDED_FIELDS));
        
        // Let the chain handle the comparison with our exclusions
        ObjectDifferences differences = chain.findDifferences(
            databaseObject1, databaseObject2, accordingTo, compareControl, exclude
        );
        
        // Add any custom comparison logic here
        MyObject obj1 = (MyObject) databaseObject1;
        MyObject obj2 = (MyObject) databaseObject2;
        
        // Example: Custom comparison for complex properties
        if (shouldCompareComplexProperty(compareControl)) {
            compareComplexProperty(obj1, obj2, differences);
        }
        
        return differences;
    }
    
    private boolean shouldCompareComplexProperty(CompareControl compareControl) {
        // Check if we should compare this property based on CompareControl settings
        return compareControl.getSchemaComparisons() != null && 
               compareControl.getSchemaComparisons().length > 0;
    }
    
    private void compareComplexProperty(MyObject obj1, MyObject obj2, ObjectDifferences differences) {
        // Example: Compare a property that needs special handling
        // This is where you'd handle properties that can't be compared with simple equals()
    }
}