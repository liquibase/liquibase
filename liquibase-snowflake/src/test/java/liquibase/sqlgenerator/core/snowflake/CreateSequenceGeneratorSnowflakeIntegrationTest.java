package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.CreateSequenceStatement;
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
    private String testDatabase; // Will be set from YAML configuration
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
        // Use YAML configuration instead of environment variables
        connection = TestDatabaseConfigUtil.getSnowflakeConnection();
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        
        // Get database name from the connection
        testDatabase = database.getDefaultCatalogName();
        if (testDatabase == null) {
            testDatabase = "LB_DBEXT_INT_DB"; // Fallback to YAML configured database
        }
        
        // Create test schema for sequence isolation
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
        // MANDATORY: Cleanup all created sequences using unique names
        for (String sequenceName : createdSequences) {
            try {
                PreparedStatement dropStmt = connection.prepareStatement("DROP SEQUENCE IF EXISTS " + testDatabase + "." + testSchema + "." + sequenceName);
                dropStmt.execute();
                dropStmt.close();
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

    }

    @Test
    public void testSequenceWithStartValue() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithStartValue");
        createdSequences.add(sequenceName);


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

    }

    @Test
    public void testSequenceWithIncrement() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithIncrement");
        createdSequences.add(sequenceName);


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

    }

    @Test
    public void testSequenceWithMinMaxValues() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithMinMaxValues");


        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setMinValue(java.math.BigInteger.valueOf(1));
        statement.setMaxValue(java.math.BigInteger.valueOf(1000));

        // Snowflake doesn't support MINVALUE/MAXVALUE - should get validation error
        try {
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for unsupported MINVALUE/MAXVALUE in Snowflake");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("minValue") || e.getMessage().contains("maxValue"), "Assertion should be true");        }
    }

    @Test
    public void testSequenceWithCycle() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithCycle");


        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, sequenceName);
        statement.setCycle(true);

        // Snowflake doesn't support CYCLE - should get validation error
        try {
            Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for unsupported CYCLE in Snowflake");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("cycle"), "Assertion should be true");        }
    }

    @Test
    public void testSequenceWithSupportedProperties() throws Exception {
        String sequenceName = getUniqueSequenceName("testSequenceWithSupportedProperties");
        createdSequences.add(sequenceName);


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

    }

    @Test
    public void testValidationMissingSequenceName() throws Exception {

        CreateSequenceStatement statement = new CreateSequenceStatement(null, null, null);

        try {
            SqlGeneratorFactory.getInstance().generateSql(statement, database);
            fail("Expected validation error for missing sequence name");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("sequence") || e.getMessage().contains("name") || e.getMessage().contains("required"));
        }
    }

    @Test
    public void testUniqueNamingStrategy() throws Exception {

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


    }
}