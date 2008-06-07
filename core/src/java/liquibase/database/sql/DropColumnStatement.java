package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropColumnStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;

    public DropColumnStatement(String schemaName, String tableName, String columnName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        } else if (database instanceof DB2Database) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP COLUMN " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName());
        } else if (database instanceof SybaseDatabase || database instanceof FirebirdDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName());
        }
        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP COLUMN " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName());
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof DerbyDatabase);
    }
}
