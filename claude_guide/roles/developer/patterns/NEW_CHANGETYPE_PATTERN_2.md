# NEW_CHANGETYPE_PATTERN_2.md - Comprehensive Guide for Implementing New Liquibase Change Types

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Pre-Implementation Requirements Research](#pre-implementation-requirements-research)
3. [Implementation Guide](#implementation-guide)
4. [Unit and Integration Testing](#unit-and-integration-testing)
5. [Test Harness Implementation](#test-harness-implementation)
6. [Troubleshooting Common Issues](#troubleshooting-common-issues)
7. [Key Assumptions and System Knowledge](#key-assumptions-and-system-knowledge)

---

## Prerequisites

### Required Dependencies

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.liquibase</groupId>
        <artifactId>liquibase-core</artifactId>
        <version>4.33.0</version>
        <scope>provided</scope>
    </dependency>
    <!-- JUnit 5 (Jupiter) for all testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Maven Configuration

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.14.0</version>
            <configuration>
                <source>8</source>
                <target>8</target>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.3</version>
        </plugin>
    </plugins>
</build>
```

### Project Structure Requirements

```
liquibase-<extension>/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── liquibase/
│   │   │       ├── change/core/
│   │   │       ├── statement/core/
│   │   │       └── sqlgenerator/core/<database>/
│   │   └── resources/
│   │       ├── META-INF/services/
│   │       └── www.liquibase.org/xml/ns/<extension>/
│   └── test/
│       └── java/
│           └── liquibase/
└── pom.xml
```

### Development Environment Setup

1. **Java Version**: Java 8 minimum (Java 11+ for Liquibase 4.25+)
2. **Maven Version**: 3.6+
3. **IDE Setup**: Enable annotation processing for @DatabaseChangeProperty
4. **Database Access**: Test database for integration testing

---

## Pre-Implementation Requirements Research

### CRITICAL: Before ANY Implementation

Before writing any code, you MUST create a detailed requirements document by researching the database vendor's official documentation.

#### 1. Create Requirements Document

Create a file: `claude_guide/project/requirements/detailed_requirements/<changeTypeName>_requirements.md`

Example: `claude_guide/project/requirements/detailed_requirements/createSchema_requirements.md`

#### 2. Requirements Document Template

```markdown
# <ChangeType> Detailed Requirements

## 1. SQL Syntax Research

### Official Documentation Reference
- URL: [Link to vendor documentation]
- Version: [Database version]
- Last Updated: [Date]

### Basic Syntax
```sql
-- Minimal syntax
CREATE SCHEMA schema_name;

-- Full syntax with all options
CREATE [OR REPLACE] [TRANSIENT] SCHEMA [IF NOT EXISTS] schema_name
  [CLONE source_schema_name]
  [WITH MANAGED ACCESS]
  [DATA_RETENTION_TIME_IN_DAYS = <integer>]
  [MAX_DATA_EXTENSION_TIME_IN_DAYS = <integer>]
  [DEFAULT_DDL_COLLATION = '<collation_specification>']
  [COMMENT = '<string_literal>'];
```

## 2. Attribute Analysis

| Attribute | Description | Data Type | Default | Valid Values | Required |
|-----------|-------------|-----------|---------|--------------|----------|
| schemaName | Name of the schema | String | - | Valid identifier | Yes |
| orReplace | Replace if exists | Boolean | false | true/false | No |
| transient | Create transient schema | Boolean | false | true/false | No |
| ifNotExists | Only create if not exists | Boolean | false | true/false | No |
| managedAccess | Enable managed access | Boolean | false | true/false | No |
| dataRetentionTimeInDays | Data retention period | Integer | 1 | 0-90 | No |
| comment | Schema comment | String | null | Any string | No |

## 3. Mutual Exclusivity Rules

### Mutually Exclusive Combinations
1. `orReplace` and `ifNotExists` - Cannot be used together
2. `transient` and `dataRetentionTimeInDays > 0` - Transient schemas have 0 retention

### Required Combinations
1. None identified

## 4. SQL Examples for Testing

### Example 1: Basic Schema
```sql
CREATE SCHEMA basic_schema;
```

### Example 2: Transient Schema
```sql
CREATE TRANSIENT SCHEMA transient_schema;
```

### Example 3: Schema with All Options
```sql
CREATE OR REPLACE SCHEMA full_schema
  WITH MANAGED ACCESS
  DATA_RETENTION_TIME_IN_DAYS = 7
  COMMENT = 'Full featured schema';
```

### Example 4: Conditional Creation
```sql
CREATE SCHEMA IF NOT EXISTS conditional_schema;
```

## 5. Test Scenarios

Based on the mutual exclusivity rules, we need the following test files:

1. **createSchema.xml** - Basic functionality, transient, managed access, retention
2. **createOrReplaceSchema.xml** - OR REPLACE variations (separate due to mutual exclusivity)
3. **createSchemaIfNotExists.xml** - IF NOT EXISTS variations (separate due to mutual exclusivity)

## 6. Validation Rules

1. schemaName cannot be null or empty
2. If orReplace=true and ifNotExists=true, throw validation error
3. dataRetentionTimeInDays must be between 0 and 90
4. Schema names must follow identifier rules (alphanumeric, underscore, no spaces)
```

#### 3. Research Checklist

- [ ] Find official vendor documentation
- [ ] Document complete SQL syntax
- [ ] List ALL attributes with descriptions
- [ ] Identify data types and valid ranges
- [ ] Find default values
- [ ] Identify mutual exclusivity rules
- [ ] Identify required combinations
- [ ] Create syntactically correct examples
- [ ] Plan test scenarios based on rules

---

## Implementation Guide

### IMPORTANT: Testing Integration Points

**When implementing each phase below, create the corresponding tests immediately:**

- **After Phase 1 (Change Class)**: Create production Change test + quick dev test
- **After Phase 2 (Statement Class)**: Create production Statement test + quick dev test  
- **After Phase 3 (SQL Generator)**: Create production Generator test + quick dev test
- **After Phase 4 (Service Registration)**: Update ServiceRegistrationTest
- **After Phase 5 (XSD Update)**: Run all tests to verify integration
- **After All Phases Complete**: Delete dev tests, run full test suite, then create test harness tests

This approach ensures each component is tested immediately rather than waiting until the end.

---

### Phase 1: Create the Change Class

#### File: `src/main/java/liquibase/change/core/<ChangeType>Change.java`

```java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.<ChangeType>Statement;

/**
 * Creates a <changeType> change.
 */
@DatabaseChange(
    name = "<changeType>",
    description = "<Description>",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "<object-type>"
)
public class <ChangeType>Change extends AbstractChange {

    // Attributes from requirements document
    private String <attributeName>;
    
    @DatabaseChangeProperty(description = "<description>", requiredForDatabase = "snowflake")
    public String get<AttributeName>() {
        return <attributeName>;
    }
    
    public void set<AttributeName>(String <attributeName>) {
        this.<attributeName> = <attributeName>;
    }
    
    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        // Set all properties from requirements
        statement.set<AttributeName>(get<AttributeName>());
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "<Object> " + get<ObjectName>() + " created";
    }
    
    @Override
    public boolean supportsRollback(Database database) {
        return false; // or true if inverse exists
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        // Add validation rules from requirements document
        
        return errors;
    }
    
    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}
```

### Step-by-Step Test for Phase 1

After creating the Change class, immediately test it to verify it works correctly before proceeding:

#### Test File: `src/test/java/liquibase/change/core/Step1_<ChangeType>ChangeTest.java`

```java
package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 1 verification test - ensures Change class is working before proceeding
 * This is a UNIT TEST that runs in the liquibase-snowflake project
 */
public class Step1_<ChangeType>ChangeTest {
    
    @Test
    public void testChangeClassBasics() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        // Test required fields validation
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        assertTrue("Should have validation error for missing required field", 
                  errors.hasErrors());
        
        // Set required fields
        change.set<RequiredAttribute>("TEST_VALUE");
        errors = change.validate(new SnowflakeDatabase());
        assertFalse("Should not have errors with valid required field", 
                   errors.hasErrors());
        
        // Test supports
        assertTrue("Should support Snowflake", 
                  change.supports(new SnowflakeDatabase()));
        assertFalse("Should not support other databases", 
                   change.supports(new PostgresDatabase()));
        
        // Test attribute validation (from requirements)
        // Example: dataRetentionTimeInDays validation
        change.setDataRetentionTimeInDays(100);
        errors = change.validate(new SnowflakeDatabase());
        assertTrue("Should have error for invalid value", 
                  errors.hasErrors());
        
        change.setDataRetentionTimeInDays(30);
        errors = change.validate(new SnowflakeDatabase());
        assertFalse("Should not have errors with valid value", 
                   errors.hasErrors());
        
        System.out.println("✓ Step 1 Test Passed: Change class working correctly");
    }
    
    @Test 
    public void testMutualExclusivity() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST_VALUE");
        
        // Test mutual exclusivity from requirements
        change.set<MutuallyExclusiveOption1>(true);
        change.set<MutuallyExclusiveOption2>(true);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        assertTrue("Should have error for mutually exclusive options", 
                  errors.hasErrors());
        assertTrue("Error message should mention mutual exclusivity",
                  errors.getErrorMessages().get(0).contains("Cannot use both"));
    }
}
```

**Run this test before proceeding to Phase 2:**
```bash
mvn test -Dtest=Step1_<ChangeType>ChangeTest
```

### Phase 2: Create the Statement Class

#### File: `src/main/java/liquibase/statement/core/<ChangeType>Statement.java`

```java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class <ChangeType>Statement extends AbstractSqlStatement {
    
    // All attributes from requirements
    private String <attributeName>;
    
    // Getters and setters for all attributes
    public String get<AttributeName>() {
        return <attributeName>;
    }
    
    public void set<AttributeName>(String <attributeName>) {
        this.<attributeName> = <attributeName>;
    }
}
```

### Step-by-Step Test for Phase 2

After creating the Statement class, immediately test it:

#### Test File: `src/test/java/liquibase/statement/core/Step2_<ChangeType>StatementTest.java`

```java
package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 2 verification test - ensures Statement class is working before proceeding
 * This is a UNIT TEST that runs in the liquibase-snowflake project
 */
public class Step2_<ChangeType>StatementTest {
    
    @Test
    public void testStatementClass() {
        // Create statement and test initial state
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        // Verify all properties start as null
        assertNull("Should start with null required attribute", 
                  statement.get<RequiredAttribute>());
        assertNull("Should start with null optional attribute", 
                  statement.get<OptionalAttribute>());
        
        // Test setters and getters
        statement.set<RequiredAttribute>("TEST_VALUE");
        assertEquals("Should return set value", 
                    "TEST_VALUE", statement.get<RequiredAttribute>());
        
        statement.set<OptionalAttribute>("OPTIONAL_VALUE");
        assertEquals("Should return set optional value", 
                    "OPTIONAL_VALUE", statement.get<OptionalAttribute>());
        
        // Test Boolean properties if applicable
        statement.set<BooleanAttribute>(true);
        assertTrue("Should return true for boolean", 
                  statement.get<BooleanAttribute>());
        
        statement.set<BooleanAttribute>(false);
        assertFalse("Should return false for boolean", 
                   statement.get<BooleanAttribute>());
        
        // Test null handling
        statement.set<RequiredAttribute>(null);
        assertNull("Should handle null values", 
                  statement.get<RequiredAttribute>());
        
        System.out.println("✓ Step 2 Test Passed: Statement class working correctly");
    }
}
```

**Run this test before proceeding to Phase 3:**
```bash
mvn test -Dtest=Step2_<ChangeType>StatementTest
```

### Phase 3: Create the SQL Generator

#### File: `src/main/java/liquibase/sqlgenerator/core/snowflake/<ChangeType>GeneratorSnowflake.java`

```java
package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.<ChangeType>Statement;

public class <ChangeType>GeneratorSnowflake extends AbstractSqlGenerator<<ChangeType>Statement> {
    
    @Override
    public boolean supports(<ChangeType>Statement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(<ChangeType>Statement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        
        // Build SQL based on requirements document
        sql.append("CREATE ");
        
        // Handle mutually exclusive options correctly
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}
```

### Step-by-Step Test for Phase 3

After creating the SQL Generator, immediately test it:

#### Test File: `src/test/java/liquibase/sqlgenerator/core/snowflake/Step3_<ChangeType>GeneratorTest.java`

```java
package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.<ChangeType>Statement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 3 verification test - ensures SQL Generator is working before proceeding
 * This is a UNIT TEST that runs in the liquibase-snowflake project
 */
public class Step3_<ChangeType>GeneratorTest {
    
    @Test
    public void testSqlGenerator() {
        // Create test statement
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        
        // Create generator and database
        <ChangeType>GeneratorSnowflake generator = new <ChangeType>GeneratorSnowflake();
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        // Test supports
        assertTrue("Should support Snowflake database", 
                  generator.supports(statement, database));
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        assertNotNull("Should generate SQL", sqls);
        assertTrue("Should generate at least one SQL statement", sqls.length > 0);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL: " + sql);
        
        // Verify SQL contains expected elements
        assertTrue("Should contain CREATE keyword", sql.contains("CREATE"));
        assertTrue("Should contain object name", sql.contains("TEST_OBJECT"));
        
        System.out.println("✓ Basic SQL generation working");
    }
    
    @Test
    public void testSqlGeneratorWithOptions() {
        // Test with optional attributes from requirements
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        statement.set<OptionalAttribute1>("value1");
        statement.set<BooleanAttribute>(true);
        
        <ChangeType>GeneratorSnowflake generator = new <ChangeType>GeneratorSnowflake();
        Sql[] sqls = generator.generateSql(statement, new SnowflakeDatabase(), null);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL with options: " + sql);
        
        // Verify optional attributes are included
        assertTrue("Should contain optional attribute", 
                  sql.contains("value1") || sql.contains("VALUE1"));
        
        System.out.println("✓ Step 3 Test Passed: SQL Generator working correctly");
    }
    
    @Test
    public void testMutuallyExclusiveOptions() {
        // Test mutual exclusivity handling in SQL generation
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        statement.set<MutuallyExclusiveOption1>(true);
        statement.set<MutuallyExclusiveOption2>(true);
        
        <ChangeType>GeneratorSnowflake generator = new <ChangeType>GeneratorSnowflake();
        Sql[] sqls = generator.generateSql(statement, new SnowflakeDatabase(), null);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL with mutual exclusive options: " + sql);
        
        // Verify only one option is used (document which takes precedence)
        int optionCount = 0;
        if (sql.contains("<OPTION1>")) optionCount++;
        if (sql.contains("<OPTION2>")) optionCount++;
        
        assertEquals("Should only include one mutually exclusive option", 
                    1, optionCount);
    }
}
```

**Run this test before proceeding to Phase 4:**
```bash
mvn test -Dtest=Step3_<ChangeType>GeneratorTest
```

### Phase 4: Service Registration

#### File: `src/main/resources/META-INF/services/liquibase.change.Change`
```
liquibase.change.core.<ChangeType>Change
```

#### File: `src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`
```
liquibase.sqlgenerator.core.snowflake.<ChangeType>GeneratorSnowflake
```

### Step-by-Step Test for Phase 4

After registering services, immediately test that they're loaded:

#### Test File: `src/test/java/liquibase/Step4_ServiceRegistrationTest.java`

```java
package liquibase;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.core.<ChangeType>Change;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.parser.NamespaceDetailsFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 4 verification test - ensures services are registered correctly
 * This is an INTEGRATION TEST that runs in the liquibase-snowflake project
 */
public class Step4_ServiceRegistrationTest {
    
    @Test
    public void testChangeRegistration() {
        // Test that our change is registered and can be created
        try {
            Change change = ChangeFactory.getInstance().create("<changeType>");
            assertNotNull("Should create change instance", change);
            assertTrue("Should be our change class", 
                      change instanceof <ChangeType>Change);
            System.out.println("✓ Change registered: " + change.getClass().getName());
        } catch (Exception e) {
            fail("Failed to create change: " + e.getMessage());
        }
    }
    
    @Test
    public void testGeneratorRegistration() {
        // Test that our generator is registered
        boolean foundGenerator = false;
        for (liquibase.sqlgenerator.SqlGenerator generator : 
             SqlGeneratorFactory.getInstance().getGenerators()) {
            if (generator.getClass().getName().contains("<ChangeType>GeneratorSnowflake")) {
                foundGenerator = true;
                System.out.println("✓ Generator registered: " + generator.getClass().getName());
                break;
            }
        }
        assertTrue("Should find our SQL generator", foundGenerator);
    }
    
    @Test
    public void testNamespaceRegistration() {
        // Test that our namespace is registered
        boolean foundNamespace = false;
        String expectedNamespace = "http://www.liquibase.org/xml/ns/snowflake";
        
        for (liquibase.parser.NamespaceDetails namespace : 
             NamespaceDetailsFactory.getInstance().getNamespaceDetails()) {
            for (String ns : namespace.getNamespaces()) {
                if (expectedNamespace.equals(ns)) {
                    foundNamespace = true;
                    System.out.println("✓ Namespace registered: " + ns);
                    System.out.println("  Short name: " + namespace.getShortName(ns));
                    System.out.println("  Schema URL: " + namespace.getSchemaUrl(ns));
                    break;
                }
            }
        }
        assertTrue("Should find our namespace details", foundNamespace);
        
        System.out.println("✓ Step 4 Test Passed: All components registered correctly");
    }
}
```

**Run this test before proceeding to Phase 5:**
```bash
mvn test -Dtest=Step4_ServiceRegistrationTest
```

### Phase 5: XSD Schema Update

#### File: `src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd`

Add the element definition based on requirements:

```xml
<xsd:element name="<changeType>">
    <xsd:complexType>
        <xsd:complexContent>
            <xsd:extension base="abstractChange">
                <xsd:attribute name="<attributeName>" type="xsd:string" use="required"/>
                <!-- Add all attributes from requirements -->
            </xsd:extension>
        </xsd:complexContent>
    </xsd:complexType>
</xsd:element>
```

### Step-by-Step Test for Phase 5

After updating the XSD, immediately test that XML parsing works:

#### Test File: `src/test/java/liquibase/Step5_XsdValidationTest.java`

```java
package liquibase;

import org.junit.jupiter.api.Test;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step 5 verification test - ensures XSD is valid and XML can be parsed
 * This is a UNIT TEST that runs in the liquibase-snowflake project
 */
public class Step5_XsdValidationTest {
    
    @Test
    public void testXsdExists() {
        // Test that XSD is in the correct location
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(
            "www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
        );
        assertNotNull("XSD file should exist at standard Liquibase path", xsdStream);
        System.out.println("✓ XSD found at correct location");
    }
    
    @Test
    public void testXsdValidity() throws Exception {
        // Test that the XSD is valid
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(
            "www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
        );
        
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        assertNotNull("Should create valid schema", schema);
        
        System.out.println("✓ XSD is valid XML Schema");
    }
    
    @Test
    public void testSampleXmlValidation() throws Exception {
        // Test that a sample XML validates against our XSD
        String testXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<snowflake:<changeType> \n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    <requiredAttribute>=\"TEST_VALUE\"/>";
        
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(
            "www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd"
        );
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(xsdStream));
        Validator validator = schema.newValidator();
        
        // This should validate without throwing an exception
        try {
            validator.validate(new StreamSource(new java.io.StringReader(testXml)));
            System.out.println("✓ Sample XML validates against XSD");
        } catch (Exception e) {
            fail("XML validation failed: " + e.getMessage());
        }
        
        System.out.println("✓ Step 5 Test Passed: XSD schema working correctly");
    }
}
```

**Run this test to verify XSD is working:**
```bash
mvn test -Dtest=Step5_XsdValidationTest
```

### Final Integration Test

After all components are implemented and individual tests pass:

#### Test File: `src/test/java/liquibase/Step6_FullIntegrationTest.java`

```java
package liquibase;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.OfflineConnection;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Final integration test - ensures complete flow from XML to SQL works
 * This is an INTEGRATION TEST that runs in the liquibase-snowflake project
 */
public class Step6_FullIntegrationTest {
    
    @Test
    public void testFullIntegration() throws Exception {
        // Create test changelog
        String changelog = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "        http://www.liquibase.org/xml/ns/snowflake\n" +
            "        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet id=\"1\" author=\"developer\">\n" +
            "        <snowflake:<changeType> \n" +
            "            <requiredAttribute>=\"TEST_OBJECT\"\n" +
            "            <optionalAttribute>=\"optional_value\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        // Write to temp file
        File tempFile = File.createTempFile("integration-test", ".xml");
        Files.write(tempFile.toPath(), changelog.getBytes());
        tempFile.deleteOnExit();
        
        // Create database
        Database database = new SnowflakeDatabase();
        database.setConnection(new OfflineConnection(
            "offline:snowflake", new FileSystemResourceAccessor()
        ));
        
        // Create Liquibase instance
        Liquibase liquibase = new Liquibase(
            tempFile.getAbsolutePath(),
            new FileSystemResourceAccessor(tempFile.getParent()),
            database
        );
        
        // Get SQL without executing
        StringWriter writer = new StringWriter();
        liquibase.update((String) null, writer);
        
        String sql = writer.toString();
        System.out.println("Generated SQL:\n" + sql);
        
        // Verify SQL was generated
        assertFalse("Should generate SQL", sql.isEmpty());
        assertTrue("Should contain CREATE statement", sql.contains("CREATE"));
        assertTrue("Should contain object name", sql.contains("TEST_OBJECT"));
        
        System.out.println("✓ Integration Test Passed: Full pipeline working!");
    }
}
```

**Run the final integration test:**
```bash
mvn test -Dtest=Step6_FullIntegrationTest
```

### Summary of Step-by-Step Tests

These step-by-step tests should be run after implementing each phase:

1. **Step1_<ChangeType>ChangeTest** - After creating Change class
2. **Step2_<ChangeType>StatementTest** - After creating Statement class
3. **Step3_<ChangeType>GeneratorTest** - After creating SQL Generator
4. **Step4_ServiceRegistrationTest** - After adding service registrations
5. **Step5_XsdValidationTest** - After creating XSD schema
6. **Step6_FullIntegrationTest** - After all components are complete

These tests are **throwaway tests** meant for immediate verification during development. Once all tests pass:
1. Delete the Step* test files
2. Ensure production unit tests are complete
3. Move on to test harness implementation

---

## Unit and Integration Testing

### IMPORTANT: Unit/Integration Tests vs Test Harness Tests

**Unit/Integration Tests** (this section):
- Run in the `liquibase-snowflake` project via Maven
- Test Java code directly (Change, Statement, Generator classes)
- Use JUnit 5 to verify code behavior
- **Do NOT connect to a real database** (use mocks/offline mode if needed)
- Run quickly as part of the Maven build process
- Integration tests here mean testing integration between Java components, not database integration

**Test Harness Tests** (separate section below):
- Run in the `liquibase-test-harness` project
- Test actual database behavior with real Snowflake connection
- Execute changesets and verify SQL execution
- Compare expected vs actual SQL output
- Validate database object creation

These are completely different types of tests serving different purposes.

### Overview

This section covers unit and integration testing approaches:

1. **Production Unit/Integration Tests**: Proper test files that become part of the codebase
2. **Step-by-Step Verification Tests**: Development workflow tests for immediate feedback

Both are important - the production tests ensure long-term maintainability, while the step-by-step tests provide rapid development feedback.

---

### Production Unit and Integration Tests

#### Step 1: Create Change Class Unit Test

File: `src/test/java/liquibase/change/core/<ChangeType>ChangeTest.java`

```java
package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.ChangeMetaData;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.<ChangeType>Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for <ChangeType>Change
 */
@DisplayName("<ChangeType>Change")
public class <ChangeType>ChangeTest {
    
    @Test
    @DisplayName("Should set and get all basic properties correctly")
    public void testBasicProperties() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        // Test required property
        assertNull(change.get<RequiredAttribute>());
        change.set<RequiredAttribute>("TEST_VALUE");
        assertEquals("TEST_VALUE", change.get<RequiredAttribute>());
        
        // Test optional properties from requirements document
        assertNull(change.get<OptionalAttribute>());
        change.set<OptionalAttribute>("optional_value");
        assertEquals("optional_value", change.get<OptionalAttribute>());
    }
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        assertTrue(change.supports(new SnowflakeDatabase()));
        assertFalse(change.supports(new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate correct statement with all properties")
    public void testGenerateStatements() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST_VALUE");
        change.set<OptionalAttribute>("optional_value");
        
        SqlStatement[] statements = change.generateStatements(new SnowflakeDatabase());
        
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof <ChangeType>Statement);
        
        <ChangeType>Statement stmt = (<ChangeType>Statement) statements[0];
        assertEquals("TEST_VALUE", stmt.get<RequiredAttribute>());
        assertEquals("optional_value", stmt.get<OptionalAttribute>());
    }
    
    @Test
    @DisplayName("Should fail validation when required attribute is missing")
    public void testValidationFailsWithoutRequired() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("<requiredAttribute> is required")));
    }
    
    @Test
    @DisplayName("Should fail validation with mutually exclusive options")
    public void testValidationFailsWithMutualExclusions() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST_VALUE");
        // Set mutually exclusive options from requirements
        change.set<MutuallyExclusiveOption1>(true);
        change.set<MutuallyExclusiveOption2>(true);
        
        ValidationErrors errors = change.validate(new SnowflakeDatabase());
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot use both")));
    }
    
    @Test
    @DisplayName("Should support/not support rollback as appropriate")
    public void testRollbackSupport() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST_VALUE");
        
        // Set based on requirements - CREATE operations typically support rollback
        assertTrue(change.supportsRollback(new SnowflakeDatabase()));
        
        // Test inverse generation if supported
        liquibase.change.Change[] inverses = change.createInverses();
        if (change.supportsRollback(new SnowflakeDatabase())) {
            assertEquals(1, inverses.length);
            assertTrue(inverses[0] instanceof <InverseChangeType>Change);
        } else {
            assertEquals(0, inverses.length);
        }
    }
    
    @Test
    @DisplayName("Should return correct confirmation message")
    public void testConfirmationMessage() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST_VALUE");
        
        assertEquals("<Object> TEST_VALUE created", change.getConfirmationMessage());
    }
    
    @Test
    @DisplayName("Should return correct serialized namespace")
    public void testSerializedNamespace() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", 
                     change.getSerializedObjectNamespace());
    }
    
    @Test
    @DisplayName("Should be registered in ChangeFactory")
    public void testChangeRegistration() {
        ChangeMetaData metadata = ChangeFactory.getInstance()
            .getChangeMetaData("<changeType>");
        
        assertNotNull(metadata);
        assertEquals("<changeType>", metadata.getName());
        assertEquals("<Description>", metadata.getDescription());
    }
}
```

#### Step 2: Create Statement Class Unit Test

File: `src/test/java/liquibase/statement/core/<ChangeType>StatementTest.java`

```java
package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for <ChangeType>Statement
 */
