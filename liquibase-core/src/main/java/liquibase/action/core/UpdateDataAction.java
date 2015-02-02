package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.actionlogic.UpdateResult;

public class UpdateDataAction extends AbstractAction {
    
    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnNames,
        newColumnValues,
        whereClause,
        whereColumnNames,
        whereParameters,
    }

    public UpdateDataAction() {
    }

    public UpdateDataAction(String catalogName, String schemaName, String tableName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.tableName, tableName);
    }

    public UpdateDataAction addNewColumnValue(String columnName, Object columnValue) {
        add(Attr.columnNames, columnName);
        add(Attr.newColumnValues, columnValue);

        return this;
    }

    public UpdateDataAction addWhereParameters(Object... parameters) {
        if (parameters != null) {
            for (Object param : parameters) {
                add(Attr.whereParameters, param);
            }
        }

        return this;
    }

}
