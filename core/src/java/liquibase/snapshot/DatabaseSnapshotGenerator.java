package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.DatabaseException;

import java.util.Set;

public interface DatabaseSnapshotGenerator {

    public static final int PRIORITY_DEFAULT = 1;
    public static final int PRIORITY_DATABASE = 5;

    boolean supports(Database database);

    int getPriority(Database database);


    DatabaseSnapshot createSnapshot(Database database, String schema, Set<DiffStatusListener> listeners) throws DatabaseException;
}
