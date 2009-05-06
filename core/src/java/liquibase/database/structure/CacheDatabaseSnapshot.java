package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.util.Set;

public class CacheDatabaseSnapshot extends SqlDatabaseSnapshot {
    public CacheDatabaseSnapshot() {
    }

    public CacheDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public CacheDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public CacheDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public CacheDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}