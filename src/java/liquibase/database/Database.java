package liquibase.database;

import liquibase.migrator.DatabaseChangeLogLock;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.Migrator;
import liquibase.migrator.change.ColumnConfig;

import java.io.IOException;
import java.sql.Connection;

public interface Database {
    /**
     * Is this AbstractDatabase subclass the correct one to use for the given connection.
     */
    boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException;

    Connection getConnection();

    void setConnection(Connection conn);

    String getDatabaseProductName();

    /**
     * Returns the full database product name.  May be different than what the JDBC connection reports (getDatabaseProductName())
     */
    String getProductName();

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    String getTypeName();

    String getDriverName() throws JDBCException;

    String getConnectionURL() throws JDBCException;

    String getConnectionUsername() throws JDBCException;

    String getCatalogName() throws JDBCException;

    String getSchemaName() throws JDBCException;

    /**
     * Returns whether this database support initially deferrable columns.
     */
    boolean supportsInitiallyDeferrableColumns();

    String getColumnType(ColumnConfig column);

    String getFalseBooleanValue();

    String getTrueBooleanValue();

    /**
     * Returns database-specific function for generating the current date/time.
     */
    String getCurrentDateTimeFunction();

    String getLineComment();

    String getAutoIncrementClause();

    String getCommitSQL();

    String getDatabaseChangeLogTableName();

    String getDatabaseChangeLogLockTableName();

    boolean acquireLock(Migrator migrator) throws MigrationFailedException;

    void releaseLock() throws MigrationFailedException;

    DatabaseChangeLogLock[] listLocks() throws MigrationFailedException;

    boolean doesChangeLogTableExist();

    boolean doesChangeLogLockTableExist();

    void checkDatabaseChangeLogTable(Migrator migrator) throws JDBCException, IOException;

    void checkDatabaseChangeLogLockTable(Migrator migrator) throws JDBCException, IOException;

    void dropDatabaseObjects() throws JDBCException, MigrationFailedException;

    String getDropTableSQL(String tableName);

    void tag(String tagString) throws MigrationFailedException;

    boolean doesTagExist(String tag) throws JDBCException;
}
