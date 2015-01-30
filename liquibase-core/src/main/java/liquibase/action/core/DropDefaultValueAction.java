package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropDefaultValueAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        columnDataType,
    }
}
