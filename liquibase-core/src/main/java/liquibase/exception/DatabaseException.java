package liquibase.exception;

public class DatabaseException extends LiquibaseException {

    private static final long serialVersionUID = 1L;

    private final String sqlState;

    public DatabaseException() {
        this.sqlState = null;
    }

    public DatabaseException(String message) {
        super(message);
        this.sqlState = null;
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.sqlState = null;
    }

    public DatabaseException(Throwable cause) {
        super(cause);
        this.sqlState = null;
    }

    public DatabaseException(String message, String sqlState, Throwable cause) {
        super(message, cause);
        this.sqlState = sqlState;
    }

    /**
     * @see java.sql.SQLException#getSQLState()
     */
    public String getSqlState() {
        return sqlState;
    }
}
