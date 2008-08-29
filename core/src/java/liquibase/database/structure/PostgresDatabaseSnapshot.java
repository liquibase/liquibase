package liquibase.database.structure;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.diff.DiffStatusListener;

import java.util.Set;

public class PostgresDatabaseSnapshot extends SqlDatabaseSnapshot {
    public PostgresDatabaseSnapshot() {
    }

    public PostgresDatabaseSnapshot(Database database) throws JDBCException {
        super(database);
    }

    public PostgresDatabaseSnapshot(Database database, String schema) throws JDBCException {
        super(database, schema);
    }

    public PostgresDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners) throws JDBCException {
        super(database, statusListeners);
    }

    public PostgresDatabaseSnapshot(Database database, Set<DiffStatusListener> statusListeners, String requestedSchema) throws JDBCException {
        super(database, statusListeners, requestedSchema);
    }
}