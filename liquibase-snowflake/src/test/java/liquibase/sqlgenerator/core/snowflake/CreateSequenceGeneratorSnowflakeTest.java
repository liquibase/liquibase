package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.sqlgenerator.core.CreateSequenceGeneratorSnowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class CreateSequenceGeneratorSnowflakeTest {

    private CreateSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private CreateSequenceStatement statement;

    @BeforeEach
    void setUp() {
        generator = new CreateSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new CreateSequenceStatement(null, null, "test_sequence");
    }

    @Test
    void testSupports() {
        assertTrue(generator.supports(statement, database));
        assertFalse(generator.supports(statement, new liquibase.database.core.PostgresDatabase()));
    }

    @Test
    void testPriority() {
        assertEquals(generator.PRIORITY_DATABASE, generator.getPriority());
    }

    @Test
    void testGenerateBasicSequenceSql() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        assertEquals("CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1", sql[0].toSql());
    }

    @Test
    void testGenerateSequenceWithOrderedTrue() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        statement.setOrdered(true);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        assertEquals("CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 ORDER", sql[0].toSql());
    }

    @Test
    void testGenerateSequenceWithOrderedFalse() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        statement.setOrdered(false);
        
        Sql[] sql = generator.generateSql(statement, database, null);
        assertNotNull(sql);
        assertEquals(1, sql.length);
        
        assertEquals("CREATE SEQUENCE test_sequence START WITH 1 INCREMENT BY 1 NOORDER", sql[0].toSql());
    }

    @Test
    void testValidationWithUnsupportedFeatures() {
        statement.setMinValue(BigInteger.valueOf(1));
        statement.setMaxValue(BigInteger.valueOf(1000));
        statement.setCycle(true);
        statement.setCacheSize(BigInteger.valueOf(20));
        statement.setDataType("INT");
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertTrue(errors.hasErrors());
        
        // Should have errors for unsupported Snowflake features
        String errorString = errors.toString();
        assertTrue(errorString.contains("minValue") || errorString.contains("maxValue") || 
                  errorString.contains("cycle") || errorString.contains("cacheSize") || 
                  errorString.contains("datatype"));
    }

    @Test
    void testValidationWithRequiredFields() {
        CreateSequenceStatement nullNameStatement = new CreateSequenceStatement(null, null, null);
        
        ValidationErrors errors = generator.validate(nullNameStatement, database, null);
        assertNotNull(errors);
        assertTrue(errors.hasErrors());
    }

    @Test
    void testValidationSuccess() {
        statement.setStartValue(BigInteger.valueOf(1));
        statement.setIncrementBy(BigInteger.valueOf(1));
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertNotNull(errors);
        assertFalse(errors.hasErrors());
    }
}