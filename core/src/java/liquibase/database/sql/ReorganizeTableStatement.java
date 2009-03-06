package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class ReorganizeTableStatement implements SqlStatement {
    private String schemaName;
    private String tableName;

    public ReorganizeTableStatement(String schemaName, String tableName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException("Cannot reorganize table", this, database);
        }
        try {
            if (database.getDatabaseMajorVersion() >= 9) {
                return "CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + "')";
            } else {
                return null;
            }
        } catch (JDBCException e) {
            throw new RuntimeException(e);
        }
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database instanceof DB2Database;
    }
}
