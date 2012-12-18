package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.UUID;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private UUID snapshotId;

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        if (this.snapshotId != null) {
            throw new UnexpectedLiquibaseException("snapshotId already set");
        }
        this.snapshotId = snapshotId;
    }

    public boolean snapshotByDefault() {
        return true;
    }

    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }
}
