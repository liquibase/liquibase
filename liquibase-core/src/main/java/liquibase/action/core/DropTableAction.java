package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DropTableAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        cascadeConstraints,
    }

    public DropTableAction() {
    }

    public DropTableAction(String catalogName, String schemaName, String tableName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
    }


}
