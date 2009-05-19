package liquibase.change;

import liquibase.database.*;
import liquibase.statement.RenameViewStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class RenameViewChangeTest extends AbstractChangeTest {

    private RenameViewChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameViewChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Rename View", refactoring.getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        RenameViewChange refactoring = new RenameViewChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");


        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameViewStatement);
        assertEquals("SCHEMA_NAME", ((RenameViewStatement) sqlStatements[0]).getSchemaName());
        assertEquals("OLD_NAME", ((RenameViewStatement) sqlStatements[0]).getOldViewName());
        assertEquals("NEW_NAME", ((RenameViewStatement) sqlStatements[0]).getNewViewName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");

        assertEquals("View OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SybaseASADatabase
                || database instanceof InformixDatabase
                || database instanceof DerbyDatabase
                || database instanceof HsqlDatabase
                || database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof FirebirdDatabase;
    }
}
