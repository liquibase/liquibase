package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.Database;
import liquibase.database.SybaseASADatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.statement.DropDefaultValueStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DropDefaultValueChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Default Value", new DropDefaultValueChange().getChangeMetaData().getDescription());
    }

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
