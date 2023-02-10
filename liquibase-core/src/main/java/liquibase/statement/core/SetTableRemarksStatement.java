package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetTableRemarksStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final String remarks;

    public SetTableRemarksStatement(String catalogName, String schemaName, String tableName, String remarks) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.remarks = remarks;
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

    public String getRemarks() {
        return remarks;
    }

}
