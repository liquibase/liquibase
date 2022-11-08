package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String viewName;
    private Boolean ifExists;

    public DropViewStatement(String catalogName, String schemaName, String viewName) {
        this(catalogName, schemaName, viewName, null);
    }

    public DropViewStatement(String catalogName, String schemaName, String viewName, Boolean ifExists) {
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

    public Boolean isIfExists() {
        return ifExists;
    }
}
