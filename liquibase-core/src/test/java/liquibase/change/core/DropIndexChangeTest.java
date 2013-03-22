package liquibase.change.core;

import liquibase.change.StandardChangeTest;
import liquibase.database.core.MockDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropIndexStatement;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link liquibase.change.core.DropIndexChange}
 */
public class DropIndexChangeTest extends StandardChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("dropIndex", new DropIndexChange().getChangeMetaData().getName());
    }

    @Override
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

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropIndexChange refactoring = new DropIndexChange();
        refactoring.setIndexName("IDX_NAME");
        refactoring.setTableName("TABLE_NAME");

        assertEquals("Index IDX_NAME dropped from table TABLE_NAME", refactoring.getConfirmationMessage());
    }
}