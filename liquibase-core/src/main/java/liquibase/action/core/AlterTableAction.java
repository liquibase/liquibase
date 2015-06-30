package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.util.StringClauses;

public class AlterTableAction extends AbstractAction {

    public ObjectName tableName;
    public StringClauses newDefinition;

    public AlterTableAction() {
    }

    public AlterTableAction(ObjectName tableName, StringClauses newDefinition) {
        this.tableName = tableName;
        this.newDefinition = newDefinition;
    }

}
