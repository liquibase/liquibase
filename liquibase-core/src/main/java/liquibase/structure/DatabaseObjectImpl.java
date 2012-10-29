package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;

public abstract class DatabaseObjectImpl implements DatabaseObject {

    public boolean snapshotByDefault() {
        return true;
    }

    public boolean equals(DatabaseObject otherObject, Database accordingTo) {
        if (otherObject == null) {
            return false;
        }
        if (this.getName() == null) {
            return otherObject.getName() == null;
        }

        if (accordingTo.isCaseSensitive()) {
            return this.getName().equals(otherObject.getName());
        } else {
            return this.getName().equalsIgnoreCase(otherObject.getName());
        }
    }
}
