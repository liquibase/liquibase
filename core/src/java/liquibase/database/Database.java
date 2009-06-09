package liquibase.database;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public interface Database extends DatabaseObject {

	String databaseChangeLogTableName = "DatabaseChangeLog".toUpperCase();
	String databaseChangeLogLockTableName = "DatabaseChangeLogLock".toUpperCase();

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

     int getDatabaseMajorVersion() throws JDBCException;

    int getDatabaseMinorVersion() throws JDBCException;

    /**
     * Returns an all-lower-case short name of the product.  Used for end-user selecting of database type
     * such as the DBMS precondition.
     */
    String getTypeName();

    String getDriverName() throws JDBCException;

    String getConnectionURL() throws JDBCException;

    String getConnectionUsername() throws JDBCException;

    String getDefaultCatalogName() throws JDBCException;

    String getDefaultSchemaName();

    String getLiquibaseSchemaName();
    
    boolean isPeculiarLiquibaseSchema(); 
    	
    void setDefaultSchemaName(String schemaName) throws JDBCException;

    /**
     * Returns whether this database support initially deferrable columns.
     */
    boolean supportsInitiallyDeferrableColumns();

    public boolean supportsSequences();

    public boolean supportsAutoIncrement();

    String getColumnType(String columnType, Boolean autoIncrement);

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

    boolean doesChangeLogTableExist() throws JDBCException;

    boolean doesChangeLogLockTableExist() throws JDBCException;

    void checkDatabaseChangeLogTable() throws JDBCException;

    void checkDatabaseChangeLogLockTable() throws JDBCException;

    void dropDatabaseObjects(String schema) throws JDBCException;

    void tag(String tagString) throws JDBCException;

    boolean doesTagExist(String tag) throws JDBCException;

    boolean isSystemTable(String catalogName, String schemaName, String tableName);

    boolean isLiquibaseTable(String tableName);

    boolean shouldQuoteValue(String value);

    boolean supportsTablespaces();

    String getViewDefinition(String schemaName, String name) throws JDBCException;

    int getDatabaseType(int type);

    String getDatabaseProductName(Connection conn) throws JDBCException;

    /**
     * Returns the actual database-specific data type to use for a "char" column.
     */
    DataType getCharType();

    /**
     * Returns the actual database-specific data type to use for a "varchar" column.
     */
    DataType getVarcharType();

    /**
     * Returns the actual database-specific data type to use a "boolean" column.
     */
    DataType getBooleanType();

    /**
     * Returns the actual database-specific data type to use a "currency" column.
     */
    DataType getCurrencyType();

    /**
     * Returns the actual database-specific data type to use a "UUID" column.
     */
    DataType getUUIDType();

    /**
     * Returns the actual database-specific data type to use a "CLOB" column.
     */
    DataType getClobType();

    /**
     * Returns the actual database-specific data type to use a "BLOB" column.
     */
    DataType getBlobType();

    DataType getDateType();

    /**
     * Returns the actual database-specific data type to use for a "float" column.
     *
     * @return database-specific type for float
     */
    DataType getFloatType();

    /**
     * Returns the actual database-specific data type to use for a "double" column.
     *
     * @return database-specific type for double
     */
    DataType getDoubleType();

    /**
     * Returns the actual database-specific data type to use for a "int" column.
     *
     * @return database-specific type for int
     */
    DataType getIntType();

    /**
     * Returns the actual database-specific data type to use for a "tinyint" column.
     *
     * @return database-specific type for tinyint
     */
    DataType getTinyIntType();

    /**
     * Returns the actual database-specific data type to use a "datetime" column.
     */
    DataType getDateTimeType();

    DataType getTimeType();

    DataType getBigIntType();

    Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException;

    String convertJavaObjectToString(Object value);

    boolean isSystemView(String catalogName, String schemaName, String name);

    String getDateLiteral(java.sql.Date date);

    String getDateLiteral(java.sql.Time time);

    String getDateLiteral(java.sql.Timestamp timeStamp);

    String getDateLiteral(Date defaultDateValue);

    /**
     * Escapes the table name in a database-dependent manner so reserved words can be used as a table name (i.e. "order").
     * Currently only escapes MS-SQL because other DBMSs store table names case-sensitively when escaping is used which
     * could confuse end-users.  Pass null to schemaName to use the default schema
     */
    String escapeTableName(String schemaName, String tableName);

    String escapeIndexName(String schemaName, String indexName);

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
    String escapeColumnName(String schemaName, String tableName, String columnName);

    /**
     * Escapes a list of column names in a database-dependent manner so reserved words can be used as a column
     * name (i.e. "return").
     *
     * @param columnNames list of column names
     * @return escaped column name list
     */
    String escapeColumnNameList(String columnNames);

//    Set<UniqueConstraint> findUniqueConstraints(String schema) throws JDBCException;

    String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException;

    String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException;

    boolean supportsSchemas();

    String generatePrimaryKeyName(String tableName);

    String escapeSequenceName(String schemaName, String sequenceName);

    String escapeViewName(String schemaName, String viewName);

    boolean isColumnAutoIncrement(String schemaName, String tableName, String columnName) throws SQLException, JDBCException;

    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException;

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException;

    void markChangeSetAsRan(ChangeSet changeSet) throws JDBCException;

    void markChangeSetAsReRan(ChangeSet changeSet) throws JDBCException;

    List<RanChangeSet> getRanChangeSetList() throws JDBCException;

    Date getRanDate(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException;

    void removeRanStatus(ChangeSet changeSet) throws JDBCException;

    void commit() throws JDBCException;

    void rollback() throws JDBCException;

    String escapeStringForDatabase(String string);

    void close() throws JDBCException;

    DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException;

    boolean supportsRestrictForeignKeys();

    String escapeConstraintName(String constraintName);

    boolean isAutoCommit() throws JDBCException;

    void setAutoCommit(boolean b) throws JDBCException;
    
    boolean isLocalDatabase() throws JDBCException;

    void executeStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException;/*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws JDBCException if there were problems issuing the statements
     */

    void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, StatementNotSupportedOnDatabaseException, LiquibaseException;

    void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException, RollbackImpossibleException;

    void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException;
}