@DisplayName("<ChangeType>Statement")
public class <ChangeType>StatementTest {
    
    @Test
    @DisplayName("Should initialize with null values")
    public void testInitialState() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        // Test all properties from requirements document
        assertNull(statement.get<RequiredAttribute>());
        assertNull(statement.get<OptionalAttribute>());
        assertNull(statement.get<BooleanAttribute>());
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    public void testAllProperties() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        // Test all setters and getters from requirements
        statement.set<RequiredAttribute>("TEST_VALUE");
        assertEquals("TEST_VALUE", statement.get<RequiredAttribute>());
        
        statement.set<OptionalAttribute>("optional_value");
        assertEquals("optional_value", statement.get<OptionalAttribute>());
        
        statement.set<BooleanAttribute>(true);
        assertTrue(statement.get<BooleanAttribute>());
    }
    
    @Test
    @DisplayName("Should handle null values properly")
    public void testNullHandling() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        // Set values then set back to null
        statement.set<RequiredAttribute>("TEST");
        statement.set<RequiredAttribute>(null);
        assertNull(statement.get<RequiredAttribute>());
        
        statement.set<BooleanAttribute>(true);
        statement.set<BooleanAttribute>(null);
        assertNull(statement.get<BooleanAttribute>());
    }
}
```

#### Step 3: Create SQL Generator Unit Test

File: `src/test/java/liquibase/sqlgenerator/core/snowflake/<ChangeType>GeneratorSnowflakeTest.java`

```java
package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.<ChangeType>Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for <ChangeType>GeneratorSnowflake
 */
