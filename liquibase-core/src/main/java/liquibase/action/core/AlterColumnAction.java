package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AlterColumnAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        newDefinition
    }

    public AlterColumnAction() {
    }

    public AlterColumnAction(String catalogName, String schemaName, String tableName, String columnName, String newDefinition) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.columnName, columnName);
        set(Attr.newDefinition, newDefinition);
    }

}
