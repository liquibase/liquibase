package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddUniqueConstraintAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;

public class AddUniqueConstraintLogicMSSQL extends AddUniqueConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddUniqueConstraintAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        String tablespace = action.tablespace;

        clauses.replace(Clauses.tablespace, "ON " + tablespace);
        return clauses;
    }
}