@DisplayName("<ChangeType>GeneratorSnowflake")
public class <ChangeType>GeneratorSnowflakeTest {
    
    private final <ChangeType>GeneratorSnowflake generator = new <ChangeType>GeneratorSnowflake();
    private final SnowflakeDatabase database = new SnowflakeDatabase();
    
    @Test
    @DisplayName("Should only support Snowflake database")
    public void testSupports() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new PostgresDatabase()));
    }
    
    @Test
    @DisplayName("Should generate basic SQL")
    public void testBasicGeneration() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE <OBJECT> TEST_OBJECT", sqls[0].toSql());
    }
    
    // Add tests for all SQL variations from requirements document
    @Test
    @DisplayName("Should generate SQL with optional attributes")
    public void testWithOptionalAttributes() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        statement.set<OptionalAttribute>("value");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        assertEquals("CREATE <OBJECT> TEST_OBJECT <OPTION> value", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle special characters in names")
    public void testSpecialCharacters() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("\"TEST-OBJECT\"");
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        // Note: Liquibase may double-quote identifiers - adjust expectation based on actual behavior
        assertTrue(sqls[0].toSql().contains("TEST-OBJECT"));
    }
    
    @Test
    @DisplayName("Should handle mutually exclusive options correctly")
    public void testMutualExclusivity() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        statement.set<MutuallyExclusiveOption1>(true);
        statement.set<MutuallyExclusiveOption2>(true);
        
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        assertEquals(1, sqls.length);
        // Should prioritize one option over another - document the priority
        assertTrue(sqls[0].toSql().contains("<OPTION1>"));
        assertFalse(sqls[0].toSql().contains("<OPTION2>"));
    }
}
```

#### Step 4: Update ServiceRegistrationTest

Add tests for the new change type in `src/test/java/liquibase/ServiceRegistrationTest.java`:

```java
@Test
@DisplayName("Should instantiate <ChangeType> change type")
public void test<ChangeType>ChangeInstantiation() {
    <ChangeType>Change change = new <ChangeType>Change();
    assertNotNull(change);
    assertTrue(change.supports(new SnowflakeDatabase()));
}
```

#### Step 5: Update Test Suite

Add the new tests to `src/test/java/liquibase/SnowflakeExtensionTestSuite.java`:

```java
// <ChangeType> tests
<ChangeType>ChangeTest.class,
<ChangeType>StatementTest.class,
<ChangeType>GeneratorSnowflakeTest.class,
```

---

### Step-by-Step Verification Tests (Development Workflow)

Create these tests **in addition to** the production tests above. These are for rapid development feedback and can be temporary files:

#### Development Step 1: Quick Change Class Test
File: `src/test/java/liquibase/change/core/DevStep1_<ChangeType>ChangeTest.java`

```java
package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Quick development test - can be deleted after implementation is complete
 */
