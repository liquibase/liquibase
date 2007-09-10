package liquibase.change;

import liquibase.database.OracleDatabase;
import static org.junit.Assert.assertEquals;
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
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        OracleDatabase database = new OracleDatabase();
        assertEquals("ALTER TABLE TABLE_NAME DROP COLUMN COL_HERE",
                change.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Column TABLE_NAME(COL_HERE) dropped", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }
}