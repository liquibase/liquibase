package liquibase.sqlgenerator.core.snowflake;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateDatabaseStatement;
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
 * Integration tests for CreateDatabaseGeneratorSnowflake.
 * Tests all CREATE DATABASE SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 */
public class CreateDatabaseGeneratorSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdDatabases = new ArrayList<>();

    /**
     * CRITICAL: Generates unique database name based on test method name.
     * ADDRESSES_CORE_ISSUE: Account-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique database name for parallel execution
     */
    private String getUniqueDatabaseName(String methodName) {
        return "TEST_CREATE_DB_" + methodName.toUpperCase() + "_" + System.currentTimeMillis();
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created databases using unique names
        for (String databaseName : createdDatabases) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP DATABASE IF EXISTS " + databaseName);
                dropStmt.execute();
                dropStmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to cleanup database " + databaseName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testBasicRequiredOnly() throws Exception {
        String databaseName = getUniqueDatabaseName("testBasicRequiredOnly");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE DATABASE " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testOrReplace() throws Exception {
        String databaseName = getUniqueDatabaseName("testOrReplace");
        createdDatabases.add(databaseName);


        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setOrReplace(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE OR REPLACE DATABASE " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testIfNotExists() throws Exception {
        String databaseName = getUniqueDatabaseName("testIfNotExists");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setIfNotExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE DATABASE IF NOT EXISTS " + databaseName;
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
        String databaseName = getUniqueDatabaseName("testWithComment");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setComment("Test database for integration testing");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE DATABASE " + databaseName), "SQL should start with CREATE DATABASE: " + sql);
        assertTrue(sql.contains("COMMENT"), "SQL should contain COMMENT clause: " + sql);
        assertTrue(sql.contains("'Test database for integration testing'"), "SQL should contain comment text: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testWithDataRetention() throws Exception {
        String databaseName = getUniqueDatabaseName("testWithDataRetention");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setDataRetentionTimeInDays("7");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE DATABASE " + databaseName), "SQL should start with CREATE DATABASE: " + sql);
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"), "SQL should contain data retention clause: " + sql);
        assertTrue(sql.contains("7"), "SQL should contain retention time value: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testTransientDatabase() throws Exception {
        String databaseName = getUniqueDatabaseName("testTransientDatabase");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setTransient(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE TRANSIENT DATABASE " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testWithCollation() throws Exception {
        String databaseName = getUniqueDatabaseName("testWithCollation");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setDefaultDdlCollation("utf8");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE DATABASE " + databaseName), "SQL should start with CREATE DATABASE: " + sql);
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"), "SQL should contain collation clause: " + sql);
        assertTrue(sql.contains("'utf8'"), "SQL should contain collation value: " + sql);

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testAllProperties() throws Exception {
        String databaseName = getUniqueDatabaseName("testAllProperties");
        createdDatabases.add(databaseName);


        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setComment("Comprehensive test database");
        statement.setDataRetentionTimeInDays("14");
        statement.setMaxDataExtensionTimeInDays("28");
        statement.setDefaultDdlCollation("utf8");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("CREATE DATABASE " + databaseName), "SQL should start with CREATE DATABASE: " + sql);
        assertTrue(sql.contains("COMMENT"), "SQL should contain COMMENT clause: " + sql);
        assertTrue(sql.contains("'Comprehensive test database'"), "SQL should contain comment text: " + sql);
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
    public void testCloneDatabase() throws Exception {
        String sourceDatabaseName = getUniqueDatabaseName("testCloneSource");
        String cloneDatabaseName = getUniqueDatabaseName("testCloneTarget");
        createdDatabases.add(sourceDatabaseName);
        createdDatabases.add(cloneDatabaseName);


        // First create source database
        PreparedStatement createSourceStmt = connection.prepareStatement("CREATE DATABASE " + sourceDatabaseName);
        createSourceStmt.execute();
        createSourceStmt.close();

        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        statement.setDatabaseName(cloneDatabaseName);
        statement.setCloneFrom(sourceDatabaseName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "CREATE DATABASE " + cloneDatabaseName + " CLONE " + sourceDatabaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

    }

    @Test
    public void testValidationMissingDatabaseName() throws Exception {

        CreateDatabaseStatement statement = new CreateDatabaseStatement();
        // Intentionally not setting databaseName

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing database name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("database") || e.getMessage().contains("name") || e.getMessage().contains("required"));
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {

        // Create multiple databases using the naming strategy
        String db1 = getUniqueDatabaseName("testMethod1");
        String db2 = getUniqueDatabaseName("testMethod2");
        String db3 = getUniqueDatabaseName("testMethod3");

        assertNotEquals(db1, db2);
        assertNotEquals(db2, db3);
        assertNotEquals(db1, db3);

        assertTrue(db1.startsWith("TEST_CREATE_DB_"));
        assertTrue(db2.startsWith("TEST_CREATE_DB_"));
        assertTrue(db3.startsWith("TEST_CREATE_DB_"));


    }
}