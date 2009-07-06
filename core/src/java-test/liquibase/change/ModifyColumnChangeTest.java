package liquibase.change;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.DocumentBuilderFactory;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests for {@link ModifyColumnChange}
 */
public class ModifyColumnChangeTest extends AbstractChangeTest {

    ModifyColumnChange change;

    @Before
    public void setUp() throws Exception {
        change = new ModifyColumnChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("NAME");
        col1.setType("integer(3)");

        change.addColumn(col1);
    }

    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Modify Column", change.getChangeName());
    }

    @Test
    public void generateStatement() throws Exception {
        OracleDatabase database = new OracleDatabase();
        assertEquals("ALTER TABLE TABLE_NAME MODIFY ( NAME integer(3) )", change.generateStatements(database)[0].getSqlStatement(database));
    }

    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Columns NAME(integer(3)) of TABLE_NAME modified", change.getConfirmationMessage());
    }

    @Test
    public void createNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = change.createNode(document);
        assertEquals("modifyColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));

        NodeList columns = node.getElementsByTagName("column");
        assertEquals(1, columns.getLength());
        assertEquals("column", ((Element) columns.item(0)).getTagName());
        assertEquals("NAME", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals("integer(3)", ((Element) columns.item(0)).getAttribute("type"));
    }
    
    @Test
    public void generateFullStatementForMysql() throws Exception {
        Database database = new MySQLDatabase();
        ModifyColumnChange mysqlchange = new ModifyColumnChange();
        mysqlchange.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("NAME");
        col1.setType("integer(3)");
        col1.setAutoIncrement(true);
        col1.setDefaultValueNumeric(0);
        
        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setPrimaryKey(true);
        constraints.setNullable(false);
        col1.setConstraints(constraints);

        mysqlchange.addColumn(col1);
        assertEquals("ALTER TABLE `TABLE_NAME` MODIFY `NAME` integer(3) NOT NULL DEFAULT 0 AUTO_INCREMENT PRIMARY KEY", mysqlchange.generateStatements(database)[0].getSqlStatement(database));
    }

    
}
