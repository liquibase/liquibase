package liquibase.database;

import liquibase.change.ColumnConfig;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Base test class for database-specific tests
 */
public abstract class AbstractDatabaseTest {

    protected AbstractDatabase database;

    protected AbstractDatabaseTest(AbstractDatabase database) throws Exception {
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
        assertEquals(database.getBooleanType().getDataTypeName(), database.getColumnType(column));
        column.setType("BooLean");
        assertEquals(database.getBooleanType().getDataTypeName(), database.getColumnType(column));


        column.setType("currency");
        assertEquals(database.getCurrencyType().getDataTypeName(), database.getColumnType(column));
        column.setType("currEncy");
        assertEquals(database.getCurrencyType().getDataTypeName(), database.getColumnType(column));

        column.setType("uuid");
        assertEquals(database.getUUIDType().getDataTypeName(), database.getColumnType(column));
        column.setType("UUID");
        assertEquals(database.getUUIDType().getDataTypeName(), database.getColumnType(column));

        column.setType("blob");
        assertEquals(database.getBlobType().getDataTypeName(), database.getColumnType(column));
        column.setType("BLOB");
        assertEquals(database.getBlobType().getDataTypeName(), database.getColumnType(column));

        column.setType("clob");
        assertEquals(database.getClobType().getDataTypeName(), database.getColumnType(column));
        column.setType("CLOB");
        assertEquals(database.getClobType().getDataTypeName(), database.getColumnType(column));

        column.setType("SomethingElse");
        assertEquals("SomethingElse", database.getColumnType(column));
    }

    @Test
    public void isCorrectDatabaseImplementation() throws Exception {
        assertTrue(getDatabase().isCorrectDatabaseImplementation(getMockConnection()));
    }

    protected DatabaseConnection getMockConnection() throws Exception {
        DatabaseConnection conn = createMock(DatabaseConnection.class);
//        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
        conn.setAutoCommit(false);

        expectLastCall().anyTimes();
//        expect(((JdbcConnection) conn).getUnderlyingConnection().getMetaData()).andReturn(metaData).anyTimes();
        expect(conn.getDatabaseProductName()).andReturn(getProductNameString()).anyTimes();
        replay(conn);
//        replay(metaData);
        return conn;
    }

    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, "tableName"));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("schemaName.tableName", database.escapeTableName("schemaName", "tableName"));
    }

//    @Test
//    public void getColumnType_javaTypes() throws SQLException {
//        Database database = getDatabase();
//        DatabaseConnection connection = database.getConnection();
//        if (connection != null) {
//            ((JdbcConnection) connection).getUnderlyingConnection().rollback();
//            assertEquals(database.getDateType().getDataTypeName().toUpperCase(), database.getColumnType("java.sql.Types.DATE", false).toUpperCase());
//            assertEquals(database.getBooleanType().getDataTypeName().toUpperCase(), database.getColumnType("java.sql.Types.BOOLEAN", false).toUpperCase());
//            assertEquals("VARCHAR(255)", database.getColumnType("java.sql.Types.VARCHAR(255)", false).toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
//        }
//    }
}
