package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropPrimaryKeyAction extends AbstractAction {
    public ObjectName tableName;
    public String constraintName;
}