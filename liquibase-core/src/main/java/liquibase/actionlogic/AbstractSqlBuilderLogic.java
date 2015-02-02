package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.StringClauses;
import liquibase.exception.ActionPerformException;

public abstract class AbstractSqlBuilderLogic extends AbstractActionLogic {

    protected abstract StringClauses generateSql(Action action, Scope scope);

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

}
