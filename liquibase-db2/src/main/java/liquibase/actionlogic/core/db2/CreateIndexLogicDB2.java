package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.core.CreateIndexAction;
import liquibase.actionlogic.core.CreateIndexLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateIndexLogicDB2 extends CreateIndexLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    protected StringClauses generateSql(CreateIndexAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        if (ObjectUtil.defaultIfEmpty(action.clustered, false)){
            clauses.append("CLUSTER");
        }


        return clauses;
    }
}
