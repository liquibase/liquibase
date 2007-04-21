package liquibase.database.struture;

import junit.framework.*;
import liquibase.database.struture.Table;
import static org.easymock.classextension.EasyMock.*;

public class TableTest extends TestCase {
    Table table;

    public void testTable() throws Exception {
        DatabaseSystem databaseSystem = createMock(DatabaseSystem.class);
        replay(databaseSystem);
        Table table = new Table("name", "catalog", "schema", "type", "remarks", databaseSystem);
        assertEquals("name", table.getName());
        assertEquals("catalog", table.getCatalog());
        assertEquals("schema", table.getSchema());
        assertEquals("type", table.getType());
        assertEquals("remarks", table.getRemarks());


    }

    public void testToString() throws Exception {
        DatabaseSystem databaseSystem = createMock(DatabaseSystem.class);
        replay(databaseSystem);
        Table table = new Table("name", "catalog", "schema", "type", "remarks", databaseSystem);

        assertEquals("name", table.toString());
    }

    public void testEquals() throws Exception {
        DatabaseSystem databaseSystem = createMock(DatabaseSystem.class);
        replay(databaseSystem);
        Table table1 = new Table("name", "catalog", "schema", "type", "remarks", databaseSystem);
        assertEquals(table1, table1);
        assertNotNull(table1);
        assertFalse(table1.equals(new String()));

    }
}