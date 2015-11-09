package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class ReorganizeTableAction extends AbstractAction {
    public ObjectReference tableName;

    public ReorganizeTableAction() {
    }

    public ReorganizeTableAction(ObjectReference tableName) {
        this.tableName = tableName;
    }

}
