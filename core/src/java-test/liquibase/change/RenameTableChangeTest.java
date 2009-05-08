package liquibase.change;

import liquibase.database.CacheDatabase;
import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.MockDatabase;
import liquibase.statement.RenameTableStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
        assertEquals("Rename Table", refactoring.getChangeMetaData().getDescription());
    }

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

    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldTableName("OLD_NAME");
        refactoring.setNewTableName("NEW_NAME");

        assertEquals("Table OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof CacheDatabase
                || database instanceof FirebirdDatabase;
    }

}
