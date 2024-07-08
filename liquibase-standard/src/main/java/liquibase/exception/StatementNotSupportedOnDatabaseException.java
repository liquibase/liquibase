package liquibase.exception;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;

public class StatementNotSupportedOnDatabaseException extends DatabaseException {
    private static final long serialVersionUID = 889271005149363642L;
    private String reason;

    public StatementNotSupportedOnDatabaseException(SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getDisplayName());
    }

    public StatementNotSupportedOnDatabaseException(String reason, SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getDisplayName()+": "+reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
