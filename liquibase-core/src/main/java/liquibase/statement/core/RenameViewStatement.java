package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

/**
 * Renames an existing view.
 */
public class RenameViewStatement extends AbstractViewStatement {

    public static final String NEW_VIEW_NAME = "newViewName";

    public RenameViewStatement() {
    }

    public RenameViewStatement(String catalogName, String schemaName, String oldViewName, String newViewName) {
        super(catalogName, schemaName, oldViewName);
        setNewViewName(newViewName);
    }

    public String getNewViewName() {
        return getAttribute(NEW_VIEW_NAME, String.class);
    }

    public RenameViewStatement setNewViewName(String newViewName) {
        return (RenameViewStatement) setAttribute(NEW_VIEW_NAME, newViewName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getNewViewName()).setSchema(getCatalogName(), getSchemaName()),
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName()),
        };
    }
}
