package liquibase.database.sql;

import liquibase.database.DatabaseConnection;

import java.sql.CallableStatement;
import java.sql.SQLException;

public interface CallableSqlStatement {

    /**
     * Create a callable statement in this connection. Allows implementations to use
     * CallableStatements.
     *
     * @param con Connection to use to create statement
     * @return a callable statement
     * @throws java.sql.SQLException there is no need to catch SQLExceptions
     *                               that may be thrown in the implementation of this method.
     *                               The JdbcTemplate class will handle them.
     */
    CallableStatement createCallableStatement(DatabaseConnection con) throws SQLException;
}
