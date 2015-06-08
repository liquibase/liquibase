package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropDefaultValueAction extends AbstractAction {
    public ObjectName columnName;
    public String columnDataType;
}
