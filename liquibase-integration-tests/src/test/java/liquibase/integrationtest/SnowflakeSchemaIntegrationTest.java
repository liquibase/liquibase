package liquibase.integrationtest;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.sdk.resource.MockResourceAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class SnowflakeSchemaIntegrationTest {
    
    private Database database;
    private Connection connection;
    
    @Before
    public void setUp() throws Exception {
        ResourceBundle bundle = ResourceBundle.getBundle("liquibase.sdk.test.local");
        String url = bundle.getString("snowflake.url");
        String username = bundle.getString("snowflake.username");
        String password = bundle.getString("snowflake.password");
        
        assumeTrue("Snowflake test not configured", url != null && !url.isEmpty());
        
        connection = DriverManager.getConnection(url, username, password);
        DatabaseConnection dbConnection = new JdbcConnection(connection);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dbConnection);
        
        assertTrue("Not a Snowflake database", database instanceof SnowflakeDatabase);
        
        // Clean up any existing test schemas
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS TEST_TRANSIENT_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS TEST_MANAGED_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS TEST_FULL_SCHEMA CASCADE");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        if (database != null) {
            database.close();
        }
        if (connection != null && !connection.isClosed()) {
            // Clean up
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DROP SCHEMA IF EXISTS TEST_TRANSIENT_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS TEST_MANAGED_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS TEST_FULL_SCHEMA CASCADE");
            }
            connection.close();
        }
    }
    
    @Test
    public void testCreateTransientSchemaWithComment() throws Exception {
        String changelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"1\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_TRANSIENT_SCHEMA\" \n" +
            "                      transient=\"true\" \n" +
            "                      comment=\"Transient test schema\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        Map<String, String> resources = new HashMap<>();
        resources.put("changelog.xml", changelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml");
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            updateCommand.execute();
        });
        
        // Verify the schema was created
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_TRANSIENT_SCHEMA'").next();
            assertTrue("Schema TEST_TRANSIENT_SCHEMA should exist", exists);
        }
    }
    
    @Test
    public void testCreateManagedAccessSchema() throws Exception {
        String changelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"2\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_MANAGED_SCHEMA\" \n" +
            "                      managedAccess=\"true\" \n" +
            "                      comment=\"Managed access schema\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        Map<String, String> resources = new HashMap<>();
        resources.put("changelog.xml", changelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml");
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            updateCommand.execute();
        });
        
        // Verify the schema was created
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_MANAGED_SCHEMA'").next();
            assertTrue("Schema TEST_MANAGED_SCHEMA should exist", exists);
        }
    }
    
    @Test
    public void testCreateTransientManagedSchema() throws Exception {
        String changelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"3\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_FULL_SCHEMA\" \n" +
            "                      transient=\"true\"\n" +
            "                      managedAccess=\"true\" \n" +
            "                      comment=\"Full featured schema\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        Map<String, String> resources = new HashMap<>();
        resources.put("changelog.xml", changelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelog.xml");
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            updateCommand.execute();
        });
        
        // Verify the schema was created
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_FULL_SCHEMA'").next();
            assertTrue("Schema TEST_FULL_SCHEMA should exist", exists);
        }
    }
}