package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.InsertStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InsertOrUpdateGeneratorPostgresTest {
    private static final String CATALOG_NAME = "mycatalog";
    private static final String SCHEMA_NAME = "myschema";
    private static final String TABLE_NAME = "mytable";
    private static final String SEQUENCE_NAME = "my_sequence";

    @Test
    public void testInsertSequenceValWithSchema(){
        PostgresDatabase postgresDatabase = new PostgresDatabase();
        InsertGenerator generator = new InsertGenerator();
        InsertStatement statement = new InsertStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setValueSequenceNext(new SequenceNextValueFunction(SEQUENCE_NAME, SCHEMA_NAME));
        columnConfig.setName("col3");
        statement.addColumn(columnConfig);

        Sql[] sql = generator.generateSql( statement, postgresDatabase,  null);
        String theSql = sql[0].toSql();
        assertEquals(String.format("INSERT INTO %s.%s (col3) VALUES (nextval('%s.%s'))",SCHEMA_NAME,TABLE_NAME,SCHEMA_NAME,SEQUENCE_NAME)
                ,theSql);
    }
}
