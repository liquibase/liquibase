package liquibase.actionlogic;

import liquibase.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ActionLogic result which rewrites an action in the form of one or more new actions.
 */
public class RewriteResult extends ActionResult {

    private List<Action> actions = new ArrayList<Action>();

    public RewriteResult(Action... actions) {
        if (actions != null) {
            this.actions.addAll(Arrays.asList(actions));
        }
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }
}
