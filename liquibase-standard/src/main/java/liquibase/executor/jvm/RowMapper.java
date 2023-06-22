package liquibase.executor.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface used by {@link liquibase.executor.Executor} for mapping rows of a
 * {@link java.sql.ResultSet} on a per-row basis. Implementations of this
 * interface perform the actual work of mapping each row to a result object,
 * but don't need to worry about exception handling.
 * {@link java.sql.SQLException SQLExceptions} will be caught and handled
 * by the calling JdbcTemplate.
 * <p/>
 *
 * @author Spring Framework
 * @see liquibase.executor.Executor
 * @see RowCallbackHandler
 * @see ResultSetExtractor
 */
public interface RowMapper {

    /**
     * Implementations must implement this method to map each row of data
     * in the ResultSet. This method should not call <code>next()</code> on
     * the ResultSet; it is only supposed to map values of the current row.
     *
     * @param rs     the ResultSet to map (pre-initialized for the current row)
     * @param rowNum the number of the current row
     * @return the result object for the current row
     * @throws java.sql.SQLException if a SQLException is encountered getting
     *                               column values (that is, there's no need to catch SQLException)
     */
    Object mapRow(ResultSet rs, int rowNum) throws SQLException;

}
