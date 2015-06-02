package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropIndexAction extends AbstractAction {

    public static enum Attr {
        indexName,
        tableName,
        associatedWith,
    }

}
