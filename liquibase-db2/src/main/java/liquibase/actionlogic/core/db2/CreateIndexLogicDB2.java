package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateIndexAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;

public class CreateIndexLogicDB2 extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.get(CreateIndexAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        if (action.get(CreateIndexAction.Attr.clustered, false)){
            clauses.append("CLUSTER");
        }


        return clauses;
    }
}
