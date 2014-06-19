package liquibase.statement.core;

import liquibase.database.Database;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class RuntimeStatement extends AbstractSqlStatement {
    public Sql[] generate(ExecutionOptions options) {
        return new Sql[0];
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
