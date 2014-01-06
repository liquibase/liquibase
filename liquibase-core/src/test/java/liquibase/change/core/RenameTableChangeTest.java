package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameTableStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RenameTableChange}
 */
public class RenameTableChangeTest extends StandardChangeTest {

    private RenameTableChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameTableChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("renameTable", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        RenameTableChange refactoring = new RenameTableChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");


        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameTableStatement);
        assertEquals("SCHEMA_NAME", ((RenameTableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("OLD_NAME", ((RenameTableStatement) sqlStatements[0]).getOldTableName());
        assertEquals("NEW_NAME", ((RenameTableStatement) sqlStatements[0]).getNewTableName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof FirebirdDatabase;
    }

}
