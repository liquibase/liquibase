package liquibase.executor.jvm;

import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Generic callback interface for code that operates on a CallableStatement.
 * Allows to execute any number of operations on a single CallableStatement,
 * for example a single execute call or repeated execute calls with varying
 * parameters.
 *
 * @author Spring Framework
 */
interface CallableStatementCallback {

    /**
     * Gets called by <code>JdbcTemplate.execute</code> with an active JDBC
     * CallableStatement. Does not need to care about closing the Statement
     * or the Connection, or about handling transactions: this will all be
     * handled by Spring's JdbcTemplate.
     * @param cs active JDBC CallableStatement
     * @return a result object, or <code>null</code> if none
     * @throws SQLException        if thrown by a JDBC method, to be auto-converted
     *                             into a DataAccessException by a SQLExceptionTranslator
     * @throws liquibase.exception.DatabaseException in case of custom exceptions
     */
    Object doInCallableStatement(CallableStatement cs) throws SQLException, DatabaseException;

    SqlStatement getStatement();
}
