package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AlterColumnAction extends AbstractAction {

    public ObjectName columnName;
    public StringClauses newDefinition;

    public AlterColumnAction() {
    }

    public AlterColumnAction(ObjectName columnName, StringClauses newDefinition) {
        this.columnName = columnName;
        this.newDefinition = newDefinition;
    }

}
