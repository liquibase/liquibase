package liquibase.change.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RenamePrimaryKeyStatement;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RenamePrimaryKeyChange.java}
 */
public class RenamePrimaryKeyChangeTest extends StandardChangeTest {

    private RenamePrimaryKeyChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenamePrimaryKeyChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("renamePrimaryKey", ChangeFactory.getInstance().getChangeMetaData(refactoring).getName());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        RenamePrimaryKeyChange refactoring = new RenamePrimaryKeyChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldConstraintName("OLD_NAME");
        refactoring.setNewConstraintName("NEW_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenamePrimaryKeyStatement);
        assertEquals("SCHEMA_NAME", ((RenamePrimaryKeyStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((RenamePrimaryKeyStatement) sqlStatements[0]).getTableName());
        assertEquals("OLD_NAME", ((RenamePrimaryKeyStatement) sqlStatements[0]).getOldConstraintName());
        assertEquals("NEW_NAME", ((RenamePrimaryKeyStatement) sqlStatements[0]).getNewConstraintName());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        RenamePrimaryKeyChange change = new RenamePrimaryKeyChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setOldConstraintName("OLD_NAME");
        change.setNewConstraintName("NEW_NAME");

        assertEquals("Primary key of table TABLE_NAME renamed to NEW_NAME", change.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        // TODO support for other DBs
        return !((database instanceof OracleDatabase) || (database instanceof PostgresDatabase));
    }

}
