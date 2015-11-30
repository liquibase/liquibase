package liquibase.actionlogic;

import groovy.lang.Closure;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;

public class MockActionLogic extends AbstractActionLogic {

    private Closure executeLogic;
    private int priority;
    private String name;
    private Class<? extends Action> supportedAction;

    public MockActionLogic(String name, int priority, Class<? extends Action> supportedAction) {
        this.name = name;
        this.priority = priority;
        this.supportedAction = supportedAction;
    }

    public MockActionLogic(String name, int priority, Class<? extends Action> supportedAction, Closure executeLogic) {
        this(name, priority, supportedAction);
        this.executeLogic = executeLogic;
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return supportedAction;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        if (executeLogic == null) {
            return new ExecuteResult("Executed "+toString());
        } else {
            return (ActionResult) executeLogic.call();
        }

    }

    @Override
    public String toString() {
        return "Mock action logic '"+name+"'";
    }

    @Override
    public int getPriority(Action action, Scope scope) {
        int parentPriority = super.getPriority(action, scope);
        if (parentPriority < 0) {
            return parentPriority;
        } else {
            return priority;
        }
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return null;
    }
}
