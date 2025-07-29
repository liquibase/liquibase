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
import static org.junit.Assert.assertFalse;
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
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_TRANSIENT_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_MANAGED_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_FULL_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_SCHEMA CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_CASCADE CASCADE");
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_RESTRICT CASCADE");
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
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_TRANSIENT_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_MANAGED_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_FULL_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_SCHEMA CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_CASCADE CASCADE");
                stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_DROP_RESTRICT CASCADE");
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
    
    @Test
    public void testDropBasicSchema() throws Exception {
        // First create a schema to drop
        String createChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"1\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_DROP_SCHEMA\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        Map<String, String> resources = new HashMap<>();
        resources.put("createChangelog.xml", createChangelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope createCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        createCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "createChangelog.xml");
        createCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            createCommand.execute();
        });
        
        // Verify schema was created
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_DROP_SCHEMA'").next();
            assertTrue("Schema TEST_DROP_SCHEMA should exist before drop", exists);
        }
        
        // Now drop the schema
        String dropChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"2\">\n" +
            "        <snowflake:dropSchema schemaName=\"TEST_DROP_SCHEMA\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        resources.put("dropChangelog.xml", dropChangelog);
        resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope dropCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        dropCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "dropChangelog.xml");
        dropCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            dropCommand.execute();
        });
        
        // Verify the schema was dropped
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_DROP_SCHEMA'").next();
            assertFalse("Schema TEST_DROP_SCHEMA should not exist after drop", exists);
        }
    }
    
    @Test
    public void testDropSchemaWithCascade() throws Exception {
        // Create schema with a table to test CASCADE behavior
        String createChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"1\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_DROP_CASCADE\"/>\n" +
            "    </changeSet>\n" +
            "    <changeSet author=\"test\" id=\"2\">\n" +
            "        <sql>CREATE TABLE LIQUIBASE_INTEGRATION_TEST.TEST_DROP_CASCADE.test_table (id INT)</sql>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        Map<String, String> resources = new HashMap<>();
        resources.put("createChangelog.xml", createChangelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope createCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        createCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "createChangelog.xml");
        createCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            createCommand.execute();
        });
        
        // Now drop the schema with CASCADE
        String dropChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"3\">\n" +
            "        <snowflake:dropSchema schemaName=\"TEST_DROP_CASCADE\" cascade=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        resources.put("dropChangelog.xml", dropChangelog);
        resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope dropCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        dropCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "dropChangelog.xml");
        dropCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            dropCommand.execute();
        });
        
        // Verify the schema was dropped
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_DROP_CASCADE'").next();
            assertFalse("Schema TEST_DROP_CASCADE should not exist after cascade drop", exists);
        }
    }
    
    @Test
    public void testDropSchemaIfExists() throws Exception {
        // Test IF EXISTS with non-existent schema - should not fail
        String dropChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"1\">\n" +
            "        <snowflake:dropSchema schemaName=\"NONEXISTENT_SCHEMA\" ifExists=\"true\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        Map<String, String> resources = new HashMap<>();
        resources.put("dropChangelog.xml", dropChangelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope dropCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        dropCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "dropChangelog.xml");
        dropCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        // This should complete without error
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            dropCommand.execute();
        });
        
        // Test should pass if no exception was thrown
        assertTrue("IF EXISTS should handle non-existent schema gracefully", true);
    }
    
    @Test
    public void testAlterSchemaBasic() throws Exception {
        // First create a schema to alter
        String createChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"test-alter-1\">\n" +
            "        <snowflake:createSchema schemaName=\"TEST_ALTER_SCHEMA\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";

        Map<String, String> resources = new HashMap<>();
        resources.put("createAlterSchema.xml", createChangelog);
        MockResourceAccessor resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope createCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        createCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "createAlterSchema.xml");
        createCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            createCommand.execute();
        });
        
        // Now alter the schema with a comment
        String alterChangelog = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                   xmlns:snowflake=\"http://www.liquibase.org/xml/ns/snowflake\"\n" +
            "                   xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
            "                      http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake\n" +
            "                      http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd\">\n" +
            "    \n" +
            "    <changeSet author=\"test\" id=\"test-alter-2\">\n" +
            "        <snowflake:alterSchema schemaName=\"TEST_ALTER_SCHEMA\" newComment=\"Test comment for ALTER SCHEMA\"/>\n" +
            "    </changeSet>\n" +
            "</databaseChangeLog>";
        
        resources.put("alterSchemaChangelog.xml", alterChangelog);
        resourceAccessor = new MockResourceAccessor(resources);
        
        CommandScope alterCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        alterCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "alterSchemaChangelog.xml");
        alterCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
        
        // This should complete without error
        Scope.child(Scope.Attr.resourceAccessor, resourceAccessor, () -> {
            alterCommand.execute();
        });
        
        // Verify the schema still exists (basic validation that alter didn't break it)
        try (Statement stmt = connection.createStatement()) {
            boolean exists = stmt.executeQuery("SHOW SCHEMAS LIKE 'TEST_ALTER_SCHEMA'").next();
            assertTrue("Schema TEST_ALTER_SCHEMA should still exist after alter", exists);
        }
        
        // Clean up
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS LIQUIBASE_INTEGRATION_TEST.TEST_ALTER_SCHEMA CASCADE");
        }
    }
}