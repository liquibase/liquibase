package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.statement.PrimaryKeyConstraint;
import liquibase.statement.core.AddColumnStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryAddColumnGeneratorTest {

    private BigQueryAddColumnGenerator generator;
    private BigQueryDatabase database;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryAddColumnGenerator();
    }

    @Test
    void generateSingleColumnSQL() {
        AddColumnStatement statement = new AddColumnStatement(null, null, null, "columnName", "NUMERIC", null);
        String sql = generator.generateSingleColumnSQL(statement, database);
        assertEquals(" ADD COLUMN columnName NUMERIC", sql);
    }

    @Test
    void generateSingleColumnSQLForPrimaryKey() {
        AddColumnStatement statement = new AddColumnStatement("catalog", "schema", "table", "columnName", "NUMERIC", null, new PrimaryKeyConstraint());
        String sql = generator.generateSingleColumnSQL(statement, database);
        assertEquals(" ADD COLUMN columnName NUMERIC; ALTER TABLE schema.table ADD PRIMARY KEY (columnName) NOT ENFORCED", sql);
    }
}