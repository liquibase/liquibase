package liquibase.statement.core;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class RuntimeStatement extends AbstractSqlStatement {
    public Sql[] generate(Database database) {
        return new Sql[0];
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
