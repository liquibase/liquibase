package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class DropPrimaryKeyAction extends AbstractAction {
    public ObjectReference tableName;
    public String constraintName;
}