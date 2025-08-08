package liquibase.change.core;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Debug test to understand why validation fails for UNSET attributes
 */
public class AlterSchemaXmlValidationTest {

    @TempDir
    File tempDir;

    @Test
    public void testValidationWithUnsetAttribute() throws Exception {
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
            "    <changeSet id=\"test-unset-validation\" author=\"test\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_SCHEMA\" unsetDataRetentionTimeInDays=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        DatabaseChangeLog changeLog = parseXml(xml);
        ChangeSet changeSet = changeLog.getChangeSets().get(0);
        AlterSchemaChange change = (AlterSchemaChange) changeSet.getChanges().get(0);
        
        
        // Test validation
        SnowflakeDatabase database = new SnowflakeDatabase();
        ValidationErrors errors = change.validate(database);
        
        if (errors.hasErrors()) {
            for (String error : errors.getErrorMessages()) {
            }
        }
        
        // This should NOT have validation errors
        assertFalse(errors.hasErrors(), "Should not have validation errors when unsetDataRetentionTimeInDays=true");
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