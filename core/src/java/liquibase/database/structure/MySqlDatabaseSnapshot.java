package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;

public class MySqlDatabaseSnapshot extends SqlDatabaseSnapshot {
	
	/**
	 * Table schema cache 
	 */
	private static Map<String, Map<String, List<String>>> schemaCache = new HashMap<String, Map<String, List<String>>>();
	
	/**
     * Creates an empty database snapshot
     */
    public MySqlDatabaseSnapshot () {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public MySqlDatabaseSnapshot (Database database) throws JDBCException {
        this(database, null, null);
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public MySqlDatabaseSnapshot (Database database, String schema) throws JDBCException {
        this(database, null, schema);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public MySqlDatabaseSnapshot (Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        this(database, statusListeners, database.getDefaultSchemaName());
    }

    /**
     * Creates a snapshot of the given database.
     */
    public MySqlDatabaseSnapshot  (Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
    	super (database, statusListeners, requestedSchema);
    }
    
    /**
     * MySQL specific implementation
     */
    protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, JDBCException {

    	String columnTypeName = rs.getString("TYPE_NAME");
        String columnName     = rs.getString("COLUMN_NAME");
        String tableName      = rs.getString("TABLE_NAME");
        String schemaName     = rs.getString("TABLE_CAT");
        
        Map<String, List<String>> tableSchema = new HashMap<String, List<String>>();
        
        if (!schemaCache.containsKey(tableName)) {
        	
        	Statement selectStatement = database.getConnection().createStatement();
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
        		String tmpDefaultValue = (String)database.convertDatabaseValueToJavaObject(tableSchema.get(columnName).get(1), columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits());
        		if ("".equals(tmpDefaultValue)) {
        			columnInfo.setDefaultValue(null);
        		} else {
        			columnInfo.setDefaultValue(tmpDefaultValue);
        		}
        	} catch (ParseException e) {
        		throw new JDBCException(e);
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

    protected String convertPrimaryKeyName(String pkName) throws SQLException {
        if (pkName.equals("PRIMARY")) {
            return null;
        } else {
            return pkName;
        }
    }
}