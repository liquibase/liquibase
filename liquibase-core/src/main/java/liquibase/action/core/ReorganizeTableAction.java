package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class ReorganizeTableAction extends AbstractAction {
    public static enum Attr {
        tableName,
    }

    public ReorganizeTableAction() {
    }

    public ReorganizeTableAction(ObjectName tableName) {
        set(Attr.tableName, tableName);
    }

}
