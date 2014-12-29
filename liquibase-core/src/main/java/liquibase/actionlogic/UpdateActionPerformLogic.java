package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.ExecuteAction;
import liquibase.action.UpdateAction;
import liquibase.exception.ActionPerformException;
import liquibase.executor.Executor;

public interface UpdateActionPerformLogic extends ActionPerformLogic {

    public UpdateAction.Result update(UpdateAction action, Scope scope) throws ActionPerformException;

}
