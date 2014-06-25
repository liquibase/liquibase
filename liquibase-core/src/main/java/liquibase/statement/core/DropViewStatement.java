package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

public class DropViewStatement extends AbstractStatement {

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
