package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.ObjectBasedQueryResult;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.ActionPerformException;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Set;

public abstract class AbstractSnapshotDatabaseObjectsLogicOffline<T extends SnapshotDatabaseObjectsAction> extends AbstractSnapshotDatabaseObjectsLogic<T> {

    @Override
    protected Class<? extends DatabaseConnection> getRequiredConnection() {
        return OfflineConnection.class;
    }


    @Override
    public ActionResult execute(SnapshotDatabaseObjectsAction action, Scope scope) throws ActionPerformException {
        final Database database = scope.getDatabase();
        OfflineConnection connection = (OfflineConnection) database.getConnection();
        Snapshot snapshot = connection.getSnapshot();

        if (snapshot == null) {
            throw new ActionPerformException("No snapshot found");
        }

        final ObjectReference relatedTo = action.relatedTo;

        if (relatedTo.instanceOf(Catalog.class) && !database.supportsCatalogs()) {
            throw new ActionPerformException("Cannot snapshot catalogs on "+database.getShortName());
        }

        if (relatedTo.instanceOf(Schema.class) && !database.supportsSchemas()) {
            throw new ActionPerformException("Cannot snapshot schemas on "+database.getShortName());
        }

        Set allObjectsOfType = snapshot.get(getTypeToSnapshot());


        return new ObjectBasedQueryResult(CollectionUtil.select(new ArrayList(allObjectsOfType), getDatabaseObjectFilter(action, scope)));
    }

    protected abstract CollectionUtil.CollectionFilter<? extends DatabaseObject> getDatabaseObjectFilter(SnapshotDatabaseObjectsAction action, Scope scope);


}
