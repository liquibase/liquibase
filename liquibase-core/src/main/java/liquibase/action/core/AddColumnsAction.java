package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddColumnsAction extends AbstractAction {

    public static enum Attr {
        tableName,
        columnDefinitions,
        uniqueConstraintDefinitions, foreignKeyDefinitions,
    }

}
