package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.snapshot.*;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class ColumnSnapshotGenerator extends JdbcSnapshotGenerator {

    public ColumnSnapshotGenerator() {
        super(Column.class, new Class[]{Table.class, View.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Relation relation = ((Column) example).getRelation();
        Schema schema = relation.getSchema();

        List<CachedRow> columnMetadataRs = null;
        try {

            JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

            columnMetadataRs = databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), relation.getName(), example.getName());

            if (columnMetadataRs.size() > 0) {
                CachedRow data = columnMetadataRs.get(0);
                return readColumn(data, relation, database);
            } else {
                return null;
            }
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

                JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

                Schema schema;

                schema = relation.getSchema();
                allColumnsMetadataRs = databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), relation.getName(), null);

                for (CachedRow row : allColumnsMetadataRs) {
                    Column exampleColumn = new Column().setRelation(relation).setName(row.getString("COLUMN_NAME"));
                    relation.getColumns().add(exampleColumn);
                }
            } catch (Exception e) {
                throw new DatabaseException(e);
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


        Column column = new Column();
        column.setName(rawColumnName);
        column.setRelation(table);
        column.setRemarks(remarks);

        if (database instanceof OracleDatabase) {
            String nullable = columnMetadataResultSet.getString("NULLABLE");
            if (nullable.equals("Y")) {
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
                LogFactory.getLogger().info("Unknown nullable state for column " + column.toString() + ". Assuming nullable");
                column.setNullable(true);
            }
        }

        if (database.supportsAutoIncrement()) {
            if (table instanceof Table) {
                if (columnMetadataResultSet.containsColumn("IS_AUTOINCREMENT")) {
                    String isAutoincrement = (String) columnMetadataResultSet.get("IS_AUTOINCREMENT");
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
                } else {
                    //probably older version of java, need to select from the column to find out if it is auto-increment
                    String selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
                    LogFactory.getLogger().debug("Checking "+rawTableName+"."+rawCatalogName+" for auto-increment with SQL: '"+selectStatement+"'");
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

        DataType type = readDataType(columnMetadataResultSet, column, database);
        column.setType(type);

        column.setDefaultValue(readDefaultValue(columnMetadataResultSet, column, database));

        return column;
    }

    protected DataType readDataType(CachedRow columnMetadataResultSet, Column column, Database database) throws SQLException {

        if (database instanceof OracleDatabase) {
            String dataType = columnMetadataResultSet.getString("DATA_TYPE");
            dataType = dataType.replace("VARCHAR2", "VARCHAR");
            dataType = dataType.replace("NVARCHAR2", "NVARCHAR");

            DataType type = new DataType(dataType);
//            type.setDataTypeId(dataType);
            if (dataType.equalsIgnoreCase("NUMBER")) {
                type.setColumnSize(columnMetadataResultSet.getInt("DATA_PRECISION"));
                if (type.getColumnSize() == null) {
                    type.setColumnSize(38);
                }
                type.setDecimalDigits(columnMetadataResultSet.getInt("DATA_SCALE"));
//            type.setRadix(10);
            } else {
                type.setColumnSize(columnMetadataResultSet.getInt("DATA_LENGTH"));

                if (dataType.equalsIgnoreCase("NVARCHAR")) {
                    //data length is in bytes but specified in chars
                    type.setColumnSize(type.getColumnSize() / 2);
                    type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
                } else {
                    String charUsed = columnMetadataResultSet.getString("CHAR_USED");
                    DataType.ColumnSizeUnit unit = null;
                    if ("C".equals(charUsed)) {
                        unit = DataType.ColumnSizeUnit.CHAR;
                    }
                    type.setColumnSizeUnit(unit);
                }
            }


            return type;
        }

        String columnTypeName = (String) columnMetadataResultSet.get("TYPE_NAME");

        if (database instanceof FirebirdDatabase) {
            if (columnTypeName.equals("BLOB SUB_TYPE 0")) {
                columnTypeName = "BLOB";
            }
            if (columnTypeName.equals("BLOB SUB_TYPE 1")) {
                columnTypeName = "CLOB";
            }
        }

        DataType.ColumnSizeUnit columnSizeUnit = DataType.ColumnSizeUnit.BYTE;

        int dataType = columnMetadataResultSet.getInt("DATA_TYPE");
        Integer columnSize = columnMetadataResultSet.getInt("COLUMN_SIZE");
        // don't set size for types like int4, int8 etc
        if (database.dataTypeIsNotModifiable(columnTypeName)) {
            columnSize = null;
        }

        Integer decimalDigits = columnMetadataResultSet.getInt("DECIMAL_DIGITS");
        if (decimalDigits != null && decimalDigits.equals(0)) {
            decimalDigits = null;
        }

        Integer radix = columnMetadataResultSet.getInt("NUM_PREC_RADIX");

        Integer characterOctetLength = columnMetadataResultSet.getInt("CHAR_OCTET_LENGTH");

        DataType type = new DataType(columnTypeName);
        type.setDataTypeId(dataType);
        type.setColumnSize(columnSize);
        type.setDecimalDigits(decimalDigits);
        type.setRadix(radix);
        type.setCharacterOctetLength(characterOctetLength);
        type.setColumnSizeUnit(columnSizeUnit);

        return type;
    }

    protected Object readDefaultValue(CachedRow columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        if (database instanceof MSSQLDatabase) {
            Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");

            if (defaultValue != null && defaultValue instanceof String) {
                if (defaultValue.equals("(NULL)")) {
                    columnMetadataResultSet.set("COLUMN_DEF", null);
                }
            }
        }

        Object val = columnMetadataResultSet.get("COLUMN_DEF");
        if (!(val instanceof String)) {
            return val;
        }

        String stringVal = (String) val;
        if (stringVal.isEmpty()) {
            return null;
        }

        if (stringVal.startsWith("'") && stringVal.endsWith("'")) {
            stringVal = stringVal.substring(1, stringVal.length() - 1);
        } else if (stringVal.startsWith("((") && stringVal.endsWith("))")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("('") && stringVal.endsWith("')")) {
            stringVal = stringVal.substring(2, stringVal.length() - 2);
        } else if (stringVal.startsWith("(") && stringVal.endsWith(")")) {
            return new DatabaseFunction(stringVal.substring(1, stringVal.length() - 1));
        }

        int type = columnInfo.getType().getDataTypeId();
        String typeName = columnInfo.getType().getTypeName();
        Scanner scanner = new Scanner(stringVal.trim());
        try {
            if (type == Types.ARRAY) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.BIGINT && scanner.hasNextBigInteger()) {
                return scanner.nextBigInteger();
            } else if (type == Types.BINARY) {
                return new DatabaseFunction(stringVal.trim());
            } else if (type == Types.BIT) {
                if (stringVal.startsWith("b'")) { //mysql returns boolean values as b'0' and b'1'
                    stringVal = stringVal.replaceFirst("b'", "").replaceFirst("'$", "");
                }
                stringVal = stringVal.trim();
                if (scanner.hasNextBoolean()) {
                    return scanner.nextBoolean();
                } else {
                    return new Integer(stringVal);
                }
            } else if (type == Types.BLOB) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.BOOLEAN && scanner.hasNextBoolean()) {
                return scanner.nextBoolean();
            } else if (type == Types.CHAR) {
                return stringVal;
            } else if (type == Types.DATALINK) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.DATE) {
                if (typeName.equalsIgnoreCase("year")) {
                    return stringVal.trim();
                }
                if (zeroTime(stringVal)) {
                    return stringVal;
                }
                return new java.sql.Date(getDateFormat(database).parse(stringVal.trim()).getTime());
            } else if (type == Types.DECIMAL && scanner.hasNextBigDecimal()) {
                return scanner.nextBigDecimal();
            } else if (type == Types.DISTINCT) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.DOUBLE && scanner.hasNextDouble()) {
                return scanner.nextDouble();
            } else if (type == Types.FLOAT && scanner.hasNextFloat()) {
                return scanner.nextFloat();
            } else if (type == Types.INTEGER && scanner.hasNextInt()) {
                return scanner.nextInt();
            } else if (type == Types.JAVA_OBJECT) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.LONGNVARCHAR) {
                return stringVal;
            } else if (type == Types.LONGVARBINARY) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.LONGVARCHAR) {
                return stringVal;
            } else if (type == Types.NCHAR) {
                return stringVal;
            } else if (type == Types.NCLOB) {
                return stringVal;
            } else if (type == Types.NULL) {
                return null;
            } else if (type == Types.NUMERIC && scanner.hasNextBigDecimal()) {
                return scanner.nextBigDecimal();
            } else if (type == Types.NVARCHAR) {
                return stringVal;
            } else if (type == Types.OTHER) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.REAL) {
                return new BigDecimal(stringVal.trim());
            } else if (type == Types.REF) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.ROWID) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.SMALLINT && scanner.hasNextInt()) {
                return scanner.nextInt();
            } else if (type == Types.SQLXML) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.STRUCT) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.TIME) {
                if (zeroTime(stringVal)) {
                    return stringVal;
                }
                return new java.sql.Time(getTimeFormat(database).parse(stringVal).getTime());
            } else if (type == Types.TIMESTAMP) {
                if (zeroTime(stringVal)) {
                    return stringVal;
                }
                return new Timestamp(getDateTimeFormat(database).parse(stringVal).getTime());
            } else if (type == Types.TINYINT && scanner.hasNextInt()) {
                return scanner.nextInt();
            } else if (type == Types.VARBINARY) {
                return new DatabaseFunction(stringVal);
            } else if (type == Types.VARCHAR) {
                return stringVal;
            } else {
                LogFactory.getLogger().info("Unknown default value: value '" + stringVal + "' type " + typeName + " (" + type + "), assuming it is a function");
                return new DatabaseFunction(stringVal);
            }
        } catch (ParseException e) {
            return new DatabaseFunction(stringVal);
        }
    }

    private boolean zeroTime(String stringVal) {
        return stringVal.replace("-","").replace(":", "").replace(" ","").replace("0","").equals("");
    }

    protected DateFormat getDateFormat(Database database) {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    protected DateFormat getTimeFormat(Database database) {
        return new SimpleDateFormat("HH:mm:ss");
    }

    protected DateFormat getDateTimeFormat(Database database) {
        if (database instanceof MySQLDatabase) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //no ms in mysql
        }
        if (database instanceof MSSQLDatabase) {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); //no ms in mysql
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }


    //START CODE FROM SQLITEDatabaseSnapshotGenerator

