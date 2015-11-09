package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;

public class Data extends AbstractDatabaseObject {

    @Override
    public boolean snapshotByDefault() {
        return false;
    }

}
