package liquibase.sqlgenerator;

import liquibase.change.ColumnConfig;
import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.AddPrimaryKeyStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryAddPrimaryKeyConstraintGeneratorTest {

    private BigQueryAddPrimaryKeyConstraintGenerator generator;
    private BigQueryDatabase database;
    private AddPrimaryKeyStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryAddPrimaryKeyConstraintGenerator();
        statement = new AddPrimaryKeyStatement(
                "catalogName",
                "schemaName",
                "tableName",
                new ColumnConfig[]{ColumnConfig.fromName("baseColumn1"), ColumnConfig.fromName("baseColumn2")},
                "constraintName");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("ALTER TABLE schemaName.tableName ADD PRIMARY KEY (baseColumn1, baseColumn2) NOT ENFORCED", sql[0].toSql());
    }
}