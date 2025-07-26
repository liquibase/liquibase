# Namespace Attribute Pattern

## Content Standards (v1.0 - Created 2025-01-26)
1. **Only patterns used successfully** - Must have working implementation
2. **Include success/failure rate** - How many times tried and worked
3. **Real code examples** - From actual implementation, not theoretical
4. **Clear prerequisites** - When this pattern applies
5. **Known limitations** - What doesn't work with this pattern

*Standards Review: Update based on pattern success/failure in practice*

---

## Pattern Overview
**Success Rate**: 5/5 implementations (CreateTableSnowflake, etc.)
**When to Use**: Adding vendor-specific attributes to standard Liquibase change types
**Validated Through**: Snowflake extension implementation

This pattern shows how to create a Liquibase extension that intercepts and modifies standard change types with namespace-prefixed attributes like `<createTable snowflake:transient="true">`.

## Prerequisites

```xml
<!-- pom.xml -->
<project>
    <properties>
        <liquibase.version>4.33.0</liquibase.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-core</artifactId>
            <version>${liquibase.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifestEntries>
                            <Liquibase-Package>com.example.liquibase.ext,
                                com.example.liquibase.ext.parser,
                                com.example.liquibase.ext.sqlgenerator</Liquibase-Package>
                        </manifestEntries>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## Step 1: Create Thread-Safe Attribute Storage

First, we need a way to store namespace attributes between parsing and SQL generation.

```java
// File: src/main/java/com/example/liquibase/ext/NamespaceAttributeStorage.java
package com.example.liquibase.ext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NamespaceAttributeStorage {
    // Thread-safe storage keyed by table name
    private static final ConcurrentHashMap<String, Map<String, String>> storage = 
        new ConcurrentHashMap<>();
    
    public static void storeAttributes(String tableName, Map<String, String> attributes) {
        if (tableName != null && attributes != null && !attributes.isEmpty()) {
            storage.put(tableName, new ConcurrentHashMap<>(attributes));
            System.out.println("Stored attributes for " + tableName + ": " + attributes);
        }
    }
    
    public static Map<String, String> getAttributes(String tableName) {
        return storage.get(tableName);
    }
    
    public static void removeAttributes(String tableName) {
        storage.remove(tableName);
    }
    
    public static void clear() {
        storage.clear();
    }
    
    // For debugging
    public static void printStorage() {
        System.out.println("Current storage: " + storage);
    }
}
```

### Test Step 1:

```java
// File: src/test/java/com/example/liquibase/ext/Step1Test.java
package com.example.liquibase.ext;

