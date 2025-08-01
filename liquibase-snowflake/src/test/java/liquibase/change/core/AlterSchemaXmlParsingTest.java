package liquibase.change.core;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;

public class AlterSchemaXmlParsingTest {

    @TempDir
    Path tempDir;

    @Test
    public void testUnsetCommentXmlParsing() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    <changeSet author=\"test\" id=\"test-unset-comment\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_SCHEMA\" unsetComment=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        XMLChangeLogSAXParser parser = new XMLChangeLogSAXParser();
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        // Write XML to temporary file
        File xmlFile = new File(tempDir.toFile(), "test.xml");
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xml);
        }
        
        DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(tempDir.toFile());
        
        DatabaseChangeLog changeLog = parser.parse(
            "test.xml", 
            new ChangeLogParameters(database), 
            resourceAccessor
        );
        
        System.out.println("Parsed successfully! Found " + changeLog.getChangeSets().size() + " changesets");
        
        if (!changeLog.getChangeSets().isEmpty()) {
            AlterSchemaChange change = (AlterSchemaChange) changeLog.getChangeSets().get(0).getChanges().get(0);
            System.out.println("unsetComment value: " + change.getUnsetComment());
        }
    }

    @Test
    public void testUnsetDataRetentionXmlParsing() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    <changeSet author=\"test\" id=\"test-unset-retention\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_SCHEMA\" unsetDataRetentionTimeInDays=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        XMLChangeLogSAXParser parser = new XMLChangeLogSAXParser();
        SnowflakeDatabase database = new SnowflakeDatabase();
        
        // Write XML to temporary file
        File xmlFile = new File(tempDir.toFile(), "test.xml");
        try (FileWriter writer = new FileWriter(xmlFile)) {
            writer.write(xml);
        }
        
        DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(tempDir.toFile());
        
        DatabaseChangeLog changeLog = parser.parse(
            "test.xml", 
            new ChangeLogParameters(database), 
            resourceAccessor
        );
        
        System.out.println("Parsed successfully! Found " + changeLog.getChangeSets().size() + " changesets");
        
        if (!changeLog.getChangeSets().isEmpty()) {
            AlterSchemaChange change = (AlterSchemaChange) changeLog.getChangeSets().get(0).getChanges().get(0);
            System.out.println("unsetDataRetentionTimeInDays value: " + change.getUnsetDataRetentionTimeInDays());
        }
    }
}