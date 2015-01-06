package liquibase.actionlogic;

import liquibase.action.Action;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;

/**
 * Result of an action that returns more than one {@link liquibase.actionlogic.ActionResult}.
 */
public class CompoundResult extends ActionResult {

    private LinkedHashMap<Action, ActionResult> results;

    /**
     * Creates a new CompoundResult of the given Action/ActionResult pairs.
     */
    public CompoundResult(LinkedHashMap<Action, ActionResult> results) {
        if (results == null || results.size() == 0) {
            throw new UnexpectedLiquibaseException("Null or empty results passed to a CompoundResult");
        }

        this.results = results;

    }

    /**
     * Returns the results stored in this CompoundResult.
     * They are returned in the order the actions were executed.
     * The returned list is unmodifiable.
     */
    public List<ActionResult> getResults() {
        return Collections.unmodifiableList(new ArrayList<ActionResult>(results.values()));
    }

    /**
     * Returns the results stored in this CompoundResult, keyed by the action that generated the result.
     * The returned Map iterator order preserves the original execution order.
     * The returned map is unmodifiable.
     */
    public Map<Action, ActionResult> getResultsBySource() {
        return Collections.unmodifiableMap(results);
    }
}
