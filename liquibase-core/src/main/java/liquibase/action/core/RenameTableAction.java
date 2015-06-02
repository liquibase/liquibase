package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RenameTableAction extends AbstractAction {
    public static enum Attr {
        oldTableName,
        newTableName,
    }
}
