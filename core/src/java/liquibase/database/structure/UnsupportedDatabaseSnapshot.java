package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.util.Set;

public class UnsupportedDatabaseSnapshot extends SqlDatabaseSnapshot {
    public UnsupportedDatabaseSnapshot() {
    }

    public UnsupportedDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public UnsupportedDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public UnsupportedDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public UnsupportedDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}
