package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropPrimaryKeyAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        constraintName,

    }
}