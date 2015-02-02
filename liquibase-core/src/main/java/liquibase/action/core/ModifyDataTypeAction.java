package liquibase.action.core;

import liquibase.action.AbstractAction;

public class ModifyDataTypeAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        newDataType,

    }
}
