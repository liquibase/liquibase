package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class DropTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private boolean cascadeConstraints;

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
