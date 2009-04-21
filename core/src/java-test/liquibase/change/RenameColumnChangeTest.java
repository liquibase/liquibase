package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.RenameColumnStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
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

        refactoring.setSchemaName("SCHEMA_NAME");
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
        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof RenameColumnStatement);
        assertEquals("SCHEMA_NAME", ((RenameColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((RenameColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("oldColName", ((RenameColumnStatement) sqlStatements[0]).getOldColumnName());
        assertEquals("newColName", ((RenameColumnStatement) sqlStatements[0]).getNewColumnName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Column TABLE_NAME.oldColName renamed to newColName", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = refactoring.createNode(document);
        assertEquals("renameColumn", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("oldColName", node.getAttribute("oldColumnName"));
        assertEquals("newColName", node.getAttribute("newColumnName"));
    }
}
