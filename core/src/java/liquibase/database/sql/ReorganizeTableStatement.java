package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

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

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException("Cannot reorganize table", this, database);
        }
        return "CALL SYSPROC.ADMIN_CMD ('REORG TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + "')";
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return database instanceof DB2Database;
    }
}
