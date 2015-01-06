package liquibase.action;

import liquibase.ExtensibleObject;

/**
 * Implementations of Action describe interactions to perform against the outside environment.
 * If the action is more of a query or update, implement {@link liquibase.action.QueryAction} or {@link liquibase.action.UpdateAction} instead.
 * Implementations should not actually contain the logic to perform the action, they only describe it. The logic to perform the action goes in implementations of {@link liquibase.actionlogic.ActionLogic}.
 * For ease of implementation, consider subclassing {@link AbstractAction}.
 */
public interface Action extends ExtensibleObject {

    /**
     * Return a text description of this action.
     * Two Action objects should return the same description if and only if they are interchangeable. Used for logging and testing.
     */
    String describe();

}