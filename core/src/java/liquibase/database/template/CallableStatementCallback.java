package liquibase.database.template;

import liquibase.exception.JDBCException;

import java.sql.SQLException;
import java.sql.CallableStatement;

/**
 * Generic callback interface for code that operates on a CallableStatement.
 * Allows to execute any number of operations on a single CallableStatement,
 * for example a single execute call or repeated execute calls with varying
 * parameters.
 *
 * <p>Used internally by JdbcTemplate, but also useful for application code.
 * Note that the passed-in CallableStatement can have been created by the
 * framework or by a custom CallableStatementCreator. However, the latter is
 * hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard CallableStatement is fine. Custom actions will
 * always set parameter values themselves, so that CallableStatementCreator
 * capability is not needed either.
 *
 * @author Juergen Hoeller
 * @since 16.03.2004
 * @see JdbcTemplate#execute(String, CallableStatementCallback)
 * @see JdbcTemplate#execute(CallableStatementCreator, CallableStatementCallback)
 */
public interface CallableStatementCallback {

	/**
	 * Gets called by <code>JdbcTemplate.execute</code> with an active JDBC
	 * CallableStatement. Does not need to care about closing the Statement
	 * or the Connection, or about handling transactions: this will all be
	 * handled by Spring's JdbcTemplate.
	 *
	 * <p><b>NOTE:</b> Any ResultSets opened should be closed in finally blocks
	 * within the callback implementation. Spring will close the Statement
	 * object after the callback returned, but this does not necessarily imply
	 * that the ResultSet resources will be closed: the Statement objects might
	 * get pooled by the connection pool, with <code>close</code> calls only
	 * returning the object to the pool but not physically closing the resources.
	 *
	 * <p>If called without a thread-bound JDBC transaction (initiated by
	 * DataSourceTransactionManager), the code will simply get executed on the
	 * JDBC connection with its transactional semantics. If JdbcTemplate is
	 * configured to use a JTA-aware DataSource, the JDBC connection and thus
	 * the callback code will be transactional if a JTA transaction is active.
	 *
	 * <p>Allows for returning a result object created within the callback, i.e.
	 * a domain object or a collection of domain objects. A thrown RuntimeException
	 * is treated as application exception: it gets propagated to the caller of
	 * the template.
	 *
	 * @param cs active JDBC CallableStatement
	 * @return a result object, or <code>null</code> if none
	 * @throws java.sql.SQLException if thrown by a JDBC method, to be auto-converted
	 * into a DataAccessException by a SQLExceptionTranslator
	 * @throws liquibase.exception.JDBCException in case of custom exceptions
	 */
	Object doInCallableStatement(CallableStatement cs) throws SQLException, JDBCException;

}