public class DevStep1_<ChangeType>ChangeTest {
    
    @Test
    public void testBasicCreation() {
        // Quick test to verify class compiles and basic functionality works
        <ChangeType>Change change = new <ChangeType>Change();
        assertNotNull(change);
        assertTrue(change.supports(new SnowflakeDatabase()));
    }
    
    @Test
    public void testRequiredAttribute() {
        <ChangeType>Change change = new <ChangeType>Change();
        
        // Test primary required attribute
        change.set<RequiredAttribute>("TEST_VALUE");
        assertEquals("TEST_VALUE", change.get<RequiredAttribute>());
        
        // Quick validation test
        assertDoesNotThrow(() -> change.generateStatements(new SnowflakeDatabase()));
    }
}
```

#### Development Step 2: Quick Statement Test
File: `src/test/java/liquibase/statement/core/DevStep2_<ChangeType>StatementTest.java`

```java
package liquibase.statement.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Quick development test - can be deleted after implementation is complete
 */
public class DevStep2_<ChangeType>StatementTest {
    
    @Test
    public void testBasicProperties() {
        <ChangeType>Statement statement = new <ChangeType>Statement();
        
        // Quick test of key properties
        assertNull(statement.get<RequiredAttribute>());
        
        statement.set<RequiredAttribute>("TEST");
        assertEquals("TEST", statement.get<RequiredAttribute>());
    }
}
```

#### Development Step 3: Quick Generator Test
File: `src/test/java/liquibase/sqlgenerator/core/snowflake/DevStep3_<ChangeType>GeneratorTest.java`

```java
package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.statement.core.<ChangeType>Statement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Quick development test - can be deleted after implementation is complete
 */
