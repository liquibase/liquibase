package liquibase.change;

import liquibase.database.Database;
import liquibase.database.MockDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.statement.DropPrimaryKeyStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DropPrimaryKeyChangeTest extends AbstractChangeTest {
        @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Primary Key", new DropPrimaryKeyChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropPrimaryKeyStatement);
        assertEquals("SCHEMA_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getTableName());
        assertEquals("PK_NAME", ((DropPrimaryKeyStatement) sqlStatements[0]).getConstraintName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setConstraintName("PK_NAME");
        assertEquals("Primary key dropped from TABLE_NAME", change.getConfirmationMessage());

    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }
}
