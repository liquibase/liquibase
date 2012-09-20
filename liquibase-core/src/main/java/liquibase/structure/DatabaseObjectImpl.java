package liquibase.structure;

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
