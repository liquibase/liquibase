# NEW_CHANGETYPE_PATTERN_2.md - Comprehensive Guide for Implementing New Liquibase Change Types

## 🛑 STOP: Before You Start

**When encountering ANY error during implementation or testing:**
1. **DO NOT assume where the bug is**
2. **DO NOT skip to "fixing" the assumed problem**
3. **DO follow the systematic debugging pattern in the Troubleshooting section**
4. **DO run the phase tests after EACH implementation phase**

**This guide contains phase tests specifically to prevent wasting hours on false assumptions.**

### 📝 HOW TO UPDATE THIS GUIDE

When you learn something new:
1. **Find where you needed it** (not at the end)
2. **Add it with a marker**: 🛑 ⚠️ 📌 ✅
3. **Keep it under 5 lines**
4. **Include exact error + fix**
5. **Show impact** ("wasted 4 hours")

Example:
```
⚠️ **ERROR**: Attribute 'X' is not allowed
**FIX**: Add to liquibase-snowflake-latest.xsd
```

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

Before writing any code, you MUST create a detailed requirements document.

**See**: `DETAILED_REQUIREMENTS_CREATION_GUIDE.md` for complete instructions on creating requirements.

**Location**: Requirements must be stored in `claude_guide/project/requirements/detailed_requirements/<changeTypeName>_requirements.md`

### Requirements Checklist

- [ ] Created requirements document following the guide
- [ ] Researched official database documentation  
- [ ] Documented all SQL syntax variations
- [ ] Created attribute analysis table
- [ ] Identified mutual exclusivity rules
- [ ] Planned test scenarios
- [ ] Defined validation rules

---

## Implementation Guide

### ⚡ MANDATORY: Test Each Phase Before Moving On

**DO NOT SKIP THESE TESTS** - They prevent wasted debugging time later!

After EACH phase below, you MUST run the corresponding test BEFORE proceeding:
- Phase 1 → Run Step1 test → Fix any issues → Only then proceed to Phase 2
- Phase 2 → Run Step2 test → Fix any issues → Only then proceed to Phase 3
- Phase 3 → Run Step3 test → Fix any issues → Only then proceed to Phase 4
- Phase 4 → Run Step4 test → Fix any issues → Only then proceed to Phase 5
- Phase 5 → Run Step5 test → Fix any issues → Only then proceed to Phase 6

**Real Example**: In alterSchema, skipping these tests would have made us waste hours on a non-existent "XML parsing bug"

---

### Phase 1: Create the Change Class

🚨 **CHECKPOINT: Requirements-Driven Development**
Before writing ANY code:
- [ ] Created detailed requirements document? (`detailed_requirements/<changeType>_requirements.md`)
- [ ] Compared existing implementation vs requirements? (if enhancing existing change type)
- [ ] Calculated coverage percentage? (e.g., alterSchema was only 70% complete)
- [ ] Listed all missing attributes/functionality?

**Why this matters**: In alterSchema, we discovered the existing implementation was missing 30% of required functionality (6 attributes). Always verify completeness first!

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

### 🧪 Quick Test for Phase 1

```bash
# Quick verification test - takes 30 seconds, saves hours of debugging
mvn test -Dtest=QuickChangeTest -Dtest.method=testPhase1

# Or create this minimal test:
@Test
public void testPhase1_ChangeClass() {
    <ChangeType>Change change = new <ChangeType>Change();
    change.set<RequiredAttribute>("TEST");
    
    // Must support Snowflake
    assertTrue(change.supports(new SnowflakeDatabase()));
    
    // Must generate statement
    SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
    assertEquals(1, stmts.length);
    
    System.out.println("✅ Phase 1 PASS - Change class works");
}
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

### 🧪 Quick Test for Phase 2

```java
@Test
public void testPhase2_Statement() {
    <ChangeType>Statement stmt = new <ChangeType>Statement();
    stmt.set<RequiredAttribute>("TEST");
    assertEquals("TEST", stmt.get<RequiredAttribute>());
    System.out.println("✅ Phase 2 PASS - Statement class works");
}
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

### 🧪 Quick Test for Phase 3

