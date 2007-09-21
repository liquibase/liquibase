package liquibase.database.template;

import liquibase.exception.JDBCException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Generic callback interface for code that operates on a PreparedStatement.
 * Allows to execute any number of operations on a single PreparedStatement,
 * for example a single <code>executeUpdate</code> call or repeated
 * <code>executeUpdate</code> calls with varying parameters.
 * <p/>
 * <p>Used internally by JdbcTemplate, but also useful for application code.
 * Note that the passed-in PreparedStatement can have been created by the
 * framework or by a custom PreparedStatementCreator. However, the latter is
 * hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard PreparedStatement is fine. Custom actions will
 * always set parameter values themselves, so that PreparedStatementCreator
 * capability is not needed either.
 *
 * @author Juergen Hoeller
 * @see JdbcTemplate#execute(String,PreparedStatementCallback)
 * @see JdbcTemplate#execute(PreparedStatementCreator,PreparedStatementCallback)
 * @since 16.03.2004
 */
public interface PreparedStatementCallback {

    /**
     * Gets called by <code>JdbcTemplate.execute</code> with an active JDBC
     * PreparedStatement. Does not need to care about closing the Statement
     * or the Connection, or about handling transactions: this will all be
     * handled by Spring's JdbcTemplate.
     * <p/>
     * <p><b>NOTE:</b> Any ResultSets opened should be closed in finally blocks
     * within the callback implementation. Spring will close the Statement
     * object after the callback returned, but this does not necessarily imply
     * that the ResultSet resources will be closed: the Statement objects might
     * get pooled by the connection pool, with <code>close</code> calls only
     * returning the object to the pool but not physically closing the resources.
     * <p/>
     * <p>If called without a thread-bound JDBC transaction (initiated by
     * DataSourceTransactionManager), the code will simply get executed on the
     * JDBC connection with its transactional semantics. If JdbcTemplate is
     * configured to use a JTA-aware DataSource, the JDBC connection and thus
     * the callback code will be transactional if a JTA transaction is active.
     * <p/>
     * <p>Allows for returning a result object created within the callback, i.e.
     * a domain object or a collection of domain objects. Note that there's
     * special support for single step actions: see JdbcTemplate.queryForObject etc.
     * A thrown RuntimeException is treated as application exception, it gets
     * propagated to the caller of the template.
     *
     * @param ps active JDBC PreparedStatement
     * @return a result object, or <code>null</code> if none
     * @throws java.sql.SQLException if thrown by a JDBC method, to be auto-converted
     *                               to a DataAccessException by a SQLExceptionTranslator
     * @throws JDBCException         in case of custom exceptions
     * @see JdbcTemplate#queryForObject(String,Object[],Class)
     * @see JdbcTemplate#queryForList(String,Object[])
     */
    Object doInPreparedStatement(PreparedStatement ps) throws SQLException, JDBCException;

}
