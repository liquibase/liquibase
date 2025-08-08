package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.DropSchemaStatement;
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
 * Integration tests for DropSchemaGeneratorSnowflake.
 * Tests all DROP SCHEMA SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 * NOTE: Schemas have schema isolation within each database for parallel execution.
 */
public class DropSchemaGeneratorSnowflakeIntegrationTest {

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
        return "TEST_DROP_SCHEMA_" + methodName;
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
        // MANDATORY: Backup cleanup all created schemas using unique names
        for (String schemaName : createdSchemas) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP SCHEMA IF EXISTS " + testDatabase + "." + schemaName);
                dropStmt.execute();
                dropStmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to backup cleanup schema " + schemaName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testBasicDrop() throws Exception {
        String schemaName = getUniqueSchemaName("testBasicDrop");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropIfExists() throws Exception {
        String schemaName = getUniqueSchemaName("testDropIfExists");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA IF EXISTS " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropIfExistsNonExistent() throws Exception {
        String schemaName = getUniqueSchemaName("testDropIfExistsNonExistent");
        // Do NOT add to createdSchemas since it doesn't exist


        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA IF EXISTS " + schemaName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database - should succeed even though schema doesn't exist
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropRestrict() throws Exception {
        String schemaName = getUniqueSchemaName("testDropRestrict");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setRestrict(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA " + schemaName + " RESTRICT";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropCascade() throws Exception {
        String schemaName = getUniqueSchemaName("testDropCascade");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setCascade(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA " + schemaName + " CASCADE";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropIfExistsRestrict() throws Exception {
        String schemaName = getUniqueSchemaName("testDropIfExistsRestrict");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setIfExists(true);
        statement.setRestrict(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA IF EXISTS " + schemaName + " RESTRICT";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testDropIfExistsCascade() throws Exception {
        String schemaName = getUniqueSchemaName("testDropIfExistsCascade");
        createdSchemas.add(schemaName);


        // First create the schema
        PreparedStatement createStmt = connection.prepareStatement("CREATE SCHEMA " + schemaName);
        createStmt.execute();
        createStmt.close();

        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setIfExists(true);
        statement.setCascade(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP SCHEMA IF EXISTS " + schemaName + " CASCADE";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testValidationMissingSchemaName() throws Exception {

        DropSchemaStatement statement = new DropSchemaStatement();
        // Intentionally not setting schemaName

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing schema name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("schema") || e.getMessage().contains("name") || e.getMessage().contains("required"));
        }
    }

    @Test
    public void testValidationCascadeAndRestrict() throws Exception {
        String schemaName = getUniqueSchemaName("testValidationCascadeAndRestrict");


        DropSchemaStatement statement = new DropSchemaStatement();
        statement.setSchemaName(schemaName);
        statement.setCascade(true);
        statement.setRestrict(true);

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for both CASCADE and RESTRICT");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("cascade") || e.getMessage().toLowerCase().contains("restrict") || e.getMessage().toLowerCase().contains("mutual"));
        }
    }

    @Test
    public void testSequentialDropOperations() throws Exception {
        String schema1 = getUniqueSchemaName("testSequential1");
        String schema2 = getUniqueSchemaName("testSequential2");
        String schema3 = getUniqueSchemaName("testSequential3");
        createdSchemas.add(schema1);
        createdSchemas.add(schema2);
        createdSchemas.add(schema3);


        // Create all schemas
        PreparedStatement createStmt1 = connection.prepareStatement("CREATE SCHEMA " + schema1);
        createStmt1.execute();
        createStmt1.close();

        PreparedStatement createStmt2 = connection.prepareStatement("CREATE SCHEMA " + schema2);
        createStmt2.execute();
        createStmt2.close();

        PreparedStatement createStmt3 = connection.prepareStatement("CREATE SCHEMA " + schema3);
        createStmt3.execute();
        createStmt3.close();

        // Drop all schemas sequentially
        String[] schemas = {schema1, schema2, schema3};
        for (String schName : schemas) {
            DropSchemaStatement statement = new DropSchemaStatement();
            statement.setSchemaName(schName);

            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            assertNotNull(sqls);
            assertEquals(1, sqls.length);

            String expectedSQL = "DROP SCHEMA " + schName;
            assertEquals(expectedSQL, sqls[0].toSql());

            // Execute against live database
            PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
            preparedStatement.execute();
            preparedStatement.close();

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

        assertTrue(schema1.startsWith("TEST_DROP_SCHEMA_"));
        assertTrue(schema2.startsWith("TEST_DROP_SCHEMA_"));
        assertTrue(schema3.startsWith("TEST_DROP_SCHEMA_"));


    }
}