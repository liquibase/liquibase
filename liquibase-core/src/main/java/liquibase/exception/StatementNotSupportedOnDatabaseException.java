package liquibase.exception;

import liquibase.database.Database;
import liquibase.statement.Statement;

public class StatementNotSupportedOnDatabaseException extends DatabaseException {
    private String reason;

    public StatementNotSupportedOnDatabaseException(Statement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getShortName());
    }

    public StatementNotSupportedOnDatabaseException(String reason, Statement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getShortName()+": "+reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
