package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * A Sybase specific Delegate that removes the calls to commit
 * and rollback as Sybase requires that autocommit be set to true.
 * 
 * @author <a href="mailto:csuml@yahoo.co.uk">Paul Keeble</a>
 *
 */
public class SybaseConnectionDelegate extends SQLConnectionDelegate {
    public SybaseConnectionDelegate(Connection delegate) {
        super(delegate);
    }

    @Override
    public void commit() throws SQLException {
        
    }

    @Override
    public void rollback() throws SQLException {
        
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        
    }
}
