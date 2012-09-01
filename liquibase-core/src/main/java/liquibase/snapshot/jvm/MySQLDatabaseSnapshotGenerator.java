package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MySQLDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
	

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    public DateFormat getDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"); //no ms in mysql
    }

    @Override
    protected boolean includeInSnapshot(DatabaseObject obj) {
        if (obj instanceof Index && obj.getName().equals("PRIMARY")) {
            return false;
        }
        return super.includeInSnapshot(obj);
    }

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


}