```java
@Test
public void testPhase3_SqlGenerator() {
    <ChangeType>Statement stmt = new <ChangeType>Statement();
    stmt.set<RequiredAttribute>("TEST_OBJECT");
    
    <ChangeType>GeneratorSnowflake gen = new <ChangeType>GeneratorSnowflake();
    Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
    
    assertTrue(sqls.length > 0);
    assertTrue(sqls[0].toSql().contains("CREATE"));
    System.out.println("✅ Phase 3 PASS - SQL: " + sqls[0].toSql());
}
```

### Phase 4: Service Registration

#### Register the Change Class
**File**: `src/main/resources/META-INF/services/liquibase.change.Change`
**Add line**: `liquibase.change.<database>.<ChangeType>Change<Database>`
**Example**: `liquibase.change.snowflake.CreateWarehouseChangeSnowflake`

#### Register the SQL Generator
**File**: `src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`  
**Add line**: `liquibase.sqlgenerator.core.<database>.<ChangeType>Generator<Database>`
**Example**: `liquibase.sqlgenerator.core.snowflake.CreateWarehouseGeneratorSnowflake`

### 🧪 Quick Test for Phase 4

```java
@Test
public void testPhase4_ServiceRegistration() {
    // Can we create the change via factory?
    Change change = ChangeFactory.getInstance().create("<changeType>");
    assertNotNull(change);
    System.out.println("✅ Phase 4 PASS - Services registered");
}
```

### Phase 5: XSD Schema Update

⚠️ **CRITICAL: XSD Update is MANDATORY**
**Common Error**: `Attribute 'X' is not allowed to appear in element 'snowflake:changeType'`
**Cause**: Missing XSD update when adding new XML attributes
**Solution**: ALWAYS update liquibase-snowflake-latest.xsd when adding ANY new attributes

**Real Example**: In alterSchema, we added `ifExists` attribute but integration tests failed until XSD was updated.

**File**: `src/main/resources/www.liquibase.org/xml/ns/<database>/liquibase-<database>-latest.xsd`
**Example**: `src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd`

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

### 🧪 Quick Test for Phase 5 

```java
@Test
public void testPhase5_XsdParsing() {
    // Just verify XSD exists - full XML parsing tested in Phase 6
    InputStream xsd = getClass().getResourceAsStream(
        "/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd");
    assertNotNull(xsd);
    System.out.println("✅ Phase 5 PASS - XSD exists");
}
```

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

**File**: `src/test/java/liquibase/change/<database>/<ChangeType>Change<Database>Test.java`
**Example**: `src/test/java/liquibase/change/snowflake/CreateWarehouseChangeSnowflakeTest.java`

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

**File**: `src/test/java/liquibase/statement/<database>/<ChangeType>Statement<Database>Test.java`
**Example**: `src/test/java/liquibase/statement/snowflake/CreateWarehouseStatementSnowflakeTest.java`

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

**File**: `src/test/java/liquibase/sqlgenerator/core/<database>/<ChangeType>Generator<Database>Test.java`
**Example**: `src/test/java/liquibase/sqlgenerator/core/snowflake/CreateWarehouseGeneratorSnowflakeTest.java`

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

⚠️ **INTEGRATION TEST TIP: Use Unique Changeset IDs**
**Common Error**: `Validation Failed: 1 changesets check sum`
**Cause**: Reusing common changeset IDs like "test-1" causes checksum conflicts in DATABASECHANGELOG
**Solution**: Use descriptive, unique IDs: "test-alter-retention", "test-alter-comment", etc.

```java
// ❌ BAD: Generic IDs cause conflicts
<changeSet id="test-1" author="test">

// ✅ GOOD: Descriptive unique IDs
<changeSet id="test-alterSchema-retention" author="test">
<changeSet id="test-alterSchema-comment" author="test">
```

```java
// <ChangeType> tests
<ChangeType>ChangeTest.class,
<ChangeType>StatementTest.class,
<ChangeType>GeneratorSnowflakeTest.class,
```

---

### 🎯 Complete Phase Tests in One File

Create a single test file with all phase tests:

