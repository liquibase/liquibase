package liquibase.migrator.change;

import liquibase.database.MySQLDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class DropNotNullConstraintChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Not-Null Constraint", new DropNotNullConstraintChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("alter table TABLE_NAME modify COL_HERE varchar(200) default null", change.generateStatement(new MySQLDatabase() {
            public String getColumnDataType(String tblName, String colName) {
                return "varchar(200)";
            }

            public void updateNullColumns(String tblName, String colName, String defaultValue) {

            }
        }));
    }

    public void testGetConfirmationMessage() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null Constraint has been dropped to the column COL_HERE of the table TABLE_NAME", change.getConfirmationMessage());

    }

    public void testIsApplicableTo() throws Exception {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[]{
                createColumnDatabaseStructure(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[]{
                createColumnDatabaseStructure(),
                createColumnDatabaseStructure(),
        }))));

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