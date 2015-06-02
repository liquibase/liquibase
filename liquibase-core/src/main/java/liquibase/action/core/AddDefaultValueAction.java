package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AddDefaultValueAction extends AbstractAction {

    public static enum Attr {
        columnName,
        columnDataType,
        defaultValue,
    }

    public AddDefaultValueAction() {
    }

    public AddDefaultValueAction(ObjectName columnName, String columnDataType, Object defaultValue) {
        set(Attr.columnName, columnName);
        set(Attr.columnDataType, columnDataType);
        set(Attr.defaultValue, defaultValue);
    }
}
