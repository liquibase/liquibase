package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropTableAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        cascadeConstraints,
    }
    
}
