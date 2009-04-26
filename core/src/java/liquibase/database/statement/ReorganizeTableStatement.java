package liquibase.database.statement;

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

}
