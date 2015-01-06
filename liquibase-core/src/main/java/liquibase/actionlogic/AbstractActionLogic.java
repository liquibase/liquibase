package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;

/**
 * Convenience base class for {@link liquibase.actionlogic.ActionLogic} implementations.
 */
public abstract class AbstractActionLogic implements ActionLogic {

    /**
     * Standard implementation returns an empty ValidationErrors
     */
    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return new ValidationErrors();
    }
}
