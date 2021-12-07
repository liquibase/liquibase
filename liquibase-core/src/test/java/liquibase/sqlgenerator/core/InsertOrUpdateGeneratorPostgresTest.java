package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.core.PostgresDatabase;
import liquibase.sql.Sql;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.InsertOrUpdateStatement;
import liquibase.statement.core.InsertStatement;
import org.junit.Test;

import static org.junit.Assert.*;

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
        columnConfig.setValueSequenceNext(new SequenceNextValueFunction(SCHEMA_NAME, SEQUENCE_NAME));
        columnConfig.setName("col3");
        statement.addColumn(columnConfig);

        Sql[] sql = generator.generateSql( statement, postgresDatabase,  null);
        String theSql = sql[0].toSql();
        assertEquals(String.format("INSERT INTO %s.%s (col3) VALUES (nextval('%s.%s'))",SCHEMA_NAME,TABLE_NAME,SCHEMA_NAME,SEQUENCE_NAME)
                ,theSql);
    }

    @Test
    public void testInsertSequenceValWithSchemaInWholeStatement(){
        PostgresDatabase postgresDatabase = new PostgresDatabase();
        InsertGenerator generator = new InsertGenerator();
        InsertStatement statement = new InsertStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME);
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setValueSequenceNext(new SequenceNextValueFunction(SCHEMA_NAME, SEQUENCE_NAME));
        columnConfig.setName("col3");
        statement.addColumn(columnConfig);

        Sql[] sql = generator.generateSql( statement, postgresDatabase,  null);
        String theSql = sql[0].toSql();
        assertEquals(String.format("INSERT INTO %s.%s (col3) VALUES (nextval('%s.%s'))",SCHEMA_NAME,TABLE_NAME,SCHEMA_NAME,SEQUENCE_NAME)
                ,theSql);
    }

    @Test
    public void testOnlyUpdateFlag(){
        PostgresDatabase database = new PostgresDatabase();
        InsertOrUpdateGeneratorPostgres generator = new InsertOrUpdateGeneratorPostgres();
        InsertOrUpdateStatement statement = new InsertOrUpdateStatement("mycatalog", "myschema","mytable","pk_col1", true);
        statement.addColumnValue("pk_col1","value1");
        statement.addColumnValue("col2","value2");
        Sql[] sql = generator.generateSql( statement, database,  null);
        String theSql = sql[0].toSql();
        assertTrue("missing update statement", theSql.contains("UPDATE myschema.mytable SET col2 = 'value2' WHERE pk_col1 = 'value1'"));
        assertFalse("should not have had insert statement",theSql.contains("INSERT INTO myschema.mytable (pk_col1, col2) VALUES ('value1', 'value2');"));
    }
}
