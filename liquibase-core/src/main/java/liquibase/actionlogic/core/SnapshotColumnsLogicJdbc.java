package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.*;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;
import liquibase.util.Validate;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

/**
 * Logic to snapshot database column(s). Delegates to {@link QueryJdbcMetaDataAction} getColumns().
 */
public class SnapshotColumnsLogicJdbc extends AbstractSnapshotDatabaseObjectsLogicJdbc {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Column.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                Column.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    /**
     * Creates an ObjectName with null values for "unknown" portions and calls {@link #createColumnSnapshotAction(ObjectName)}.
     */
    protected Action createSnapshotAction(SnapshotDatabaseObjectsAction action, Scope scope) throws DatabaseException, ActionPerformException {
        ObjectReference relatedTo = action.relatedTo;

        ObjectName columnName;

        Database database = scope.getDatabase();

        if (relatedTo.instanceOf(Catalog.class)) {
            if (!database.supportsCatalogs()) {
                throw new ActionPerformException("Cannot snapshot catalogs on " + database.getShortName());
            }
            columnName = new ObjectName(relatedTo.getSimpleName(), null, null, null);
        } else if (relatedTo.instanceOf(Schema.class)) {
            columnName = new ObjectName(relatedTo.getSimpleName(), null, null);
        } else if (relatedTo.instanceOf(Relation.class)) {
            columnName = new ObjectName(relatedTo.objectName, null);
        } else if (relatedTo.instanceOf(Column.class)) {
            columnName = relatedTo.objectName;
        } else {
            throw Validate.failure("Unexpected type: " + relatedTo.getClass().getName());
        }

        return createColumnSnapshotAction(columnName, scope);
    }

    protected Action createColumnSnapshotAction(ObjectName columnName, Scope scope) {
        List<String> nameParts = columnName.asList(4);

        if (nameParts.get(0) != null || scope.getDatabase().supportsCatalogs()) {
            return new QueryJdbcMetaDataAction("getColumns", nameParts.get(0), nameParts.get(1), nameParts.get(2), nameParts.get(3));
        } else {
            return new QueryJdbcMetaDataAction("getColumns", nameParts.get(1), null, nameParts.get(2), nameParts.get(3));
        }
    }


    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, SnapshotDatabaseObjectsAction originalAction, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        String rawTableName = StringUtils.trimToNull(row.get("TABLE_NAME", String.class));
        String rawColumnName = row.get("COLUMN_NAME", String.class);
        String rawSchemaName = StringUtils.trimToNull(row.get("TABLE_SCHEM", String.class));
        String rawCatalogName = StringUtils.trimToNull(row.get("TABLE_CAT", String.class));
        String remarks = StringUtils.trimToNull(row.get("REMARKS", String.class));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }


        Column column = new Column();
        column.setName(new ObjectName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName));

        column.remarks = remarks;

//        if (database instanceof OracleDatabase) {
//            String nullable = row.getString("NULLABLE");
//            if (nullable.equals("Y")) {
//                column.setNullable(true);
//            } else {
//                column.setNullable(false);
//            }
//        } else {
        int nullable = row.get("NULLABLE", Integer.class);
        if (nullable == DatabaseMetaData.columnNoNulls) {
            column.nullable = false;
        } else if (nullable == DatabaseMetaData.columnNullable) {
            column.nullable = true;
        } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
            LoggerFactory.getLogger(getClass()).info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
            column.nullable = true;
        }
