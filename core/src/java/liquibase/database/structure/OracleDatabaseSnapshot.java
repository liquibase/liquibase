package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.util.Set;

public class OracleDatabaseSnapshot extends SqlDatabaseSnapshot {
    public OracleDatabaseSnapshot() {
    }

    public OracleDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public OracleDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public OracleDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public OracleDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}