package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class DropTableChangeTest extends AbstractChangeTest {
    private DropTableChange change;

    protected void setUp() throws Exception {
        change = new DropTableChange();
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Table", change.getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("DROP TABLE TAB_NAME CASCADE CONSTRAINTS", change.generateStatement(new OracleDatabase()));

        change.setCascadeConstraints(null);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatement(new OracleDatabase()));

        change.setCascadeConstraints(false);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Table TAB_NAME dropped", change.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));

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