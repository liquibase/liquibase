package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

/**
 * Creates a view.
 */
public class CreateViewStatement extends AbstractViewStatement {

    private static final String SELECT_QUERY = "selectQuery";
    private static final String REPLACE_IF_EXISTS = "replaceIfExists";

    public CreateViewStatement() {
    }

    public CreateViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        super(catalogName, schemaName, viewName);
        setSelectQuery(selectQuery);
        setReplaceIfExists(replaceIfExists);
    }

    public String getSelectQuery() {
        return getAttribute(SELECT_QUERY, String.class);
    }

    public CreateViewStatement setSelectQuery(String selectQuery) {
        return (CreateViewStatement) setAttribute(SELECT_QUERY, selectQuery);
    }

    /**
     * Should the view be replaced if it exists. Returns false by default.
     */
    public boolean isReplaceIfExists() {
        return getAttribute(REPLACE_IF_EXISTS, false);
    }

    public CreateViewStatement setReplaceIfExists(boolean replaceIfExists) {
        return (CreateViewStatement) setAttribute(REPLACE_IF_EXISTS, replaceIfExists);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
