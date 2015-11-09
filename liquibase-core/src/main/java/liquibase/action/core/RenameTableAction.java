package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class RenameTableAction extends AbstractAction {
    public ObjectReference oldTableName;
    public ObjectReference newTableName;
}
