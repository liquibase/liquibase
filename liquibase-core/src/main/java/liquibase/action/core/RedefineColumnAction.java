package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RedefineColumnAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        newDefinition
    }

    public RedefineColumnAction() {
    }

    public RedefineColumnAction(String catalogName, String schemaName, String tableName, String columnName, StringClauses newDefinition) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.columnName, columnName);
        set(Attr.newDefinition, newDefinition);
    }

}
