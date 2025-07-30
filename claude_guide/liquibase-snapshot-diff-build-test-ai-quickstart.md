# Liquibase 4.33.0 Extension Implementation - AI-Optimized Sequential Guide

## EXECUTION PROTOCOL FOR CLAUDE
1. Execute steps in EXACT numerical order
2. Complete ALL validations before proceeding to next step
3. If ANY validation fails, STOP and report the specific failure
4. Never modify test files to make them pass - only modify implementation
5. Track progress by updating CURRENT_STEP after validations pass
6. Reference detailed implementation guides for specific examples and patterns

## REFERENCE GUIDES
This sequential guide coordinates the use of these detailed references:
- **Main Document**: Overview, prerequisites, and API reference
- **Part 1**: Database Object Model - Detailed patterns and examples
- **Part 2**: Snapshot Implementation - SQL patterns and ResultSet parsing
- **Part 3**: Diff Implementation - Comparator and DiffGenerator details
- **Part 4**: Testing Guide - Comprehensive testing strategies
- **Part 5**: Snowflake Warehouse Reference - Complete working example

## PROGRESS TRACKING
```
CURRENT_STEP: 1.1
CURRENT_STATUS: NOT_STARTED
LAST_VALIDATED_STEP: None
```

---

## STEP 1.1: Document ALL Database Object Properties
```
STATUS: NOT_STARTED
PREREQUISITES: None
BLOCKING: Steps 1.2 through 7.2
```

### REQUIRED_ACTIONS:
1. Locate official database documentation for [OBJECT_TYPE]
2. Find and read documentation for:
   - CREATE [OBJECT_TYPE] syntax
   - ALTER [OBJECT_TYPE] syntax
   - DROP [OBJECT_TYPE] syntax
   - SHOW/DESCRIBE [OBJECT_TYPE] syntax
3. Create file at path: `requirements/[ObjectType]_requirements.md`
4. Document in the file:
   ```markdown
   # [ObjectType] Requirements Specification
   
   ## Total Property Count: [NUMBER]
   
   ## Properties Table
   | Property Name | Data Type | Required | Default Value | Valid Values | Constraints |
   |--------------|-----------|----------|---------------|--------------|-------------|
   | [name]       | [type]    | [Y/N]    | [default]     | [values]     | [limits]    |
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists at path: requirements/[ObjectType]_requirements.md
VALIDATION_2: File contains section "Total Property Count: [NUMBER]"
VALIDATION_3: File contains "Properties Table" with all columns
VALIDATION_4: Every property from database documentation is in table
VALIDATION_5: CREATE syntax is documented
VALIDATION_6: ALTER syntax is documented
```

### PROCEED_CONDITIONS:
- ALL validations return TRUE
- Property count is greater than 0

---

## STEP 1.2: Categorize Properties
```
STATUS: BLOCKED
PREREQUISITES: Step 1.1 STATUS = COMPLETE
BLOCKING: Steps 2.1 through 7.2
```

### REQUIRED_ACTIONS:
1. Open file: `requirements/[ObjectType]_requirements.md`
2. Add three category sections after the properties table:
   ```markdown
   ## Property Categories
   
   ### Required Properties
   Count: [N]
   - [property_name]: [description]
   
   ### Optional Configuration Properties  
   Count: [N]
   - [property_name]: [description]
   
   ### State Properties (Read-Only)
   Count: [N]
   - [property_name]: [description]
   ```
