package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;

public abstract class DatabaseObjectImpl implements DatabaseObject {

    private boolean partial = true;

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
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
