package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ActionStatus;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * This class is used to execute {@link liquibase.action.Action} objects using the registered {@link liquibase.actionlogic.ActionLogic} implementations.
 */
public class ActionExecutor {

    /**
     * Executes an action.
     * Default implementation creates a {@link liquibase.actionlogic.ActionExecutor.Plan} for the given action and executes it.
     * Will return a single ActionResult if there is a single Action that is executed, but can return a {@link liquibase.actionlogic.CompoundResult} if multiple actions end up being executed to perform the starting action.
     */
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return createPlan(action, scope).execute(scope);
    }

    /**
     * Convenience version of {@link #execute(Action, Scope)} for performing a query
     */
    public QueryResult query(Action action, Scope scope) throws ActionPerformException {
        return (QueryResult) createPlan(action, scope).execute(scope);
    }

    public ActionStatus checkStatus(Action action, Scope scope) {
        ActionLogic logic = getActionLogic(action, scope);

        if (logic == null) {
            return new ActionStatus().unknown("No ActionLogic implementation for "+action.describe()+" for "+scope.describe());
        }

        return logic.checkStatus(action, scope);
    }

    public ValidationErrors validate(Action action, Scope scope) {
        ActionLogicFactory actionLogicFactory = scope.getSingleton(ActionLogicFactory.class);

        ActionLogic actionLogic = actionLogicFactory.getActionLogic(action, scope);
        if (actionLogic == null) {
            String scopeDescription;
            if (scope.getDatabase() == null) {
                scopeDescription = "No Database";
            } else {
                scopeDescription = scope.getDatabase().getShortName();
            }
            return new ValidationErrors().addUnsupportedError("No supported ActionLogic implementation found for '" + action.describe() + "'", scopeDescription);
        }

        return actionLogic.validate(action, scope);
    }

    /**
     * Generates a Plan listing the Actions and corresponding ActionLogic implementations that will interact with external systems.
     * Normally {@link #execute(liquibase.action.Action, liquibase.Scope)} should be called, but this method is public for logging and testing purposes.
     * The Steps in the Plan will contain {@link liquibase.actionlogic.ActionLogic.InteractsExternally} implementations.
     */
    public Plan createPlan(Action action, Scope scope) throws ActionPerformException {
        Plan plan = new Plan();

        buildPlan(action, scope, plan, new ArrayDeque<ActionResult.Modifier>());

        return plan;
    }

    protected void buildPlan(Action action, Scope scope, Plan plan, Deque<ActionResult.Modifier> modifiers) throws ActionPerformException {
        ActionLogic actionLogic = getActionLogic(action, scope);

        if (actionLogic == null) {
            throw new ActionPerformException("No supported ActionLogic implementation found for '"+action.describe()+"' against "+scope.describe());
        }

        ValidationErrors validationErrors = actionLogic.validate(action, scope);
        if (validationErrors.hasErrors()) {
            throw new ActionPerformException("Validation Error(s): "+ StringUtils.join(validationErrors.getErrorMessages(), "; ")+" for "+action.describe()+" with "+actionLogic.getClass().getName());
        }

        if (actionLogic instanceof ActionLogic.InteractsExternally && ((ActionLogic.InteractsExternally) actionLogic).interactsExternally(action, scope)) {
            plan.addStep(new Plan.Step(action, actionLogic, modifiers));
            return;
        }

        ActionResult result = actionLogic.execute(action, scope);

        if (result instanceof DelegateResult) {
            List<Action> actions = ((DelegateResult) result).getActions();
            ActionResult.Modifier modifier = ((DelegateResult) result).getModifier();
            if (actions.size() == 0) {
                throw new ActionPerformException(actionLogic.getClass().getName()+" tried to handle '"+action.describe()+"' but returned no actions to run");
            } else {
                for (Action rewroteAction : actions) {
                    if (modifier != null) {
                        modifiers.push(modifier);
                    }
                    buildPlan(rewroteAction, scope, plan, modifiers);
                }
            }
        }
    }

    protected ActionLogic getActionLogic(Action action, Scope scope) {
        ActionLogicFactory actionLogicFactory = scope.getSingleton(ActionLogicFactory.class);

        return actionLogicFactory.getActionLogic(action, scope);
    }

    /**
     * An execution plan.
     */
    public static class Plan {

        private List<Step> steps = new ArrayList<>();

        public Plan addStep(Step step) {
            this.steps.add(step);
            return this;
        }

        public List<Step> getSteps() {
            return Collections.unmodifiableList(steps);
        }

        public String describe() {
            return StringUtils.join(getSteps(), "\nAND THEN: ", new StringUtils.StringUtilsFormatter<Step>() {
                @Override
                public String toString(Step step) {
                    return step.getAction().describe();
                }
            });
        }

        @Override
        public String toString() {
            return describe();
        }

        public ActionResult execute(Scope scope) throws ActionPerformException {
            if (getSteps().size() == 0) {
                throw new ActionPerformException("No steps in action plan");
            } else {
                LinkedHashMap<Action, ActionResult> results = new LinkedHashMap<>();
                for (Plan.Step step : getSteps()) {
                    Action finalAction = step.getAction();
                    results.put(finalAction, step.execute(scope));
                }

                if (results.size() == 1) {
                    return results.values().iterator().next();
                }
                return new CompoundResult(results);
            }

        }

        /**
         * A step in a {@link liquibase.actionlogic.ActionExecutor.Plan}.
         * The step contains an Action to run, the ActionLogic to execute it, and a Deque of Modifiers to adjust the result.
         */
        public static class Step {

            private Action action;
            private ActionLogic logic;
            private Deque<ActionResult.Modifier> modifiers;

            public Step(Action action, ActionLogic logic, Deque<ActionResult.Modifier> modifiers) {
                this.action = action;
                this.logic = logic;
                this.modifiers = modifiers;
            }

            public Action getAction() {
                return action;
            }

            public ActionLogic getLogic() {
                return logic;
            }

            public Deque<ActionResult.Modifier> getModifiers() {
                return modifiers;
            }

            public ActionResult execute(Scope scope) throws ActionPerformException {
                ActionResult result = this.getLogic().execute(action, scope);
                for (ActionResult.Modifier modifier : this.getModifiers()) {
                    result = modifier.rewrite(result);
                }

                return result;
            }
        }

    }

}
