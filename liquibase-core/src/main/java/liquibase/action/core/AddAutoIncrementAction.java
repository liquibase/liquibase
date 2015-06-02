package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddAutoIncrementAction extends AbstractAction {

    public static enum Attr {
        columnName,
        columnDataType,
        startWith,
        incrementBy,
    }
}
