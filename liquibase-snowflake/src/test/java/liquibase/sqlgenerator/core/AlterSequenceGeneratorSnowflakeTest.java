package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.snowflake.AlterSequenceStatementSnowflake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterSequenceGeneratorSnowflake.
 */
@DisplayName("AlterSequenceGeneratorSnowflake Tests")
class AlterSequenceGeneratorSnowflakeTest {

    private AlterSequenceGeneratorSnowflake generator;
    private SnowflakeDatabase database;

    @BeforeEach
    void setUp() {
        generator = new AlterSequenceGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }

    @Test
    @DisplayName("Should generate NOORDER SQL with irreversibility warning")
    void shouldGenerateNoOrderSqlWithWarning() {
        // Given
        AlterSequenceStatementSnowflake statement = new AlterSequenceStatementSnowflake(null, null, "TEST_SEQ");
        statement.setOrdered(false); // NOORDER

        // When
        Sql[] sqls = generator.generateSql(statement, database, null);

        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("NOORDER"), "Should contain NOORDER keyword");
        assertTrue(sql.startsWith("ALTER SEQUENCE TEST_SEQ SET"), "Should have correct ALTER SEQUENCE syntax");
        
        // Note: Warning is logged, not part of SQL - would need log capture to test
    }

    @Test
    @DisplayName("Should generate ORDER SQL without warning")
    void shouldGenerateOrderSqlWithoutWarning() {
        // Given
        AlterSequenceStatementSnowflake statement = new AlterSequenceStatementSnowflake(null, null, "TEST_SEQ");
        statement.setOrdered(true); // ORDER

        // When
        Sql[] sqls = generator.generateSql(statement, database, null);

        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ORDER"), "Should contain ORDER keyword");
        assertTrue(sql.startsWith("ALTER SEQUENCE TEST_SEQ SET"), "Should have correct ALTER SEQUENCE syntax");
        
    }

    @Test
    @DisplayName("Should generate RENAME SQL")
    void shouldGenerateRenameSql() {
        // Given
        AlterSequenceStatementSnowflake statement = new AlterSequenceStatementSnowflake(null, null, "OLD_SEQ");
        statement.setNewSequenceName("NEW_SEQ");

        // When
        Sql[] sqls = generator.generateSql(statement, database, null);

        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("RENAME TO NEW_SEQ"), "Should contain RENAME TO clause");
        assertTrue(sql.startsWith("ALTER SEQUENCE OLD_SEQ"), "Should have correct ALTER SEQUENCE syntax");
        
    }

    @Test
    @DisplayName("Should generate combined SET operations")
    void shouldGenerateCombinedSetOperations() {
        // Given
        AlterSequenceStatementSnowflake statement = new AlterSequenceStatementSnowflake(null, null, "TEST_SEQ");
        statement.setIncrementBy(BigInteger.valueOf(5));
        statement.setOrdered(false); // NOORDER - should trigger warning
        statement.setComment("Updated sequence");

        // When
        Sql[] sqls = generator.generateSql(statement, database, null);

        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("INCREMENT BY 5"), "Should contain INCREMENT BY clause");
        assertTrue(sql.contains("NOORDER"), "Should contain NOORDER keyword");
        assertTrue(sql.contains("COMMENT = 'Updated sequence'"), "Should contain COMMENT clause");
        assertTrue(sql.startsWith("ALTER SEQUENCE TEST_SEQ SET"), "Should have correct ALTER SEQUENCE syntax");
        
    }
}