package liquibase.database.core;

import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MockDatabase implements Database {


    public DatabaseObject[] getContainingObjects() {
        return null;
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

    public String getTypeName() {
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

    public String getDefaultCatalogName() throws DatabaseException {
        return null;
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

    public boolean supportsAutoIncrement() {
        return true;
    }

    public String getDateLiteral(String isoDate) {
        return isoDate;
    }


    public String getDateLiteral(java.sql.Date date) {
        return date.toString();
    }

    public String getDateLiteral(Time time) {
        return time.toString();
    }

    public String getDateLiteral(Timestamp timeStamp) {
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

    public String getAutoIncrementClause() {
        return "AUTO_INCREMENT_CLAUSE";
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

    public boolean doesChangeLogTableExist() {
        return false;
    }

    public boolean doesChangeLogLockTableExist() {
        return false;
    }

    public void checkDatabaseChangeLogTable(Liquibase liquibase) throws DatabaseException, IOException {
    }

    public void checkDatabaseChangeLogLockTable(Liquibase liquibase) throws DatabaseException, IOException {
    }

    public void dropDatabaseObjects(String schema) throws DatabaseException {
    }

    public void tag(String tagString) throws DatabaseException {
    }

    public boolean doesTagExist(String tag) throws DatabaseException {
        return false;
    }

    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return false;
    }


    public boolean isSystemView(String catalogName, String schemaName, String name) {
        return false;
    }

    public boolean isLiquibaseTable(String tableName) {
        return false;
    }

    public boolean shouldQuoteValue(String value) {
        return true;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String schemaName, String name) throws DatabaseException {
        return null;
    }

    public String getDatabaseProductName(DatabaseConnection conn) throws DatabaseException {
        return "Mock Database";
    }

    public String getDateLiteral(Date defaultDateValue) {
        return defaultDateValue.toString();
    }

    public String escapeTableName(String schemaName, String tableName) {
        if (schemaName == null) {
            return tableName;
        } else {
            return schemaName+"."+tableName;
        }
    }

    public String escapeIndexName(String schemaName, String indexName) {
        return escapeTableName(schemaName, indexName);
    }

    public String escapeColumnName(String schemaName, String tableName, String columnName) {
        return columnName;
    }

    public String escapeColumnNameList(String columnNames) {
        return columnNames;
    }

    public String escapeSequenceName(String schemaName, String sequenceName) {
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

    public String generatePrimaryKeyName(String tableName) {
        return "PK_"+tableName;
    }

    public String escapeViewName(String schemaName, String viewName) {
        return escapeTableName(schemaName, viewName);
    }

    public boolean acquireLock() throws LockException {
        return false;
    }

    public void checkDatabaseChangeLogTable() throws DatabaseException {
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

    public void markChangeSetAsRan(ChangeSet changeSet) throws DatabaseException {
        ;
    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws DatabaseException {
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

    public void executeStatements(Change change, List<SqlVisitor> sqlVisitors) throws LiquibaseException, UnsupportedChangeException {
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

	public String getLiquibaseSchemaName(){
		return null;
	}

	public boolean isPeculiarLiquibaseSchema() {
		return false;
	}

    public int getNextChangeSetSequenceValue() throws LiquibaseException {
        return 1;
    }

    public Date parseDate(String dateAsString) throws DateParseException {
        return new Date();
    }
}
