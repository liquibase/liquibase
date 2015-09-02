package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexesAction;
import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectActionGenerator;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class MissingIndexActionGenerator implements MissingObjectActionGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
    }
    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        List<Action> returnList = new ArrayList<>();
        returnList.add(new CreateIndexesAction((Index) missingObject));

        return returnList;
    }
}