////    @Override
////    protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
////        Database database = snapshot.getDatabase();
////        updateListeners("Reading columns for " + database.toString() + " ...");
////
////        if (database instanceof SQLiteDatabase) {
////            // ...work around for SQLite
////            for (Table cur_table : snapshot.getTables()) {
////                Statement selectStatement = null;
////                ResultSet rs = null;
////                try {
////                    selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
////                    rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
////                    if (rs == null) {
////                        rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
////                    }
////                    while ((rs != null) && rs.next()) {
////                        readColumnInfo(snapshot, schema, rs);
////                    }
////                } finally {
////                    if (rs != null) {
////                        try {
////                            rs.close();
////                        } catch (SQLException ignored) {
////                        }
////                    }
////                    if (selectStatement != null) {
////                        selectStatement.close();
////                    }
////                }
////            }
////        } else {
////            // ...if it is no SQLite database
////            Statement selectStatement = null;
////            ResultSet rs = null;
////            try {
////                selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
////                rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
////                while (rs.next()) {
////                    readColumnInfo(snapshot, schema, rs);
////                }
////            } finally {
////                if (rs != null) {
////                    try {
////                        rs.close();
////                    } catch (SQLException ignored) {
////                    }
////                }
////                if (selectStatement != null) {
////                    selectStatement.close();
////                }
////            }
////        }
////    }
//
////    private Column readColumnInfo(DatabaseSnapshot snapshot, String schema, ResultSet rs) throws SQLException, DatabaseException {
////        Database database = snapshot.getDatabase();
////        Column columnInfo = new Column();
////
////        String tableName = rs.getString("TABLE_NAME");
////        String columnName = rs.getString("COLUMN_NAME");
////        String schemaName = rs.getString("TABLE_SCHEM");
////        String catalogName = rs.getString("TABLE_CAT");
////
////        String upperCaseTableName = tableName.toUpperCase(Locale.ENGLISH);
////
////        if (database.isSystemTable(catalogName, schemaName, upperCaseTableName) ||
////                database.isLiquibaseTable(upperCaseTableName)) {
////            return null;
////        }
////
////        Table table = snapshot.getTable(tableName);
////        if (table == null) {
////            View view = snapshot.getView(tableName);
////            if (view == null) {
////                LogFactory.getLogger().debug("Could not find table or view " + tableName + " for column " + columnName);
////                return null;
////            } else {
////                columnInfo.setView(view);
////                view.getColumns().add(columnInfo);
////            }
////        } else {
////            columnInfo.setTable(table);
////            table.getColumns().add(columnInfo);
////        }
////
////        columnInfo.setName(columnName);
////        columnInfo.setDataType(rs.getInt("DATA_TYPE"));
////        columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
////        columnInfo.setDecimalDigits(rs.getInt("DECIMAL_POINTS"));
////        Object defaultValue = rs.getObject("COLUMN_DEF");
//////        try {
////            //todo columnInfo.setDefaultValue(TypeConverterFactory.getInstance().findTypeConverter(database).convertDatabaseValueToObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits(), database));
//////        } catch (ParseException e) {
//////            throw new DatabaseException(e);
//////        }
////
////        int nullable = rs.getInt("NULLABLE");
////        if (nullable == DatabaseMetaData.columnNoNulls) {
////            columnInfo.setNullable(false);
////        } else if (nullable == DatabaseMetaData.columnNullable) {
////            columnInfo.setNullable(true);
////        }
////
////        columnInfo.setPrimaryKey(snapshot.isPrimaryKey(columnInfo));
////        columnInfo.setAutoIncrement(isColumnAutoIncrement(database, schema, tableName, columnName));
////        String typeName = rs.getString("TYPE_NAME");
////        if (columnInfo.isAutoIncrement()) {
////            typeName += "{autoIncrement:true}";
////        }
////        columnInfo.setType(DataTypeFactory.getInstance().parse(typeName));
////
////        return columnInfo;
////    }
    //END CODE FROM SQLiteDatabaseSnapshotGenerator


    //method was from DerbyDatabaseSnapshotGenerator
