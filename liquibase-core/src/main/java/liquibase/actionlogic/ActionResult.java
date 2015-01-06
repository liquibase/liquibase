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

    public String getMessage() {
        return message;
    }
}
