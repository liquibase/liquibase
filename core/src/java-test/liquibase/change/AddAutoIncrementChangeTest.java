package liquibase.change;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.sql.AddAutoIncrementStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AddAutoIncrementChangeTest {

    @Test
    public void constructor() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        assertEquals("addAutoIncrement", change.getTagName());
        assertEquals("Set Column as Auto-Increment", change.getChangeName());
    }
    
    @Test
    public void generateStatements() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddAutoIncrementChange change = new AddAutoIncrementChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnName("COLUMN_NAME");
                change.setColumnDataType("DATATYPE(255)");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                if (database instanceof PostgresDatabase) {
                    assertEquals(3, sqlStatements.length);
                    //todo: improve test as statements are no longer raw statements
                    assertTrue(sqlStatements[0] instanceof RawSqlStatement);
                    assertTrue(sqlStatements[1] instanceof RawSqlStatement);
                    assertTrue(sqlStatements[2] instanceof RawSqlStatement);
                } else {
                    assertEquals(1, sqlStatements.length);
                    assertTrue(sqlStatements[0] instanceof AddAutoIncrementStatement);
                    assertEquals("SCHEMA_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getSchemaName());
                    assertEquals("TABLE_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getTableName());
                    assertEquals("COLUMN_NAME", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnName());
                    assertEquals("DATATYPE(255)", ((AddAutoIncrementStatement) sqlStatements[0]).getColumnDataType());
                }
            }
        });
    }

}
