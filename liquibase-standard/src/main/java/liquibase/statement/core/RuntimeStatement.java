package liquibase.statement.core;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.AbstractSqlStatement;

import static liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;

public class RuntimeStatement extends AbstractSqlStatement {
    public Sql[] generate(Database database) {
        return EMPTY_SQL;
    }
}
