package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class ReorganizeTableAction extends AbstractAction {
    public ObjectName tableName;

    public ReorganizeTableAction() {
    }

    public ReorganizeTableAction(ObjectName tableName) {
        this.tableName = tableName;
    }

}
