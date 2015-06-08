package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RenameColumnAction extends AbstractAction {
    public ObjectName tableName;
    public String oldColumnName;
    public String newColumnName;
    public String columnDataType;
    public String remarks;
}
