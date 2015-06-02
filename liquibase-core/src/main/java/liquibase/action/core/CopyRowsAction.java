package liquibase.action.core;

import liquibase.action.AbstractAction;

public class CopyRowsAction extends AbstractAction {

    public static enum Attr {
        sourceTableName,

        targetTableName,

        sourceColumns,
        targetColumns,
    }
}
