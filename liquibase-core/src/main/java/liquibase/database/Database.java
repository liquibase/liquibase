package liquibase.database;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.*;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Interface that every DBMS supported by this software must implement. Most methods belong into ont of these
 * categories:
 * <ul></ul>
 * <li>Information about the capabilities of the DBMS (e.g. can it work with catalogs? with schemas?)</li>
 * <li>changing and manipulating types defined in the SQL standard into native types and vice-versa</li>
 * <li>creating strings for use in SQL statements, e.g. literals for dates, time, numerals, etc.</li>
 * </ul>
 */
public interface Database extends PrioritizedService {

    String databaseChangeLogTableName = "DatabaseChangeLog".toUpperCase(Locale.US);
    String databaseChangeLogLockTableName = "DatabaseChangeLogLock".toUpperCase(Locale.US);

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

    /**
     * Returns the name of the database product according to the underlying database.
     */
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

    /**
     * Returns the default precision for a given native data type, e.g. "datetime2" for Microsoft SQL Server.
     * @param nativeDataType the name of the native data type (case-insensitive).
     * @return The default precision of the native data type, or null if the type is unknown to this database.
     */
    Integer getDefaultScaleForNativeDataType(String nativeDataType);

    void setDefaultSchemaName(String schemaName) throws DatabaseException;

    Integer getDefaultPort();

    Integer getFetchSize();

    String getLiquibaseCatalogName();

    void setLiquibaseCatalogName(String catalogName);

    String getLiquibaseSchemaName();

    void setLiquibaseSchemaName(String schemaName);

    /**
     * Returns whether this database support initially deferrable columns.
     */
    boolean supportsInitiallyDeferrableColumns();

    boolean supportsSequences();

    boolean supportsDropTableCascadeConstraints();

    boolean supportsAutoIncrement();

    String getDateLiteral(String isoDate);

    /**
     * Returns database-specific function for generating the current date/time.
     */
    String getCurrentDateTimeFunction();

    void setCurrentDateTimeFunction(String function);

    String getLineComment();

    /**
     * Returns database-specific auto-increment DDL clause.
     */
    String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy);

    String getDatabaseChangeLogTableName();

    /**
     * Set the table name of the change log to the given table name
     *
     * @param tableName
     */
    void setDatabaseChangeLogTableName(String tableName);

    String getDatabaseChangeLogLockTableName();

    /**
     * Set the table name of the change log lock to the given table name
     *
     * @param tableName
     */
    void setDatabaseChangeLogLockTableName(String tableName);

    String getLiquibaseTablespaceName();

    void setLiquibaseTablespaceName(String tablespaceName);

    /**
     * Returns SQL to concat the passed values.
     */
    String getConcatSql(String... values);

    void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo);

    /**
     * Drops all objects in a specified schema.
     * @param schema schema (catalog+)schema to drop
     * @throws LiquibaseException if any problem occurs
     */
    void dropDatabaseObjects(CatalogAndSchema schema) throws LiquibaseException;

    /**
     * Tags the database changelog with the given string.
     */
    void tag(String tagString) throws DatabaseException;

    boolean doesTagExist(String tag) throws DatabaseException;

    boolean isSystemObject(DatabaseObject example);

    boolean isLiquibaseObject(DatabaseObject object);

    String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException;

    String getDateLiteral(java.sql.Date date);

    String getTimeLiteral(java.sql.Time time);

    String getDateTimeLiteral(java.sql.Timestamp timeStamp);

    String getDateLiteral(Date defaultDateValue);

    String escapeObjectName(String catalogName, String schemaName, String objectName, Class<? extends DatabaseObject> objectType);

    String escapeTableName(String catalogName, String schemaName, String tableName);

    String escapeIndexName(String catalogName, String schemaName, String indexName);

    String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType);

    /**
     * Determines the maximum precision (number of fractional digits) for TIMESTAMP columns for the given database.
     * Might not always be able to give an exact answer since, for some DBMS, it depends on the actual software version
     * if fractional digits are supported. A warning should be logged in this case.
     *
     * @return the number of allowed fractional digits for TIMESTAMP columns. May return 0.
     */
    int getMaxFractionalDigitsForTimestamp();

    /**
     * When a TIMESTAMP column without the parameter "number of fractional digits" is created, what is the default
     * value?
     *
     * @return The default number of fractional digits for TIMESTAMP columns
     */
    int getDefaultFractionalDigitsForTimestamp();

    /**
     * Escapes a single column name in a database-dependent manner so reserved words can be used as a column
     * name (i.e. "return").
     *
     * @param schemaName
     * @param tableName
     * @param columnName column name
     * @return escaped column name
     */
    String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName);

    /**
     * Similar to {@link #escapeColumnName(String, String, String, String)} but allows control over whether function-like names should be left unquoted.
     *
     * @deprecated Know if you should quote the name or not, and use {@link #escapeColumnName(String, String, String, String)} which will quote things that look like functions, or leave it along as you see fit. Don't rely on this function guessing.
     */
    String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName, boolean quoteNamesThatMayBeFunctions);

    /**
     * Escapes a list of column names in a database-dependent manner so reserved words can be used as a column
     * name (i.e. "return").
     *
     * @param columnNames list of column names
     * @return escaped column name list
     */
    String escapeColumnNameList(String columnNames);

