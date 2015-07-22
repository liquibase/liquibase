package liquibase.sdk.database;

import java.io.IOException;
import java.io.Writer;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.DateParseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

public class MockDatabase implements Database, InternalDatabase {

    private DatabaseConnection connection;

    private int maxReferenceContainerDepth = 2;
    private int maxSnapshotContainerDepth = 2;
    private boolean outputDefaultSchema;
    private boolean outputDefaultCatalog;
    private String defaultCatalogName;
    private String defaultSchemaName;
    private boolean caseSensitive;


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
        return otherObject.getSimpleName().equalsIgnoreCase(this.getName());
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
    public boolean isCorrectDatabaseImplementation(final DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    @Override
    public String getDefaultDriver(final String url) {
        return null;
    }

    @Override
    public DatabaseConnection getConnection() {
        return connection;
    }

    @Override
    public void setConnection(final DatabaseConnection conn) {
        this.connection = conn;
    }

    @Override
    public boolean getAutoCommitMode() {
        return false;
    }

    @Override
    public boolean isAutoCommit() throws DatabaseException {
        return false;
    }


    @Override
    public boolean isCaseSensitive(Class<? extends DatabaseObject> type) {
        return caseSensitive;
    }

    @Override
    public boolean canStoreObjectName(String name, boolean quoted, Class<? extends DatabaseObject> type) {
        return true;
    }

    @Override
    public boolean canStoreObjectName(String name, Class<? extends DatabaseObject> type) {
        return true;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public void setAutoCommit(final boolean b) throws DatabaseException {

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
    public String getDatabaseProductVersion() throws DatabaseException {
        return null;
    }


    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return 0;
    }

    @Override
    public String getShortName() {
        return "mock";
    }

    public String getDriverName() throws DatabaseException {
        return null;
    }

    public String getConnectionURL() throws DatabaseException {
        return null;
    }

    public String getConnectionUsername() throws DatabaseException {
        return null;
    }

    @Override
    public String getDefaultCatalogName() {
        return defaultCatalogName;
    }

    @Override
    public void setDefaultCatalogName(final String catalogName) throws DatabaseException {
        this.defaultCatalogName = catalogName;
    }

    @Override
    public String getDefaultSchemaName()  {
        return defaultSchemaName;
    }

    @Override
    public void setDefaultSchemaName(final String schemaName) throws DatabaseException {
        this.defaultSchemaName = schemaName;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return true;
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

	/**
	 * Does nothing
	 *
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    @Override
    public void setDatabaseChangeLogTableName(final String tableName) {
    }

	@Override
    public String getConcatSql(final String... values) {
        return null;
    }

    public boolean acquireLock(final Liquibase liquibase) throws LockException {
        return false;
    }

    public void releaseLock() throws LockException {
    }

    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void dropDatabaseObjects(final CatalogAndSchema schema) throws DatabaseException {
    }

    @Override
    public void tag(final String tagString) throws DatabaseException {
    }

    @Override
    public boolean doesTagExist(final String tag) throws DatabaseException {
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
    public String getViewDefinition(final CatalogAndSchema schema, final String name) throws DatabaseException {
        return null;
    }

    public String getDatabaseProductName(final DatabaseConnection conn) throws DatabaseException {
        return "Mock Database";
    }

    @Override
    public String getDateLiteral(final Date defaultDateValue) {
        return defaultDateValue.toString();
    }

    public String convertRequestedSchemaToSchema(final String requestedSchema) throws DatabaseException {
        return requestedSchema;
    }

    public String convertRequestedSchemaToCatalog(final String requestedSchema) throws DatabaseException {
        return null;
    }

    public boolean supportsCatalogInObjectName() {
        return true;
    }

    @Override
    public String generatePrimaryKeyName(final String tableName) {
        return "PK_"+tableName;
    }

    public boolean acquireLock() throws LockException {
        return false;
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public void markChangeSetExecStatus(final ChangeSet changeSet, final ChangeSet.ExecType execType) throws DatabaseException {
        ;
    }

    @Override
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return null;
    }

    @Override
    public Date getRanDate(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public void removeRanStatus(final ChangeSet changeSet) throws DatabaseException {
        ;
    }

    @Override
    public void commit() {
        ;
    }

    @Override
    public void rollback() {
        ;
    }

    public SqlStatement getSelectChangeLogLockSQL() throws DatabaseException {
        return null;
    }

    @Override
    public String escapeStringForDatabase(final String string) {
        return string;
    }

    @Override
    public void close() throws DatabaseException {
        ;
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
    	return true;
    }

    @Override
    public String escapeObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        return "`"+objectName+"`";
    }

    @Override
    public String escapeObjectName(ObjectName objectName, Class<? extends DatabaseObject> objectType) {
        return StringUtils.join(objectName.asList(), ".", new StringUtils.ObjectNameFormatter(objectType, this));
    }

    @Override
    public void executeStatements(final Change change, final DatabaseChangeLog changeLog, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        ;
    }

    @Override
    public void execute(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        ;
    }

    @Override
    public void saveStatements(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws IOException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        ;
    }

    @Override
    public void executeRollbackStatements(final Change change, final List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException {
        ;
    }

    @Override
    public void executeRollbackStatements(final SqlStatement[] statements, final List<SqlVisitor> sqlVisitors) throws LiquibaseException, RollbackImpossibleException {
        ;
    }
    
    @Override
    public void saveRollbackStatement(final Change change, final List<SqlVisitor> sqlVisitors, final Writer writer) throws IOException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        ;
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
    public Date parseDate(final String dateAsString) throws DateParseException {
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
    public boolean disableForeignKeyChecks() throws DatabaseException {
        return false;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {

    }

    public void updateChecksum(final ChangeSet changeSet) throws DatabaseException {

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
        finalName = finalName.substring(0, finalName.length()-1)+finalName.substring(finalName.length()-1, finalName.length()).toUpperCase();
        return finalName;
    }

    public String correctObjectName(final String name, final Class<? extends DatabaseObject> objectType, final boolean quoteCorrectedName) {
        return correctObjectName(name, objectType);
    }

    @Override
    public boolean isFunction(final String string) {
        if (string.endsWith("()")) {
            return true;
        }
        return false;
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
        return schema.getSimpleName();
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
    public void setObjectQuotingStrategy(final ObjectQuotingStrategy quotingStrategy) {
    }

    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return ObjectQuotingStrategy.LEGACY;
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return false;
    }


	@Override
    public void setOutputDefaultSchema(final boolean outputDefaultSchema) {
		this.outputDefaultSchema = outputDefaultSchema;
	}


	@Override
    public boolean getOutputDefaultSchema() {
		return outputDefaultSchema;
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
    public boolean supportsClustered(Class<? extends DatabaseObject> objectType) {
        return true;
    }

    @Override
    public boolean requiresDefiningColumnsAsNull() {
        return false;
    }

    @Override
    public boolean looksLikeFunctionCall(String value) {
        return false;
    }

    @Override
    public int getMaxReferenceContainerDepth() {
        return maxReferenceContainerDepth;
    }

    public MockDatabase setMaxReferenceContainerDepth(int maxReferenceContainerDepth) {
        this.maxReferenceContainerDepth = maxReferenceContainerDepth;
        return this;
    }

    @Override
    public int getMaxSnapshotContainerDepth() {
        return maxSnapshotContainerDepth;
    }

    public MockDatabase setMaxSnapshotContainerDepth(int maxContainerDepth) {
        this.maxSnapshotContainerDepth = maxContainerDepth;
        return this;
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
}
