package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.StringClauses;
import liquibase.exception.ActionPerformException;

public abstract class AbstractSqlBuilderLogic<T extends AbstractAction> extends AbstractActionLogic<T> {

    protected abstract StringClauses generateSql(T action, Scope scope);

    @Override
    public ActionResult execute(T action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

}
