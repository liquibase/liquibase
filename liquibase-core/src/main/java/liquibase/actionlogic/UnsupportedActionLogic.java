package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public abstract class UnsupportedActionLogic extends AbstractActionLogic {

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return new ValidationErrors().addUnsupportedError(action.getClass().getSimpleName(), scope.get(Scope.Attr.database, Database.class).getShortName());
    }

    @Override
    public int getPriority(Action action, Scope scope) {
        int priority = super.getPriority(action, scope);
        if (priority == PRIORITY_DEFAULT) {
            priority = PRIORITY_SPECIALIZED;
        }
        return priority;
    }

    protected abstract Class<? extends Database> getUnsupportedDatabase();

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && getUnsupportedDatabase().isAssignableFrom(scope.get(Scope.Attr.database, Database.class).getClass());
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return null;
    }
}
