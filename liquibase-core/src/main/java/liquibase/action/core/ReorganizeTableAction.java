package liquibase.action.core;

import liquibase.action.AbstractAction;

public class ReorganizeTableAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
    }

    public ReorganizeTableAction() {
    }

    public ReorganizeTableAction(String catalogName, String schemaName, String tableName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
    }

}
