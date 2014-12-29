package liquibase.actionlogic;

import liquibase.action.Action;

public interface ActionRewriteLogic extends ActionLogic {

    public Action[] rewrite(Action action);
}