public class DevStep3_<ChangeType>GeneratorTest {
    
    @Test
    public void testBasicSqlGeneration() {
        <ChangeType>GeneratorSnowflake generator = new <ChangeType>GeneratorSnowflake();
        <ChangeType>Statement statement = new <ChangeType>Statement();
        statement.set<RequiredAttribute>("TEST_OBJECT");
        
        // Quick test that SQL is generated
        assertDoesNotThrow(() -> {
            generator.generateSql(statement, new SnowflakeDatabase(), null);
        });
    }
}
```

### Running the Tests

#### Production Tests
```bash
# Run all production tests for the change type
mvn test -Dtest="*<ChangeType>*Test"

# Run specific test class
mvn test -Dtest="<ChangeType>ChangeTest"

# Run all tests to ensure no regression
mvn test
```

#### Development Tests (Quick Feedback)
```bash
# Run just the development tests for quick feedback
mvn test -Dtest="DevStep*<ChangeType>*"

# Run individual development step
mvn test -Dtest="DevStep1_<ChangeType>ChangeTest"
```

### Test Integration Workflow

1. **Start Development**: Create development tests (`DevStep*`) for immediate feedback
2. **Implement Incrementally**: Use dev tests to verify each component as you build it
3. **Create Production Tests**: Once implementation is stable, create proper test files
4. **Delete Development Tests**: Remove `DevStep*` files once production tests are complete
5. **Run Full Test Suite**: Ensure all tests pass before moving to test harness

### Key Testing Best Practices

1. **Use @DisplayName annotations** for clear test descriptions
2. **Test all validation rules** from the requirements document
3. **Test mutual exclusivity** carefully - use correct Boolean checks
4. **Handle quoted identifiers** - Liquibase may double-quote special characters
5. **Update ServiceRegistrationTest** with each new change type
6. **Keep tests independent** - don't rely on test execution order

---

## Test Harness Implementation (Database Testing)

### IMPORTANT: This is Different from Unit/Integration Tests

Test Harness tests:
- Run in a **completely separate project** (`liquibase-test-harness`)
- Require a **real Snowflake database connection**
- Test **actual SQL execution** against the database
- Verify **database objects are created correctly**
- Compare **expected vs actual SQL output**

### CRITICAL: Test Harness Pattern

After all unit/integration tests pass in `liquibase-snowflake`, create test harness tests following this EXACT pattern:

#### 1. Directory Structure
```
liquibase-test-harness/
└── src/main/resources/liquibase/harness/change/
    ├── changelogs/snowflake/
    │   └── <changeType>.xml
    ├── expectedSql/snowflake/
    │   └── <changeType>.sql
    └── expectedSnapshot/snowflake/
        └── <changeType>.json
