package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

import java.util.Set;

public class DB2DatabaseSnapshot extends SqlDatabaseSnapshot {
    public DB2DatabaseSnapshot() {
    }

    public DB2DatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public DB2DatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public DB2DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public DB2DatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}