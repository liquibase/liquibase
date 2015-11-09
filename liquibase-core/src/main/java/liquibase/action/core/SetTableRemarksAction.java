package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class SetTableRemarksAction extends AbstractAction {

    public ObjectReference tableName;
    public String remarks;
}
