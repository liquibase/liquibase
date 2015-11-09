package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class AlterTableAction extends AbstractAction {

    public ObjectReference tableName;
    public StringClauses newDefinition;

    public AlterTableAction() {
    }

    public AlterTableAction(ObjectReference tableName, StringClauses newDefinition) {
        this.tableName = tableName;
        this.newDefinition = newDefinition;
    }

}
