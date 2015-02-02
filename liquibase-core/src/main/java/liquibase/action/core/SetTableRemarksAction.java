package liquibase.action.core;

import liquibase.action.AbstractAction;

public class SetTableRemarksAction extends AbstractAction {
    
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        remarks,
    }
}