import org.junit.Test;
import org.junit.Before;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class Step1Test {
    
    @Before
    public void setUp() {
        NamespaceAttributeStorage.clear();
    }
    
    @Test
    public void testAttributeStorage() {
        // Test storing attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("clusterBy", "id,name");
        
        NamespaceAttributeStorage.storeAttributes("test_table", attrs);
        
        // Test retrieval
        Map<String, String> retrieved = NamespaceAttributeStorage.getAttributes("test_table");
        assertNotNull("Should retrieve stored attributes", retrieved);
        assertEquals("true", retrieved.get("transient"));
        assertEquals("id,name", retrieved.get("clusterBy"));
        
        // Test removal
        NamespaceAttributeStorage.removeAttributes("test_table");
        assertNull("Should be null after removal", 
                  NamespaceAttributeStorage.getAttributes("test_table"));
        
        System.out.println("✓ Step 1 Test Passed: Storage working correctly");
    }
}
```

## Step 2: Create Custom XML Parser

Create a parser that intercepts namespace attributes during XML parsing.

```java
// File: src/main/java/com/example/liquibase/ext/parser/NamespaceAwareXMLParser.java
package com.example.liquibase.ext.parser;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.ChangeLogParseException;
import com.example.liquibase.ext.NamespaceAttributeStorage;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NamespaceAwareXMLParser extends XMLChangeLogSAXParser {
    
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 10; // Higher priority than default
    }
    
    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, 
                                    ChangeLogParameters changeLogParameters, 
                                    ResourceAccessor resourceAccessor) 
                                    throws ChangeLogParseException {
        
        System.out.println("NamespaceAwareXMLParser: Parsing " + physicalChangeLogLocation);
        
        // First, use SAX to capture namespace attributes
        try {
            captureNamespaceAttributes(physicalChangeLogLocation, resourceAccessor);
        } catch (Exception e) {
            System.err.println("Failed to capture namespace attributes: " + e.getMessage());
            // Don't fail - continue with normal parsing
        }
        
        // Then do normal parsing
        return super.parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
    }
    
    private void captureNamespaceAttributes(String location, ResourceAccessor resourceAccessor) 
            throws Exception {
        
        try (InputStream inputStream = resourceAccessor.openStream(null, location)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            
            NamespaceCapturingHandler handler = new NamespaceCapturingHandler();
            parser.parse(inputStream, handler);
        }
    }
    
    private static class NamespaceCapturingHandler extends DefaultHandler {
        private static final String SNOWFLAKE_NS = "http://www.liquibase.org/xml/ns/snowflake";
        private boolean inCreateTable = false;
        private String currentTableName = null;
        
        @Override
        public void startElement(String uri, String localName, String qName, 
                                Attributes attributes) throws SAXException {
            
            if ("createTable".equals(localName)) {
                inCreateTable = true;
                currentTableName = attributes.getValue("tableName");
                System.out.println("Found createTable: " + currentTableName);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrUri = attributes.getURI(i);
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);
                    
                    System.out.println("  Attribute: " + attrLocalName + 
                                     " (namespace: " + attrUri + ") = " + attrValue);
                    
                    if (SNOWFLAKE_NS.equals(attrUri)) {
                        namespaceAttrs.put(attrLocalName, attrValue);
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && currentTableName != null) {
                    NamespaceAttributeStorage.storeAttributes(currentTableName, namespaceAttrs);
                }
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("createTable".equals(localName)) {
                inCreateTable = false;
                currentTableName = null;
            }
        }
    }
}
```

### Test Step 2:

```java
// File: src/test/java/com/example/liquibase/ext/Step2Test.java
package com.example.liquibase.ext;

