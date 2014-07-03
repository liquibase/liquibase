package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Finds definition of an existing view.
 */
public class GetViewDefinitionStatement extends AbstractViewStatement {

    public GetViewDefinitionStatement() {
    }

    public GetViewDefinitionStatement(String catalogName, String schemaName, String viewName) {
        super(catalogName, schemaName, viewName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
