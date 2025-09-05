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

    }

    @Test
    public void testOrReplace() throws Exception {
        String schemaName = getUniqueSchemaName("testOrReplace");
        createdSchemas.add(schemaName);


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

    }

    @Test
    public void testIfNotExists() throws Exception {
        String schemaName = getUniqueSchemaName("testIfNotExists");
        createdSchemas.add(schemaName);


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

    }

    @Test
    public void testWithComment() throws Exception {
        String schemaName = getUniqueSchemaName("testWithComment");
        createdSchemas.add(schemaName);


        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setComment("Test schema for integration testing");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE SCHEMA " + schemaName), "SQL should start with CREATE SCHEMA: " + sql);
        assertTrue(sql.contains("COMMENT"), "SQL should contain COMMENT clause: " + sql);
        assertTrue(sql.contains("'Test schema for integration testing'"), "SQL should contain comment text: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testWithDataRetention() throws Exception {
        String schemaName = getUniqueSchemaName("testWithDataRetention");
        createdSchemas.add(schemaName);


        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setDataRetentionTimeInDays("7");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE SCHEMA " + schemaName), "SQL should start with CREATE SCHEMA: " + sql);
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"), "SQL should contain data retention clause: " + sql);
        assertTrue(sql.contains("7"), "SQL should contain retention time value: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testTransientSchema() throws Exception {
        String schemaName = getUniqueSchemaName("testTransientSchema");
        createdSchemas.add(schemaName);


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

    }

    @Test
    public void testManagedAccessSchema() throws Exception {
        String schemaName = getUniqueSchemaName("testManagedAccessSchema");
        createdSchemas.add(schemaName);


        CreateSchemaStatement statement = new CreateSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setManaged(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE SCHEMA " + schemaName), "SQL should start with CREATE SCHEMA: " + sql);
        assertTrue(sql.contains("WITH MANAGED ACCESS"), "SQL should contain managed access clause: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testAllProperties() throws Exception {
        String schemaName = getUniqueSchemaName("testAllProperties");
        createdSchemas.add(schemaName);


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
        assertTrue(sql.startsWith("CREATE SCHEMA " + schemaName), "SQL should start with CREATE SCHEMA: " + sql);
        assertTrue(sql.contains("COMMENT"), "SQL should contain COMMENT clause: " + sql);
        assertTrue(sql.contains("'Comprehensive test schema'"), "SQL should contain comment text: " + sql);
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"), "SQL should contain data retention clause: " + sql);
        assertTrue(sql.contains("14"), "SQL should contain retention time value: " + sql);
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"), "SQL should contain max extension time clause: " + sql);
        assertTrue(sql.contains("28"), "SQL should contain extension time value: " + sql);
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"), "SQL should contain collation clause: " + sql);
        assertTrue(sql.contains("'utf8'"), "SQL should contain collation value: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testBasicSchemaValidation() throws Exception {
        String schemaName = getUniqueSchemaName("testBasicSchemaValidation");
        createdSchemas.add(schemaName);


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

    }

    @Test
    public void testValidationMissingSchemaName() throws Exception {

        CreateSchemaStatement statement = new CreateSchemaStatement();
        // Intentionally not setting schemaName

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing schema name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("schema") || e.getMessage().contains("name") || e.getMessage().contains("required"));
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {

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


    }
}