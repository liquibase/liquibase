package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.core.CreateIndexesLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.Index;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateIndexesLogicMSSQL extends CreateIndexesLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        StringClauses clauses = super.generateSql(index, action, scope);

        if (ObjectUtil.defaultIfEmpty(index.clustered, false)) {
            clauses.insertBefore("INDEX", "CLUSTERED");
        } else {
            clauses.insertBefore("INDEX", "NONCLUSTERED");
        }

        String tablespace = index.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "ON " + tablespace);
        }

        return clauses;
    }
}
