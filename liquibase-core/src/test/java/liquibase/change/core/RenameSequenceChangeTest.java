package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenameSequenceStatement;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RenameSequenceChange.java}
 */
public class RenameSequenceChangeTest extends StandardChangeTest {

    private RenameSequenceChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameSequenceChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("renameSequence", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        RenameSequenceChange refactoring = new RenameSequenceChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldSequenceName("OLD_NAME");
        refactoring.setNewSequenceName("NEW_NAME");


        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameSequenceStatement);
        assertEquals("SCHEMA_NAME", ((RenameSequenceStatement) sqlStatements[0]).getSchemaName());
        assertEquals("OLD_NAME", ((RenameSequenceStatement) sqlStatements[0]).getOldSequenceName());
        assertEquals("NEW_NAME", ((RenameSequenceStatement) sqlStatements[0]).getNewSequenceName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldSequenceName("OLD_NAME");
        refactoring.setNewSequenceName("NEW_NAME");

        assertEquals("Sequence OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return !database.supportsSequences()
            // TODO: following are not implemented/tested currently
            || (database instanceof DB2Database)
            || (database instanceof FirebirdDatabase)
            || (database instanceof H2Database)
            || (database instanceof HsqlDatabase)
            || (database instanceof InformixDatabase);
    }

}
