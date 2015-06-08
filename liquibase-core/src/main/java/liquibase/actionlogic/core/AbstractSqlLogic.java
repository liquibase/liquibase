package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;

public abstract class AbstractSqlLogic<T extends Action> extends AbstractActionLogic<T> {

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase() instanceof AbstractJdbcDatabase;
    }
}
