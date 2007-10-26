package liquibase.exception;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;

public class StatementNotSupportedOnDatabaseException extends JDBCException {
    private String reason;
    private SqlStatement statement;
    private Database database;

    public StatementNotSupportedOnDatabaseException(SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getProductName());
        this.statement = statement;
        this.database = database;
    }

    public StatementNotSupportedOnDatabaseException(String reason, SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getProductName()+": "+reason);
        this.statement = statement;
        this.database = database;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
