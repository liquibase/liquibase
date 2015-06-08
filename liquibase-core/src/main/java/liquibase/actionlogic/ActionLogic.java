package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.servicelocator.Service;

/**
 * Implementations of this interface contain the logic to handle an {@link liquibase.action.Action} object.
 * Which ActionLogic implementation is used will be based on which returns the highest value from {@link #getPriority(liquibase.action.Action, liquibase.Scope)}.
 * For convenience, consider extending {@link AbstractActionLogic}.
 * If this ActionLogic interacts with an external system, implement {@link liquibase.actionlogic.ActionLogic.InteractsExternally}
 */
public interface ActionLogic<T extends Action> extends Service{

    /**
     * Returns the priority for this ActionLogic implementation for the given Action and Scope.
     * If this ActionLogic does not apply, return {@link #PRIORITY_NOT_APPLICABLE}.
     */
    int getPriority(T action, Scope scope);

    /**
     * Validates the given action. Validation can include both errors and warnings.
     */
    ValidationErrors validate(T action, Scope scope);

    ActionResult execute(T action, Scope scope) throws ActionPerformException;

    /**
     * All ActionLogic implementations that (potentially) interact with external systems should implement this interface.
     * Implementations of this interface are used by {@link liquibase.actionlogic.ActionExecutor.Plan} to know when to stop decomposing actions as well as for logging.
     */
    public static interface InteractsExternally<T extends Action> {

        /**
         * Returns true if this ActionLogic implementation will interact with an external system (database, server, etc.) for the given action and scope.
         */
        public boolean interactsExternally(T action, Scope scope);
    }

}
