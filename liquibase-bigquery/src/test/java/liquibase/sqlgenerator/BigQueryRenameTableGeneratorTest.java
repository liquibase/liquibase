package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.RenameTableStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryRenameTableGeneratorTest {

    private BigQueryRenameTableGenerator generator;
    private BigQueryDatabase database;
    private RenameTableStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryRenameTableGenerator();
        statement = new RenameTableStatement("catalogName", "schemaName", "oldTableName", "newTableName");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("ALTER TABLE schemaName.oldTableName RENAME TO newTableName", sql[0].toSql());
    }
}