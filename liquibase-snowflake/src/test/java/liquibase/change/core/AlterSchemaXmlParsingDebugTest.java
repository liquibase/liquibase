package liquibase.change.core;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to isolate XML parsing issues with UNSET boolean attributes
 */
public class AlterSchemaXmlParsingDebugTest {

    @TempDir
    File tempDir;

    @Test
    public void testParseWorkingBooleanAttribute() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog \n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "        http://www.liquibase.org/xml/ns/snowflake\n" +
            "        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "        \n" +
            "    <changeSet id=\"test-working-boolean\" author=\"test\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_SCHEMA\" ifExists=\"true\" newName=\"NEW_SCHEMA\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        DatabaseChangeLog changeLog = parseXml(xml);
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        AlterSchemaChange change = (AlterSchemaChange) changeSet.getChanges().get(0);
        
        System.out.println("Working boolean test:");
        System.out.println("ifExists: " + change.getIfExists());
        System.out.println("newName: " + change.getNewName());
        
        // This should work
        assertNotNull(change.getIfExists());
        assertTrue(change.getIfExists());
        assertEquals("NEW_SCHEMA", change.getNewName());
    }

    @Test
    public void testParseUnsetBooleanAttribute() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog \n" +
            "    xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "    xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "        http://www.liquibase.org/xml/ns/snowflake\n" +
            "        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "        \n" +
            "    <changeSet id=\"test-unset-boolean\" author=\"test\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_SCHEMA\" unsetDataRetentionTimeInDays=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        DatabaseChangeLog changeLog = parseXml(xml);
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        AlterSchemaChange change = (AlterSchemaChange) changeSet.getChanges().get(0);
        
        System.out.println("UNSET boolean test:");
        System.out.println("unsetDataRetentionTimeInDays: " + change.getUnsetDataRetentionTimeInDays());
        System.out.println("schemaName: " + change.getSchemaName());
        
        // This is what we expect but doesn't work
        assertNotNull(change.getUnsetDataRetentionTimeInDays());
        assertTrue(change.getUnsetDataRetentionTimeInDays());
    }

    private DatabaseChangeLog parseXml(String xml) throws Exception {
        File xmlFile = new File(tempDir, "test.xml");
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xml);
        }
        
        DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(tempDir);
        XMLChangeLogSAXParser parser = new XMLChangeLogSAXParser();
        return parser.parse("test.xml", new ChangeLogParameters(), resourceAccessor);
    }
}