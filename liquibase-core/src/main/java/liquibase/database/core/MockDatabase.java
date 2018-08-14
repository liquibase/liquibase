package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.InternalDatabase;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ValidationErrors;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.io.Writer;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A DBMS that effectively does nothing, but it is useful for tests where everything should work in the same way
 * for all databases.
 */
// Most of the methods here are empty because this is, after all, a Database that is supposed not to do much.
@SuppressWarnings("squid:S1186")
public class MockDatabase implements Database, InternalDatabase {

    private static final int FETCH_SIZE = 1000;

    private boolean outputDefaultSchema;
    private boolean outputDefaultCatalog;
    private boolean supportsCatalogs = true;
    private boolean supportsSchemas = true;
    private boolean supportsSequences = true;
    private String defaultCatalogName;
    private String defaultSchemaName;
    private boolean caseSensitive;


    @Override
    public Integer getDefaultScaleForNativeDataType(String nativeDataType) {
        // Nothing special to do for the mock database.
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    public Schema getSchema() {
        return null;
    }

    public String getName() {
        return "Mock Database";
    }

    @Override
    public CatalogAndSchema getDefaultSchema() {
        return new CatalogAndSchema("default", "default");
    }

    @Override
    public Integer getDefaultPort() {
        return null;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public boolean equals(final DatabaseObject otherObject, final Database accordingTo) {
        return otherObject.getName().equalsIgnoreCase(this.getName());
    }

    @Override
    public void setCanCacheLiquibaseTableInfo(final boolean canCacheLiquibaseTableInfo) {
        //
    }

    @Override
    public boolean requiresUsername() {
        return false;
    }

    @Override
    public boolean requiresPassword() {
        return false;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(final DatabaseConnection conn) {
        return false;
    }

    @Override
    public String getDefaultDriver(final String url) {
        return null;
    }

    @Override
    public DatabaseConnection getConnection() {
        return null;
    }

    @Override
    public void setConnection(final DatabaseConnection conn) {
    }

    public void setConnection(final Connection conn) {
    }

    @Override
    public boolean getAutoCommitMode() {
        return false;
    }

    @Override
    public boolean isAutoCommit() {
        return false;
    }

    @Override
    public void setAutoCommit(final boolean b) {

    }

    @Override
    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public String getDatabaseProductName() {
        return null;
    }

    @Override
    public String getDatabaseProductVersion() {
        return null;
    }


    @Override
    public int getDatabaseMajorVersion() {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() {
        return 0;
    }

    @Override
    public String getShortName() {
        return "mock";
    }

    public String getDriverName() {
        return null;
    }

    public String getConnectionURL() {
        return null;
    }

    public String getConnectionUsername() {
        return null;
    }

    @Override
    public String getDefaultCatalogName() {
        return defaultCatalogName;
    }

    @Override
    public void setDefaultCatalogName(final String catalogName) {
        this.defaultCatalogName = catalogName;
    }

    @Override
    public String getDefaultSchemaName()  {
        return defaultSchemaName;
    }

    @Override
    public void setDefaultSchemaName(final String schemaName) {
        this.defaultSchemaName = schemaName;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return supportsSequences;
    }

    public void setSupportsSequences(boolean supportsSequences) {
        this.supportsSequences = supportsSequences;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return true;
    }

    @Override
    public String getDateLiteral(final String isoDate) {
        return isoDate;
    }


    @Override
    public String getDateLiteral(final java.sql.Date date) {
        return date.toString();
    }

    @Override
    public String getTimeLiteral(final Time time) {
        return time.toString();
    }

    @Override
    public String getDateTimeLiteral(final Timestamp timeStamp) {
        return timeStamp.toString();
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return "DATETIME()";
    }

    @Override
    public void setCurrentDateTimeFunction(final String function) {
    }

    @Override
    public String getLineComment() {
        return null;
    }

    @Override
    public String getAutoIncrementClause(final BigInteger startWith, final BigInteger incrementBy) {
        return (("AUTO_INCREMENT_CLAUSE" + startWith) != null) ? (" " + startWith) : ((("" + incrementBy) != null) ?
            (" " + incrementBy) : "");
    }

    public SqlStatement getCommitSQL() {
        return null;
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    @Override
    public String getDatabaseChangeLogTableName() {
        return "DATABASECHANGELOG";
    }

    /**
     * Does nothing
     *
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    @Override
    public void setDatabaseChangeLogTableName(final String tableName) {
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    @Override
    public String getDatabaseChangeLogLockTableName() {
        return "DATABASECHANGELOGLOCK";
    }

    /**
     * Does nothing
     *
     * @see liquibase.database.Database#setDatabaseChangeLogLockTableName(java.lang.String)
     */
    @Override
    public void setDatabaseChangeLogLockTableName(final String tableName) {
    }

    @Override
    public String getConcatSql(final String... values) {
        return null;
    }

    public boolean acquireLock(final Liquibase liquibase) {
        return false;
    }

    public void releaseLock() {
    }

    public DatabaseChangeLogLock[] listLocks() {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void dropDatabaseObjects(final CatalogAndSchema schema) {
    }

    @Override
    public void tag(final String tagString) {
    }

    @Override
    public boolean doesTagExist(final String tag) {
        return false;
    }


    @Override
    public boolean isSystemObject(final DatabaseObject example) {
        return false;
    }

    @Override
    public boolean isLiquibaseObject(final DatabaseObject object) {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(final CatalogAndSchema schema, final String name) {
        return null;
    }

    public String getDatabaseProductName(final DatabaseConnection conn) {
        return "Mock Database";
    }

    @Override
    public String getDateLiteral(final Date defaultDateValue) {
        return defaultDateValue.toString();
    }

    @Override
    public String escapeTableName(final String catalogName, final String schemaName, final String tableName) {
        if (schemaName == null) {
            return tableName;
        } else {
            return schemaName+"."+tableName;
        }
    }

    @Override
    public String escapeIndexName(final String catalogName, final String schemaName, final String indexName) {
        return escapeTableName(catalogName, schemaName, indexName);
    }

    @Override
    public String escapeColumnName(final String catalogName, final String schemaName, final String tableName,
                                   final String columnName) {
        return columnName;
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName,
                                   boolean quoteNamesThatMayBeFunctions) {
        return columnName;
    }

    @Override
    public String escapeColumnNameList(final String columnNames) {
        return columnNames;
    }

    @Override
    public String escapeSequenceName(final String catalogName, final String schemaName, final String sequenceName) {
        if (sequenceName == null) {
            return sequenceName;
        } else {
            return schemaName+"."+sequenceName;
        }
    }

    public String convertRequestedSchemaToSchema(final String requestedSchema) {
        return requestedSchema;
    }

    public String convertRequestedSchemaToCatalog(final String requestedSchema) {
        return null;
    }

    @Override
    public boolean supportsSchemas() {
        return supportsSchemas;
    }

    public void setSupportsSchemas(boolean supportsSchemas) {
        this.supportsSchemas = supportsSchemas;
    }

    @Override
    public boolean supportsCatalogs() {
        return supportsCatalogs;
    }

    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.UPPER_CASE;
    }

    public void setSupportsCatalogs(boolean supportsCatalogs) {
        this.supportsCatalogs = supportsCatalogs;
    }

    public boolean supportsCatalogInObjectName() {
        return true;
    }

    @Override
    public String generatePrimaryKeyName(final String tableName) {
        return "PK_"+tableName;
    }

    @Override
    public String escapeViewName(final String catalogName, final String schemaName, final String viewName) {
        return escapeTableName(catalogName, schemaName, viewName);
    }

    public boolean acquireLock() {
        return false;
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet) {
        return null;
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) {
        return null;
    }

    @Override
    public void markChangeSetExecStatus(final ChangeSet changeSet, final ChangeSet.ExecType execType) {
    }

    @Override
    public List<RanChangeSet> getRanChangeSetList() {
        return null;
    }

    @Override
    public Date getRanDate(final ChangeSet changeSet) {
        return null;
    }

    @Override
    public void removeRanStatus(final ChangeSet changeSet) {
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    public SqlStatement getSelectChangeLogLockSQL() {
        return null;
    }

    @Override
    public String escapeStringForDatabase(final String string) {
        return string;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    @Override
    public String escapeConstraintName(final String constraintName) {
        return constraintName;
    }

    @Override
    public boolean isSafeToRunUpdate() {
        return true;
    }

    @Override
    public String escapeObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        return objectName;
    }

    @Override
    public String escapeObjectName(final String catalogName, final String schemaName, final String objectName,
                                   final Class<? extends DatabaseObject> objectType) {
        return catalogName +"."+schemaName+"."+objectName;
    }

    @Override
    public void executeStatements(final Change change, final DatabaseChangeLog changeLog,
                                  final List<SqlVisitor> sqlVisitors) {
    }

    @Override
    public void execute(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) {
    }

    @Override
    public void saveStatements(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) {
    }

    @Override
    public void executeRollbackStatements(final Change change, final List<SqlVisitor> sqlVisitors) {
    }

    @Override
    public void executeRollbackStatements(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) {
    }
    
    @Override
    public void saveRollbackStatement(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) {
    }

    @Override
    public String getLiquibaseCatalogName() {
        return null;
    }

    @Override
    public void setLiquibaseCatalogName(final String catalogName) {

    }

    @Override
    public String getLiquibaseSchemaName(){
        return null;
    }

    @Override
    public void setLiquibaseSchemaName(final String schemaName) {

    }

    @Override
    public String getLiquibaseTablespaceName() {
        return null;
    }

    @Override
    public void setLiquibaseTablespaceName(final String tablespaceName) {

    }

    @Override
    public Date parseDate(final String dateAsString) {
        return new Date();
    }

    @Override
    public List<DatabaseFunction> getDateFunctions() {
        return null;
    }

    @Override
    public void resetInternalState() {

    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return false;
    }

    @Override
    public boolean disableForeignKeyChecks() {
        return false;
    }

    @Override
    public void enableForeignKeyChecks() {

    }

    public void updateChecksum(final ChangeSet changeSet) {

    }

    @Override
    public boolean isReservedWord(final String string) {
        return false;
    }

    @Override
    public CatalogAndSchema correctSchema(final CatalogAndSchema schema) {
        return schema.standardize(this);
    }

    @Override
    /**
     * Returns name all lower case except for the last letter capital for easier detection of corrected names.
     */
    public String correctObjectName(final String name, final Class<? extends DatabaseObject> objectType) {
        if (name == null) {
            return null;
        }
        String finalName = name.toLowerCase();
        finalName = finalName.substring(0, finalName.length()-1)
            + finalName.substring(finalName.length()-1, finalName.length()).toUpperCase();
        return finalName;
    }

    public String correctObjectName(final String name, final Class<? extends DatabaseObject> objectType,
                                    final boolean quoteCorrectedName) {
        return correctObjectName(name, objectType);
    }

    @Override
    public Integer getFetchSize() {
        return FETCH_SIZE;
    }

    @Override
    public boolean isFunction(final String string) {
        return string.endsWith("()");
    }

    @Override
    public int getDataTypeMaxParameters(final String dataTypeName) {
        return 2;
    }

    public CatalogAndSchema getSchemaFromJdbcInfo(final String rawCatalogName, final String rawSchemaName) {
        return new CatalogAndSchema(rawCatalogName, rawSchemaName);
    }

    public String getJdbcCatalogName(final CatalogAndSchema schema) {
        return schema.getCatalogName();
    }

    public String getJdbcSchemaName(final CatalogAndSchema schema) {
        return schema.getSchemaName();
    }

    public String getJdbcCatalogName(final Schema schema) {
        return schema.getCatalogName();
    }

    public String getJdbcSchemaName(final Schema schema) {
        return schema.getName();
    }

    @Override
    public boolean dataTypeIsNotModifiable(final String typeName) {
        return true;
    }

    @Override
    public String generateDatabaseFunctionValue(final DatabaseFunction databaseFunction) {
        return null;
    }

    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return ObjectQuotingStrategy.LEGACY;
    }

    @Override
    public void setObjectQuotingStrategy(final ObjectQuotingStrategy quotingStrategy) {
    }

    @Override
    public boolean supportsCatalogInObjectName(final Class<? extends DatabaseObject> type) {
        return true;
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return false;
    }

    @Override
    public boolean getOutputDefaultSchema() {
        return outputDefaultSchema;
    }

    @Override
    public void setOutputDefaultSchema(final boolean outputDefaultSchema) {
        this.outputDefaultSchema = outputDefaultSchema;
    }

    @Override
    public boolean getOutputDefaultCatalog() {
        return outputDefaultCatalog;
    }

    @Override
    public void setOutputDefaultCatalog(final boolean outputDefaultCatalog) {
        this.outputDefaultCatalog = outputDefaultCatalog;
    }

    @Override
    public boolean isDefaultSchema(final String catalog, final String schema) {
        return false;
    }

    @Override
    public boolean isDefaultCatalog(final String catalog) {
        return false;
    }

    @Override
    public boolean supportsPrimaryKeyNames() {
        return true;
    }


    @Override
    public String getSystemSchema() {
        return "information_schema";
    }

    @Override
    public void addReservedWords(Collection<String> words) {

    }

    @Override
    public String toString() {
        return "Mock database";
    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        return dataTypeName;
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return dataTypeName;
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        return dataTypeString;
    }

    @Override
    public ValidationErrors validate() {
        return new ValidationErrors();
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() {
        return false;
    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        return 9;
    }

    @Override
    public int getDefaultFractionalDigitsForTimestamp() {
        return 6;
    }

    @Override
    public boolean requiresExplicitNullForColumns() {
        return false;
    }
}
