package liquibase.executor.jvm;

import java.sql.ResultSet;

/**
 * An interface used by {@link liquibase.executor.Executor} for processing rows of a
 * {@link java.sql.ResultSet} on a per-row basis. Implementations of
 * this interface perform the actual work of processing each row
 * but don't need to worry about exception handling.
 * {@link java.sql.SQLException SQLExceptions} will be caught and handled
 * by the calling JdbcTemplate.
 * <p/>
 * <p>In contrast to a {@link ResultSetExtractor}, a RowCallbackHandler
 * object is typically stateful: It keeps the result state within the
 * object, to be available for later inspection.
 * <p/>
 * <p>Consider using a {@link RowMapper} instead if you need to map
 * exactly one result object per row, assembling them into a List.
 *
 * @author Spring Framework
 * @see liquibase.executor.Executor
 * @see RowMapper
 * @see ResultSetExtractor
 */
interface RowCallbackHandler {

    /**
     * Implementations must implement this method to process each row of data
     * in the ResultSet. This method should not call <code>next()</code> on
     * the ResultSet; it is only supposed to extract values of the current row.
     * <p>Exactly what the implementation chooses to do is up to it:
     * A trivial implementation might simply count rows, while another
     * implementation might build an XML document.
     *
     * @param rs the ResultSet to process (pre-initialized for the current row)
     * @throws java.sql.SQLException if a SQLException is encountered getting
     *                               column values (that is, there's no need to catch SQLException)
     */
    void processRow(ResultSet rs);

}
