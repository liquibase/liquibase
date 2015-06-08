package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RenameTableAction extends AbstractAction {
    public ObjectName oldTableName;
    public ObjectName newTableName;
}
