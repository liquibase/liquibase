package liquibase.database.jvm;

import liquibase.exception.DatabaseException;
import liquibase.servicelocator.LiquibaseService;

import java.sql.Connection;
import java.sql.Savepoint;

/**
 * A Sybase specific Delegate that removes the calls to commit
 * and rollback as Sybase requires that autocommit be set to true.
 *
 */
@LiquibaseService(skip=true)
public class SybaseConnection extends JdbcConnection {

    public SybaseConnection() {}

    public SybaseConnection(Connection delegate) {
        super(delegate);
    }

    @Override
    public void commit() throws DatabaseException {
        
    }

    @Override
    public void rollback() throws DatabaseException {
        
    }

    @Override
    public void rollback(Savepoint savepoint) throws DatabaseException {
        
    }
}
