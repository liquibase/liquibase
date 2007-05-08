package liquibase.migrator.change;

import liquibase.database.MySQLDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class DropNotNullConstraintChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Not-Null Constraint", new DropNotNullConstraintChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setColumnDataType("varchar(200)");
        assertEquals("ALTER TABLE TABLE_NAME MODIFY COL_HERE varchar(200) DEFAULT NULL", change.generateStatements(new MySQLDatabase())[0]);
    }

    public void testGetConfirmationMessage() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null Constraint has been dropped to the column COL_HERE of the table TABLE_NAME", change.getConfirmationMessage());

    }

    public void testCreateNode() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropNotNullConstraint", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnName"));
    }
}