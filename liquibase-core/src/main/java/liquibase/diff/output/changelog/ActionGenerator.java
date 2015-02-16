package liquibase.diff.output.changelog;

import liquibase.Scope;
import liquibase.servicelocator.Service;
import liquibase.structure.DatabaseObject;

public interface ActionGenerator extends Service{

    int getPriority(Class<? extends DatabaseObject> objectType, Scope referenceScope, Scope targetScope);

    public Class<? extends DatabaseObject>[] runAfterTypes();

    public Class<? extends DatabaseObject>[] runBeforeTypes();

}
