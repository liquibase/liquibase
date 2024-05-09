package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.DeleteStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryDeleteGeneratorTest {

    private BigQueryDeleteGenerator generator;
    private BigQueryDatabase database;
    private DeleteStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryDeleteGenerator();
        statement = new DeleteStatement("catalog", "schema", "table");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("DELETE FROM schema.table WHERE 1 = 1", sql[0].toSql());
    }
}