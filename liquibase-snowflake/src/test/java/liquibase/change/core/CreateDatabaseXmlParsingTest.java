package liquibase.change.core;

import liquibase.change.Change;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML parsing tests for createDatabase change type
 */
@DisplayName("CreateDatabase XML Parsing")
public class CreateDatabaseXmlParsingTest {
    
    private XMLChangeLogSAXParser parser;
    private ResourceAccessor resourceAccessor;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        parser = new XMLChangeLogSAXParser();
        resourceAccessor = new ClassLoaderResourceAccessor();
        database = new SnowflakeDatabase();
    }
    
    @Test
    @DisplayName("Should parse valid createDatabase XML")
    void shouldParseValidXml() throws Exception {
        // Create a simple test XML
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    <changeSet id=\"test-1\" author=\"test\">\n" +
            "        <snowflake:createDatabase databaseName=\"TEST_DB\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        // Save to a temporary file for parsing
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-", ".xml");
        java.nio.file.Files.write(tempFile, xml.getBytes());
        
        try {
            DatabaseChangeLog changeLog = parser.parse(tempFile.toString(), 
                new ChangeLogParameters(database), resourceAccessor);
            
            assertNotNull(changeLog);
            List<ChangeSet> changeSets = changeLog.getChangeSets();
            assertEquals(1, changeSets.size());
            
            ChangeSet changeSet = changeSets.get(0);
            assertEquals(1, changeSet.getChanges().size());
            
            Change change = changeSet.getChanges().get(0);
            assertTrue(change instanceof CreateDatabaseChange);
            
            CreateDatabaseChange createDb = (CreateDatabaseChange) change;
            assertEquals("TEST_DB", createDb.getDatabaseName());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }
    
    @Test
    @DisplayName("Should parse createDatabase with all attributes")
    void shouldParseAllAttributes() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    <changeSet id=\"test-1\" author=\"test\">\n" +
            "        <snowflake:createDatabase databaseName=\"FULL_DB\"\n" +
            "                                  comment=\"Test database\"\n" +
            "                                  dataRetentionTimeInDays=\"7\"\n" +
            "                                  maxDataExtensionTimeInDays=\"30\"\n" +
            "                                  transient=\"false\"\n" +
            "                                  defaultDdlCollation=\"en-ci\"\n" +
            "                                  orReplace=\"true\"\n" +
            "                                  ifNotExists=\"false\"\n" +
            "                                  cloneFrom=\"SOURCE_DB\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        java.nio.file.Path tempFile = java.nio.file.Files.createTempFile("test-", ".xml");
        java.nio.file.Files.write(tempFile, xml.getBytes());
        
        try {
            DatabaseChangeLog changeLog = parser.parse(tempFile.toString(), 
                new ChangeLogParameters(database), resourceAccessor);
            
            CreateDatabaseChange createDb = (CreateDatabaseChange) changeLog.getChangeSets().get(0).getChanges().get(0);
            
            assertEquals("FULL_DB", createDb.getDatabaseName());
            assertEquals("Test database", createDb.getComment());
            assertEquals("7", createDb.getDataRetentionTimeInDays());
            assertEquals("30", createDb.getMaxDataExtensionTimeInDays());
            assertEquals(false, createDb.getTransient());
            assertEquals("en-ci", createDb.getDefaultDdlCollation());
            assertEquals(true, createDb.getOrReplace());
            assertEquals(false, createDb.getIfNotExists());
            assertEquals("SOURCE_DB", createDb.getCloneFrom());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }
}