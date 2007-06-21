package liquibase.database;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import liquibase.migrator.change.ColumnConfig;

import org.junit.Test;

/**
 * Base test class for database-specific tests
 */
public abstract class AbstractDatabaseTest {

    protected AbstractDatabase database;

    protected AbstractDatabaseTest(AbstractDatabase database) {
        this.database = database;
    }

    public AbstractDatabase getDatabase() {
        return database;
    }

    protected abstract String getProductNameString();

    public abstract void testGetBlobType();

    public abstract void testSupportsInitiallyDeferrableColumns();

    public abstract void testGetBooleanType();

    public abstract void testGetCurrencyType();

    public abstract void testGetUUIDType();

    public abstract void testGetClobType();

    public abstract void testGetDateType();

    public abstract void testGetDateTimeType();

    public abstract void testGetCurrentDateTimeFunction();

    @Test
    public void testGetColumnType() {
        ColumnConfig column = new ColumnConfig();

        column.setType("boolean");
        assertEquals(database.getBooleanType(), database.getColumnType(column));
        column.setType("BooLean");
        assertEquals(database.getBooleanType(), database.getColumnType(column));


        column.setType("currency");
        assertEquals(database.getCurrencyType(), database.getColumnType(column));
        column.setType("currEncy");
        assertEquals(database.getCurrencyType(), database.getColumnType(column));

        column.setType("uuid");
        assertEquals(database.getUUIDType(), database.getColumnType(column));
        column.setType("UUID");
        assertEquals(database.getUUIDType(), database.getColumnType(column));

        column.setType("blob");
        assertEquals(database.getBlobType(), database.getColumnType(column));
        column.setType("BLOB");
        assertEquals(database.getBlobType(), database.getColumnType(column));

        column.setType("clob");
        assertEquals(database.getClobType(), database.getColumnType(column));
        column.setType("CLOB");
        assertEquals(database.getClobType(), database.getColumnType(column));

        column.setType("SomethingElse");
        assertEquals("SomethingElse", database.getColumnType(column));
    }

    @Test
    public void testGetDriverName() throws Exception {
        Connection connection = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);

        expect(connection.getMetaData()).andReturn(metaData);
        expect(metaData.getDriverName()).andReturn("DriverNameHere");
        replay(connection);
        replay(metaData);

        Database database = getDatabase();
        database.setConnection(connection);

        assertEquals("DriverNameHere", database.getDriverName());
    }

    @Test
    public void testGetConnectionURL() throws Exception {
        Connection connection = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);

        expect(connection.getMetaData()).andReturn(metaData);
        expect(metaData.getURL()).andReturn("URLHere");
        replay(connection);
        replay(metaData);

        Database database = getDatabase();
        database.setConnection(connection);

        assertEquals("URLHere", database.getConnectionURL());
    }

    @Test
    public void testGetConnectionUsername() throws Exception {
        Connection connection = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);

        expect(connection.getMetaData()).andReturn(metaData);
        expect(metaData.getUserName()).andReturn("usernameHere");
        replay(connection);
        replay(metaData);

        Database database = getDatabase();
        database.setConnection(connection);

        assertEquals("usernameHere", database.getConnectionUsername());
    }

    @Test
    public void testIsCorrectDatabaseImplementation() throws Exception {
        assertTrue(getDatabase().isCorrectDatabaseImplementation(getMockConnection()));
    }

    protected Connection getMockConnection() throws SQLException {
        Connection conn = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);

        conn.setAutoCommit(false);
        expectLastCall().anyTimes();
        expect(conn.getMetaData()).andReturn(metaData).anyTimes();
        expect(metaData.getDatabaseProductName()).andReturn(getProductNameString()).anyTimes();
        replay(conn);
        replay(metaData);
        return conn;
    }
}
