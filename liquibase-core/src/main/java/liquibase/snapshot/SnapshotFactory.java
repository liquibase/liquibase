package liquibase.snapshot;

import liquibase.Scope;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.actionlogic.QueryResult;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

public class SnapshotFactory {

    protected SnapshotFactory() {
    }

    public boolean has(ObjectReference object, Scope scope) throws ActionPerformException, InvalidExampleException {
        QueryResult result = (QueryResult) new ActionExecutor().execute(new SnapshotDatabaseObjectsAction(object), scope);
        return result.size() > 0;
    }

    public <T extends DatabaseObject> T get(ObjectReference object, Scope scope) throws ActionPerformException, InvalidExampleException {
        QueryResult result = (QueryResult) new ActionExecutor().execute(new SnapshotDatabaseObjectsAction(object), scope);

        return (T) result.asObject(object.objectType);
    }
}