//    @Override
//    protected Object readDefaultValue(Map<String, Object> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
//        Object val = columnMetadataResultSet.get("COLUMN_DEF");
//
//        if (val instanceof String && "GENERATED_BY_DEFAULT".equals(val)) {
//            return null;
//        }
//        return super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
//    }


    //START CODE FROM MysqlDatabaseSnapshotGenerator


    //    @Override
//    protected Object readDefaultValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
//            try {
//                Object tmpDefaultValue = columnInfo.getType().toLiquibaseType().sqlToObject(tableSchema.get(columnName).get(1), database);
//                // this just makes explicit the following implicit behavior defined in the mysql docs:
//                // "If an ENUM column is declared to permit NULL, the NULL value is a legal value for
//                // the column, and the default value is NULL. If an ENUM column is declared NOT NULL,
//                // its default value is the first element of the list of permitted values."
//                if (tmpDefaultValue == null && columnInfo.isNullable()) {
//                    columnInfo.setDefaultValue("NULL");
//                }
//                // column is NOT NULL, and this causes no "DEFAULT VALUE XXX" to be generated at all. per
//                // the above from MySQL docs, this will cause the first value in the enumeration to be the
//                // default.
//                else if (tmpDefaultValue == null) {
//                    columnInfo.setDefaultValue(null);
//                } else {
//                    columnInfo.setDefaultValue("'" + database.escapeStringForDatabase(tmpDefaultValue) + "'");
//                }
//            } catch (ParseException e) {
//                throw new DatabaseException(e);
//            }
//
//            // TEXT and BLOB column types always have null as default value
//        } else if (columnTypeName.toLowerCase().equals("text") || columnTypeName.toLowerCase().equals("blob")) {
//            columnInfo.setType(new DatabaseDataType(columnTypeName));
//            columnInfo.setDefaultValue(null);
//
//            // Parsing TIMESTAMP database.convertDatabaseValueToObject() produces incorrect results
//            // eg. for default value 0000-00-00 00:00:00 we have 0002-11-30T00:00:00.0 as parsing result
//        } else if (columnTypeName.toLowerCase().equals("timestamp") && !"CURRENT_TIMESTAMP".equals(tableSchema.get(columnName).get(1))) {
//            columnInfo.setType(new DatabaseDataType(columnTypeName));
//            columnInfo.setDefaultValue(tableSchema.get(columnName).get(1));
//        } else {
//            super.readDefaultValue(columnInfo, rs, database);
//        }
//
//    }

