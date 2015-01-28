package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddDefaultValueAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        columnDataType,
        defaultValue,
    }

    public AddDefaultValueAction() {
    }

    public AddDefaultValueAction(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
        set(Attr.columnName, columnName);
        set(Attr.columnDataType, columnDataType);
        set(Attr.defaultValue, defaultValue);
    }
}
