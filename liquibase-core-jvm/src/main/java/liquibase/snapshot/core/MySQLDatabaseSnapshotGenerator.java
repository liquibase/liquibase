package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.JdbcConnection;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.structure.Column;
import liquibase.exception.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySQLDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
	
	/**
	 * Table schema cache 
	 */
	private static Map<String, Map<String, List<String>>> schemaCache = new HashMap<String, Map<String, List<String>>>();

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }

    /**
     * MySQL specific implementation
     */
    @Override
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {

    	String columnTypeName = rs.getString("TYPE_NAME");
        String columnName     = rs.getString("COLUMN_NAME");
        String tableName      = rs.getString("TABLE_NAME");
        String schemaName     = rs.getString("TABLE_CAT");
        
        Map<String, List<String>> tableSchema = new HashMap<String, List<String>>();
        
        if (!schemaCache.containsKey(tableName)) {
        	
        	Statement selectStatement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            ResultSet rsColumnType = selectStatement.executeQuery("DESC "+database.escapeTableName(schemaName, tableName));
            
            while(rsColumnType.next()) {
            	List<String> colSchema = new ArrayList<String>();
            	colSchema.add(rsColumnType.getString("Type"));
            	colSchema.add(rsColumnType.getString("Default"));
            	tableSchema.put(rsColumnType.getString("Field"), colSchema);
            }
            
            rsColumnType.close();
            
            schemaCache.put(tableName, tableSchema);
        	
        }
        
        tableSchema = schemaCache.get(tableName);
        
        // Parse ENUM and SET column types correctly
        if (columnTypeName.toLowerCase().startsWith("enum") || columnTypeName.toLowerCase().startsWith("set")) {       	

        	columnInfo.setTypeName(tableSchema.get(columnName).get(0));
        	try {
        		String tmpDefaultValue = (String) TypeConverterFactory.getInstance().findTypeConverter(database).convertDatabaseValueToJavaObject(tableSchema.get(columnName).get(1), columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits(), database);
        		if ("".equals(tmpDefaultValue)) {
        			columnInfo.setDefaultValue(null);
        		} else {
        			columnInfo.setDefaultValue(tmpDefaultValue);
        		}
        	} catch (ParseException e) {
        		throw new DatabaseException(e);
        	}
        	
        // TEXT and BLOB column types always have null as default value 
        } else if (columnTypeName.toLowerCase().equals("text") || columnTypeName.toLowerCase().equals("blob")) {
        	columnInfo.setTypeName(columnTypeName);
        	columnInfo.setDefaultValue(null);
        	
        // Parsing TIMESTAMP database.convertDatabaseValueToJavaObject() produces incorrect results
        // eg. for default value 0000-00-00 00:00:00 we have 0002-11-30T00:00:00.0 as parsing result
        } else if (columnTypeName.toLowerCase().equals("timestamp") && !"CURRENT_TIMESTAMP".equals(tableSchema.get(columnName).get(1))) {
        	columnInfo.setTypeName(columnTypeName);
        	columnInfo.setDefaultValue(tableSchema.get(columnName).get(1));
        } else {
        	super.getColumnTypeAndDefValue(columnInfo, rs, database);
        }
    }

    @Override
    protected String convertPrimaryKeyName(String pkName) throws SQLException {
        if (pkName.equals("PRIMARY")) {
            return null;
        } else {
            return pkName;
        }
    }
}