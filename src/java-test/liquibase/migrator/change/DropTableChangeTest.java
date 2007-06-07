package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropTableChangeTest extends AbstractChangeTest {
    private DropTableChange change;

    protected void setUp() throws Exception {
        super.setUp();
        change = new DropTableChange();
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Table", change.getChangeName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("DROP TABLE TAB_NAME CASCADE CONSTRAINTS", change.generateStatements(new OracleDatabase())[0]);

        change.setCascadeConstraints(null);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatements(new OracleDatabase())[0]);

        change.setCascadeConstraints(false);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Table TAB_NAME dropped", change.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("true", element.getAttribute("cascadeConstraints"));

        change.setCascadeConstraints(null);
        element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertFalse(element.hasAttribute("cascadeConstraints"));
    }
}