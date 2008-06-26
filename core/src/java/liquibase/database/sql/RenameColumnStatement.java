package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class RenameColumnStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String oldColumnName;
    private String newColumnName;
    private String columnDataType;

    public RenameColumnStatement(String schemaName, String tableName, String oldColumnName, String newColumnName, String columnDataType) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
        this.columnDataType = columnDataType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getOldColumnName() {
        return oldColumnName;
    }

    public void setOldColumnName(String oldColumnName) {
        this.oldColumnName = oldColumnName;
    }

    public String getNewColumnName() {
        return newColumnName;
    }

    public void setNewColumnName(String newColumnName) {
        this.newColumnName = newColumnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }


    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        if (database instanceof MSSQLDatabase) {
            return "exec sp_rename '" + database.escapeTableName(getSchemaName(), getTableName()) + "." + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + "', '" + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName()) + "'";
        } else if (database instanceof MySQLDatabase) {
            if (getColumnDataType() == null) {
                throw new StatementNotSupportedOnDatabaseException("columnDataType is required to rename columns", this, database);
            }

            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " CHANGE " + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + " " + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName()) + " " + getColumnDataType();
        } else if (database instanceof HsqlDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN " + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + " RENAME TO " + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName());
        } else if (database instanceof FirebirdDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN " + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + " TO " + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName());
        } else if (database instanceof MaxDBDatabase) {
          return "RENAME COLUMN " + database.escapeTableName(getSchemaName(), getTableName()) + "." + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + " TO " + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName());
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " RENAME COLUMN " + database.escapeColumnName(getSchemaName(), getTableName(), getOldColumnName()) + " TO " + database.escapeColumnName(getSchemaName(), getTableName(), getNewColumnName());
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof DerbyDatabase
                || database instanceof SQLiteDatabase);
    }
}
