package liquibase.executor.jvm;

import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;

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
 * @author Spring Framework
 */
interface StatementCallback {

    /**
     * Gets called by <code>JdbcTemplate.execute</code> with an active JDBC
     * Statement. Does not need to care about closing the Statement or the
     * Connection, or about handling transactions: this will all be handled
     * by JdbcTemplate.
     * <p/>
     *
     * @param stmt active JDBC Statement
     * @return a result object, or <code>null</code> if none
     */
    Object doInStatement(Statement stmt) throws SQLException, DatabaseException;

    SqlStatement getStatement();
}
