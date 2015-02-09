package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.sql.*;

/**
 * Logic to snapshot database column(s). Delegates to {@link QueryJdbcMetaDataAction} getColumns().
 */
public class SnapshotColumnsLogic extends AbstractSnapshotDatabaseObjectsLogic {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Column.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[] {
                Column.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    protected Action createSnapshotAction(Action action, Scope scope) throws DatabaseException {
        DatabaseObject relatedTo = action.get(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);

        String catalogName = null;
        String schemaName = null;
        String relationName = null;
        String columnName = null;

        if (relatedTo instanceof Catalog) {
            catalogName = relatedTo.getSimpleName();
        } else if (relatedTo instanceof Schema) {
            catalogName = ((Schema) relatedTo).getCatalogName();
            schemaName = relatedTo.getSimpleName();
        } else if (relatedTo instanceof Relation) {
            relationName = relatedTo.getSimpleName();

            Schema schema = relatedTo.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getSimpleName();
            }
        } else if (relatedTo instanceof Column) {
            columnName = relatedTo.getSimpleName();

            Relation relation = ((Column) relatedTo).getRelation();
            relationName = relation.getSimpleName();

            Schema schema = relation.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getSimpleName();
            }
        } else {
            throw Validate.failure("Unexpected type: "+relatedTo.getClass().getName());
        }

        return new QueryJdbcMetaDataAction("getColumns", catalogName, schemaName, relationName, columnName);

    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, Action originalAction, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String rawTableName = StringUtils.trimToNull(row.get("TABLE_NAME", String.class));
        String rawColumnName = row.get("COLUMN_NAME", String.class);
        String rawSchemaName = StringUtils.trimToNull(row.get("TABLE_SCHEM", String.class));
        String rawCatalogName = StringUtils.trimToNull(row.get("TABLE_CAT", String.class));
        String remarks = StringUtils.trimToNull(row.get("REMARKS", String.class));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }


        Column column = new Column();
        column.setName(rawColumnName);
        column.setRelation(new Table(rawCatalogName, rawSchemaName, rawTableName));
        column.setRemarks(remarks);

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
                column.setNullable(false);
            } else if (nullable == DatabaseMetaData.columnNullable) {
                column.setNullable(true);
            } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
                LogFactory.getLogger().info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
                column.setNullable(true);
            }
//        }

        if (database.supportsAutoIncrement()) {
//            if (table instanceof Table) {
                if (row.get("IS_AUTOINCREMENT", Object.class) != null) {
                    String isAutoincrement = row.get("IS_AUTOINCREMENT", String.class);
                    isAutoincrement = StringUtils.trimToNull(isAutoincrement);
                    if (isAutoincrement == null) {
                        column.setAutoIncrementInformation(null);
                    } else if (isAutoincrement.equals("YES")) {
                        column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                    } else if (isAutoincrement.equals("NO")) {
                        column.setAutoIncrementInformation(null);
                    } else if (isAutoincrement.equals("")) {
                        LogFactory.getLogger().info("Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
                        column.setAutoIncrementInformation(null);
                    } else {
                        throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: '" + isAutoincrement+"'");
                    }
//                } else {
//                    //probably older version of java, need to select from the column to find out if it is auto-increment
//                    String selectStatement;
//                    if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
//                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
//                        LogFactory.getLogger().debug("rawCatalogName : <" + rawCatalogName + ">");
//                        LogFactory.getLogger().debug("rawSchemaName : <" + rawSchemaName + ">");
//                        LogFactory.getLogger().debug("rawTableName : <" + rawTableName + ">");
//                        LogFactory.getLogger().debug("raw selectStatement : <" + selectStatement + ">");
//
//
//                    }
//                    else{
//                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
//                    }
//                    LogFactory.getLogger().debug("Checking "+rawTableName+"."+rawCatalogName+" for auto-increment with SQL: '"+selectStatement+"'");
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

        DataType type = readDataType(row, column, database);
        column.setType(type);

        column.setDefaultValue(readDefaultValue(row, column, database));

        return column;
    }

    protected DataType readDataType(RowBasedQueryResult.Row row, Column column, Database database) {

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

        String columnTypeName = row.get("TYPE_NAME", String.class);

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
//                LogFactory.getLogger().warning("Error fetching enum values", e);
//            }
//        }
        DataType.ColumnSizeUnit columnSizeUnit = DataType.ColumnSizeUnit.BYTE;

        int dataType = row.get("DATA_TYPE", Integer.class);
        Integer columnSize = null;
        Integer decimalDigits = null;
        if (!database.dataTypeIsNotModifiable(columnTypeName)) { // don't set size for types like int4, int8 etc
            columnSize = row.get("COLUMN_SIZE", Integer.class);
            decimalDigits = row.get("DECIMAL_DIGITS", Integer.class);
            if (decimalDigits != null && decimalDigits.equals(0)) {
                decimalDigits = null;
            }
        }

        Integer radix = row.get("NUM_PREC_RADIX", Integer.class);

        Integer characterOctetLength = row.get("CHAR_OCTET_LENGTH", Integer.class);

//TODO: refactor action        if (database instanceof DB2Database) {
//            String typeName = row.get("TYPE_NAME", String.class);
//            if (typeName.equalsIgnoreCase("DBCLOB") || typeName.equalsIgnoreCase("GRAPHIC") || typeName.equalsIgnoreCase("VARGRAPHIC")) {
//                if (columnSize != null) {
//                    columnSize = columnSize / 2; //Stored as double length chars
//                }
//            }
//        }


        DataType type = new DataType(columnTypeName);
        type.setDataTypeId(dataType);
        type.setColumnSize(columnSize);
        type.setDecimalDigits(decimalDigits);
        type.setRadix(radix);
        type.setCharacterOctetLength(characterOctetLength);
        type.setColumnSizeUnit(columnSizeUnit);

        return type;
    }

    protected Object readDefaultValue(RowBasedQueryResult.Row row, Column columnInfo, Database database) {
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

        return SqlUtil.parseValue(database, row.get("COLUMN_DEF", String.class), columnInfo.getType());
    }
}
