package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.database.sql.SqlStatement;
import liquibase.database.template.JdbcTemplate;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.HibernateDatabaseSnapshot;
import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.diff.DiffStatusListener;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;

public class HibernateDatabase implements Database {

    private String configFile;
    private String defaultSchema;

    public HibernateDatabase(String configFile) {
        this.configFile = configFile;
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

    public boolean supportsDDLInTransaction() {
        return false;
    }

    public String getDatabaseProductName() {
        return "Hibernate Mapping";
    }

    public String getDatabaseProductVersion() throws JDBCException {
        return "N/A";
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

    public String getBooleanType() {
        return null;
    }

    public String getCurrencyType() {
        return null;
    }

    public String getUUIDType() {
        return null;
    }

    public String getClobType() {
        return null;
    }

    public String getBlobType() {
        return null;
    }

    public String getDateType() {
        return null;
    }

    public String getDateTimeType() {
        return null;
    }

    public String getTimeType() {
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

    public String escapeColumnName(String columnName) {
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

    public JdbcTemplate getJdbcTemplate() {
        return null;
    }

    public void setJdbcTemplate(JdbcTemplate template) {

    }

    public String escapeStringForDatabase(String string) {
        return null;
    }

    public void close() throws JDBCException {

    }
}
