package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.*;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience abstract base class for {@link liquibase.action.core.SnapshotDatabaseObjectsAction} related logic.
 * Implementations should not directly execute the metadata read, but instead return a {@link liquibase.actionlogic.DelegateResult} that returns simple lower-level actions.
 * This pattern is built into this methods {@link #execute(liquibase.action.Action, liquibase.Scope)} method.
 */
public abstract class AbstractSnapshotDatabaseObjectsLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SnapshotDatabaseObjectsAction.class;
    }

    @Override
    public int getPriority(Action action, Scope scope) {
        int priority = super.getPriority(action, scope);
        if (priority == PRIORITY_NOT_APPLICABLE) {
            return priority;
        }

        DatabaseObject relatedTo = action.get(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);
        Class typeToSnapshot = action.get(SnapshotDatabaseObjectsAction.Attr.typeToSnapshot, Class.class);
        if (relatedTo == null || typeToSnapshot == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (typeToSnapshot.isAssignableFrom(getTypeToSnapshot())) {
            for (Class clazz : getSupportedRelatedTypes()) {
                if (relatedTo.getClass().isAssignableFrom(clazz)) {
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
     * Return the type(s) of {@link liquibase.action.core.SnapshotDatabaseObjectsAction.Attr#relatedTo} objects this implementation supports.
     * Used in {@link #getPriority(liquibase.action.Action, liquibase.Scope)}.
     */
    abstract protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes();

    /**
     * Default implementation returns a {@link liquibase.actionlogic.DelegateResult} based on {@link #createSnapshotAction(liquibase.action.Action, liquibase.Scope)}  and {@link #createModifier(liquibase.action.Action, liquibase.Scope)}.
     */
    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        try {
            return new DelegateResult(createModifier(action, scope), createSnapshotAction(action, scope));
        } catch (DatabaseException e) {
            throw new ActionPerformException(e);
        }

    }

    /**
     * Return a lower-level action that will snapshot given type of objects that are related to the given object.
     * The QueryResult from the Action returned by this method will be fed through the object returned by {@link #createModifier(liquibase.action.Action, liquibase.Scope)}.
     */
    protected abstract Action createSnapshotAction(Action action, Scope scope) throws DatabaseException;

    /**
     * Returns a {@link liquibase.actionlogic.ActionResult.Modifier} that will convert the raw results from the action returned by {@link #createSnapshotAction(liquibase.action.Action, liquibase.Scope)}
     * to a list of objects.
     * Default implementation returns {@link liquibase.actionlogic.core.AbstractSnapshotDatabaseObjectsLogic.SnapshotModifier} which uses {@link #convertToObject(liquibase.actionlogic.RowBasedQueryResult.Row, liquibase.action.Action, liquibase.Scope)}
     * to convert the returned QueryResult to the correct DatabaseObject.
     *
     * The passed action is the original action, not the one returned by {@link #createSnapshotAction(liquibase.action.Action, liquibase.Scope)}
     */
    protected ActionResult.Modifier createModifier(final Action originalAction, final Scope scope) {
        return new SnapshotModifier(originalAction, scope);
    }

    /**
     * Converts a row returned by the generated action into the final object type.
     */
    protected abstract DatabaseObject convertToObject(RowBasedQueryResult.Row row, Action originalAction, Scope scope);

    /**
     * Called for each DatabaseObject in {@link SnapshotModifier#rewrite(liquibase.actionlogic.ActionResult)} to "fix" any raw data coming back from the database.
     * Default implementation trims object name to null.
     */
    protected void correctObject(DatabaseObject object) {
        object.setName(StringUtils.trimToNull(object.getSimpleName()));
    }

    /**
     * Class used by default {@link #createModifier(liquibase.action.Action, liquibase.Scope)} implementation.
     */
    protected class SnapshotModifier implements ActionResult.Modifier {

        private Action originalAction;
        private Scope scope;

        public SnapshotModifier(Action originalAction, Scope scope) {
            this.originalAction = originalAction;
            this.scope = scope;
        }

        public Action getOriginalAction() {
            return originalAction;
        }

        public Scope getScope() {
            return scope;
        }

        @Override
        public ActionResult rewrite(ActionResult result) {
            List<DatabaseObject> databaseObjects = new ArrayList<DatabaseObject>();
            for (RowBasedQueryResult.Row row : ((RowBasedQueryResult) result).getRows()) {
                DatabaseObject object = convertToObject(row, getOriginalAction(), getScope());
                correctObject(object);
                databaseObjects.add(object);
            }

            return new ObjectBasedQueryResult(databaseObjects);
        }

    }

}
