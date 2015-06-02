package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AlterColumnAction extends AbstractAction {

    public static enum Attr {
        columnName,
        newDefinition
    }

    public AlterColumnAction() {
    }

    public AlterColumnAction(ObjectName columnName, StringClauses newDefinition) {
        set(Attr.columnName, columnName);
        set(Attr.newDefinition, newDefinition);
    }

}
