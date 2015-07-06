package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.AddUniqueConstraintAction;
import liquibase.actionlogic.core.AddUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.util.StringClauses;

public class AddUniqueConstraintLogicMSSQL extends AddUniqueConstraintLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddUniqueConstraintAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        String tablespace = action.uniqueConstraint.tablespace;

        clauses.replace(Clauses.tablespace, "ON " + tablespace);
        return clauses;
    }
}
