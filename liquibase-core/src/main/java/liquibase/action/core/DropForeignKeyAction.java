package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class DropForeignKeyAction extends AbstractAction {

    public ObjectReference baseTableName;
    public ObjectReference constraintName;

    public DropForeignKeyAction() {
    }

    public DropForeignKeyAction(ObjectReference constraintName, ObjectReference baseTableName) {
        this.constraintName = constraintName;
        this.baseTableName = baseTableName;
    }
}
