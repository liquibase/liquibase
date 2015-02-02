package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.GetNextChangeSetSequenceValueAction;
import liquibase.action.core.SelectFromDatabaseChangeLogAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;

public class GetNextChangeSetSequenceValueLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return GetNextChangeSetSequenceValueAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new SelectFromDatabaseChangeLogAction(new String[] {"MAX(ORDEREXECUTED)"}));
    }
}
