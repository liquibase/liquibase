package liquibase.sqlgenerator;

import liquibase.change.ColumnConfig;
import liquibase.database.BigQueryDatabase;
import liquibase.statement.core.InsertOrUpdateStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BigQueryInsertOrUpdateGeneratorTest {

    private BigQueryInsertOrUpdateGenerator generator;
    private BigQueryDatabase database;
    private InsertOrUpdateStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryInsertOrUpdateGenerator();
        statement = new InsertOrUpdateStatement("catalog", "schema", "table", "column", false);
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName("columnName");
        columnConfig.setType("columnType");
        columnConfig.setValueBoolean("valueBoolean");
        statement.addColumn(columnConfig);
    }

    @Test
    void getInsertStatement() {
        String insertStatement = generator.getInsertStatement(statement, database, null);
        assertNotNull(insertStatement);
        assertEquals("INSERT (columnName) VALUES (valueBoolean)", insertStatement);
    }

    @Test
    void getUpdateStatement() {
        String updateStatement = generator.getUpdateStatement(statement, database, "", null);
        assertNotNull(updateStatement);
        assertEquals("UPDATE SET columnName = valueBoolean", updateStatement);
    }

    @Test
    void getRecordEmptyCheck() {
        String recordCheck = generator.getRecordCheck(statement, database, null);
        assertNotNull(recordCheck);
        assertEquals("MERGE INTO table USING (SELECT 1) ON WHERE 1 = 1 WHEN NOT MATCHED THEN ", recordCheck);
    }

    @Test
    void getRecordWhereCheck() {
        String recordCheck = generator.getRecordCheck(statement, database, "WHERE ID = 1");
        assertNotNull(recordCheck);
        assertEquals("MERGE INTO table USING (SELECT 1) ON WHERE ID = 1 WHEN NOT MATCHED THEN ", recordCheck);
    }
}