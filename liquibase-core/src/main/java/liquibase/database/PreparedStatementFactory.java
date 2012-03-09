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
     * Create a <code>PreparedStatement</code> object,
     * sql pre-compilation might take place, depending on driver support. 
     * @param sql to execute
     * @return a <code>PreparedStatement</code> object
     * @throws DatabaseException
     */
    public PreparedStatement create(String sql) throws DatabaseException {
        return con.prepareStatement(sql);
    }

    @Override
    public String toString() {
        return "[con: " + con.toString() + "]";
    }
}