3. Assign EVERY property to EXACTLY ONE category

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Section exists: "## Property Categories"
VALIDATION_2: Section exists: "### Required Properties"
VALIDATION_3: Section exists: "### Optional Configuration Properties"
VALIDATION_4: Section exists: "### State Properties (Read-Only)"
VALIDATION_5: Each section has "Count: [N]" specified
VALIDATION_6: Sum of all counts equals Total Property Count from Step 1.1
VALIDATION_7: Every property appears in exactly one category
```

---

## STEP 2.1: Create Unit Test File
```
STATUS: BLOCKED
PREREQUISITES: Step 1.2 STATUS = COMPLETE
BLOCKING: Steps 2.2 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/test/groovy/.../database/object/[ObjectType]Test.groovy`
2. **REFERENCE**: See Part 1, Section "Testing the Object Model" for test patterns
3. **REFERENCE**: See Part 4, Section "Unit Testing" for Spock examples
4. Implement test structure:
   ```groovy
   package [your.package].database.object
   
   import spock.lang.Specification
   import spock.lang.Unroll
   
   class [ObjectType]Test extends Specification {
       
       def "test minimal object creation"() {
           when:
           def obj = new [ObjectType]()
           obj.setName("TEST_NAME")
           
           then:
           obj.getName() == "TEST_NAME"
           obj.getSnapshotId() == "TEST_NAME"
       }
       
       def "test all properties"() {
           // Test for EVERY property from requirements
       }
       
       def "test property validation"() {
           // Test constraints for EVERY property with constraints
       }
       
       def "test equals and hashCode"() {
           // Test object equality
       }
   }
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/test/groovy/.../database/object/[ObjectType]Test.groovy
VALIDATION_2: Test exists for every Required Property from Step 1.2
VALIDATION_3: Test exists for every Optional Configuration Property from Step 1.2
VALIDATION_4: Validation test exists for every property with constraints
VALIDATION_5: Command `./gradlew test --tests [ObjectType]Test` executes
VALIDATION_6: ALL tests FAIL with error containing "Class not found" or "unable to resolve class"
```

---

## STEP 2.2: Implement Database Object Class
```
STATUS: BLOCKED
PREREQUISITES: Step 2.1 STATUS = COMPLETE
BLOCKING: Steps 3.1 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/main/java/.../database/object/[ObjectType].java`
2. **REFERENCE**: See Part 1, Section "Database Object Model Implementation" for patterns
3. **EXAMPLE**: See Part 5, `Warehouse.java` for complete implementation
4. Implement class structure:
   ```java
   package [your.package].database.object;
   
   import liquibase.structure.AbstractDatabaseObject;
   import liquibase.structure.DatabaseObject;
   
   public class [ObjectType] extends AbstractDatabaseObject {
       // Add ALL properties from requirements
       
       public [ObjectType]() {
           setSnapshotId("name"); // or "schema,name" for composite
       }
       
       @Override
       public String getName() { return name; }
       
       @Override
       public [ObjectType] setName(String name) {
           // Add validation
           this.name = name;
           return this;
       }
       
       // Implement ALL property getters/setters with validation
   }
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/main/java/.../database/object/[ObjectType].java
VALIDATION_2: Class extends AbstractDatabaseObject
VALIDATION_3: Every property from requirements has a field
VALIDATION_4: Every property has getter and setter methods
VALIDATION_5: Validation logic matches constraints from requirements
VALIDATION_6: Command `./gradlew test --tests [ObjectType]Test` executes
VALIDATION_7: ALL tests now PASS
VALIDATION_8: NO modifications were made to test file
```

---

## STEP 3.1: Create SnapshotGenerator Test
```
STATUS: BLOCKED
PREREQUISITES: Step 2.2 STATUS = COMPLETE
BLOCKING: Steps 3.2 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/test/groovy/.../snapshot/[ObjectType]SnapshotGeneratorTest.groovy`
2. Implement test structure:
   ```groovy
   class [ObjectType]SnapshotGeneratorTest extends Specification {
       
       def generator = new [ObjectType]SnapshotGenerator()
       def database = Mock([YourDatabase])
       def snapshot = Mock(DatabaseSnapshot)
       
       def "test getPriority"() {
           expect:
           generator.getPriority([ObjectType].class, database) == SnapshotGenerator.PRIORITY_DATABASE
           generator.getPriority([ObjectType].class, Mock(Database)) == SnapshotGenerator.PRIORITY_NONE
       }
       
       def "test snapshotObject with existing object"() {
           // Mock database query and ResultSet
       }
       
       def "test snapshotObject returns null for non-existent"() {
           // Test null return
       }
       
       def "test addTo for bulk snapshot"() {
           // Test bulk capture
       }
   }
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/test/groovy/.../snapshot/[ObjectType]SnapshotGeneratorTest.groovy
VALIDATION_2: Test exists for getPriority method
VALIDATION_3: Test exists for single object snapshot
VALIDATION_4: Test exists for non-existent object returning null
VALIDATION_5: Test exists for bulk snapshot via addTo
VALIDATION_6: Tests FAIL with "unable to resolve class [ObjectType]SnapshotGenerator"
```

---

## STEP 3.2: Research Database Query
```
STATUS: BLOCKED
PREREQUISITES: Step 3.1 STATUS = COMPLETE
BLOCKING: Steps 3.3 through 7.2
```

### REQUIRED_ACTIONS:
1. Connect to target database using database client
2. **REFERENCE**: See Part 2, Section "SQL Query Patterns" for database-specific examples
3. Execute and document query for single object:
   ```sql
   -- Example for Snowflake
   SHOW [OBJECT_TYPE]S LIKE 'object_name';
   
   -- Example for PostgreSQL  
   SELECT * FROM information_schema.[object_type]s WHERE [object_type]_name = 'object_name';
   ```
3. Execute and document query for all objects
4. Create mapping document:
   ```
   ## Query Column Mappings
   
   Query Type: [SHOW/SELECT]
   Single Object Query: [EXACT SQL]
   All Objects Query: [EXACT SQL]
   
   Column Mappings:
   - Column "name" -> property "name"
   - Column "size" -> property "size"
   [Map EVERY property]
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Query executes successfully in database
VALIDATION_2: Query returns at least one row when object exists
VALIDATION_3: Every property from requirements is visible in query results
VALIDATION_4: Column names are documented for every property
VALIDATION_5: Mapping document created with all mappings
```

---

## STEP 3.3: Implement SnapshotGenerator
```
STATUS: BLOCKED
PREREQUISITES: Step 3.2 STATUS = COMPLETE
BLOCKING: Steps 3.4 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/main/java/.../snapshot/[ObjectType]SnapshotGenerator.java`
2. **REFERENCE**: See Part 2, Section "Implementation Steps" for SnapshotGenerator patterns
3. **REFERENCE**: See Part 2, Section "SQL Query Patterns" for database-specific queries
4. **EXAMPLE**: See Part 5, `WarehouseSnapshotGenerator.java` for complete implementation
5. Implement class:
   ```java
   public class [ObjectType]SnapshotGenerator extends SnapshotGenerator {
       
       @Override
       public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
           if (database instanceof [YourDatabase] && [ObjectType].class.isAssignableFrom(objectType)) {
               return PRIORITY_DATABASE;
           }
           return PRIORITY_NONE;
       }
       
       @Override
       protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) {
           // Use query from Step 3.2
           // Parse ResultSet using mappings from Step 3.2
       }
       
       @Override
       protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) {
           // Implement bulk snapshot
       }
       
       @Override
       public Class<? extends DatabaseObject>[] addsTo() {
           return new Class[] { Schema.class }; // or null for global objects
       }
   }
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/main/java/.../snapshot/[ObjectType]SnapshotGenerator.java
VALIDATION_2: Class extends SnapshotGenerator
VALIDATION_3: getPriority returns PRIORITY_DATABASE for correct database type
VALIDATION_4: snapshotObject uses query from Step 3.2
VALIDATION_5: ResultSet parsing handles ALL properties
VALIDATION_6: ALL tests from Step 3.1 now PASS
```

---

## STEP 3.4: Register SnapshotGenerator Service
```
STATUS: BLOCKED
PREREQUISITES: Step 3.3 STATUS = COMPLETE
BLOCKING: Steps 4.1 through 7.2
```

### REQUIRED_ACTIONS:
1. **REFERENCE**: See Main Document, Section "Service Registration" for details
2. Create directory if not exists: `src/main/resources/META-INF/services/`
3. Create or append to file: `src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator`
4. Add line: `[your.package].snapshot.[ObjectType]SnapshotGenerator`

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Directory exists: src/main/resources/META-INF/services/
VALIDATION_2: File exists: src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator
VALIDATION_3: File contains line: [your.package].snapshot.[ObjectType]SnapshotGenerator
VALIDATION_4: ./gradlew jar executes successfully
VALIDATION_5: JAR contains META-INF/services/liquibase.snapshot.SnapshotGenerator
```

