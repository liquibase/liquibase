package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.AlterDatabaseStatement;
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
 * Integration tests for AlterDatabaseGeneratorSnowflake.
 * Tests all ALTER DATABASE SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 */
public class AlterDatabaseGeneratorSnowflakeIntegrationTest {

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
        return "TEST_ALTER_DB_" + methodName;
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
                System.out.println("Cleaned up database: " + databaseName);
            } catch (SQLException e) {
                System.err.println("Failed to cleanup database " + databaseName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testRenameDatabase() throws Exception {
        String originalName = getUniqueDatabaseName("testRenameOriginal");
        String newName = getUniqueDatabaseName("testRenameNew");
        createdDatabases.add(originalName);
        createdDatabases.add(newName); // Track both names for cleanup

        System.out.println("Testing RENAME: ALTER DATABASE " + originalName + " RENAME TO " + newName);

        // First create the original database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + originalName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(originalName);
        statement.setNewName(newName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "ALTER DATABASE " + originalName + " RENAME TO " + newName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: RENAME DATABASE");
    }

    @Test
    public void testRenameWithIfExists() throws Exception {
        String originalName = getUniqueDatabaseName("testRenameIfExistsOriginal");
        String newName = getUniqueDatabaseName("testRenameIfExistsNew");
        createdDatabases.add(originalName);
        createdDatabases.add(newName);

        System.out.println("Testing RENAME IF EXISTS: ALTER DATABASE IF EXISTS " + originalName + " RENAME TO " + newName);

        // First create the original database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + originalName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(originalName);
        statement.setNewName(newName);
        statement.setIfExists(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String expectedSQL = "ALTER DATABASE IF EXISTS " + originalName + " RENAME TO " + newName;
        assertEquals(expectedSQL, sqls[0].toSql());

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: RENAME IF EXISTS");
    }

    @Test
    public void testSetDataRetention() throws Exception {
        String databaseName = getUniqueDatabaseName("testSetDataRetention");
        createdDatabases.add(databaseName);

        System.out.println("Testing SET DATA_RETENTION_TIME_IN_DAYS: ALTER DATABASE " + databaseName + " SET DATA_RETENTION_TIME_IN_DAYS = 14");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setNewDataRetentionTimeInDays("14");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
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
        String databaseName = getUniqueDatabaseName("testSetComment");
        createdDatabases.add(databaseName);

        System.out.println("Testing SET COMMENT: ALTER DATABASE " + databaseName + " SET COMMENT = 'Updated comment'");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setNewComment("Updated comment for integration testing");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
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
        String databaseName = getUniqueDatabaseName("testSetMultipleProperties");
        createdDatabases.add(databaseName);

        System.out.println("Testing SET Multiple Properties: ALTER DATABASE " + databaseName + " SET multiple properties");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setNewDataRetentionTimeInDays("21");
        statement.setNewMaxDataExtensionTimeInDays("28");
        statement.setNewComment("Multi-property test database");
        statement.setNewDefaultDdlCollation("utf8");

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("21"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("28"));
        assertTrue(sql.contains("COMMENT"));
        assertTrue(sql.contains("'Multi-property test database'"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"));
        assertTrue(sql.contains("'utf8'"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: SET Multiple Properties");
    }

    @Test
    public void testUnsetDataRetention() throws Exception {
        String databaseName = getUniqueDatabaseName("testUnsetDataRetention");
        createdDatabases.add(databaseName);

        System.out.println("Testing UNSET DATA_RETENTION_TIME_IN_DAYS: ALTER DATABASE " + databaseName + " UNSET DATA_RETENTION_TIME_IN_DAYS");

        // First create the database with data retention
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName + " DATA_RETENTION_TIME_IN_DAYS = 7");
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setUnsetDataRetentionTimeInDays(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
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
        String databaseName = getUniqueDatabaseName("testUnsetComment");
        createdDatabases.add(databaseName);

        System.out.println("Testing UNSET COMMENT: ALTER DATABASE " + databaseName + " UNSET COMMENT");

        // First create the database with comment
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName + " COMMENT = 'Initial comment'");
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setUnsetComment(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("COMMENT"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: UNSET COMMENT");
    }

    @Test
    public void testUnsetMultipleProperties() throws Exception {
        String databaseName = getUniqueDatabaseName("testUnsetMultipleProperties");
        createdDatabases.add(databaseName);

        System.out.println("Testing UNSET Multiple Properties: ALTER DATABASE " + databaseName + " UNSET multiple properties");

        // First create the database with multiple properties
        PreparedStatement createStmt = connection.prepareStatement(
            "CREATE DATABASE " + databaseName + " " +
            "DATA_RETENTION_TIME_IN_DAYS = 14 " +
            "MAX_DATA_EXTENSION_TIME_IN_DAYS = 21 " +
            "COMMENT = 'Test database with properties'"
        );
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
        statement.setUnsetDataRetentionTimeInDays(true);
        statement.setUnsetMaxDataExtensionTimeInDays(true);
        statement.setUnsetComment(true);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER DATABASE " + databaseName));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("COMMENT"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: UNSET Multiple Properties");
    }

    @Test
    public void testValidationMissingDatabaseName() throws Exception {
        System.out.println("Testing Validation: Missing database name should fail");

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        // Intentionally not setting databaseName
        statement.setNewComment("This should fail");

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing database name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("database") || e.getMessage().contains("name") || e.getMessage().contains("required"));
            System.out.println("✅ SUCCESS: Validation correctly failed for missing database name");
        }
    }

    @Test
    public void testValidationNoProperties() throws Exception {
        String databaseName = getUniqueDatabaseName("testValidationNoProperties");
        createdDatabases.add(databaseName);

        System.out.println("Testing Validation: No alteration properties should fail");

        // First create the database
        PreparedStatement createStmt = connection.prepareStatement("CREATE DATABASE " + databaseName);
        createStmt.execute();
        createStmt.close();

        AlterDatabaseStatement statement = new AlterDatabaseStatement();
        statement.setDatabaseName(databaseName);
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
        System.out.println("Testing Unique Naming Strategy: Verifying all test databases have unique names");

        // Create multiple databases using the naming strategy
        String db1 = getUniqueDatabaseName("testMethod1");
        String db2 = getUniqueDatabaseName("testMethod2");
        String db3 = getUniqueDatabaseName("testMethod3");

        assertNotEquals(db1, db2);
        assertNotEquals(db2, db3);
        assertNotEquals(db1, db3);

        assertTrue(db1.startsWith("TEST_ALTER_DB_"));
        assertTrue(db2.startsWith("TEST_ALTER_DB_"));
        assertTrue(db3.startsWith("TEST_ALTER_DB_"));

        System.out.println("Database 1: " + db1);
        System.out.println("Database 2: " + db2);
        System.out.println("Database 3: " + db3);

        System.out.println("✅ SUCCESS: Unique Naming Strategy validated");
    }
}