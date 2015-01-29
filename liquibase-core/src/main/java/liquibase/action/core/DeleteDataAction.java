package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DeleteDataAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        where,
        whereParameters,
        whereColumnNames,

    }
}
