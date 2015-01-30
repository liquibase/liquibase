package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropViewAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        viewName,
    }
}
