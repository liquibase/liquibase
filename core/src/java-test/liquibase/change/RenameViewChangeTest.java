package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.sql.RenameViewStatement;
import liquibase.database.sql.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class RenameViewChangeTest extends AbstractChangeTest {

    private RenameViewChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameViewChange();
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Rename View", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        RenameViewChange refactoring = new RenameViewChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");


        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameViewStatement);
        assertEquals("SCHEMA_NAME", ((RenameViewStatement) sqlStatements[0]).getSchemaName());
        assertEquals("OLD_NAME", ((RenameViewStatement) sqlStatements[0]).getOldViewName());
        assertEquals("NEW_NAME", ((RenameViewStatement) sqlStatements[0]).getNewViewName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");

        assertEquals("View OLD_NAME renamed to NEW_NAME", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setOldViewName("OLD_NAME");
        refactoring.setNewViewName("NEW_NAME");

        Element node = refactoring.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("renameView", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("OLD_NAME", node.getAttribute("oldViewName"));
        assertEquals("NEW_NAME", node.getAttribute("newViewName"));
    }
}
