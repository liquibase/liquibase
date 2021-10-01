package liquibase.ext.bigquery.snapshot;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.ColumnSnapshotGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

/**
 * Implements the Bigquery-specific parts of column snapshotting.
 */
public class BigqueryColumnSnapshotGenerator extends ColumnSnapshotGenerator {


    @Override
    protected Column readColumn(CachedRow columnMetadataResultSet, Relation table, Database database)
        throws SQLException, DatabaseException {
        String rawTableName = (String) columnMetadataResultSet.get("TABLE_NAME");
        String rawColumnName = (String) columnMetadataResultSet.get("COLUMN_NAME");
        String rawSchemaName = StringUtil.trimToNull((String) columnMetadataResultSet.get("TABLE_SCHEM"));
        String rawCatalogName = StringUtil.trimToNull((String) columnMetadataResultSet.get("TABLE_CAT"));
        String remarks = StringUtil.trimToNull((String) columnMetadataResultSet.get("REMARKS"));
        if (remarks != null) {
            // Comes back escaped sometimes
            remarks = remarks.replace("''", "'");
        }
        Integer position = columnMetadataResultSet.getInt("ORDINAL_POSITION");

        Column column = new Column();
        column.setName(StringUtil.trimToNull(rawColumnName));
        column.setRelation(table);
        column.setRemarks(remarks);
        column.setOrder(position);
        Boolean isComputed = columnMetadataResultSet.getBoolean("IS_COMPUTED");
        if (isComputed != null) {
            column.setComputed(isComputed);
        }


        if (columnMetadataResultSet.get("IS_FILESTREAM") != null && (Boolean) columnMetadataResultSet.get("IS_FILESTREAM")) {
            column.setAttribute("fileStream", true);
        }
        if (columnMetadataResultSet.get("IS_ROWGUIDCOL") != null && (Boolean) columnMetadataResultSet.get("IS_ROWGUIDCOL")) {
            column.setAttribute("rowGuid", true);
        }
        if (database instanceof OracleDatabase) {
            String nullable = columnMetadataResultSet.getString("NULLABLE");
            if ("Y".equals(nullable)) {
                column.setNullable(true);
            } else {
                column.setNullable(false);
            }
        } else {
            Integer nullable = columnMetadataResultSet.getInt("NULLABLE");
            if (nullable != null) {
                if (nullable == DatabaseMetaData.columnNoNulls) {
                    column.setNullable(false);
                } else if (nullable == DatabaseMetaData.columnNullable) {
                    column.setNullable(true);
                } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
                    // Scope.getCurrentScope().getLog(getClass()).info("Unknown nullable state for column "
                    //     + column.toString() + ". Assuming nullable");
                    column.setNullable(true);
                }
            }
        }

        if (database.supportsAutoIncrement()) {
            if (table instanceof Table) {
                if (database instanceof OracleDatabase) {
                    Column.AutoIncrementInformation autoIncrementInfo = new Column.AutoIncrementInformation();
                    String data_default = StringUtil.trimToEmpty((String) columnMetadataResultSet.get("DATA_DEFAULT")).toLowerCase();
                    if (data_default.contains("iseq$$") && data_default.endsWith("nextval")) {
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
                } else {
                    if (columnMetadataResultSet.containsColumn("IS_AUTOINCREMENT")) {
                        String isAutoincrement = (String) columnMetadataResultSet.get("IS_AUTOINCREMENT");
                        isAutoincrement = StringUtil.trimToNull(isAutoincrement);
                        if (isAutoincrement == null) {
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
                    } else {
                        //probably older version of java, need to select from the column to find out if it is auto-increment
                        String selectStatement;
                        if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
                            Scope.getCurrentScope().getLog(getClass()).fine("rawCatalogName : <" + rawCatalogName + ">");
                            Scope.getCurrentScope().getLog(getClass()).fine("rawSchemaName : <" + rawSchemaName + ">");
                            Scope.getCurrentScope().getLog(getClass()).fine("rawTableName : <" + rawTableName + ">");
                            Scope.getCurrentScope().getLog(getClass()).fine("raw selectStatement : <" + selectStatement + ">");


                        } else {
                            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
                        }
                        Scope.getCurrentScope().getLog(getClass()).fine("Checking " + rawTableName + "." + rawCatalogName + " for auto-increment with SQL: '" + selectStatement + "'");
                        Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
                        Statement statement = null;
                        ResultSet columnSelectRS = null;

                        try {
                            statement = underlyingConnection.createStatement();
                            columnSelectRS = statement.executeQuery(selectStatement);
                            if (columnSelectRS.getMetaData().isAutoIncrement(1)) {
                                column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                            } else {
                                column.setAutoIncrementInformation(null);
                            }
                        } finally {
                            try {
                                if (statement != null) {
                                    statement.close();
                                }
                            } catch (SQLException ignore) {
                            }
                            if (columnSelectRS != null) {
                                columnSelectRS.close();
                            }
                        }
                    }
                }
            }
        }

        DataType type = readDataType(columnMetadataResultSet, column, database);
        column.setType(type);

        Object defaultValue = readDefaultValue(columnMetadataResultSet, column, database);

        // TODO Is uppercasing the potential function name always a good idea?
        // In theory, we could get a quoted function name (inprobable, but not impossible)
        if ((defaultValue != null) && (defaultValue instanceof DatabaseFunction) && ((DatabaseFunction) defaultValue)
            .getValue().matches("\\w+")) {
            defaultValue = new DatabaseFunction(((DatabaseFunction) defaultValue).getValue().toUpperCase());
        }
        column.setDefaultValue(defaultValue);
        column.setDefaultValueConstraintName(columnMetadataResultSet.getString("COLUMN_DEF_NAME"));

        return column;
    }


}
