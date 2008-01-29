package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropIndexStatement implements SqlStatement {

    private String indexName;
    private String tableSchemaName;
    private String tableName;

    public DropIndexStatement(String indexName, String tableSchemaName, String tableName) {
        this.tableSchemaName = tableSchemaName;
        this.indexName = indexName;
        this.tableName = tableName;
    }

    public String getTableSchemaName() {
        return tableSchemaName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        String schemaName = getTableSchemaName();
        
        if (schemaName != null && !database.supportsSchemas()) {
            throw new StatementNotSupportedOnDatabaseException("Database does not support schemas", this, database);
        }
        if (database instanceof MySQLDatabase) {
            if (getTableName() == null) {
                throw new StatementNotSupportedOnDatabaseException("tableName is required", this, database);
            }
            return "DROP INDEX " +getIndexName() + " ON " + database.escapeTableName(schemaName, getTableName());
        } else if (database instanceof MSSQLDatabase) {
            if (getTableName() == null) {
                throw new StatementNotSupportedOnDatabaseException("tableName is required", this, database);
            }
            return "DROP INDEX " + database.escapeTableName(schemaName, getTableName()) + "." + getIndexName();
        }

        return "DROP INDEX " + getIndexName();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
