package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.DropDatabaseStatement;
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
 * Integration tests for DropDatabaseGeneratorSnowflake.
 * Tests all DROP DATABASE SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 */
public class DropDatabaseGeneratorSnowflakeIntegrationTest {

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
        return "TEST_DROP_DB_" + methodName;
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
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created databases using unique names (backup cleanup)
        for (String databaseName : createdDatabases) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP DATABASE IF EXISTS " + databaseName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Backup cleanup database: " + databaseName);
            } catch (SQLException e) {
                System.err.println("Failed to backup cleanup database " + databaseName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testBasicDrop() throws Exception {
        String databaseName = getUniqueDatabaseName("testBasicDrop");
        createdDatabases.add(databaseName);

        System.out.println("Testing Basic DROP: DROP DATABASE " + databaseName);

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Basic DROP");
    }

    @Test
    public void testDropIfExists() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropIfExists");
        createdDatabases.add(databaseName);

        System.out.println("Testing DROP IF EXISTS: DROP DATABASE IF EXISTS " + databaseName);

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE IF EXISTS " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP IF EXISTS");
    }

    @Test
    public void testDropIfExistsNonExistent() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropIfExistsNonExistent");
        // Do NOT add to createdDatabases since it doesn't exist

        System.out.println("Testing DROP IF EXISTS on non-existent database: DROP DATABASE IF EXISTS " + databaseName);

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE IF EXISTS " + databaseName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database - should succeed even though database doesn't exist
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP IF EXISTS on non-existent database");
    }

    @Test
    public void testDropRestrict() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropRestrict");
        createdDatabases.add(databaseName);

        System.out.println("Testing DROP RESTRICT: DROP DATABASE " + databaseName + " RESTRICT");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setRestrict(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE " + databaseName + " RESTRICT";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP RESTRICT");
    }

    @Test
    public void testDropCascade() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropCascade");
        createdDatabases.add(databaseName);

        System.out.println("Testing DROP CASCADE: DROP DATABASE " + databaseName + " CASCADE");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setCascade(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE " + databaseName + " CASCADE";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP CASCADE");
    }

    @Test
    public void testDropIfExistsRestrict() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropIfExistsRestrict");
        createdDatabases.add(databaseName);

        System.out.println("Testing DROP IF EXISTS RESTRICT: DROP DATABASE IF EXISTS " + databaseName + " RESTRICT");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setIfExists(true);
        statement.setRestrict(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE IF EXISTS " + databaseName + " RESTRICT";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP IF EXISTS RESTRICT");
    }

    @Test
    public void testDropIfExistsCascade() throws Exception {
        String databaseName = getUniqueDatabaseName("testDropIfExistsCascade");
        createdDatabases.add(databaseName);

        System.out.println("Testing DROP IF EXISTS CASCADE: DROP DATABASE IF EXISTS " + databaseName + " CASCADE");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setIfExists(true);
        statement.setCascade(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "DROP DATABASE IF EXISTS " + databaseName + " CASCADE";
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: DROP IF EXISTS CASCADE");
    }

    @Test
    public void testValidationMissingDatabaseName() throws Exception {
        System.out.println("Testing Validation: Missing database name should fail");

        DropDatabaseStatement statement = new DropDatabaseStatement();
        // Intentionally not setting databaseName

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing database name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("database") || e.getMessage().contains("name") || e.getMessage().contains("required"));
            System.out.println("✅ SUCCESS: Validation correctly failed for missing database name");
        }
    }

    @Test
    public void testValidationCascadeAndRestrict() throws Exception {
        String databaseName = getUniqueDatabaseName("testValidationCascadeAndRestrict");

        System.out.println("Testing Validation: Both CASCADE and RESTRICT should fail");

        DropDatabaseStatement statement = new DropDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setCascade(true);
        statement.setRestrict(true);

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for both CASCADE and RESTRICT");
        } catch (Exception e) {
            assertTrue(e.getMessage().toLowerCase().contains("cascade") || e.getMessage().toLowerCase().contains("restrict") || e.getMessage().toLowerCase().contains("mutual"));
            System.out.println("✅ SUCCESS: Validation correctly failed for both CASCADE and RESTRICT");
        }
    }

    @Test
    public void testSequentialDropOperations() throws Exception {
        String database1 = getUniqueDatabaseName("testSequential1");
        String database2 = getUniqueDatabaseName("testSequential2");
        String database3 = getUniqueDatabaseName("testSequential3");
        createdDatabases.add(database1);
        createdDatabases.add(database2);
        createdDatabases.add(database3);

        System.out.println("Testing Sequential DROP operations on multiple databases");

        // Create all databases
        PreparedStatement createStmt1 = connection.prepareStatement("CREATE DATABASE " + database1);
        createStmt1.execute();
        createStmt1.close();

        PreparedStatement createStmt2 = connection.prepareStatement("CREATE DATABASE " + database2);
        createStmt2.execute();
        createStmt2.close();

        PreparedStatement createStmt3 = connection.prepareStatement("CREATE DATABASE " + database3);
        createStmt3.execute();
        createStmt3.close();

        // Drop all databases sequentially
        String[] databases = {database1, database2, database3};
        for (String dbName : databases) {
            DropDatabaseStatement statement = new DropDatabaseStatement();
            statement.setDatabaseName(dbName);

            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            assertNotNull(sqls);
            assertEquals(1, sqls.length);

            String expectedSQL = "DROP DATABASE " + dbName;
            assertEquals(expectedSQL, sqls[0].toSql());

            // Execute against live database
            PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
            preparedStatement.execute();
            preparedStatement.close();

            System.out.println("Dropped database: " + dbName);
        }

        System.out.println("✅ SUCCESS: Sequential DROP operations");
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {
        System.out.println("Testing Unique Naming Strategy: Verifying all test databases have unique names");

        // Create multiple databases using the naming strategy
        String db1 = getUniqueDatabaseName("testMethod1");
        String db2 = getUniqueDatabaseName("testMethod2");
        String db3 = getUniqueDatabaseName("testMethod3");

        assertNotEquals(db1, db2);
        assertNotEquals(db2, db3);
        assertNotEquals(db1, db3);

        assertTrue(db1.startsWith("TEST_DROP_DB_"));
        assertTrue(db2.startsWith("TEST_DROP_DB_"));
        assertTrue(db3.startsWith("TEST_DROP_DB_"));

        System.out.println("Database 1: " + db1);
        System.out.println("Database 2: " + db2);
        System.out.println("Database 3: " + db3);

        System.out.println("✅ SUCCESS: Unique Naming Strategy validated");
    }
}