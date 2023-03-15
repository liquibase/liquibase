package liquibase.sqlgenerator.core;

import liquibase.change.AddColumnConfig;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateIndexStatement;
import org.junit.Assert;
import org.junit.Test;

public class CreateIndexGeneratorPostgresTest {
    @Test
    public void generateSql_IndexWithDescSorting_Created() {
        // Given
        CreateIndexStatement statement = newStatement("INDEX1", "CATALOG1", "SCHEMA1", "TABLE1", newAddColumnConfig("COL1", false), newAddColumnConfig("COL2", true));

        // When
        Sql[] result = generateSql(statement);

        // Then
        Assert.assertEquals("CREATE INDEX INDEX1 ON SCHEMA1.TABLE1(COL1, COL2 DESC)", result[0].toSql());
    }

    private static CreateIndexStatement newStatement(String indexName, String catalogName, String schemaName, String tableName, AddColumnConfig... columns) {
        return new CreateIndexStatement(indexName, catalogName, schemaName, tableName, false, "" , columns);
    }

    private static AddColumnConfig newAddColumnConfig(String name, Boolean descending) {
        AddColumnConfig column = new AddColumnConfig();
        column.setName(name);
        column.setDescending(descending);
        return column;
    }

    private static Sql[] generateSql(CreateIndexStatement statement) {
        return new CreateIndexGeneratorPostgres().generateSql(statement, new PostgresDatabase(), null);
    }
}
