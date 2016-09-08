package liquibase.statement;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

/**
 * To be implemented by instances that use a native jdbc drivers for execution
 */
public interface ExecutableConnectionStatement extends SqlStatement {
    /**
     * Execute the prepared statement
     * @param factory for creating a <code>PreparedStatement</code> object
     * @throws DatabaseException
     */
    void execute(JdbcConnection connection) throws DatabaseException;
}
