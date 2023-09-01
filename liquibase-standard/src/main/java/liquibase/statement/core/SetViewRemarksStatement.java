package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetViewRemarksStatement extends AbstractSqlStatement {
    private final String catalogName;
    private final String schemaName;
    private final String viewName;
    private final String remarks;

    public SetViewRemarksStatement(String catalogName, String schemaName, String viewName, String remarks) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.remarks = remarks;
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

    public String getRemarks() {
        return remarks;
    }
}