```

#### 2. Changelog File Pattern

File: `changelogs/snowflake/<changeType>.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/snowflake
        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <!-- ALWAYS include init.xml first -->
    <include file="liquibase/harness/change/changelogs/snowflake/init.xml"/>
    
    <!-- Cleanup changeset - CRITICAL for test isolation -->
    <changeSet id="cleanup" author="test-harness" runAlways="true">
        <sql>
            <!-- Drop all objects this test will create -->
            DROP <OBJECT> IF EXISTS TEST_OBJECT_1 CASCADE;
            DROP <OBJECT> IF EXISTS TEST_OBJECT_2 CASCADE;
        </sql>
    </changeSet>

    <!-- Test changesets - one per variation -->
    <changeSet id="1" author="test-harness">
        <snowflake:<changeType> <requiredAttribute>="TEST_OBJECT_1"/>
    </changeSet>

    <changeSet id="2" author="test-harness">
        <snowflake:<changeType> <requiredAttribute>="TEST_OBJECT_2"
                               <optionalAttribute>="value"/>
    </changeSet>
</databaseChangeLog>
```

#### 3. Expected SQL File Pattern

File: `expectedSql/snowflake/<changeType>.sql`

```sql
-- Liquibase Snowflake SQL
-- MUST include ALL SQL including init.xml
USE ROLE LIQUIBASE_TEST_HARNESS_ROLE
DROP SCHEMA IF EXISTS TESTHARNESS CASCADE
CREATE SCHEMA TESTHARNESS
USE SCHEMA TESTHARNESS
GRANT ALL PRIVILEGES ON SCHEMA TESTHARNESS TO ROLE LIQUIBASE_TEST_HARNESS_ROLE
CREATE TABLE DATABASECHANGELOG (
ID VARCHAR(255) NOT NULL,
AUTHOR VARCHAR(255) NOT NULL,
FILENAME VARCHAR(255) NOT NULL,
DATEEXECUTED TIMESTAMP NOT NULL,
ORDEREXECUTED INT NOT NULL,
EXECTYPE VARCHAR(10) NOT NULL,
MD5SUM VARCHAR(35),
DESCRIPTION VARCHAR(255),
COMMENTS VARCHAR(255),
TAG VARCHAR(255),
LIQUIBASE VARCHAR(20),
CONTEXTS VARCHAR(255),
LABELS VARCHAR(255),
DEPLOYMENT_ID VARCHAR(10)
)
CREATE TABLE DATABASECHANGELOGLOCK (
ID INT NOT NULL,
LOCKED BOOLEAN NOT NULL,
LOCKGRANTED TIMESTAMP,
LOCKEDBY VARCHAR(255),
PRIMARY KEY (ID)
)
INSERT INTO DATABASECHANGELOGLOCK (ID, LOCKED) VALUES (1, TRUE)
-- Cleanup SQL
DROP <OBJECT> IF EXISTS TEST_OBJECT_1 CASCADE
DROP <OBJECT> IF EXISTS TEST_OBJECT_2 CASCADE
-- Actual test SQL
CREATE <OBJECT> TEST_OBJECT_1
CREATE <OBJECT> TEST_OBJECT_2
```

#### 4. Expected Snapshot File Pattern

File: `expectedSnapshot/snowflake/<changeType>.json`

```json
{
  "<objects>": [
    {
      "<object>": {
        "name": "TEST_OBJECT_1"
      }
    },
    {
      "<object>": {
        "name": "TEST_OBJECT_2",
        "<attribute>": "value"
      }
    }
  ]
}
```

#### 5. Running Test Harness Tests

```bash
# First, rebuild the extension JAR
cd liquibase-snowflake
mvn package -DskipTests
cp target/liquibase-snowflake-*.jar ../liquibase-test-harness/lib/

