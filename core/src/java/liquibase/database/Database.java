package liquibase.database;

import liquibase.DatabaseChangeLogLock;
import liquibase.database.sql.SqlStatement;
import liquibase.migrator.Migrator;
import liquibase.change.ColumnConfig;
import liquibase.exception.JDBCException;
import liquibase.exception.LockException;

import java.io.IOException;
import java.sql.Connection;

public interface Database {
    /**
     * Is this AbstractDatabase subclass the correct one to use for the given connection.
     */
    boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException;

    /**
     * If this database understands the given url, return the default driver class name.  Otherwise return null.
     */
    String getDefaultDriver(String url);

    DatabaseConnection getConnection();

    void setConnection(Connection conn);
    
    void setConnection(DatabaseConnection conn);
    
    /**
     * Auto-commit mode to run in
     */
    public boolean getAutoCommitMode();

    /**
     * Determines if the database supports DDL within a transaction or not.
     * 
     * @return True if the database supports DDL within a transaction, otherwise false.
     */
    boolean supportsDDLInTransaction();

    String getDatabaseProductName();

    String getDatabaseProductVersion() throws JDBCException;

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

    public boolean supportsSequences();

    String getColumnType(ColumnConfig column);

    String getFalseBooleanValue();

    String getTrueBooleanValue();

    String getDateLiteral(String isoDate);

    /**
     * Returns database-specific function for generating the current date/time.
     */
    String getCurrentDateTimeFunction();

    void setCurrentDateTimeFunction(String function);


    String getLineComment();

    String getAutoIncrementClause();

    SqlStatement getCommitSQL();

    String getDatabaseChangeLogTableName();

    String getDatabaseChangeLogLockTableName();

    /**
     * Returns SQL to concat the passed values.
     */
    String getConcatSql(String ... values);

    boolean acquireLock(Migrator migrator) throws LockException;

    void releaseLock() throws LockException;

    DatabaseChangeLogLock[] listLocks() throws LockException;

    boolean doesChangeLogTableExist();

    boolean doesChangeLogLockTableExist();

    void checkDatabaseChangeLogTable(Migrator migrator) throws JDBCException, IOException;

    void checkDatabaseChangeLogLockTable(Migrator migrator) throws JDBCException, IOException;

    void dropDatabaseObjects() throws JDBCException;

    void tag(String tagString) throws JDBCException;

    boolean doesTagExist(String tag) throws JDBCException;

    boolean isSystemTable(String catalogName, String schemaName, String tableName);

    boolean isLiquibaseTable(String tableName);

    SqlStatement createFindSequencesSQL() throws JDBCException;

    boolean shouldQuoteValue(String value);

    boolean supportsTablespaces();

    String getViewDefinition(String name) throws JDBCException;

    int getDatabaseType(int type);
}
