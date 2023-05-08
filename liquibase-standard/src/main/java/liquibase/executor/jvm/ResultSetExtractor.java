package liquibase.executor.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback interface used by {@link liquibase.executor.Executor}'s query methods.
 * Implementations of this interface perform the actual work of extracting
 * results from a {@link java.sql.ResultSet}, but don't need to worry
 * about exception handling. {@link java.sql.SQLException SQLExceptions}
 * will be caught and handled by the calling JdbcTemplate.
 * <p/>
 *
 * @author Spring Framework
 * @see liquibase.executor.Executor
 * @see RowCallbackHandler
 * @see RowMapper
 */
interface ResultSetExtractor {

    /**
     * Implementations must implement this method to process the entire ResultSet.
     *
     * @param rs ResultSet to extract data from. Implementations should
     *           not close this: it will be closed by the calling JdbcTemplate.
     * @return an arbitrary result object, or <code>null</code> if none
     *         (the extractor will typically be stateful in the latter case).
     * @throws java.sql.SQLException if a SQLException is encountered getting column
     *                               values or navigating (that is, there's no need to catch SQLException)
     * @throws liquibase.exception.DatabaseException         in case of custom exceptions
     */
    Object extractData(ResultSet rs) throws SQLException;

}
