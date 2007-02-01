package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class DropColumnChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        assertEquals("Drop Column", new DropColumnChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("ALTER TABLE TABLE_NAME DROP COLUMN COL_HERE", change.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");

        assertEquals("Column TABLE_NAME(COL_HERE) dropped", change.getConfirmationMessage());
    }

    public void testIsApplicableTo() throws Exception {
        DropColumnChange change = new DropColumnChange();
        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
                createColumnDatabaseStructure(),
        }))));

    }


    public void testCreateNode() throws Exception {
        DropColumnChange change = new DropColumnChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_NAME");

        Element node = change.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("dropColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("COL_NAME", node.getAttribute("columnName"));
    }
}