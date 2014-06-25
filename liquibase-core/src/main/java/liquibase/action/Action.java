package liquibase.action;

/**
 * Base interface for all actions to perform against the outside environment.
 * See {@link liquibase.action.QueryAction}, {@link liquibase.action.ExecuteAction}, and {@link liquibase.action.UpdateAction} for types of Actions to implement.
 */
public interface Action {

    /**
     * Return a text description of this action.
     * Two Action objects should return the same description if and only if they are interchangeable.
     */
    String describe();
}
