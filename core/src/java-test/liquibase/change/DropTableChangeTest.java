package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.DropTableStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link DropTableChange}
 */
public class DropTableChangeTest extends AbstractChangeTest {
    private DropTableChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Table", change.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropTableStatement);
        assertEquals("SCHEMA_NAME", ((DropTableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB_NAME", ((DropTableStatement) sqlStatements[0]).getTableName());
        assertTrue(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

    @Test
    public void generateStatement_nullCascadeConstraints() throws Exception {
        change.setCascadeConstraints(null);
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertFalse(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Table TAB_NAME dropped", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("true", element.getAttribute("cascadeConstraints"));
    }

    @Test
    public void createNode_withSchema() throws Exception {
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertEquals("true", element.getAttribute("cascadeConstraints"));
        assertTrue(element.hasAttribute("schemaName"));
    }

    @Test
    public void createNode_nullConstraint() throws Exception {
        change.setCascadeConstraints(null);
        Element element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertFalse(element.hasAttribute("cascadeConstraints"));
    }
}