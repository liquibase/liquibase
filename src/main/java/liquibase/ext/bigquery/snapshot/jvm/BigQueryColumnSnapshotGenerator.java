package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.ext.bigquery.database.BigqueryDatabase;

import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.ColumnSnapshotGenerator;
;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BigQueryColumnSnapshotGenerator extends ColumnSnapshotGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    /*
    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        System.out.println("Wlazlem ssssssss");
        if (!snapshot.getSnapshotControl().shouldInclude(Column.class)) {
            return;
        }
        if (foundObject instanceof Relation) {
            Database database = snapshot.getDatabase();
            Relation relation = (Relation) foundObject;
            String query = String.format("SELECT KEYSPACE_NAME, COLUMN_NAME, TYPE, KIND FROM system_schema.columns WHERE KEYSPACE_NAME = '%s' AND table_name='%s';"
                    , database.getDefaultCatalogName(), relation.getName());
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc",
                    database);
            List<Map<String, ?>> returnList = executor.queryForList(new RawSqlStatement(query));

            for (Map<String, ?> columnPropertiesMap : returnList) {
               // relation.getColumns().add(readColumn(columnPropertiesMap, relation));
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        Relation relation = ((Column) example).getRelation();
        //we can't add column name as query parameter here as AWS keyspaces don't support such where statement
        String query = String.format("SELECT KEYSPACE_NAME, COLUMN_NAME, TYPE, KIND FROM system_schema.columns WHERE keyspace_name = '%s' AND table_name='%s';"
                , database.getDefaultCatalogName(), relation);

        List<Map<String, ?>> returnList = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database).queryForList(new RawSqlStatement(query));
        returnList = returnList.stream()
                .filter(stringMap -> ((String)stringMap.get("COLUMN_NAME")).equalsIgnoreCase(example.getName()))
                .collect(Collectors.toList());
        if (returnList.size() != 1) {
            Scope.getCurrentScope().getLog(BigQueryColumnSnapshotGenerator.class).warning(String.format(
                    "expecting exactly 1 column with name %s, got %s", example.getName(), returnList.size()));
            return null;
        } else {
            return null;//readColumn(returnList.get(0), relation);
        }
    }


    @Override
    protected Column readColumn(CachedRow columnMetadataResultSet, Relation table, Database database) throws SQLException, DatabaseException {


        String rawTableName = (String)columnMetadataResultSet.get("TABLE_NAME");
        String rawColumnName = (String)columnMetadataResultSet.get("COLUMN_NAME");

        System.out.println("Column snapshot "+rawColumnName);
        String rawSchemaName = StringUtil.trimToNull((String)columnMetadataResultSet.get("TABLE_SCHEM"));
        String rawCatalogName = StringUtil.trimToNull((String)columnMetadataResultSet.get("TABLE_CAT"));
        String remarks = StringUtil.trimToNull((String)columnMetadataResultSet.get("REMARKS"));
        if (remarks != null) {
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

        if (columnMetadataResultSet.get("IS_FILESTREAM") != null && (Boolean)columnMetadataResultSet.get("IS_FILESTREAM")) {
            column.setAttribute("fileStream", true);
        }

        if (columnMetadataResultSet.get("IS_ROWGUIDCOL") != null && (Boolean)columnMetadataResultSet.get("IS_ROWGUIDCOL")) {
            column.setAttribute("rowGuid", true);
        }

        String isAutoincrement;
        if (database instanceof OracleDatabase) {
            isAutoincrement = columnMetadataResultSet.getString("NULLABLE");
            if ("Y".equals(isAutoincrement)) {
                column.setNullable(true);
            } else {
                column.setNullable(false);
            }
        } else {
            Integer nullable = columnMetadataResultSet.getInt("NULLABLE");
            if (nullable != null) {
                if (nullable == 0) {
                    column.setNullable(false);
                } else if (nullable == 1) {
                    column.setNullable(true);
                } else if (nullable == 2) {
                    Scope.getCurrentScope().getLog(this.getClass()).info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
                    column.setNullable(true);
                }
            }
        }

        if (database.supportsAutoIncrement() && table instanceof Table) {
            if (database instanceof OracleDatabase) {
                Column.AutoIncrementInformation autoIncrementInfo = new Column.AutoIncrementInformation();
                String data_default = StringUtil.trimToEmpty((String)columnMetadataResultSet.get("DATA_DEFAULT")).toLowerCase();
                if (data_default.contains("iseq$$") && data_default.endsWith("nextval")) {
                    column.setAutoIncrementInformation(autoIncrementInfo);
                }

                Boolean isIdentityColumn = columnMetadataResultSet.yesNoToBoolean("IDENTITY_COLUMN");
                if (Boolean.TRUE.equals(isIdentityColumn)) {
                    Boolean defaultOnNull = columnMetadataResultSet.yesNoToBoolean("DEFAULT_ON_NULL");
                    String generationType = columnMetadataResultSet.getString("GENERATION_TYPE");
                    autoIncrementInfo.setDefaultOnNull(defaultOnNull);
                    autoIncrementInfo.setGenerationType(generationType);
                    column.setAutoIncrementInformation(autoIncrementInfo);
                }
            } else if (columnMetadataResultSet.containsColumn("IS_AUTOINCREMENT")) {
                isAutoincrement = (String)columnMetadataResultSet.get("IS_AUTOINCREMENT");
                isAutoincrement = StringUtil.trimToNull(isAutoincrement);
                if (isAutoincrement == null) {
                    column.setAutoIncrementInformation((Column.AutoIncrementInformation)null);
                } else if (database instanceof PostgresDatabase && PostgresDatabase.VALID_AUTO_INCREMENT_COLUMN_TYPE_NAMES.stream().noneMatch((typeName) -> {
                    return typeName.equalsIgnoreCase((String)columnMetadataResultSet.get("TYPE_NAME"));
                })) {
                    column.setAutoIncrementInformation((Column.AutoIncrementInformation)null);
                } else if (isAutoincrement.equals("YES")) {
                    column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                } else if (isAutoincrement.equals("NO")) {
                    column.setAutoIncrementInformation((Column.AutoIncrementInformation)null);
                } else {
                    if (!isAutoincrement.equals("")) {
                        throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: '" + isAutoincrement + "'");
                    }

                    Scope.getCurrentScope().getLog(this.getClass()).info("Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
                    column.setAutoIncrementInformation((Column.AutoIncrementInformation)null);
                }
            } else {
                if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                    isAutoincrement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
                    Scope.getCurrentScope().getLog(this.getClass()).fine("rawCatalogName : <" + rawCatalogName + ">");
                    Scope.getCurrentScope().getLog(this.getClass()).fine("rawSchemaName : <" + rawSchemaName + ">");
                    Scope.getCurrentScope().getLog(this.getClass()).fine("rawTableName : <" + rawTableName + ">");
                    Scope.getCurrentScope().getLog(this.getClass()).fine("raw selectStatement : <" + isAutoincrement + ">");
                } else {
                    isAutoincrement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
                }

                Scope.getCurrentScope().getLog(this.getClass()).fine("Checking " + rawTableName + "." + rawCatalogName + " for auto-increment with SQL: '" + isAutoincrement + "'");
                Connection underlyingConnection = ((JdbcConnection)database.getConnection()).getUnderlyingConnection();
                Statement statement = null;
                ResultSet columnSelectRS = null;

                try {
                    statement = underlyingConnection.createStatement();
                    columnSelectRS = statement.executeQuery(isAutoincrement);
                    if (columnSelectRS.getMetaData().isAutoIncrement(1)) {
                        column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                    } else {
                        column.setAutoIncrementInformation((Column.AutoIncrementInformation)null);
                    }
                } finally {
                    try {
                        if (statement != null) {
                            statement.close();
                        }
                    } catch (SQLException var22) {
                    }

                    if (columnSelectRS != null) {
                        columnSelectRS.close();
                    }

                }
            }
        }

        DataType type = this.readDataType(columnMetadataResultSet, column, database);
        column.setType(type);
        Object defaultValue = this.readDefaultValue(columnMetadataResultSet, column, database);
        if (defaultValue != null && defaultValue instanceof DatabaseFunction && ((DatabaseFunction)defaultValue).getValue().matches("\\w+")) {
            defaultValue = new DatabaseFunction(((DatabaseFunction)defaultValue).getValue().toUpperCase());
        }

        column.setDefaultValue(defaultValue);
        column.setDefaultValueConstraintName(columnMetadataResultSet.getString("COLUMN_DEF_NAME"));
        return column;
    }
 */
}
