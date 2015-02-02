package liquibase.action.core;

import liquibase.action.AbstractAction;

public class InsertOrUpdateDataAction extends AbstractAction {
    public static enum Attr {
        primaryKeyColumnNames,
        onlyUpdate,
        catalogName,
        schemaName,
        tableName,
        columnNames,
        columnValues,
    }
}
