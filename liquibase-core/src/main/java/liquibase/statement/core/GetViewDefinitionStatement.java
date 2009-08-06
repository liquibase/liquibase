package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class GetViewDefinitionStatement implements SqlStatement {
    private String schemaName;
    private String viewName;

    public GetViewDefinitionStatement(String schemaName, String viewName) {
        this.schemaName = schemaName;
        this.viewName = viewName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }
}
