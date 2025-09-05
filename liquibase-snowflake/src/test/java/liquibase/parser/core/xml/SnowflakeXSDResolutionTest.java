package liquibase.parser.core.xml;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.DirectoryResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test to verify that Snowflake XSD files can be resolved from the classpath
 * when secure parsing is enabled.
 */
public class SnowflakeXSDResolutionTest {

    private ResourceAccessor resourceAccessor;
    
    @BeforeEach
    public void setUp() {
        resourceAccessor = new ClassLoaderResourceAccessor();
    }

    @Test
    public void testSnowflakeXSDResolutionWithSecureParsingEnabled() throws Exception {
        // Enable secure parsing
        System.setProperty(GlobalConfiguration.SECURE_PARSING.getKey(), "true");
        
        String changeLogXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<databaseChangeLog\n" +
                "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "        xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
                "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
                "                            http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
                "                            http://www.liquibase.org/xml/ns/snowflake\n" +
                "                            http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
                "\n" +
                "    <changeSet id=\"1\" author=\"test\">\n" +
                "        <snowflake:createWarehouse warehouseName=\"TEST_WAREHOUSE\" warehouseSize=\"XSMALL\"/>\n" +
                "    </changeSet>\n" +
                "\n" +
                "</databaseChangeLog>";

        // Write the changelog to a temporary file
        java.io.File tempFile = java.io.File.createTempFile("test-changelog", ".xml");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), changeLogXml.getBytes(StandardCharsets.UTF_8));
        
        // Use a DirectoryResourceAccessor to access the temp file
        liquibase.resource.DirectoryResourceAccessor tempResourceAccessor = 
            new liquibase.resource.DirectoryResourceAccessor(tempFile.getParentFile());

        // Parse the changelog
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(tempFile.getName(), tempResourceAccessor);
        DatabaseChangeLog changeLog = parser.parse(tempFile.getName(), new ChangeLogParameters(), tempResourceAccessor);
        
        // Verify the changelog was parsed successfully
        assertNotNull(changeLog);
        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("1", changeLog.getChangeSets().get(0).getId());
        
        // Reset the property
        System.clearProperty(GlobalConfiguration.SECURE_PARSING.getKey());
    }
    
    @Test
    public void testSnowflakeNamespaceDetailsRegistered() {
        // Verify that SnowflakeNamespaceDetails is properly registered
        SnowflakeNamespaceDetails namespaceDetails = new SnowflakeNamespaceDetails();
        
        assertEquals("http://www.liquibase.org/xml/ns/snowflake", namespaceDetails.getNamespaces()[0]);
        assertEquals("snowflake", namespaceDetails.getShortName("http://www.liquibase.org/xml/ns/snowflake"));
        assertEquals("http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd", 
                    namespaceDetails.getSchemaUrl("http://www.liquibase.org/xml/ns/snowflake"));
    }
}