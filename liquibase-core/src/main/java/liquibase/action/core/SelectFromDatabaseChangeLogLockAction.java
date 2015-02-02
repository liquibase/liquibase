package liquibase.action.core;

import liquibase.action.AbstractAction;

public class SelectFromDatabaseChangeLogLockAction extends AbstractAction {

    public static enum Attr {
        selectColumnDefinitions,
    }
}
