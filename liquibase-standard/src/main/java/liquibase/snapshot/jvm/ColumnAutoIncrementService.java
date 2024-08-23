package liquibase.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class that centralizes database specific auto increment parameters information.
 *
 */
public class ColumnAutoIncrementService {

    private Map<String, Column.AutoIncrementInformation> autoIncrementColumns;


    /**
     * If the database support autoincrement columns details (as starts with and increment by), this method returns the
     * detailed information about them.
     * If a new database needs to be supported just add the query to method getQueryForDatabase .
     *
     * @param database the database connection
     * @param snapshot snapshot data used to store cache information.
     * @return Map with the sequence name and auto increment details
     */
    public Map<String, Column.AutoIncrementInformation> obtainSequencesInformation(Database database, Schema schema, DatabaseSnapshot snapshot) {
        if (autoIncrementColumns != null) {
            return autoIncrementColumns;
        } else {
            autoIncrementColumns = new ConcurrentHashMap<>();
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            try {
                String query = this.getQueryForDatabaseAndSchema(database, schema);
                List<Map<String, ?>> rows = executor.queryForList(new RawParameterizedSqlStatement(query));
                for (Map<String, ?> row : rows) {
                    String schemaName = (String) row.get("SCHEMA_NAME");
                    String tableName = (String) row.get("TABLE_NAME");
                    String columnName = (String) row.get("COLUMN_NAME");
                    Long startValue = (Long) row.get("START_VALUE");
                    Long incrementBy = (Long) row.get("INCREMENT_BY");

                    Column.AutoIncrementInformation info = new Column.AutoIncrementInformation(startValue, incrementBy);
                    autoIncrementColumns.put(String.format("%s.%s.%s", schemaName, tableName, columnName), info);
                }
                snapshot.setScratchData("autoIncrementColumns", autoIncrementColumns);
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).info("Could not read identity information", e);
            } catch (LiquibaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine(e.getMessage());
                return autoIncrementColumns;
            }
        }
        return autoIncrementColumns;
    }

    private String getQueryForDatabaseAndSchema(Database database, Schema schema) throws LiquibaseException {
        if (database instanceof MSSQLDatabase) {
            return "SELECT object_schema_name(object_id) AS schema_name, " +
                    "object_name(object_id) AS table_name, name AS column_name, " +
                    "CAST(seed_value AS bigint) AS start_value, " +
                    "CAST(increment_value AS bigint) AS increment_by " +
                    "FROM sys.identity_columns " +
                    "WHERE object_schema_name(object_id) = '" + schema.getName() + "'";
        } else if (database instanceof PostgresDatabase) {
            int version = 9;
            try {
                version = database.getDatabaseMajorVersion();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed to retrieve database version: " + e);
            }
            if (version < 10) { // 'pg_sequence' view does not exists yet
                return "SELECT " +
                        "    ns.nspname as SCHEMA_NAME, " +
                        "    td.relname as TABLE_NAME, " +
                        "    pa.attname as COLUMN_NAME, " +
                        "    s.start_value::bigint AS START_VALUE, " +
                        "    s.increment::bigint AS INCREMENT_BY " +
                        "FROM pg_class c " +
                        "    JOIN pg_namespace ns on c.relnamespace = ns.oid " +
                        "    JOIN information_schema.sequences s on c.relname = s.sequence_name AND ns.nspname = s.sequence_schema::varchar" +
                        "    JOIN pg_depend d ON c.oid = d.objid " +
                        "    JOIN pg_class td ON td.oid = d.refobjid " +
                        "    JOIN pg_attribute pa ON  pa.attrelid=td.oid AND pa.attnum=d.refobjsubid " +
                        "WHERE c.relkind = 'S' AND d.deptype = 'a' AND ns.nspname = '" + schema.getName() + "'";
            } else {
                return "SELECT " +
                        "    ns.nspname as SCHEMA_NAME, " +
                        "    td.relname as TABLE_NAME, " +
                        "    pa.attname as COLUMN_NAME, " +
                        "    COALESCE(pg_sequence_last_value(c.oid::regclass) + s.seqincrement, " +
                        "      s.seqstart) AS START_VALUE, " +
                        "    s.seqincrement AS INCREMENT_BY " +
                        "FROM pg_class c " +
                        "    JOIN pg_sequence s on c.oid = s.seqrelid " +
                        "    JOIN pg_namespace ns on c.relnamespace = ns.oid " +
                        "    JOIN pg_depend d ON c.oid = d.objid " +
                        "    JOIN pg_class td ON td.oid = d.refobjid " +
                        "    JOIN pg_attribute pa ON  pa.attrelid=td.oid AND pa.attnum=d.refobjsubid " +
                        "WHERE c.relkind = 'S' AND d.deptype = 'a' AND ns.nspname = '" + schema.getName() + "'";
            }
        }

        throw new LiquibaseException("Liquibase or the database do not support auto increment parameters.");
    }

    /**
     * Verify if a column has auto increment capabilities and set the autoIncrement field with a placeholder object (starts
     * with 1, increment 1)
     */
    public Column enableColumnAutoIncrementIfAvailable(Column column, Database database, CachedRow columnMetadataResultSet,
                                                       String rawCatalogName, String rawSchemaName, String rawTableName,
                                                       String rawColumnName) throws SQLException {
        if (database instanceof OracleDatabase) {
            this.handleEnableAutoIncrementForOracle(column, columnMetadataResultSet);
        } else {
            if (columnMetadataResultSet.containsColumn("IS_AUTOINCREMENT")) {
                this.handleEnableAutoIncrementValidation(column, database, columnMetadataResultSet);
            } else {
                this.handleEnableAutoIncrementValidationLegacyJdbc(column, database, rawCatalogName, rawSchemaName,
                        rawTableName, rawColumnName);
            }
        }
        return column;
    }

    private void handleEnableAutoIncrementForOracle(Column column, CachedRow columnMetadataResultSet) {
        Column.AutoIncrementInformation autoIncrementInfo = new Column.AutoIncrementInformation();
        String dataDefault = StringUtil.trimToEmpty((String) columnMetadataResultSet.get("DATA_DEFAULT")).toLowerCase();
        if (dataDefault.contains("iseq$$") && dataDefault.endsWith("nextval")) {
            column.setAutoIncrementInformation(autoIncrementInfo);
        }

        Boolean isIdentityColumn = columnMetadataResultSet.yesNoToBoolean("IDENTITY_COLUMN");
        if (Boolean.TRUE.equals(isIdentityColumn)) { // Oracle 12+
            Boolean defaultOnNull = columnMetadataResultSet.yesNoToBoolean("DEFAULT_ON_NULL");
            String generationType = columnMetadataResultSet.getString("GENERATION_TYPE");
            autoIncrementInfo.setDefaultOnNull(defaultOnNull);
            autoIncrementInfo.setGenerationType(generationType);

            column.setAutoIncrementInformation(autoIncrementInfo);
        }
    }

    private void handleEnableAutoIncrementValidation(Column column, Database database, CachedRow columnMetadataResultSet) {
        // It is possible to make a column in Postgres that is varchar (for example) and reported as auto-increment. In this case,
        // we'd rather just preserve the default value, so we remove the auto-increment information since that doesn't really make any sense.
        String isAutoincrement = (String) columnMetadataResultSet.get("IS_AUTOINCREMENT");
        isAutoincrement = StringUtil.trimToNull(isAutoincrement);
        if (isAutoincrement == null) {
            column.setAutoIncrementInformation(null);
        } else if (database instanceof PostgresDatabase && PostgresDatabase.VALID_AUTO_INCREMENT_COLUMN_TYPE_NAMES.stream().noneMatch(typeName -> typeName.equalsIgnoreCase((String) columnMetadataResultSet.get("TYPE_NAME")))) {
            column.setAutoIncrementInformation(null);
        } else if (isAutoincrement.equals("YES")) {
            column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
        } else if (isAutoincrement.equals("NO")) {
            column.setAutoIncrementInformation(null);
        } else if (isAutoincrement.equals("")) {
            Scope.getCurrentScope().getLog(getClass()).info("Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
            column.setAutoIncrementInformation(null);
        } else {
            throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: '" + isAutoincrement + "'");
        }
    }

    private void handleEnableAutoIncrementValidationLegacyJdbc(Column column, Database database, String rawCatalogName,
                                                               String rawSchemaName, String rawTableName, String rawColumnName) throws SQLException {
        //probably older version of java, need to select from the column to find out if it is auto-increment
        String selectStatement;
        if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) +
                    " from " + rawSchemaName + "." + rawTableName + " where 0=1";
            Scope.getCurrentScope().getLog(getClass()).fine("rawCatalogName : <" + rawCatalogName + ">");
            Scope.getCurrentScope().getLog(getClass()).fine("rawSchemaName : <" + rawSchemaName + ">");
            Scope.getCurrentScope().getLog(getClass()).fine("rawTableName : <" + rawTableName + ">");
            Scope.getCurrentScope().getLog(getClass()).fine("raw selectStatement : <" + selectStatement + ">");


        } else {
            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName)
                    + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
        }
        Scope.getCurrentScope().getLog(getClass()).fine("Checking " + rawTableName + "." + rawCatalogName +
                " for auto-increment with SQL: '" + selectStatement + "'");
        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

        try (Statement statement = underlyingConnection.createStatement();
             ResultSet columnSelectRS = statement.executeQuery(selectStatement)) {
            if (columnSelectRS.getMetaData().isAutoIncrement(1)) {
                column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
            } else {
                column.setAutoIncrementInformation(null);
            }
        }
    }

}
