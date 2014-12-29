package liquibase.actionlogic.core.perform;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.actionlogic.ActionLogic;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;

public abstract class AbstractSqlPerformLogic implements ActionLogic {

    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        Database database = scope.get(ScopeAttributes.database, Database.class);
        if (database != null && database instanceof AbstractJdbcDatabase) {
            return ActionLogicPriority.DEFAULT;
        }
        return ActionLogicPriority.NOT_APPLICABLE;
    }
}
