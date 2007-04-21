package liquibase.migrator.change;

import liquibase.database.MySQLDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class AddNotNullConstraintChangeTest extends AbstractChangeTest {

    public void testGetRefactoringName() throws Exception {
        assertEquals("Add Not-Null Constraint", new AddNotNullConstraintChange().getRefactoringName());
    }

    public void testGenerateStatement() throws Exception {
    	AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        change.setDefaultNullValue("DEFAULT_VALUE");
        assertEquals("alter table TABLE_NAME modify COL_HERE varchar(200) not null", change.generateStatement(new MySQLDatabase(){
        	public  String getColumnDataType(String tblName,String colName) {
        		return "varchar(200)";
            }
        	public void updateNullColumns(String tblName,String colName,String defaultValue) {
            
        	}
        	}));
    }

    public void testGetConfirmationMessage() throws Exception {
    	AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName("TABLE_NAME");
        change.setColumnName("COL_HERE");
        assertEquals("Null Constraint has been added to the column COL_HERE of the table TABLE_NAME",change.getConfirmationMessage());
        
    }

    public void testIsApplicableTo() throws Exception {
    	AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
                createColumnDatabaseStructure(),
        }))));

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