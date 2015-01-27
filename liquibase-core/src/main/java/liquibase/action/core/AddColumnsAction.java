package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddColumnsAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnDefinitions,
        uniqueConstraintDefinitions, foreignKeyDefinitions,
    }

}
