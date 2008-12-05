package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.sql.visitor.SqlStatementVisitor;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

import java.util.Collection;

public interface SqlStatement {
    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException;

    public String getEndDelimiter(Database database);

    public boolean supportsDatabase(Database database);

}
