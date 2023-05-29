package liquibase.statement;

import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;

/**
 * To be implemented by instances that use a prepared statement for execution
 */
public interface ExecutablePreparedStatement extends SqlStatement {
    /**
     * Executes the prepared statement created by the given factory.
     *
     * @param factory a factory for creating a <code>PreparedStatement</code> object.
     * @throws DatabaseException if an exception occurs while executing the prepared statement.
     */
    void execute(PreparedStatementFactory factory) throws DatabaseException;
}
