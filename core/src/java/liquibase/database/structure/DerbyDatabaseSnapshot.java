package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.util.Set;

public class DerbyDatabaseSnapshot extends SqlDatabaseSnapshot {
    public DerbyDatabaseSnapshot() {
    }

    public DerbyDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public DerbyDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public DerbyDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public DerbyDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}