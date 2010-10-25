package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropTableStatement extends AbstractSqlStatement {

    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;

    public DropTableStatement(String schemaName, String tableName, boolean cascadeConstraints) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }
}
