package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RedefineTableAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        newDefinition
    }

    public RedefineTableAction() {
    }

    public RedefineTableAction(String catalogName, String schemaName, String tableName, StringClauses newDefinition) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.newDefinition, newDefinition);
    }

}
