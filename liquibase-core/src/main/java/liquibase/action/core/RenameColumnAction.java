package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class RenameColumnAction extends AbstractAction {
    public ObjectReference tableName;
    public String oldColumnName;
    public String newColumnName;
    public String columnDataType;
    public String remarks;
}
