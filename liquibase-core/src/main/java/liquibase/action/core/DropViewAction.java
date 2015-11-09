package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class DropViewAction extends AbstractAction {
    public ObjectReference viewName;
}
