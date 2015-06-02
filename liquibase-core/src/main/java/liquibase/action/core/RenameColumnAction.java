package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RenameColumnAction extends AbstractAction {
    public static enum Attr {
        tableName,
        oldColumnName,
        newColumnName,
        columnDataType,
        remarks,
    }
}
