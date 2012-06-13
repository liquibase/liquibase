package liquibase.database;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.DateParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;

public interface Database extends DatabaseObject, PrioritizedService {

	String databaseChangeLogTableName = "DatabaseChangeLog".toUpperCase();
	String databaseChangeLogLockTableName = "DatabaseChangeLogLock".toUpperCase();

	/**
     * Is this AbstractDatabase subclass the correct one to use for the given connection.
     */
    boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException;

    /**
     * If this database understands the given url, return the default driver class name.  Otherwise return null.
     */
    String getDefaultDriver(String url);

    DatabaseConnection getConnection();

    void setConnection(DatabaseConnection conn);

    boolean requiresUsername();

    boolean requiresPassword();
    
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

    String getDatabaseProductVersion() throws DatabaseException;

     int getDatabaseMajorVersion() throws DatabaseException;

    int getDatabaseMinorVersion() throws DatabaseException;

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    String getTypeName();

    String getDefaultCatalogName();

    String getDefaultSchemaName();

    String getLiquibaseCatalogName();
    
    String getLiquibaseSchemaName();
    
    void setDefaultSchemaName(String schemaName) throws DatabaseException;

    /**
     * Returns whether this database support initially deferrable columns.
     */
    boolean supportsInitiallyDeferrableColumns();

    public boolean supportsSequences();

    public boolean supportsDropTableCascadeConstraints();
    
    public boolean supportsAutoIncrement();

    String getDateLiteral(String isoDate);

    /**
     * Returns database-specific function for generating the current date/time.
     */
    String getCurrentDateTimeFunction();

    void setCurrentDateTimeFunction(String function);


    String getLineComment();

    String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy);

    String getDatabaseChangeLogTableName();

    String getDatabaseChangeLogLockTableName();
    
    /**
     * Set the table name of the change log to the given table name
     * 
     * @param tableName
     */
    public void setDatabaseChangeLogTableName(String tableName);
    
    /**
     * Set the table name of the change log lock to the given table name
     * 
     * @param tableName
     */
    public void setDatabaseChangeLogLockTableName(String tableName);

    /**
     * Returns SQL to concat the passed values.
     */
    String getConcatSql(String ... values);

    boolean hasDatabaseChangeLogTable() throws DatabaseException;

    public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo);
    
    boolean hasDatabaseChangeLogLockTable() throws DatabaseException;

    void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String[] contexts) throws DatabaseException;

    void checkDatabaseChangeLogLockTable() throws DatabaseException;

    void dropDatabaseObjects(Schema schema) throws DatabaseException;

    void tag(String tagString) throws DatabaseException;

    boolean doesTagExist(String tag) throws DatabaseException;

    boolean isSystemTable(Schema schema, String tableName);

    boolean isLiquibaseTable(String tableName);

    boolean shouldQuoteValue(String value);

    boolean supportsTablespaces();

    String getViewDefinition(Schema schema, String name) throws DatabaseException;

    boolean isSystemView(Schema schema, String name);

    String getDateLiteral(java.sql.Date date);

    String getTimeLiteral(java.sql.Time time);

    String getDateTimeLiteral(java.sql.Timestamp timeStamp);

    String getDateLiteral(Date defaultDateValue);

    /**
     * Escapes the table name in a database-dependent manner so reserved words can be used as a table name (i.e. "order").
     * Currently only escapes MS-SQL because other DBMSs store table names case-sensitively when escaping is used which
     * could confuse end-users.  Pass null to schemaName to use the default schema
     */
    String escapeTableName(String catalogName, String schemaName, String tableName);

    String escapeIndexName(String catalogName, String schemaName, String indexName);

    String escapeDatabaseObject(String objectName);

    /**
     * Escapes a single column name in a database-dependent manner so reserved words can be used as a column
     * name (i.e. "return"). 
     * @param schemaName
     * @param tableName 
     * @param columnName column name
     *
     * @return escaped column name
     */
    String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName);

    /**
     * Escapes a list of column names in a database-dependent manner so reserved words can be used as a column
     * name (i.e. "return").
     *
     * @param columnNames list of column names
     * @return escaped column name list
     */
    String escapeColumnNameList(String columnNames);

//    Set<UniqueConstraint> findUniqueConstraints(String schema) throws DatabaseException;

    boolean supportsSchemas();

    boolean supportsCatalogs();

    String generatePrimaryKeyName(String tableName);

    String escapeSequenceName(String catalogName, String schemaName, String sequenceName);

    String escapeViewName(String catalogName, String schemaName, String viewName);

    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException;

    List<RanChangeSet> getRanChangeSetList() throws DatabaseException;

    Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    void removeRanStatus(ChangeSet changeSet) throws DatabaseException;

    void commit() throws DatabaseException;

    void rollback() throws DatabaseException;

    String escapeStringForDatabase(String string);

    void close() throws DatabaseException;

    boolean supportsRestrictForeignKeys();

    String escapeConstraintName(String constraintName);

    boolean isAutoCommit() throws DatabaseException;

    void setAutoCommit(boolean b) throws DatabaseException;
    
    boolean isLocalDatabase() throws DatabaseException;

    void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException;/*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws DatabaseException if there were problems issuing the statements
     */

    void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, StatementNotSupportedOnDatabaseException, LiquibaseException;

    void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException, RollbackImpossibleException;

    void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException;

    int getNextChangeSetSequenceValue() throws LiquibaseException;

    public Date parseDate(String dateAsString) throws DateParseException;

	/**
	 * Returns list of database native functions
	 * */
	public List<DatabaseFunction> getDatabaseFunctions();

    void reset();

    boolean supportsForeignKeyDisable();

    boolean disableForeignKeyChecks() throws DatabaseException;

    void enableForeignKeyChecks() throws DatabaseException;

    public boolean isReservedWord(String string);

    String correctCatalogName(String catalogName);

    String correctSchemaName(String schemaName);
    
    String correctTableName(String tableName);

    String correctConstraintName(String constraintName);

    String correctColumnName(String columnName);

    String correctPrimaryKeyName(String pkName);

    String correctForeignKeyName(String fkName);
    
    String correctIndexName(String indexName);
        
    /**
     * Gets the database specific datatype factory
     * @return A database vendor specific datatype factory.
     */
    DataTypeFactory getDataTypeFactory();
}
