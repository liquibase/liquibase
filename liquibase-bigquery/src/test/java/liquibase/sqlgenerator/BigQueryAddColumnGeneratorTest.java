package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.statement.core.AddColumnStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryAddColumnGeneratorTest {

    private BigQueryAddColumnGenerator generator;
    private BigQueryDatabase database;
    private AddColumnStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryAddColumnGenerator();
        statement = new AddColumnStatement();
    }

    @Test
    void generateSingleColumnSQL() {
        String sql = generator.generateSingleColumnSQL(statement, database);
        assertEquals(" ADD COLUMN null", sql);
    }
}