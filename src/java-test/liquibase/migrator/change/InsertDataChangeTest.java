package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.DatabaseStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Arrays;
import java.util.HashSet;

public class InsertDataChangeTest extends AbstractChangeTest {

    InsertDataChange refactoring;

    public void setUp() throws Exception {
        super.setUp();
        refactoring = new InsertDataChange();
        refactoring.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName("id");
        col1.setValue("123");

        ColumnConfig col2 = new ColumnConfig();
        col2.setName("name");
        col2.setValue("Andrew");

        ColumnConfig col3 = new ColumnConfig();
        col3.setName("age");
        col3.setValue("21");

        refactoring.addColumn(col1);
        refactoring.addColumn(col2);
        refactoring.addColumn(col3);
    }

    public void testGetRefactoringName() throws Exception {
        assertEquals( "Insert Row", refactoring.getRefactoringName() );
    }

    public void testGenerateStatement() throws Exception {
         assertEquals( "insert into TABLE_NAME (id, name, age) values ('123', 'Andrew', '21')",
                refactoring.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals( "New rows have been inserted into the table TABLE_NAME", refactoring.getConfirmationMessage() );
    }

    public void testIsApplicableTo() throws Exception {
        assertTrue(refactoring.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createTableDatabaseStructure(),
        }))));
    }

    public void testCreateNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element node = refactoring.createNode(document);

        assertEquals( "insert", node.getTagName() );
        assertEquals( "TABLE_NAME", node.getAttribute("tableName") );

        NodeList columns = node.getChildNodes();
        assertEquals( 3, columns.getLength() );

        assertEquals( "column", ((Element) columns.item(0)).getTagName() );
        assertEquals( "id", ((Element) columns.item(0)).getAttribute("name"));
        assertEquals( "123", ((Element) columns.item(0)).getAttribute("value"));

        assertEquals( "column", ((Element) columns.item(1)).getTagName() );
        assertEquals( "name", ((Element) columns.item(1)).getAttribute("name"));
        assertEquals( "Andrew", ((Element) columns.item(1)).getAttribute("value"));

        assertEquals( "column", ((Element) columns.item(2)).getTagName() );
        assertEquals( "age", ((Element) columns.item(2)).getAttribute("name"));
        assertEquals( "21", ((Element) columns.item(2)).getAttribute("value"));
    }
}