//    Set<UniqueConstraint> findUniqueConstraints(String schema) throws DatabaseException;

    boolean supportsTablespaces();

    boolean supportsCatalogs();

    CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase();

    boolean supportsSchemas();

    boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type);

    String generatePrimaryKeyName(String tableName);

    String escapeSequenceName(String catalogName, String schemaName, String sequenceName);

    String escapeViewName(String catalogName, String schemaName, String viewName);

    /**
     * Returns the run status for the given ChangeSet
     */
    ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException;

    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     */
    void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException;

    /**
     * Returns the ChangeSets that have been run against the current database.
     */
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

    boolean isSafeToRunUpdate() throws DatabaseException;

    void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    /*
     * Executes the statements passed as argument to a target {@link Database}
     *
     * @param statements an array containing the SQL statements to be issued
     * @param database the target {@link Database}
     * @throws DatabaseException if there were problems issuing the statements
     */
    void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, LiquibaseException;

    void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void executeRollbackStatements(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException;

    void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, LiquibaseException;

    Date parseDate(String dateAsString) throws DateParseException;

    /**
     * Returns list of database native date functions
     */
    List<DatabaseFunction> getDateFunctions();

    void resetInternalState();

    boolean supportsForeignKeyDisable();

    boolean disableForeignKeyChecks() throws DatabaseException;

    void enableForeignKeyChecks() throws DatabaseException;

    boolean isCaseSensitive();

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

    ObjectQuotingStrategy getObjectQuotingStrategy();

    void setObjectQuotingStrategy(ObjectQuotingStrategy quotingStrategy);

    boolean createsIndexesForForeignKeys();

    /**
     * Should the schema be included in identifiers even if it is the default schema?
     *
     * @return true (if the schema should be included in every case) or false (omit if default schema)
     */
    boolean getOutputDefaultSchema();

    /**
     * Whether the default schema should be included in generated SQL
     */
    void setOutputDefaultSchema(boolean outputDefaultSchema);

    /**
     * If the database supports schemas, test if a given combination of catalog and schema name equals to the default
     * catalog and schema of the current logged in user.
     * @param catalog catalog name to be tested
     * @param schema schema name to be tested
     * @return if the database supports catalogs: true if it is the default schema, false if not. If it does not
     * support schemas, the behaviour of this method is undefined (please call supportsSchemas first!)
     */
    boolean isDefaultSchema(String catalog, String schema);

    /**
     * If the database supports catalogs, test if a given catalog name equals to the default catalog of the current
     * logged in user.
     * @param catalog catalog name to be tested
     * @return if the database supports catalogs: true if it is the default catalog, false if not. If it does not
     * support catalogs, the behaviour of this method is undefined (please call supportsCatalog first!)
     */
    boolean isDefaultCatalog(String catalog);

    boolean getOutputDefaultCatalog();

    void setOutputDefaultCatalog(boolean outputDefaultCatalog);

    boolean supportsPrimaryKeyNames();

    /**
     * Does this database treat NOT NULL as an own kind of CONSTRAINT (in addition of simply being a column property)?
     * This will affect the CONSTRAINT clause SQL generators.
     * @return true if the database supports naming NOT NULL constraints, false if not.
     */
    boolean supportsNotNullConstraintNames();

    /**
     * Does the database support batched DML (INSERT/UPDATE/DELETE/MERGE/...) statements sent in a single call to
     * the database?
     * @return true if the database has this capability for all DML statements, false in all other cases
     */
    boolean supportsBatchUpdates() throws DatabaseException;

    /**
     * Does the database require explicit NULL for nullable columns?
     *
     * @return true if the database behaves incorrectly if NULL is not explicitly specified,
     * false if it behaves like any good RDBMS should.
     */
    boolean requiresExplicitNullForColumns();

    String getSystemSchema();

    void addReservedWords(Collection<String> words);

    String escapeDataTypeName(String dataTypeName);

    String unescapeDataTypeName(String dataTypeName);

    String unescapeDataTypeString(String dataTypeString);

    ValidationErrors validate();
}

