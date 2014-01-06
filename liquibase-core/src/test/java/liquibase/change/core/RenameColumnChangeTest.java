package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameColumnStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RenameColumnChange}
 */
public class RenameColumnChangeTest extends StandardChangeTest {

    RenameColumnChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameColumnChange();

        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldColumnName("oldColName");
        refactoring.setNewColumnName("newColName");
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("renameColumn", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameColumnStatement);
        assertEquals("SCHEMA_NAME", ((RenameColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((RenameColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("oldColName", ((RenameColumnStatement) sqlStatements[0]).getOldColumnName());
        assertEquals("newColName", ((RenameColumnStatement) sqlStatements[0]).getNewColumnName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Column TABLE_NAME.oldColName renamed to newColName", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase;
    }
}
