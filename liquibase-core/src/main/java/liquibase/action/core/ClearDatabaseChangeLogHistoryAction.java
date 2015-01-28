package liquibase.action.core;

import liquibase.action.AbstractAction;

public class ClearDatabaseChangeLogHistoryAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
    }
}
