package liquibase.database;

import liquibase.change.core.CreateTableChange;
import liquibase.executor.ExecutorService;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sql.visitor.AppendSqlVisitor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Base test class for database-specific tests
 */
public abstract class AbstractJdbcDatabaseTest {

    protected AbstractJdbcDatabase database;

    protected AbstractJdbcDatabaseTest(AbstractJdbcDatabase database) throws Exception {
        this.database = database;
    }

    public AbstractJdbcDatabase getDatabase() {
        return database;
    }

    protected abstract String getProductNameString();

    public abstract void supportsInitiallyDeferrableColumns();

    public abstract void getCurrentDateTimeFunction();

//    @Test
//    public void onlyAdjustAutoCommitOnMismatch() throws Exception {
//        // Train expectations for setConnection(). If getAutoCommit() returns the same value the Database wants, based
//        // on getAutoCommitMode(), it should _not_ call setAutoCommit(boolean) on the connection
//        DatabaseConnection connection = createStrictMock(DatabaseConnection.class);
//        expect(connection.getConnectionUserName()).andReturn("user").anyTimes();
//        expect(connection.getURL()).andReturn("URL");
//        expect(connection.getAutoCommit()).andReturn(getDatabase().getAutoCommitMode());
//        replay(connection);
//        getDatabase().setConnection(connection);
//        verify(connection);
//
//        // Reset the mock and train expectations for close(). Since the auto-commit mode was not adjusted while setting
//        // the connection, it should not be adjusted on close() either
//        reset(connection);
//        connection.close();
//        replay(connection);
//
//        getDatabase().close();
//        verify(connection);
//    }


