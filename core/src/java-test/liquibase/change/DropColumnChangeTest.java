package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.statement.DropColumnStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests for {@link DropColumnChange}
 */
public class DropColumnChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Column", new DropColumnChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropColumnStatement);
        assertEquals("SCHEMA_NAME", ((DropColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("COL_HERE", ((DropColumnStatement) sqlStatements[0]).getColumnName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Column TABLE_NAME.COL_HERE dropped", change.getConfirmationMessage());
    }

}