---

## STEP 4.1: Create Comparator Test
```
STATUS: BLOCKED
PREREQUISITES: Step 3.4 STATUS = COMPLETE
BLOCKING: Steps 4.2 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/test/groovy/.../diff/output/[ObjectType]ComparatorTest.groovy`
2. Implement tests for:
   - getPriority method
   - isSameObject for identity comparison
   - findDifferences for each configuration property
   - State properties are excluded from differences

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/test/groovy/.../diff/output/[ObjectType]ComparatorTest.groovy
VALIDATION_2: Test exists for every Optional Configuration Property difference detection
VALIDATION_3: Test verifies State Properties do NOT create differences
VALIDATION_4: Tests FAIL with "unable to resolve class [ObjectType]Comparator"
```

---

## STEP 4.2: Implement Comparator
```
STATUS: BLOCKED
PREREQUISITES: Step 4.1 STATUS = COMPLETE
BLOCKING: Steps 4.3 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/main/java/.../diff/output/[ObjectType]Comparator.java`
2. **REFERENCE**: See Part 3, Section "Implementing the Comparator" for patterns
3. **EXAMPLE**: See Part 5, `WarehouseComparator.java` for complete implementation
4. Define EXCLUDED_FIELDS array with ALL State Properties from Step 1.2
5. Implement DatabaseObjectComparator interface

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: EXCLUDED_FIELDS contains every State Property from Step 1.2
VALIDATION_2: getPriority returns PRIORITY_DATABASE for correct database
VALIDATION_3: isSameObject compares identity field(s) only
VALIDATION_4: findDifferences excludes all EXCLUDED_FIELDS
VALIDATION_5: ALL tests from Step 4.1 now PASS
```

