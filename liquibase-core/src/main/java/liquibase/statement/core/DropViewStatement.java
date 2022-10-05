package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String viewName;
    private boolean ifExists;

    public DropViewStatement(String catalogName, String schemaName, String viewName) {
        this(catalogName, schemaName, viewName, false);
    }

    public DropViewStatement(String catalogName, String schemaName, String viewName, boolean ifExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.ifExists = ifExists;
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

    public boolean isIfExists() {
        return ifExists;
    }
}
