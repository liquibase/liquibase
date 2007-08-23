package liquibase.migrator.change;

import liquibase.database.MySQLDatabase;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link DropNotNullConstraintChange}
 */
public class DropNotNullConstraintChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Not-Null Constraint", new DropNotNullConstraintChange().getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setColumnDataType("varchar(200)");
        assertEquals("ALTER TABLE TABLE_NAME MODIFY COL_HERE varchar(200) DEFAULT NULL", change.generateStatements(new MySQLDatabase())[0]);
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null Constraint has been dropped to the column COL_HERE of the table TABLE_NAME", change.getConfirmationMessage());

    }

    @Test
    public void createNode() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropNotNullConstraint", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnName"));
    }
}