# Run the test
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=<changeType> -DdbName=snowflake
```

### Test Harness Best Practices

1. **ALWAYS include init.xml** - It resets the database state
2. **ALWAYS include cleanup changeset** - Drop objects before creating
3. **Self-contained tests** - Don't assume any pre-existing objects
4. **Include ALL SQL in expectedSql** - Including init.xml output
5. **Test multiple variations** - But keep mutually exclusive options in separate files
6. **Use descriptive IDs** - Help identify which test failed

---

## Troubleshooting Common Issues

### Issue 1: Change Not Recognized

**Symptom**: `Unknown change type 'snowflake:<changeType>'`

**Solutions**:
1. Check service registration in `META-INF/services/liquibase.change.Change`
2. Verify package name matches registration
3. Rebuild JAR and copy to test harness
4. Check for typos in change name

### Issue 2: SQL Not Generated

**Symptom**: Empty SQL or no SQL in updateSql

**Solutions**:
1. Add `supports()` method returning `database instanceof SnowflakeDatabase`
2. Check SqlGenerator registration
3. Verify generator `supports()` method returns true
4. Check for exceptions in generator

### Issue 3: XSD Validation Errors

**Symptom**: `Cannot find the declaration of element`

**Solutions**:
1. Update XSD with element definition
2. Check namespace in XSD matches Java code
3. Verify xsi:schemaLocation in test XML
4. Check attribute names match exactly (case-sensitive)

### Issue 4: Test Harness SQL Mismatch

**Symptom**: `Expected sql doesn't match generated sql`

**Solutions**:
1. Generated SQL includes init.xml output - include it in expectedSql
2. Check for missing/extra semicolons
3. Verify exact whitespace/newline format
4. Use `runAlways="true"` ONLY on cleanup changesets

### Issue 5: Namespace Not Found

**Symptom**: Changes work in unit tests but fail in test harness

**Solutions**:
1. Verify NamespaceDetails registration exists
2. Check `getSerializedObjectNamespace()` returns correct URL
3. Ensure XSD is in correct location
4. Rebuild and copy JAR to test harness

