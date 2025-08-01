package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.AlterSchemaStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AlterSchemaGeneratorSnowflake.
 * Tests all ALTER SCHEMA SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 * NOTE: Schemas have schema isolation within each database for parallel execution.
 */
public class AlterSchemaGeneratorSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdSchemas = new ArrayList<>();
    private String testDatabase = "TEST_INTEGRATION_DB";

    /**
     * CRITICAL: Generates unique schema name based on test method name.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique schema name for parallel execution
     */
    private String getUniqueSchemaName(String methodName) {
        return "TEST_ALTER_SCHEMA_" + methodName;
    }

    @BeforeEach
    public void setUp() throws Exception {
        String url = System.getenv("SNOWFLAKE_URL");
        String user = System.getenv("SNOWFLAKE_USER");
        String password = System.getenv("SNOWFLAKE_PASSWORD");
        
        if (url == null || user == null || password == null) {
            throw new RuntimeException("Snowflake connection environment variables not set");
        }

        connection = DriverManager.getConnection(url, user, password);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Create test database for schema isolation
        try {
            PreparedStatement createDbStmt = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + testDatabase);
            createDbStmt.execute();
            createDbStmt.close();
            
            PreparedStatement useDbStmt = connection.prepareStatement("USE DATABASE " + testDatabase);
            useDbStmt.execute();
            useDbStmt.close();
        } catch (SQLException e) {
            System.out.println("Database already exists or creation failed: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created schemas using unique names
        for (String schemaName : createdSchemas) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP SCHEMA IF EXISTS " + testDatabase + "." + schemaName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Cleaned up schema: " + testDatabase + "." + schemaName);
            } catch (SQLException e) {
                System.err.println("Failed to cleanup schema " + schemaName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testRenameSchema() throws Exception {
        String originalName = getUniqueSchemaName("testRenameOriginal");
        String newName = getUniqueSchemaName("testRenameNew");
        createdSchemas.add(originalName);
        createdSchemas.add(newName); // Track both names for cleanup

        System.out.println("Testing RENAME: ALTER SCHEMA " + originalName + " RENAME TO " + newName);

        // First create the original schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + originalName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(originalName);
        statement.setNewName(newName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "ALTER SCHEMA " + originalName + " RENAME TO " + newName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: RENAME SCHEMA");
    }

    @Test
    public void testRenameWithIfExists() throws Exception {
        String originalName = getUniqueSchemaName("testRenameIfExistsOriginal");
        String newName = getUniqueSchemaName("testRenameIfExistsNew");
        createdSchemas.add(originalName);
        createdSchemas.add(newName);

        System.out.println("Testing RENAME IF EXISTS: ALTER SCHEMA IF EXISTS " + originalName + " RENAME TO " + newName);

        // First create the original schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + originalName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(originalName);
        statement.setNewName(newName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "ALTER SCHEMA IF EXISTS " + originalName + " RENAME TO " + newName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: RENAME IF EXISTS");
    }

    @Test
    public void testSetDataRetention() throws Exception {
        String schemaName = getUniqueSchemaName("testSetDataRetention");
        createdSchemas.add(schemaName);

        System.out.println("Testing SET DATA_RETENTION_TIME_IN_DAYS: ALTER SCHEMA " + schemaName + " SET DATA_RETENTION_TIME_IN_DAYS = 14");

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setNewDataRetentionTimeInDays("14");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("14"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: SET DATA_RETENTION_TIME_IN_DAYS");
    }

    @Test
    public void testSetComment() throws Exception {
        String schemaName = getUniqueSchemaName("testSetComment");
        createdSchemas.add(schemaName);

        System.out.println("Testing SET COMMENT: ALTER SCHEMA " + schemaName + " SET COMMENT = 'Updated comment'");

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setNewComment("Updated comment for integration testing");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("COMMENT"));
        assertTrue(sql.contains("'Updated comment for integration testing'"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: SET COMMENT");
    }

    @Test
    public void testSetMultipleProperties() throws Exception {
        String schemaName = getUniqueSchemaName("testSetMultipleProperties");
        createdSchemas.add(schemaName);

        System.out.println("Testing SET Multiple Properties: ALTER SCHEMA " + schemaName + " SET multiple properties");

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setNewDataRetentionTimeInDays("21");
        statement.setNewMaxDataExtensionTimeInDays("28");
        statement.setNewComment("Multi-property test schema");
        statement.setNewDefaultDdlCollation("utf8");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("21"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("28"));
        assertTrue(sql.contains("COMMENT"));
        assertTrue(sql.contains("'Multi-property test schema'"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"));
        assertTrue(sql.contains("'utf8'"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: SET Multiple Properties");
    }

    @Test
    public void testEnableManagedAccess() throws Exception {
        String schemaName = getUniqueSchemaName("testEnableManagedAccess");
        createdSchemas.add(schemaName);

        System.out.println("Testing ENABLE MANAGED ACCESS: ALTER SCHEMA " + schemaName + " ENABLE MANAGED ACCESS");

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setEnableManagedAccess(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("ENABLE MANAGED ACCESS"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: ENABLE MANAGED ACCESS");
    }

    @Test
    public void testDisableManagedAccess() throws Exception {
        String schemaName = getUniqueSchemaName("testDisableManagedAccess");
        createdSchemas.add(schemaName);

        System.out.println("Testing DISABLE MANAGED ACCESS: ALTER SCHEMA " + schemaName + " DISABLE MANAGED ACCESS");

        // First create the schema with managed access
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName + " WITH MANAGED ACCESS");
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setDisableManagedAccess(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("DISABLE MANAGED ACCESS"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DISABLE MANAGED ACCESS");
    }

    @Test
    public void testUnsetDataRetention() throws Exception {
        String schemaName = getUniqueSchemaName("testUnsetDataRetention");
        createdSchemas.add(schemaName);

        System.out.println("Testing UNSET DATA_RETENTION_TIME_IN_DAYS: ALTER SCHEMA " + schemaName + " UNSET DATA_RETENTION_TIME_IN_DAYS");

        // First create the schema with data retention
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName + " DATA_RETENTION_TIME_IN_DAYS = 7");
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setUnsetDataRetentionTimeInDays(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: UNSET DATA_RETENTION_TIME_IN_DAYS");
    }

    @Test
    public void testUnsetComment() throws Exception {
        String schemaName = getUniqueSchemaName("testUnsetComment");
        createdSchemas.add(schemaName);

        System.out.println("Testing UNSET COMMENT: ALTER SCHEMA " + schemaName + " UNSET COMMENT");

        // First create the schema with comment
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName + " COMMENT = 'Initial comment'");
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setUnsetComment(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA " + schemaName));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("COMMENT"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: UNSET COMMENT");
    }

    @Test
    public void testValidationMissingSchemaName() throws Exception {
        System.out.println("Testing Validation: Missing schema name should fail");

        AlterSchemaStatement statement = new AlterSchemaStatement();
        // Intentionally not setting schemaName
        statement.setNewComment("This should fail");

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing schema name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("schema") || e.getMessage().contains("name") || e.getMessage().contains("required"));
            System.out.println("✅ SUCCESS: Validation correctly failed for missing schema name");
        }
    }

    @Test
    public void testValidationNoProperties() throws Exception {
        String schemaName = getUniqueSchemaName("testValidationNoProperties");
        createdSchemas.add(schemaName);

        System.out.println("Testing Validation: No alteration properties should fail");

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName(schemaName);
        // Intentionally not setting any alteration properties

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for no alteration properties");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("property") || e.getMessage().contains("change") || e.getMessage().contains("alteration"));
            System.out.println("✅ SUCCESS: Validation correctly failed for no alteration properties");
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {
        System.out.println("Testing Unique Naming Strategy: Verifying all test schemas have unique names");

        // Create multiple schemas using the naming strategy
        String schema1 = getUniqueSchemaName("testMethod1");
        String schema2 = getUniqueSchemaName("testMethod2");
        String schema3 = getUniqueSchemaName("testMethod3");

        assertNotEquals(schema1, schema2);
        assertNotEquals(schema2, schema3);
        assertNotEquals(schema1, schema3);

        assertTrue(schema1.startsWith("TEST_ALTER_SCHEMA_"));
        assertTrue(schema2.startsWith("TEST_ALTER_SCHEMA_"));
        assertTrue(schema3.startsWith("TEST_ALTER_SCHEMA_"));

        System.out.println("Schema 1: " + schema1);
        System.out.println("Schema 2: " + schema2);
        System.out.println("Schema 3: " + schema3);

        System.out.println("✅ SUCCESS: Unique Naming Strategy validated");
    }
}