package liquibase.action;

import liquibase.ExtensibleObject;
import liquibase.Scope;

/**
 * Implementations of Action describe interactions to perform against the outside environment.
 * If the action is more of a query or update, implement {@link liquibase.action.QueryAction} or {@link liquibase.action.UpdateAction} instead.
 * Implementations should not actually contain the logic to perform the action, they only describe it. The logic to perform the action goes in implementations of {@link liquibase.actionlogic.ActionLogic}.
 * For ease of implementation, consider subclassing {@link AbstractAction}.
 * Actions are executed using {@link liquibase.actionlogic.ActionExecutor}
 */
public interface Action extends ExtensibleObject {

    /**
     * Return a text description of this action.
     * Two Action instances should return the same description if and only if they are equivalent calls.
     * This function should return a description of this action that contains enough information to know everything the Action is going to do and nothing that has no impact on what the Action does.
     * The return value is similar to a serialization of the object, but does not need to be deserialized.
     * Example output would include SQL to execute, a function name and all parameters, or a query string.
     * Used for equals() testing as well as logging and testing.
     */
    String describe();

    /**
     * Check if this action has already been executed against the given scope. Return {@link liquibase.action.ActionStatus.Status#unknown} if it is impossible to test.
     */
    ActionStatus checkStatus(Scope scope);

}