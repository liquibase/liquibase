package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.RenameViewStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryRenameViewGeneratorTest {

    private BigQueryRenameViewGenerator generator;
    private BigQueryDatabase database;
    private RenameViewStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryRenameViewGenerator();
        statement = new RenameViewStatement("catalogName", "schemaName", "oldTableName", "newTableName");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("ALTER VIEW schemaName.oldTableName RENAME TO schemaName.newTableName", sql[0].toSql());
    }
}