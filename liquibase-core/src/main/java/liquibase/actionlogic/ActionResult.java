package liquibase.actionlogic;

/**
 * Base class for results of {@link ActionLogic#execute(liquibase.action.Action, liquibase.Scope)}.
 * Actual implementation returned will be one of:
 * <ul>
 *     <li>{@link liquibase.actionlogic.ExecuteResult}</li>
 *     <li>{@link liquibase.actionlogic.QueryResult}</li>
 *     <li>{@link liquibase.actionlogic.UpdateResult}</li>
 *     <li>{@link liquibase.actionlogic.RewriteResult}</li>
 * </ul>
 */
public abstract class ActionResult {

    private String message;

    public ActionResult() {
    }

    public ActionResult(String message) {
        this.message = message;
    }

    /**
     * Returns the message (if any) associated with this result.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Implementations contain logic to modify the data in an ActionResult and return a new result.
     * Used to adapt the results of an {@link liquibase.actionlogic.ActionLogic} implementation through another.
     */
    public static interface Modifier {

        public ActionResult rewrite(ActionResult result);

    }
}
