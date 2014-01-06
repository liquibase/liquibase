package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameViewStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class RenameViewChangeTest extends StandardChangeTest {

    private RenameViewChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameViewChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("renameView", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
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
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase;
    }
}
