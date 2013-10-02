package liquibase.statement.core;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.AbstractSqlStatement;

public class RuntimeStatement extends AbstractSqlStatement {
    public Sql[] generate(Database database) {
        return new Sql[0];
    }
}
