package liquibase.structure;

import liquibase.database.Database;

public abstract class DatabaseObjectImpl implements DatabaseObject {

    public boolean equals(DatabaseObject otherObject, Database accordingTo) {
        return equals(otherObject.getName(), accordingTo);
    }

    public boolean equals(String otherObjectName, Database accordingTo) {
        if (this.getName() == null) {
            return otherObjectName == null;
        }
        if (otherObjectName == null) {
            return false;
        }
        if (accordingTo.isCaseSensitive()) {
            return this.getName().equals(otherObjectName);
        } else {
            return this.getName().equalsIgnoreCase(otherObjectName);
        }
    }
}
