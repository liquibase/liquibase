package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.DataType;

public class AddDefaultValueAction extends AbstractAction {

    public ObjectReference columnName;
    public DataType columnDataType;
    public Object defaultValue;

    public AddDefaultValueAction() {
    }

    public AddDefaultValueAction(ObjectReference columnName, DataType columnDataType, Object defaultValue) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }
}
