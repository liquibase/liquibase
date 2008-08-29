package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.util.Set;

public class FirebirdDatabaseSnapshot extends SqlDatabaseSnapshot {
    public FirebirdDatabaseSnapshot() {
    }

    public FirebirdDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public FirebirdDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public FirebirdDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public FirebirdDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}