package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropForeignKeyConstraintAction extends AbstractAction {

    public ObjectName baseTableName;
    public String constraintName;
}
