package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.actionlogic.UpdateResult;
import liquibase.structure.ObjectName;

public class UpdateDataAction extends AbstractAction {
    
    public static enum Attr {
        tableName,
        columnNames,
        newColumnValues,
        whereClause,
        whereColumnNames,
        whereParameters,
    }

    public UpdateDataAction() {
    }

    public UpdateDataAction(ObjectName tableName) {
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
