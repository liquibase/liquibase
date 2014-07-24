package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

/**
 * Creates a view.
 */
public class CreateViewStatement extends AbstractViewStatement {

    public static final String SELECT_QUERY = "selectQuery";
    public static final String REPLACE_IF_EXISTS = "replaceIfExists";

    public static final String FULL_DEFINITION = "fullDefinition";

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

    public boolean isFullDefinition() {
        return getAttribute(FULL_DEFINITION, false);
    }

    public CreateViewStatement setFullDefinition(boolean fullDefinition) {
        return (CreateViewStatement) setAttribute(FULL_DEFINITION, fullDefinition);
    }
    
    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
