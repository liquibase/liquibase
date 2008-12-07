package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public interface SqlStatement {
    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException;

    public String getEndDelimiter(Database database);

    public boolean supportsDatabase(Database database);

}
