package liquibase.action.core;

import liquibase.action.AbstractAction;

public class CreateViewAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        viewName,
        selectQuery,
        replaceIfExists,
        fullDefinition,
    }
}