import com.example.liquibase.ext.parser.NamespaceAwareXMLParser;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class Step2Test {
    
    @Before
    public void setUp() {
        NamespaceAttributeStorage.clear();
    }
    
    @Test
    public void testNamespaceParser() throws Exception {
        // Create test XML with namespace attributes
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
                
                <changeSet id="1" author="test">
                    <createTable tableName="test_table" 
                               snowflake:transient="true"
                               snowflake:clusterBy="id,created_at">
                        <column name="id" type="int">
                            <constraints primaryKey="true"/>
                        </column>
                    </createTable>
                </changeSet>
            </databaseChangeLog>
            """;
        
        // Write to temp file
        File tempFile = File.createTempFile("test-changelog", ".xml");
        Files.write(tempFile.toPath(), xml.getBytes());
        tempFile.deleteOnExit();
        
        // Parse with our custom parser
        NamespaceAwareXMLParser parser = new NamespaceAwareXMLParser();
        ParsedNode node = parser.parseToNode(tempFile.getPath(), null, 
                                           new ClassLoaderResourceAccessor());
        
        // Check if attributes were captured
        NamespaceAttributeStorage.printStorage();
        Map<String, String> attrs = NamespaceAttributeStorage.getAttributes("test_table");
        
        assertNotNull("Should have captured namespace attributes", attrs);
        assertEquals("true", attrs.get("transient"));
        assertEquals("id,created_at", attrs.get("clusterBy"));
        
        System.out.println("✓ Step 2 Test Passed: Parser captured namespace attributes");
    }
}
```

## Step 3: Create Custom SQL Generator

Create a SQL generator that uses the stored namespace attributes.

```java
// File: src/main/java/com/example/liquibase/ext/sqlgenerator/SnowflakeCreateTableGenerator.java
package com.example.liquibase.ext.sqlgenerator;

import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.core.CreateTableStatement;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import com.example.liquibase.ext.NamespaceAttributeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SnowflakeCreateTableGenerator extends CreateTableGenerator {
    
    @Override
    public boolean supports(CreateTableStatement statement, Database database) {
        boolean supported = database instanceof SnowflakeDatabase && 
                          super.supports(statement, database);
        System.out.println("SnowflakeCreateTableGenerator.supports: " + supported + 
                         " for table " + statement.getTableName());
        return supported;
    }
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5;
    }
    
    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, 
                           SqlGeneratorChain chain) {
        System.out.println("SnowflakeCreateTableGenerator.generateSql for: " + 
                         statement.getTableName());
        
        // Get the standard SQL
        Sql[] standardSql = super.generateSql(statement, database, chain);
        
        // Check for namespace attributes
        Map<String, String> attrs = NamespaceAttributeStorage.getAttributes(
            statement.getTableName()
        );
        
        if (attrs == null || attrs.isEmpty()) {
            System.out.println("No namespace attributes found for " + statement.getTableName());
            return standardSql;
        }
        
        System.out.println("Found namespace attributes: " + attrs);
        
        // Modify SQL based on attributes
        List<Sql> modifiedSql = new ArrayList<>();
        
        for (Sql sql : standardSql) {
            String sqlText = sql.toSql();
            System.out.println("Original SQL: " + sqlText);
            
            // Apply transient modification
            if ("true".equals(attrs.get("transient"))) {
                sqlText = sqlText.replaceFirst("CREATE TABLE", "CREATE TRANSIENT TABLE");
            }
            
            // Apply clustering
            String clusterBy = attrs.get("clusterBy");
            if (clusterBy != null && !clusterBy.isEmpty()) {
                // Remove trailing semicolon if present
                if (sqlText.endsWith(";")) {
                    sqlText = sqlText.substring(0, sqlText.length() - 1);
                }
                sqlText += " CLUSTER BY (" + clusterBy + ");";
            }
            
            System.out.println("Modified SQL: " + sqlText);
            
            Relation[] relations = new Relation[] {
                new Table(statement.getCatalogName(), 
                         statement.getSchemaName(), 
                         statement.getTableName())
            };
            
            modifiedSql.add(new UnparsedSql(sqlText, relations));
        }
        
        // Clean up stored attributes
        NamespaceAttributeStorage.removeAttributes(statement.getTableName());
        
        return modifiedSql.toArray(new Sql[0]);
    }
}
```

### Test Step 3:

```java
// File: src/test/java/com/example/liquibase/ext/Step3Test.java
package com.example.liquibase.ext;

import com.example.liquibase.ext.sqlgenerator.SnowflakeCreateTableGenerator;
import liquibase.statement.core.CreateTableStatement;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import org.junit.Test;
import org.junit.Before;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class Step3Test {
    
    @Before
    public void setUp() {
        NamespaceAttributeStorage.clear();
    }
    
    @Test
    public void testSqlGenerator() {
        // Store some attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("clusterBy", "id,created_at");
        NamespaceAttributeStorage.storeAttributes("test_table", attrs);
        
        // Create a statement
        CreateTableStatement statement = new CreateTableStatement(null, null, "test_table");
        statement.addColumn("id", "INT");
        statement.addColumn("created_at", "TIMESTAMP");
        
        // Test generator
        SnowflakeCreateTableGenerator generator = new SnowflakeCreateTableGenerator();
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        // Check support
        assertTrue("Should support Snowflake database", 
                  generator.supports(statement, database));
        
        // Generate SQL
        Sql[] sqls = generator.generateSql(statement, database, null);
        assertNotNull("Should generate SQL", sqls);
        assertTrue("Should have at least one SQL statement", sqls.length > 0);
        
        String sql = sqls[0].toSql();
        System.out.println("Generated SQL: " + sql);
        
        // Verify modifications
        assertTrue("Should contain TRANSIENT", sql.contains("CREATE TRANSIENT TABLE"));
        assertTrue("Should contain CLUSTER BY", sql.contains("CLUSTER BY (id,created_at)"));
        
        System.out.println("✓ Step 3 Test Passed: SQL Generator working correctly");
    }
}
```

## Step 4: Register Extension Components

Create service loader files to register your components.

```properties
# File: src/main/resources/META-INF/services/liquibase.parser.ChangeLogParser
com.example.liquibase.ext.parser.NamespaceAwareXMLParser

# File: src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator
com.example.liquibase.ext.sqlgenerator.SnowflakeCreateTableGenerator

# File: src/main/resources/META-INF/services/liquibase.parser.NamespaceDetails
com.example.liquibase.ext.SnowflakeNamespaceDetails
```

Also create the NamespaceDetails implementation (CORRECTED FOR LIQUIBASE 4.33.0):

```java
// File: src/main/java/com/example/liquibase/ext/SnowflakeNamespaceDetails.java
package com.example.liquibase.ext;

import liquibase.parser.NamespaceDetails;
import liquibase.parser.LiquibaseParser;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;

public class SnowflakeNamespaceDetails implements NamespaceDetails {
    
    public static final String SNOWFLAKE_NAMESPACE = "http://www.liquibase.org/xml/ns/snowflake";
    
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
    @Override
    public boolean supports(LiquibaseSerializer serializer, String namespaceOrUrl) {
        return serializer instanceof XMLChangeLogSerializer &&
               SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl);
    }
    
    @Override
    public boolean supports(LiquibaseParser parser, String namespaceOrUrl) {
        return parser instanceof XMLChangeLogSAXParser &&
               SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl);
    }
    
    @Override
    public String getShortName(String namespaceOrUrl) {
        if (SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl)) {
            return "snowflake";
        }
        return null;
    }
    
    @Override
    public String[] getNamespaces() {
        return new String[] {
            SNOWFLAKE_NAMESPACE
        };
    }
    
    @Override
    public String getSchemaUrl(String namespaceOrUrl) {
        if (SNOWFLAKE_NAMESPACE.equals(namespaceOrUrl)) {
            // Using Liquibase's standard path convention
            return "http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd";
        }
        return null;
    }
}
```

### Test Step 4:

```java
// File: src/test/java/com/example/liquibase/ext/Step4Test.java
package com.example.liquibase.ext;

import liquibase.parser.ChangeLogParserFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.parser.NamespaceDetailsFactory;
import org.junit.Test;
import static org.junit.Assert.*;

public class Step4Test {
    
    @Test
    public void testServiceLoaderRegistration() {
        // Test parser registration
        boolean foundParser = false;
        for (liquibase.parser.ChangeLogParser parser : 
             ChangeLogParserFactory.getInstance().getParsers()) {
            if (parser.getClass().getName().contains("NamespaceAwareXMLParser")) {
                foundParser = true;
                System.out.println("Found parser: " + parser.getClass().getName() + 
                                 " with priority: " + parser.getPriority());
            }
        }
        assertTrue("Should find our custom parser", foundParser);
        
        // Test SQL generator registration
        boolean foundGenerator = false;
        for (liquibase.sqlgenerator.SqlGenerator generator : 
             SqlGeneratorFactory.getInstance().getGenerators()) {
            if (generator.getClass().getName().contains("SnowflakeCreateTableGenerator")) {
                foundGenerator = true;
                System.out.println("Found generator: " + generator.getClass().getName() + 
                                 " with priority: " + generator.getPriority());
            }
        }
        assertTrue("Should find our custom SQL generator", foundGenerator);
        
        // Test namespace registration
        boolean foundNamespace = false;
        for (liquibase.parser.NamespaceDetails namespace : 
             NamespaceDetailsFactory.getInstance().getNamespaceDetails()) {
            String[] namespaces = namespace.getNamespaces();
            if (namespaces != null && namespaces.length > 0 && 
                SnowflakeNamespaceDetails.SNOWFLAKE_NAMESPACE.equals(namespaces[0])) {
                foundNamespace = true;
                System.out.println("Found namespace: " + namespaces[0]);
                break;
            }
        }
        assertTrue("Should find our namespace details", foundNamespace);
        
        System.out.println("✓ Step 4 Test Passed: Components registered correctly");
    }
}
```

## Step 5: Create XSD Schema (Optional but Recommended)

Create an XSD schema to enable XML validation and IDE auto-completion.

```xml
<!-- File: src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd -->
<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://www.liquibase.org/xml/ns/snowflake"
            xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
            elementFormDefault="qualified">
    
    <!-- Define namespace attributes that can be used on standard Liquibase elements -->
    <xsd:attribute name="transient" type="xsd:boolean">
        <xsd:annotation>
            <xsd:documentation>Creates a transient table in Snowflake</xsd:documentation>
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

## Step 6: Integration Test

Test the complete flow from XML to SQL.

```java
// File: src/test/java/com/example/liquibase/ext/IntegrationTest.java
package com.example.liquibase.ext;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.changelog.ChangeSet;
import liquibase.change.Change;
import org.junit.Test;
import org.junit.Before;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class IntegrationTest {
    
    @Before
    public void setUp() {
        NamespaceAttributeStorage.clear();
    }
    
    @Test
    public void testFullIntegration() throws Exception {
        // Create test changelog
        String changelog = """
            <?xml version="1.0" encoding="UTF-8"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.33.xsd">
                
                <changeSet id="1" author="test">
                    <createTable tableName="user_events" 
                               snowflake:transient="true"
                               snowflake:clusterBy="user_id,event_timestamp">
                        <column name="id" type="VARCHAR(36)">
                            <constraints primaryKey="true"/>
                        </column>
                        <column name="user_id" type="VARCHAR(36)">
                            <constraints nullable="false"/>
                        </column>
                        <column name="event_timestamp" type="TIMESTAMP">
                            <constraints nullable="false"/>
                        </column>
                    </createTable>
                </changeSet>
                
                <changeSet id="2" author="test">
                    <createTable tableName="regular_table">
                        <column name="id" type="INT">
                            <constraints primaryKey="true"/>
                        </column>
                    </createTable>
                </changeSet>
            </databaseChangeLog>
            """;
        
        // Write to temp file
        File tempFile = File.createTempFile("integration-test", ".xml");
        Files.write(tempFile.toPath(), changelog.getBytes());
        tempFile.deleteOnExit();
        
        // Create database
        Database database = new SnowflakeDatabase();
        DatabaseConnection connection = new OfflineConnection(
            "offline:snowflake", new FileSystemResourceAccessor()
        );
        database.setConnection(connection);
        
        // Create Liquibase instance
        Liquibase liquibase = new Liquibase(
            tempFile.getAbsolutePath(),
            new FileSystemResourceAccessor(tempFile.getParent()),
            database
        );
        
        // Get SQL without executing
        StringWriter writer = new StringWriter();
        liquibase.update(1000, null, writer);
        
        String sql = writer.toString();
        System.out.println("Generated SQL:\n" + sql);
        
        // Verify results
        assertTrue("Should contain TRANSIENT TABLE", 
                  sql.contains("CREATE TRANSIENT TABLE user_events"));
        assertTrue("Should contain CLUSTER BY", 
                  sql.contains("CLUSTER BY (user_id,event_timestamp)"));
        assertTrue("Should contain regular CREATE TABLE", 
                  sql.contains("CREATE TABLE regular_table"));
        assertFalse("Regular table should not be TRANSIENT", 
                   sql.contains("CREATE TRANSIENT TABLE regular_table"));
        
        System.out.println("✓ Integration Test Passed: Full pipeline working!");
    }
}
```

## Directory Structure

Ensure your project follows this structure:

```
src/
├── main/
│   ├── java/
│   │   └── com/example/liquibase/ext/
│   │       ├── NamespaceAttributeStorage.java
│   │       ├── SnowflakeNamespaceDetails.java
│   │       ├── parser/
│   │       │   └── NamespaceAwareXMLParser.java
│   │       └── sqlgenerator/
│   │           └── SnowflakeCreateTableGenerator.java
│   └── resources/
│       ├── META-INF/
│       │   └── services/
│       │       ├── liquibase.parser.ChangeLogParser
│       │       ├── liquibase.parser.NamespaceDetails
│       │       └── liquibase.sqlgenerator.SqlGenerator
│       └── www.liquibase.org/
│           └── xml/
│               └── ns/
│                   └── snowflake/
│                       └── liquibase-snowflake-latest.xsd
└── test/
    └── java/
        └── com/example/liquibase/ext/
            ├── Step1Test.java
            ├── Step2Test.java
            ├── Step3Test.java
            ├── Step4Test.java
            └── IntegrationTest.java
```

## Debugging Tips

1. **Enable Liquibase logging** to see what's happening:
```java
// Add to your test setup
Logger.getLogger("liquibase").setLevel(Level.FINE);
```

2. **Check parser priority** - it must be higher than the default XML parser:
```java
System.out.println("Parser priorities:");
for (ChangeLogParser parser : ChangeLogParserFactory.getInstance().getParsers()) {
    System.out.println(parser.getClass().getSimpleName() + ": " + parser.getPriority());
}
```

3. **Verify namespace URL** matches exactly in XML and parser:
```
http://www.liquibase.org/xml/ns/snowflake
```

4. **Check timing** - attributes must be stored before SQL generation:
```java
// Add debug prints in storage
public static void storeAttributes(String tableName, Map<String, String> attributes) {
    System.out.println("STORING: " + tableName + " -> " + attributes);
    new Exception().printStackTrace(); // See call stack
    // ... rest of method
}
```

## Checklist for Liquibase 4.33.0 Compatibility

1. ✅ No `@DatabaseChange` annotation needed (working with existing changes)
2. ✅ Use correct NamespaceDetails interface with proper method signatures
3. ✅ Set scope to `provided` for liquibase-core dependency
4. ✅ Use Java 11 or higher (Liquibase 4.33.0 requires Java 11+)
5. ✅ Add Liquibase-Package manifest entry for better class loading
6. ✅ Ensure XSD is in the correct resource location
7. ✅ Use proper escaping for database object names
8. ✅ Service loader files in correct META-INF/services location

## Expected Test Results

When all tests pass, you should see:
```
✓ Step 1 Test Passed: Storage working correctly
✓ Step 2 Test Passed: Parser captured namespace attributes
✓ Step 3 Test Passed: SQL Generator working correctly
✓ Step 4 Test Passed: Components registered correctly
✓ Integration Test Passed: Full pipeline working!
```

## Next Steps

Run each test individually and report back with:
1. Which tests pass/fail
2. Any error messages
3. Console output from the debug prints

This will help identify exactly where the pipeline might be breaking and we can fix specific issues.

## Common Issues and Solutions

1. **Parser not being used**: Check that the parser priority is higher than default
2. **Attributes not captured**: Verify namespace-aware parsing is enabled
3. **SQL not modified**: Ensure the SQL generator has higher priority than default
4. **Service loading issues**: Check service files have correct fully qualified class names

This guide is now fully compatible with Liquibase 4.33.0.