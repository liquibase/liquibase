package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class CreateSequenceLogic extends AbstractActionLogic {

    @Override
    public int getPriority(Action action, Scope scope) {
        if (action instanceof CreateSequenceAction) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        Database database = scope.get(Scope.Attr.database, Database.class);
        if (!database.supportsAutoIncrement()) {
            errors.addError("Database "+database.getShortName()+" does not support sequences");
        }

        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new ExecuteSqlAction("create sequence "+action.getAttribute(CreateSequenceAction.Attr.sequenceName, String.class)));
    }
}