```java
public class <ChangeType>PhaseTests {
    
    @Test
    public void testPhase1_ChangeClass() {
        <ChangeType>Change change = new <ChangeType>Change();
        change.set<RequiredAttribute>("TEST");
        assertTrue(change.supports(new SnowflakeDatabase()));
        assertEquals(1, change.generateStatements(new SnowflakeDatabase()).length);
        System.out.println("✅ Phase 1 PASS");
    }
    
    @Test 
    public void testPhase2_Statement() {
        <ChangeType>Statement stmt = new <ChangeType>Statement();
        stmt.set<RequiredAttribute>("TEST");
        assertEquals("TEST", stmt.get<RequiredAttribute>());
        System.out.println("✅ Phase 2 PASS");
    }
    
    @Test
    public void testPhase3_SqlGenerator() {
        <ChangeType>Statement stmt = new <ChangeType>Statement();
        stmt.set<RequiredAttribute>("TEST");
        <ChangeType>GeneratorSnowflake gen = new <ChangeType>GeneratorSnowflake();
        Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
        assertTrue(sqls[0].toSql().contains("CREATE"));
        System.out.println("✅ Phase 3 PASS: " + sqls[0].toSql());
    }
    
    @Test
    public void testPhase4_ServiceRegistration() {
        Change change = ChangeFactory.getInstance().create("<changeType>");
        assertNotNull(change);
        System.out.println("✅ Phase 4 PASS");
    }
    
    @Test
    public void testPhase6_FullIntegration() throws Exception {
        // Create simple XML and parse it
        String xml = "<changeSet id='1' author='test'>" +
                    "<snowflake:<changeType> <requiredAttribute>='TEST'/>" + 
                    "</changeSet>";
        // Parse and verify it works end-to-end
        System.out.println("✅ Phase 6 PASS - Full integration works");
    }
}
```

**Run after each phase:**
```bash
mvn test -Dtest=<ChangeType>PhaseTests -Dtest.method=testPhase*
```

### 📌 TEST RUNNING REFERENCE (Because I Always Forget This)

**Running Unit Tests in liquibase-snowflake:**
```bash
# Run ALL tests for a change type
mvn test -Dtest="*AlterSchema*"

# Run specific test class
mvn test -Dtest=AlterSchemaChangeTest

# Run specific test method
mvn test -Dtest=AlterSchemaChangeTest#testBasicProperties

# Run with more output when debugging
mvn test -Dtest=AlterSchemaChangeTest -X

# COMMON MISTAKES I MAKE:
# ❌ mvn test AlterSchemaChangeTest  (missing -Dtest=)
# ❌ mvn test -Dtest="AlterSchemaChangeTest.java"  (no .java extension)
# ❌ mvn test -Dtest=AlterSchemaChange  (missing Test suffix)
```

**Integration Test Gotchas:**
```bash
# Integration tests need proper setup
cd liquibase-snowflake
mvn clean install -DskipTests  # Build first
mvn test -Dtest=SnowflakeIntegrationTest

# If "class not found":
mvn clean test-compile  # Recompile tests
mvn test -Dtest=YourTest

# If weird errors:
mvn clean  # Clear target directory
mvn test-compile
mvn test -Dtest=YourTest
```

**Test Harness (Different Project!):**
```bash
# CRITICAL: Test harness is NOT in liquibase-snowflake
cd liquibase-test-harness  # Different directory!

# Must install JAR first - test harness loads via Maven dependencies
cd ../liquibase-snowflake && mvn install -DskipTests && cd ../liquibase-test-harness

# Run test harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=alterSchema -DdbName=snowflake

# Multiple tests
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema,alterSchema -DdbName=snowflake
```

---

## Test Harness Implementation (Database Testing)

### IMPORTANT: This is Different from Unit/Integration Tests

Test Harness tests:
- Run in a **completely separate project** (`liquibase-test-harness`)
- Require a **real Snowflake database connection**
- Test **actual SQL execution** against the database
- Verify **database objects are created correctly**
- Compare **expected vs actual SQL output**

