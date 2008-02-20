package liquibase.database.sql;

import liquibase.database.*;

public class AddDefaultValueStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private Object defaultValue;


    public AddDefaultValueStatement(String schemaName, String tableName, String columnName, Object defaultValue) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.defaultValue = defaultValue;
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }

    public String getSqlStatement(Database database) {
        if (database instanceof SybaseDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " REPLACE " + database.escapeColumnName(getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof MSSQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue()) + " FOR " + getColumnName();
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER " + database.escapeColumnName(getColumnName()) + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof OracleDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + database.escapeColumnName(getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof DerbyDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getColumnName()) + " WITH DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof MaxDBDatabase) {
        		return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " COLUMN  " + database.escapeColumnName(getColumnName()) + " ADD DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getColumnName()) + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
    }

    public String getColumnName() {
        return columnName;
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
