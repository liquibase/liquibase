package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ClearDatabaseChangeLogTableStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;

    public ClearDatabaseChangeLogTableStatement(String catalogName, String schemaName) {
        super();
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