    @Test
    public void defaultsWorkWithoutAConnection() {
        database.getDatabaseProductName();
        database.getDefaultCatalogName();
        database.getDefaultSchemaName();
        database.getDefaultPort();
    }
//    @Test
//    public void isCorrectDatabaseImplementation() throws Exception {
//        assertTrue(getDatabase().isCorrectDatabaseImplementation(getMockConnection()));
//    }

//    protected DatabaseConnection getMockConnection() throws Exception {
//        DatabaseConnection conn = createMock(DatabaseConnection.class);
////        DatabaseMetaData metaData = createMock(DatabaseMetaData.class);
//        conn.setAutoCommit(false);
//
//        expectLastCall().anyTimes();
////        expect(((JdbcConnection) conn).getUnderlyingConnection().getMetaData()).andReturn(metaData).anyTimes();
//        expect(conn.getDatabaseProductName()).andReturn(getProductNameString()).anyTimes();
//        replay(conn);
////        replay(metaData);
//        return conn;
//    }

    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        if (database.supportsCatalogInObjectName(Table.class)) {
            assertEquals("catalogName.schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
        } else {
            assertEquals("schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
        }
    }

    @Test
    public void executeRollbackStatements_WithStatementsOverload_ShouldNotIncludeAppendTextFromApplyToRollbackFalseVisitor() throws Exception {
        Database database = getDatabase();

        final MockExecutor mockExecutor = new MockExecutor();

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        final List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

        final SqlStatement dropTableStatement = new DropTableStatement(null, null, "test_table", false);

        final AppendSqlVisitor appendSqlVisitor = new AppendSqlVisitor();
        appendSqlVisitor.setApplyToRollback(false);
        appendSqlVisitor.setValue(" SHOULD NOT BE APPENDED");

        sqlVisitors.add(appendSqlVisitor);

        database.executeRollbackStatements(new SqlStatement[] {dropTableStatement}, sqlVisitors);

        assertEquals("DROP TABLE test_table;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void executeRollbackStatements_WithStatementsOverload_ShouldIncludeAppendTextFromApplyToRollbackTrueVisitor() throws Exception {
        Database database = getDatabase();

        final MockExecutor mockExecutor = new MockExecutor();

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        final List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

        final SqlStatement dropTableStatement = new DropTableStatement(null, null, "test_table", false);

        final AppendSqlVisitor appendSqlVisitor = new AppendSqlVisitor();

        appendSqlVisitor.setApplyToRollback(true);
        appendSqlVisitor.setValue(" SHOULD BE APPENDED");

        sqlVisitors.add(appendSqlVisitor);

        database.executeRollbackStatements(new SqlStatement[] {dropTableStatement}, sqlVisitors);

        assertEquals("DROP TABLE test_table SHOULD BE APPENDED;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void executeRollbackStatements_WithChangeOverload_ShouldNotIncludeAppendTextFromApplyToRollbackFalseVisitor() throws Exception {
        Database database = getDatabase();

        final MockExecutor mockExecutor = new MockExecutor();

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        final List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

        final CreateTableChange change = new CreateTableChange();
        change.setTableName("test_table");

        final AppendSqlVisitor appendSqlVisitor = new AppendSqlVisitor();
        appendSqlVisitor.setApplyToRollback(false);
        appendSqlVisitor.setValue(" SHOULD NOT BE APPENDED");

        sqlVisitors.add(appendSqlVisitor);

        database.executeRollbackStatements(change, sqlVisitors);

        assertEquals("DROP TABLE test_table;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void executeRollbackStatements_WithChangeOverload_ShouldIncludeAppendTextFromApplyToRollbackTrueVisitor() throws Exception {
        Database database = getDatabase();

        final MockExecutor mockExecutor = new MockExecutor();

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        final List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

        final CreateTableChange change = new CreateTableChange();
        change.setTableName("test_table");

        final AppendSqlVisitor appendSqlVisitor = new AppendSqlVisitor();

        appendSqlVisitor.setApplyToRollback(true);
        appendSqlVisitor.setValue(" SHOULD BE APPENDED");

        sqlVisitors.add(appendSqlVisitor);

        database.executeRollbackStatements(change, sqlVisitors);

        assertEquals("DROP TABLE test_table SHOULD BE APPENDED;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void test_isDateOnly() {
        assertTrue(database.isDateOnly("2018-01-01"));
        assertFalse(database.isDateOnly("18-01-01"));
        assertFalse(database.isDateOnly("2018-1-1"));
        assertTrue(database.isDateOnly("now"));
        assertTrue(database.isDateOnly("now+1year"));
        assertTrue(database.isDateOnly("now+1day"));
        assertTrue(database.isDateOnly("now-11hours"));
        assertTrue(database.isDateOnly("now+111minutes"));
        assertTrue(database.isDateOnly("today"));
        assertTrue(database.isDateOnly("today+2"));
        assertTrue(database.isDateOnly("today-1"));
        assertTrue(database.isDateOnly("TODAY"));
        assertFalse(database.isDateOnly("NO"));
        assertFalse(database.isDateOnly("TODA"));
    }

    @Test
    public void test_isDateTime() {
        assertTrue(database.isDateTime("2018-01-01 10:11:12"));
        assertTrue(database.isDateTime("2018-01-01 10:11:12.2"));
        assertTrue(database.isDateTime("2018-01-01T10:11:12"));
        assertTrue(database.isDateTime("2018-01-01T10:11:12.2"));
        assertFalse(database.isDateTime("18-01-01T10:11:12.2"));
        assertFalse(database.isDateTime("2018-01-01"));
        assertTrue(database.isDateTime("now"));
        assertTrue(database.isDateTime("now+1year"));
        assertTrue(database.isDateTime("now+1day"));
        assertTrue(database.isDateTime("now-11hours"));
        assertTrue(database.isDateTime("now+111minutes"));
        assertTrue(database.isDateTime("today"));
        assertTrue(database.isDateTime("today+2"));
        assertTrue(database.isDateTime("today-2"));
        assertTrue(database.isDateTime("TODAY"));
        assertFalse(database.isDateTime("NO"));
        assertFalse(database.isDateTime("TODA"));
    }

    @Test
    public void test_isTimestamp() {
        assertTrue(database.isTimestamp("2018-01-01T10:11:12.2"));
        assertFalse(database.isTimestamp("2018-01-01T10:11:12"));
        assertFalse(database.isTimestamp("2018-01-01 10:11:12.2"));
        assertFalse(database.isTimestamp("18-01-01T10:11:12.2"));
        assertFalse(database.isTimestamp("2018-01-01"));
        assertTrue(database.isTimestamp("now"));
        assertTrue(database.isTimestamp("now+1year"));
        assertTrue(database.isTimestamp("now+1day"));
        assertTrue(database.isTimestamp("now-11hours"));
        assertTrue(database.isTimestamp("now+111minutes"));
        assertTrue(database.isTimestamp("today"));
        assertTrue(database.isTimestamp("today+2"));
        assertTrue(database.isTimestamp("today-2"));
        assertTrue(database.isTimestamp("TODAY"));
        assertFalse(database.isTimestamp("NO"));
        assertFalse(database.isTimestamp("TODA"));
    }

    @Test
    public void test_isTimeOnly() {
        assertTrue(database.isTimeOnly("10:11:12"));
        assertFalse(database.isTimeOnly("2018-01-01 10:11:12"));
        assertFalse(database.isTimeOnly("2018-01-01T10:11:12"));
        assertFalse(database.isTimeOnly("10:11:12.2"));
        assertTrue(database.isTimeOnly("now"));
        assertTrue(database.isTimeOnly("now+1year"));
        assertTrue(database.isTimeOnly("now+1day"));
        assertTrue(database.isTimeOnly("now-11hours"));
        assertTrue(database.isTimeOnly("now+111minutes"));
        assertTrue(database.isTimeOnly("today"));
        assertTrue(database.isTimeOnly("today+2"));
        assertTrue(database.isTimeOnly("today-2"));
        assertTrue(database.isTimeOnly("TODAY"));
        assertFalse(database.isTimeOnly("NO"));
        assertFalse(database.isTimeOnly("TODA"));
    }

//    @Test
//    public void getColumnType_javaTypes() throws SQLException {
//        Database database = getDatabase();
//        DatabaseConnection connection = database.getConnection();
//        if (connection != null) {
//            ((JdbcConnection) connection).getUnderlyingConnection().rollback();
//            assertEquals(database.getDateType().getDataTypeName().toUpperCase(), database.getDataType("java.sql.Types.DATE", false).toUpperCase());
//            assertEquals(database.getBooleanType().getDataTypeName().toUpperCase(), database.getDataType("java.sql.Types.BOOLEAN", false).toUpperCase());
//            assertEquals("VARCHAR(255)", database.getDataType("java.sql.Types.VARCHAR(255)", false).toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
//        }
//    }
}
