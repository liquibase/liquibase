package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;

public class CreateIndexLogicMSSQL extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public StringClauses getCreateIndexClauses(Action action, Scope scope) {
        StringClauses clauses = super.getCreateIndexClauses(action, scope);

        if (action.get(CreateIndexAction.Attr.clustered, false)) {
            clauses.insertBefore("INDEX", "CLUSTERED");
        } else {
            clauses.insertBefore("INDEX", "NONCLUSTERED");
        }

        String tablespace = action.get(CreateIndexAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(tablespace, "ON "+tablespace);
        }

        return clauses;
    }
}
