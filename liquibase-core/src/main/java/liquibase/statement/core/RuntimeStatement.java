package liquibase.statement.core;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

public class RuntimeStatement implements SqlStatement {
    public Sql[] generate(Database database) {
        return new Sql[0];
    }
}
