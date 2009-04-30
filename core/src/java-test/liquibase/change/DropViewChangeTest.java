package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.DropViewStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

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
