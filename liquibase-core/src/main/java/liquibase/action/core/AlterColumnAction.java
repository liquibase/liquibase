package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class AlterColumnAction extends AbstractAction {

    public ObjectReference columnName;
    public StringClauses newDefinition;

    public AlterColumnAction() {
    }

    public AlterColumnAction(ObjectReference columnName, StringClauses newDefinition) {
        this.columnName = columnName;
        this.newDefinition = newDefinition;
    }

}
