package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class SetNullableAction extends AbstractAction {

    public ObjectReference columnName;
    public String columnDataType;
    public Boolean nullable;

    public SetNullableAction() {
    }

    public SetNullableAction(ObjectReference columnName, String columnDataType, boolean nullable) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
    }
}
