package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddUniqueConstraintsAction;
import liquibase.actionlogic.core.AddUniqueConstraintsLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringClauses;

public class AddUniqueConstraintsLogicMSSQL extends AddUniqueConstraintsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(UniqueConstraint uniqueConstraint, AddUniqueConstraintsAction action, Scope scope) {
        StringClauses clauses = super.generateSql(uniqueConstraint, action, scope);
        String tablespace = uniqueConstraint.tablespace;

        clauses.replace(Clauses.tablespace, "ON " + tablespace);
        return clauses;
    }
}
