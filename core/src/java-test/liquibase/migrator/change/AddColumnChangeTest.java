package liquibase.migrator.change;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;

import liquibase.database.OracleDatabase;
import liquibase.database.SybaseDatabase;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests for {@link AddColumnChange}
 */
public class AddColumnChangeTest extends AbstractChangeTest {

    @Test
    public void getRefactoringName() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        assertEquals("Add Column", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setPrimaryKey(Boolean.FALSE);
        constraints.setNullable(Boolean.FALSE);

        column.setConstraints(constraints);

        refactoring.setColumn(column);

        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP NOT NULL", refactoring.generateStatements(new OracleDatabase())[0]);
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        assertEquals("Column NEWCOL(TYP) has been added to TAB", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        Element node = refactoring.createNode(document);
        assertEquals("addColumn", node.getTagName());
        assertEquals("TAB", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NEWCOL", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("TYP", ((Element) columns.item(0)).getAttribute("type"));

    }
    
    @Test
    public void sybaseNull() throws Exception {
    	AddColumnChange refactoring = new AddColumnChange();
    	refactoring.setTableName("TAB");
    	ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);
        
        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP NULL", refactoring.generateStatements(new SybaseDatabase())[0]);
    }
    
    @Test
    public void sybaseNotNull() throws Exception{
    	AddColumnChange refactoring = new AddColumnChange();
    	refactoring.setTableName("TAB");
    	ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);
        
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setPrimaryKey(Boolean.FALSE);
        constraints.setNullable(Boolean.FALSE);
        
        column.setConstraints(constraints);
        
        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP NOT NULL", refactoring.generateStatements(new SybaseDatabase())[0]);

    }
    
    @Test
    public void sybaseConstraintsNull() throws Exception{
    	AddColumnChange refactoring = new AddColumnChange();
    	refactoring.setTableName("TAB");
    	ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);
        
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setPrimaryKey(Boolean.FALSE);
        constraints.setNullable(Boolean.TRUE);
        
        column.setConstraints(constraints);
        
        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP NULL", refactoring.generateStatements(new SybaseDatabase())[0]);

    }
}