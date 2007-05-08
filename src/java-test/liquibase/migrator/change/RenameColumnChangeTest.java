package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class RenameColumnChangeTest extends AbstractChangeTest {

    RenameColumnChange refactoring;

    public void setUp() throws Exception {
        super.setUp();
        refactoring = new RenameColumnChange();

        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldColumnName("oldColName");
        refactoring.setNewColumnName("newColName");
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals("Rename Column", refactoring.getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        assertEquals("ALTER TABLE TABLE_NAME RENAME COLUMN oldColName TO newColName", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals("Column with the name oldColName has been renamed to newColName", refactoring.getConfirmationMessage());
    }

    public void testCreateNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = refactoring.createNode(document);
        assertEquals("renameColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("oldColName", node.getAttribute("oldColumnName"));
        assertEquals("newColName", node.getAttribute("newColumnName"));
    }
}
