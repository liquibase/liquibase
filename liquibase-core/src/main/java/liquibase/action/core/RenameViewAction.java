package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class RenameViewAction extends AbstractAction {
    public ObjectReference oldViewName;
    public ObjectReference newViewName;
}
