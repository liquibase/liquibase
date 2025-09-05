package liquibase.integration;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.statement.core.snowflake.CreateSequenceStatementSnowflake;
import liquibase.util.TestDatabaseConfigUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify that sequence functionality is working correctly.
 * This tests the existing sequence implementation in the extension.
 */
public class SequenceFunctionalityTest {

    private Connection connection;
    private Database database;

    @BeforeEach
    public void setUp() throws Exception {
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testCreateSequenceGeneratorExists() {
        // Test that CreateSequenceGeneratorSnowflake is properly registered
        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, "test_sequence");
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sql, "SQL should be generated for CreateSequence");
        assertTrue(sql.length > 0, "Should generate at least one SQL statement");
        assertTrue(sql[0].toSql().contains("CREATE SEQUENCE"), "Should generate CREATE SEQUENCE SQL");
        
    }

    @Test
    public void testCreateSequenceSnowflakeStatementWithOrderedFlag() {
        // Test Snowflake-specific CreateSequenceStatementSnowflake with ORDER flag
        CreateSequenceStatementSnowflake statement = new CreateSequenceStatementSnowflake(null, null, "test_ordered_sequence");
        statement.setStartValue(BigInteger.valueOf(100));
        statement.setIncrementBy(BigInteger.valueOf(5));
        statement.setIfNotExists(true);
        statement.setOrder(true); // This is the key Snowflake feature
        statement.setComment("Test sequence with ordering");
        
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sql, "SQL should be generated for CreateSequenceStatementSnowflake");
        assertTrue(sql.length > 0, "Should generate at least one SQL statement");
        
        String sqlText = sql[0].toSql();
        assertTrue(sqlText.contains("CREATE SEQUENCE"), "Should generate CREATE SEQUENCE SQL");
        assertTrue(sqlText.contains("IF NOT EXISTS"), "Should include IF NOT EXISTS");
        assertTrue(sqlText.contains("START WITH 100"), "Should include start value");
        assertTrue(sqlText.contains("INCREMENT BY 5"), "Should include increment value");
        assertTrue(sqlText.contains("ORDER"), "Should include ORDER keyword");
        assertTrue(sqlText.contains("COMMENT"), "Should include comment");
        
    }

    @Test
    public void testDropSequenceGeneratorExists() {
        // Test that DropSequenceGeneratorSnowflake is properly registered
        DropSequenceStatement statement = new DropSequenceStatement(null, null, "test_sequence");
        
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sql, "SQL should be generated for DropSequence");
        assertTrue(sql.length > 0, "Should generate at least one SQL statement");
        assertTrue(sql[0].toSql().contains("DROP SEQUENCE"), "Should generate DROP SEQUENCE SQL");
        
    }

    @Test
    public void testSequenceValidation() {
        // Test validation logic for mutually exclusive options
        CreateSequenceStatementSnowflake statement = new CreateSequenceStatementSnowflake(null, null, "test_sequence");
        statement.setOrReplace(true);
        statement.setIfNotExists(true); // This should cause validation error
        
        assertThrows(RuntimeException.class, () -> {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
        }, "Should throw validation error for OR REPLACE + IF NOT EXISTS");
        
    }
}