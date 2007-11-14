package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class SetNullableStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private boolean nullable;

    public SetNullableStatement(String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
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

    public boolean isNullable() {
        return nullable;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        String nullableString;
        if (isNullable()) {
            nullableString = " NULL";
        } else {
            nullableString = " NOT NULL";
        }

        if (database instanceof OracleDatabase || database instanceof SybaseDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + getColumnName() + nullableString;
        } else if (database instanceof MSSQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN " + getColumnName() + " " + getColumnDataType() + nullableString;
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + getColumnName() + " " + getColumnDataType() + nullableString;
        } else if (database instanceof DerbyDatabase || database instanceof CacheDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + nullableString;
        } else if (database instanceof HsqlDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + " " + getColumnDataType() + nullableString;
        } else if (database instanceof FirebirdDatabase) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        } else if (database instanceof MaxDBDatabase) {
        		return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " COLUMN  " + getColumnName() + (isNullable() ? " DEFAULT NULL" : " NOT NULL");
        }
        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + getColumnName() + (isNullable()?" DROP NOT NULL":" SET NOT NULL");
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof FirebirdDatabase);
    }
}
