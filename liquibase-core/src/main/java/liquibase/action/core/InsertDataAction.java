package liquibase.action.core;

import liquibase.action.AbstractAction;

public class InsertDataAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnNames,
        columnValues,
    }

    public InsertDataAction() {
    }

    public InsertDataAction(String catalogName, String schemaName, String tableName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
    }

    public InsertDataAction addColumnValue(String columnName, Object columnValue) {
        add(Attr.columnNames, columnName);
        add(Attr.columnValues, columnValue);

        return this;
    }


}
