package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class RenameViewStatement implements SqlStatement {

    private String schemaName;
    private String oldViewName;
    private String newViewName;

    public RenameViewStatement(String schemaName, String oldViewName, String newViewName) {
        this.schemaName = schemaName;
        this.oldViewName = oldViewName;
        this.newViewName = newViewName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getOldViewName() {
        return oldViewName;
    }

    public String getNewViewName() {
        return newViewName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }

        if (database instanceof MSSQLDatabase) {
            return "exec sp_rename '" + database.escapeViewName(getSchemaName(), getOldViewName()) + "', " + getNewViewName();
        } else if (database instanceof MySQLDatabase) {
            return "RENAME TABLE " + database.escapeViewName(getSchemaName(), getOldViewName()) + " TO " + database.escapeTableName(getSchemaName(), getNewViewName());
        } else if (database instanceof PostgresDatabase) {
            return "ALTER TABLE " + database.escapeViewName(getSchemaName(), getOldViewName()) + " RENAME TO " + getNewViewName();
        }

        if (getSchemaName() != null && database instanceof OracleDatabase) {
            throw new StatementNotSupportedOnDatabaseException("Cannot specify schema when renaming in oracle", this, database);
        }

        return "RENAME " + database.escapeViewName(getSchemaName(), getOldViewName()) + " TO " + getNewViewName();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return !(database instanceof DerbyDatabase
        || database instanceof HsqlDatabase
        || database instanceof DB2Database
        || database instanceof CacheDatabase
        || database instanceof FirebirdDatabase);
    }
}
