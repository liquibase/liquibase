package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.GetNextChangeSetSequenceValueAction;
import liquibase.action.core.SelectFromDatabaseChangeLogAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;

public class GetNextChangeSetSequenceValueLogic extends AbstractActionLogic<GetNextChangeSetSequenceValueAction> {

    @Override
    protected Class<GetNextChangeSetSequenceValueAction> getSupportedAction() {
        return GetNextChangeSetSequenceValueAction.class;
    }

    @Override
    public ActionResult execute(GetNextChangeSetSequenceValueAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new SelectFromDatabaseChangeLogAction(new ColumnDefinition("MAX(ORDEREXECUTED)", null)));
    }
}
