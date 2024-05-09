package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.ModifyDataTypeStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigQueryModifyDataTypeGeneratorTest {

    private BigQueryModifyDataTypeGenerator generator;
    private BigQueryDatabase database;
    private ModifyDataTypeStatement statement;

    @BeforeEach
    void setUp() {
        database = new BigQueryDatabase();
        generator = new BigQueryModifyDataTypeGenerator();
        statement = new ModifyDataTypeStatement("catalogName", "schemaName", "tableName", "columnName", "newDataType");
    }

    @Test
    void generateSql() {
        Sql[] sql = generator.generateSql(statement, database, null);
        assertEquals(1, sql.length);
        assertEquals(";", sql[0].getEndDelimiter());
        assertEquals("ALTER TABLE schemaName.tableName ALTER COLUMN columnName SET DATA TYPE NEWDATATYPE", sql[0].toSql());
    }
}