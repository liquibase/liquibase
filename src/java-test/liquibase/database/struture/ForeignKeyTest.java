package liquibase.database.struture;

import junit.framework.*;
import static org.easymock.classextension.EasyMock.*;
import liquibase.database.struture.ForeignKey;

import java.sql.Connection;

public class ForeignKeyTest extends TestCase {
    ForeignKey foreignKey;

    public void testForeignKey() throws Exception {
        Table pkTable = new Table("pkTable", "cat", null, null, null, null);
        Table fkTable = new Table("fkTable", "cat", null, null, null, null);
        ForeignKey key = new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName");
        assertEquals(pkTable, key.getPrimaryKeyTable());
        assertEquals(fkTable, key.getForeignKeyTable());
        assertEquals("pkColName", key.getPrimaryKeyColumnName());
        assertEquals("fkColName", key.getForeignKeyColumnName());
        assertEquals("fkName", key.getForeignKeyName());
        assertEquals("pkName", key.getPrimaryKeyName());
    }

    public void testToString() throws Exception {
        Table pkTable = new Table("pkTable", "cat", null, null, null, null);
        Table fkTable = new Table("fkTable", "cat", null, null, null, null);
        ForeignKey key = new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName");

        assertEquals("fkTable.fkColName->pkTable.pkColName", key.toString());
    }

    public void testEquals() throws Exception {
        Table pkTable = new Table("pkTable", "cat", null, null, null, null);
        Table pkTable2 = new Table("pkTable2", "cat", null, null, null, null);
        Table fkTable = new Table("fkTable", "cat", null, null, null, null);
        Table fkTable2 = new Table("fkTable2", "cat", null, null, null, null);
        ForeignKey key1 = new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName");

        assertTrue(key1.equals(key1));
        assertFalse(key1.equals(null));
        assertFalse(key1.equals(new String()));
        assertTrue(key1.equals(new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName")));
        assertFalse(key1.equals(new ForeignKey(pkTable, "pkCol2", fkTable, "fkColName", "fkName", "pkName")));
        assertFalse(key1.equals(new ForeignKey(pkTable, "pkColName", fkTable, "fkColName2", "fkName", "pkName")));
        assertFalse(key1.equals(new ForeignKey(pkTable, "pkColName", fkTable2, "fkColName", "fkName", "pkName")));
        assertFalse(key1.equals(new ForeignKey(pkTable2, "pkColName", fkTable, "fkColName", "fkName", "pkName")));
    }

    public void testCompareTo() throws Exception {
        Table pkTable = new Table("pkTableA", "cat", null, null, null, null);
        Table pkTable2 = new Table("pkTableB", "cat", null, null, null, null);
        Table fkTable = new Table("fkTableA", "cat", null, null, null, null);
        Table fkTable2 = new Table("fkTableB", "cat", null, null, null, null);
        ForeignKey key1 = new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName");
        ForeignKey key2 = new ForeignKey(pkTable2, "pkColName", fkTable2, "fkColName", "fkName", "pkName");

        assertTrue(key1.compareTo(new String()) > 0);
        assertEquals(0, key1.compareTo(key1));
        assertTrue(key1.compareTo(key2) < 0);
    }

    public void testGetConnection() throws Exception {
        Table pkTable = createMock(Table.class);
        Connection conn = createMock(Connection.class);
        expect(pkTable.getConnection()).andReturn(conn);
        Table fkTable = new Table("fkTableA", "cat", null, null, null, null);
        replay(pkTable);
        replay(conn);

        ForeignKey key = new ForeignKey(pkTable, "pkColName", fkTable, "fkColName", "fkName", "pkName");
        assertEquals(conn, key.getConnection());
        verify(pkTable);
        verify(conn);

    }
}