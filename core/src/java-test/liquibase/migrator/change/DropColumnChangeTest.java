package liquibase.migrator.change;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;

import liquibase.database.OracleDatabase;

import org.junit.Test;
import org.w3c.dom.Element;

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

        assertEquals("ALTER TABLE TABLE_NAME DROP COLUMN COL_HERE", change.generateStatements(new OracleDatabase())[0]);
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