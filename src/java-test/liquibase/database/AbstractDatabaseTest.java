package liquibase.database;

import junit.framework.TestCase;
import liquibase.migrator.change.ColumnConfig;
import static org.easymock.EasyMock.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public abstract class AbstractDatabaseTest extends TestCase {

    private AbstractDatabase database;

    protected AbstractDatabaseTest(AbstractDatabase database) {
        this.database = database;
    }

    public AbstractDatabase getDatabase() {
        return database;
    }

    public abstract void testGetBlobType();

    public abstract void testSupportsInitiallyDeferrableColumns();

    public abstract void testGetBooleanType();

    public abstract void testGetCurrencyType();

    public abstract void testGetUUIDType();

    public abstract void testGetClobType();

    public abstract void testGetDateType();

    public abstract void testGetDateTimeType();

    public abstract void testGetCurrentDateTimeFunction();

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

    public void testIsCorrectDatabaseImplementation() throws Exception {
        assertTrue(getDatabase().isCorrectDatabaseImplementation(getMockConnection()));
    }

//    public void testDropDatabaseObjects() throws Exception {
//        Connection connection = createMock(Connection.class);
//        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
//
//        expect(connection.getMetaData()).andReturn(metaData).atLeastOnce();
//
//        ResultSet tableResultSet = createMock(ResultSet.class);
//        expect(tableResultSet.next()).andReturn(Boolean.TRUE);
//        expect(tableResultSet.next()).andReturn(Boolean.TRUE);
//        expect(tableResultSet.next()).andReturn(Boolean.TRUE);
//        expect(tableResultSet.next()).andReturn(Boolean.FALSE);
//        expect(tableResultSet.getString("TABLE_NAME")).andReturn("tableA");
//        expect(tableResultSet.getString("TABLE_TYPE")).andReturn("TABLE");
//        expect(tableResultSet.getString("TABLE_NAME")).andReturn("tableB");
//        expect(tableResultSet.getString("TABLE_TYPE")).andReturn("TABLE");
//        expect(tableResultSet.getString("TABLE_NAME")).andReturn("tableC");
//        expect(tableResultSet.getString("TABLE_TYPE")).andReturn("TABLE");
//        tableResultSet.close();
//        expectLastCall().atLeastOnce();
//        replay(tableResultSet);
//
//        expect(metaData.getTables((String) isNull(), eq("dbo"), (String) isNull(), aryEq(new String[] {"TABLE", "VIEW", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"}))).andReturn(tableResultSet);
//
//        Statement statement = createMock(Statement.class);
//        expect(connection.createStatement()).andReturn(statement).atLeastOnce();
//
//        expect(statement.executeUpdate("DROP TABLE tableA CASCADE CONSTRAINTS")).andStubReturn(-1);
//        statement.close();
//        expectLastCall().atLeastOnce();
//
//        expect(statement.executeUpdate("DROP TABLE tableB CASCADE CONSTRAINTS")).andStubReturn(-1);
//
//        expect(statement.executeUpdate("DROP TABLE tableC CASCADE CONSTRAINTS")).andStubReturn(-1);
//
//        ResultSet sequenceRS = createMock(ResultSet.class);
//
//        expect(statement.executeQuery("SELECT SEQUENCE_NAME FROM USER_SEQUENCES")).andReturn(sequenceRS);
//        expect(sequenceRS.next()).andReturn(Boolean.TRUE);
//        expect(sequenceRS.next()).andReturn(Boolean.TRUE);
//        expect(sequenceRS.next()).andReturn(Boolean.TRUE);
//        expect(sequenceRS.next()).andReturn(Boolean.FALSE);
//        expect(sequenceRS.getString("SEQUENCE_NAME")).andReturn("sequenceA");
//        expect(sequenceRS.getString("SEQUENCE_NAME")).andReturn("sequenceB");
//        expect(sequenceRS.getString("SEQUENCE_NAME")).andReturn("sequenceC");
//        sequenceRS.close();
//        expectLastCall().atLeastOnce();
//        replay(sequenceRS);
//
//        expect(statement.executeUpdate("DROP SEQUENCE sequenceA")).andStubReturn(-1);
//
//        expect(statement.executeUpdate("DROP SEQUENCE sequenceB")).andStubReturn(-1);
//
//        expect(statement.executeUpdate("DROP SEQUENCE sequenceC")).andStubReturn(-1);
//
//        connection.commit();
//        expectLastCall().atLeastOnce();
//
//        replay(statement);
//        replay(connection);
//        replay(metaData);
//
//        AbstractDatabase database = getDatabase();
//        database.setConnection(connection);
//
//        database.dropDatabaseObjects();
//
//        verify(connection);
//        verify(metaData);
//        verify(tableResultSet);
//    }

//    public void testCheckDatabaseChangeLogTable() throws Exception {
//        //Test if table doesn't exist
//        Connection connection = createMock(Connection.class);
//        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
//
//        expect(connection.getMetaData()).andReturn(metaData).atLeastOnce();
//
//        ResultSet tableResultSet = createMock(ResultSet.class);
//
//        expect(metaData.getTables(null, "dbo", "DatabaseChangeLog".toUpperCase(), null)).andReturn(tableResultSet);
//        expect(tableResultSet.next()).andReturn(Boolean.FALSE);
//
//        Statement statement = createMock(Statement.class);
//        expect(connection.createStatement()).andReturn(statement);
//        expect(statement.executeUpdate(("create table DatabaseChangeLog (id varchar(255) not null, author varchar(255) not null, filename varchar(255) not null, dateExecuted " + database.getDateTimeType() + " not null, md5sum varchar(32), primary key(id, author, filename))").toUpperCase())).andReturn(new Integer(1));
//
//        connection.commit();
//        expectLastCall();
//
//        statement.close();
//        expectLastCall();
//
//        tableResultSet.close();
//        expectLastCall();
//
//        replay(connection);
//        replay(metaData);
//        replay(tableResultSet);
//        replay(statement);
//
//        AbstractDatabase database = getDatabase();
//        database.setConnection(connection);
//        Migrator migrator = new Migrator(null, null);
//        migrator.init(getMockConnection());
//        database.checkDatabaseChangeLogTable(migrator);
//
//
//        //test if does exist
//        reset(connection);
//        reset(metaData);
//        reset(tableResultSet);
//        reset(statement);
//
//        connection = createMock(Connection.class);
//        metaData = createMock(DatabaseMetaData.class);
//
//        expect(connection.getMetaData()).andReturn(metaData).atLeastOnce();
//        expect(metaData.getUserName()).andReturn("testSchema").atLeastOnce();
//
//        tableResultSet = createMock(ResultSet.class);
//        expect(metaData.getTables(null, "testSchema", "DATABASECHANGELOG", null)).andReturn(tableResultSet);
//
//        expect(tableResultSet.next()).andReturn(Boolean.TRUE);
//
//        statement.close();
//        expectLastCall();
//
//        tableResultSet.close();
//        expectLastCall();
//
//        replay(connection);
//        replay(metaData);
//        replay(tableResultSet);
//        replay(statement);
//
//        database = getDatabase();
//        database.setConnection(connection);
//        migrator = new Migrator(null, null);
//        migrator.init(getMockConnection());
//
//    }

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

    protected abstract String getProductNameString();

}
