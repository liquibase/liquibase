package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateSequenceStatement;
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
 * Integration tests for CreateSequenceGeneratorSnowflake.
 * Tests all CREATE SEQUENCE SQL variations against live Snowflake database.
 * 
 * ADDRESSES_CORE_ISSUE: Integration tests ALL generated SQL with parallel execution capability.
 * NOTE: Sequences have schema isolation for parallel execution.
 */
public class CreateSequenceGeneratorSnowflakeIntegrationTest {

    private Database database;
    private Connection connection;
    private List<String> createdSequences = new ArrayList<>();
    private String testDatabase = "TEST_INTEGRATION_DB";
    private String testSchema = "TEST_SEQUENCE_SCHEMA";

    /**
     * CRITICAL: Generates unique sequence name based on test method name.
     * ADDRESSES_CORE_ISSUE: Schema-level object naming conflicts preventing parallel execution.
     * 
     * @param methodName The test method name
     * @return Unique sequence name for parallel execution
     */
    private String getUniqueSequenceName(String methodName) {
        return "TEST_CREATE_SEQ_" + methodName;
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
        
        // Create test database and schema for sequence isolation
        try {
            PreparedStatement createDbStmt = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS " + testDatabase);
            createDbStmt.execute();
            createDbStmt.close();
            
            PreparedStatement useDbStmt = connection.prepareStatement("USE DATABASE " + testDatabase);
            useDbStmt.execute();
            useDbStmt.close();
            
            PreparedStatement createSchemaStmt = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + testSchema);
            createSchemaStmt.execute();
            createSchemaStmt.close();
            
            PreparedStatement useSchemaStmt = connection.prepareStatement("USE SCHEMA " + testSchema);
            useSchemaStmt.execute();
            useSchemaStmt.close();
        } catch (SQLException e) {
            System.out.println("Database/schema already exists or creation failed: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        // MANDATORY: Cleanup all created sequences using unique names
        for (String sequenceName : createdSequences) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP SEQUENCE IF EXISTS " + testDatabase + "." + testSchema + "." + sequenceName);
                dropStmt.execute();
                dropStmt.close();
                System.out.println("Cleaned up sequence: " + testDatabase + "." + testSchema + "." + sequenceName);
            } catch (SQLException e) {
                System.err.println("Failed to cleanup sequence " + sequenceName + ": " + e.getMessage());
            }
        }
        
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testBasicRequiredOnly() throws Exception {
        String sequenceName = getUniqueSequenceName("testBasicRequiredOnly");
        createdSequences.add(sequenceName);

        System.out.println("Testing Basic Required Only: CREATE SEQUENCE " + sequenceName);

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SEQUENCE") && sql.contains(sequenceName));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sqls[0].toSql());
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Basic Required Only");
    }

    @Test
    public void testSequenceWithStartValue() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithStartValue");
        createdSequences.add(sequenceName);

        System.out.println("Testing WITH START VALUE: CREATE SEQUENCE " + sequenceName + " START WITH 100");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setStartValue(java.math.BigInteger.valueOf(100));

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SEQUENCE") && sql.contains(sequenceName));
        assertTrue(sql.contains("START WITH 100") || sql.contains("START 100"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: WITH START VALUE");
    }

    @Test
    public void testSequenceWithIncrement() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithIncrement");
        createdSequences.add(sequenceName);

        System.out.println("Testing WITH INCREMENT: CREATE SEQUENCE " + sequenceName + " INCREMENT BY 5");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setIncrementBy(java.math.BigInteger.valueOf(5));

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SEQUENCE") && sql.contains(sequenceName));
        assertTrue(sql.contains("INCREMENT BY 5") || sql.contains("INCREMENT 5"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: WITH INCREMENT");
    }

    @Test
    public void testSequenceWithMinMaxValues() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithMinMaxValues");

        System.out.println("Testing MIN/MAX VALUES validation (should fail): CREATE SEQUENCE " + sequenceName + " MINVALUE 1 MAXVALUE 1000");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setMinValue(java.math.BigInteger.valueOf(1));
        statement.setMaxValue(java.math.BigInteger.valueOf(1000));

        // Snowflake doesn't support MINVALUE/MAXVALUE - should get validation error
        try {
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for unsupported MINVALUE/MAXVALUE in Snowflake");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("minValue") || e.getMessage().contains("maxValue"), 
                      "Expected validation error about minValue or maxValue, got: " + e.getMessage());
            System.out.println("✅ SUCCESS: MIN/MAX VALUES correctly rejected");
        }
    }

    @Test
    public void testSequenceWithCycle() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithCycle");

        System.out.println("Testing CYCLE validation (should fail): CREATE SEQUENCE " + sequenceName + " CYCLE");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setCycle(true);

        // Snowflake doesn't support CYCLE - should get validation error
        try {
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for unsupported CYCLE in Snowflake");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("cycle"), 
                      "Expected validation error about cycle, got: " + e.getMessage());
            System.out.println("✅ SUCCESS: CYCLE correctly rejected");
        }
    }

    @Test
    public void testSequenceWithSupportedProperties() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithSupportedProperties");
        createdSequences.add(sequenceName);

        System.out.println("Testing Supported Properties: CREATE SEQUENCE " + sequenceName + " with START WITH and INCREMENT BY");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setStartValue(java.math.BigInteger.valueOf(10));
        statement.setIncrementBy(java.math.BigInteger.valueOf(2));

        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        assertNotNull(sqls);
        assertEquals(1, sqls.length);

        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SEQUENCE") && sql.contains(sequenceName));
        assertTrue(sql.contains("START WITH 10") || sql.contains("START 10"));
        assertTrue(sql.contains("INCREMENT BY 2") || sql.contains("INCREMENT 2"));

        // Execute against live database
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.execute();
        preparedStatement.close();

        System.out.println("✅ SUCCESS: Supported Properties");
    }

    @Test
    public void testValidationMissingSequenceName() throws Exception {
        System.out.println("Testing Validation: Missing sequence name should fail");

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, null);

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing sequence name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("sequence") || e.getMessage().contains("name") || e.getMessage().contains("required"));
            System.out.println("✅ SUCCESS: Validation correctly failed for missing sequence name");
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {
        System.out.println("Testing Unique Naming Strategy: Verifying all test sequences have unique names");

        // Create multiple sequences using the naming strategy
        String seq1 = getUniqueSequenceName("testMethod1");
        String seq2 = getUniqueSequenceName("testMethod2");
        String seq3 = getUniqueSequenceName("testMethod3");

        assertNotEquals(seq1, seq2);
        assertNotEquals(seq2, seq3);
        assertNotEquals(seq1, seq3);

        assertTrue(seq1.startsWith("TEST_CREATE_SEQ_"));
        assertTrue(seq2.startsWith("TEST_CREATE_SEQ_"));
        assertTrue(seq3.startsWith("TEST_CREATE_SEQ_"));

        System.out.println("Sequence 1: " + seq1);
        System.out.println("Sequence 2: " + seq2);
        System.out.println("Sequence 3: " + seq3);

        System.out.println("✅ SUCCESS: Unique Naming Strategy validated");
    }
}