---

## STEP 4.3: Register Comparator Service
```
STATUS: BLOCKED
PREREQUISITES: Step 4.2 STATUS = COMPLETE
BLOCKING: Steps 5.1 through 7.2
```

### REQUIRED_ACTIONS:
1. Create or append to file: `src/main/resources/META-INF/services/liquibase.diff.compare.DatabaseObjectComparator`
2. Add line: `[your.package].diff.output.[ObjectType]Comparator`

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File contains line: [your.package].diff.output.[ObjectType]Comparator
VALIDATION_2: JAR contains updated service file
```

---

## STEP 5.1: Verify Change Classes Exist
```
STATUS: BLOCKED
PREREQUISITES: Step 4.3 STATUS = COMPLETE
BLOCKING: Steps 5.2 through 7.2
```

### REQUIRED_ACTIONS:
1. **REFERENCE**: See Part 3, Section "Change Type Integration" for minimal examples
2. **NOTE**: Change classes may already exist in your project
3. Verify or create: `Create[ObjectType]Change.java`
4. Verify or create: `Alter[ObjectType]Change.java`
5. Verify or create: `Drop[ObjectType]Change.java`

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File exists: src/main/java/.../change/Create[ObjectType]Change.java
VALIDATION_2: File exists: src/main/java/.../change/Alter[ObjectType]Change.java
VALIDATION_3: File exists: src/main/java/.../change/Drop[ObjectType]Change.java
VALIDATION_4: All change classes compile successfully
```

---

