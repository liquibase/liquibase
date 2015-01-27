package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AlterTableAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        newDefinition
    }

    public AlterTableAction() {
    }

    public AlterTableAction(String catalogName, String schemaName, String tableName, String newDefinition) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.newDefinition, newDefinition);
    }

}
