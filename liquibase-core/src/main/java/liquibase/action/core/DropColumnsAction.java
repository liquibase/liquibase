package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropColumnsAction extends AbstractAction {
    public static enum Attr {
        tableName,
        columnNames,
    }
}
