package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.structure.DatabaseObject;

public abstract class AbstractSnapshotLogic extends AbstractActionLogic {

    abstract protected Class <? extends DatabaseObject> getTypeToSnapshot();

    abstract protected Class<? extends DatabaseObject>[] getSupportedBaseObject();

    @Override
    public int getPriority(Action action, Scope scope) {
        if (!(action instanceof SnapshotDatabaseObjectsAction)) {
            return PRIORITY_NOT_APPLICABLE;
        }

        DatabaseObject relatedTo = action.getAttribute(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);
        Class typeToSnapshot = action.getAttribute(SnapshotDatabaseObjectsAction.Attr.typeToSnapshot, Class.class);
        if (relatedTo == null || typeToSnapshot == null) {
            return PRIORITY_NOT_APPLICABLE;
        }

        if (typeToSnapshot.isAssignableFrom(getTypeToSnapshot())) {
            for (Class clazz : getSupportedBaseObject()) {
                if (relatedTo.getClass().isAssignableFrom(clazz)) {
                    return PRIORITY_DEFAULT;
                }
            }
        }

        return PRIORITY_NOT_APPLICABLE;
    }
}
