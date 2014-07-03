package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

public class DropViewStatement extends AbstractViewStatement {

    public DropViewStatement() {
    }

    public DropViewStatement(String catalogName, String schemaName, String viewName) {
        super(catalogName, schemaName, viewName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
