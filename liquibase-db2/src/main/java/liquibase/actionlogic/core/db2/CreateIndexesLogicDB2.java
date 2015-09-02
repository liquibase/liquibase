package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.core.CreateIndexesLogic;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.structure.core.Index;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateIndexesLogicDB2 extends CreateIndexesLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DB2Database.class;
    }

    @Override
    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        StringClauses clauses = super.generateSql(index, action, scope);

        String tablespace = index.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "IN " + tablespace);
        }

        if (ObjectUtil.defaultIfEmpty(index.clustered, false)){
            clauses.append("CLUSTER");
        }


        return clauses;
    }
}
