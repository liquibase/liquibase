package liquibase.database;

import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

import java.sql.PreparedStatement;

/**
 * Factory for PreparedStatements
 */
public final class PreparedStatementFactory {

    private final JdbcConnection con;

    public PreparedStatementFactory(JdbcConnection con) {
        if(con == null) throw new IllegalArgumentException("connection must not be null");
        this.con = con;
    }

    /**
     * Creates a <code>PreparedStatement</code> object for the specified SQL statement.
     * The SQL statement may be pre-compiled by the driver depending on its support.
     *
     * @param sql the SQL statement to execute
     * @return a <code>PreparedStatement</code> object representing the specified SQL statement
     * @throws DatabaseException if a database access error occurs or the given SQL statement is invalid
     */
    public PreparedStatement create(String sql) throws DatabaseException {
        return con.prepareStatement(sql);
    }

    @Override
    public String toString() {
        return "[con: " + con + "]";
    }
}
