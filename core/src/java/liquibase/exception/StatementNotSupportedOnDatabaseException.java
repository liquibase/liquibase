package liquibase.exception;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;

public class StatementNotSupportedOnDatabaseException extends JDBCException {
    private String reason;

    public StatementNotSupportedOnDatabaseException(SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getProductName());
    }

    public StatementNotSupportedOnDatabaseException(String reason, SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getProductName()+": "+reason);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
