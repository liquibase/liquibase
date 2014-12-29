package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;

public interface ActionLogic {

    ActionLogicPriority getPriority(Action action, Scope scope);

}
