package liquibase.change.core;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ChangeLogParseException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class AlterSchemaXmlParsingTest {

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
        
        try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
            DatabaseChangeLog changeLog = parser.parse(
                "test.xml", 
                new ChangeLogParameters(database), 
                new ClassLoaderResourceAccessor()
            );
            
            System.out.println("Parsed successfully! Found " + changeLog.getChangeSets().size() + " changesets");
            
            if (!changeLog.getChangeSets().isEmpty()) {
                AlterSchemaChange change = (AlterSchemaChange) changeLog.getChangeSets().get(0).getChanges().get(0);
                System.out.println("unsetComment value: " + change.getUnsetComment());
            }
            
        } catch (ChangeLogParseException e) {
            System.out.println("Parse error: " + e.getMessage());
            throw e;
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
        
        try (InputStream is = new ByteArrayInputStream(xml.getBytes())) {
            DatabaseChangeLog changeLog = parser.parse(
                "test.xml", 
                new ChangeLogParameters(database), 
                new ClassLoaderResourceAccessor()
            );
            
            System.out.println("Parsed successfully! Found " + changeLog.getChangeSets().size() + " changesets");
            
            if (!changeLog.getChangeSets().isEmpty()) {
                AlterSchemaChange change = (AlterSchemaChange) changeLog.getChangeSets().get(0).getChanges().get(0);
                System.out.println("unsetDataRetentionTimeInDays value: " + change.getUnsetDataRetentionTimeInDays());
            }
            
        } catch (ChangeLogParseException e) {
            System.out.println("Parse error: " + e.getMessage());
            throw e;
        }
    }
}