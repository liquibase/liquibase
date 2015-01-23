package liquibase.actionlogic;

import liquibase.util.StringUtils;

/**
 * Result for a generic action which isn't a query, update or rewrite.
 */
public class ExecuteResult extends ActionResult {

    public ExecuteResult() {
    }

    public ExecuteResult(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "Executed: "+ StringUtils.defaultIfEmpty(getMessage(), "No Message");
    }
}
