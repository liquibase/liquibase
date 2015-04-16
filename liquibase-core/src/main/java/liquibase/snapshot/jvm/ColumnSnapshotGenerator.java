package liquibase.snapshot.jvm;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.*;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.SqlUtil;
import liquibase.util.StringUtils;

import java.sql.*;
import java.util.List;

public class ColumnSnapshotGenerator extends JdbcSnapshotGenerator {

    public ColumnSnapshotGenerator() {
        super(Column.class, new Class[]{Table.class, View.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Relation relation = ((Column) example).getRelation();
        if (((Column) example).getComputed() != null && ((Column) example).getComputed()) {
            return example;
        }
        Schema schema = relation.getSchema();

        List<CachedRow> columnMetadataRs = null;
        try {

            JdbcDatabaseSnapshot.CachingDatabaseMetaData databaseMetaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();

            columnMetadataRs = databaseMetaData.getColumns(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), relation.getName(), example.getName());

            if (columnMetadataRs.size() > 0) {
                CachedRow data = columnMetadataRs.get(0);
                Column column = readColumn(data, relation, database);

                if (column != null && database instanceof MSSQLDatabase && database.getDatabaseMajorVersion() >= 8) {
                    String sql;
                    if (database.getDatabaseMajorVersion() >= 9) {
                        // SQL Server 2005 or later
                        // https://technet.microsoft.com/en-us/library/ms177541.aspx
                        sql =
                                "SELECT CAST([ep].[value] AS [nvarchar](MAX)) AS [REMARKS] " +
                                "FROM [sys].[extended_properties] AS [ep] " +
                                "WHERE [ep].[class] = 1 " +
                                "AND [ep].[major_id] = OBJECT_ID(N'" + database.escapeStringForDatabase(database.escapeTableName(schema.getCatalogName(), schema.getName(), relation.getName())) + "') " +
                                "AND [ep].[minor_id] = COLUMNPROPERTY([ep].[major_id], N'" + database.escapeStringForDatabase(column.getName()) + "', 'ColumnId') " +
                                "AND [ep].[name] = 'MS_Description'";
                    } else {
                        // SQL Server 2000
                        // https://technet.microsoft.com/en-us/library/aa224810%28v=sql.80%29.aspx
                        sql =
                                "SELECT CAST([p].[value] AS [ntext]) AS [REMARKS] " +
                                "FROM [dbo].[sysproperties] AS [p] " +
                                "WHERE [p].[id] = OBJECT_ID(N'" + database.escapeStringForDatabase(database.escapeTableName(schema.getCatalogName(), schema.getName(), relation.getName())) + "') " +
                                "AND [p].[smallid] = COLUMNPROPERTY([p].[id], N'" + database.escapeStringForDatabase(column.getName()) + "', 'ColumnId') " +
                                "AND [p].[type] = 4 " +
                                "AND [p].[name] = 'MS_Description'";
                    }

                    List<String> remarks = ExecutorService.getInstance().getExecutor(snapshot.getDatabase()).queryForList(new RawSqlStatement(sql), String.class);
                    if (remarks != null && remarks.size() > 0) {
                        column.setRemarks(StringUtils.trimToNull(remarks.iterator().next()));
                    }
                }

                return column;
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
                    Column exampleColumn = new Column().setRelation(relation).setName(StringUtils.trimToNull(row.getString("COLUMN_NAME")));
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
        column.setName(StringUtils.trimToNull(rawColumnName));
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
                    String selectStatement;
                    if (database.getDatabaseProductName().startsWith("DB2 UDB for AS/400")) {
                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + rawSchemaName + "." + rawTableName + " where 0=1";
                        LogFactory.getLogger().debug("rawCatalogName : <" + rawCatalogName + ">");
                        LogFactory.getLogger().debug("rawSchemaName : <" + rawSchemaName + ">");
                        LogFactory.getLogger().debug("rawTableName : <" + rawTableName + ">");
                        LogFactory.getLogger().debug("raw selectStatement : <" + selectStatement + ">");
                        
                        
                    }
                    else{
                        selectStatement = "select " + database.escapeColumnName(rawCatalogName, rawSchemaName, rawTableName, rawColumnName) + " from " + database.escapeTableName(rawCatalogName, rawSchemaName, rawTableName) + " where 0=1";
                    }
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
//                if (type.getColumnSize() == null) {
//                    type.setColumnSize(38);
//                }
                type.setDecimalDigits(columnMetadataResultSet.getInt("DATA_SCALE"));
//                if (type.getDecimalDigits() == null) {
//                    type.setDecimalDigits(0);
//                }
//            type.setRadix(10);
            } else {
                type.setColumnSize(columnMetadataResultSet.getInt("DATA_LENGTH"));

                if (dataType.equalsIgnoreCase("NCLOB") || dataType.equalsIgnoreCase("BLOB") || dataType.equalsIgnoreCase("CLOB")) {
                    type.setColumnSize(null);
                } else if (dataType.equalsIgnoreCase("NVARCHAR") || dataType.equalsIgnoreCase("NCHAR")) {
                    type.setColumnSize(columnMetadataResultSet.getInt("CHAR_LENGTH"));
                    type.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
                } else {
                    String charUsed = columnMetadataResultSet.getString("CHAR_USED");
                    DataType.ColumnSizeUnit unit = null;
                    if ("C".equals(charUsed)) {
                        unit = DataType.ColumnSizeUnit.CHAR;
                        type.setColumnSize(columnMetadataResultSet.getInt("CHAR_LENGTH"));
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

        if (database instanceof MySQLDatabase && (columnTypeName.equalsIgnoreCase("ENUM") || columnTypeName.equalsIgnoreCase("SET"))) {
            try {
                String boilerLength;
                if (columnTypeName.equalsIgnoreCase("ENUM"))
                    boilerLength = "7";
                else // SET
                    boilerLength = "6";
                List<String> enumValues = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement("SELECT DISTINCT SUBSTRING_INDEX(SUBSTRING_INDEX(SUBSTRING(COLUMN_TYPE, " + boilerLength + ", LENGTH(COLUMN_TYPE) - " + boilerLength + " - 1 ), \"','\", 1 + units.i + tens.i * 10) , \"','\", -1)\n" +
                        "FROM INFORMATION_SCHEMA.COLUMNS\n" +
                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) units\n" +
                        "CROSS JOIN (SELECT 0 AS i UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) tens\n" +
                        "WHERE TABLE_NAME = '"+column.getRelation().getName()+"' \n" +
                        "AND COLUMN_NAME = '"+column.getName()+"'"), String.class);
                String enumClause = "";
                for (String enumValue : enumValues) {
                    enumClause += "'"+enumValue+"', ";
                }
                enumClause = enumClause.replaceFirst(", $", "");
                return new DataType(columnTypeName + "("+enumClause+")");
            } catch (DatabaseException e) {
                LogFactory.getLogger().warning("Error fetching enum values", e);
            }
        }
        DataType.ColumnSizeUnit columnSizeUnit = DataType.ColumnSizeUnit.BYTE;

        int dataType = columnMetadataResultSet.getInt("DATA_TYPE");
        Integer columnSize = null;
        Integer decimalDigits = null;
        if (!database.dataTypeIsNotModifiable(columnTypeName)) { // don't set size for types like int4, int8 etc
            columnSize = columnMetadataResultSet.getInt("COLUMN_SIZE");
            decimalDigits = columnMetadataResultSet.getInt("DECIMAL_DIGITS");
            if (decimalDigits != null && decimalDigits.equals(0)) {
                decimalDigits = null;
            }
        }

        Integer radix = columnMetadataResultSet.getInt("NUM_PREC_RADIX");

        Integer characterOctetLength = columnMetadataResultSet.getInt("CHAR_OCTET_LENGTH");

        if (database instanceof DB2Database) {
            String typeName = columnMetadataResultSet.getString("TYPE_NAME");
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

    protected Object readDefaultValue(CachedRow columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        if (database instanceof MSSQLDatabase) {
            Object defaultValue = columnMetadataResultSet.get("COLUMN_DEF");

            if (defaultValue != null && defaultValue instanceof String) {
                if (defaultValue.equals("(NULL)")) {
                    columnMetadataResultSet.set("COLUMN_DEF", null);
                }
            }
        }

        if (database instanceof OracleDatabase) {
            if (columnMetadataResultSet.get("COLUMN_DEF") == null) {
                columnMetadataResultSet.set("COLUMN_DEF", columnMetadataResultSet.get("DATA_DEFAULT"));

                if (columnMetadataResultSet.get("COLUMN_DEF") != null && ((String) columnMetadataResultSet.get("COLUMN_DEF")).equalsIgnoreCase("NULL")) {
                    columnMetadataResultSet.set("COLUMN_DEF", null);
                }

                if (columnMetadataResultSet.get("VIRTUAL_COLUMN").equals("YES")) {
                    columnMetadataResultSet.set("COLUMN_DEF", "GENERATED ALWAYS AS ("+columnMetadataResultSet.get("COLUMN_DEF")+")");
                }
            }

        }

        return SqlUtil.parseValue(database, columnMetadataResultSet.get("COLUMN_DEF"), columnInfo.getType());
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