//    @Override
//    protected DatabaseDataType readDataType(ResultSet rs, Database database) throws SQLException {
//    	String columnTypeName = rs.getString("TYPE_NAME");
//        String columnName     = rs.getString("COLUMN_NAME");
//        String tableName      = rs.getString("TABLE_NAME");
//        String schemaName     = rs.getString("TABLE_CAT");
//
//        Map<String, List<String>> tableSchema = new HashMap<String, List<String>>();
//
//        if (!schemaCache.containsKey(tableName)) {
//
//            Statement selectStatement = null;
//            ResultSet rsColumnType = null;
//            try {
//                selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//                rsColumnType = selectStatement.executeQuery("DESC "+database.escapeTableName(schemaName, tableName));
//
//                while(rsColumnType.next()) {
//                    List<String> colSchema = new ArrayList<String>();
//                    colSchema.add(rsColumnType.getString("Type"));
//                    colSchema.add(rsColumnType.getString("Default"));
//                    tableSchema.put(rsColumnType.getString("Field"), colSchema);
//                }
//            } finally {
//                if (rsColumnType != null) {
//                    try {
//                        rsColumnType.close();
//                    } catch (SQLException ignore) { }
//                }
//                if (selectStatement != null) {
//                    try {
//                        selectStatement.close();
//                    } catch (SQLException ignore) { }
//                }
//            }
//
//
//            schemaCache.put(tableName, tableSchema);
//
//        }
//
//        tableSchema = schemaCache.get(tableName);
//
//        // Parse ENUM and SET column types correctly
//        if (columnTypeName.toLowerCase().startsWith("enum") || columnTypeName.toLowerCase().startsWith("set")) {
//
//            DatabaseDataType dataType = new DatabaseDataType(tableSchema.get(columnName).get(0));
//        	try {
//                Object tmpDefaultValue = dataType.toLiquibaseType().sqlToObject(tableSchema.get(columnName).get(1), database);
//                // this just makes explicit the following implicit behavior defined in the mysql docs:
//                // "If an ENUM column is declared to permit NULL, the NULL value is a legal value for
//                // the column, and the default value is NULL. If an ENUM column is declared NOT NULL,
//                // its default value is the first element of the list of permitted values."
//                if (tmpDefaultValue == null && columnInfo.isNullable()) {
//                    columnInfo.setDefaultValue("NULL");
//                }
//                // column is NOT NULL, and this causes no "DEFAULT VALUE XXX" to be generated at all. per
//                // the above from MySQL docs, this will cause the first value in the enumeration to be the
//                // default.
//                else if (tmpDefaultValue == null) {
//                    columnInfo.setDefaultValue(null);
//                } else {
//                    columnInfo.setDefaultValue("'" + database.escapeStringForDatabase(tmpDefaultValue) + "'");
//                }
//        	} catch (ParseException e) {
//        		throw new DatabaseException(e);
//        	}
//
//        // TEXT and BLOB column types always have null as default value
//        } else if (columnTypeName.toLowerCase().equals("text") || columnTypeName.toLowerCase().equals("blob")) {
//        	columnInfo.setType(new DatabaseDataType(columnTypeName));
//        	columnInfo.setDefaultValue(null);
//
//        // Parsing TIMESTAMP database.convertDatabaseValueToObject() produces incorrect results
//        // eg. for default value 0000-00-00 00:00:00 we have 0002-11-30T00:00:00.0 as parsing result
//        } else if (columnTypeName.toLowerCase().equals("timestamp") && !"CURRENT_TIMESTAMP".equals(tableSchema.get(columnName).get(1))) {
//        	columnInfo.setType(new DatabaseDataType(columnTypeName));
//        	columnInfo.setDefaultValue(tableSchema.get(columnName).get(1));
//        } else {
//        	super.readDefaultValue(columnInfo, rs, database);
//        }
//    }


