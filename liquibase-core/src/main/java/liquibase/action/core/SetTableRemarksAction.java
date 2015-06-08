package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class SetTableRemarksAction extends AbstractAction {

    public ObjectName tableName;
    public String remarks;
}