## STEP 5.2: Create DiffGenerator Test
```
STATUS: BLOCKED
PREREQUISITES: Step 5.1 STATUS = COMPLETE
BLOCKING: Steps 5.3 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/test/groovy/.../diff/output/[ObjectType]DiffGeneratorTest.groovy`
2. Test fixMissing creates Create change with all properties
3. Test fixUnexpected creates Drop change
4. Test fixChanged creates Alter change with only changed properties

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Test verifies all configuration properties map to Create change
VALIDATION_2: Test verifies only changed properties map to Alter change
VALIDATION_3: Tests FAIL with "unable to resolve class [ObjectType]DiffGenerator"
```

---

## STEP 5.3: Implement DiffGenerator
```
STATUS: BLOCKED
PREREQUISITES: Step 5.2 STATUS = COMPLETE
BLOCKING: Steps 5.4 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/main/java/.../diff/output/[ObjectType]DiffGenerator.java`
2. **REFERENCE**: See Part 3, Section "Implementing the DiffGenerator" for patterns
3. **EXAMPLE**: See Part 5, `WarehouseDiffGenerator.java` for complete implementation
4. Implement all three interfaces: MissingObjectChangeGenerator, UnexpectedObjectChangeGenerator, ChangedObjectChangeGenerator
5. Map every Optional Configuration Property to change objects

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Every Optional Configuration Property has mapping in fixMissing
VALIDATION_2: Every Optional Configuration Property has mapping in fixChanged
VALIDATION_3: ALL tests from Step 5.2 now PASS
```

---

## STEP 5.4: Register DiffGenerator Service
```
STATUS: BLOCKED
PREREQUISITES: Step 5.3 STATUS = COMPLETE
BLOCKING: Steps 6.1 through 7.2
```

### REQUIRED_ACTIONS:
1. Create or append to file: `src/main/resources/META-INF/services/liquibase.diff.output.changelog.ChangeGenerator`
2. Add line: `[your.package].diff.output.[ObjectType]DiffGenerator`

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: File contains line: [your.package].diff.output.[ObjectType]DiffGenerator
VALIDATION_2: JAR contains all three service files
```

---

## STEP 6.1: Create Integration Test
```
STATUS: BLOCKED
PREREQUISITES: Step 5.4 STATUS = COMPLETE
BLOCKING: Steps 6.2 through 7.2
```

### REQUIRED_ACTIONS:
1. Create file: `src/test/groovy/.../snapshot/[ObjectType]SnapshotGeneratorIntegrationTest.groovy`
2. **REFERENCE**: See Part 2, Section "Integration Test" for patterns
3. **REFERENCE**: See Part 4, Section "Integration Testing" for database setup
4. Add annotation: `@Requires({ System.getenv("TEST_DB_URL") })`
5. Test actual database connection and snapshot

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Test connects to real database
VALIDATION_2: Test creates actual database object
VALIDATION_3: Test captures object via snapshot
VALIDATION_4: Test verifies all properties captured correctly
```

---

## STEP 6.2: Run Integration Tests
```
STATUS: BLOCKED
PREREQUISITES: Step 6.1 STATUS = COMPLETE
BLOCKING: Steps 7.1 through 7.2
```

