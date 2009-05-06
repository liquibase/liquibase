package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.util.Set;

public class MaxDBDatabaseSnapshot extends SqlDatabaseSnapshot {
    public MaxDBDatabaseSnapshot() {
    }

    public MaxDBDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public MaxDBDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public MaxDBDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public MaxDBDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}