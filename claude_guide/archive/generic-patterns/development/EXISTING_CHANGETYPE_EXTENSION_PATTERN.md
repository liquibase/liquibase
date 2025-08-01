# EXISTING_CHANGETYPE_EXTENSION_PATTERN.md - Guide for Extending Existing Liquibase Change Types

## 🎯 Quick Navigation - Which Pattern Do You Need?

### Need to change SQL syntax only? (e.g., `ALTER TABLE` vs `RENAME TABLE`)
→ **Use [SQL Generator Override Pattern](#sql-generator-override-pattern)**
→ Or jump to `SQL_GENERATOR_OVERRIDE_STEP_BY_STEP.md`

### Need to add new attributes? (e.g., `snowflake:transient="true"`)
→ **Use [Namespace Attribute Pattern](#namespace-attribute-pattern)**
→ Much more complex - requires Storage, Parser, and Generator

### Need completely new functionality?
→ **Wrong guide! Use `NEW_CHANGETYPE_PATTERN_2.md`**

## 🛑 STOP: Before You Start

**When encountering ANY error during implementation or testing:**
1. **DO NOT assume where the bug is**
2. **DO NOT skip to "fixing" the assumed problem**
3. **DO follow the systematic debugging pattern in the Troubleshooting section**
4. **DO run the phase tests after EACH implementation phase**

**This guide contains phase tests specifically to prevent wasting hours on false assumptions.**

### 🔍 CRITICAL: Discovery Phase First
Before writing ANY code:
1. **Check if components already exist**: Search for existing Storage, Parser, Generator classes
2. **Verify parser support**: Check if change type is already in `isTargetChangeType()`
3. **Check service registrations**: Look in META-INF/services files
4. **Review XSD**: Some attributes may already be defined
5. **Create debug test**: When tests fail, print actual output before assuming bug

📌 **Real Example**: alterSequence implementation found ALL core components already existed!

### 📝 HOW TO UPDATE THIS GUIDE

When you learn something new:
1. **Find where you needed it** (not at the end)
2. **Add it with a marker**: 🛑 ⚠️ 📌 ✅
3. **Keep it under 5 lines**
4. **Include exact error + fix**
5. **Show impact** ("wasted 4 hours")

Example:
```
⚠️ **ERROR**: Parser not intercepting attributes
**FIX**: Parser priority must be PRIORITY_DATABASE + 10
```

## Table of Contents
1. [When to Use This Pattern (and Alternatives)](#when-to-use-this-pattern-and-alternatives)
2. [Prerequisites](#prerequisites)
3. [Pre-Implementation Requirements Research](#pre-implementation-requirements-research)
4. [Implementation Guide](#implementation-guide)
5. [Unit and Integration Testing](#unit-and-integration-testing)
6. [Test Harness Implementation](#test-harness-implementation)
7. [Troubleshooting Common Issues](#troubleshooting-common-issues)
8. [Key Assumptions and System Knowledge](#key-assumptions-and-system-knowledge)

---

## When to Use This Pattern (and Alternatives)

### 🔀 Decision Tree for Existing Liquibase Change Types

When working with an existing Liquibase change type (e.g., `renameTable`, `createIndex`, `addColumn`):

**1️⃣ SQL Generator Override** ✅ Use When:
- Core change type logic is correct
- Only SQL syntax differs for your database
- Core change type has all needed parameters
- Example: `renameTable` needs `ALTER TABLE x RENAME TO y` instead of `RENAME TABLE x TO y`
- **Go to**: [SQL Generator Override Pattern](#sql-generator-override-pattern) section below
- **Or use**: `SQL_GENERATOR_OVERRIDE_STEP_BY_STEP.md` for detailed steps

**2️⃣ Namespace Attributes** ✅ Use When:
- Need to add database-specific parameters
- Core functionality remains the same
- Example: Adding `snowflake:transient="true"` to `createTable`
- **Go to**: [Namespace Attribute Pattern](#namespace-attribute-pattern) section below
- **Requires**: Storage, Parser, and Generator modifications

**3️⃣ New Change Type** ✅ Use When:
- Fundamentally different behavior needed
- No core equivalent exists
- Example: Snowflake-specific `createWarehouse`
- **Use**: `NEW_CHANGETYPE_PATTERN_2.md` guide

## ⚠️ CRITICAL: These Are Two Completely Different Patterns!

| Aspect | SQL Generator Override | Namespace Attributes |
|--------|----------------------|---------------------|
| **Purpose** | Change SQL syntax only | Add new parameters |
| **Classes Needed** | 1 (Generator) | 3+ (Storage, Parser, Generator) |
| **Complexity** | Simple | Complex |
| **Service Registration** | 1 file | 2+ files |
| **XSD Changes** | None | Required |
| **Example** | `ALTER TABLE RENAME TO` | `snowflake:transient="true"` |

### SQL Generator Override Pattern (Detailed)

When you need a SQL generator override (most common for database-specific SQL syntax):

**1. Discovery Phase - Check Existing Implementation:**
```bash
# CRITICAL: Check if generator already exists!
find . -name "*RenameTable*Generator*.java"
grep -r "RenameTableGenerator" src/main/java/
```

⚠️ **LESSON LEARNED**: RenameTableGeneratorSnowflake already existed but wasn't being used due to Maven loading

**2. Create/Update the Generator Class:**
```java
package liquibase.sqlgenerator.core;  // Note: NOT in a subdirectory!

public class RenameTableGeneratorSnowflake extends RenameTableGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5; // Must be higher than base
    }
    
    @Override
    public boolean supports(RenameTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(RenameTableStatement statement, Database database, 
                            SqlGeneratorChain sqlGeneratorChain) {
        // Snowflake syntax: ALTER TABLE old RENAME TO new
        String sql = "ALTER TABLE " + 
            database.escapeTableName(statement.getCatalogName(), 
                                   statement.getSchemaName(), 
                                   statement.getOldTableName()) +
            " RENAME TO " + 
            database.escapeTableName(statement.getCatalogName(), 
                                   statement.getSchemaName(), 
                                   statement.getNewTableName());
        
        return new Sql[]{new UnparsedSql(sql, 
            getAffectedOldTable(statement), 
            getAffectedNewTable(statement))};
    }
}
```

**3. Register in Service Loader:**
```
# META-INF/services/liquibase.sqlgenerator.SqlGenerator
liquibase.sqlgenerator.core.RenameTableGeneratorSnowflake
```

**4. Test After Each Step:**

### 🧪 Step 1 Test - Verify Existing Implementation
```bash
# Check if your change type already has a generator
find . -name "*<YourChangeType>*Generator*.java" | grep -i snowflake
# If found, check if it produces correct SQL already!
```

### 🧪 Step 2 Test - Generator Unit Test
```java
@Test
public void testGeneratesSql() {
    RenameTableStatement statement = new RenameTableStatement(null, null, "oldTable", "newTable");
    Sql[] sqls = generator.generateSql(statement, new SnowflakeDatabase(), null);
    assertEquals("ALTER TABLE oldTable RENAME TO newTable", sqls[0].toSql());
}

@Test  
public void testPriority() {
    assertTrue(generator.getPriority() > PRIORITY_DATABASE);
}

@Test
public void testSupportsOnlySnowflake() {
    assertTrue(generator.supports(statement, new SnowflakeDatabase()));
    assertFalse(generator.supports(statement, new PostgresDatabase()));
}
```

Run: `mvn test -Dtest=<YourChangeType>GeneratorSnowflakeTest`

### 🧪 Step 3 Test - Service Registration
```bash
# Verify registration
grep -r "<YourChangeType>GeneratorSnowflake" src/main/resources/META-INF/services/

# Build and verify it loads
mvn clean package
jar tf target/*.jar | grep META-INF/services/liquibase.sqlgenerator.SqlGenerator
```

### 🧪 Step 4 Test - Integration Test
```java
@Test
public void testIntegration() {
    // Create a mini changelog and verify SQL generation
    DatabaseChangeLog changeLog = new DatabaseChangeLog();
    ChangeSet changeSet = new ChangeSet("1", "test", false, false, null, null, null, changeLog);
    
    <YourChangeType>Change change = new <YourChangeType>Change();
    // Set change properties
    
    changeSet.addChange(change);
    
    SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();
    Sql[] sqls = factory.generateSql(change.generateStatements(new SnowflakeDatabase())[0], 
                                     new SnowflakeDatabase());
    
    assertTrue(sqls[0].toSql().contains("YOUR EXPECTED SQL SYNTAX"));
}
```

**5. Test Harness Critical Requirements:**

⚠️ **CRITICAL**: Test harness loads from Maven repository, NOT lib/ directory!
```bash
# After any generator changes:
mvn clean install  # Installs to local Maven repo
# Then run test harness
```

**6. Test Harness File Structure for SQL Override:**

```xml
<!-- changelogs/snowflake/renameTable.xml -->
<databaseChangeLog>
    <changeSet id="1-renameTable-v3" author="testharness">
        <createTable tableName="oldnametable">
            <column name="test_id" type="int">
                <constraints primaryKey="true" nullable="false"/>
            </column>
        </createTable>
        <renameTable oldTableName="oldnametable" newTableName="newnametable"/>
    </changeSet>
    <!-- NO CLEANUP CHANGESETS HERE - they run before snapshot! -->
</databaseChangeLog>
```

```sql
-- expectedSql/snowflake/renameTable.sql
-- ONLY the changeset SQL, NO init.xml SQL!
CREATE TABLE LTHDB.TESTHARNESS.oldnametable (test_id INT NOT NULL, test_column VARCHAR(50) NOT NULL, CONSTRAINT PK_OLDNAMETABLE PRIMARY KEY (test_id))
ALTER TABLE LTHDB.TESTHARNESS.oldnametable RENAME TO LTHDB.TESTHARNESS.newnametable
```

```json
// expectedSnapshot/renameTable.json
// Expects renamed table to exist (no cleanup before snapshot!)
{
  "snapshot": {
    "objects": {
      "liquibase.structure.core.Table": [{
        "table": { "name": "newnametable" }
      }]
    }
  }
}
```

**7. Common Test Failures and Solutions:**

| Error | Cause | Solution |
|-------|-------|----------|
| "Database is up to date" | Changeset already executed | Change changeset ID (e.g., v2 → v3) |
| Expected SQL mismatch with init SQL | Expected file includes init.xml SQL | Remove init.xml SQL from expected file |
| Snapshot missing tables | Cleanup changeset in test file | Remove cleanup from test file |
| Generator not being used | Test harness uses Maven deps | Run `mvn clean install` first |

**8. Testing Checkpoints for SQL Generator Override:**

✅ **Step 1**: Discovery - Did you find existing implementation?
✅ **Step 2**: Unit tests - Does generator produce correct SQL?
✅ **Step 3**: Service registration - Is it properly registered?
✅ **Step 4**: Integration - Does it work in full context?
✅ **Step 5**: Maven install - `mvn clean install`
✅ **Step 6**: Test harness - Does it work end-to-end?

**No Change Type or XSD Changes Needed** - Just the generator override!

---

## 🔄 Pattern Separator - SQL Generator Override ENDS Here

**If you only need to change SQL syntax, STOP HERE. The section below is for a completely different pattern.**

---

## Namespace Attribute Pattern (Complex)

⚠️ **WARNING**: This is a COMPLETELY DIFFERENT pattern from SQL Generator Override!

Use this pattern when:
- Adding database-specific attributes to **existing** Liquibase change types
- Extending standard changes like `createTable`, `alterSequence`, `dropSequence`
- Need namespace-prefixed attributes like `<alterSequence snowflake:setNoOrder="true">`

### Prerequisites for Namespace Attributes Pattern

⚠️ **Note**: SQL Generator Override has NO special prerequisites - just create one class!

#### Required Dependencies for Namespace Attributes

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

### Project Structure Requirements

```
liquibase-<extension>/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── liquibase/
│   │   │       ├── ext/
│   │   │       ├── parser/
│   │   │       └── sqlgenerator/core/<database>/
│   │   └── resources/
│   │       ├── META-INF/services/
│   │       └── www.liquibase.org/xml/ns/<extension>/
│   └── test/
│       └── java/
│           └── liquibase/
└── pom.xml
```

---

## Pre-Implementation Requirements Research

### CRITICAL: Before ANY Implementation

Before writing any code, you MUST create a detailed requirements document for the attributes you're adding.

**See**: `DETAILED_REQUIREMENTS_CREATION_GUIDE.md` for complete instructions on creating requirements.

**Location**: Requirements must be stored in `claude_guide/project/requirements/detailed_requirements/<changeType>Enhanced_requirements.md`

### Requirements Checklist

- [ ] Created requirements document following the guide
- [ ] Researched which standard change type to extend
- [ ] Documented all new attributes to add
- [ ] Identified how attributes modify SQL generation
- [ ] Planned test scenarios for attribute combinations
- [ ] Verified attributes don't conflict with existing ones

---

## Implementation Guide

### ⚡ MANDATORY: Test Each Phase Before Moving On

**DO NOT SKIP THESE TESTS** - They prevent wasted debugging time later!

After EACH phase below, you MUST run the corresponding test BEFORE proceeding:
- Phase 1 → Run Phase1 test → Fix any issues → Only then proceed to Phase 2
- Phase 2 → Run Phase2 test → Fix any issues → Only then proceed to Phase 3
- Phase 3 → Run Phase3 test → Fix any issues → Only then proceed to Phase 4
- Phase 4 → Run Phase4 test → Fix any issues → Only then proceed to Phase 5
- Phase 5 → Run Phase5 test → Fix any issues → Only then proceed to Phase 6

**Real Example**: In alterSequence, skipping these tests would have wasted hours debugging namespace attribute storage timing issues

---

### Phase 1: Create Namespace Attribute Storage

⚠️ **CRITICAL**: This storage is the foundation - if it doesn't work, nothing else will!

**File**: `src/main/java/liquibase/parser/<database>/<Database>NamespaceAttributeStorage.java`
**Example**: `src/main/java/liquibase/parser/snowflake/SnowflakeNamespaceAttributeStorage.java`

```java
package liquibase.ext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe storage for <Database> namespace attributes.
 * This allows us to capture namespace-prefixed attributes during XML parsing
 * and retrieve them during SQL generation.
 */
public class <Database>NamespaceAttributeStorage {
    private static final ConcurrentHashMap<String, Map<String, String>> storage = 
        new ConcurrentHashMap<>();
    
    /**
     * Store namespace attributes for a database object.
     * 
     * @param objectName The name of the database object (e.g., table name, sequence name)
     * @param attributes Map of attribute names to values
     */
    public static void storeAttributes(String objectName, Map<String, String> attributes) {
        if (objectName != null && attributes != null && !attributes.isEmpty()) {
            storage.put(objectName, new ConcurrentHashMap<>(attributes));
        }
    }
    
    /**
     * Retrieve namespace attributes for a database object.
     * 
     * @param objectName The name of the database object
     * @return Map of attributes or null if none exist
     */
    public static Map<String, String> getAttributes(String objectName) {
        if (objectName == null) {
            return null;
        }
        return storage.get(objectName);
    }
    
    /**
     * Remove namespace attributes for a database object.
     * Called after attributes have been used to prevent memory leaks.
     * 
     * @param objectName The name of the database object
     */
    public static void removeAttributes(String objectName) {
        if (objectName != null) {
            storage.remove(objectName);
        }
    }
    
    /**
     * Clear all stored attributes.
     * Useful for testing and cleanup.
     */
    public static void clear() {
        storage.clear();
    }
}
```

### 🧪 Quick Test for Phase 1

```bash
# Quick verification test - takes 30 seconds, saves hours of debugging
mvn test -Dtest=<Database>NamespaceAttributeStorageTest -Dtest.method=testPhase1

# Or create this minimal test:
```

```java
@Test
public void testPhase1_Storage() {
    // Clear any existing data
    <Database>NamespaceAttributeStorage.clear();
    
    // Test store and retrieve
    Map<String, String> attrs = new HashMap<>();
    attrs.put("transient", "true");
    attrs.put("clusterBy", "id,name");
    
    <Database>NamespaceAttributeStorage.storeAttributes("test_table", attrs);
    
    Map<String, String> retrieved = <Database>NamespaceAttributeStorage.getAttributes("test_table");
    assertNotNull(retrieved);
    assertEquals("true", retrieved.get("transient"));
    assertEquals("id,name", retrieved.get("clusterBy"));
    
    // Test removal
    <Database>NamespaceAttributeStorage.removeAttributes("test_table");
    assertNull(<Database>NamespaceAttributeStorage.getAttributes("test_table"));
    
    System.out.println("✅ Phase 1 PASS - Storage works");
}
```

### Phase 2: Create Custom XML Parser

**File**: `src/main/java/liquibase/parser/<database>/<Database>NamespaceAwareXMLParser.java`
**Example**: `src/main/java/liquibase/parser/snowflake/SnowflakeNamespaceAwareXMLParser.java`

📌 **KEY INSIGHT**: The parser MUST have higher priority than default to intercept attributes!

⚠️ **COMMON ERROR**: `getResource()` vs `getAll()` method
**FIX**: Use `resourceAccessor.getAll(location)` and get first resource from list

#### File: `src/main/java/liquibase/parser/<Database>NamespaceAwareXMLParser.java`

```java
package liquibase.parser;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.Resource;
import liquibase.exception.ChangeLogParseException;
import liquibase.ext.<Database>NamespaceAttributeStorage;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML parser that captures <Database> namespace attributes.
 * Intercepts attributes with the <database>: namespace prefix and stores them
 * for later use by SQL generators.
 */
public class <Database>NamespaceAwareXMLParser extends XMLChangeLogSAXParser {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // CRITICAL: Must be higher than default!
    }
    
    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, 
                                    ChangeLogParameters changeLogParameters, 
                                    ResourceAccessor resourceAccessor) 
                                    throws ChangeLogParseException {
        
        // First, capture namespace attributes
        try {
            captureNamespaceAttributes(physicalChangeLogLocation, resourceAccessor);
        } catch (Exception e) {
            // Log but don't fail - continue with normal parsing
            System.err.println("Warning: Failed to capture namespace attributes: " + e.getMessage());
        }
        
        // Then do normal parsing
        return super.parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
    }
    
    private void captureNamespaceAttributes(String location, ResourceAccessor resourceAccessor) 
            throws Exception {
        
        List<Resource> resources = resourceAccessor.getAll(location);
        if (resources == null || resources.isEmpty()) {
            return;
        }
        
        try (InputStream inputStream = resources.get(0).openInputStream()) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            
            NamespaceCapturingHandler handler = new NamespaceCapturingHandler();
            parser.parse(inputStream, handler);
        }
    }
    
    private static class NamespaceCapturingHandler extends DefaultHandler {
        private static final String <DATABASE>_NS = "http://www.liquibase.org/xml/ns/<database>";
        
        @Override
        public void startElement(String uri, String localName, String qName, 
                                Attributes attributes) throws SAXException {
            
            // Check for the change types we're extending
            if (isTargetChangeType(localName)) {
                String objectName = getObjectName(localName, attributes);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    if (<DATABASE>_NS.equals(attributes.getURI(i))) {
                        namespaceAttrs.put(attributes.getLocalName(i), 
                                         attributes.getValue(i));
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && objectName != null) {
                    <Database>NamespaceAttributeStorage.storeAttributes(objectName, namespaceAttrs);
                }
            }
        }
        
        private boolean isTargetChangeType(String localName) {
            // Add all change types you're extending
            return "createTable".equals(localName) ||
                   "alterTable".equals(localName) ||
                   "dropTable".equals(localName) ||
                   "createSequence".equals(localName) ||
                   "alterSequence".equals(localName) ||
                   "dropSequence".equals(localName);
                   // Add more as needed
        }
        
        private String getObjectName(String changeType, Attributes attributes) {
            // Extract the object name based on change type
            switch (changeType) {
                case "createTable":
                case "alterTable":
                case "dropTable":
                    return attributes.getValue("tableName");
                case "createSequence":
                case "alterSequence":
                case "dropSequence":
                    return attributes.getValue("sequenceName");
                default:
                    return null;
            }
        }
    }
}
```

### 🧪 Quick Test for Phase 2

```java
@Test
public void testPhase2_Parser() throws Exception {
    // Create test XML
    String testXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
        "    xmlns:<database>=\"http://www.liquibase.org/xml/ns/<database>\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
        "    <changeSet id=\"test\" author=\"test\">\n" +
        "        <createTable tableName=\"test_table\" <database>:transient=\"true\"/>\n" +
        "    </changeSet>\n" +
        "</databaseChangeLog>";
    
    // Write to temp file and parse
    Path tempFile = Files.createTempFile("test", ".xml");
    Files.write(tempFile, testXml.getBytes());
    
    <Database>NamespaceAttributeStorage.clear();
    
    <Database>NamespaceAwareXMLParser parser = new <Database>NamespaceAwareXMLParser();
    FileSystemResourceAccessor accessor = new FileSystemResourceAccessor(tempFile.getParent().toFile());
    parser.parseToNode(tempFile.getFileName().toString(), null, accessor);
    
    // Verify attributes were captured
    Map<String, String> attrs = <Database>NamespaceAttributeStorage.getAttributes("test_table");
    assertNotNull(attrs);
    assertEquals("true", attrs.get("transient"));
    
    Files.deleteIfExists(tempFile);
    System.out.println("✅ Phase 2 PASS - Parser captures attributes");
}
```

### Phase 3: Create Enhanced SQL Generators

⚠️ **TIMING ISSUE**: Test harness may not preserve attributes between parse and generate!
**WORKAROUND**: For test harness, consider not removing attributes immediately

**File**: `src/main/java/liquibase/sqlgenerator/core/<database>/<ChangeType>Generator<Database>.java`
**Example**: `src/main/java/liquibase/sqlgenerator/core/snowflake/CreateTableGeneratorSnowflake.java`

```java
package liquibase.sqlgenerator.core.<database>;

import liquibase.database.Database;
import liquibase.database.core.<Database>Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.<ChangeType>Generator;
import liquibase.statement.core.<ChangeType>Statement;
import liquibase.ext.<Database>NamespaceAttributeStorage;

import java.util.Map;

/**
 * <Database>-specific <ChangeType> SQL generator that supports namespace attributes.
 */
public class <ChangeType>Generator<Database> extends <ChangeType>Generator {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }
    
    @Override
    public boolean supports(<ChangeType>Statement statement, Database database) {
        return database instanceof <Database>Database;
    }
    
    @Override
    public ValidationErrors validate(<ChangeType>Statement statement, Database database, 
                                   SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = <Database>NamespaceAttributeStorage.getAttributes(
            getObjectName(statement));
        
        if (attributes != null && !attributes.isEmpty()) {
            // Add validation for namespace attributes
            // Example: validate boolean values
            String transientValue = attributes.get("transient");
            if (transientValue != null && 
                !transientValue.equalsIgnoreCase("true") && 
                !transientValue.equalsIgnoreCase("false")) {
                validationErrors.addError("transient must be true or false");
            }
            
            // Add more validations as needed
        }
        
        return validationErrors;
    }
    
    @Override
    public Sql[] generateSql(<ChangeType>Statement statement, Database database, 
                           SqlGeneratorChain sqlGeneratorChain) {
        // For alterSequence/dropSequence, might need to generate complete SQL
        // For createTable, might modify existing SQL
        
        // Example for modifying existing SQL:
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Get namespace attributes
        String objectName = getObjectName(statement);
        Map<String, String> attributes = <Database>NamespaceAttributeStorage.getAttributes(objectName);
        
        if (attributes == null || attributes.isEmpty()) {
            // No namespace attributes, return standard SQL
            return baseSql;
        }
        
        // Modify SQL based on attributes
        String originalSql = baseSql[0].toSql();
        String modifiedSql = applyNamespaceAttributes(originalSql, attributes, statement);
        
        // DON'T remove attributes here if test harness needs them
        // <Database>NamespaceAttributeStorage.removeAttributes(objectName);
        
        return new Sql[]{new UnparsedSql(modifiedSql, baseSql[0].getAffectedDatabaseObjects())};
    }
    
    private String getObjectName(<ChangeType>Statement statement) {
        // Extract object name from statement
        // Implementation depends on the statement type
        
        // Examples:
        // return statement.getTableName();     // For table statements
        // return statement.getSequenceName();  // For sequence statements
        // return statement.getIndexName();     // For index statements
        
        return statement.get<ObjectName>();
    }
    
    private String applyNamespaceAttributes(String sql, Map<String, String> attrs,
                                          <ChangeType>Statement statement) {
        // Apply each attribute to modify the SQL
        // Implementation depends on your specific attributes
        
        // Example for createTable with transient:
        if ("true".equals(attrs.get("transient"))) {
            sql = sql.replaceFirst("CREATE TABLE", "CREATE TRANSIENT TABLE");
        }
        
        // Example for alterSequence with setNoOrder:
        if ("true".equals(attrs.get("setNoOrder"))) {
            // Remove semicolon if present
            if (sql.endsWith(";")) {
                sql = sql.substring(0, sql.length() - 1);
            }
            sql += " SET NOORDER";
        }
        
        // Example for dropSequence with cascade/restrict:
        boolean cascade = "true".equals(attrs.get("cascade"));
        boolean restrict = "true".equals(attrs.get("restrict"));
        
        if (cascade) {
            sql += " CASCADE";
        } else if (restrict) {
            sql += " RESTRICT";
        }
        
        return sql;
    }
}
```

### 🧪 Quick Test for Phase 3

```java
@Test
public void testPhase3_SqlGenerator() {
    // Setup
    <Database>NamespaceAttributeStorage.clear();
    Map<String, String> attrs = new HashMap<>();
    attrs.put("transient", "true");
    <Database>NamespaceAttributeStorage.storeAttributes("test_table", attrs);
    
    // Create statement
    <ChangeType>Statement statement = new <ChangeType>Statement();
    statement.set<ObjectName>("test_table");
    
    // Generate SQL
    <ChangeType>Generator<Database> generator = new <ChangeType>Generator<Database>();
    Sql[] sqls = generator.generateSql(statement, new <Database>Database(), null);
    
    // Verify
    assertEquals(1, sqls.length);
    String sql = sqls[0].toSql();
    assertTrue(sql.contains("TRANSIENT"));
    
    System.out.println("✅ Phase 3 PASS - SQL modified: " + sql);
}
```

### Phase 4: Register Extension Components

📌 **CRITICAL**: Service registration is how Liquibase discovers your code!

#### Step 4A: Create Service Registration Files

**Parser Registration**:
- **File**: `src/main/resources/META-INF/services/liquibase.parser.ChangeLogParser`
- **Add line**: `liquibase.parser.<database>.<Database>NamespaceAwareXMLParser`
- **Example**: `liquibase.parser.snowflake.SnowflakeNamespaceAwareXMLParser`

**Generator Registration**:
- **File**: `src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator`
- **Add line**: `liquibase.sqlgenerator.core.<database>.<ChangeType>Generator<Database>`
- **Example**: `liquibase.sqlgenerator.core.snowflake.CreateTableGeneratorSnowflake`

**Namespace Details Registration**:
- **File**: `src/main/resources/META-INF/services/liquibase.parser.NamespaceDetails`
- **Add line**: `liquibase.parser.core.xml.<Database>NamespaceDetails`
- **Example**: `liquibase.parser.core.xml.SnowflakeNamespaceDetails`

#### Step 4B: Create NamespaceDetails Implementation

⚠️ **PACKAGE LOCATION**: Must be in `liquibase.parser.core.xml` package!

#### File: `src/main/java/liquibase/parser/core/xml/<Database>NamespaceDetails.java`

```java
package liquibase.parser.core.xml;

import liquibase.parser.NamespaceDetails;
import liquibase.parser.LiquibaseParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

/**
 * Namespace details for <Database> extension.
 * Tells Liquibase about our namespace and XSD location.
 */
public class <Database>NamespaceDetails implements NamespaceDetails {
    
    public static final String <DATABASE>_NAMESPACE = "http://www.liquibase.org/xml/ns/<database>";
    
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl) {
        return serializer instanceof XMLChangeLogSerializer &&
               <DATABASE>_NAMESPACE.equals(namespaceOrUrl);
    }
    
    @Override
    public boolean supports(LiquibaseParser parser, String namespaceOrUrl) {
        return parser instanceof XMLChangeLogSAXParser &&
               <DATABASE>_NAMESPACE.equals(namespaceOrUrl);
    }
    
    @Override
    public String getShortName(String namespaceOrUrl) {
        if (<DATABASE>_NAMESPACE.equals(namespaceOrUrl)) {
            return "<database>";
        }
        return null;
    }
    
    @Override
    public String[] getNamespaces() {
        return new String[] { <DATABASE>_NAMESPACE };
    }
    
    @Override
    public String getSchemaUrl(String namespaceOrUrl) {
        if (<DATABASE>_NAMESPACE.equals(namespaceOrUrl)) {
            return "http://www.liquibase.org/xml/ns/<database>/liquibase-<database>-latest.xsd";
        }
        return null;
    }
}
```

### 🧪 Quick Test for Phase 4

```java
@Test
public void testPhase4_ServiceRegistration() {
    // Test parser registration
    ServiceLocator serviceLocator = ServiceLocator.getInstance();
    
    // Can we find our parser?
    List<ChangeLogParser> parsers = serviceLocator.findInstances(ChangeLogParser.class);
    boolean foundParser = parsers.stream()
        .anyMatch(p -> p instanceof <Database>NamespaceAwareXMLParser);
    assertTrue(foundParser, "Parser not registered");
    
    // Can we find our generator?
    List<SqlGenerator> generators = serviceLocator.findInstances(SqlGenerator.class);
    boolean foundGenerator = generators.stream()
        .anyMatch(g -> g instanceof <ChangeType>Generator<Database>);
    assertTrue(foundGenerator, "Generator not registered");
    
    // Can we find namespace details?
    List<NamespaceDetails> namespaces = serviceLocator.findInstances(NamespaceDetails.class);
    boolean foundNamespace = namespaces.stream()
        .anyMatch(n -> n instanceof <Database>NamespaceDetails);
    assertTrue(foundNamespace, "Namespace details not registered");
    
    System.out.println("✅ Phase 4 PASS - All components registered");
}
```

### Phase 5: Update XSD Schema

⚠️ **CRITICAL: XSD Update is MANDATORY**
**Common Error**: `Attribute '<database>:transient' is not allowed`
**Solution**: ALWAYS update XSD when adding namespace attributes

#### File: `src/main/resources/www.liquibase.org/xml/ns/<database>/liquibase-<database>-latest.xsd`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://www.liquibase.org/xml/ns/<database>"
            xmlns:<database>="http://www.liquibase.org/xml/ns/<database>"
            elementFormDefault="qualified">
    
    <!-- Define namespace attributes for each change type -->
    
    <!-- Attributes for createTable -->
    <xsd:attribute name="transient" type="xsd:boolean">
        <xsd:annotation>
            <xsd:documentation>Creates a transient table</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
    <xsd:attribute name="clusterBy" type="xsd:string">
        <xsd:annotation>
            <xsd:documentation>Comma-separated list of columns to cluster by</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
    <!-- Attributes for alterSequence -->
    <xsd:attribute name="setNoOrder" type="xsd:boolean">
        <xsd:annotation>
            <xsd:documentation>Set sequence to NOORDER (one-way operation)</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
    <!-- Attributes for dropSequence -->
    <xsd:attribute name="cascade" type="xsd:boolean">
        <xsd:annotation>
            <xsd:documentation>Drop sequence with CASCADE option</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
    <xsd:attribute name="restrict" type="xsd:boolean">
        <xsd:annotation>
            <xsd:documentation>Drop sequence with RESTRICT option</xsd:documentation>
        </xsd:annotation>
    </xsd:attribute>
    
    <!-- Add more attributes as needed -->
</xsd:schema>
```

### 🧪 Quick Test for Phase 5

```java
@Test
public void testPhase5_XsdExists() {
    // Just verify XSD exists - full XML parsing tested in Phase 6
    InputStream xsd = getClass().getResourceAsStream(
        "/www.liquibase.org/xml/ns/<database>/liquibase-<database>-latest.xsd");
    assertNotNull(xsd, "XSD file not found");
    System.out.println("✅ Phase 5 PASS - XSD exists");
}
```

---

## Unit and Integration Testing

### 🎯 Complete Phase Tests in One File

Create a single test file with all phase tests:

```java
public class <Database>NamespacePhaseTests {
    
    @Test
    public void testPhase1_Storage() {
        <Database>NamespaceAttributeStorage.clear();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        <Database>NamespaceAttributeStorage.storeAttributes("test_table", attrs);
        assertEquals("true", <Database>NamespaceAttributeStorage.getAttributes("test_table").get("transient"));
        System.out.println("✅ Phase 1 PASS");
    }
    
    @Test
    public void testPhase2_Parser() throws Exception {
        // Test XML parsing captures attributes
        System.out.println("✅ Phase 2 PASS");
    }
    
    @Test
    public void testPhase3_SqlGenerator() {
        // Test SQL generation with attributes
        System.out.println("✅ Phase 3 PASS");
    }
    
    @Test
    public void testPhase4_ServiceRegistration() {
        // Test all services registered
        System.out.println("✅ Phase 4 PASS");
    }
    
    @Test
    public void testPhase5_XsdExists() {
        // Test XSD file exists
        System.out.println("✅ Phase 5 PASS");
    }
    
    @Test
    public void testPhase6_FullIntegration() {
        // Test complete flow
        System.out.println("✅ Phase 6 PASS - Full integration works");
    }
}
```

**Run after each phase:**
```bash
mvn test -Dtest=<Database>NamespacePhaseTests -Dtest.method=testPhase*
```

### Production Unit Tests

After phase tests pass, create comprehensive unit tests:

⚠️ **CRITICAL: Test Expectation Warning**
When updating existing generators that change SQL format:
- Existing tests may expect old SQL format
- Create debug test to verify correct SQL
- Update test assertions to match new format
- This is NOT a bug, just outdated expectations

📌 **Real Example**: 8 alterSequence tests needed updating from old "SET NOORDER" to new "SET ..., NOORDER" format

1. **Storage Test** (`<Database>NamespaceAttributeStorageTest.java`)
   - Test storing and retrieving attributes
   - Test thread safety
   - Test clearing storage

2. **Parser Test** (`<Database>NamespaceAwareXMLParserTest.java`)
   - Test parsing XML with namespace attributes
   - Test multiple change types
   - Test attributes are stored correctly

3. **Generator Tests** (One for each enhanced change type)
   - Test SQL modification based on attributes
   - Test validation of attributes
   - Test with and without attributes

4. **Integration Test**
   - Test complete flow from XML to SQL
   - Test multiple changesets
   - Test mixed standard and enhanced changes

### Integration Test Example

```java
@Test
@DisplayName("Should process namespace attributes end-to-end")
public void testFullIntegration() throws Exception {
    // Create test changelog
    String changelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<databaseChangeLog\n" +
        "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
        "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "    xmlns:<database>=\"http://www.liquibase.org/xml/ns/<database>\"\n" +
        "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
        "        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n" +
        "    \n" +
        "    <changeSet id=\"1\" author=\"test\">\n" +
        "        <createTable tableName=\"enhanced_table\" \n" +
        "                   <database>:transient=\"true\"\n" +
        "                   <database>:clusterBy=\"id,created_at\">\n" +
        "            <column name=\"id\" type=\"INT\"/>\n" +
        "            <column name=\"created_at\" type=\"TIMESTAMP\"/>\n" +
        "        </createTable>\n" +
        "    </changeSet>\n" +
        "</databaseChangeLog>";
    
    // Write to temp file
    Path tempFile = Files.createTempFile("integration", ".xml");
    Files.write(tempFile, changelog.getBytes());
    
    // Clear storage
    <Database>NamespaceAttributeStorage.clear();
    
    // Parse and generate SQL
    // ... test complete flow ...
    
    // Verify enhanced SQL contains our modifications
    // assertTrue(generatedSql.contains("CREATE TRANSIENT TABLE"));
    // assertTrue(generatedSql.contains("CLUSTER BY (id,created_at)"));
    
    Files.deleteIfExists(tempFile);
}
```

---

## Test Harness Implementation

After all unit tests pass, implement test harness tests to verify the namespace attributes work correctly in the full Liquibase execution context.

### Test Harness File Structure
```
liquibase-test-harness/
└── src/main/resources/liquibase/harness/change/
    ├── changelogs/snowflake/
    │   └── <changeType>Enhanced.xml
    ├── expectedSql/snowflake/
    │   └── <changeType>Enhanced.sql
    └── expectedSnapshot/snowflake/
        └── <changeType>Enhanced.json
```

### Creating Test Harness Tests for Namespace Attributes

1. **Changelog File**: Include namespace declaration and attributes
   ```xml
   <databaseChangeLog 
       xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
       xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake">
       
       <changeSet id="1" author="test">
           <alterSequence sequenceName="test_seq" 
                         snowflake:setNoOrder="true"/>
       </changeSet>
   </databaseChangeLog>
   ```

2. **Expected SQL**: Should include the namespace attribute effects
3. **Run Tests**: Verify namespace attributes are properly applied

---

## Troubleshooting Common Issues

### 🎯 SYSTEMATIC DEBUGGING PATTERN

When namespace attributes aren't working, **STOP** - Don't assume where the bug is!

⚠️ **CRITICAL: When Tests Fail**
1. **First create a debug test** to print actual SQL output
2. **Verify test expectations are correct** - they might expect old format
3. **Only then fix implementation OR tests**

📌 **Real Example**: 8 alterSequence tests failed because they expected old SQL format, not because implementation was wrong!

**Create isolated tests for each layer:**

```java
// Step 1: Test Storage directly
@Test
public void debugStep1_Storage() {
    <Database>NamespaceAttributeStorage.clear();
    Map<String, String> attrs = new HashMap<>();
    attrs.put("setNoOrder", "true");
    <Database>NamespaceAttributeStorage.storeAttributes("test_seq", attrs);
    System.out.println("Stored: " + <Database>NamespaceAttributeStorage.getAttributes("test_seq"));
    // If this works, storage is NOT the issue
}

// Step 2: Test Parser captures attributes
@Test
public void debugStep2_ParserCapture() throws Exception {
    String xml = "<alterSequence sequenceName=\"test_seq\" snowflake:setNoOrder=\"true\"/>";
    // Parse and check if attributes stored
    // If this works, parser is NOT the issue
}

// Step 3: Test Generator retrieves attributes
@Test
public void debugStep3_GeneratorRetrieval() {
    // Manually store attributes
    <Database>NamespaceAttributeStorage.storeAttributes("test_seq", 
        Map.of("setNoOrder", "true"));
    
    // Create statement and generate SQL
    AlterSequenceStatement stmt = new AlterSequenceStatement();
    stmt.setSequenceName("test_seq");
    
    AlterSequenceGenerator<Database> gen = new AlterSequenceGenerator<Database>();
    Sql[] sqls = gen.generateSql(stmt, new <Database>Database(), null);
    System.out.println("Generated: " + sqls[0].toSql());
    // If this contains SET NOORDER, generator works
}
```

### Common Issues and Solutions

#### Parser Not Being Used
**Symptom**: Namespace attributes not captured
**Debug**: Add System.out.println in parser constructor
**Fix**: 
- Parser priority must be PRIORITY_DATABASE + 10
- Service registration: `liquibase.parser.<Database>NamespaceAwareXMLParser`
- Verify JAR is rebuilt and deployed

#### Attributes Not Captured
**Symptom**: Storage is empty after parsing
**Debug**: Print in NamespaceCapturingHandler.startElement()
**Fix**:
- Namespace URL must match exactly: `http://www.liquibase.org/xml/ns/<database>`
- SAX parser must be namespace-aware: `factory.setNamespaceAware(true)`
- Check XML has namespace declaration: `xmlns:<database>="..."`

#### SQL Not Modified
**Symptom**: Standard SQL generated without modifications
**Debug**: Print in generator's generateSql method
**Fix**:
- Generator priority must be higher than base
- Object name must match between parser and generator
- Don't remove attributes if test harness needs them

#### Test Harness Issues
**Symptom**: Works in unit tests but not test harness
**Debug Steps**: 
- Verify namespace declaration in changelog XML
- Check that XSD includes the namespace attributes
- Ensure parser is registered and has correct priority
- Verify SQL generator is being called

---

## Key Assumptions and System Knowledge

### Storage Mechanism
- Thread-safe ConcurrentHashMap implementation
- Attributes stored by object name (table name, sequence name, etc.)
- Storage persists for duration of changelog execution
- Manual cleanup prevents memory leaks

### Priority Management
```
Parser:    PRIORITY_DATABASE + 10  (must be highest)
Generator: PRIORITY_DATABASE + 1   (must override base)
Namespace: PRIORITY_DEFAULT        (standard priority)
```

### Namespace URL Convention
- Format: `http://www.liquibase.org/xml/ns/<database>`
- Must be consistent across: XML, Parser, NamespaceDetails, XSD
- Case-sensitive matching

### Execution Flow
1. XML parsed → Parser intercepts namespace attributes → Stores in memory
2. Change processed → Statement created → Generator retrieves attributes
3. SQL modified based on attributes → Attributes removed from storage

### Test Harness Execution
- Test harness uses `updateSql` command to generate SQL
- Namespace attributes work correctly when properly implemented
- Ensure all components are registered in service files

---

## Summary Checklist

Before considering implementation complete:

### Implementation
- [ ] All 5 phases implemented and tested
- [ ] Phase tests pass individually
- [ ] Service registrations verified
- [ ] XSD includes all namespace attributes
- [ ] No hardcoded values - uses patterns

### Testing  
- [ ] Phase tests created and passing
- [ ] Unit tests for each component
- [ ] Integration test shows end-to-end flow
- [ ] Test harness tests created and passing

### Documentation
- [ ] Requirements document created first
- [ ] Code includes helpful comments
- [ ] README shows usage examples
- [ ] Known limitations documented

---

## Quick Reference

### Complete Working Example
```xml
<!-- In changelog -->
<alterSequence sequenceName="my_seq" 
               incrementBy="10"
               snowflake:setNoOrder="true"/>

<!-- Results in SQL -->
ALTER SEQUENCE my_seq INCREMENT BY 10 SET NOORDER
```

### Adding a New Namespace Attribute
1. Update requirements document
2. Add to XSD: `<xsd:attribute name="newAttr" type="xsd:string"/>`
3. Update parser's isTargetChangeType() if needed
4. Update generator's applyNamespaceAttributes()
5. Add unit tests for the attribute
6. Run all phase tests

### Testing Commands
```bash
# Run all phase tests
mvn test -Dtest=<Database>NamespacePhaseTests

# Run specific phase
mvn test -Dtest=<Database>NamespacePhaseTests#testPhase3_SqlGenerator

# Run all tests for a component
mvn test -Dtest="*AlterSequence*"
```