package liquibase.statement;

import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;

/**
 * To be implemented by instances that use a prepared statement for execution
 */
public interface ExecutablePreparedStatement extends Statement {
    /**
     * Execute the prepared statement
     * @param factory for creating a <code>PreparedStatement</code> object
     * @throws DatabaseException
     */
    void execute(PreparedStatementFactory factory) throws DatabaseException;
}
