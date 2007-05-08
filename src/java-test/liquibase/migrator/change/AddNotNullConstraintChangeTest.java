package liquibase.migrator.change;

import liquibase.database.MySQLDatabase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class AddNotNullConstraintChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        assertEquals("Add Not-Null Constraint", new AddNotNullConstraintChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setDefaultNullValue("DEFAULT_VALUE");
        change.setColumnDataType("varchar(200)");
        MySQLDatabase database = new MySQLDatabase();
        assertEquals("UPDATE TABLE_NAME SET COL_HERE='DEFAULT_VALUE' WHERE COL_HERE IS NULL", change.generateStatements(database)[0]);
        assertEquals("ALTER TABLE TABLE_NAME MODIFY COL_HERE varchar(200) NOT NULL", change.generateStatements(database)[1]);
    }

    public void testGetConfirmationMessage() throws Exception {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null Constraint has been added to the column COL_HERE of the table TABLE_NAME", change.getConfirmationMessage());

    }

    public void testCreateNode() throws Exception {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setDefaultNullValue("DEFAULT_VALUE");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("addNotNullConstraint", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_HERE", node.getAttribute("columnName"));
        assertEquals("DEFAULT_VALUE", node.getAttribute("defaultNullValue"));


    }
}