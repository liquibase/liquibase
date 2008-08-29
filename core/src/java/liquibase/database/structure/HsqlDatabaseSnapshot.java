package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.util.Set;

public class HsqlDatabaseSnapshot extends SqlDatabaseSnapshot {
    public HsqlDatabaseSnapshot() {
    }

    public HsqlDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public HsqlDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public HsqlDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public HsqlDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}