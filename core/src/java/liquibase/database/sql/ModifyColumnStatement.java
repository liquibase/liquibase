package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents statements like "ALTER TABLE tablename MODIFY COLUMN VOIDED tinyint(1) NOT NULL"
 */
public class ModifyColumnStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnType;
    private Object defaultValue;
    private Set<ColumnConstraint> constraints = new HashSet<ColumnConstraint>();

    public ModifyColumnStatement(String schemaName, String tableName, String columnName, String columnType, Object defaultValue, ColumnConstraint... constraints) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
        this.defaultValue = defaultValue;
        if (constraints != null) {
            this.constraints.addAll(Arrays.asList(constraints));
        }
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

    public String getColumnType() {
        return columnType;
    }

    public Set<ColumnConstraint> getConstraints() {
        return constraints;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (isPrimaryKey() && (database instanceof CacheDatabase
                || database instanceof H2Database
                || database instanceof DB2Database
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase)) {
            throw new StatementNotSupportedOnDatabaseException("Adding primary key columns is not supported", this, database);
        }

        String alterTable = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName());
        
        // add "MODIFY"
        alterTable += " " + getModifyString(database) + " ";
        
        // add column name
        alterTable += database.escapeColumnName(getSchemaName(), getTableName(), getColumnName());
        
        alterTable += getPreDataTypeString(database); // adds a space if nothing else
        
        // add column type
        alterTable += database.getColumnType(getColumnType(), false);

        if (supportsExtraMetaData(database)) {
	        if (!isNullable()) {
	            alterTable += " NOT NULL";
	        } else {
	            if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
	                alterTable += " NULL";
	            }
	        }
	        
	        alterTable += getDefaultClause(database);
	        
	        if (isAutoIncrement()) {
	            alterTable += " " + database.getAutoIncrementClause();
	        }
	        
            if (isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }
        
        alterTable += getPostDataTypeString(database);
        
        return alterTable;
    }
    
    /**
     * Whether the ALTER command can take things like "DEFAULT VALUE" or "PRIMARY KEY" as well as type changes
     * 
     * @param database
     * @return true/false whether extra information can be included
     */
    private boolean supportsExtraMetaData(Database database) {
    	if (database instanceof MSSQLDatabase
    			|| database instanceof MySQLDatabase) {
    		return true;
    	}
    	
    	return false;
    }

	/**
     * @return either "MODIFY" or "ALTER COLUMN" depending on the current db
     */
    private String getModifyString(Database database) {
    	if (database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof MSSQLDatabase
                || database instanceof CacheDatabase) {
    		return "ALTER COLUMN";
    	}
    	else if (database instanceof SybaseASADatabase 
    				|| database instanceof SybaseDatabase
    				|| database instanceof MySQLDatabase) {
    		return "MODIFY";
    	}
    	else if (database instanceof OracleDatabase
    				|| database instanceof MaxDBDatabase) {
    		return "MODIFY (";
    	}
    	else {
    		return "ALTER COLUMN";
    	}
    }
    
    /**
     * @return the string that comes before the column type 
     * 		definition (like 'set data type' for derby or an open parentheses for Oracle)
     */
    private String getPreDataTypeString(Database database) {
    	if (database instanceof DerbyDatabase
    			|| database instanceof DB2Database) {
    		return " SET DATA TYPE ";
    	}
    	else if (database instanceof SybaseASADatabase 
    			|| database instanceof SybaseDatabase
    			|| database instanceof MSSQLDatabase
    			|| database instanceof MySQLDatabase
    			|| database instanceof HsqlDatabase
    			|| database instanceof CacheDatabase
    			|| database instanceof OracleDatabase
				|| database instanceof MaxDBDatabase) {
    		return " ";
    	}
    	else {
    		return " TYPE ";
    	}
    }
    
    /**
     * @return the string that comes after the column type definition (like a close parentheses for Oracle)
     */
    private String getPostDataTypeString(Database database) {
    	if (database instanceof OracleDatabase
    				|| database instanceof MaxDBDatabase ) {
    		return " )";
    	}
    	else {
    		return "";
    	}
    }

    public boolean isAutoIncrement() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof AutoIncrementConstraint) {
                return true;
            }
        }
        return false;
    }

    public boolean isPrimaryKey() {
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof PrimaryKeyConstraint) {
                return true;
            }
        }
        return false;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }

    private String getDefaultClause(Database database) {
        String clause = "";
        if (getDefaultValue() != null) {
            if (database instanceof MySQLDatabase) {
            	clause += " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
            }
        }
        return clause;
    }

    public boolean isNullable() {
        if (isPrimaryKey()) {
            return false;
        }
        for (ColumnConstraint constraint : getConstraints()) {
            if (constraint instanceof NotNullConstraint) {
                return false;
            }
        }
        return true;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
