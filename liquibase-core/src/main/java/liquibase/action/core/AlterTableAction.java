package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AlterTableAction extends AbstractAction {

    public static enum Attr {
        tableName,
        newDefinition
    }

    public AlterTableAction() {
    }

    public AlterTableAction(ObjectName tableName, StringClauses newDefinition) {
        set(Attr.tableName, tableName);
        set(Attr.newDefinition, newDefinition);
    }

}
