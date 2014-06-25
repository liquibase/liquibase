package liquibase.statement.core;

import liquibase.action.Action;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class RuntimeStatement extends AbstractSqlStatement {
    public Action[] generate(ExecutionEnvironment env) {
        return new Action[0];
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
