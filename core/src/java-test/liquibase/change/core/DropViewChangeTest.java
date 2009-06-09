package liquibase.change.core;

import liquibase.database.core.MockDatabase;
import liquibase.statement.DropViewStatement;
import liquibase.statement.SqlStatement;
import liquibase.change.core.DropViewChange;
import liquibase.change.AbstractChangeTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DropViewChangeTest  extends AbstractChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop View", new DropViewChange().getChangeMetaData().getDescription());
    }

    @Override
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

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        DropViewChange change = new DropViewChange();
        change.setViewName("VIEW_NAME");

        assertEquals("View VIEW_NAME dropped", change.getConfirmationMessage());
    }
}
