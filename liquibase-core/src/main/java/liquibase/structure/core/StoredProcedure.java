package liquibase.structure.core;

import liquibase.structure.ObjectReference;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(String name) {
        super(name);
    }

    public StoredProcedure(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public StoredProcedure(ObjectReference container, String name) {
        super(container, name);
    }
}
