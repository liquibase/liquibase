package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class SetTableRemarksStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String remarks;

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
