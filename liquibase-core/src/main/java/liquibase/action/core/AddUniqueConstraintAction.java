package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddUniqueConstraintAction extends AbstractAction {

    public static enum Attr {

        catalogName,
        schemaName,
        tableName,
        columnNames,
        constraintName,
        tablespace,

        deferrable,
        initiallyDeferred,
        disabled,

    }

    public AddUniqueConstraintAction() {

    }

    public AddUniqueConstraintAction(String catalogName, String schemaName, String tableName, String constraintName, String[] columnNames) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.columnNames, columnNames);
        set(Attr.constraintName, constraintName);
    }

}
