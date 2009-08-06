package liquibase.statement.core;

import liquibase.statement.SqlStatement;

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
