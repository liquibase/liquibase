package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.JDBCException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class AddDefaultValueStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private Object defaultValue;


    public AddDefaultValueStatement(String schemaName, String tableName, String columnName, String columnDataType) {
        this(schemaName, tableName, columnName, columnDataType, null);
    }

    public AddDefaultValueStatement(String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }
    	
        if (database instanceof SybaseDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " REPLACE " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof SybaseASADatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof MSSQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue()) + " FOR " + getColumnName();
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof OracleDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof DerbyDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " WITH DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof MaxDBDatabase) {
        	return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " ADD DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof InformixDatabase) {
            if (getColumnDataType() == null) {
                throw new StatementNotSupportedOnDatabaseException("Database requires columnDataType parameter", this, database);
            }
        	return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY (" + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " " + database.getColumnType(getColumnDataType(), false) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue()) + ")";
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
    }

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
		return columnDataType;
	}

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
