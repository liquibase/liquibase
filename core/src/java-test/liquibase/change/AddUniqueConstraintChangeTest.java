package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.statement.SqlStatement;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AddUniqueConstraintChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Add Unique Constraint", new AddUniqueConstraintChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {

        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                AddUniqueConstraintChange change = new AddUniqueConstraintChange();
                change.setSchemaName("SCHEMA_NAME");
                change.setTableName("TABLE_NAME");
                change.setColumnNames("COL_HERE");
                change.setConstraintName("PK_NAME");
                change.setTablespace("TABLESPACE_NAME");

                SqlStatement[] sqlStatements = change.generateStatements(database);
                assertEquals(1, sqlStatements.length);
                assertTrue(sqlStatements[0] instanceof AddUniqueConstraintStatement);

                assertEquals("SCHEMA_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getSchemaName());
                assertEquals("TABLE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTableName());
                assertEquals("COL_HERE", ((AddUniqueConstraintStatement) sqlStatements[0]).getColumnNames());
                assertEquals("PK_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getConstraintName());
                assertEquals("TABLESPACE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTablespace());

            }
        });
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        assertEquals("Unique constraint added to TABLE_NAME(COL_HERE)", change.getConfirmationMessage());
    }

}
