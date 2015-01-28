package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateProcedureAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class CreateProcedureLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateProcedureAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(CreateProcedureAction.Attr.procedureText, action);
        validationErrors.checkForDisallowedField(CreateProcedureAction.Attr.replaceIfExists, action, scope.get(Scope.Attr.database, Database.class).getShortName());

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new ExecuteSqlAction(action.get(CreateProcedureAction.Attr.procedureText, String.class)));
    }
}