package liquibase.parser;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.sqlgenerator.core.snowflake.AlterSequenceGeneratorSnowflake;
import liquibase.statement.core.AlterSequenceStatement;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SnowflakeNamespaceTest {
    
    @Test
    public void testNamespaceAttributeParsing() throws Exception {
        // Create a test changelog file
        String testChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog\n" +
            "        xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "        xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "         http://www.liquibase.org/xml/ns/snowflake\n" +
            "         http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet id=\"test\" author=\"test\">\n" +
            "        <alterSequence sequenceName=\"test_seq\" \n" +
            "                      incrementBy=\"5\"\n" +
            "                      snowflake:setNoOrder=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        // Write to temp file
        Path tempFile = Files.createTempFile("test", ".xml");
        Files.write(tempFile, testChangelog.getBytes());
        
        try {
            // Clear storage first
            SnowflakeNamespaceAttributeStorage.clear();
            
            // Parse the file
            SnowflakeNamespaceAwareXMLParser parser = new SnowflakeNamespaceAwareXMLParser();
            FileSystemResourceAccessor resourceAccessor = new FileSystemResourceAccessor(tempFile.getParent().toFile());
            parser.parseToNode(tempFile.getFileName().toString(), null, resourceAccessor);
            
            // Check if attributes were stored
            Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes("test_seq");
            System.out.println("DEBUG: Stored attributes for test_seq: " + attributes);
            
            assertNotNull(attributes, "Namespace attributes should have been stored");
            assertEquals("true", attributes.get("setNoOrder"), "setNoOrder attribute should be 'true'");
            
            // Test the generator with the stored attributes
            AlterSequenceStatement statement = new AlterSequenceStatement(null, "TESTHARNESS", "test_seq");
            statement.setIncrementBy(new java.math.BigInteger("5"));
            
            AlterSequenceGeneratorSnowflake generator = new AlterSequenceGeneratorSnowflake();
            liquibase.sql.Sql[] sqls = generator.generateSql(statement, new SnowflakeDatabase(), null);
            
            assertTrue(sqls.length > 0, "Should generate SQL");
            String sql = sqls[0].toSql();
            System.out.println("Generated SQL: " + sql);
            
            assertEquals("ALTER SEQUENCE TESTHARNESS.test_seq SET INCREMENT BY 5, NOORDER", sql);
            
        } finally {
            // Cleanup
            Files.deleteIfExists(tempFile);
            SnowflakeNamespaceAttributeStorage.clear();
        }
    }
}