### Test Harness File Locations
- **Test XML Files**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/<database>/<changeType>.xml`
- **Expected SQL**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSql/<database>/<changeType>.sql`
- **Expected Snapshot**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/expectedSnapshot/<database>/<changeType>.json`
- **Global Cleanup**: `liquibase-test-harness/src/test/resources/liquibase/harness/change/changelogs/<database>/cleanup.xml`

### CRITICAL: Test Harness Pattern

🔍 **ALWAYS QUESTION "KNOWN ISSUES"**
If you find disabled tests with comments like:
```xml
<!-- DISABLED DUE TO XML PARSING BUG -->
<!-- <changeSet id="test-unset" author="test">
    <snowflake:alterSchema unsetDataRetentionTimeInDays="true"/>
</changeSet> -->
```

**DO NOT ACCEPT THIS AT FACE VALUE!**
1. Create an isolated test to verify the bug actually exists
2. Often the "known bug" is a false assumption
3. In alterSchema, the "UNSET XML parsing bug" didn't exist - the test was disabled incorrectly

**Action**: Always verify with concrete evidence before perpetuating "known bugs"

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
# First, rebuild and install the extension JAR
cd liquibase-snowflake
mvn install -DskipTests

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

### 🎯 SYSTEMATIC DEBUGGING PATTERN

When encountering mysterious errors (like "At least one schema property must be changed" with boolean attributes):

**STOP** - Don't assume where the bug is!

**Create isolated tests for each layer:**

```java
// Step 1: Test Change class directly (programmatic)
@Test
public void debugStep1_ChangeClass() {
    AlterSchemaChange change = new AlterSchemaChange();
    change.setUnsetDataRetentionTimeInDays(true);
    System.out.println("Value set: " + change.getUnsetDataRetentionTimeInDays());
    // If this works, issue is NOT in Change class
}

// Step 2: Test Statement generation
@Test
public void debugStep2_Statement() {
    AlterSchemaChange change = new AlterSchemaChange();
    change.setSchemaName("TEST");
    change.setUnsetDataRetentionTimeInDays(true);
    SqlStatement[] stmts = change.generateStatements(new SnowflakeDatabase());
    AlterSchemaStatement stmt = (AlterSchemaStatement) stmts[0];
    System.out.println("Statement value: " + stmt.getUnsetDataRetentionTimeInDays());
    // If this works, issue is NOT in Statement generation
}

// Step 3: Test SQL generation
@Test
public void debugStep3_SqlGeneration() {
    AlterSchemaStatement stmt = new AlterSchemaStatement();
    stmt.setSchemaName("TEST");
    stmt.setUnsetDataRetentionTimeInDays(true);
    AlterSchemaGeneratorSnowflake gen = new AlterSchemaGeneratorSnowflake();
    Sql[] sqls = gen.generateSql(stmt, new SnowflakeDatabase(), null);
    System.out.println("Generated SQL: " + sqls[0].toSql());
    // If this contains UNSET, SQL generation works
}

// Step 4: Test XML parsing
@Test
public void debugStep4_XmlParsing() {
    String xml = "<snowflake:alterSchema schemaName=\"TEST\" unsetDataRetentionTimeInDays=\"true\"/>";
    // Parse and check if boolean is set correctly
}
```

**Real Example**: This approach revealed that the "UNSET XML parsing bug" in alterSchema was actually a false assumption - XML parsing worked perfectly all along!

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

### Issue 12: SqlGeneratorChain Constructor Error

**Symptom**: `constructor SqlGeneratorChain in class liquibase.sqlgenerator.SqlGeneratorChain<T> cannot be applied to given types`

**Quick Fix**: Pass `null` instead of `new SqlGeneratorChain()` in test methods
```java
// ❌ WRONG
Sql[] sqls = generator.generateSql(statement, database, new SqlGeneratorChain());

// ✅ CORRECT
Sql[] sqls = generator.generateSql(statement, database, null);
```

**Why**: The SqlGeneratorChain constructor requires specific parameters. In unit tests, passing null is acceptable and standard practice.

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
4. Install to Maven repo: `mvn install -DskipTests` (test harness loads via Maven dependencies)
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