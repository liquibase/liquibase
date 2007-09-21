package liquibase.database.template;

import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Generic callback interface for code that operates on a JDBC Statement.
 * Allows to execute any number of operations on a single Statement,
 * for example a single <code>executeUpdate</code> call or repeated
 * <code>executeUpdate</code> calls with varying SQL.
 * <p/>
 * <p>Used internally by JdbcTemplate, but also useful for application code.
 *
 * @author Juergen Hoeller
 * @see JdbcTemplate#execute(StatementCallback)
 * @since 16.03.2004
 */
public interface StatementCallback {

    /**
     * Gets called by <code>JdbcTemplate.execute</code> with an active JDBC
     * Statement. Does not need to care about closing the Statement or the
     * Connection, or about handling transactions: this will all be handled
     * by Spring's JdbcTemplate.
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
     * @param stmt active JDBC Statement
     * @return a result object, or <code>null</code> if none
     * @throws java.sql.SQLException        if thrown by a JDBC method, to be auto-converted
     *                             to a DataAccessException by a SQLExceptionTranslator
     * @see JdbcTemplate#queryForObject(String,Class)
     * @see JdbcTemplate#queryForRowSet(String)
     */
	Object doInStatement(Statement stmt) throws SQLException, JDBCException;

    SqlStatement getStatement();
}
