package liquibase.exception;

import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;

public class StatementNotSupportedOnDatabaseException extends JDBCException {
    public StatementNotSupportedOnDatabaseException(SqlStatement statement, Database database) {
        super(statement.getClass().getName()+" is not supported on "+database.getProductName());
    }
}
