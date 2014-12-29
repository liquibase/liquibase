package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.ExecuteAction;
import liquibase.exception.ActionPerformException;
import liquibase.executor.Executor;

public interface ExecuteActionPerformLogic extends ActionPerformLogic {

    public ExecuteAction.Result execute(ExecuteAction action, Scope scope) throws ActionPerformException;

}