//    @Override
//    protected ForeignKeyInfo readForeignKey(ResultSet importedKeyMetadataResultSet) throws DatabaseException, SQLException {
//        ForeignKeyInfo fkinfo= super.readForeignKey(importedKeyMetadataResultSet);
//        //MySQL in reality doesn't has schemas. It has databases that can have relations like schemas.
//        fkinfo.setPkTableSchema(cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("PKTABLE_CAT")));
//        fkinfo.setFkSchema(cleanObjectNameFromDatabase(importedKeyMetadataResultSet.getString("FKTABLE_CAT")));
//        return fkinfo;
//    }
//END CODE FROM MySQLDatabaseSNapshotGenerator

    //START CODE from InformixSnapshotGenerator
//    private static final Map<Integer, String> qualifiers = new HashMap<Integer, String>();
//
//    static {
//        qualifiers.put(0, "YEAR");
//        qualifiers.put(2, "MONTH");
//        qualifiers.put(4, "DAY");
//        qualifiers.put(6, "HOUR");
//        qualifiers.put(8, "MINUTE");
//        qualifiers.put(10, "SECOND");
//        qualifiers.put(11, "FRACTION(1)");
//        qualifiers.put(12, "FRACTION(2)");
//        qualifiers.put(13, "FRACTION(3)");
//        qualifiers.put(14, "FRACTION(4)");
//        qualifiers.put(15, "FRACTION(5)");
//    }
//    protected DataType readDataType(Map<String, Object> rs, Column column, Database database) throws SQLException {
//        // See http://publib.boulder.ibm.com/infocenter/idshelp/v115/topic/com.ibm.sqlr.doc/sqlr07.htm
//        String typeName = ((String) rs.get("TYPE_NAME")).toUpperCase();
//        if ("DATETIME".equals(typeName) || "INTERVAL".equals(typeName)) {
//            int collength = (Integer) rs.get("COLUMN_SIZE");
//            //int positions = collength / 256;
//            int firstQualifierType = (collength % 256) / 16;
//            int lastQualifierType = (collength % 256) % 16;
//            String type = "DATETIME".equals(typeName) ? "DATETIME" : "INTERVAL";
//            String firstQualifier = qualifiers.get(firstQualifierType);
//            String lastQualifier = qualifiers.get(lastQualifierType);
//            DataType dataTypeMetaData = new DataType(type + " " + firstQualifier + " TO " + lastQualifier);
//            dataTypeMetaData.setColumnSizeUnit(DataType.ColumnSizeUnit.BYTE);
//
//            return dataTypeMetaData;
//        } else {
//            return super.readDataType(rs, column, database);
//        }
//    }
    //END CODE FROM InformaixSnapshotGenerator

    //Code below was from OracleDatabaseSnapshotGenerator
    //    @Override
