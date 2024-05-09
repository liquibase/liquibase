package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.UpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryUpdateGeneratorTest {

    private BigQueryUpdateGenerator generator;
    private BigQueryDatabase database;
    private UpdateStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryUpdateGenerator();
        statement = new UpdateStatement("catalogName", "schemaName", "tableName");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("UPDATE schemaName.tableName SET WHERE 1 = 1", sql[0].toSql());
    }
}