package liquibase.sqlgenerator;

import liquibase.change.ColumnConfig;
import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryAddForeignKeyConstraintGeneratorTest {

    private BigQueryAddForeignKeyConstraintGenerator generator;
    private BigQueryDatabase database;
    private AddForeignKeyConstraintStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryAddForeignKeyConstraintGenerator();
        statement = new AddForeignKeyConstraintStatement(
                "constraintName",
                "baseTableCatalogName",
                "baseTableSchemaName",
                "baseTableName",
                new ColumnConfig[]{new ColumnConfig()},
                "referencedTableCatalogName",
                "referencedTableSchemaName",
                "referencedTableName",
                new ColumnConfig[]{new ColumnConfig()});
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("SELECT 1", sql[0].toSql());
    }
}