package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;

import java.util.UUID;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private UUID snapshotId;

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        this.snapshotId = snapshotId;
    }

    public boolean snapshotByDefault() {
        return true;
    }

    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }

    public boolean equals(DatabaseObject otherObject, Database accordingTo) {
        if (otherObject == null) {
            return false;
        }
        if (this.getName() == null) {
            return otherObject.getName() == null;
        }

        return accordingTo.correctObjectName(getName(), otherObject.getClass()).equals(accordingTo.correctObjectName(otherObject.getName(), otherObject.getClass()));
    }

    public boolean matches(DatabaseObject otherObject, Database accordingTo) {
        if (otherObject == null) {
            return false;
        }

        if (!equals(otherObject, accordingTo)) {
            return false;
        }
        return accordingTo.correctObjectName(getName(), otherObject.getClass()).equals(accordingTo.correctObjectName(otherObject.getName(), otherObject.getClass()));
        //todo: better matching
    }
}
