package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.util.ObjectUtil;

public class CreateIndexLogicMSSQL extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.clustered, false)) {
            clauses.insertBefore("INDEX", "CLUSTERED");
        } else {
            clauses.insertBefore("INDEX", "NONCLUSTERED");
        }

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "ON " + tablespace);
        }

        return clauses;
    }
}
