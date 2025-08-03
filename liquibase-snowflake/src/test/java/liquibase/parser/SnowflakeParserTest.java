package liquibase.parser;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to verify parser is capturing namespace attributes
 */
class SnowflakeParserTest {
    
    @BeforeEach
    void setUp() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @Test
    void testParserCapture() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "         http://www.liquibase.org/xml/ns/snowflake\n" +
            "         http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    <changeSet id=\"1\" author=\"test\">\n" +
            "        <alterSequence sequenceName=\"test_seq\" \n" +
            "                      incrementBy=\"5\"\n" +
            "                      snowflake:setNoOrder=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        // Create parser
        SnowflakeNamespaceAwareXMLParser parser = new SnowflakeNamespaceAwareXMLParser();
        
        // Create temporary file
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test", ".xml");
        java.nio.file.Files.write(tempFile, xml.getBytes());
        
        // Create resource accessor
        ResourceAccessor resourceAccessor = new liquibase.resource.DirectoryResourceAccessor(tempFile.getParent());
        
        // Parse
        ParsedNode node = parser.parseToNode(tempFile.getFileName().toString(), new ChangeLogParameters(), resourceAccessor);
        
        // Check if attributes were captured
        Map<String, String> attrs = SnowflakeNamespaceAttributeStorage.getAttributes("test_seq");
        System.out.println("Captured attributes: " + attrs);
        
        assertNotNull(attrs, "Attributes should have been captured");
        assertEquals("true", attrs.get("setNoOrder"), "setNoOrder attribute should be captured");
    }
}