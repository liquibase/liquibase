package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import static org.junit.Assert.*;
import org.junit.Test;

public class AddUniqueConstraintChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Add Unique Constraint", new AddUniqueConstraintChange().getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {

//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                AddUniqueConstraintChange change = new AddUniqueConstraintChange();
//                change.setSchemaName("SCHEMA_NAME");
//                change.setTableName("TABLE_NAME");
//                change.setColumnNames("COL_HERE");
//                change.setConstraintName("PK_NAME");
//                change.setTablespace("TABLESPACE_NAME");
//
//                SqlStatement[] sqlStatements = change.generateStatements(database);
//                assertEquals(1, sqlStatements.length);
//                assertTrue(sqlStatements[0] instanceof AddUniqueConstraintStatement);
//
//                assertEquals("SCHEMA_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getSchemaName());
//                assertEquals("TABLE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTableName());
//                assertEquals("COL_HERE", ((AddUniqueConstraintStatement) sqlStatements[0]).getColumnNames());
//                assertEquals("PK_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getConstraintName());
//                assertEquals("TABLESPACE_NAME", ((AddUniqueConstraintStatement) sqlStatements[0]).getTablespace());
//
//            }
//        });
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL_HERE");

        assertEquals("Unique constraint added to TABLE_NAME(COL_HERE)", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }
}
