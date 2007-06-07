package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

public class ModifyColumnChangeTest extends AbstractChangeTest {

    ModifyColumnChange change;

    public void setUp() throws Exception {
        super.setUp();
        change = new ModifyColumnChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("NAME");
        col1.setType("integer(3)");

        change.setColumn(col1);
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Modify Column", change.getChangeName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("ALTER TABLE TABLE_NAME MODIFY (NAME integer(3))", change.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Column with the name NAME has been modified.", change.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = change.createNode(document);
        assertEquals("modifyColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NAME", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("integer(3)", ((Element) columns.item(0)).getAttribute("type"));
    }
}
