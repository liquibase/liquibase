package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class RenameTableStatement implements SqlStatement {
    private String schemaName;
    private String oldTableName;
    private String newTableName;

    public RenameTableStatement(String schemaName, String oldTableName, String newTableName) {
        this.schemaName = schemaName;
        this.oldTableName = oldTableName;
        this.newTableName = newTableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        if (database instanceof MSSQLDatabase) {
            return "exec sp_rename '" + database.escapeTableName(getSchemaName(), oldTableName) + "', " + database.escapeTableName(null, newTableName);
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME " + database.escapeTableName(getSchemaName(), getNewTableName());
        } else if (database instanceof PostgresDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName);
        } else if (database instanceof SybaseASADatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME " + database.escapeTableName(null, newTableName);
        } else if ((database instanceof DerbyDatabase) || ((database instanceof MaxDBDatabase))) {
            return "RENAME TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " TO " + database.escapeTableName(null, newTableName);
        } else if (database instanceof HsqlDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName);
        } else if (database instanceof OracleDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName);
        } else if (database instanceof DB2Database) {
            return "RENAME " + database.escapeTableName(getSchemaName(), oldTableName) + " TO " + database.escapeTableName(null, newTableName);//db2 doesn't allow specifying new schema name
        } else if (database instanceof SQLiteDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), oldTableName) + " RENAME TO " + database.escapeTableName(null, newTableName);
        }

        return "RENAME " + database.escapeTableName(getSchemaName(), getOldTableName()) + " TO " + database.escapeTableName(getSchemaName(), getNewTableName());
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof CacheDatabase || database instanceof FirebirdDatabase);
    }
}
