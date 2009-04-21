package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.DropColumnStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link DropColumnChange}
 */
public class DropColumnChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Column", new DropColumnChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropColumnStatement);
        assertEquals("SCHEMA_NAME", ((DropColumnStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TABLE_NAME", ((DropColumnStatement) sqlStatements[0]).getTableName());
        assertEquals("COL_HERE", ((DropColumnStatement) sqlStatements[0]).getColumnName());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Column TABLE_NAME.COL_HERE dropped", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropColumn", node.getTagName());
        assertFalse(node.hasAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }

    @Test
    public void createNode_withSchema() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropColumn", node.getTagName());
        assertEquals("SCHEMA_NAME", node.getAttribute("schemaName"));
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }
}