package liquibase.statement;

import liquibase.database.Database;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableSqlStatement extends SqlStatement {

    /**
     * Create a callable statement in this connection. Allows implementations to use
     * CallableStatements.
     *
     * @return a callable statement
     * @throws java.sql.SQLException there is no need to catch SQLExceptions
     *                               that may be thrown in the implementation of this method.
     *                               The JdbcTemplate class will handle them.
     */
    CallableStatement createCallableStatement(Database database) throws SQLException;
}
