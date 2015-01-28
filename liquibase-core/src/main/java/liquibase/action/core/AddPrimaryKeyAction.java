package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddPrimaryKeyAction extends AbstractAction {
    
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        tablespace,
        columnNames,
        constraintName,
        clustered,
    }
}
