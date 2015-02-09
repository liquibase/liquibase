package liquibase.snapshot;

import liquibase.Scope;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionExecutor;
import liquibase.actionlogic.QueryResult;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;

public class SnapshotFactory {

    protected SnapshotFactory() {
    }

    public boolean has(DatabaseObject example, Scope scope) throws ActionPerformException, InvalidExampleException {
        QueryResult result = (QueryResult) new ActionExecutor().execute(new SnapshotDatabaseObjectsAction(example.getClass(), example), scope);
        return result.size() > 0;
    }
}
