package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;

import java.util.Set;

public interface DatabaseSnapshotGenerator extends PrioritizedService {
    /**
     * Default generator, lower priority.
     */
    public static final int PRIORITY_DEFAULT = 1;
    /**
     * Generator specific to database, higher priority.
     *
     */
    public static final int PRIORITY_DATABASE = 5;

    boolean supports(Database database);

    int getPriority(Database database);

    DatabaseSnapshot createSnapshot(Database database, String schema, Set<DiffStatusListener> listeners) throws DatabaseException;
}
