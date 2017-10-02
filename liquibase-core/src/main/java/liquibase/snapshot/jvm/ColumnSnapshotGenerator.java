package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnSnapshotGenerator extends JdbcSnapshotGenerator {

    private static final String LIQUIBASE_COMPLETE = "liquibase-complete";

    private Pattern postgresStringValuePattern = Pattern.compile("'(.*)'::[\\w ]+");
    private Pattern postgresNumberValuePattern = Pattern.compile("(\\d*)::[\\w ]+");


    public ColumnSnapshotGenerator() {
        super(Column.class, new Class[]{Table.class, View.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Relation relation = ((Column) example).getRelation();
        if ((((Column) example).getComputed() != null) && ((Column) example).getComputed()) {
            return example;
        }
        Schema schema = relation.getSchema();

        List<CachedRow> columnMetadataRs = null;
        try {

            Column column = null;

            if (example.getAttribute(LIQUIBASE_COMPLETE, false)) {
                column = (Column) example;
                example.setAttribute(LIQUIBASE_COMPLETE, null);
            } else {
                JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache();

                columnMetadataRs = databaseMetaData.getColumns(
                        ((AbstractJdbcDatabase) database).getJdbcCatalogName(schema),
                        ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema),
                        relation.getName(),
                        example.getName()
                );

                if (!columnMetadataRs.isEmpty()) {
                    CachedRow data = columnMetadataRs.get(0);
                    column = readColumn(data, relation, database);
                    setAutoIncrementDetails(column, database, snapshot);
                }
            }

            return column;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Column.class)) {
            return;
        }
        if (foundObject instanceof Relation) {
            Database database = snapshot.getDatabase();
            Relation relation = (Relation) foundObject;
            List<CachedRow> allColumnsMetadataRs = null;
            try {

                JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache();

                Schema schema;

                schema = relation.getSchema();
                allColumnsMetadataRs = databaseMetaData.getColumns(
                        ((AbstractJdbcDatabase) database).getJdbcCatalogName(schema),
                        ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema),
                        relation.getName(),
                        null);

                /*
                 * Microsoft SQL Server, SAP SQL Anywhere and probably other RDBMS guarantee non-duplicate
                 * ORDINAL_POSITIONs for the columns of a single table. But they do not guarantee there are no gaps
                 * in that integers (e.g. if columns have been deleted). So we need to check for that and renumber
                 * if needed.
                 */
                TreeMap<Integer, CachedRow> treeSet = new TreeMap<>();
                for (CachedRow row : allColumnsMetadataRs) {
                    treeSet.put(row.getInt("ORDINAL_POSITION"), row);
                }
                Logger log = LogService.getLog(getClass());

                // Now we can iterate through the sorted list and repair if needed.
                int currentOrdinal = 0;
                for (CachedRow row : treeSet.values()) {
                    currentOrdinal++;
                    int rsOrdinal = row.getInt("ORDINAL_POSITION");
                    if (rsOrdinal != currentOrdinal) {
                        log.debug(
                                LogType.LOG, String.format(
                                        "Repairing ORDINAL_POSITION with gaps for table=%s, column name=%s, " +
                                                "bad ordinal=%d, new ordinal=%d",
                                        relation.getName(),
                                        row.getString("COLUMN_NAME"),
                                        rsOrdinal,
                                        currentOrdinal
                                )
                        );
                        row.set("ORDINAL_POSITION", currentOrdinal);
                    }
                }

                // Iterate through all (repaired) rows and add the columns to our result.
                for (CachedRow row : allColumnsMetadataRs) {
                    Column column = readColumn(row, relation, database);
                    setAutoIncrementDetails(column, database, snapshot);
                    column.setAttribute(LIQUIBASE_COMPLETE, true);
                    relation.getColumns().add(column);
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
            }
        }

    }

    protected void setAutoIncrementDetails(Column column, Database database, DatabaseSnapshot snapshot) {
        if ((column.getAutoIncrementInformation() != null) && (database instanceof MSSQLDatabase) && (database
            .getConnection() != null) && !(database.getConnection() instanceof OfflineConnection)) {
            Map<String, Column.AutoIncrementInformation> autoIncrementColumns = (Map) snapshot.getScratchData("autoIncrementColumns");
            if (autoIncrementColumns == null) {
                autoIncrementColumns = new HashMap<>();
                Executor executor = ExecutorService.getInstance().getExecutor(database);
                try {
                    List<Map<String, ?>> rows = executor.queryForList(new RawSqlStatement("select object_schema_name(object_id) as schema_name, object_name(object_id) as table_name, name as column_name, cast(seed_value as bigint) as start_value, cast(increment_value as bigint) as increment_by from sys.identity_columns"));
                    for (Map row : rows) {
                        String schemaName = (String) row.get("SCHEMA_NAME");
                        String tableName = (String) row.get("TABLE_NAME");
                        String columnName = (String) row.get("COLUMN_NAME");
                        Long startValue = (Long) row.get("START_VALUE");
                        Long incrementBy = (Long) row.get("INCREMENT_BY");

                        Column.AutoIncrementInformation info = new Column.AutoIncrementInformation(startValue, incrementBy);
                        autoIncrementColumns.put(schemaName+"."+tableName+"."+columnName, info);
                    }
                    snapshot.setScratchData("autoIncrementColumns", autoIncrementColumns);
                } catch (DatabaseException e) {
                    LogService.getLog(getClass()).info(LogType.LOG, "Could not read identity information", e);
                }
            }
            if ((column.getRelation() != null) && (column.getSchema() != null)) {
                Column.AutoIncrementInformation autoIncrementInformation = autoIncrementColumns.get(column.getSchema().getName() + "." + column.getRelation().getName() + "." + column.getName());
                if (autoIncrementInformation != null) {
                    column.setAutoIncrementInformation(autoIncrementInformation);
                }
            }
        }
    }

    protected Column readColumn(CachedRow columnMetadataResultSet, Relation table, Database database) throws SQLException, DatabaseException {
        String rawTableName = (String) columnMetadataResultSet.get("TABLE_NAME");
        String rawColumnName = (String) columnMetadataResultSet.get("COLUMN_NAME");
        String rawSchemaName = StringUtils.trimToNull((String) columnMetadataResultSet.get("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull((String) columnMetadataResultSet.get("TABLE_CAT"));
        String remarks = StringUtils.trimToNull((String) columnMetadataResultSet.get("REMARKS"));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }
        Integer position = columnMetadataResultSet.getInt("ORDINAL_POSITION");


        Column column = new Column();
        column.setName(StringUtils.trimToNull(rawColumnName));
        column.setRelation(table);
        column.setRemarks(remarks);
        column.setOrder(position);

        if ((columnMetadataResultSet.get("IS_FILESTREAM") != null) && (Boolean) columnMetadataResultSet.get
            ("IS_FILESTREAM"))  {
            column.setAttribute("fileStream", true);
        }
        if ((columnMetadataResultSet.get("IS_ROWGUIDCOL") != null) && (Boolean) columnMetadataResultSet.get
            ("IS_ROWGUIDCOL"))  {
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
            int nullable = columnMetadataResultSet.getInt("NULLABLE");
            if (nullable == DatabaseMetaData.columnNoNulls) {
                column.setNullable(false);
            } else if (nullable == DatabaseMetaData.columnNullable) {
                column.setNullable(true);
            } else if (nullable == DatabaseMetaData.columnNullableUnknown) {
                LogService.getLog(getClass()).info(LogType.LOG, "Unknown nullable state for column " + column.toString() + ". Assuming nullable");
                column.setNullable(true);
            }
        }

        if (database.supportsAutoIncrement()) {
            if (table instanceof Table) {
                if (database instanceof OracleDatabase) {
                    String data_default = StringUtils.trimToEmpty((String) columnMetadataResultSet.get("DATA_DEFAULT")).toLowerCase();
                    if (data_default.contains("iseq$$") && data_default.endsWith("nextval")) {
                        column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                    }
                } else {
                    if (columnMetadataResultSet.containsColumn("IS_AUTOINCREMENT")) {
                        String isAutoincrement = (String) columnMetadataResultSet.get("IS_AUTOINCREMENT");
                        isAutoincrement = StringUtils.trimToNull(isAutoincrement);
                        if (isAutoincrement == null) {
                            column.setAutoIncrementInformation(null);
                        } else if ("YES".equals(isAutoincrement)) {
                            column.setAutoIncrementInformation(new Column.AutoIncrementInformation());
                        } else if ("NO".equals(isAutoincrement)) {
                            column.setAutoIncrementInformation(null);
                        } else if ("".equals(isAutoincrement)) {
                            LogService.getLog(getClass()).info(LogType.LOG, "Unknown auto increment state for column " + column.toString() + ". Assuming not auto increment");
                            column.setAutoIncrementInformation(null);
                        } else {
                            throw new UnexpectedLiquibaseException("Unknown is_autoincrement value: '" + isAutoincrement + "'");
                        }
                    } else {
                        //probably older version of java, need to select from the column to find out if it is auto-increment
                        String selectStatement;
                        if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
                            LogService.getLog(getClass()).debug(LogType.LOG, "rawCatalogName : <" + rawCatalogName + ">");
                            LogService.getLog(getClass()).debug(LogType.LOG, "rawSchemaName : <" + rawSchemaName + ">");
                            LogService.getLog(getClass()).debug(LogType.LOG, "rawTableName : <" + rawTableName + ">");
                            LogService.getLog(getClass()).debug(LogType.LOG, "raw selectStatement : <" + selectStatement + ">");


                        } else {
                            selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
                        }
                        LogService.getLog(getClass()).debug(LogType.LOG, "Checking " + rawTableName + "." + rawCatalogName + " for auto-increment with SQL: '" + selectStatement + "'");
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

        // TODO Is uppercasing the potential function name always a good idea? In theory, we could get a quoted function name (inprobable, but not impossible)
        if ((defaultValue != null) && (defaultValue instanceof DatabaseFunction) && ((DatabaseFunction) defaultValue)
            .getValue().matches("\\w+")) {
            defaultValue = new DatabaseFunction(((DatabaseFunction) defaultValue).getValue().toUpperCase());
        }
        column.setDefaultValue(defaultValue);
        column.setDefaultValueConstraintName(columnMetadataResultSet.getString("COLUMN_DEF_NAME"));

        return column;
    }

    /**
     * Processes metadata of a column, e.g. name, type and default value. We start with the result of the JDBC
     * {@link DatabaseMetaData}.getColumns() method. Depending on Database, additional columns might be present.
     *
     * @param columnMetadataResultSet the result from the JDBC getColumns() call for the column
     * @param column                  basic definition of the column
     * @param database                the database from which the column originates
     * @return a DataType object with detailed information about the type
     * @throws SQLException If an error occurs during processing (mostly caused by Exceptions in JDBC calls)
     */
    protected DataType readDataType(CachedRow columnMetadataResultSet, Column column, Database database) throws SQLException, DatabaseException {

        String columnTypeName = (String) columnMetadataResultSet.get("TYPE_NAME");

        if (database instanceof MSSQLDatabase) {
            if ("numeric() identity".equalsIgnoreCase(columnTypeName)) {
                columnTypeName = "numeric";
            } else if ("decimal() identity".equalsIgnoreCase(columnTypeName)) {
                columnTypeName = "decimal";
            } else if ("xml".equalsIgnoreCase(columnTypeName)) {
                columnMetadataResultSet.set("COLUMN_SIZE", null);
                columnMetadataResultSet.set("DECIMAL_DIGITS", null);
            } else if ("datetimeoffset".equalsIgnoreCase(columnTypeName)) {
                columnMetadataResultSet.set("COLUMN_SIZE", columnMetadataResultSet.getInt("DECIMAL_DIGITS"));
                columnMetadataResultSet.set("DECIMAL_DIGITS", null);
            } else if ("time".equalsIgnoreCase(columnTypeName)) {
                columnMetadataResultSet.set("COLUMN_SIZE", columnMetadataResultSet.getInt("DECIMAL_DIGITS"));
                columnMetadataResultSet.set("DECIMAL_DIGITS", null);
            }
        }

        if (database instanceof FirebirdDatabase) {
            if ("BLOB SUB_TYPE 0".equals(columnTypeName)) {
                columnTypeName = "BLOB";
            }
            if ("BLOB SUB_TYPE 1".equals(columnTypeName)) {
                columnTypeName = "CLOB";
            }
        }

        if ((database instanceof MySQLDatabase) && ("ENUM".equalsIgnoreCase(columnTypeName) || "SET".equalsIgnoreCase
            (columnTypeName))) {
            try {
                String boilerLength;
                if ("ENUM".equalsIgnoreCase(columnTypeName))
                    boilerLength = "7";
                else // SET
                    boilerLength = "6";
                List<String> enumValues = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement("SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING(COLUMN_TYPE, " + boilerLength + ", LENGTH(COLUMN_TYPE) - " + boilerLength + " - 1 ), \"','\", 1 + units.i + tens.i * 10) , \"','\", -1)\n" +
                        "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) units\n" +
                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) tens\n" +
                        "WHERE TABLE_NAME = '" + column.getRelation().getName() + "' \n" +
                        "AND COLUMN_NAME = '" + column.getName() + "'"), String.class);
                String enumClause = "";
                for (String enumValue : enumValues) {
                    enumClause += "'" + enumValue + "', ";
                }
                enumClause = enumClause.replaceFirst(", $", "");
                return new DataType(columnTypeName + "(" + enumClause + ")");
            } catch (DatabaseException e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Error fetching enum values", e);
            }
        }

        DataType.ColumnSizeUnit columnSizeUnit = DataType.ColumnSizeUnit.BYTE;

        int dataType = columnMetadataResultSet.getInt("DATA_TYPE");
        Integer columnSize = null;
        Integer decimalDigits = null;
        if (!database.dataTypeIsNotModifiable(columnTypeName)) { // don't set size for types like int4, int8 etc
            columnSize = columnMetadataResultSet.getInt("COLUMN_SIZE");
            decimalDigits = columnMetadataResultSet.getInt("DECIMAL_DIGITS");
            if ((decimalDigits != null) && decimalDigits.equals(0)) {
                decimalDigits = null;
            }
        }

        Integer radix = columnMetadataResultSet.getInt("NUM_PREC_RADIX");

        Integer characterOctetLength = columnMetadataResultSet.getInt("CHAR_OCTET_LENGTH");

        if (database instanceof DB2Database) {
            String typeName = columnMetadataResultSet.getString("TYPE_NAME");
            if ("DBCLOB".equalsIgnoreCase(typeName) || "GRAPHIC".equalsIgnoreCase(typeName)
                    || "VARGRAPHIC".equalsIgnoreCase(typeName)) {
                if (columnSize != null) {
                    columnSize = columnSize / 2; //Stored as double length chars
                }
            }
            if ("TIMESTAMP".equalsIgnoreCase(columnTypeName) && (decimalDigits == null)) { //actually a date
                columnTypeName = "DATE";
                dataType = Types.DATE;
            }
        }

        if ((database instanceof PostgresDatabase) && (columnSize != null) && columnSize.equals(Integer.MAX_VALUE)) {
            columnSize = null;
        }

        // For SAP (Sybase) SQL ANywhere, JDBC returns "LONG(2147483647) binary" (the number is 2^31-1)
        // but when creating a column, LONG BINARY must not have parameters.
        // The same applies to LONG(...) VARCHAR.
        if (database instanceof SybaseASADatabase) {
            if ("LONG BINARY".equalsIgnoreCase(columnTypeName)
                    || "LONG VARCHAR".equalsIgnoreCase(columnTypeName)) {
                columnSize = null;
            }

        }

        DataType type = new DataType(columnTypeName);
        type.setDataTypeId(dataType);

        /*
         * According to the description of DatabaseMetaData.getColumns, the content of the "COLUMN_SIZE" column is
         * pretty worthless for datetime/timestamp columns:
         *
         * "For datetime datatypes, this is the length in characters of the String representation
         * (assuming the maximum allowed precision of the fractional seconds component)."
         * In the case of TIMESTAMP columns, the information we are really looking for (fractional digits) is located
         * in the column DECIMAL_DIGITS.
         */
        int jdbcType = columnMetadataResultSet.getInt("DATA_TYPE");

        // Java 8 compatibility notes: When upgrading this project to JDK8 and beyond, also execute this if-branch
        // if jdbcType is TIMESTAMP_WITH_TIMEZONE (does not exist yet in JDK7)
        if (jdbcType == Types.TIMESTAMP) {

            if (decimalDigits == null) {
                type.setColumnSize(null);
            } else {
                type.setColumnSize((decimalDigits != database.getDefaultFractionalDigitsForTimestamp()) ?
                    decimalDigits : null
                );
            }

            type.setDecimalDigits(null);
        } else {
            type.setColumnSize(columnSize);
            type.setDecimalDigits(decimalDigits);
        }
        type.setRadix(radix);
        type.setCharacterOctetLength(characterOctetLength);
        type.setColumnSizeUnit(columnSizeUnit);


        return type;
    }

    protected Object readDefaultValue(CachedRow columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        if (database instanceof MSSQLDatabase) {
            Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");

            if ((defaultValue != null) && (defaultValue instanceof String)) {
                if ("(NULL)".equals(defaultValue)) {
                    columnMetadataResultSet.set("COLUMN_DEF", new DatabaseFunction("null"));
                }
            }
        }

        if (database instanceof OracleDatabase) {
            if (columnMetadataResultSet.get("COLUMN_DEF") == null) {
                columnMetadataResultSet.set("COLUMN_DEF", columnMetadataResultSet.get("DATA_DEFAULT"));

                if ((columnMetadataResultSet.get("COLUMN_DEF") != null) && "NULL".equalsIgnoreCase((String)
                    columnMetadataResultSet.get("COLUMN_DEF"))) {
                    columnMetadataResultSet.set("COLUMN_DEF", null);
                }

                Object columnDef = columnMetadataResultSet.get("COLUMN_DEF");
                if ("CHAR".equalsIgnoreCase(columnInfo.getType().getTypeName()) && (columnDef instanceof String) && !
                    ((String) columnDef).startsWith("'") && !((String) columnDef).endsWith("'")) {
                    return new DatabaseFunction((String) columnDef);
                }

                if ("YES".equals(columnMetadataResultSet.get("VIRTUAL_COLUMN"))) {
                    Object column_def = columnMetadataResultSet.get("COLUMN_DEF");
                    if ((column_def != null) && !"null".equals(column_def)) {
                        columnMetadataResultSet.set("COLUMN_DEF", "GENERATED ALWAYS AS (" + column_def + ")");
                    }
                }

                Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");
                if ((defaultValue != null) && (defaultValue instanceof String)) {
                    String lowerCaseDefaultValue = ((String) defaultValue).toLowerCase();
                    if (lowerCaseDefaultValue.contains("iseq$$") && lowerCaseDefaultValue.endsWith(".nextval")) {
                        columnMetadataResultSet.set("COLUMN_DEF", null);
                    }

                }
            }

        }

        if (database instanceof PostgresDatabase) {
            Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");
            if ((defaultValue != null) && (defaultValue instanceof String)) {
                Matcher matcher = postgresStringValuePattern.matcher((String) defaultValue);
                if (matcher.matches()) {
                    defaultValue = matcher.group(1);
                } else {
                    matcher = postgresNumberValuePattern.matcher((String) defaultValue);
                    if (matcher.matches()) {
                        defaultValue = matcher.group(1);
                    }

                }
                columnMetadataResultSet.set("COLUMN_DEF", defaultValue);
            }
        }

        if (database instanceof DB2Database) {
            if ((columnMetadataResultSet.get("COLUMN_DEF") != null) && "NULL".equalsIgnoreCase((String)
                columnMetadataResultSet.get("COLUMN_DEF"))) {
                columnMetadataResultSet.set("COLUMN_DEF", null);
            }
        }

        return SqlUtil.parseValue(database, columnMetadataResultSet.get("COLUMN_DEF"), columnInfo.getType());
    }

}