//    protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
//        findIntegerColumns(snapshot, schema);
//        super.readColumns(snapshot, schema, databaseMetaData);
//
//        /*
//          * Code Description:
//          * Finding all 'tablespace' attributes of column's PKs
//          * */
//        Database database = snapshot.getDatabase();
//        Statement statement = null;
//        ResultSet rs = null;
//        try {
//            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//
//            // Setting default schema name. Needed for correct statement generation
//            if (schema == null)
//                schema = database.convertRequestedSchemaToSchema(schema);
//
//            String query = "select ui.tablespace_name TABLESPACE, ucc.table_name TABLE_NAME, ucc.column_name COLUMN_NAME FROM all_indexes ui , all_constraints uc , all_cons_columns ucc where uc.constraint_type = 'P' and ucc.constraint_name = uc.constraint_name and uc.index_name = ui.index_name and uc.owner = '" + schema + "' and ui.table_owner = '" + schema + "' and ucc.owner = '" + schema + "'";
//            rs = statement.executeQuery(query);
//
//            while (rs.next()) {
//                Column column = snapshot.getColumn(rs.getString("TABLE_NAME"), rs.getString("COLUMN_NAME"));
//                // setting up tablespace property to column, to configure it's PK-index
//                if (column == null) {
//                    continue; //probably a different schema
//                }
//                column.setTablespace(rs.getString("TABLESPACE"));
//            }
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ignore) {
//                }
//            }
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        }
//
//    }
//
//    /**
//     * Method finds all INTEGER columns in snapshot's database
//     *
//     * @param snapshot current database snapshot
//     * @return String list with names of all INTEGER columns
//     * @throws java.sql.SQLException execute statement error
//     */
//    private List<String> findIntegerColumns(DatabaseSnapshot snapshot, String schema) throws SQLException, DatabaseException {
//
//        Database database = snapshot.getDatabase();
//        // Setting default schema name. Needed for correct statement generation
//        if (schema == null) {
//            schema = database.convertRequestedSchemaToSchema(schema);
//        }
//        Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
//        ResultSet integerListRS = null;
//        // Finding all columns created as 'INTEGER'
//        try {
//            integerListRS = statement.executeQuery("select TABLE_NAME, COLUMN_NAME from all_tab_columns where data_precision is null and data_scale = 0 and data_type = 'NUMBER' and owner = '" + schema + "'");
//            while (integerListRS.next()) {
//                integerList.add(integerListRS.getString("TABLE_NAME") + "." + integerListRS.getString("COLUMN_NAME"));
//            }
//        } finally {
//            if (integerListRS != null) {
//                try {
//                    integerListRS.close();
//                } catch (SQLException ignore) {
//                }
//            }
//
//            if (statement != null) {
//                try {
//                    statement.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        }
//
//
//        return integerList;
//    }
//
////    @Override
////    protected DatabaseDataType readDataType(ResultSet rs, Database database) throws SQLException {
////        if (integerList.contains(column.getTable().getName() + "." + column.getName())) {
////            column.setDataType(Types.INTEGER);
////        } else {
////            column.setDataType(rs.getInt("DATA_TYPE"));
////        }
////        column.setColumnSize(rs.getInt("COLUMN_SIZE"));
////        column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
////
////        // Set true, if precision should be initialize
////        column.setInitPrecision(
////                !((column.getDataType() == Types.DECIMAL ||
////                        column.getDataType() == Types.NUMERIC ||
////                        column.getDataType() == Types.REAL) && rs.getString("DECIMAL_DIGITS") == null)
////        );
////    }
//
//
    ////    @Override
////    protected Object readDefaultValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
////        super.readDefaultValue(columnInfo, rs, database);
////
////        // Exclusive setting for oracle INTEGER type
////        // Details:
////        // INTEGER means NUMBER type with 'data_precision IS NULL and scale = 0'
////        if (columnInfo.getDataType() == Types.INTEGER) {
////            columnInfo.setType(DataTypeFactory.getInstance().parse("INTEGER"));
////        }
////
////        String columnTypeName = rs.getString("TYPE_NAME");
////        if ("VARCHAR2".equals(columnTypeName)) {
////            int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
////            int columnSize = rs.getInt("COLUMN_SIZE");
////            if (columnSize == charOctetLength) {
////                columnInfo.setLengthSemantics(Column.ColumnSizeUnit.BYTE);
////            } else {
////                columnInfo.setLengthSemantics(Column.ColumnSizeUnit.CHAR);
////            }
////        }
////    }
}
