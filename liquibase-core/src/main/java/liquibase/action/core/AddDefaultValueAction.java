package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AddDefaultValueAction extends AbstractAction {

    public ObjectName columnName;
    public String columnDataType;
    public Object defaultValue;

    public AddDefaultValueAction() {
    }

    public AddDefaultValueAction(ObjectName columnName, String columnDataType, Object defaultValue) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }
}