### Issue 6: Mutual Exclusivity Validation

**Symptom**: Validation errors for valid combinations

**Solutions**:
1. Check validation logic uses correct boolean checks
2. Use `Boolean.TRUE.equals()` for null-safe comparison
3. Create separate test files for mutually exclusive options

### Issue 7: Snapshot Comparison Failures

**Symptom**: `Expected: <attribute> but none found`

**Solutions**:
1. Check snapshot generator implementation
2. Verify attribute names in JSON match database
3. Ensure snapshot includes all expected attributes
4. Check for case sensitivity issues

### Issue 8: Test Compilation Errors

**Symptom**: `cannot find symbol` or package errors in tests

**Solutions**:
1. Check imports match actual package structure
2. Use full qualified names for ambiguous classes (`liquibase.change.Change`)
3. Remove JUnit 5 Suite annotations if dependencies not available
4. Use simplified ServiceRegistrationTest pattern (instantiation vs. service loader)

### Issue 9: SQL Generator Test Failures

**Symptom**: Expected SQL doesn't match generated SQL with quoted identifiers

**Solutions**:
1. Liquibase automatically escapes identifiers - expect double quotes
2. Test expectations should match `database.escapeObjectName()` behavior  
3. Use `assertTrue(sql.contains("expected_part"))` for complex SQL
4. Check for extra whitespace or formatting differences

### Issue 10: Validation Test Issues

**Symptom**: Validation tests failing when they should pass

**Solutions**:
1. Use null-safe Boolean comparisons: `Boolean.TRUE.equals(value)`
2. Check validation message text matches exactly (case-sensitive)
3. Verify validation logic handles null values correctly
4. Test each validation rule independently

### Issue 11: Duplicate @Override Annotations

**Symptom**: `java.lang.Override is not a repeatable annotation interface`

**Solutions**:
1. Check for accidentally duplicated `@Override` annotations
2. Remove duplicate annotations during automated fixes
3. Verify import statements don't conflict

---

## Key Assumptions and System Knowledge

### Liquibase Architecture

1. **Service Loader Pattern**: Liquibase uses Java ServiceLoader to discover implementations
2. **Namespace Architecture**: Extensions use XML namespaces for isolation
3. **Change/Statement/Generator Pattern**: Separation of concerns for flexibility
4. **Database Abstraction**: All code should work with Database interface

### Snowflake Specifics

1. **Object Names**: Always uppercase unless quoted
2. **Transient Objects**: Have 0 data retention, fail-safe after 1 day
3. **Managed Access**: Centralizes grant management
4. **OR REPLACE**: Different from DROP/CREATE - preserves grants

### Test Harness Specifics

1. **Persistent Database**: Snowflake persists between runs unlike Docker DBs
2. **DATABASECHANGELOG**: Tracks executed changesets - must be reset
3. **updateSql vs update**: updateSql shows pending changes only
4. **init.xml Purpose**: Resets database state for clean tests

### Common Patterns

1. **Boolean Attributes**: Use `Boolean` not `boolean` for null handling
2. **Validation**: Check required fields and mutual exclusivity
3. **SQL Building**: Use StringBuilder for complex SQL
4. **Error Messages**: Be specific about what failed

### Testing Philosophy

1. **Test First**: Write tests before implementation
2. **Step by Step**: Test each component in isolation
3. **Real Database**: Always test against actual Snowflake
4. **Document Failures**: Add to troubleshooting section

### File Locations

```
liquibase/
├── liquibase-snowflake/           # Extension code
│   ├── src/main/java/            # Implementation
│   ├── src/test/java/            # Unit tests
│   └── src/main/resources/       # Resources, XSD, services
├── liquibase-test-harness/       # Integration tests
│   └── src/main/resources/       # Test files
└── claude_guide/                 # Documentation
    └── project/requirements/     # Requirements docs
```

### Build and Deploy Process

1. Make changes in liquibase-snowflake
2. Run unit tests: `mvn test`
3. Build JAR: `mvn package -DskipTests`
4. Copy to test harness: `cp target/*.jar ../liquibase-test-harness/lib/`
5. Run test harness tests

### Version Compatibility

- Java 8 minimum (no `var` keyword)
- Liquibase 4.33.0+
- Snowflake JDBC 3.13.22+
- Maven 3.6+

---

## Appendix: Complete Examples

### CreateSchema Implementation
- **Requirements**: `claude_guide/project/requirements/detailed_requirements/createSchema_requirements.md`
- **Implementation**: `src/main/java/liquibase/change/core/CreateSchemaChange.java`
- **Tests**: `src/test/java/liquibase/change/core/CreateSchemaChangeTest.java`
  - `src/test/java/liquibase/statement/core/CreateSchemaStatementTest.java`
  - `src/test/java/liquibase/sqlgenerator/core/snowflake/CreateSchemaGeneratorSnowflakeTest.java`

### DropSchema Implementation  
- **Implementation**: `src/main/java/liquibase/change/core/DropSchemaChange.java`
- **Tests**: `src/test/java/liquibase/change/core/DropSchemaChangeTest.java`
  - `src/test/java/liquibase/statement/core/DropSchemaStatementTest.java`
  - `src/test/java/liquibase/sqlgenerator/core/snowflake/DropSchemaGeneratorSnowflakeTest.java`

### Test Organization
- **Overview**: `src/test/java/README_TEST_STRUCTURE.md`
- **Service Registration**: `src/test/java/liquibase/ServiceRegistrationTest.java`
- **Test Suite**: `src/test/java/liquibase/SnowflakeExtensionTestSuite.java`

### Verified Test Patterns
Both CreateSchema and DropSchema implementations follow this guide and have:
- ✅ Complete unit test coverage (Change, Statement, Generator)
- ✅ Comprehensive validation testing (required fields, mutual exclusivity)
- ✅ SQL generation testing (all variations, special characters)
- ✅ Integration with service registration
- ✅ All tests passing in Maven

Use these as reference implementations when creating new change types.