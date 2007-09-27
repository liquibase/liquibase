package liquibase.change;

import liquibase.database.OracleDatabase;
import liquibase.database.SybaseDatabase;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

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

        OracleDatabase db = new OracleDatabase();
        assertEquals("ALTER TABLE TAB ADD NEWCOL TYP NOT NULL", refactoring.generateStatements(db)[0].getSqlStatement(db));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        AddColumnChange refactoring = new AddColumnChange();
        refactoring.setTableName("TAB");
        ColumnConfig column = new ColumnConfig();
        column.setName("NEWCOL");
        column.setType("TYP");
        refactoring.setColumn(column);

        assertEquals("Column NEWCOL(TYP) added to TAB", refactoring.getConfirmationMessage());
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

        SybaseDatabase db = new SybaseDatabase();
        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NULL", refactoring.generateStatements(db)[0].getSqlStatement(db));
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

        SybaseDatabase database = new SybaseDatabase();
        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NOT NULL", refactoring.generateStatements(database)[0].getSqlStatement(database));

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

        SybaseDatabase database = new SybaseDatabase();
        assertEquals("ALTER TABLE [TAB] ADD NEWCOL TYP NULL", refactoring.generateStatements(database)[0].getSqlStatement(database));

    }
}