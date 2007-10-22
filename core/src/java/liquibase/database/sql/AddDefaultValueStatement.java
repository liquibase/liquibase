package liquibase.database.sql;

import liquibase.database.*;
import liquibase.util.SqlUtil;

import java.util.Date;

public class AddDefaultValueStatement implements SqlStatement {
    private String tableName;
    private String columnName;
    private Object defaultValue;


    public AddDefaultValueStatement(String tableName, String columnName, Object defaultValue) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.defaultValue = defaultValue;
    }

    public String getSqlStatement(Database database) {
        if (database instanceof SybaseDatabase) {
            return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " REPLACE " + getColumnName() + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof MSSQLDatabase) {
            return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ADD CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(getTableName(), getColumnName()) + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue()) + " FOR " + getColumnName();
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER " + getColumnName() + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof OracleDatabase) {
            return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " MODIFY " + getColumnName() + " DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        } else if (database instanceof DerbyDatabase) {
            return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER COLUMN  " + getColumnName() + " WITH DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
        }

        return "ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " ALTER COLUMN  " + getColumnName() + " SET DEFAULT " + database.convertJavaObjectToString(getDefaultValue());
    }

    public String getColumnName() {
        return columnName;
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
