package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class SetColumnRemarksAction extends AbstractAction {
    public ObjectName columnName;
    public String remarks;
}
