package liquibase.action.core;

import liquibase.action.MetaDataQueryAction;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.Row;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Action implementation that uses the JDBC MetaData.getColumns() call.
 * Catalog and/or Schema are required if the target database supports it.
 * TableName and columnName can be null, which acts as a "Match Any" filter.
 * No changes in case are made to any object names, they must be corrected as needed before creating this object.
 */
public class ColumnsJdbcMetaDataQueryAction extends MetaDataQueryAction {
    private static final String CATALOG_NAME = "catalogName";
    private static final String SCHEMA_NAME = "schemaName";
    private static final String TABLE_NAME = "tableName";
    private static final String COLUMN_NAME = "columnName";

    public ColumnsJdbcMetaDataQueryAction(String catalogName, String schemaName, String tableName, String columnName) {
        this.setAttribute(CATALOG_NAME, catalogName);
        this.setAttribute(SCHEMA_NAME, schemaName);
        this.setAttribute(TABLE_NAME, tableName);
        this.setAttribute(COLUMN_NAME, columnName);
    }

    public String getColumnName() {
        return getAttribute(COLUMN_NAME, String.class);
    }

    public String getTableName() {
        return getAttribute(TABLE_NAME, String.class);
    }

    public String getSchemaName() {
        return getAttribute(SCHEMA_NAME, String.class);
    }

    public String getCatalogName() {
        return getAttribute(CATALOG_NAME, String.class);
    }

    protected DatabaseObject rawMetaDataToObject(Row row, ExecutionOptions options) {
        Column column = new Column(Table.class, row.get("TABLE_CAT", String.class), row.get("TABLE_SCHEM", String.class), row.get("TABLE_NAME", String.class), row.get("COLUMN_NAME", String.class))
                .setPosition(row.get("ORDINAL_POSITION", Integer.class))
                .setRemarks(StringUtils.trimToNull(row.get("REMARKS", String.class)))
                .setNullable(row.get("IS_NULLABLE", false));
        if (row.get("IS_AUTOINCREMENT", false)) {
            column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
        }
        column.setType(readDataType(row, options));

        return column;
    }

    @Override
    protected QueryResult getRawMetaData(ExecutionOptions options) throws DatabaseException {
        DatabaseMetaData metaData = ((JdbcConnection) options.getRuntimeEnvironment().getTargetDatabase().getConnection()).getMetaData();

        try {
            return new QueryResult(JdbcUtils.extract(metaData.getColumns(
                    getCatalogName(),
                    getSchemaName(),
                    getTableName(),
                    getColumnName())));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    protected DataType readDataType(Row row, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        if (database instanceof OracleDatabase) {
            String dataType = row.get("DATA_TYPE", String.class);
            dataType = dataType.replace("VARCHAR2", "VARCHAR");
            dataType = dataType.replace("NVARCHAR2", "NVARCHAR");

            DataType type = new DataType(dataType);
//            type.setDataTypeId(dataType);
            if (dataType.equalsIgnoreCase("NUMBER")) {
                type.setColumnSize(row.get("DATA_PRECISION", Integer.class));
//                if (type.getColumnSize() == null) {
//                    type.setColumnSize(38);
//                }
                type.setDecimalDigits(row.get("DATA_SCALE", Integer.class));
//                if (type.getDecimalDigits() == null) {
//                    type.setDecimalDigits(0);
//                }
//            type.setRadix(10);
            } else {
                type.setColumnSize(row.get("DATA_LENGTH", Integer.class));

                if (dataType.equalsIgnoreCase("NCLOB")) {
                    //no attributes
                } else if (dataType.equalsIgnoreCase("NVARCHAR") || dataType.equalsIgnoreCase("NCHAR")) {
                    //data length is in bytes but specified in chars
                    type.setColumnSize(type.getColumnSize() / 2);
                    type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
                } else {
                    String charUsed = row.get("CHAR_USED", String.class);
                    DataType.ColumnSizeUnit unit = null;
                    if ("C".equals(charUsed)) {
                        unit = DataType.ColumnSizeUnit.CHAR;
                        type.setColumnSize(type.getColumnSize() / 2);
                    }
                    type.setColumnSizeUnit(unit);
                }
            }


            return type;
        }

        String columnTypeName = row.get("TYPE_NAME", String.class);

        if (database instanceof FirebirdDatabase) {
            if (columnTypeName.equals("BLOB SUB_TYPE 0")) {
                columnTypeName = "BLOB";
            }
            if (columnTypeName.equals("BLOB SUB_TYPE 1")) {
                columnTypeName = "CLOB";
            }
        }

//TODO        if (database instanceof MySQLDatabase && (columnTypeName.equalsIgnoreCase("ENUM") || columnTypeName.equalsIgnoreCase("SET"))) {
//            try {
//                String boilerLength;
//                if (columnTypeName.equalsIgnoreCase("ENUM"))
//                    boilerLength = "7";
//                else // SET
//                    boilerLength = "6";
//                List<String> enumValues = ExecutorService.getInstance().getExecutor(database).query(new RawSqlStatement("SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING(COLUMN_TYPE, " + boilerLength + ", LENGTH(COLUMN_TYPE) - " + boilerLength + " - 1 ), \"','\", 1 + units.i + tens.i * 10) , \"','\", -1)\n" +
//                        "FROM INFORMATION_SCHEMA.COLUMNS\n" +
//                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) units\n" +
//                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) tens\n" +
//                        "WHERE TABLE_NAME = '"+column.getRelation().getName()+"' \n" +
//                        "AND COLUMN_NAME = '"+column.getName()+"'")).toList(String.class);
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
        Integer columnSize = row.get("COLUMN_SIZE", Integer.class);
        // don't set size for types like int4, int8 etc
        if (database.dataTypeIsNotModifiable(columnTypeName)) {
            columnSize = null;
        }

        Integer decimalDigits = row.get("DECIMAL_DIGITS", Integer.class);
        if (decimalDigits != null && decimalDigits.equals(0)) {
            decimalDigits = null;
        }

        Integer radix = row.get("NUM_PREC_RADIX", Integer.class);

        Integer characterOctetLength = row.get("CHAR_OCTET_LENGTH", Integer.class);

        if (database instanceof DB2Database) {
            String typeName = row.get("TYPE_NAME", String.class);
            if (typeName.equalsIgnoreCase("DBCLOB") || typeName.equalsIgnoreCase("GRAPHIC") || typeName.equalsIgnoreCase("VARGRAPHIC")) {
                if (columnSize != null) {
                    columnSize = columnSize / 2; //Stored as double length chars
                }
            }
        }


        DataType type = new DataType(columnTypeName);
        type.setDataTypeId(dataType);
        type.setColumnSize(columnSize);
        type.setDecimalDigits(decimalDigits);
        type.setRadix(radix);
        type.setCharacterOctetLength(characterOctetLength);
        type.setColumnSizeUnit(columnSizeUnit);

        return type;
    }
}