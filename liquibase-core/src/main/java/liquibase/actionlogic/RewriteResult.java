package liquibase.actionlogic;

import liquibase.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * ActionLogic result which rewrites an action in the form of one or more new actions.
 * A {@link liquibase.actionlogic.ActionResult.Modifier} can be us included which will adapt the results of the rewritten action.
 */
public class RewriteResult extends ActionResult {

    private List<Action> actions = new ArrayList<>();

    private ActionResult.Modifier modifier;

    public RewriteResult(Action... actions) {
        this((Modifier) null, actions);
    }

    /**
     * Creates a new RewriteResult with the actions of the given previousResult and then the additional actions.
     * Any modifier defined in the previousResult will be kept as well.
     */
    public RewriteResult(RewriteResult previousResult, Action... actions) {
        this(previousResult.getModifier(), previousResult.getActions().toArray(new Action[previousResult.getActions().size()]));
        this.actions.addAll(Arrays.asList(actions));
    }

    public RewriteResult(ActionResult.Modifier modifier, Action... actions) {
        if (actions != null) {
            this.actions.addAll(Arrays.asList(actions));
        }
        if (modifier != null) {
            this.modifier = modifier;
        }
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public ActionResult.Modifier getModifier() {
        return modifier;
    }
}
