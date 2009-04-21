package liquibase.change;

import liquibase.database.MockDatabase;
import liquibase.database.statement.InsertStatement;
import liquibase.database.statement.SqlStatement;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link InsertDataChange}
 */
public class InsertDataChangeTest extends AbstractChangeTest {

    InsertDataChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new InsertDataChange();
        refactoring.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValueNumeric("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValueNumeric("21");
        
        ColumnConfig col4 = new ColumnConfig();
        col4.setName("height");
        col4.setValueNumeric("1.78");

        refactoring.addColumn(col1);
        refactoring.addColumn(col2);
        refactoring.addColumn(col3);
        refactoring.addColumn(col4);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Insert Row", refactoring.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof InsertStatement);
        assertEquals("123", ((InsertStatement) sqlStatements[0]).getColumnValue("id").toString());
        assertEquals("Andrew", ((InsertStatement) sqlStatements[0]).getColumnValue("name").toString());
        assertEquals("21", ((InsertStatement) sqlStatements[0]).getColumnValue("age").toString());
        assertEquals("1.78", ((InsertStatement) sqlStatements[0]).getColumnValue("height").toString());
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("New row inserted into TABLE_NAME", refactoring.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element node = refactoring.createNode(document);

        assertEquals("insert", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));

        NodeList columns = node.getChildNodes();
        assertEquals(4, columns.getLength());

        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("id", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("123", ((Element) columns.item(0)).getAttribute("valueNumeric"));

        assertEquals("column", ((Element) columns.item(1)).getTagName());
        assertEquals("name", ((Element) columns.item(1)).getAttribute("name"));
        assertEquals("Andrew", ((Element) columns.item(1)).getAttribute("value"));

        assertEquals("column", ((Element) columns.item(2)).getTagName());
        assertEquals("age", ((Element) columns.item(2)).getAttribute("name"));
        assertEquals("21", ((Element) columns.item(2)).getAttribute("valueNumeric"));
        
        assertEquals("column", ((Element) columns.item(3)).getTagName());
        assertEquals("height", ((Element) columns.item(3)).getAttribute("name"));
        assertEquals("1.78", ((Element) columns.item(3)).getAttribute("valueNumeric"));
    }
}
