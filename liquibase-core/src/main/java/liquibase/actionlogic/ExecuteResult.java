package liquibase.actionlogic;

/**
 * Result for a generic action which isn't a query, update or rewrite.
 */
public class ExecuteResult extends ActionResult {

    public ExecuteResult() {
    }

    public ExecuteResult(String message) {
        super(message);
    }
}