//        }

        if (database.supportsAutoIncrement()) {
//            if (table instanceof Table) {
            if (row.get("IS_AUTOINCREMENT", Object.class) != null) {
                String isAutoincrement = row.get("IS_AUTOINCREMENT", String.class);
                isAutoincrement = StringUtils.trimToNull(isAutoincrement);
                if (isAutoincrement == null) {
                    column.autoIncrementInformation = null;
                } else if (isAutoincrement.equals("YES")) {
                    column.autoIncrementInformation = new Column.AutoIncrementInformation();
                } else if (isAutoincrement.equals("NO")) {
                    column.autoIncrementInformation = null;
                } else if (isAutoincrement.equals("")) {
                    LoggerFactory.getLogger(getClass()).info("Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
                    column.autoIncrementInformation = null;
                } else {
                    throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: '" + isAutoincrement + "'");
                }
//                } else {
//                    //probably older version of java, need to select from the column to find out if it is auto-increment
//                    String selectStatement;
//                    if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
//                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
//                        LoggerFactory.getLogger(getClass()).debug("rawCatalogName : <" + rawCatalogName + ">");
//                        LoggerFactory.getLogger(getClass()).debug("rawSchemaName : <" + rawSchemaName + ">");
//                        LoggerFactory.getLogger(getClass()).debug("rawTableName : <" + rawTableName + ">");
//                        LoggerFactory.getLogger(getClass()).debug("raw selectStatement : <" + selectStatement + ">");
//
//
//                    }
//                    else{
//                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
//                    }
//                    LoggerFactory.getLogger(getClass()).debug("Checking "+rawTableName+"."+rawCatalogName+" for auto-increment with SQL: '"+selectStatement+"'");
//                    Connection underlyingConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
//                    Statement statement = null;
//                    ResultSet columnSelectRS = null;
//
//                    try {
//                        statement = underlyingConnection.createStatement();
//                        columnSelectRS = statement.executeQuery(selectStatement);
//                        if (columnSelectRS.getMetaData().isAutoIncrement(1)) {
//                            column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
//                        } else {
//                            column.setAutoIncrementInformation(null);
//                        }
//                    } finally {
//                        try {
//                            if (statement != null) {
//                                statement.close();
//                            }
//                        } catch (SQLException ignore) {
//                        }
//                        if (columnSelectRS != null) {
//                            columnSelectRS.close();
//                        }
//                    }
////                }
            }
        }

        column.type = readDataType(row, column, scope);
        column.defaultValue = readDefaultValue(row, column, scope);

        return column;
    }

    protected DataType readDataType(RowBasedQueryResult.Row row, Column column, Scope scope) {
        DataType dataType = new DataType(row.get("TYPE_NAME", String.class));

        dataType.origin = scope.getDatabase().getShortName();
        setDataTypeStandardType(dataType, row, column, scope);
        setDataTypeParameters(dataType, row, column, scope);

        return dataType;
//        if (database instanceof OracleDatabase) {
//            String dataType = columnMetadataResultSet.getString("DATA_TYPE");
//            dataType = dataType.replace("VARCHAR2", "VARCHAR");
//            dataType = dataType.replace("NVARCHAR2", "NVARCHAR");
//
//            DataType type = new DataType(dataType);
////            type.setDataTypeId(dataType);
//            if (dataType.equalsIgnoreCase("NUMBER")) {
//                type.setColumnSize(columnMetadataResultSet.getInt("DATA_PRECISION"));
////                if (type.getColumnSize() == null) {
////                    type.setColumnSize(38);
////                }
//                type.setDecimalDigits(columnMetadataResultSet.getInt("DATA_SCALE"));
////                if (type.getDecimalDigits() == null) {
////                    type.setDecimalDigits(0);
////                }
////            type.setRadix(10);
//            } else {
//                type.setColumnSize(columnMetadataResultSet.getInt("DATA_LENGTH"));
//
//                if (dataType.equalsIgnoreCase("NCLOB") || dataType.equalsIgnoreCase("BLOB") || dataType.equalsIgnoreCase("CLOB")) {
//                    type.setColumnSize(null);
//                } else if (dataType.equalsIgnoreCase("NVARCHAR") || dataType.equalsIgnoreCase("NCHAR")) {
//                    type.setColumnSize(columnMetadataResultSet.getInt("CHAR_LENGTH"));
//                    type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
//                } else {
//                    String charUsed = columnMetadataResultSet.getString("CHAR_USED");
//                    DataType.ColumnSizeUnit unit = null;
//                    if ("C".equals(charUsed)) {
//                        unit = DataType.ColumnSizeUnit.CHAR;
//                        type.setColumnSize(type.getColumnSize());
//                    }
//                    type.setColumnSizeUnit(unit);
//                }
//            }
//
//
//            return type;
//        }

//        String columnTypeName = row.get("TYPE_NAME", String.class);

//        if (database instanceof FirebirdDatabase) {
//            if (columnTypeName.equals("BLOB SUB_TYPE 0")) {
//                columnTypeName = "BLOB";
//            }
//            if (columnTypeName.equals("BLOB SUB_TYPE 1")) {
//                columnTypeName = "CLOB";
//            }
//        }

//        if (database instanceof MySQLDatabase && (columnTypeName.equalsIgnoreCase("ENUM") || columnTypeName.equalsIgnoreCase("SET"))) {
//            try {
//                String boilerLength;
//                if (columnTypeName.equalsIgnoreCase("ENUM"))
//                    boilerLength = "7";
//                else // SET
//                    boilerLength = "6";
//                List<String> enumValues = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement("SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING(COLUMN_TYPE, " + boilerLength + ", LENGTH(COLUMN_TYPE) - " + boilerLength + " - 1 ), \"','\", 1 + units.i + tens.i * 10) , \"','\", -1)\n" +
//                        "FROM INFORMATION_SCHEMA.COLUMNS\n" +
//                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) units\n" +
//                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) tens\n" +
//                        "WHERE TABLE_NAME = '"+column.getRelation().getName()+"' \n" +
//                        "AND COLUMN_NAME = '"+column.getName()+"'"), String.class);
//                String enumClause = "";
//                for (String enumValue : enumValues) {
//                    enumClause += "'"+enumValue+"', ";
//                }
//                enumClause = enumClause.replaceFirst(", $", "");
//                return new DataType(columnTypeName + "("+enumClause+")");
//            } catch (DatabaseException e) {
//                LoggerFactory.getLogger(getClass()).warn("Error fetching enum values", e);
//            }
//        }
//        OldDataType.ColumnSizeUnit columnSizeUnit = OldDataType.ColumnSizeUnit.BYTE;
//
//        int dataType = row.get("DATA_TYPE", Integer.class);
//        Integer columnSize = null;
//        Integer decimalDigits = null;
//        if (!database.dataTypeIsNotModifiable(columnTypeName)) { // don't set size for types like int4, int8 etc
//            columnSize = row.get("COLUMN_SIZE", Integer.class);
//            decimalDigits = row.get("DECIMAL_DIGITS", Integer.class);
//            if (decimalDigits != null && decimalDigits.equals(0)) {
//                decimalDigits = null;
//            }
//        }
//
//        Integer radix = row.get("NUM_PREC_RADIX", Integer.class);
//
//        Integer characterOctetLength = row.get("CHAR_OCTET_LENGTH", Integer.class);

//TODO: refactor action        if (database instanceof DB2Database) {
//            String typeName = row.get("TYPE_NAME", String.class);
//            if (typeName.equalsIgnoreCase("DBCLOB") || typeName.equalsIgnoreCase("GRAPHIC") || typeName.equalsIgnoreCase("VARGRAPHIC")) {
//                if (columnSize != null) {
//                    columnSize = columnSize / 2; //Stored as double length chars
//                }
//            }
//        }


//        DataType type = new DataType(columnTypeName);
//        type.setDataTypeId(dataType);
//        type.setColumnSize(columnSize);
//        type.setDecimalDigits(decimalDigits);
//        type.setRadix(radix);
//        type.setCharacterOctetLength(characterOctetLength);
//        type.setColumnSizeUnit(columnSizeUnit);

//        return type;
    }

    protected void setDataTypeStandardType(DataType dataType, RowBasedQueryResult.Row row, Column column, Scope scope) {
        dataType.standardType = DataType.standardType(dataType.name);
    }

    protected void setDataTypeParameters(DataType dataType, RowBasedQueryResult.Row row, Column column, Scope scope) {
        if (dataType.standardType == DataType.StandardType.VARCHAR || dataType.standardType == DataType.StandardType.NVARCHAR) {
            Long columnSize = row.get("COLUMN_SIZE", Long.class);
            if (columnSize != null) {
                dataType.parameters.add(columnSize.toString());
            }
        }
    }

    protected Object readDefaultValue(RowBasedQueryResult.Row row, Column columnInfo, Scope scope) {
//TODO: refactor action        if (database instanceof MSSQLDatabase) {
//            Object defaultValue = row.get("COLUMN_DEF", Object.class);
//
//            if (defaultValue != null && defaultValue instanceof String) {
//                if (defaultValue.equals("(NULL)")) {
//                    row.set("COLUMN_DEF", null);
//                }
//            }
//        }
//
//        if (database instanceof OracleDatabase) {
//            if (row.get("COLUMN_DEF", Object.class) == null) {
//                row.set("COLUMN_DEF", row.get("DATA_DEFAULT", Object.class));
//
//                if (row.get("COLUMN_DEF", Object.class) != null && ((String) row.get("COLUMN_DEF", String.class)).equalsIgnoreCase("NULL")) {
//                    row.set("COLUMN_DEF", null);
//                }
//
//                if (row.get("VIRTUAL_COLUMN", String.class).equals("YES")) {
//                    row.set("COLUMN_DEF", "GENERATED ALWAYS AS ("+row.get("COLUMN_DEF", String.class)+")");
//                }
//            }
//
//        }

        return null; //SqlUtil.parseValue(database, row.get("COLUMN_DEF", String.class), columnInfo.type);
    }
}
