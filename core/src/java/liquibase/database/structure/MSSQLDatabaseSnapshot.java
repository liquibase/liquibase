package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.util.Set;

public class MSSQLDatabaseSnapshot extends SqlDatabaseSnapshot {
    public MSSQLDatabaseSnapshot() {
    }

    public MSSQLDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public MSSQLDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public MSSQLDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public MSSQLDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}