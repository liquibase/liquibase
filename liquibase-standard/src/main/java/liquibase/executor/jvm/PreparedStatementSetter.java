package liquibase.executor.jvm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * General callback interface used by the {@link liquibase.executor.Executor} class.
 * <p/>
 * <p>This interface sets values on a {@link java.sql.PreparedStatement} provided
 * by the JdbcTemplate class, for each of a number of updates in a batch using the
 * same SQL. Implementations are responsible for setting any necessary parameters.
 * SQL with placeholders will already have been supplied.
 * <p/>
 * <p>Implementations <i>do not</i> need to concern themselves with
 * SQLExceptions that may be thrown from operations they attempt.
 * The JdbcTemplate class will catch and handle SQLExceptions appropriately.
 *
 * @author Spring Framework
 */
interface PreparedStatementSetter {

    /**
     * Set parameter values on the given PreparedStatement.
     *
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws java.sql.SQLException if a SQLException is encountered
     *                               (i.e. there is no need to catch SQLException)
     */
    void setValues(PreparedStatement ps) throws SQLException;

}
