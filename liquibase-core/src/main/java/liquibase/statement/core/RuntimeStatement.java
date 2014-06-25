package liquibase.statement.core;

import liquibase.action.Action;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class RuntimeStatement extends AbstractStatement {
    public Action[] generate(ExecutionEnvironment env) {
        return new Action[0];
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
