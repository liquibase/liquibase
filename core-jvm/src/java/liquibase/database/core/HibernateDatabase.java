package liquibase.database.core;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.DataType;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.DatabaseObject;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.snapshot.core.HibernateDatabaseSnapshotGenerator;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.text.ParseException;
import java.util.List;
import java.util.Set;

public class HibernateDatabase implements Database {

    private String configFile;
    private String defaultSchema;

    public HibernateDatabase(String configFile) {
        this.configFile = configFile;
    }

    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    public String getConfigFile() {
        return configFile;
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
        ;
    }

    public void setConnection(DatabaseConnection conn) {
        ;
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
        return "Hibernate Mapping";
    }

    public String getDatabaseProductVersion() throws DatabaseException {
        return "N/A";
    }

    public int getDatabaseMajorVersion() throws DatabaseException {
        return -1;
    }

    public int getDatabaseMinorVersion() throws DatabaseException {
        return -1;
    }

    public String getTypeName() {
        return "hibernate";
    }

    public String getDriverName() throws DatabaseException {
        return null;
    }

    public String getConnectionURL() throws DatabaseException {
        return "hibernate:"+configFile;
    }

    public String getConnectionUsername() throws DatabaseException {
        return "";
    }

    public String getDefaultCatalogName() throws DatabaseException {
        return null;
    }

    public String getDefaultSchemaName() {
        return defaultSchema;
    }

    public void setDefaultSchemaName(String schemaName) throws DatabaseException {
        this.defaultSchema = schemaName;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsSequences() {
        return false;
    }

    public boolean supportsAutoIncrement() {
        return false;
    }

    public String getColumnType(String columnType, Boolean autoIncrement) {
        return null;
    }

    public String getFalseBooleanValue() {
        return null;
    }

    public String getTrueBooleanValue() {
        return null;
    }

    public String getDateLiteral(String isoDate) {
        return null;
    }

    public String getCurrentDateTimeFunction() {
        return null;
    }

    public void setCurrentDateTimeFunction(String function) {

    }

    public String getLineComment() {
        return null;
    }

    public String getAutoIncrementClause() {
        return null;
    }

    public String getDatabaseChangeLogTableName() {
        return null;
    }

    public String getDatabaseChangeLogLockTableName() {
        return null;
    }
    
    /**
     * Does nothing because this is a hibernate database
     * @see liquibase.database.Database#setDatabaseChangeLogLockTableName(java.lang.String)
     */
    public void setDatabaseChangeLogLockTableName(String tableName) {
    }

	/**
	 * Does nothing because this is a hibernate database
     * @see liquibase.database.Database#setDatabaseChangeLogTableName(java.lang.String)
     */
    public void setDatabaseChangeLogTableName(String tableName) {
    }

	public String getConcatSql(String... values) {
        return null;
    }

    public boolean doesChangeLogTableExist() {
        return false;
    }

    public boolean doesChangeLogLockTableExist() {
        return false;
    }

    public void checkDatabaseChangeLogTable() throws DatabaseException {

    }

    public void checkDatabaseChangeLogLockTable() throws DatabaseException {

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

    public boolean isLiquibaseTable(String tableName) {
        return false;
    }

    public boolean shouldQuoteValue(String value) {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String schemaName, String name) throws DatabaseException {
        return null;
    }

    public int getDatabaseType(int type) {
        return 0;
    }

    public String getDatabaseProductName(DatabaseConnection conn) throws DatabaseException {
        return null;
    }

    public DataType getBooleanType() {
        return null;
    }

    public DataType getCurrencyType() {
        return null;
    }

    public DataType getUUIDType() {
        return null;
    }

    public DataType getCharType()
    {
        return null;
    }

    public DataType getVarcharType() {
        return null;
    }
    
    public DataType getClobType() {
        return null;
    }

    public DataType getBlobType() {
        return null;
    }

    public DataType getDateType() {
        return null;
    }

    public DataType getDateTimeType() {
        return null;
    }

    public DataType getTimeType() {
        return null;
    }

    public DataType getBigIntType() {
        return null;
    }

    public DataType getFloatType()
    {
        return null;
    }

    public DataType getDoubleType()
    {
        return null;
    }

    public DataType getIntType()
    {
        return null;
    }

    public DataType getTinyIntType()
    {
        return null;
    }

    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        return null;
    }

    public String convertJavaObjectToString(Object value) {
        return null;
    }

    public boolean isSystemView(String catalogName, String schemaName, String name) {
        return false;
    }

    public String getDateLiteral(Date date) {
        return null;
    }

    public String getDateLiteral(Time time) {
        return null;
    }

    public String getDateLiteral(Timestamp timeStamp) {
        return null;
    }

    public String getDateLiteral(java.util.Date defaultDateValue) {
        return null;
    }

    public String escapeTableName(String schemaName, String tableName) {
        return null;
    }

    public String escapeIndexName(String schemaName, String indexName) {
        return null;
    }

    public String escapeDatabaseObject(String objectName) {
        return null;
    }

    public String escapeColumnName(String schemaName, String tableName, String columnName) {
        return null;
    }

    public String escapeColumnNameList(String columnNames) {
        return null;
    }

    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
        return null;
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws DatabaseException {
        return null;
    }

    public boolean supportsSchemas() {
        return false;
    }

    public String generatePrimaryKeyName(String tableName) {
        return null;
    }

    public String escapeSequenceName(String schemaName, String sequenceName) {
        return null;
    }

    public String escapeViewName(String schemaName, String viewName) {
        return null;
    }

    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public void markChangeSetAsRan(ChangeSet changeSet) throws DatabaseException {

    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws DatabaseException {

    }

    public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
        return null;
    }

    public java.util.Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        return null;
    }

    public void removeRanStatus(ChangeSet changeSet) throws DatabaseException {

    }

    public void commit() throws DatabaseException {

    }

    public void rollback() throws DatabaseException {

    }

    public String escapeStringForDatabase(String string) {
        return null;
    }

    public void close() throws DatabaseException {

    }

    public Configuration createConfiguration() {
        return new AnnotationConfiguration();
    }

    public boolean supportsRestrictForeignKeys() {
        return false;
    }

    public String escapeConstraintName(String constraintName) {
        return constraintName;
    }

    /**
     * As the connectionURL for Hibernate refers to a config file, just return false
     */
    public boolean isLocalDatabase() throws DatabaseException {
    	return false;
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
}
