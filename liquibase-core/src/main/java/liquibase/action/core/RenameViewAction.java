package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RenameViewAction extends AbstractAction {
    public ObjectName oldViewName;
    public ObjectName newViewName;
}
