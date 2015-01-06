package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is used to execute {@link liquibase.action.Action} objects using the registered {@link liquibase.actionlogic.ActionLogic} implementations.
 */
public class ActionExecutor {

    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        ActionLogicFactory actionLogicFactory = scope.getSingleton(ActionLogicFactory.class);

        ActionLogic actionLogic = actionLogicFactory.getActionLogic(action, scope);
        if (actionLogic == null) {
            throw new ActionPerformException("No supported ActionLogic implementation found for '"+action.describe()+"'");
        }

        ValidationErrors validationErrors = actionLogic.validate(action, scope);
        if (validationErrors.hasErrors()) {
            throw new ActionPerformException("Validation Error(s): "+ StringUtils.join(validationErrors.getErrorMessages(), "; "));
        }

        ActionResult result = actionLogic.execute(action, scope);

        if (result instanceof RewriteResult) {
            List<Action> actions = ((RewriteResult) result).getActions();
            if (actions.size() == 0) {
                throw new ActionPerformException(actionLogic.getClass().getName()+" tried to handle '"+action.describe()+"' but returned no actions to run");
            } else if (actions.size() == 1) {
                return execute(actions.get(0), scope);
            } else {
                LinkedHashMap<Action, ActionResult> results = new LinkedHashMap<Action, ActionResult>();
                for (Action rewroteAction : actions) {
                    results.put(rewroteAction, this.execute(rewroteAction, scope));
                }

                return new CompoundResult(results);
            }
        }
        return result;
    }

}
