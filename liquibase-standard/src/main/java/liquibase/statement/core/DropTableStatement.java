package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropTableStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final boolean cascadeConstraints;

    public DropTableStatement(String catalogName, String schemaName, String tableName, boolean cascadeConstraints) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.cascadeConstraints = cascadeConstraints;
    }

    public String getCatalogName() {
        return catalogName;
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
