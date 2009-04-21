package liquibase.database.sql;

import java.sql.ResultSet;
import java.sql.Statement;
import liquibase.database.*;
import liquibase.exception.JDBCException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropDefaultValueStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;

    public DropDefaultValueStatement(String schemaName, String tableName, String columnName, String columnDataType) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }
    	
        if (database instanceof MSSQLDatabase) {   
        	try {
        		if(database.getDatabaseProductVersion().startsWith("9")) { // SQL Server 2005
			      // SQL Server 2005 does not often work with the simpler query shown below
        			String query = "DECLARE @default sysname\n";
        			query += "SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('" + getSchemaName() + "." + getTableName() + "') AND name='" + columnName + "'\n";
        			query += "EXEC ('ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT ' + @default)";
        			//System.out.println("DROP QUERY : " + query);
        			return query;        		        		       
        		} else {
        			return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT select d.name from syscolumns c,sysobjects d, sysobjects t where c.id=t.id AND d.parent_obj=t.id AND d.type='D' AND t.type='U' AND c.name='"+getColumnName()+"' AND t.name='"+getTableName()+"'";        	
        		}
        	} catch(JDBCException e) {
        		return e.getMessage();
        	}
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof OracleDatabase || database instanceof SybaseASADatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof DerbyDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " WITH DEFAULT NULL";
        } else if (database instanceof MaxDBDatabase) {
          	return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof InformixDatabase) {
        	if (getColumnDataType() == null) {
                throw new StatementNotSupportedOnDatabaseException("Database requires columnDataType parameter", this, database);
        	}
        	return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY (" + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " " + getColumnDataType() + ")";
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " SET DEFAULT NULL";
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return !(database instanceof SQLiteDatabase);
    }
}
