package liquibase.action.core;

import liquibase.action.AbstractAction;

public class SelectDataAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        selectColumnDefinitions,
        where,
        orderByColumnNames,
    }

    public SelectDataAction() {
    }

    public SelectDataAction(String catalogName, String schemaName, String tableName, ColumnDefinition... selectColumnNames) {
        set(Attr.selectColumnDefinitions, selectColumnNames);
    }
}
