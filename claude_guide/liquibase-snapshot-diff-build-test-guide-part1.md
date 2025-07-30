# Liquibase 4.33.0 Snapshot and Diff Implementation Guide
## Part 1: Database Object Model

This document covers the research phase and database object model implementation.

### Table of Contents
1. [Research and Requirements Gathering](#research-and-requirements-gathering)
2. [Database Object Model Implementation](#database-object-model-implementation)
3. [Property Normalization](#property-normalization)
4. [Testing the Object Model](#testing-the-object-model)

---

## Research and Requirements Gathering

### Step 1.1: Document Database Object Specifications

Before implementing, thoroughly research your database object type:

1. **Official Documentation**
   - Navigate to your database vendor's documentation
   - Find CREATE, ALTER, DROP, and SHOW/DESCRIBE commands
   - Document ALL properties, including:
     - Required properties
     - Optional properties with defaults
     - Read-only/state properties
     - Constraints and valid values

2. **Create Requirements Document**
   ```markdown
   # [Object Type] Requirements Specification
   
   ## Overview
   [Brief description of the object type]
   
   ## Properties
   ### Required Properties
   - **property_name**: type - description
   
   ### Optional Properties with Defaults
   - **property_name**: type - Default: value
     - Valid values: [list]
     - Constraints: [description]
   
   ### State Properties (Read-only)
   - **property_name**: type - description
   
   ## SQL Commands
   ### Creation
   ```sql
   CREATE [OBJECT_TYPE] [IF NOT EXISTS] <name>
     [property = value]
     ...
   ```
   
   ### Alteration
   ```sql
   ALTER [OBJECT_TYPE] [IF EXISTS] <name> SET ...
   ALTER [OBJECT_TYPE] [IF EXISTS] <name> UNSET ...
   ```
   
   ### Dropping
   ```sql
   DROP [OBJECT_TYPE] [IF EXISTS] <name>
   ```
   
   ### Querying
   ```sql
   SHOW [OBJECT_TYPE]S [LIKE '<pattern>']
   DESCRIBE [OBJECT_TYPE] <name>
   ```
   
   ## Special Considerations
   [List any special behaviors, constraints, or gotchas]
   ```

### Example: Snowflake Warehouse Research

Here's a condensed example for Snowflake warehouses:

```markdown
# Snowflake Warehouse Requirements

## Properties
### Required
- **name**: String - Unique identifier

### Optional with Defaults
- **warehouse_size**: String - Default: 'X-SMALL'
  - Valid: 'X-SMALL', 'SMALL', 'MEDIUM', 'LARGE', 'X-LARGE', '2X-LARGE', etc.
- **warehouse_type**: String - Default: 'STANDARD'
  - Valid: 'STANDARD', 'SNOWPARK-OPTIMIZED'
- **auto_suspend**: Integer - Default: 600 seconds
  - Minimum: 60 (or 0 to disable)
- **auto_resume**: Boolean - Default: true

### State (Read-only)
- **state**: String - 'STARTED', 'SUSPENDED', 'RESIZING'
- **owner**: String - Owner role name
- **created_on**: Timestamp
```

---

## Database Object Model Implementation

### Step 1.2: Create the Database Object Class

#### First, Write the Test

Create `src/test/groovy/.../database/object/[ObjectType]Test.groovy`:

```groovy
package liquibase.ext.mydb.database.object

import spock.lang.Specification
import spock.lang.Unroll
import liquibase.structure.core.Schema

class MyObjectTest extends Specification {

    def "test object creation with minimal properties"() {
        when:
        def obj = new MyObject()
        obj.setName("TEST_OBJ")
        
        then:
        obj.getName() == "TEST_OBJ"
        obj.getSnapshotId() == "TEST_OBJ"
    }

    def "test object with all properties"() {
        when:
        def obj = new MyObject()
        obj.setName("TEST_OBJ")
        obj.setProperty1("value1")
        obj.setProperty2(42)
        obj.setProperty3(true)
        
        then:
        obj.getName() == "TEST_OBJ"
        obj.getProperty1() == "value1"
        obj.getProperty2() == 42
        obj.getProperty3() == true
    }

    def "test equals and hashCode"() {
        given:
        def obj1 = new MyObject()
        obj1.setName("TEST_OBJ")
        obj1.setSchema(new Schema("CATALOG", "SCHEMA"))
        
        def obj2 = new MyObject()
        obj2.setName("TEST_OBJ")
        obj2.setSchema(new Schema("CATALOG", "SCHEMA"))
        
        def obj3 = new MyObject()
        obj3.setName("OTHER_OBJ")
        obj3.setSchema(new Schema("CATALOG", "SCHEMA"))
        
        expect:
        obj1.equals(obj2)
        obj1.hashCode() == obj2.hashCode()
        !obj1.equals(obj3)
        obj1.hashCode() != obj3.hashCode()
    }

    @Unroll
    def "test property validation: #scenario"() {
        given:
        def obj = new MyObject()
        
        when:
        obj.setName(name)
        obj.setProperty1(property1)
        
        then:
        if (shouldThrow) {
            thrown(expectedException)
        } else {
            obj.getName() == expectedName
            obj.getProperty1() == expectedProperty1

        }
        
        where:
        scenario               | name      | property1  | shouldThrow | expectedException        | expectedName | expectedProperty1
        "valid values"         | "VALID"   | "VALUE1"   | false       | null                    | "VALID"      | "VALUE1"
        "null name"           | null      | "VALUE1"   | true        | NullPointerException    | null         | null
        "empty name"          | ""        | "VALUE1"   | true        | IllegalArgumentException | null         | null
        "invalid enum value"  | "VALID"   | "INVALID"  | true        | IllegalArgumentException | null         | null
    }
}
```

#### Then, Implement the Object

Create `src/main/java/.../database/object/[ObjectType].java`:

```java
package liquibase.ext.mydb.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MyObject extends AbstractDatabaseObject {
    
    // Define valid values for enum-like properties
    private static final List<String> VALID_PROPERTY1_VALUES = Arrays.asList(
        "VALUE1", "VALUE2", "VALUE3"
    );
    
    // Required properties
    private String name;
    
    // Optional configuration properties
    private String property1;
    private Integer property2;
    private Boolean property3;
    private String comment;
    
    // State properties (read-only, populated by snapshot)
    private String state;
    private Date createdOn;
    private String owner;
    
    public MyObject() {
        // Define what makes this object unique
        // For single field: setSnapshotId("name")
        // For composite: setSnapshotId("schema,name")
        setSnapshotId("name");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MyObject setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name;
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        // Define the hierarchy - what contains this object?
        return new DatabaseObject[] { getSchema() };
    }

    @Override
    public Schema getSchema() {
        return getAttribute("schema", Schema.class);
    }

    public MyObject setSchema(Schema schema) {
        setAttribute("schema", schema);
        return this;
    }

    // Property with validation
    public String getProperty1() {
        return property1;
    }

    public void setProperty1(String property1) {
        if (property1 != null && !VALID_PROPERTY1_VALUES.contains(property1.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid value for property1: " + property1 + 
                ". Valid values are: " + VALID_PROPERTY1_VALUES
            );
        }
        this.property1 = property1 != null ? property1.toUpperCase() : null;
    }

    // Property with normalization
    public Integer getProperty2() {
        return property2;
    }

    public void setProperty2(Integer property2) {
        if (property2 != null && property2 < 0) {
            throw new IllegalArgumentException("Property2 cannot be negative");
        }
        this.property2 = property2;
    }

    // Simple property
    public Boolean getProperty3() {
        return property3;
    }

    public void setProperty3(Boolean property3) {
        this.property3 = property3;
    }

    // Comment property (common pattern)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (comment != null && comment.length() > 255) {
            throw new IllegalArgumentException("Comment cannot exceed 255 characters");
        }
        this.comment = comment;
    }

    // State properties (read-only)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "MyObject{" +
               "name='" + name + '\'' +
               ", property1='" + property1 + '\'' +
               ", property2=" + property2 +
               ", property3=" + property3 +
               ", state='" + state + '\'' +
               '}';
    }
}
```

---

## Property Normalization

### Common Normalization Patterns

1. **Case Normalization**
   ```java
   public void setPropertyName(String value) {
       // Store identifiers in uppercase (database-specific)
       this.propertyName = value != null ? value.toUpperCase() : null;
   }
   ```

2. **Value Mapping**
   ```java
   public void setSize(String size) {
       if (size != null) {
           // Map alternative names to canonical form
           size = size.toUpperCase()
               .replace("XSMALL", "X-SMALL")
               .replace("XLARGE", "X-LARGE");
       }
       this.size = size;
   }
   ```

3. **Boolean Parsing**
   ```java
   private Boolean parseBoolean(String value) {
       if (value == null) return null;
       return "true".equalsIgnoreCase(value) || 
              "yes".equalsIgnoreCase(value) || 
              "y".equalsIgnoreCase(value) ||
              "1".equals(value);
   }
   ```

4. **Null vs Default Handling**
   ```java
   public Integer getTimeout() {
       // Return null for "no timeout" rather than 0
       return (timeout != null && timeout == 0) ? null : timeout;
   }
   ```

---

## Testing the Object Model

### Unit Test Patterns

1. **Test Property Validation**
   ```groovy
   @Unroll
   def "test #property validation"() {
       given:
       def obj = new MyObject()
       
       when:
       obj."set${property.capitalize()}"(value)
       
       then:
       if (shouldThrow) {
           thrown(IllegalArgumentException)
       } else {
           obj."get${property.capitalize()}"() == expectedValue
       }
       
       where:
       property | value     | shouldThrow | expectedValue
       "size"   | "SMALL"   | false       | "SMALL"
       "size"   | "INVALID" | true        | null
       "count"  | 5         | false       | 5
       "count"  | -1        | true        | null
   }
   ```

2. **Test Composite Keys**
   ```groovy
   def "test composite snapshot ID"() {
       given:
       def obj = new MyObject()
       obj.setSchema(new Schema("CATALOG", "SCHEMA"))
       obj.setName("OBJECT")
       
       expect:
       obj.getSnapshotId() == "CATALOG.SCHEMA.OBJECT"
   }
   ```

3. **Test Serialization**
   ```groovy
   def "test object serialization"() {
       given:
       def obj = createFullyPopulatedObject()
       
       when:
       def serialized = serialize(obj)
       def deserialized = deserialize(serialized)
       
       then:
       deserialized.equals(obj)
       deserialized.getName() == obj.getName()
       // Test all properties...
   }
   ```

### Best Practices for Object Model

1. **Immutability for Key Fields**
   - Once set, identity fields should not change
   - Consider making setters validate against existing values

2. **Defensive Copying**
   - Return copies of mutable objects (dates, lists)
   - Accept copies in setters

3. **Consistent Null Handling**
   - Decide if null means "not set" or "explicitly null"
   - Document the behavior

4. **Validation at Set Time**
   - Validate in setters, not just in change execution
   - Fail fast with clear error messages

5. **ToString Implementation**
   - Include key identifying information
   - Useful for debugging and logging

### Running the Tests

```bash
# Run object model tests
./gradlew test --tests MyObjectTest

# Run with coverage
./gradlew test jacocoTestReport
```

### Next Steps

Once your database object model is complete and tested:
1. Verify all properties from requirements are included
2. Ensure proper validation and normalization
3. Confirm test coverage is comprehensive
4. Proceed to **Part 2: Snapshot Implementation**

### Checklist

- [ ] Requirements document created
- [ ] All properties identified and categorized
- [ ] Database object class created
- [ ] Property validation implemented
- [ ] Property normalization implemented
- [ ] Unit tests written and passing
- [ ] Edge cases tested
- [ ] Documentation complete
