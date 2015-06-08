package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropTableAction extends AbstractAction {
    public ObjectName tableName;
    public Boolean cascadeConstraints;

    public DropTableAction() {
    }

    public DropTableAction(ObjectName tableName) {
        this.tableName = tableName;
    }


}
