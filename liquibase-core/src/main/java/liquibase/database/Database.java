package liquibase.database;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.structure.DatabaseObject;
import liquibase.exception.*;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Catalog;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Database extends PrioritizedService {

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
    boolean getAutoCommitMode();

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
    String getShortName();

    String getDefaultCatalogName();

    void setDefaultCatalogName(String catalogName) throws DatabaseException;

    String getDefaultSchemaName();

    void setDefaultSchemaName(String schemaName) throws DatabaseException;

    Integer getDefaultPort();

    String getLiquibaseCatalogName();

    void setLiquibaseCatalogName(String catalogName);

    String getLiquibaseSchemaName();

    void setLiquibaseSchemaName(String schemaName);

    /**
     * Returns whether this database support initially deferrable columns.
     */
    boolean supportsInitiallyDeferrableColumns();

    boolean supportsSequences();

    boolean supportsAutoIncrement();

    String getDateLiteral(String isoDate);

    /**
     * Returns database-specific function for generating the current date/time.
     */
    String getCurrentDateTimeFunction();

    void setCurrentDateTimeFunction(String function);

    String getLineComment();

    String getDatabaseChangeLogTableName();

    String getDatabaseChangeLogLockTableName();

    String getLiquibaseTablespaceName();

    void setLiquibaseTablespaceName(String tablespaceName);

    /**
     * Set the table name of the change log to the given table name
     *
     * @param tableName
     */
    void setDatabaseChangeLogTableName(String tableName);

    /**
     * Set the table name of the change log lock to the given table name
     *
     * @param tableName
     */
    void setDatabaseChangeLogLockTableName(String tableName);

    /**
     * Returns SQL to concat the passed values.
     */
    String getConcatSql(String... values);

    void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo);

    void dropDatabaseObjects(CatalogAndSchema schema) throws LiquibaseException;

    void tag(String tagString) throws DatabaseException;

    boolean doesTagExist(String tag) throws DatabaseException;

    boolean isSystemObject(ObjectReference example);

    boolean isLiquibaseObject(ObjectReference object);

    String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException;

    String getDateLiteral(java.sql.Date date);

    String getTimeLiteral(java.sql.Time time);

    String getDateTimeLiteral(java.sql.Timestamp timeStamp);

    String getDateLiteral(Date defaultDateValue);

    String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType);

    String escapeObjectName(ObjectReference objectReference);

    boolean supportsTablespaces();

    String generatePrimaryKeyName(String tableName);

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

    boolean isAutoCommit() throws DatabaseException;

    void setAutoCommit(boolean b) throws DatabaseException;

    boolean isSafeToRunUpdate() throws DatabaseException;

    void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException;/*

     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws DatabaseException if there were problems issuing the statements
     */
    void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, StatementNotSupportedOnDatabaseException, LiquibaseException;

    void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException;

    void executeRollbackStatements(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException;

    void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException;

    Date parseDate(String dateAsString) throws DateParseException;

    /**
     * Returns list of database native date functions
     */
    List<DatabaseFunction> getDateFunctions();

    void resetInternalState();

    boolean supportsForeignKeyDisable();

    boolean disableForeignKeyChecks() throws DatabaseException;

    void enableForeignKeyChecks() throws DatabaseException;

    boolean isCaseSensitive(Class<? extends DatabaseObject> type);

    /**
     * Return true if the database is able to store the given name as is.
     */
    boolean canStoreObjectName(String name, Class<? extends DatabaseObject> type);

    boolean canStoreObjectName(String name, boolean quoted, Class<? extends DatabaseObject> type);

    boolean isReservedWord(String string);

    /**
     * Returns a new CatalogAndSchema adjusted for this database. Examples of adjustments include:
     * fixes for case issues,
     * replacing null schema or catalog names with the default values
     * removing set schema or catalog names if they are not supported
     * @deprecated use {@link liquibase.CatalogAndSchema#standardize(Database)}
     */
    CatalogAndSchema correctSchema(CatalogAndSchema schema);

    /**
     * Fix the object name to the format the database expects, handling changes in case, etc.
     */
    String correctObjectName(String name, Class<? extends DatabaseObject> objectType);

    boolean isFunction(String string);

    int getDataTypeMaxParameters(String dataTypeName);

    CatalogAndSchema getDefaultSchema();

    /**
     * Types like int4 in postgres cannot have a modifier. e.g. int4(10)
     * Checks whether the type is allowed to have a modifier
     *
     * @param typeName type name
     * @return Whether data type can have a modifier
     */
    boolean dataTypeIsNotModifiable(String typeName);

    /**
     * Some function names are placeholders that need to be replaced with the specific database value.
     * e.g. nextSequenceValue(sequenceName) should be replaced with NEXT_VAL('sequenceName') for Postgresql
     * @param databaseFunction database function to check.
     * @return the string value to use for an update or generate
     */
    String generateDatabaseFunctionValue(DatabaseFunction databaseFunction);

    void setObjectQuotingStrategy(ObjectQuotingStrategy quotingStrategy);

    ObjectQuotingStrategy getObjectQuotingStrategy();

    boolean createsIndexesForForeignKeys();

    /**
     * Whether the default schema should be included in generated SQL
     */
    void setOutputDefaultSchema(boolean outputDefaultSchema);

    boolean getOutputDefaultSchema();

    boolean isDefaultSchema(String catalog, String schema);

    boolean isDefaultCatalog(String catalog);

    boolean getOutputDefaultCatalog();

    void setOutputDefaultCatalog(boolean outputDefaultCatalog);

    boolean supportsPrimaryKeyNames();

    String getSystemSchema();

    void addReservedWords(Collection<String> words);

    String escapeDataTypeName(String dataTypeName);

    String unescapeDataTypeName(String dataTypeName);

    String unescapeDataTypeString(String dataTypeString);

    boolean requiresDefiningColumnsAsNull();

    boolean supportsClustered(Class<? extends DatabaseObject> objectType);

    boolean looksLikeFunctionCall(String value);

    boolean supports(Class<? extends DatabaseObject> type);
}

