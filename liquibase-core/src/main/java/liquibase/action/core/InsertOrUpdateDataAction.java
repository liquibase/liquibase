package liquibase.action.core;

import liquibase.action.AbstractAction;

public class InsertOrUpdateDataAction extends AbstractAction {
    public static enum Attr {
        primaryKeyColumnNames,
        onlyUpdate,
        tableName,
        columnNames,
        columnValues,
    }
}
