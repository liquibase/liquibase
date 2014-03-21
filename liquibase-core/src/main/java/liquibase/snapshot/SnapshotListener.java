package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

/**
 * Listener interface to be called during the snapshot process. Attach instances to {@link liquibase.snapshot.SnapshotControl}
 */
public interface SnapshotListener {

    /**
     * Called before a snapshot is done.
     * @param example Example of object to be created
     * @param database Database to be read from
     */
    public void willSnapshot(DatabaseObject example, Database database);

    /**
     * Called after an object is fully loaded from the database. Dependent objects may have their willSnapshot and finishSnapshot methods called before this method is called for a given example.
     * @param example Original example object used for the snapshot
     * @param snapshot Final snapshot object
     * @param database Database read from
     */
    void finishedSnapshot(DatabaseObject example, DatabaseObject snapshot, Database database);
}
