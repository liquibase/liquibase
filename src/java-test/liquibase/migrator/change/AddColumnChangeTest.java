package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

public class AddColumnChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        assertEquals("Add Column", refactoring.getChangeName());
    }

    public void testGenerateStatement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        assertEquals("Column NEWCOL(TYP) has been added to TAB", refactoring.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        Element node = refactoring.createNode(document);
        assertEquals("addColumn", node.getTagName());
        assertEquals("TAB", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NEWCOL", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("TYP", ((Element) columns.item(0)).getAttribute("type"));

    }
}