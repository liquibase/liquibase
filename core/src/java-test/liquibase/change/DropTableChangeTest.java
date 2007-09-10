package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Table", change.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        OracleDatabase database = new OracleDatabase();
        assertEquals("DROP TABLE TAB_NAME CASCADE CONSTRAINTS", change.generateStatements(database)[0].getSqlStatement(database));

        change.setCascadeConstraints(null);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatements(database)[0].getSqlStatement(database));

        change.setCascadeConstraints(false);
        assertEquals("DROP TABLE TAB_NAME", change.generateStatements(database)[0].getSqlStatement(database));
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

        change.setCascadeConstraints(null);
        element = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropTable", element.getTagName());
        assertEquals("TAB_NAME", element.getAttribute("tableName"));
        assertFalse(element.hasAttribute("cascadeConstraints"));
    }
}