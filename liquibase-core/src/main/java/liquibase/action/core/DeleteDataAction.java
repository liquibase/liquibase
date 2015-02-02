package liquibase.action.core;

import liquibase.action.AbstractAction;

public class DeleteDataAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        where,
        whereParameters,
        whereColumnNames,
    }

    public DeleteDataAction() {
    }

    public DeleteDataAction(String catalogName, String schemaName, String tableName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
    }

    public DeleteDataAction addWhereParameters(Object... parameters) {
        if (parameters != null) {
            for (Object param : parameters) {
                add(Attr.whereParameters, param);
            }
        }

        return this;
    }

}
