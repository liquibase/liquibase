package liquibase.action.core;

import liquibase.action.AbstractAction;

public class SelectFromDatabaseChangeLogAction extends AbstractAction {

    public static enum Attr {
        selectColumnDefinitions,
        where,
        orderByColumnNames,
    }

    public SelectFromDatabaseChangeLogAction() {
    }

    public SelectFromDatabaseChangeLogAction(String[] selectColumnNames) {
        set(Attr.selectColumnDefinitions, selectColumnNames);
    }
}
