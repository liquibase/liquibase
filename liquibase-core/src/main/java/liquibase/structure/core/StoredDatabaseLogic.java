package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.ObjectReference;

public abstract class StoredDatabaseLogic<T extends StoredDatabaseLogic> extends AbstractDatabaseObject {

    public Boolean valid;
    public String body;

    public StoredDatabaseLogic() {
    }

    public StoredDatabaseLogic(String name) {
        super(name);
    }

    public StoredDatabaseLogic(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public StoredDatabaseLogic(ObjectReference container, String name) {
        super(container, name);
    }
}
