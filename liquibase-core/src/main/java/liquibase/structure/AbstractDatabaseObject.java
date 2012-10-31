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
