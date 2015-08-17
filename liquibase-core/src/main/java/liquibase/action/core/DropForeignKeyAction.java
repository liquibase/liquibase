package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropForeignKeyAction extends AbstractAction {

    public ObjectName baseTableName;
    public ObjectName constraintName;

    public DropForeignKeyAction() {
    }

    public DropForeignKeyAction(ObjectName constraintName, ObjectName baseTableName) {
        this.constraintName = constraintName;
        this.baseTableName = baseTableName;
    }
}
