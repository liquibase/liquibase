package liquibase.actionlogic.core.rewrite;

import liquibase.Scope;
import liquibase.ScopeAttributes;
import liquibase.action.Action;
import liquibase.action.core.CreateSequenceAction;
import liquibase.action.core.ExecuteSqlAction;
import liquibase.actionlogic.ActionLogicPriority;
import liquibase.actionlogic.ActionRewriteLogic;
import liquibase.database.Database;

public class CreateSequenceLogic implements ActionRewriteLogic {
    @Override
    public ActionLogicPriority getPriority(Action action, Scope scope) {
        if (action instanceof CreateSequenceAction) {
            Database database = scope.get(ScopeAttributes.database, Database.class);
            if (database.supportsAutoIncrement()) {
                return ActionLogicPriority.DEFAULT;
            } else {
                return ActionLogicPriority.DEFAULT_NOT_SUPPORTED;
            }
        } else {
            return ActionLogicPriority.NOT_APPLICABLE;
        }
    }

    @Override
    public Action[] rewrite(Action action) {
        return new Action[] {
                new ExecuteSqlAction("create sequence "+action.getAttribute(CreateSequenceAction.Attributes.sequenceName, String.class))
        };
    }
}
