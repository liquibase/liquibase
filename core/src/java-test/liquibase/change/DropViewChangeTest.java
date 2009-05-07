package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.Database;
import liquibase.database.SybaseASADatabase;
import liquibase.statement.DropViewStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DropViewChangeTest  extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop View", new DropViewChange().getChangeMetaData().getDescription());
    }

    @Test
    public void generateStatement() throws Exception {
        DropViewChange change = new DropViewChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setViewName("VIEW_NAME");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropViewStatement);
        assertEquals("SCHEMA_NAME", ((DropViewStatement) sqlStatements[0]).getSchemaName());
        assertEquals("VIEW_NAME", ((DropViewStatement) sqlStatements[0]).getViewName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropViewChange change = new DropViewChange();
        change.setViewName("VIEW_NAME");

        assertEquals("View VIEW_NAME dropped", change.getConfirmationMessage());
    }
}
