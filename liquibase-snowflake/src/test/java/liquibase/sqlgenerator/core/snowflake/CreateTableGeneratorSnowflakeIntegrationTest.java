package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.datatype.core.IntType;
import liquibase.datatype.core.VarcharType;
import liquibase.datatype.core.TimestampType;
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
 * Integration tests for CreateTableGeneratorSnowflake.
 * Tests CREATE TABLE SQL and namespace implementations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests namespace implementations with parallel execution capability.
 * NOTE: Tables have schema isolation for parallel execution.
 */
public class CreateTableGeneratorSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdTables = new ArrayList<>();
    private String testDatabase; // Will be set from YAML configuration
    private String testSchema = "TEST_TABLE_SCHEMA";

    /**
     * CRITICAL: Generates unique table name based on test method name.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique table name for parallel execution
     */
    private String getUniqueTableName(String methodName) {
        return "TEST_CREATE_TBL_" + methodName;
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
        
        // Create test schema for table isolation
        try {
            PreparedStatement createSchemaStmt = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + testSchema);
            createSchemaStmt.execute();
            createSchemaStmt.close();
            
            PreparedStatement useSchemaStmt = connection.prepareStatement("USE SCHEMA " + testSchema);
            useSchemaStmt.execute();
            useSchemaStmt.close();
        } catch (SQLException e) {
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created tables using unique names
        for (String tableName : createdTables) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP TABLE IF EXISTS " + testDatabase + "." + testSchema + "." + tableName);
                dropStmt.execute();
                dropStmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to cleanup table " + tableName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testBasicTableCreation() throws Exception {
        String tableName = getUniqueTableName("testBasicTableCreation");
        createdTables.add(tableName);


        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("id", new IntType(), null, new ColumnConstraint[]{new NotNullConstraint()});
        statement.addColumn("name", new VarcharType(), null);
        statement.addColumn("created_at", new TimestampType(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TABLE") && sql.contains(tableName));
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("INT")); // Snowflake uses INT instead of INTEGER
        assertTrue(sql.contains("name"));
        assertTrue(sql.contains("VARCHAR")); // VARCHAR length may not be specified
        assertTrue(sql.contains("created_at"));
        assertTrue(sql.toUpperCase().contains("TIMESTAMP") || sql.toUpperCase().contains("TIMESTAMP_NTZ") || sql.toUpperCase().contains("DATETIME"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testTableWithNamespaceValidation() throws Exception {
        String tableName = getUniqueTableName("testTableWithNamespaceValidation");
        createdTables.add(tableName);


        // Test that tables are created in the correct namespace (database.schema.table)
        CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
        statement.addColumn("test_col", new VarcharType(), null);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TABLE") && sql.contains(tableName));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        // Verify table exists in correct namespace by querying information_schema
        String verifyQuery = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND UPPER(TABLE_NAME) = UPPER(?)";
        PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery);
        verifyStmt.setString(1, testSchema);
        verifyStmt.setString(2, tableName);
        
        java.sql.ResultSet rs = verifyStmt.executeQuery();
        
        // Debug: show what tables actually exist
        PreparedStatement debugStmt = connection.prepareStatement("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) LIKE UPPER(?)");
        debugStmt.setString(1, tableName + "%");
        java.sql.ResultSet debugRs = debugStmt.executeQuery();
        while (debugRs.next()) {
        }
        debugRs.close();
        debugStmt.close();
        
        assertTrue(rs.next(), "Table should exist in the specified schema");
        assertEquals(tableName.toUpperCase(), rs.getString("TABLE_NAME").toUpperCase());
        
        rs.close();
        verifyStmt.close();

    }

    @Test
    public void testParallelTableCreation() throws Exception {
        String table1 = getUniqueTableName("testParallel1");
        String table2 = getUniqueTableName("testParallel2");
        String table3 = getUniqueTableName("testParallel3");
        createdTables.add(table1);
        createdTables.add(table2);
        createdTables.add(table3);


        // Create multiple tables that could run in parallel
        String[] tables = {table1, table2, table3};
        
        for (String tableName : tables) {
            CreateTableStatement statement = new CreateTableStatement(null, null, tableName);
            statement.addColumn("id", new IntType(), null);
            statement.addColumn("parallel_test", new VarcharType(), null);

            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            assertNotNull(sqls);
            assertEquals(1, sqls.length);

            String sql = sqls[0].toSql();
            assertTrue(sql.contains("CREATE TABLE") && sql.contains(tableName));

            // Execute against live database
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
            preparedStatement.close();

        }

    }

    @Test
    public void testValidationMissingTableName() throws Exception {

        CreateTableStatement statement = new CreateTableStatement(null, null, null);
        statement.addColumn("test_col", new VarcharType(), null);

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing table name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("table") || e.getMessage().contains("name") || e.getMessage().contains("required"));
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {

        // Create multiple tables using the naming strategy
        String table1 = getUniqueTableName("testMethod1");
        String table2 = getUniqueTableName("testMethod2");
        String table3 = getUniqueTableName("testMethod3");

        assertNotEquals(table1, table2);
        assertNotEquals(table2, table3);
        assertNotEquals(table1, table3);

        assertTrue(table1.startsWith("TEST_CREATE_TBL_"));
        assertTrue(table2.startsWith("TEST_CREATE_TBL_"));
        assertTrue(table3.startsWith("TEST_CREATE_TBL_"));


    }
}