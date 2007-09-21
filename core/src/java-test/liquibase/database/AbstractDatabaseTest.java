package liquibase.database;

import liquibase.change.ColumnConfig;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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

    public abstract void getBlobType();

    public abstract void supportsInitiallyDeferrableColumns();

    public abstract void getBooleanType();

    public abstract void getCurrencyType();

    public abstract void getUUIDType();

    public abstract void getClobType();

    public abstract void getDateType();

    public abstract void getDateTimeType();

    public abstract void getCurrentDateTimeFunction();

    @Test
    public void getColumnType() {
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
    public void getDriverName() throws Exception {
        Connection connection = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
        
        connection.setAutoCommit(false);
        expect(connection.getMetaData()).andReturn(metaData);
        expect(metaData.getDriverName()).andReturn("DriverNameHere");
        replay(connection);
        replay(metaData);

        Database database = getDatabase();
        database.setConnection(connection);

        assertEquals("DriverNameHere", database.getDriverName());
    }

    @Test
    public void getConnectionURL() throws Exception {
        Connection connection = createMock(Connection.class);
        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
        connection.setAutoCommit(false);
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
        
        connection.setAutoCommit(false);
        expect(connection.getMetaData()).andReturn(metaData);
        expect(metaData.getUserName()).andReturn("usernameHere");
        replay(connection);
        replay(metaData);

        Database database = getDatabase();
        database.setConnection(connection);

        assertEquals("usernameHere", database.getConnectionUsername());
    }

    @Test
    public void isCorrectDatabaseImplementation() throws Exception {
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
