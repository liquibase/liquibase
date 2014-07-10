package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String viewName;
    private String selectQuery;
    private boolean replaceIfExists;
    private boolean fullDefinition;

    public CreateViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.selectQuery = selectQuery;
        this.replaceIfExists = replaceIfExists;
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

    public String getSelectQuery() {
        return selectQuery;
    }

    public boolean isReplaceIfExists() {
        return replaceIfExists;
    }

    public boolean isFullDefinition() {
        return fullDefinition;
    }

    public CreateViewStatement setFullDefinition(boolean fullDefinition) {
        this.fullDefinition = fullDefinition;
        return this;
    }
}
