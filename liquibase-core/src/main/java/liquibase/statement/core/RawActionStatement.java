package liquibase.statement.core;

import liquibase.action.Action;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;


/**
 * Statement class that wraps arbitrary {@link liquibase.action.Action} classes.
 * This class is used primarily when a {@link liquibase.change.Change} class wants to execute Java code.
 */
public class RawActionStatement extends AbstractStatement {

    public static final String ACTIONS = "actions";

    public RawActionStatement(Action... actions) {
        setActions(actions);
    }

    /**
     * Return actions to execute.
     */
    public Action[] getActions() {
        return getAttribute(ACTIONS, Action[].class);
    }

    public RawActionStatement setActions(Action... actions) {
        if (actions == null || actions.length == 0) {
            return (RawActionStatement) setAttribute(ACTIONS, null);
        } else {
            return (RawActionStatement) setAttribute(ACTIONS, actions);
        }
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
