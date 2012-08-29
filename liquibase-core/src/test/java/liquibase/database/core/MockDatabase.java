package liquibase.database.core;

import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SqlStatement;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MockDatabase implements Database {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    
    public Schema getSchema() {
        return null;
    }

    public String getName() {
        return "Mock Database";
    }

    public Integer getDefaultPort() {
        return null;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public boolean equals(DatabaseObject otherObject, Database accordingTo) {
        return otherObject.getName().equalsIgnoreCase(this.getName());
    }

    public boolean equals(String otherObjectName, Database accordingTo) {
        return otherObjectName.equalsIgnoreCase(this.getName());
    }

    public void setCanCacheLiquibaseTableInfo(boolean canCacheLiquibaseTableInfo) {
        //
    }

    public boolean requiresUsername() {
        return false;
    }

    public boolean requiresPassword() {
        return false;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    public String getDefaultDriver(String url) {
        return null;
    }

    public DatabaseConnection getConnection() {
        return null;
    }

    public void setConnection(Connection conn) {
    }

    public void setConnection(DatabaseConnection conn) {
    }

    public boolean getAutoCommitMode() {
        return false;
    }

    public boolean isAutoCommit() throws DatabaseException {
        return false;
    }


    public boolean isCaseSensitive() {
        return false;
    }

    public void setAutoCommit(boolean b) throws DatabaseException {

    }

    public boolean supportsDDLInTransaction() {
        return false;
    }

    public String getDatabaseProductName() {
        return null;
    }

    public String getDatabaseProductVersion() throws DatabaseException {
        return null;
    }


    public int getDatabaseMajorVersion() throws DatabaseException {
        return 0;
    }

    public int getDatabaseMinorVersion() throws DatabaseException {
        return 0;
    }

    public String getShortName() {
        return null;
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

    public String getDefaultCatalogName() {
        return null;
    }

    public void setDefaultCatalogName(String catalogName) throws DatabaseException {

    }

    public String getDefaultSchemaName()  {
        return null;
    }

    public void setDefaultSchemaName(String schemaName) throws DatabaseException {
        
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsSequences() {
        return true;
    }

    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    public boolean supportsAutoIncrement() {
        return true;
    }

    public String getDateLiteral(String isoDate) {
        return isoDate;
    }


    public String getDateLiteral(java.sql.Date date) {
        return date.toString();
    }

    public String getTimeLiteral(Time time) {
        return time.toString();
    }

    public String getDateTimeLiteral(Timestamp timeStamp) {
        return timeStamp.toString();
    }

    public String getCurrentDateTimeFunction() {
        return "DATETIME()";
    }

    public void setCurrentDateTimeFunction(String function) {
    }

    public String getLineComment() {
        return null;
    }

    public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy) {
    	return "AUTO_INCREMENT_CLAUSE"
    			+ startWith != null ? (" " + startWith) : ""
    		    + incrementBy != null ? (" " + incrementBy) : "";
    }

    public SqlStatement getCommitSQL() {
        return null;
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogTableName()
     */
    public String getDatabaseChangeLogTableName() {
        return "DATABASECHANGELOG";
    }

    /**
     * @see liquibase.database.Database#getDatabaseChangeLogLockTableName()
     */
    public String getDatabaseChangeLogLockTableName() {
        return "DATABASECHANGELOGLOCK";
    }
    
    /**
     * Does nothing
     * 
     * @see liquibase.database.Database#setDatabaseChangeLogLockTableName(java.lang.String)
     */
    public void setDatabaseChangeLogLockTableName(String tableName) {
    }

	/**
	 * Does nothing
	 * 
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    public void setDatabaseChangeLogTableName(String tableName) {
    }

	public String getConcatSql(String... values) {
        return null;
    }

    public boolean acquireLock(Liquibase liquibase) throws LockException {
        return false;
    }

    public void releaseLock() throws LockException {
    }

    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    public boolean hasDatabaseChangeLogTable() {
        return false;
    }

    public boolean hasDatabaseChangeLogLockTable() {
        return false;
    }

    public void checkDatabaseChangeLogTable(Liquibase liquibase) throws DatabaseException, IOException {
    }

    public void checkDatabaseChangeLogLockTable(Liquibase liquibase) throws DatabaseException, IOException {
    }

    public void dropDatabaseObjects(Schema schema) throws DatabaseException {
    }

    public void tag(String tagString) throws DatabaseException {
    }

    public boolean doesTagExist(String tag) throws DatabaseException {
        return false;
    }

    public boolean isSystemTable(Schema schema, String tableName) {
        return false;
    }


    public boolean isSystemView(Schema schema, String name) {
        return false;
    }

    public boolean isLiquibaseTable(Schema schema, String tableName) {
        return false;
    }

    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(Schema schema, String name) throws DatabaseException {
        return null;
    }

    public String getDatabaseProductName(DatabaseConnection conn) throws DatabaseException {
        return "Mock Database";
    }

    public String getDateLiteral(Date defaultDateValue) {
        return defaultDateValue.toString();
    }

    public String escapeTableName(String catalogName, String schemaName, String tableName) {
        if (schemaName == null) {
            return tableName;
        } else {
            return schemaName+"."+tableName;
        }
    }

    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return escapeTableName(catalogName, schemaName, indexName);
    }

    public String escapeColumnName(String catalogName, String schemaName, String tableName, String columnName) {
        return columnName;
    }

    public String escapeColumnNameList(String columnNames) {
        return columnNames;
    }

    public String escapeSequenceName(String catalogName, String schemaName, String sequenceName) {
        if (sequenceName == null) {
            return sequenceName;
        } else {
            return schemaName+"."+sequenceName;
        }
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        return requestedSchema;
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return null;
    }

    public boolean supportsSchemas() {
        return true;
    }

    public boolean supportsCatalogs() {
        return true;
    }

    public String generatePrimaryKeyName(String tableName) {
        return "PK_"+tableName;
    }

    public String escapeViewName(String catalogName, String schemaName, String viewName) {
        return escapeTableName(catalogName, schemaName, viewName);
    }

    public boolean acquireLock() throws LockException {
        return false;
    }

    public void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String[] contexts) throws DatabaseException {
        ;
    }

    public void checkDatabaseChangeLogLockTable() throws DatabaseException {
        ;
    }

    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public void markChangeSetExecStatus(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        ;
    }

    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return null;
    }

    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {
        ;
    }

    public void commit() {
        ;
    }

    public void rollback() {
        ;
    }

    public SqlStatement getSelectChangeLogLockSQL() throws DatabaseException {
        return null;
    }

    public String escapeStringForDatabase(String string) {
        return string;
    }

    public void close() throws DatabaseException {
        ;
    }

    public DatabaseSnapshotGenerator createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws DatabaseException {
        return null;
    }

    public boolean supportsRestrictForeignKeys() {
        return true;
    }

    public String escapeConstraintName(String constraintName) {
        return constraintName;
    }
    
    public boolean isLocalDatabase() throws DatabaseException {
    	return true;
    }

    public String escapeDatabaseObject(String objectName) {
        return objectName;
    }

    public void executeStatements(Change change, DatabaseChangeLog changeLog, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException {
        ;
    }

    public void execute(SqlStatement[] statements, List<SqlVisitor> sqlVisitors) throws LiquibaseException {
        ;
    }

    public void saveStatements(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        ;
    }

    public void executeRollbackStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException, RollbackImpossibleException {
        ;
    }

    public void saveRollbackStatement(Change change, List<SqlVisitor> sqlVisitors, Writer writer) throws IOException, UnsupportedChangeException, RollbackImpossibleException, StatementNotSupportedOnDatabaseException, LiquibaseException {
        ;
    }

    public String getLiquibaseCatalogName() {
        return null;
    }

    public String getLiquibaseSchemaName(){
		return null;
	}

    public int getNextChangeSetSequenceValue() throws LiquibaseException {
        return 1;
    }

    public Date parseDate(String dateAsString) throws DateParseException {
        return new Date();
    }

	public List<DatabaseFunction> getDatabaseFunctions() {
		return null;
	}

    public void reset() {
        
    }
    
        public boolean supportsForeignKeyDisable() {
        return false;
    }

    public boolean disableForeignKeyChecks() throws DatabaseException {
        return false;
    }

    public void enableForeignKeyChecks() throws DatabaseException {
        
    }

    public void updateChecksum(ChangeSet changeSet) throws DatabaseException {
        
    }

    public boolean isReservedWord(String string) {
        return false;
    }

    public Schema correctSchema(Schema schema) {
        return schema;
    }

    public String correctTableName(String tableName) {
        return tableName;
    }

    public String correctConstraintName(String constraintName) {
        return constraintName;
    }

    public String correctColumnName(String columnName) {
        return columnName;
    }

    public String correctPrimaryKeyName(String pkName) {
        return pkName;
    }

    public String correctForeignKeyName(String fkName) {
        return fkName;
    }

    public String correctIndexName(String indexName) {
        return indexName;
    }

    public String escapeSchemaName(String schemaName) {
        return schemaName;
    }

    public String escapeCatalogName(String name) {
        return name;
    }

    public String getAssumedSchemaName(String catalogName, String schemaName) {
        return schemaName;
    }

    public String getAssumedCatalogName(String catalogName, String schemaName) {
        return catalogName;
    }
}
