package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.DataType;

public class AddDefaultValueAction extends AbstractAction {

    public ObjectName columnName;
    public DataType columnDataType;
    public Object defaultValue;

    public AddDefaultValueAction() {
    }

    public AddDefaultValueAction(ObjectName columnName, DataType columnDataType, Object defaultValue) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }
}
