package liquibase.database.struture;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class DatabaseSystemTest extends TestCase {

    public void testCompareTo() throws Exception {
        DatabaseSystem databaseSystem1 = createDatabaseSystem();
        DatabaseSystem databaseSystem2 = createDatabaseSystem();

        assertEquals(0, databaseSystem1.compareTo(databaseSystem2));

        assertTrue(databaseSystem1.compareTo(new String()) > 0);
    }

    public void testDatabaseSystem() throws Exception {
        Connection mockConnection = createMock(Connection.class);
        DatabaseMetaData mockMetaData = createMock(DatabaseMetaData.class);

        expect(mockConnection.getMetaData()).andReturn(mockMetaData);
        expect(mockMetaData.getURL()).andReturn("jdbc:mock");
        replay(mockMetaData);
        replay(mockConnection);

        new DatabaseSystem(mockConnection);
        verify(mockMetaData);
        verify(mockConnection);
    }

    private DatabaseSystem createDatabaseSystem() throws Exception {
        Connection mockConnection = createMock(Connection.class);
        DatabaseMetaData mockMetaData = createMock(DatabaseMetaData.class);

        expect(mockConnection.getMetaData()).andReturn(mockMetaData);
        expect(mockMetaData.getURL()).andReturn("jdbc:mock");
        replay(mockMetaData);
        replay(mockConnection);
        return new DatabaseSystem(mockConnection);
    }
}