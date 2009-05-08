package liquibase.database;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.HibernateDatabaseSnapshot;
import liquibase.database.template.Executor;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.*;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
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

    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new HibernateDatabaseSnapshot(this);
    }

    public String getConfigFile() {
        return configFile;
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
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

    public boolean isAutoCommit() throws JDBCException {
        return false;
    }

    public void setAutoCommit(boolean b) throws JDBCException {
        
    }

    public boolean supportsDDLInTransaction() {
        return false;
    }

    public String getDatabaseProductName() {
        return "Hibernate Mapping";
    }

    public String getDatabaseProductVersion() throws JDBCException {
        return "N/A";
    }

    public int getDatabaseMajorVersion() throws JDBCException {
        return -1;
    }

    public int getDatabaseMinorVersion() throws JDBCException {
        return -1;
    }

    public String getProductName() {
        return "Hibernate Mapping";
    }

    public String getTypeName() {
        return "hibernate";
    }

    public String getDriverName() throws JDBCException {
        return null;
    }

    public String getConnectionURL() throws JDBCException {
        return "hibernate:"+configFile;
    }

    public String getConnectionUsername() throws JDBCException {
        return "";
    }

    public String getDefaultCatalogName() throws JDBCException {
        return null;
    }

    public String getDefaultSchemaName() {
        return defaultSchema;
    }

    public void setDefaultSchemaName(String schemaName) throws JDBCException {
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

    public void checkDatabaseChangeLogTable() throws JDBCException {

    }

    public void checkDatabaseChangeLogLockTable() throws JDBCException {

    }

    public void dropDatabaseObjects(String schema) throws JDBCException {

    }

    public void tag(String tagString) throws JDBCException {

    }

    public boolean doesTagExist(String tag) throws JDBCException {
        return false;
    }

    public boolean isSystemTable(String catalogName, String schemaName, String tableName) {
        return false;
    }

    public boolean isLiquibaseTable(String tableName) {
        return false;
    }

    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return null;
    }

    public boolean shouldQuoteValue(String value) {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String schemaName, String name) throws JDBCException {
        return null;
    }

    public int getDatabaseType(int type) {
        return 0;
    }

    public String getDatabaseProductName(Connection conn) throws JDBCException {
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

    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
        return null;
    }

    public String convertRequestedSchemaToCatalog(String requestedSchema) throws JDBCException {
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

    public boolean isColumnAutoIncrement(String schemaName, String tableName, String columnName) throws SQLException, JDBCException {
        return false;
    }

    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        return null;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        return null;
    }

    public void markChangeSetAsRan(ChangeSet changeSet) throws JDBCException {

    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws JDBCException {

    }

    public List<RanChangeSet> getRanChangeSetList() throws JDBCException {
        return null;
    }

    public java.util.Date getRanDate(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        return null;
    }

    public void removeRanStatus(ChangeSet changeSet) throws JDBCException {

    }

    public void commit() throws JDBCException {

    }

    public void rollback() throws JDBCException {

    }

    public SqlStatement getSelectChangeLogLockSQL() throws JDBCException {
        return null;
    }

    public Executor getJdbcTemplate() {
        return null;
    }

    public void setJdbcTemplate(Executor template) {

    }

    public String escapeStringForDatabase(String string) {
        return null;
    }

    public void close() throws JDBCException {

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
    public boolean isLocalDatabase() throws JDBCException {
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
}
