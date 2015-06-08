package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class SetNullableAction extends AbstractAction {

    public ObjectName columnName;
    public String columnDataType;
    public Boolean nullable;

    public SetNullableAction() {
    }

    public SetNullableAction(ObjectName columnName, String columnDataType, boolean nullable) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
    }
}
