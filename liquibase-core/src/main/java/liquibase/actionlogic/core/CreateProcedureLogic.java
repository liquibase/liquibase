package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateProcedureAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class CreateProcedureLogic extends AbstractActionLogic<CreateProcedureAction> {

    @Override
    protected Class<CreateProcedureAction> getSupportedAction() {
        return CreateProcedureAction.class;
    }

    @Override
    public ValidationErrors validate(CreateProcedureAction action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField("procedureText", action);
        validationErrors.checkForDisallowedField("replaceIfExists", action, scope.getDatabase().getShortName());

        return validationErrors;
    }

    @Override
    public ActionResult execute(CreateProcedureAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(action.procedureText.toString()));
    }
}