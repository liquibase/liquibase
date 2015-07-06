package liquibase.diff.output.changelog;

import liquibase.Scope;
import liquibase.servicelocator.Service;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;

public interface ActionGenerator extends Service{

    int getPriority(Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope);

    public Class<? extends DatabaseObject>[] runAfterTypes();

    public Class<? extends DatabaseObject>[] runBeforeTypes();

}
