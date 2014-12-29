package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.QueryAction;
import liquibase.exception.ActionPerformException;
import liquibase.executor.Executor;

public interface QueryActionPerformLogic extends ActionPerformLogic {

    public QueryAction.Result query(QueryAction action, Scope scope) throws ActionPerformException;

}
