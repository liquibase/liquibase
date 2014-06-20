package liquibase.statement.core;

import liquibase.action.Action;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class RuntimeStatement extends AbstractSqlStatement {
    public Action[] generate(ExecutionOptions options) {
        return new Action[0];
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
