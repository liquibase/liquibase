package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

public class ModifyColumnChangeTest extends AbstractChangeTest {

    ModifyColumnChange change;

    public void setUp() throws Exception {
        super.setUp();
        change = new ModifyColumnChange();
        change.setTableName("TABLE_NAME");

        ColumnConfig col1 = new ColumnConfig();
        col1.setName( "NAME" );
        col1.setType( "integer(3)" );

        change.setColumn(col1);
    }

    protected Column createColumnDatabaseStructure() throws SQLException {
        ResultSet rs = EasyMock.createMock(ResultSet.class);
        EasyMock.expect(rs.getString("TABLE_NAME")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_CAT")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_SCHEM")).andStubReturn(null);
        EasyMock.expect(rs.getString("TABLE_TYPE")).andStubReturn(null);
        EasyMock.expect(rs.getString("REMARKS")).andStubReturn(null);
        EasyMock.expect(rs.getString("COLUMN_NAME")).andStubReturn(null);
        EasyMock.expect(rs.getString("COLUMN_DEF")).andStubReturn(null);
        EasyMock.expect(rs.getString("TYPE_NAME")).andStubReturn(null);

        EasyMock.expect(rs.getInt("DATA_TYPE")).andStubReturn(0);
        EasyMock.expect(rs.getInt("COLUMN_SIZE")).andStubReturn(0);
        EasyMock.expect(rs.getInt("DECIMAL_DIGITS")).andStubReturn(0);
        EasyMock.expect(rs.getInt("NULLABLE")).andStubReturn(0);


        EasyMock.replay(rs);

        return new Column(null, null, -1, null, -1, -1, -1, null, null);
    }
    public void testGetRefactoringName() throws Exception {
        assertEquals( "Modify Column", change.getRefactoringName() );
    }

    public void testGenerateStatement() throws Exception {
        assertEquals( "alter table TABLE_NAME modify (NAME integer(3))", change.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals( "Column with the name NAME has been modified.", change.getConfirmationMessage() );
    }

    public void testIsApplicableTo() throws Exception {
        assertTrue(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
        }))));

        assertFalse(change.isApplicableTo(new HashSet<DatabaseStructure>(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
                createColumnDatabaseStructure(),
        }))));
    }

    public void testCreateNode() throws Exception {
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
}
