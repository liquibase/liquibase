package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class ModifyDataTypeAction extends AbstractAction {
    public ObjectReference columnName;
    public String newDataType;
}
