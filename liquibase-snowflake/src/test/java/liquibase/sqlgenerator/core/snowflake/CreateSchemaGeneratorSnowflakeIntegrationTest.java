package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateSchemaStatement;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for CreateSchemaGeneratorSnowflake.
 * Tests all CREATE SCHEMA SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 * NOTE: Schemas have schema isolation within each database for parallel execution.
 */
public class CreateSchemaGeneratorSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdSchemas = new ArrayList<>();
    private String testDatabase; // Will be set from YAML configuration

    /**
     * CRITICAL: Generates unique schema name based on test method name.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique schema name for parallel execution
     */
    private String getUniqueSchemaName(String methodName) {
        return "TEST_CREATE_SCHEMA_" + methodName;
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Get database name from the connection
        testDatabase = database.getDefaultCatalogName();
        if (testDatabase == null) {
            testDatabase = "LB_DBEXT_INT_DB"; // Fallback to YAML configured database
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
    public void testBasicRequiredOnly() throws Exception {
        String schemaName = getUniqueSchemaName("testBasicRequiredOnly");
        createdSchemas.add(schemaName);

        System.out.println("Testing Basic Required Only: CREATE SCHEMA " + schemaName);

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE SCHEMA " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Basic Required Only");
    }

    @Test
    public void testOrReplace() throws Exception {
        String schemaName = getUniqueSchemaName("testOrReplace");
        createdSchemas.add(schemaName);

        System.out.println("Testing OR REPLACE: CREATE OR REPLACE SCHEMA " + schemaName);

        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setOrReplace(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE OR REPLACE SCHEMA " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: OR REPLACE");
    }

    @Test
    public void testIfNotExists() throws Exception {
        String schemaName = getUniqueSchemaName("testIfNotExists");
        createdSchemas.add(schemaName);

        System.out.println("Testing IF NOT EXISTS: CREATE SCHEMA IF NOT EXISTS " + schemaName);

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setIfNotExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        // Execute again to test IF NOT EXISTS
        PreparedStatement preparedStatement2 = connection.prepareStatement(sqls[0].toSql());
        preparedStatement2.execute();
        preparedStatement2.close();

        System.out.println("✅ SUCCESS: IF NOT EXISTS");
    }

    @Test
    public void testWithComment() throws Exception {
        String schemaName = getUniqueSchemaName("testWithComment");
        createdSchemas.add(schemaName);

        System.out.println("Testing WITH COMMENT: CREATE SCHEMA " + schemaName + " COMMENT='Test schema'");

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setComment("Test schema for integration testing");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SCHEMA " + schemaName));
        assertTrue(sql.contains("COMMENT"));
        assertTrue(sql.contains("'Test schema for integration testing'"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: WITH COMMENT");
    }

    @Test
    public void testWithDataRetention() throws Exception {
        String schemaName = getUniqueSchemaName("testWithDataRetention");
        createdSchemas.add(schemaName);

        System.out.println("Testing WITH DATA_RETENTION_TIME_IN_DAYS: CREATE SCHEMA " + schemaName + " DATA_RETENTION_TIME_IN_DAYS=7");

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setDataRetentionTimeInDays("7");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SCHEMA " + schemaName));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("7"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: WITH DATA_RETENTION_TIME_IN_DAYS");
    }

    @Test
    public void testTransientSchema() throws Exception {
        String schemaName = getUniqueSchemaName("testTransientSchema");
        createdSchemas.add(schemaName);

        System.out.println("Testing TRANSIENT: CREATE TRANSIENT SCHEMA " + schemaName);

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setTransient(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE TRANSIENT SCHEMA " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: TRANSIENT SCHEMA");
    }

    @Test
    public void testManagedAccessSchema() throws Exception {
        String schemaName = getUniqueSchemaName("testManagedAccessSchema");
        createdSchemas.add(schemaName);

        System.out.println("Testing MANAGED ACCESS: CREATE SCHEMA " + schemaName + " WITH MANAGED ACCESS");

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setManaged(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SCHEMA " + schemaName));
        assertTrue(sql.contains("WITH MANAGED ACCESS"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: MANAGED ACCESS SCHEMA");
    }

    @Test
    public void testAllProperties() throws Exception {
        String schemaName = getUniqueSchemaName("testAllProperties");
        createdSchemas.add(schemaName);

        System.out.println("Testing All Properties: CREATE SCHEMA " + schemaName + " with comprehensive configuration");

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setComment("Comprehensive test schema");
        statement.setDataRetentionTimeInDays("14");
        statement.setMaxDataExtensionTimeInDays("28");
        statement.setDefaultDdlCollation("utf8");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SCHEMA " + schemaName));
        assertTrue(sql.contains("COMMENT"));
        assertTrue(sql.contains("'Comprehensive test schema'"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("14"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("28"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"));
        assertTrue(sql.contains("'utf8'"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: All Properties");
    }

    @Test
    public void testBasicSchemaValidation() throws Exception {
        String schemaName = getUniqueSchemaName("testBasicSchemaValidation");
        createdSchemas.add(schemaName);

        System.out.println("Testing Schema Creation Validation: CREATE SCHEMA " + schemaName);

        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE SCHEMA " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Basic Schema Validation");
    }

    @Test
    public void testValidationMissingSchemaName() throws Exception {
        System.out.println("Testing Validation: Missing schema name should fail");

        CreateSchemaStatement statement = new CreateSchemaStatement();
        // Intentionally not setting schemaName

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing schema name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("schema") || e.getMessage().contains("name") || e.getMessage().contains("required"));
            System.out.println("✅ SUCCESS: Validation correctly failed for missing schema name");
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

        assertTrue(schema1.startsWith("TEST_CREATE_SCHEMA_"));
        assertTrue(schema2.startsWith("TEST_CREATE_SCHEMA_"));
        assertTrue(schema3.startsWith("TEST_CREATE_SCHEMA_"));

        System.out.println("Schema 1: " + schema1);
        System.out.println("Schema 2: " + schema2);
        System.out.println("Schema 3: " + schema3);

        System.out.println("✅ SUCCESS: Unique Naming Strategy validated");
    }
}