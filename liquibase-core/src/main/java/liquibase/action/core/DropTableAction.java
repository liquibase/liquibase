package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropTableAction extends AbstractAction {
    public static enum Attr {
        tableName,
        cascadeConstraints,
    }

    public DropTableAction() {
    }

    public DropTableAction(ObjectName tableName) {
        set(Attr.tableName, tableName);
    }


}