### REQUIRED_ACTIONS:
1. Set environment variables for database connection
2. Execute: `./gradlew test --tests *IntegrationTest`

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Environment variable TEST_DB_URL is set
VALIDATION_2: Integration tests execute without errors
VALIDATION_3: ALL integration tests PASS
VALIDATION_4: Objects are correctly captured from real database
```

---

## STEP 7.1: End-to-End Testing with Liquibase CLI
```
STATUS: BLOCKED
PREREQUISITES: Step 6.2 STATUS = COMPLETE
BLOCKING: Step 7.2
```

### REQUIRED_ACTIONS:
1. **GATHER ENVIRONMENT INFORMATION** - Request from user:
   ```
   Please provide the following information about your Liquibase environment:
   - Liquibase executable path (e.g., /usr/local/bin/liquibase or liquibase.bat)
   - Database connection URL for test environment
   - Database username and password (or property file location)
   - Path to your built JAR file containing the extension
   - Preferred output format for testing (JSON, YAML, or XML)
   ```

2. **CREATE TEST OBJECT** - Execute in database:
   ```sql
   CREATE [OBJECT_TYPE] TEST_E2E_OBJECT 
   WITH [required properties]
        [optional properties for testing];
   ```

3. **TEST SNAPSHOT CAPTURE** - Execute command:
   ```bash
   [liquibase_path] \
     --url=[db_url] \
     --username=[username] \
     --password=[password] \
     --classpath=[extension_jar_path] \
     snapshot \
     --snapshot-format=[format] \
     --output-file=snapshot-output.[ext]
   ```

4. **VERIFY SNAPSHOT OUTPUT** - Check file contents:
   ```
   - File snapshot-output.[ext] exists
   - File contains section for [ObjectType]
   - TEST_E2E_OBJECT appears with all properties
   - All property values match what was created
   ```

5. **CREATE MODIFIED OBJECT** - In second database or modify existing:
   ```sql
   -- If using same database, modify object
   ALTER [OBJECT_TYPE] TEST_E2E_OBJECT 
   SET [property] = [new_value];
   
   -- Or create in second database with differences
   CREATE [OBJECT_TYPE] TEST_E2E_OBJECT
   WITH [different property values];
   ```

6. **TEST DIFF DETECTION** - Execute diff command:
   ```bash
   [liquibase_path] \
     --url=[reference_db_url] \
     --username=[username] \
     --password=[password] \
     --referenceUrl=[target_db_url] \
     --referenceUsername=[username] \
     --referencePassword=[password] \
     --classpath=[extension_jar_path] \
     diff \
     --format=[format] \
     --output-file=diff-output.[ext]
   ```

7. **TEST CHANGELOG GENERATION** - Execute diffChangeLog:
   ```bash
   [liquibase_path] \
     --url=[reference_db_url] \
     --username=[username] \
     --password=[password] \
     --referenceUrl=[target_db_url] \
     --referenceUsername=[username] \
     --referencePassword=[password] \
     --classpath=[extension_jar_path] \
     diffChangeLog \
     --changelog-file=generated-changelog.[ext]
   ```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: snapshot-output.[ext] contains TEST_E2E_OBJECT with all properties
VALIDATION_2: diff-output.[ext] shows differences for modified properties
VALIDATION_3: generated-changelog.[ext] contains Create/Alter/Drop changes as appropriate
VALIDATION_4: No errors in any command execution
VALIDATION_5: Generated changes reference correct object type and properties
```

### CLEANUP_ACTIONS:
```sql
DROP [OBJECT_TYPE] IF EXISTS TEST_E2E_OBJECT;
```

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: Snapshot command captures object with all properties
VALIDATION_2: Diff command detects missing objects
VALIDATION_3: Diff command detects changed objects
VALIDATION_4: Diff command detects unexpected objects
VALIDATION_5: Generated changelog contains correct changes
```

---

## STEP 7.2: Final Validation Checklist
```
STATUS: BLOCKED
PREREQUISITES: Step 7.1 STATUS = COMPLETE
BLOCKING: None - This is the final step
```

### REQUIRED_ACTIONS:
1. Verify all service registrations
2. Verify all properties implemented
3. Document lessons learned

### VALIDATIONS_REQUIRED:
```
VALIDATION_1: All three service files exist in JAR
VALIDATION_2: Total properties implemented equals count from Step 1.1
VALIDATION_3: All unit tests pass
VALIDATION_4: All integration tests pass
VALIDATION_5: Manual testing successful
```

---

## COMPLETION CRITERIA
```
ALL_STEPS_COMPLETE: All steps show STATUS = COMPLETE
FINAL_JAR_VALID: JAR contains all classes and service files
TESTS_PASSING: 100% of tests pass
MANUAL_VERIFICATION: Snapshot and diff work correctly
```

## POST-IMPLEMENTATION DOCUMENTATION
After successful completion, document:
1. Exact queries that worked for your database
2. Special handling required for any properties
3. Performance observations
4. Edge cases discovered

---

## FAILURE RECOVERY PROTOCOL
If any validation fails:
1. STOP immediately
2. Report which validation failed
3. Report exact error message
4. Do not proceed to next step
5. Fix issue and re-run validation