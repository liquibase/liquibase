package liquibase.migrator.change;

import liquibase.database.OracleDatabase;
import liquibase.database.struture.Column;
import liquibase.database.struture.DatabaseStructure;
import org.easymock.EasyMock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;

public class RenameColumnChangeTest extends AbstractChangeTest {

    RenameColumnChange refactoring;

    public void setUp() throws Exception {
        super.setUp();
        refactoring = new RenameColumnChange();

        refactoring.setTableName("TABLE_NAME");
        refactoring.setOldColumnName("oldColName");
        refactoring.setNewColumnName("newColName");
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
        assertEquals( "Rename Column", refactoring.getRefactoringName() );
    }

    public void testGenerateStatement() throws Exception {
        assertEquals( "alter table TABLE_NAME rename column oldColName  to newColName", refactoring.generateStatement(new OracleDatabase()));
    }

    public void testGetConfirmationMessage() throws Exception {
        assertEquals( "Column with the name oldColName has been renamed to newColName", refactoring.getConfirmationMessage() );
    }

    public void testIsApplicableTo() throws Exception {
        assertTrue(refactoring.isApplicableTo(new HashSet(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
        }))));

        assertFalse(refactoring.isApplicableTo(new HashSet(Arrays.asList(new DatabaseStructure[] {
                createColumnDatabaseStructure(),
                createColumnDatabaseStructure(),
        }))));
    }

    public void testCreateNode() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

        Element node = refactoring.createNode(document);
        assertEquals("renameColumn", node.getTagName());
        assertEquals("TABLE_NAME", node.getAttribute("tableName"));
        assertEquals("oldColName", node.getAttribute("oldColumnName"));
        assertEquals("newColName", node.getAttribute("newColumnName"));
    }
}
