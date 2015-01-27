package liquibase.action.core;

import liquibase.action.AbstractAction;

public class SetNullableAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        columnDataType,
        nullable
    }

    public SetNullableAction() {
    }

    public SetNullableAction(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.columnName, columnName);
        set(Attr.columnDataType, columnDataType);
        set(Attr.nullable, nullable);
    }
}
