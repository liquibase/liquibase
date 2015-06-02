package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class SelectDataAction extends AbstractAction {

    public static enum Attr {
        tableName,
        selectColumnDefinitions,
        where,
        orderByColumnNames,
        limit
    }

    public SelectDataAction() {
    }

    public SelectDataAction(ObjectName tableName, ColumnDefinition... selectColumnNames) {
        set(Attr.tableName, tableName);
        set(Attr.selectColumnDefinitions, selectColumnNames);
    }
}
