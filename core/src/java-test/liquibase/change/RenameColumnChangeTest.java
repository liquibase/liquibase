package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link RenameColumnChange}
 */
public class RenameColumnChangeTest extends AbstractChangeTest {

    RenameColumnChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RenameColumnChange();

        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldColumnName("oldColName");
        refactoring.setNewColumnName("newColName");
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Rename Column", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        OracleDatabase database = new OracleDatabase();
        assertEquals("ALTER TABLE TABLE_NAME RENAME COLUMN oldColName TO newColName", refactoring.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Column with the name oldColName has been renamed to newColName", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = refactoring.createNode(document);
        assertEquals("renameColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("oldColName", node.getAttribute("oldColumnName"));
        assertEquals("newColName", node.getAttribute("newColumnName"));
    }
}
