# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Main Document - Overview and Setup

This guide provides a comprehensive, test-driven development (TDD) approach to implementing snapshot and diff capabilities for new database objects in Liquibase 4.33.0 extensions. 

### Document Structure

This implementation guide is split into the following documents:

1. **Main Document** (this document) - Overview, prerequisites, and initial setup
2. **Part 1: Database Object Model** - Research, requirements, and object model implementation
3. **Part 2: Snapshot Implementation** - SnapshotGenerator implementation and testing
4. **Part 3: Diff Implementation** - Comparator and DiffGenerator implementation
5. **Part 4: Testing Guide** - Comprehensive testing strategies and examples
6. **Part 5: Reference Implementation** - Complete Snowflake Warehouse example

### Version Compatibility

This guide is specifically written for **Liquibase 4.33.0** and uses the following key APIs:
- `AbstractDatabaseObject` for database object modeling
- `SnapshotGenerator` for capturing database state
- `DatabaseObjectComparator` for object comparison
- `MissingObjectChangeGenerator`, `UnexpectedObjectChangeGenerator`, and `ChangedObjectChangeGenerator` for diff output

### Prerequisites

- Liquibase 4.33.0 core libraries
- Existing Liquibase database extension project
- Java 8 or higher
- Gradle or Maven build system
- Unit test infrastructure (Spock/Groovy recommended)
- Integration test infrastructure
- Test harness infrastructure
- Access to target database for testing

### Project Structure

Your Liquibase extension should follow this structure:
```
liquibase-[database]-extension/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── liquibase/
│   │   │       └── ext/
│   │   │           └── [database]/
│   │   │               ├── database/
│   │   │               │   └── object/
│   │   │               │       └── [ObjectType].java
│   │   │               ├── snapshot/
│   │   │               │   └── [ObjectType]SnapshotGenerator.java
│   │   │               ├── diff/
│   │   │               │   └── output/
│   │   │               │       ├── [ObjectType]Comparator.java
│   │   │               │       └── [ObjectType]DiffGenerator.java
│   │   │               └── change/
│   │   │                   ├── Create[ObjectType]Change.java
│   │   │                   ├── Alter[ObjectType]Change.java
│   │   │                   └── Drop[ObjectType]Change.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── services/
│   │               ├── liquibase.snapshot.SnapshotGenerator
│   │               ├── liquibase.diff.compare.DatabaseObjectComparator
│   │               └── liquibase.diff.output.changelog.ChangeGenerator
│   └── test/
│       ├── groovy/
│       │   └── liquibase/
│       │       └── ext/
│       │           └── [database]/
│       │               ├── database/object/
│       │               ├── snapshot/
│       │               ├── diff/output/
│       │               └── harness/
│       └── resources/
└── build.gradle
```

### Service Registration

For Liquibase 4.33.0 to discover your extensions, you must register them in the appropriate service files:

#### META-INF/services/liquibase.snapshot.SnapshotGenerator
```
liquibase.ext.[database].snapshot.[ObjectType]SnapshotGenerator
```

#### META-INF/services/liquibase.diff.compare.DatabaseObjectComparator
```
liquibase.ext.[database].diff.output.[ObjectType]Comparator
```

#### META-INF/services/liquibase.diff.output.changelog.ChangeGenerator
```
liquibase.ext.[database].diff.output.[ObjectType]DiffGenerator
```

### Key Liquibase 4.33.0 APIs

#### AbstractDatabaseObject
The base class for all database objects. Key methods:
- `setSnapshotId(String)` - Define the unique identifier field(s)
- `getAttribute(String, Class)` / `setAttribute(String, Object)` - Generic attribute storage
- `getName()` / `setName(String)` - Standard naming interface
- `getContainingObjects()` - Define object hierarchy

#### SnapshotGenerator Priority System
```java
public static final int PRIORITY_NONE = -1;
public static final int PRIORITY_DEFAULT = 1;
public static final int PRIORITY_DATABASE = 5;
public static final int PRIORITY_ADDITIONAL = 50;
```

#### DatabaseObjectComparator Methods
- `getPriority(Class, Database)` - Determine if comparator handles this object type
- `isSameObject(DatabaseObject, DatabaseObject, Database, Chain)` - Identity comparison
- `findDifferences(DatabaseObject, DatabaseObject, Database, CompareControl, Chain, Set)` - Detailed comparison

### Development Workflow

1. **Research** - Document all properties and behaviors of your database object
2. **Model** - Create the DatabaseObject class with all properties
3. **Snapshot** - Implement SnapshotGenerator to read from database
4. **Compare** - Implement Comparator to identify differences
5. **Generate** - Implement DiffGenerator to create change objects
6. **Test** - Unit, integration, and harness testing at each step

### Testing Strategy

This guide emphasizes Test-Driven Development (TDD):
1. Write unit tests before implementation
2. Use mocks for fast, isolated testing
3. Write integration tests with real databases
4. Use test harness for end-to-end validation
5. Include edge cases and negative tests
6. Perform performance testing with realistic data volumes

### Common Pitfalls in Liquibase 4.33.0

1. **Service Registration** - Forgetting META-INF/services files
2. **Priority Values** - Using wrong priority constants
3. **Null Handling** - Not properly handling null vs empty values
4. **Case Sensitivity** - Database-specific identifier handling
5. **State vs Configuration** - Mixing runtime state with configuration
6. **Chain Delegation** - Not properly using comparator/generator chains

### Next Steps

Continue with **Part 1: Database Object Model** to begin implementing your database object type.

### Quick Reference

```java
// Database Object
public class MyObject extends AbstractDatabaseObject {
    public MyObject() {
        setSnapshotId("name"); // or composite: "schema,name"
    }
}

// Snapshot Generator
public class MyObjectSnapshotGenerator extends SnapshotGenerator {
    @Override
    public int getPriority(Class<?> objectType, Database database) {
        return database instanceof MyDatabase && 
               MyObject.class.isAssignableFrom(objectType) 
               ? PRIORITY_DATABASE : PRIORITY_NONE;
    }
}

// Comparator
public class MyObjectComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<?> objectType, Database database) {
        return MyObject.class.isAssignableFrom(objectType) && 
               database instanceof MyDatabase 
               ? PRIORITY_DATABASE : PRIORITY_NONE;
    }
}
```
