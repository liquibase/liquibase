package liquibase.structure.core;

import liquibase.structure.ObjectName;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(ObjectName name) {
        super(name);
    }
}
