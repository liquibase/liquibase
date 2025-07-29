# NAMESPACE_ATTRIBUTE_PATTERN_2.md - Comprehensive Guide for Adding Database-Specific Attributes

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Pre-Implementation Requirements Research](#pre-implementation-requirements-research)
3. [Implementation Guide](#implementation-guide)
4. [Unit and Integration Testing](#unit-and-integration-testing)
5. [Test Harness Implementation](#test-harness-implementation)
6. [Troubleshooting Common Issues](#troubleshooting-common-issues)
7. [Key Assumptions and System Knowledge](#key-assumptions-and-system-knowledge)

---

## When to Use This Pattern

Use this pattern when:
- Adding database-specific attributes to **existing** Liquibase change types
- Extending standard changes like `createTable`, `createIndex`, `addColumn`
- Need namespace-prefixed attributes like `<createTable snowflake:transient="true">`

Do NOT use this pattern when:
- Creating entirely new change types (use NEW_CHANGETYPE_PATTERN_2.md instead)
- The change type doesn't exist in standard Liquibase

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

### IMPORTANT: Testing Integration Points

Throughout implementation, create tests at each step to verify your code works before moving to the next step.

### Step 1: Create Namespace Attribute Storage

First, create a thread-safe storage mechanism for namespace attributes:

```java
package liquibase.ext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class <Database>NamespaceAttributeStorage {
    private static final ConcurrentHashMap<String, Map<String, String>> storage = 
        new ConcurrentHashMap<>();
    
    public static void storeAttributes(String objectName, Map<String, String> attributes) {
        if (objectName != null && attributes != null && !attributes.isEmpty()) {
            storage.put(objectName, new ConcurrentHashMap<>(attributes));
        }
    }
    
    public static Map<String, String> getAttributes(String objectName) {
        return storage.get(objectName);
    }
    
    public static void removeAttributes(String objectName) {
        storage.remove(objectName);
    }
    
    public static void clear() {
        storage.clear();
    }
}
```

**Test Step 1**: Create `<Database>NamespaceAttributeStorageTest.java` to verify storage works correctly.

### Step 2: Create Custom XML Parser

Create a parser that intercepts namespace attributes:

```java
package liquibase.parser;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.ChangeLogParseException;
import liquibase.ext.<Database>NamespaceAttributeStorage;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class <Database>NamespaceAwareXMLParser extends XMLChangeLogSAXParser {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // Higher priority than default
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
        }
        
        // Then do normal parsing
        return super.parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
    }
    
    private void captureNamespaceAttributes(String location, ResourceAccessor resourceAccessor) 
            throws Exception {
        
        try (InputStream inputStream = resourceAccessor.getResource(location).openInputStream()) {
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
            
            // Check for the change types you're extending
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
                   "createIndex".equals(localName) ||
                   "addColumn".equals(localName);
        }
        
        private String getObjectName(String changeType, Attributes attributes) {
            // Extract the object name based on change type
            switch (changeType) {
                case "createTable":
                    return attributes.getValue("tableName");
                case "createIndex":
                    return attributes.getValue("indexName");
                case "addColumn":
                    return attributes.getValue("tableName");
                default:
                    return null;
            }
        }
    }
}
```

**Test Step 2**: Create `<Database>NamespaceAwareXMLParserTest.java` to verify namespace attribute capture.

### Step 3: Create Enhanced SQL Generators

For each change type you're extending, create an enhanced SQL generator:

```java
package liquibase.sqlgenerator.core.<database>;

import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.<ChangeType>Generator;
import liquibase.statement.core.<ChangeType>Statement;
import liquibase.database.Database;
import liquibase.database.core.<Database>Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.ext.<Database>NamespaceAttributeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class <ChangeType>Generator<Database> extends <ChangeType>Generator {
    
    @Override
    public boolean supports(<ChangeType>Statement statement, Database database) {
        return database instanceof <Database>Database && 
               super.supports(statement, database);
    }
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5;
    }
    
    @Override
    public Sql[] generateSql(<ChangeType>Statement statement, Database database, 
                           SqlGeneratorChain chain) {
        // Get the standard SQL
        Sql[] standardSql = super.generateSql(statement, database, chain);
        
        // Check for namespace attributes
        String objectName = getObjectName(statement);
        Map<String, String> attrs = <Database>NamespaceAttributeStorage.getAttributes(objectName);
        
        if (attrs == null || attrs.isEmpty()) {
            return standardSql;
        }
        
        // Modify SQL based on attributes
        List<Sql> modifiedSql = new ArrayList<>();
        
        for (Sql sql : standardSql) {
            String sqlText = sql.toSql();
            
            // Apply modifications based on attributes
            sqlText = applyNamespaceAttributes(sqlText, attrs);
            
            modifiedSql.add(new UnparsedSql(sqlText, sql.getAffectedDatabaseObjects()));
        }
        
        // Clean up stored attributes
        <Database>NamespaceAttributeStorage.removeAttributes(objectName);
        
        return modifiedSql.toArray(new Sql[0]);
    }
    
    private String getObjectName(<ChangeType>Statement statement) {
        // Extract object name from statement
        // Implementation depends on the statement type
        return statement.getTableName(); // Example for table-related statements
    }
    
    private String applyNamespaceAttributes(String sql, Map<String, String> attrs) {
        // Apply each attribute to modify the SQL
        // Implementation depends on your specific attributes
        
        // Example: transient table
        if ("true".equals(attrs.get("transient"))) {
            sql = sql.replaceFirst("CREATE TABLE", "CREATE TRANSIENT TABLE");
        }
        
        // Example: cluster by
        String clusterBy = attrs.get("clusterBy");
        if (clusterBy != null && !clusterBy.isEmpty()) {
            if (sql.endsWith(";")) {
                sql = sql.substring(0, sql.length() - 1);
            }
            sql += " CLUSTER BY (" + clusterBy + ");";
        }
        
        return sql;
    }
}
```

**Test Step 3**: Create `<ChangeType>Generator<Database>Test.java` for each enhanced generator.

### Step 4: Register Extension Components

Create service loader files:

```properties
# File: src/main/resources/META-INF/services/liquibase.parser.ChangeLogParser
liquibase.parser.<Database>NamespaceAwareXMLParser

# File: src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator
liquibase.sqlgenerator.core.<database>.<ChangeType>Generator<Database>
# Add one line for each enhanced generator

# File: src/main/resources/META-INF/services/liquibase.parser.NamespaceDetails
liquibase.ext.<Database>NamespaceDetails
```

Create NamespaceDetails implementation:

```java
package liquibase.ext;

import liquibase.parser.NamespaceDetails;
import liquibase.parser.LiquibaseParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

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

**Test Step 4**: Create `ServiceRegistrationTest.java` to verify all components are registered.

### Step 5: Update XSD Schema

Update your extension's XSD to include the new attributes:

```xml
<!-- File: src/main/resources/www.liquibase.org/xml/ns/<database>/liquibase-<database>-latest.xsd -->
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
    
    <!-- Add more attributes as needed -->
</xsd:schema>
```

---

## Unit and Integration Testing

### Step-by-Step Test Creation

Create tests in this order:

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
   - Test attribute removal after use
   - Test with and without attributes

4. **Service Registration Test**
   - Test all components are registered
   - Test priority ordering

5. **Integration Test**
   - Test complete flow from XML to SQL
   - Test multiple changesets
   - Test mixed standard and enhanced changes

### Integration Test Example

```java
@Test
@DisplayName("Should process namespace attributes end-to-end")
public void testFullIntegration() throws Exception {
    // Create test changelog
    String changelog = """
        <?xml version="1.0" encoding="UTF-8"?>
        <databaseChangeLog
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:<database>="http://www.liquibase.org/xml/ns/<database>"
            xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
            
            <changeSet id="1" author="test">
                <createTable tableName="enhanced_table" 
                           <database>:transient="true"
                           <database>:clusterBy="id,created_at">
                    <column name="id" type="INT"/>
                    <column name="created_at" type="TIMESTAMP"/>
                </createTable>
            </changeSet>
        </databaseChangeLog>
        """;
    
    // Test complete flow
    // Verify enhanced SQL is generated
}
```

---

## Test Harness Implementation

After all unit tests pass, implement test harness tests following `TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`.

### Key Differences for Namespace Attributes

1. **Test File Naming**: Use descriptive names like `createTableTransient.xml`, `createTableClusterBy.xml`

2. **Expected SQL**: Will include your modifications (e.g., `CREATE TRANSIENT TABLE`)

3. **Snapshot Verification**: Enhanced attributes may not appear in snapshots (they modify behavior, not structure)

---

## Troubleshooting Common Issues

### Parser Not Being Used
- Check parser priority is higher than default
- Verify service registration file is correct
- Enable debug logging to see parser selection

### Attributes Not Captured
- Verify namespace URL matches exactly
- Check SAX parser is namespace-aware
- Debug print in storage to verify timing

### SQL Not Modified
- Check generator priority is higher than default
- Verify object name matching between parser and generator
- Debug print attributes retrieval

### Service Loading Issues
- Check META-INF/services files have correct paths
- Verify class names are fully qualified
- Check for typos in service files

---

## Key Assumptions and System Knowledge

### Storage Cleanup Strategy
- Attributes are removed after SQL generation
- Storage is cleared between test runs
- Thread-safe for concurrent usage

### Priority Management
- Parser priority: PRIORITY_DATABASE + 10
- Generator priority: PRIORITY_DATABASE + 5
- Ensures your components override defaults

### Namespace URL Convention
- Format: `http://www.liquibase.org/xml/ns/<database>`
- Must match in XML, parser, and namespace details

### Compatibility Notes
- Works with Liquibase 4.25+
- Uses standard extension points
- No core Liquibase modifications needed

---

## Summary Checklist

Before considering implementation complete:

### Implementation
- [ ] Storage mechanism created and tested
- [ ] Parser captures namespace attributes
- [ ] Generators modify SQL correctly
- [ ] Service registration complete
- [ ] XSD updated with new attributes

### Testing
- [ ] All unit tests passing
- [ ] Integration test demonstrates end-to-end flow
- [ ] Test harness tests passing
- [ ] Manual testing with real database

### Documentation
- [ ] Requirements document complete
- [ ] Code comments explain namespace handling
- [ ] README updated with usage examples

---

## Quick Reference

### Adding a New Attribute

1. Add to requirements document
2. Update XSD schema
3. Update parser to capture attribute
4. Update generator to apply attribute
5. Add tests for the attribute
6. Update documentation

### Testing a Specific Attribute

```xml
<createTable tableName="test_table" <database>:yourAttribute="value">
    <!-- columns -->
</createTable>
```

Should generate modified SQL based on your attribute implementation.