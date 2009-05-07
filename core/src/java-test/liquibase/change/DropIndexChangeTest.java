package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.Database;
import liquibase.database.SybaseASADatabase;
import liquibase.statement.DropIndexStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests for {@link DropIndexChange}
 */
public class DropIndexChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Index", new DropIndexChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");
        refactoring.setSchemaName("SCHEMA_NAME");

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropIndexStatement);
        assertEquals("SCHEMA_NAME", ((DropIndexStatement) sqlStatements[0]).getTableSchemaName());
        assertEquals("TABLE_NAME", ((DropIndexStatement) sqlStatements[0]).getTableName());
        assertEquals("IDX_NAME", ((DropIndexStatement) sqlStatements[0]).getIndexName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }
}