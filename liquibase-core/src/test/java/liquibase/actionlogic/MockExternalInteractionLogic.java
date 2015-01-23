package liquibase.actionlogic;

import groovy.lang.Closure;
import liquibase.Scope;
import liquibase.action.Action;

public class MockExternalInteractionLogic extends MockActionLogic implements ActionLogic.InteractsExternally {

    public MockExternalInteractionLogic(String name, int priority, Class<? extends Action> supportedAction) {
        super(name, priority, supportedAction);
    }

    public MockExternalInteractionLogic(String name, int priority, Class<? extends Action> supportedAction, Closure executeLogic) {
        super(name, priority, supportedAction, executeLogic);
    }

    @Override
    public boolean interactsExternally(Action action, Scope scope) {
        return true;
    }
}
