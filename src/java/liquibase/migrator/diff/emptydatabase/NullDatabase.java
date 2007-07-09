package liquibase.migrator.diff.emptydatabase;

import liquibase.database.Database;
import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.Migrator;
import liquibase.migrator.change.ColumnConfig;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.io.IOException;
import java.sql.Connection;

public class NullDatabase implements Database {

    private Connection connection;

    public NullDatabase() {
        this.connection = new NullConnection();
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return false;
    }

    public String getDefaultDriver(String url) {
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection conn) {
        ;
    }

    public String getDatabaseProductName() {
        return "Blank Database";
    }

    public String getProductName() {
        return getDatabaseProductName();
    }


    public String getDatabaseProductVersion() throws JDBCException {
        return "-1";
    }

    public String getTypeName() {
        return getDatabaseProductName();
    }

    public String getDriverName() throws JDBCException {
        return null;
    }

    public String getConnectionURL() throws JDBCException {
        return null;
    }

    public String getConnectionUsername() throws JDBCException {
        return null;
    }

    public String getCatalogName() throws JDBCException {
        return null;
    }

    public String getSchemaName() throws JDBCException {
        return null;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsSequences() {
        return false;
    }

    public String getColumnType(ColumnConfig column) {
        return null;
    }

    public String getFalseBooleanValue() {
        return null;
    }

    public String getTrueBooleanValue() {
        return null;
    }

    public String getDateLiteral(String isoDate) {
        return null;
    }

    public String getCurrentDateTimeFunction() {
        return null;
    }

    public void setCurrentDateTimeFunction(String function) {

    }

    public String getLineComment() {
        return null;
    }

    public String getAutoIncrementClause() {
        return null;
    }

    public String getCommitSQL() {
        return null;
    }

    public String getDatabaseChangeLogTableName() {
        return null;
    }

    public String getDatabaseChangeLogLockTableName() {
        return null;
    }

    public String getConcatSql(String... values) {
        return null;
    }

    public boolean acquireLock(Migrator migrator) throws MigrationFailedException {
        return false;
    }

    public void releaseLock() throws MigrationFailedException {

    }

    public DatabaseChangeLogLock[] listLocks() throws MigrationFailedException {
        return new DatabaseChangeLogLock[0];
    }

    public boolean doesChangeLogTableExist() {
        return false;
    }

    public boolean doesChangeLogLockTableExist() {
        return false;
    }

    public void checkDatabaseChangeLogTable(Migrator migrator) throws JDBCException, IOException {
        ;
    }

    public void checkDatabaseChangeLogLockTable(Migrator migrator) throws JDBCException, IOException {
        ;
    }

    public void dropDatabaseObjects() throws JDBCException, MigrationFailedException {
        ;
    }

    public String getDropTableSQL(String tableName) {
        return null;
    }

    public void tag(String tagString) throws MigrationFailedException {
        ;
    }

    public boolean doesTagExist(String tag) throws JDBCException {
        return false;
    }

    public boolean isSystemTable(String tableName) {
        return false;
    }

    public boolean isLiquibaseTable(String tableName) {
        return false;
    }

}