package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class ReorganizeTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;

    public ReorganizeTableStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new Table(getCatalogName(), getSchemaName(), getTableName())
        };
    }
}
