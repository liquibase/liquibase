package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class InsertDataAction extends AbstractAction {

    public static enum Attr {
        tableName,
        columnNames,
        columnValues,
    }

    public InsertDataAction() {
    }

    public InsertDataAction(ObjectName tableName) {
        set(Attr.tableName, tableName);
    }

    public InsertDataAction addColumnValue(String columnName, Object columnValue) {
        add(Attr.columnNames, columnName);
        add(Attr.columnValues, columnValue);

        return this;
    }


}
