package liquibase.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This database provides no implementations for any of the methods, and is only used for testing the validity of the
 * getPriority method inside the snapshot generators. It is not recommended to use this class anywhere else.
 */
class MockDatabase implements Database {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    @Override
    public String getDefaultDriver(String url) {
        return "";
    }

    @Override
    public DatabaseConnection getConnection() {
        return null;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {

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
    public boolean getAutoCommitMode() {
        return false;
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

    @Override
    public String getDatabaseProductName() {
        return "";
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return "";
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
        return "";
    }

    @Override
    public String getDefaultCatalogName() {
        return "";
    }

    @Override
    public void setDefaultCatalogName(String catalogName) throws DatabaseException {

    }

    @Override
    public String getDefaultSchemaName() {
        return "";
    }

    @Override
    public Integer getDefaultScaleForNativeDataType(String nativeDataType) {
        return 0;
    }

    @Override
    public void setDefaultSchemaName(String schemaName) throws DatabaseException {

    }

    @Override
    public Integer getDefaultPort() {
        return 0;
    }

    @Override
    public Integer getFetchSize() {
        return 0;
    }

    @Override
    public String getLiquibaseCatalogName() {
        return "";
    }

    @Override
    public void setLiquibaseCatalogName(String catalogName) {

    }

    @Override
    public String getLiquibaseSchemaName() {
        return "";
    }

    @Override
    public void setLiquibaseSchemaName(String schemaName) {

    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    @Override
    public boolean supportsAutoIncrement() {
        return false;
    }

    @Override
    public String getDateLiteral(String isoDate) {
        return "";
    }

    @Override
    public String getCurrentDateTimeFunction() {
        return "";
    }

    @Override
    public void setCurrentDateTimeFunction(String function) {

    }

    @Override
    public String getLineComment() {
        return "";
    }

    @Override
    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy, String generationType, Boolean defaultOnNull) {
        return "";
    }

    @Override
    public String getDatabaseChangeLogTableName() {
        return "";
    }

    @Override
    public void setDatabaseChangeLogTableName(String tableName) {

    }

    @Override
    public String getDatabaseChangeLogLockTableName() {
        return "";
    }

    @Override
    public void setDatabaseChangeLogLockTableName(String tableName) {

    }

    @Override
    public String getLiquibaseTablespaceName() {
        return "";
    }

    @Override
    public void setLiquibaseTablespaceName(String tablespaceName) {

    }

    @Override
    public String getConcatSql(String... values) {
        return "";
    }

    @Override
    public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo) {

    }

    @Override
    public void dropDatabaseObjects(CatalogAndSchema schema) throws LiquibaseException {

    }

    @Override
    public void tag(String tagString) throws DatabaseException {

    }

    @Override
    public boolean doesTagExist(String tag) throws DatabaseException {
        return false;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        return false;
    }

    @Override
    public boolean isLiquibaseObject(DatabaseObject object) {
        return false;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
        return "";
    }

    @Override
    public String getDateLiteral(Date date) {
        return "";
    }

    @Override
    public String getTimeLiteral(Time time) {
        return "";
    }

    @Override
    public String getDateTimeLiteral(Timestamp timeStamp) {
        return "";
    }

    @Override
    public String getDateLiteral(java.util.Date defaultDateValue) {
        return "";
    }

    @Override
    public String escapeObjectName(String catalogName, String schemaName, String objectName, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        return "";
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return "";
    }

    @Override
    public String escapeObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        return 0;
    }

    @Override
    public int getDefaultFractionalDigitsForTimestamp() {
        return 0;
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName) {
        return "";
    }

    @Override
    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName, boolean quoteNamesThatMayBeFunctions) {
        return "";
    }

    @Override
    public String escapeColumnNameList(String columnNames) {
        return "";
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return false;
    }

    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return null;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogInObjectName(Class<? extends DatabaseObject> type) {
        return false;
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        return "";
    }

    @Override
    public String escapeSequenceName(String catalogName, String schemaName, String sequenceName) {
        return "";
    }

    @Override
    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return "";
    }

    @Override
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {

    }

    @Override
    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return Collections.emptyList();
    }

    @Override
    public java.util.Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    @Override
    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {

    }

    @Override
    public void commit() throws DatabaseException {

    }

    @Override
    public void rollback() throws DatabaseException {

    }

    @Override
    public String escapeStringForDatabase(String string) {
        return "";
    }

    @Override
    public void close() throws DatabaseException {

    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    @Override
    public String escapeConstraintName(String constraintName) {
        return "";
    }

    @Override
    public boolean isAutoCommit() throws DatabaseException {
        return false;
    }

    @Override
    public void setAutoCommit(boolean b) throws DatabaseException {

    }

    @Override
    public boolean isSafeToRunUpdate() throws DatabaseException {
        return false;
    }

    @Override
    public void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException {

    }

    @Override
    public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {

    }

    @Override
    public void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, LiquibaseException {

    }

    @Override
    public void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException {

    }

    @Override
    public void executeRollbackStatements(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {

    }

    @Override
    public void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, LiquibaseException {

    }

    @Override
    public java.util.Date parseDate(String dateAsString) throws DateParseException {
        return null;
    }

    @Override
    public List<DatabaseFunction> getDateFunctions() {
        return Collections.emptyList();
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

    @Override
    public boolean isCaseSensitive() {
        return false;
    }

    @Override
    public boolean isReservedWord(String string) {
        return false;
    }

    @Override
    public CatalogAndSchema correctSchema(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String correctObjectName(String name, Class<? extends DatabaseObject> objectType) {
        return "";
    }

    @Override
    public boolean isFunction(String string) {
        return false;
    }

    @Override
    public int getDataTypeMaxParameters(String dataTypeName) {
        return 0;
    }

    @Override
    public CatalogAndSchema getDefaultSchema() {
        return null;
    }

    @Override
    public boolean dataTypeIsNotModifiable(String typeName) {
        return false;
    }

    @Override
    public String generateDatabaseFunctionValue(DatabaseFunction databaseFunction) {
        return "";
    }

    @Override
    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return null;
    }

    @Override
    public void setObjectQuotingStrategy(ObjectQuotingStrategy quotingStrategy) {

    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return false;
    }

    @Override
    public boolean getOutputDefaultSchema() {
        return false;
    }

    @Override
    public void setOutputDefaultSchema(boolean outputDefaultSchema) {

    }

    @Override
    public boolean isDefaultSchema(String catalog, String schema) {
        return false;
    }

    @Override
    public boolean isDefaultCatalog(String catalog) {
        return false;
    }

    @Override
    public boolean getOutputDefaultCatalog() {
        return false;
    }

    @Override
    public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {

    }

    @Override
    public boolean supportsPrimaryKeyNames() {
        return false;
    }

    @Override
    public boolean supportsNotNullConstraintNames() {
        return false;
    }

    @Override
    public boolean supportsBatchUpdates() throws DatabaseException {
        return false;
    }

    @Override
    public boolean requiresExplicitNullForColumns() {
        return false;
    }

    @Override
    public String getSystemSchema() {
        return "";
    }

    @Override
    public void addReservedWords(Collection<String> words) {

    }

    @Override
    public String escapeDataTypeName(String dataTypeName) {
        return "";
    }

    @Override
    public String unescapeDataTypeName(String dataTypeName) {
        return "";
    }

    @Override
    public String unescapeDataTypeString(String dataTypeString) {
        return "";
    }

    @Override
    public ValidationErrors validate() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
