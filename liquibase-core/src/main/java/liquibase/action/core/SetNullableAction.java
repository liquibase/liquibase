package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class SetNullableAction extends AbstractAction {

    public static enum Attr {
        columnName,
        columnDataType,
        nullable
    }

    public SetNullableAction() {
    }

    public SetNullableAction(ObjectName columnName, String columnDataType, boolean nullable) {
        set(Attr.columnName, columnName);
        set(Attr.columnDataType, columnDataType);
        set(Attr.nullable, nullable);
    }
}
