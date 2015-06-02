package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddLookupTableAction extends AbstractAction {
    public static enum Attr {
        existingColumnName,

        newColumnName,
        newColumnDataType,
        constraintName,

    }
}
