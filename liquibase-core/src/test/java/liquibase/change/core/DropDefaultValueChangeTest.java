package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropDefaultValueStatement;
import static org.junit.Assert.*;
import org.junit.Test;

public class DropDefaultValueChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Default Value", new DropDefaultValueChange().getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropDefaultValueStatement);
        assertEquals("SCHEMA_NAME", ((DropDefaultValueStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropDefaultValueStatement) sqlStatements[0]).getTableName());
        assertEquals("COL_HERE", ((DropDefaultValueStatement) sqlStatements[0]).getColumnName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Default value dropped from TABLE_NAME.COL_HERE", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }

}
