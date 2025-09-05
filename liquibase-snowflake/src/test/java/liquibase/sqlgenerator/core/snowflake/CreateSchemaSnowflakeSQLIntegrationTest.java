package liquibase.sqlgenerator.core.snowflake;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.assertTrue;

/**
 * Direct SQL execution test to verify CREATE SCHEMA syntax works in Snowflake.
 * Updated to use modern TestDatabaseConfigUtil with YAML configuration.
 */
public class CreateSchemaSnowflakeSQLIntegrationTest {
    
    private Connection connection;
    private Database database;
    
    @Before
    public void setUp() throws Exception {
        // Use YAML configuration for Snowflake connection
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Clean up any existing test schemas
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_TEST_TRANSIENT CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_TEST_MANAGED CASCADE");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            // Clean up
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_TEST_TRANSIENT CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_TEST_MANAGED CASCADE");
            } catch (Exception e) {
                // Ignore cleanup errors
            }
            connection.close();
        }
    }
    
    @Test
    public void testCreateTransientSchemaWithComment() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            // This is the SQL our generator produces
            stmt.execute("CREATE TRANSIENT SCHEMA LIQUIBASE_TEST_TRANSIENT COMMENT = 'Test transient schema'");
            
            // Verify it was created
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'LIQUIBASE_TEST_TRANSIENT'").next();
            assertTrue("Schema should exist", exists);
        }
    }
    
    @Test  
    public void testCreateManagedAccessSchemaWithComment() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            // This is the SQL our generator produces
            stmt.execute("CREATE SCHEMA LIQUIBASE_TEST_MANAGED WITH MANAGED ACCESS COMMENT = 'Test managed schema'");
            
            // Verify it was created
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'LIQUIBASE_TEST_MANAGED'").next();
            assertTrue("Schema should exist", exists);
        }
    }
    
    @Test
    public void testLiquibaseUpdateWithTransientSchema() throws Exception {
        String changelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"create-transient-schema\">\n" +
            "        <snowflake:createSchema schemaName=\"LIQUIBASE_TEST_TRANSIENT\" \n" +
            "                      transient=\"true\" \n" +
            "                      comment=\"Test transient schema via Liquibase\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        // Write changelog to temp file
        java.io.File tempFile = java.io.File.createTempFile("test-changelog", ".xml");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), changelog.getBytes());
        
        // Execute via Liquibase
        ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
        Liquibase liquibase = new Liquibase(tempFile.getName(), 
                                          new liquibase.resource.DirectoryResourceAccessor(tempFile.getParentFile()), 
                                          database);
        
        // Generate SQL to verify what would be executed
        StringWriter writer = new StringWriter();
        liquibase.update("", writer);
        String generatedSql = writer.toString();
        
        
        // Verify the SQL contains the correct syntax
        assertTrue("SQL should contain CREATE TRANSIENT SCHEMA",
                   generatedSql.contains("CREATE") && generatedSql.contains("TRANSIENT") && generatedSql.contains("SCHEMA"));
        
        // Now actually execute it
        liquibase.update("");
        
        // Verify it was created
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'LIQUIBASE_TEST_TRANSIENT'").next();
            assertTrue("Schema should exist", exists);
        }
    }
}