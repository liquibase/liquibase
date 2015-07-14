package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.*;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience abstract base class for {@link liquibase.action.core.SnapshotDatabaseObjectsAction} related logic.
 * Implementations should not directly execute the metadata read, but instead return a {@link liquibase.actionlogic.DelegateResult} that returns simple lower-level actions.
 * This pattern is built into this methods {@link #execute(liquibase.action.Action, liquibase.Scope)} method.
 */
public abstract class AbstractSnapshotDatabaseObjectsLogic<T extends SnapshotDatabaseObjectsAction> extends AbstractActionLogic<T> {

    @Override
    protected Class<T> getSupportedAction() {
        return (Class<T>) SnapshotDatabaseObjectsAction.class;
    }

    @Override
    public int getPriority(T action, Scope scope) {
        int priority = super.getPriority(action, scope);
        if (priority == PRIORITY_NOT_APPLICABLE) {
            return priority;
        }

        if (action.relatedTo == null || action.typeToSnapshot == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (action.typeToSnapshot.isAssignableFrom(getTypeToSnapshot())) {
            for (Class clazz : getSupportedRelatedTypes()) {
                if (clazz.isAssignableFrom(action.relatedTo.objectType)) {
                    return priority;
                }
            }
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    /**
     * Return the type of object this logic implementation supports.
     * Used in {@link #getPriority(liquibase.action.Action, liquibase.Scope)}.
     */
    abstract protected Class <? extends DatabaseObject> getTypeToSnapshot();

    /**
     * Return the type(s) of {@link liquibase.action.core.SnapshotDatabaseObjectsAction#relatedTo} objects this implementation supports.
     * Used in {@link #getPriority(liquibase.action.Action, liquibase.Scope)}.
     */
    abstract protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes();

}
