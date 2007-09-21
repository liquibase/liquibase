package liquibase.database.sql;

import liquibase.database.DatabaseConnection;
import liquibase.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedSqlStatement {
    /**
     * Create a statement in this connection. Allows implementations to use
     * PreparedStatements. The JdbcTemplate will close the created statement.
     *
     * @return a prepared statement
     * @throws java.sql.SQLException there is no need to catch SQLExceptions
     *                               that may be thrown in the implementation of this method.
     *                               The JdbcTemplate class will handle them.
     */
    PreparedStatement createPreparedStatement(Database database) throws SQLException;
}
