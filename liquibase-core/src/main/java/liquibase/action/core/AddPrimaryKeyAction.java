package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddPrimaryKeyAction extends AbstractAction {
    
    public static enum Attr {
        tableName,
        tablespace,
        columnNames,
        constraintName,
        clustered,
    }
}
