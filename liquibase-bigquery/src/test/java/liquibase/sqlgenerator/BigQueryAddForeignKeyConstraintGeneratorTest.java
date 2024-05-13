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
                new ColumnConfig[]{ColumnConfig.fromName("baseColumn1"), ColumnConfig.fromName("baseColumn2")},
                "referencedTableCatalogName",
                "referencedTableSchemaName",
                "referencedTableName",
                new ColumnConfig[]{ColumnConfig.fromName("referenceColumn1"), ColumnConfig.fromName("referenceColumn2")});
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("ALTER TABLE baseTableSchemaName.baseTableName ADD CONSTRAINT constraintName FOREIGN KEY (baseColumn1, baseColumn2) REFERENCES " +
                "referencedTableSchemaName.referencedTableName (referenceColumn1, referenceColumn2) NOT ENFORCED", sql[0].toSql());
    }
}