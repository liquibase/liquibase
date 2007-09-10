package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RenameTableChange}
 */
public class RenameTableChangeTest extends AbstractChangeTest {

    private RenameTableChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameTableChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Rename Table", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        RenameTableChange refactoring = new RenameTableChange();
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        OracleDatabase database = new OracleDatabase();
        assertEquals("RENAME OLD_NAME TO NEW_NAME", refactoring.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table with the name OLD_NAME has been renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("OLD_NAME", refactoring.getOldTableName());
        assertEquals("NEW_NAME", refactoring.getNewTableName());
    }
}
