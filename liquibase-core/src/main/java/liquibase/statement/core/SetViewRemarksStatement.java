package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetViewRemarksStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String viewName;
    private String remarks;

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
