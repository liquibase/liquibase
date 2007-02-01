package liquibase.database.struture;

import junit.framework.*;
import static org.easymock.classextension.EasyMock.*;
import liquibase.database.struture.Column;

import java.util.ArrayList;
import java.util.zip.ZipFile;
import java.sql.Connection;

public class ColumnTest extends TestCase {

    public void testEquals() throws Exception {
        Column column1 = new Column(new Table("table1", "catalog", "schem", null, null, null), "colName", -1, null, -1, -1, -1, null, null);
        assertTrue(column1.equals(column1));
        assertFalse(column1.equals(new String()));
        assertFalse(column1.equals(null));

        assertTrue(column1.equals(new Column(new Table("table1", "catalog", "schem", null, null, null), "colName", -1, null, -1, -1, -1, null, null)));

        assertFalse(column1.equals(new Column(new Table("table2", "catalog", "schem", null, null, null), "colName", -1, null, -1, -1, -1, null, null)));
        assertFalse(column1.equals(new Column(new Table("table1", "catalog", "schem", null, null, null), "differentColName", -1, null, -1, -1, -1, null, null)));

    }

    public void testCompareTo() throws Exception {
        Column column1 = new Column(new Table("table1", "catalog", "schem", null, null, null), "colB", -1, null, -1, -1, -1, null, null);
        assertEquals(-1, column1.compareTo(new Column(new Table("table1", "catalog", "schem", null, null, null), "colC", -1, null, -1, -1, -1, null, null)));
        assertEquals(1, column1.compareTo(new Column(new Table("table1", "catalog", "schem", null, null, null), "colA", -1, null, -1, -1, -1, null, null)));
        assertEquals(0, column1.compareTo(new Column(new Table("table1", "catalog", "schem", null, null, null), "colB", -1, null, -1, -1, -1, null, null)));

        assertTrue(column1.compareTo(new String()) > 0);
    }

    public void testGetConnection() throws Exception {
        Table mockTable = createMock(Table.class);
        Connection mockConnection = createMock(Connection.class);
        expect(mockTable.getConnection()).andReturn(mockConnection);
        replay(mockTable);
        replay(mockConnection);

        Column column = new Column(mockTable, "column", -1, null, -1, -1, -1, null, null);
        assertEquals(mockConnection, column.getConnection());
        verify(mockTable);

    }
}