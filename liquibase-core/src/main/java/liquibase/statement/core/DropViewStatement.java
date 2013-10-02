package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String viewName;

    public DropViewStatement(String catalogName, String schemaName, String viewName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }
}
