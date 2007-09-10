package liquibase.database.sql;

import liquibase.database.Database;

public interface SqlStatement {
    public String getSqlStatement(Database database);
}
