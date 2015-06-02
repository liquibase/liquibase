package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DeleteDataAction extends AbstractAction {
    public static enum Attr {
        tableName,
        where,
        whereParameters,
        whereColumnNames,
    }

    public DeleteDataAction() {
    }

    public DeleteDataAction(ObjectName tableName) {
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
